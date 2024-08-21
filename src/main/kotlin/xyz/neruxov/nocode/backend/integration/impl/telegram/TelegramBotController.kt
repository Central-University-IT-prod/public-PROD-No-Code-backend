package xyz.neruxov.nocode.backend.integration.impl.telegram

import com.github.kshashov.telegram.api.TelegramMvcController
import com.github.kshashov.telegram.api.bind.annotation.BotController
import com.github.kshashov.telegram.api.bind.annotation.BotRequest
import com.github.kshashov.telegram.api.bind.annotation.request.MessageRequest
import com.github.kshashov.telegram.config.TelegramBotGlobalProperties
import com.github.kshashov.telegram.config.TelegramBotGlobalPropertiesConfiguration
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.ChatMember
import com.pengrad.telegrambot.model.ChatMemberUpdated
import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.model.request.ParseMode
import com.pengrad.telegrambot.request.*
import org.springframework.beans.factory.annotation.Value
import xyz.neruxov.nocode.backend.data.attachment.enums.AttachmentType
import xyz.neruxov.nocode.backend.data.attachment.model.PostAttachment
import xyz.neruxov.nocode.backend.data.integration.enums.IntegrationType
import xyz.neruxov.nocode.backend.data.integration.model.IntegrationGroup
import xyz.neruxov.nocode.backend.data.integration.repo.IntegrationGroupRepository
import xyz.neruxov.nocode.backend.data.organization.repo.OrganizationRepository
import xyz.neruxov.nocode.backend.data.post.repo.MessageRepository
import xyz.neruxov.nocode.backend.data.post.repo.PostRepository
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.jvm.optionals.getOrElse

@BotController
class TelegramBotController(

    @Value("\${telegram.bot.token}")
    private val token: String,

    val organizationRepository: OrganizationRepository,
    val integrationGroupRepository: IntegrationGroupRepository,
    val messageRepository: MessageRepository,
    val postRepository: PostRepository,

    @Value("\${integration.telegram.helper-user.id}")
    private val helperUserId: Long,

    ) : TelegramMvcController, TelegramBotGlobalPropertiesConfiguration {

    @Value("\${service.telegram.statistics.url}")
    val url: String = ""

    val codesList = mutableMapOf<String, Long>()

    private val httpClient = HttpClient.newHttpClient()
    private val awaitingList = mutableMapOf<Long, Long>()
    lateinit var telegramBot: TelegramBot

    @MessageRequest(value = ["/start *"])
    fun onStart(message: Message, bot: TelegramBot): SendMessage {
        val code = message.text().split(" ")[1]
        val organizationId = codesList[code]
            ?: return SendMessage(message.chat().id(), "Передана невалидная или устаревшая ссылка.")

        awaitingList[message.from().id()] = organizationId
        codesList.remove(code)

        return SendMessage(
            message.chat().id(),
            "Добавьте меня в канал, к которому хотите привязать организацию, и дайте права на публикацию сообщений и добавление участников (необходимо для подсчета аналитики)."
        )
    }

    @BotRequest
    fun onUpdate(update: Update, bot: TelegramBot): SendMessage? {
        if (update.myChatMember() != null) {
            return onMyChatMemberUpdated(update.myChatMember(), bot)
        }

        if (update.myChatMember() != null) {
            return onChatMemberUpdated(update.chatMember(), bot)
        }

        return null
    }

    private fun onMyChatMemberUpdated(myChatMember: ChatMemberUpdated, bot: TelegramBot): SendMessage? {
        val newChatMember = myChatMember.newChatMember()
        val chat = myChatMember.chat()
        val user = myChatMember.from()

        val telegramGroup = integrationGroupRepository.findByGroupId(chat.id())

        val hasEnoughPerms = try {
            newChatMember.canPostMessages() && newChatMember.canInviteUsers()
        } catch (e: Exception) {
            false
        }

        if (telegramGroup.isPresent && !hasEnoughPerms) {
            val telegramGroupObject = telegramGroup.get()
            val messages = messageRepository.findAllByIntegrationGroupId(telegramGroupObject.id)
            val posts = postRepository.findAllByIntegrationGroupId(telegramGroupObject.id)

            posts.forEach {
                it.integrationGroups.remove(telegramGroupObject)
            }

            postRepository.saveAll(posts)
            messageRepository.deleteAll(messages)
            integrationGroupRepository.deleteById(telegramGroupObject.id)

            return SendMessage(
                user.id(),
                "Вы забрали права публиковать сообщения или добавлять пользователей в канал ${chat.title()}. Канал отвязан от организации."
            )
        }

        if (!telegramGroup.isPresent && hasEnoughPerms) {
            val organization = awaitingList[myChatMember.from().id()]
                ?: return null

            val organizationObject = organizationRepository.findById(organization)
                .getOrElse { return SendMessage(myChatMember.from().id(), "Организация не найдена") }

            val groupAvatar: ByteArray? = try {
                getChatAvatar(chat.id(), bot)
            } catch (e: Exception) {
                null
            }

            integrationGroupRepository.save(
                IntegrationGroup(
                    type = IntegrationType.TELEGRAM,
                    groupId = chat.id(),
                    groupAvatar = groupAvatar,
                    groupName = chat.title(),
                    organizationId = organizationObject.id
                )
            )

            Thread {
                Thread.sleep(5000)
                inviteHelperUser(chat.id(), bot)
            }.start()

            return SendMessage(
                myChatMember.from().id(),
                "Канал ${chat.title()} успешно привязан к организации ${organizationObject.name}."
            )
        }

        return null
    }

    private fun onChatMemberUpdated(chatMember: ChatMemberUpdated, bot: TelegramBot): SendMessage? {
        val newChatMember = chatMember.newChatMember()
        val chat = chatMember.chat()
        val user = chatMember.from()

        val telegramGroup = integrationGroupRepository.findByGroupId(chat.id())
        if (newChatMember.user().id() != helperUserId) {
            return null
        }

        if (newChatMember.status() == ChatMember.Status.left || newChatMember.status() == ChatMember.Status.kicked) {
            inviteHelperUser(chat.id(), bot)
        }

        return null
    }

    private fun getChatAvatar(chatId: Long, bot: TelegramBot): ByteArray? {
        val response = bot.execute(GetChat(chatId))
        val chat = response.chat()
        if (chat.photo() == null) {
            return null
        }

        val file = bot.execute(GetFile(chat.photo().bigFileId())).file()
        return bot.getFileContent(file)
    }

    private fun inviteHelperUser(chatId: Long, bot: TelegramBot) {
        val inviteLink = bot.execute(
            CreateChatInviteLink(chatId)
                .memberLimit(1)
        ).chatInviteLink().inviteLink()

        val inviteCode = inviteLink.split("+").last()

        httpClient.send(
            HttpRequest.newBuilder()
                .uri(URI.create("$url/join?hash=$inviteCode"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build(),
            HttpResponse.BodyHandlers.ofString()
        )
    }

    fun postMessage(chatId: Long, content: String, attachments: List<PostAttachment>): Int {
        val mutableAttachments = attachments.toMutableList()
        mutableAttachments.filter { it.type == AttachmentType.VIDEO_NOTE }.forEach { note ->
            telegramBot.execute(
                SendVideoNote(chatId, note.body)
            )

            mutableAttachments.remove(note)
        }

        return if (mutableAttachments.isNotEmpty()) {
            val mediaGroup = mutableAttachments.map { it.toInputMedia() }.toTypedArray()
            mediaGroup[mediaGroup.size - 1] = mediaGroup[mediaGroup.size - 1]
                .parseMode(ParseMode.HTML)
                .caption(content)

            val response = telegramBot.execute(
                SendMediaGroup(
                    chatId,
                    *mediaGroup
                )
            )

            response.messages().last().messageId()
        } else {
            val response = telegramBot.execute(
                SendMessage(chatId, content)
                    .parseMode(ParseMode.HTML)
            )

            response.message().messageId()
        }
    }

    override fun getToken() = token

    override fun configure(builder: TelegramBotGlobalProperties.Builder) {
        builder.processBot(token) {
            telegramBot = it
        }
    }

    fun getMembersCount(groupId: Long) = telegramBot.execute(GetChatMemberCount(groupId)).count()
}
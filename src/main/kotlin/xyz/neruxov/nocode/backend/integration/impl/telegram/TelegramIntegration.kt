package xyz.neruxov.nocode.backend.integration.impl.telegram

import com.pengrad.telegrambot.request.GetMe
import org.springframework.stereotype.Service
import xyz.neruxov.nocode.backend.data.attachment.model.PostAttachment
import xyz.neruxov.nocode.backend.data.organization.model.Organization
import xyz.neruxov.nocode.backend.integration.Integration
import xyz.neruxov.nocode.backend.util.RandomUtil

@Service
class TelegramIntegration(
    val telegramBotController: TelegramBotController
) : Integration {

    override fun getDisplayName() = "Telegram"

    override fun getAddLink(organization: Organization): String {
        val code = RandomUtil.generateRandomString(16)
        telegramBotController.codesList[code] = organization.id

        val username = telegramBotController.telegramBot.execute(GetMe()).user().username()

        return "https://t.me/$username?start=$code"
    }

    override fun postMessage(
        chatId: Long,
        content: String,
        attachments: List<PostAttachment>
    ): Int =
        telegramBotController.postMessage(chatId, content, attachments)

}
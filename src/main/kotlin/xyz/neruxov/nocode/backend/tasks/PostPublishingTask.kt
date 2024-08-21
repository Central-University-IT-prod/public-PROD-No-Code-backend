package xyz.neruxov.nocode.backend.tasks

import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import xyz.neruxov.nocode.backend.data.integration.enums.IntegrationType
import xyz.neruxov.nocode.backend.data.links.model.Link
import xyz.neruxov.nocode.backend.data.links.repo.LinkRepository
import xyz.neruxov.nocode.backend.data.post.model.Message
import xyz.neruxov.nocode.backend.data.post.model.Post
import xyz.neruxov.nocode.backend.data.post.repo.MessageRepository
import xyz.neruxov.nocode.backend.data.post.repo.PostRepository
import xyz.neruxov.nocode.backend.integration.impl.telegram.TelegramIntegration
import xyz.neruxov.nocode.backend.service.NotificationService
import xyz.neruxov.nocode.backend.util.RandomUtil
import java.util.*

@Service
@EnableAsync
class PostPublishingTask(
    val postRepository: PostRepository,
    val telegramIntegration: TelegramIntegration,
    val messageRepository: MessageRepository,
    val linkRepository: LinkRepository,
    val pushNotificationService: NotificationService
) {

    private val hrefRegex = """<a[^>]*href=["'](.*?)["']""".toRegex()

    @Async
    @Scheduled(fixedRate = 60 * 1000)
    fun run() {
        val posts = postRepository.findPostsByDateRange(
            (Date().clone() as Date).apply { time -= 60000L },
            Date()
        )

        posts.forEach {
            publishPost(it)
        }
    }

    fun publishPost(post: Post) {
        val hrefs = hrefRegex.findAll(post.body)
            .map { it.groupValues[1] }.toList()
            .distinct()

        post.organization.members.forEach {
            if (it.user.mobileDevice != null) {
                pushNotificationService.sendNotification(
                    it.user.mobileDevice!!.token,
                    "Опубликован пост",
                    "Пост \"${post.name}\" успешно опубликован!"
                )
            }
        }

        post.integrationGroups.forEach { integrationGroup ->
            when (integrationGroup.type) {
                IntegrationType.TELEGRAM -> {
                    try {
                        val links = hrefs.map {
                            Link(
                                0,
                                if (!it.startsWith("http")) "http://$it" else it,
                                0,
                                RandomUtil.generateRandomString(8),
                                0
                            )
                        }

                        val newBody = hrefs.fold(post.body) { body, href ->
                            body.replace(
                                href,
                                "https://ноу-код.рф/api/link?code=" + links.find { it.url.endsWith(href) }!!.code
                            )
                        }

                        val id = telegramIntegration.postMessage(integrationGroup.groupId, newBody, post.attachments)

                        linkRepository.saveAll(links.map { it.copy(messageId = id.toLong()) })

                        messageRepository.save(
                            Message(
                                integrationGroup = integrationGroup,
                                messageId = id.toLong(),
                                postId = post.id
                            )
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        println("Failed to publish post to Telegram: ${e.message}")
                    }
                }
            }
        }
    }

}
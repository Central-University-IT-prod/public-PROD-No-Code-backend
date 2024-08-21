package xyz.neruxov.nocode.backend.service

import org.springframework.stereotype.Service
import xyz.neruxov.nocode.backend.data.attachment.enums.AttachmentType
import xyz.neruxov.nocode.backend.data.attachment.model.PostAttachment
import xyz.neruxov.nocode.backend.data.attachment.repo.PostAttachmentRepository
import xyz.neruxov.nocode.backend.data.integration.enums.IntegrationType
import xyz.neruxov.nocode.backend.data.integration.repo.IntegrationGroupRepository
import xyz.neruxov.nocode.backend.data.organization.repo.OrganizationRepository
import xyz.neruxov.nocode.backend.data.post.model.Post
import xyz.neruxov.nocode.backend.data.post.repo.MessageRepository
import xyz.neruxov.nocode.backend.data.post.repo.PostRepository
import xyz.neruxov.nocode.backend.data.post.request.CreatePostRequest
import xyz.neruxov.nocode.backend.data.post.request.EditPostRequest
import xyz.neruxov.nocode.backend.data.post.request.UploadAttachmentsRequest
import xyz.neruxov.nocode.backend.data.post.util.fromBase64
import xyz.neruxov.nocode.backend.data.user.model.User
import xyz.neruxov.nocode.backend.exception.type.StatusCodeException
import xyz.neruxov.nocode.backend.integration.impl.telegram.TelegramIntegration
import xyz.neruxov.nocode.backend.util.MessageResponse
import java.util.*
import kotlin.jvm.optionals.getOrElse

@Service
class PostService(
    private val postRepository: PostRepository,
    private val organizationRepository: OrganizationRepository,
    private val postAttachmentRepository: PostAttachmentRepository,
    private val integrationGroupRepository: IntegrationGroupRepository,
    private val telegramStatisticsService: TelegramStatisticsService,
    private val messageRepository: MessageRepository,
    val telegramIntegration: TelegramIntegration
) {

    fun getPostById(id: Long) = MessageResponse.success(postRepository.findById(id).getOrElse {
        throw StatusCodeException(404, "Пост не найден")
    }.toMap())

    fun getPostsByDateRange(fromDate: Long, toDate: Long, organizationId: Long): Any {
        if (toDate < fromDate) {
            throw StatusCodeException(400, "Дата публикации должна быть в будущем")
        }

        return MessageResponse.success(
            postRepository.findOrganizationPostsByDateRange(
            Date(fromDate),
            Date(toDate),
            organizationId
        ).map { it.toMap() })
    }

    fun createPost(request: CreatePostRequest, user: User): Any {
        val uploadDate = Date(request.uploadAt)
        if (uploadDate.before(Date())) {
            throw StatusCodeException(400, "Дата загрузки не может быть в прошлом")
        }

        val organization = organizationRepository.findById(request.organizationId).getOrElse {
            throw StatusCodeException(404, "Организация не найдена")
        }

        val integrations = request.integrationIds.map {
            integrationGroupRepository.findById(it).getOrElse {
                throw StatusCodeException(400, "Интеграция с номером $it не найдена")
            }
        }

        if (integrations.isEmpty()) {
            throw StatusCodeException(400, "Необходимо указать хотя бы одну интеграцию")
        }

        if (integrations.any { it.organizationId != organization.id }) {
            throw StatusCodeException(400, "Интеграция не принадлежит к данной организации")
        }

        val post = postRepository.save(
            Post(
                name = request.name,
                body = request.body,
                creator = user,
                organization = organization,
                uploadDate = uploadDate,
                tags = request.tags.toTypedArray()
            )
        )

        val attachments = request.attachments.map {
            postAttachmentRepository.findById(it).getOrElse {
                throw StatusCodeException(400, "Вложение с номером $it не найдено")
            }.apply { postId = post.id }
        }

        post.attachments.addAll(attachments)
        post.integrationGroups.addAll(integrations)

        postRepository.save(post)

        return MessageResponse.success(mapOf("id" to post.id))
    }

    fun uploadAttachments(
        organizationId: Long,
        attachments: List<UploadAttachmentsRequest.AttachmentBody>
    ): Any { // base64
        val organization = organizationRepository.findById(organizationId).getOrElse {
            throw StatusCodeException(404, "Organization not found")
        }

        if (attachments.size > 10) {
            throw StatusCodeException(400, "Достигнут лимит вложений (не более 10)")
        }

        return MessageResponse.success(postAttachmentRepository.saveAll(attachments.map { attachmentBody ->
            val it = attachmentBody.body
            val isVideoNote = attachmentBody.isVideoNote

            val split = it.split(";")
            if (split.size < 2) {
                throw StatusCodeException(400, "Формат вложения $it некорректен")
            }

            val type = split[0].split(":")[1].split("/")[0].toLowerCase()
            val dataString = split[1].removePrefix("base64,")

            val attachmentType = if (isVideoNote) {
                AttachmentType.VIDEO_NOTE
            } else {
                when (type) {
                    "image" -> AttachmentType.PHOTO
                    "video" -> AttachmentType.VIDEO
                    else -> throw StatusCodeException(400, "Тип вложения $type не поддерживается")
                }
            }

            val data = try {
                dataString.fromBase64()
            } catch (ex: Exception) {
                throw StatusCodeException(400, "Содержание вложения с номером $it некорректно")
            }

            PostAttachment(
                body = data,
                type = attachmentType,
                organizationId = organization.id,
                postId = 0
            )
        }).map { it.id })
    }

    fun getAttachmentById(user: User, id: Long): Any {
        val attachment = postAttachmentRepository.findById(id).getOrElse {
            throw StatusCodeException(404, "Вложение не найдено")
        }

        if (user.organizationMembers.all { it.organization.id != attachment.organizationId }) {
            throw StatusCodeException(403, "Недостаточно прав на получение этого вложения")
        }

        return MessageResponse.success(attachment.toMap())
    }

    fun editPost(user: User, id: Long, request: EditPostRequest): Any {
        val post = postRepository.findById(id).getOrElse {
            throw StatusCodeException(404, "Публикация не найдена")
        }

        if (request.name != null) {
            post.name = request.name
        }

        if (request.body != null) {
            post.body = request.body
        }

        if (request.tags != null) {
            post.tags = request.tags.toTypedArray()
        }

        if (request.uploadAt != null) {
            val it = request.uploadAt
            val uploadDate = Date(it)
            if (uploadDate.before(Date())) {
                throw StatusCodeException(400, "Дата загрузки не может быть в прошлом")
            }

            post.uploadDate = uploadDate
        }

        if (request.attachmentIds != null) {
            val it = request.attachmentIds
            if (it.size > 10) {
                throw StatusCodeException(400, "Достигнут лимит вложений (не более 10)")
            }

            val attachments = it.map { id ->
                postAttachmentRepository.findById(id).getOrElse {
                    throw StatusCodeException(400, "Вложение с номером $id не найдено")
                }
            }

            post.attachments.clear()
            post.attachments.addAll(attachments)
        }

        if (request.integrationIds.isNotEmpty()) {
            val it = request.integrationIds
            val integrations = it.map { id ->
                integrationGroupRepository.findById(id).getOrElse {
                    throw StatusCodeException(400, "Интеграция с номером $id не найдена ")
                }
            }

            post.integrationGroups.clear()
            post.integrationGroups.addAll(integrations)
        }

        postRepository.save(post)
        return MessageResponse.success()
    }

    fun cancelPost(user: User, id: Long): Any {
        val post = postRepository.findById(id).getOrElse {
            throw StatusCodeException(404, "Публикация не найден")
        }

        if (user.organizationMembers.all { it.organization.id != post.organization.id }) {
            throw StatusCodeException(403, "У вас нет прав просматривать статистику данной публикации")
        }

        post.organization.posts.removeIf { it.id == id }
        post.attachments.clear()
        post.integrationGroups.clear()
        post.messages.clear()

        postRepository.save(post)
        postRepository.delete(post)

        return MessageResponse.success()
    }

    fun getStatistics(user: User, id: Long): Any {
        val post = postRepository.findById(id).getOrElse {
            throw StatusCodeException(404, "Публикация не найдена")
        }

        if (!user.organizationMembers.any { it.organization.id == post.organization.id }) {
            throw StatusCodeException(403, "У вас нет прав просматривать статистику данной публикации")
        }

        if (!post.isUploaded()) {
            throw StatusCodeException(400, "Публикация не опубликована")
        }

        return MessageResponse.success(post.integrationGroups.mapNotNull {
            val message = messageRepository.findMessageByPostIdAndIntegrationGroup(post.id, it).getOrElse {
                return@mapNotNull null
            }

            when (it.type) {
                IntegrationType.TELEGRAM -> mapOf(
                    "integrationId" to it.id,
                    "integrationName" to it.groupName,
                    "statistics" to telegramStatisticsService.getStatistics(it, message.messageId)
                )
            }
        })
    }

    fun testPost(user: User, integrationId: Long, organizationId: Long, attachments: List<Long>, body: String): Any {
        val integration = integrationGroupRepository.findById(integrationId).getOrElse {
            throw StatusCodeException(404, "Интеграция не найдена")
        }

        if (organizationId != integration.organizationId) {
            throw StatusCodeException(403, "Публикация не принадлежит к данной организации")
        }

        val attachmentObjects = attachments.map {
            postAttachmentRepository.findById(it).getOrElse {
                throw StatusCodeException(400, "Вложение с номером $it не найдено")
            }
        }

        when (integration.type) {
            IntegrationType.TELEGRAM -> telegramIntegration.postMessage(
                integration.groupId,
                body,
                attachmentObjects
            )
        }

        return MessageResponse.success()
    }

    fun publishPost(user: User, id: Long): MessageResponse {
        val post = postRepository.findById(id).getOrElse {
            throw StatusCodeException(404, "Публикация не найдена")
        }

        if (post.isUploaded()) {
            throw StatusCodeException(400, "Публикация уже опубликована")
        }

        if (user.organizationMembers.none { it.organization.id == post.organization.id }) {
            throw StatusCodeException(403, "У вас нет прав на публикацию данной публикации")
        }

        post.uploadDate = Date()
        postRepository.save(post)

        return MessageResponse.success()
    }

}
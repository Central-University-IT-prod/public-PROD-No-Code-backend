package xyz.neruxov.nocode.backend.integration

import xyz.neruxov.nocode.backend.data.attachment.model.PostAttachment
import xyz.neruxov.nocode.backend.data.organization.model.Organization

interface Integration {

    fun getDisplayName(): String

    fun getAddLink(organization: Organization): String

    fun postMessage(chatId: Long, content: String, attachments: List<PostAttachment>): Int

}
package xyz.neruxov.nocode.backend.data.post.request

data class UploadAttachmentsRequest(
    val organizationId: Long,
    val attachmentsBody: List<AttachmentBody> // base64
) {

    data class AttachmentBody(
        val isVideoNote: Boolean,
        val body: String // base64
    )

}
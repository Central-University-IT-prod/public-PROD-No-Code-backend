package xyz.neruxov.nocode.backend.data.attachment.model

import jakarta.persistence.*
import xyz.neruxov.nocode.backend.data.attachment.enums.AttachmentType
import xyz.neruxov.nocode.backend.data.post.util.base64

@Entity
@Table(name = "posts_attachments")
data class PostAttachment(

    @Id
    @GeneratedValue
    val id: Long = 0,

    val body: ByteArray,

    val organizationId: Long,

    var postId: Long = -1,

    @Enumerated(EnumType.STRING)
    val type: AttachmentType

) {

    fun toInputMedia() = type.toInputMedia(body)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PostAttachment

        if (id != other.id) return false
        if (!body.contentEquals(other.body)) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + body.contentHashCode()
        result = 31 * result + type.hashCode()
        return result
    }

    fun toMap() = mapOf(
        "id" to id,
        "body" to body.base64(),
        "type" to type.name.lowercase()
    )

}
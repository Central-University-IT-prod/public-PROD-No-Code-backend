package xyz.neruxov.nocode.backend.data.attachment.enums

import com.pengrad.telegrambot.model.request.InputMedia
import com.pengrad.telegrambot.model.request.InputMediaDocument
import com.pengrad.telegrambot.model.request.InputMediaPhoto
import com.pengrad.telegrambot.model.request.InputMediaVideo

enum class AttachmentType {
    PHOTO,
    VIDEO,
    VIDEO_NOTE,
    DOCUMENT;

    fun toInputMedia(bytes: ByteArray): InputMedia<*> {
        return when (this) {
            PHOTO -> InputMediaPhoto(bytes)
            VIDEO -> InputMediaVideo(bytes)
            DOCUMENT -> InputMediaDocument(bytes)
            VIDEO_NOTE -> throw IllegalArgumentException("Video note should be handled separately")
        }
    }

    companion object {

        fun fromString(type: String): AttachmentType {
            return when (type.lowercase()) {
                "photo" -> PHOTO
                "video" -> VIDEO
                "document" -> DOCUMENT
                else -> throw IllegalArgumentException("Unknown attachment type: $type")
            }
        }

    }

}
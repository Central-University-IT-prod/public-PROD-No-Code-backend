package xyz.neruxov.nocode.backend.data.post.request

import jakarta.validation.constraints.Size

data class TestPostRequest(

    @field:Size(min = 1, max = 1024, message = "Текст публикации должен быть от 1 до 1024 символов")
    val body: String,

    val organizationId: Long,

    val attachments: List<Long>, // ids

    val integrationId: Long,

    )

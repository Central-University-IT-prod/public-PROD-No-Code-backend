package xyz.neruxov.nocode.backend.data.post.request

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size

data class CreatePostRequest(

    @field:Size(min = 3, max = 200, message = "Название публикации должно быть от 3 до 200 символов")
    val name: String,

    @field:Size(min = 1, max = 1024, message = "Текст публикации должен быть от 1 до 1024 символов")
    val body: String,

    val organizationId: Long,

    @field:Min(0, message = "Дата загрузки не может быть отрицательной")
    val uploadAt: Long,

    val attachments: List<Long>, // ids

    val integrationIds: List<Long>,

    val tags: List<String>,

)

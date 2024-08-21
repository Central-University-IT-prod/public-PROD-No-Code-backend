package xyz.neruxov.nocode.backend.data.user.request

import jakarta.validation.constraints.Size

data class EditUserRequest(

    @field:Size(min = 1, max = 64, message = "Имя должно быть от 1 до 64 символов")
    val fullName: String

)
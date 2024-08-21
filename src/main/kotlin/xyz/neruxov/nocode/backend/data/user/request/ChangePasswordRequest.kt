package xyz.neruxov.nocode.backend.data.user.request

import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class ChangePasswordRequest(

    val oldPassword: String,

    @field:Size(min = 8, max = 64, message = "Пароль должен быть от 8 до 64 символов")
    @field:Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,64}\$",
        message = "Пароль должен содержать хотя бы одну цифру, одну строчную и одну заглавную букву"
    )
    val newPassword: String

)
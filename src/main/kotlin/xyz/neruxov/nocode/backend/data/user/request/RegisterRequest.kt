package xyz.neruxov.nocode.backend.data.user.request

import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class RegisterRequest(

    @field:Size(min = 3, max = 32, message = "Имя должно быть от 3 до 32 символов")
    @field:Pattern(regexp = "[a-zA-Z0-9]+", message = "Имя пользователя может содержать только буквы и цифры")
    val username: String,

    @field:Size(min = 1, max = 64, message = "Имя должно быть от 1 до 64 символов")
    val fullName: String,

    @field:Size(min = 8, max = 64, message = "Пароль должен быть от 8 до 64 символов")
    @field:Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,64}\$",
        message = "Пароль должен содержать хотя бы одну цифру, одну строчную и одну заглавную букву"
    )
    val password: String

)

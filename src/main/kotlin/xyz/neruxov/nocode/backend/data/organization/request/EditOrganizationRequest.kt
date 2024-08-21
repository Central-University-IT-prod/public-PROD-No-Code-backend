package xyz.neruxov.nocode.backend.data.organization.request

import jakarta.validation.constraints.Size

data class EditOrganizationRequest(

    @field:Size(min = 3, max = 128, message = "Название должно быть от 3 до 128 символов")
    val name: String?,

    @field:Size(min = 3, max = 1024, message = "Описание должно быть от 3 до 1024 символов")
    val description: String?

)
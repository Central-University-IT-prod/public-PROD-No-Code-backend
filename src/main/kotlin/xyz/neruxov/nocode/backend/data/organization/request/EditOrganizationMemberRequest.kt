package xyz.neruxov.nocode.backend.data.organization.request

import xyz.neruxov.nocode.backend.data.organization.enums.OrganizationRole

data class EditOrganizationMemberRequest(
    val role: OrganizationRole
)
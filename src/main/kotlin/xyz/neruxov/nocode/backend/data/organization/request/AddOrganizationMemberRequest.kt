package xyz.neruxov.nocode.backend.data.organization.request

import xyz.neruxov.nocode.backend.data.organization.enums.OrganizationRole

data class AddOrganizationMemberRequest(
    val username: String,
    val role: OrganizationRole
)
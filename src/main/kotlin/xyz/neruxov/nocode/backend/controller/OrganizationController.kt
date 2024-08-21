package xyz.neruxov.nocode.backend.controller

import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import xyz.neruxov.nocode.backend.data.integration.enums.IntegrationType
import xyz.neruxov.nocode.backend.data.organization.request.AddOrganizationMemberRequest
import xyz.neruxov.nocode.backend.data.organization.request.CreateOrganizationRequest
import xyz.neruxov.nocode.backend.data.organization.request.EditOrganizationMemberRequest
import xyz.neruxov.nocode.backend.data.organization.request.EditOrganizationRequest
import xyz.neruxov.nocode.backend.data.user.model.User
import xyz.neruxov.nocode.backend.service.OrganizationService

@RestController
@RequestMapping("/api/organization")
class OrganizationController(
    val organizationService: OrganizationService
) {

    @GetMapping("/{id}")
    fun getOrganizationById(@PathVariable id: Long) = organizationService.getOrganizationById(id)

    @PostMapping
    fun createOrganization(
        @AuthenticationPrincipal user: User,
        @RequestBody @Valid request: CreateOrganizationRequest
    ) = organizationService.createOrganization(user, request.name, request.description)

    @PatchMapping("/{id}")
    fun editOrganization(
        @PathVariable id: Long,
        @AuthenticationPrincipal user: User,
        @RequestBody request: EditOrganizationRequest
    ) = organizationService.editOrganization(user, id, request.name, request.description)

    @DeleteMapping("/{id}")
    fun deleteOrganization(
        @PathVariable id: Long,
        @AuthenticationPrincipal user: User
    ) = organizationService.deleteOrganization(user, id)

    @GetMapping("/{id}/members")
    fun getOrganizationMembers(
        @PathVariable id: Long,
    ) = organizationService.getOrganizationMembers(id)

    @PutMapping("/{id}/members")
    fun addOrganizationMember(
        @PathVariable id: Long,
        @AuthenticationPrincipal user: User,
        @RequestBody request: AddOrganizationMemberRequest
    ) = organizationService.addOrganizationMember(user, id, request.username, request.role)

    @PatchMapping("/{id}/members/{username}")
    fun editOrganizationMember(
        @PathVariable id: Long,
        @AuthenticationPrincipal user: User,
        @PathVariable username: String,
        @RequestBody request: EditOrganizationMemberRequest
    ) = organizationService.editOrganizationMember(user, id, username, request.role)

    @DeleteMapping("/{id}/members/{username}")
    fun removeOrganizationMember(
        @PathVariable id: Long,
        @AuthenticationPrincipal user: User,
        @PathVariable username: String
    ) = organizationService.removeOrganizationMember(user, id, username)

    @GetMapping("/{id}/integrations")
    fun getOrganizationIntegrations(
        @PathVariable id: Long,
        @AuthenticationPrincipal user: User,
    ) = organizationService.getOrganizationIntegrations(user, id)

    @PostMapping("/{id}/integrations/{type}/add-link")
    fun getOrganizationIntegrationAddLink(
        @PathVariable id: Long,
        @AuthenticationPrincipal user: User,
        @PathVariable type: IntegrationType
    ) = organizationService.getOrganizationIntegrationAddLink(user, id, type)

}
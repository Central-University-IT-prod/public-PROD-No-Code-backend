package xyz.neruxov.nocode.backend.controller

import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import xyz.neruxov.nocode.backend.data.user.model.User
import xyz.neruxov.nocode.backend.data.user.request.EditUserRequest
import xyz.neruxov.nocode.backend.service.OrganizationService
import xyz.neruxov.nocode.backend.service.UserService
import xyz.neruxov.nocode.backend.util.MessageResponse

@RestController
@RequestMapping("/api/user")
class UserController(
    private val userService: UserService,
    private val organizationService: OrganizationService
) {

    @GetMapping("/search")
    fun searchUsers(
        @RequestParam query: String
    ) = userService.searchUsers(query)

    @GetMapping("/{id}")
    fun getUserById(
        @PathVariable id: Long
    ) = userService.getUserById(id)

    @GetMapping("/me")
    fun getSelfUser(
        @AuthenticationPrincipal user: User
    ) = MessageResponse.success(user.toMap())

    @PatchMapping("/me")
    fun editSelfUser(
        @AuthenticationPrincipal user: User,
        @RequestBody @Valid request: EditUserRequest
    ) = userService.editUser(user.id, request.fullName)

    @DeleteMapping("/me")
    fun deleteSelfUser(
        @AuthenticationPrincipal user: User
    ) = userService.deleteUser(user.id)

    @GetMapping("/me/organizations")
    fun getSelfOrganizations(
        @AuthenticationPrincipal user: User
    ) = organizationService.getUserOrganizations(user)

}
package xyz.neruxov.nocode.backend.controller

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import xyz.neruxov.nocode.backend.data.user.model.User
import xyz.neruxov.nocode.backend.data.user.request.ChangePasswordRequest
import xyz.neruxov.nocode.backend.data.user.request.RegisterRequest
import xyz.neruxov.nocode.backend.data.user.request.SignInRequest
import xyz.neruxov.nocode.backend.service.AuthService

/**
 * @author <a href="https://github.com/Neruxov">Neruxov</a>
 */
@RestController
@RequestMapping("/api/auth")
class AuthController(
    val authService: AuthService
) {

    @PostMapping("/sign-in")
    fun signIn(
        @RequestBody request: SignInRequest
    ) = authService.signIn(request.username, request.password)

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(
        @RequestBody @Valid request: RegisterRequest
    ) = authService.register(request.username, request.fullName, request.password)

    @PostMapping("/refresh-token")
    fun refreshToken(
        request: HttpServletRequest
    ) = authService.refreshToken(request)

    @PostMapping("/change-password")
    fun changePassword(
        @RequestBody @Valid request: ChangePasswordRequest,
        @AuthenticationPrincipal user: User
    ) = authService.changePassword(request.oldPassword, request.newPassword, user)
}
package xyz.neruxov.nocode.backend.service

import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import xyz.neruxov.nocode.backend.data.token.model.RefreshToken
import xyz.neruxov.nocode.backend.data.token.repo.RefreshTokenRepository
import xyz.neruxov.nocode.backend.data.user.model.User
import xyz.neruxov.nocode.backend.data.user.repo.UserRepository
import xyz.neruxov.nocode.backend.data.user.response.AuthorizationResponse
import xyz.neruxov.nocode.backend.exception.type.StatusCodeException
import xyz.neruxov.nocode.backend.util.MessageResponse
import java.util.*

/**
 * @author <a href="https://github.com/Neruxov">Neruxov</a>
 */
@Service
class AuthService(
    val userRepository: UserRepository,
    val jwtService: JwtService,
    val passwordEncoder: PasswordEncoder,
    val authenticationManager: AuthenticationManager,
    val refreshTokenRepository: RefreshTokenRepository
) {

    fun signIn(username: String, password: String): Any {
        val auth: Authentication
        try {
            auth = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(username, password)
            )
        } catch (e: AuthenticationException) {
            throw StatusCodeException(401, "Пара юзернейм/пароль не найдена")
        }

        val user = auth.principal as User
        return MessageResponse.success(
            generateAuthorizationResponse(user)
        )
    }

    fun register(username: String, fullName: String, password: String): Any {
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw StatusCodeException(409, "Пользователь с таким юзернеймом уже существует")
        }

        val user = User(
            0,
            username,
            fullName,
            passwordEncoder.encode(password),
        )

        userRepository.save(user)
        return MessageResponse.success()
    }

    fun refreshToken(request: HttpServletRequest): Any {
        val authHeader = request.getHeader("Authorization")
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw StatusCodeException(400, "Неверный запрос")
        }

        val jwt = authHeader.substring(7)
        val username: String?

        try {
            username = jwtService.extractUsername(jwt)!!
        } catch (e: Exception) {
            throw StatusCodeException(401, "Передан неверный JWT токен")
        }

        val user = userRepository.findByUsernameIgnoreCase(username)
            .orElseThrow { throw StatusCodeException(401, "Пользователь не найден") }

        refreshTokenRepository.findByToken(jwt).ifPresent {
            it.revoked = true
            refreshTokenRepository.save(it)
        }

        return MessageResponse.success(generateAuthorizationResponse(user))
    }

    fun changePassword(oldPassword: String, newPassword: String, user: User): Any {
        if (!passwordEncoder.matches(oldPassword, user.password)) {
            throw StatusCodeException(403, "Текущий пароль не сопвадает")
        }

        revokeUserRefreshTokens(user)
        user.password = newPassword
        user.lastPasswordChange = Date()

        userRepository.save(user)
        return MessageResponse.success()
    }

    private fun revokeUserRefreshTokens(user: User) {
        val validTokens = refreshTokenRepository.findAllValidByUser(user)
        validTokens.forEach {
            it.revoked = true
        }

        refreshTokenRepository.saveAll(validTokens)
    }

    private fun generateAuthorizationResponse(user: User): AuthorizationResponse {
        val accessToken = jwtService.generateAccessToken(user)
        val refreshToken = jwtService.generateRefreshToken(user)

        val expiry = jwtService.extractClaim(refreshToken) { it.expiration }

        refreshTokenRepository.save(RefreshToken(0, refreshToken, expiry!!, false, user))

        return AuthorizationResponse(accessToken, refreshToken)
    }

}
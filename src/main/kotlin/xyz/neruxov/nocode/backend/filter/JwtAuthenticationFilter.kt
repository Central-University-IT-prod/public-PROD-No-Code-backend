package xyz.neruxov.nocode.backend.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import xyz.neruxov.nocode.backend.config.SecurityConfig
import xyz.neruxov.nocode.backend.data.user.repo.UserRepository
import xyz.neruxov.nocode.backend.service.JwtService
import java.io.IOException
import java.util.*
import kotlin.jvm.optionals.getOrElse

/**
 * @author <a href="https://github.com/Neruxov">Neruxov</a>
 */
@Component
class JwtAuthenticationFilter(
    val jwtService: JwtService,
    val userRepository: UserRepository
) : OncePerRequestFilter() {

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            if (SecurityConfig.WHITELISTED_URLS.any { request.requestURI.startsWith(it.substringBefore("/**")) })
                return filterChain.doFilter(request, response)

            val authHeader = request.getHeader("Authorization")
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw CredentialsExpiredException("")
            }

            val jwt = authHeader.substring(7)

            val username: String?
            val issuedAt: Date?

            try {
                username = jwtService.extractUsername(jwt)
                issuedAt = jwtService.extractClaim(jwt) { claim -> return@extractClaim claim.issuedAt }

                val isRefresh =
                    jwtService.extractClaim(jwt) { claim -> return@extractClaim claim.getOrElse("refresh") { false } as Boolean }!!
                if (isRefresh) throw CredentialsExpiredException("")
            } catch (e: Exception) {
                throw CredentialsExpiredException("")
            }

            if (username != null && issuedAt != null && SecurityContextHolder.getContext().authentication == null) {
                val user = userRepository.findByUsernameIgnoreCase(username)
                    .getOrElse { throw CredentialsExpiredException("") }

                val isTokenValid = issuedAt.after(user.lastPasswordChange) && jwtService.isTokenValid(jwt, user)
                if (!isTokenValid) throw CredentialsExpiredException("")

                val authToken = UsernamePasswordAuthenticationToken(user, null, user.authorities)
                authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = authToken
            }
        } catch (_: Exception) {
        }
        filterChain.doFilter(request, response)
    }

}
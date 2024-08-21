package xyz.neruxov.nocode.backend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import xyz.neruxov.nocode.backend.entrypoint.AuthenticationEntryPointResolver
import xyz.neruxov.nocode.backend.filter.JwtAuthenticationFilter

/**
 * @author <a href="https://github.com/Neruxov">Neruxov</a>
 */
@Configuration
@EnableWebSecurity
class SecurityConfig(
    val authenticationProvider: AuthenticationProvider,
    val jwtAuthenticationFilter: JwtAuthenticationFilter,
    val authenticationEntryPointResolver: AuthenticationEntryPointResolver
) {

    companion object {

        val WHITELISTED_URLS = arrayOf(
            "/api/auth/**",
            "/api/link",
        )

    }

    @Bean
    @Throws(Exception::class)
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf { it.disable() }
            .cors {
                it.configurationSource { request ->
                    CorsConfiguration().applyPermitDefaultValues().apply {
                        allowedHeaders = listOf("*")
                        allowedMethods = listOf("*")
                        allowedOrigins = listOf("*")
                    }
                }
            }
            .authorizeHttpRequests {
                WHITELISTED_URLS.forEach { url -> it.requestMatchers(url).permitAll() }
                it.anyRequest().authenticated()
            }
            .exceptionHandling {
                it.accessDeniedHandler { request, response, accessDeniedException ->
                    response.sendError(
                        401,
                        "Вы не вошли в аккаунт"
                    )
                }
                it.authenticationEntryPoint(authenticationEntryPointResolver)
            }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
        return http.build()
    }

}
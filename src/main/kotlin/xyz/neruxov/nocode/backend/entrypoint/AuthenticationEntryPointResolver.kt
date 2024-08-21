package xyz.neruxov.nocode.backend.entrypoint

import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerExceptionResolver
import java.io.IOException

@Component
class AuthenticationEntryPointResolver(
    @Qualifier("handlerExceptionResolver") private val resolver: HandlerExceptionResolver
) : AuthenticationEntryPoint {

    @Throws(IOException::class, ServletException::class)
    override fun commence(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        exception: AuthenticationException
    ) {
        resolver.resolveException(request!!, response!!, null, exception)
    }

}
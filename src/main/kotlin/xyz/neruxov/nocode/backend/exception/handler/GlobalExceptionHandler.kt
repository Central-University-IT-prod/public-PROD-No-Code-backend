package xyz.neruxov.nocode.backend.exception.handler

import jakarta.validation.ConstraintViolationException
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import xyz.neruxov.nocode.backend.exception.type.StatusCodeException
import xyz.neruxov.nocode.backend.util.MessageResponse
import java.util.stream.Collectors

@Order(1000)
@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(InsufficientAuthenticationException::class)
    fun incorrectCredentialsException(ex: InsufficientAuthenticationException): ResponseEntity<*> {
        return ResponseEntity.status(401)
            .body(MessageResponse.error("Нет доступа"))
    }

    @ExceptionHandler(UsernameNotFoundException::class)
    fun usernameNotFoundException(ex: UsernameNotFoundException): ResponseEntity<*> {
        return ResponseEntity.status(401)
            .body(MessageResponse.error("Пользователь не найден"))
    }

    @ExceptionHandler(CredentialsExpiredException::class)
    fun credentialsExpiredException(ex: CredentialsExpiredException): ResponseEntity<*> {
        return ResponseEntity.status(401)
            .body(MessageResponse.error("Авторизационные данные устарели"))
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun httpMessageNotReadable(ex: HttpMessageNotReadableException): ResponseEntity<*> {
        return ResponseEntity.status(400)
            .body(MessageResponse.error("Тело запроса некорректно"))
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(ex: ConstraintViolationException): Any {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            MessageResponse.error(
                ex.constraintViolations.stream().map { err -> err.message }
                    .distinct()
                    .collect(Collectors.joining("; "))
            )
        )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleGlobalValidationExceptions(ex: MethodArgumentNotValidException): Any {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            MessageResponse.error(
                ex.bindingResult.fieldErrors.stream().map { err: FieldError -> err.defaultMessage }
                    .distinct()
                    .collect(Collectors.joining("; "))
            )
        )
    }

    @ExceptionHandler(StatusCodeException::class)
    fun handleStatusCodeException(e: StatusCodeException): ResponseEntity<Any> {
        return ResponseEntity.status(e.statusCode).body(e.getResponse())
    }

}
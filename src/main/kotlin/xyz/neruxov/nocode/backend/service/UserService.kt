package xyz.neruxov.nocode.backend.service

import org.springframework.stereotype.Service
import xyz.neruxov.nocode.backend.data.user.repo.UserRepository
import xyz.neruxov.nocode.backend.exception.type.StatusCodeException
import xyz.neruxov.nocode.backend.util.MessageResponse
import kotlin.jvm.optionals.getOrElse

@Service
class UserService(
    private val userRepository: UserRepository
) {

    fun searchUsers(query: String) =
        MessageResponse.success(userRepository.find10ByUsernameContaining(query).map { it.toMap() })

    fun getUserById(id: Long) = MessageResponse.success(userRepository.findById(id).getOrElse {
        throw StatusCodeException(404, "Пользователь не найден")
    }.toMap())

    fun editUser(id: Long, fullName: String): Any {
        val user = userRepository.findById(id).getOrElse {
            throw StatusCodeException(404, "Пользователь не найден")
        }

        user.fullName = fullName
        userRepository.save(user)

        return MessageResponse.success()
    }

    fun deleteUser(id: Long): Any {
        val user = userRepository.findById(id).getOrElse {
            throw StatusCodeException(404, "Пользователь не найден")
        }

        userRepository.delete(user)
        return MessageResponse.success()
    }

}
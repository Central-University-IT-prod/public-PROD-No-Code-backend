package xyz.neruxov.nocode.backend.exception.type

import xyz.neruxov.nocode.backend.util.MessageResponse

/**
 * @author <a href="https://github.com/Neruxov">Neruxov</a>
 */
class StatusCodeException(
    val statusCode: Int,
    val reason: String
) : RuntimeException(
    "$statusCode $reason"
) {

    fun getResponse(): MessageResponse {
        return MessageResponse.error(reason)
    }

}
package xyz.neruxov.nocode.backend.util

data class MessageResponse(
    val type: String,
    val data: Any
) {

    companion object {
        fun error(message: String) = MessageResponse("error", message)
        fun success(message: Any = "ok") = MessageResponse("success", message)
    }

}
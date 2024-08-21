package xyz.neruxov.nocode.backend.data.user.response

data class AuthorizationResponse(
    val accessToken: String,
    val refreshToken: String
)
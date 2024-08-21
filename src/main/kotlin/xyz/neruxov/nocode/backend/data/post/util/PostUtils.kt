package xyz.neruxov.nocode.backend.data.post.util

import java.util.*

fun ByteArray.base64() = String(Base64.getEncoder().encode(this))

fun String.fromBase64(): ByteArray = Base64.getDecoder().decode(this)

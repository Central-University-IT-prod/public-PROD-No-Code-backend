package xyz.neruxov.nocode.backend.util

class RandomUtil {

    companion object {

        fun generateRandomString(length: Int): String {
            val source = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
            return (1..length)
                .map { source.random() }
                .joinToString("")
        }

    }

}
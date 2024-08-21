package xyz.neruxov.nocode.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class NoCodeBackendApplication

fun main(args: Array<String>) {
    runApplication<NoCodeBackendApplication>(*args)
}

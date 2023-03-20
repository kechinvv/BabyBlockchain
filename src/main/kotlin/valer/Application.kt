package valer

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import valer.plugins.configureRouting

fun main() {
    Blockchain.mode = System.getenv("NONCE_MODE")
    val neighbors = System.getenv("NEIGHBORS").split(",").mapNotNull { it.toIntOrNull() }
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }
    configureRouting()
}

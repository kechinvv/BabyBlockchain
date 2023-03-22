package valer

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.coroutines.*
import valer.Blockchain.createAddDistribute
import valer.plugins.configureRouting

var neighbors = emptyList<Int>()
var myPort = 0
var job: Job? = null
fun main(): Unit = runBlocking {
    Blockchain.mode = System.getenv("NONCE_MODE") ?: "0"
    neighbors = System.getenv("NEIGHBORS").split(",").mapNotNull { it.toIntOrNull() }
    myPort = System.getenv("PORT").toInt()
    val initNode = System.getenv("MASTER").toBoolean()
    if (initNode) job = launch(Dispatchers.Default) {
        delay(1000)
        while (isActive) {
            createAddDistribute()
        }
    }
    embeddedServer(Netty, port = myPort, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }
    configureRouting()
}

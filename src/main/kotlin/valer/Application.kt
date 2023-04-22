package valer

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.*
import valer.Blockchain.createAddDistribute
import valer.plugins.configureRouting

var neighbors = emptyList<String>()
var myPort = 0
var jobGenerator: Job? = null
var jobCorrector: Job? = null
fun main(): Unit = runBlocking {
    Blockchain.mode = System.getenv("NONCE_MODE") ?: "0"
    neighbors = System.getenv("NEIGHBORS").split(",")
    myPort = System.getenv("PORT").toInt()
    val initNode = System.getenv("MASTER").toBoolean()
    if (initNode) jobGenerator = launch(Dispatchers.Default) {
        delay(1000)
        while (isActive)
            try {
                createAddDistribute()
            } catch (e: IllegalArgumentException) {
                println(e.message)
            }
    }
    embeddedServer(Netty, port = myPort, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureRouting()
}

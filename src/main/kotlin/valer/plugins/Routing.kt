package valer.plugins

import com.google.gson.Gson
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.coroutines.awaitAll
import valer.Blockchain

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        post("/add_block") {
            val gson = Gson()
            //val block = call.receive<Blockchain.Block>().hash

            val blockJson = call.receiveText()
            val block = gson.fromJson(blockJson, Blockchain.Block::class.java)
            //println(block)
        }
    }
}

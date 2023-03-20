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
            try {
                val params = call.receiveParameters()
                val index = params["index"]!!.toInt()
                val prev_hash = params["prev_hash"]
                val data = params["data"]
                val nonce = params["nonce"]!!.toInt()
                val hash = params["hash"]
                println("hash = $hash ; ")
                val block = Blockchain.Block(index, prev_hash!!, data!!, nonce, hash)
                Blockchain.addBlockToChain(block)
                call.respondText("ok")
            } catch (e: Exception) {
                println(e)
                call.respondText("er")
            }
        }
    }
}

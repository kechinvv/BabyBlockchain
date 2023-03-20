package valer.plugins

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import valer.Blockchain
import valer.Utils
import valer.job

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
                val block = Blockchain.Block(index, prev_hash!!, data!!, nonce, hash)
                job?.cancel()
                Blockchain.addBlockToChain(block)
                call.respondText("ok")
                job = launch {
                    while (isActive) {
                        val newBlock = Blockchain.createBlock()
                        val res = Utils.distributeBlock(newBlock)
                        if (res) Blockchain.addBlockToChain(newBlock)
                    }
                }
            } catch (e: Exception) {
                println(e)
                call.respondText("er")
            }
        }
    }
}

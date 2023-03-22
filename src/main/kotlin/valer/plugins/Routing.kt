package valer.plugins

import com.google.gson.Gson
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import valer.Blockchain
import valer.Utils.correctingChain
import valer.job

fun Application.configureRouting() {
    routing {
        get("/get_block") {
            try {
                val index = call.request.queryParameters["index"]!!.toInt() - 1
                call.respondText { Gson().toJson(Blockchain.chain[index]) }
            } catch (e: Exception) {
                throw e
            }
        }


        post("/add_block") {
            try {
                val params = call.receiveParameters()
                val index = params["index"]!!.toInt()
                val prev_hash = params["prev_hash"]
                val data = params["data"]
                val nonce = params["nonce"]!!.toInt()
                val hash = params["hash"]
                try {
                    val block = Blockchain.Block(index, prev_hash!!, data!!, nonce, hash)
                    job?.cancel()
                    Blockchain.addBlockToChain(block)
                } catch (e: IllegalArgumentException) {
                    job?.cancel()
                    if (index > Blockchain.chain.last().index + 1) {
                        println("I am looser")
                        correctingChain(params["port"]!!.toInt(), index)
                        println("It is now my blocks")
                        println(Blockchain.chain.size)
                        println(job!!.isActive)
                    }
                } finally {
                    job = launch(Dispatchers.Default) {
                        while (isActive) {
                            Blockchain.createAddDistribute()
                        }
                    }
                }
            } catch (e: Exception) {
                throw e
            }
        }
    }
}

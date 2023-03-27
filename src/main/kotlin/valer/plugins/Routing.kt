package valer.plugins

import com.google.gson.Gson
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.*
import valer.Blockchain
import valer.IllegalIndexException
import valer.Utils.correctingChain
import valer.jobCorrector
import valer.jobGenerator


fun Application.configureRouting() {
    routing {
        get("/get_block") {
            if (jobCorrector?.isActive == true) return@get
            try {
                val index = call.request.queryParameters["index"]!!.toInt() - 1
                call.respondText { Gson().toJson(Blockchain.chain[index]) }
            } catch (e: Exception) {
                println(e)
            }
        }


        post("/add_block") {
            if (jobCorrector?.isActive == true) return@post
            val params = call.receiveParameters()
            val index = params["index"]!!.toInt()
            val prevHash = params["prev_hash"]
            val data = params["data"]
            val nonce = params["nonce"]!!.toInt()
            val hash = params["hash"]
            val port = params["port"]!!.toInt()
            try {
                val block = Blockchain.Block(index, prevHash!!, data!!, nonce, hash)
                jobGenerator?.cancel()
                Blockchain.addBlockToChain(block)
                jobGenerator = launch(Dispatchers.Default) {
                    while (isActive) {
                        try {
                            Blockchain.createAddDistribute()
                        } catch (_: Exception) {
                        }
                    }
                }
            } catch (e: IllegalIndexException) {
                if (Blockchain.chain.size > 0 && index <= Blockchain.chain.last().index + 1 ||
                    Blockchain.chain.size == 0 && index <= 1
                ) return@post
                jobGenerator?.cancel()
                if (jobCorrector?.isActive == true) return@post
                jobCorrector = launch(Dispatchers.Default) { correctingChain(call.request.local.remoteAddress, port, index) }
                runBlocking {
                    jobCorrector?.join()
                    jobGenerator = launch(Dispatchers.Default) {
                        while (isActive) {
                            try {
                                Blockchain.createAddDistribute()
                            } catch (_: Exception) {
                            }
                        }
                    }
                }

            } catch (e: Exception) {
                println(e)
            }

        }
    }
}

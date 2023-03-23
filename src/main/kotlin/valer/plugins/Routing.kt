package valer.plugins

import com.google.gson.Gson
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.*
import valer.Blockchain
import valer.Utils.correctingChain
import valer.jobCorrector
import valer.jobGenerator

fun Application.configureRouting() {
    routing {
        get("/get_block") {
            try {
                val index = call.request.queryParameters["index"]!!.toInt() - 1
                call.respondText { Gson().toJson(Blockchain.chain[index]) }
            } catch (e: Exception) {
                println(e.message)
            }
        }


        post("/add_block") {
            if (jobCorrector?.isActive == true) return@post
            try {
                val params = call.receiveParameters()
                val index = params["index"]!!.toInt()
                val prev_hash = params["prev_hash"]
                val data = params["data"]
                val nonce = params["nonce"]!!.toInt()
                val hash = params["hash"]
                try {
                    val block = Blockchain.Block(index, prev_hash!!, data!!, nonce, hash)
                    jobGenerator?.cancel()
                    Blockchain.addBlockToChain(block)
                } catch (e: IllegalArgumentException) {
                    jobGenerator?.cancel()
                    if (index > Blockchain.chain.last().index + 1) {
                        println("I am looser")
                        if (jobCorrector?.isActive == true) return@post
                        jobCorrector = launch(Dispatchers.Default) { correctingChain(params["port"]!!.toInt(), index) }
                        runBlocking {
                            jobCorrector?.join()
                            println("It is now my blocks")
                        }
                    }
                } finally {
                    jobGenerator = launch(Dispatchers.Default) {
                        while (isActive) {
                            try {
                                Blockchain.createAddDistribute()
                            } catch (e: IllegalArgumentException) {
                                println(e.message)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                throw e
            }
        }
    }
}

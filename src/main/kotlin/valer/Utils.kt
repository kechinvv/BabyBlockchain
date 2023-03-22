package valer

import com.google.gson.Gson
import com.google.gson.JsonObject
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.random.Random

object Utils {
    private val charPool = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    private val client = HttpClient(CIO)

    fun randomStr(): String {
        val randomString = (1..256)
            .map { charPool[Random.nextInt(0, charPool.size)] }
            .joinToString("");
        return randomString
    }

    suspend fun distributeBlock(block: Blockchain.Block) = coroutineScope {
        for (port in neighbors) {
            async {
                client.post("http://127.0.0.1:$port/add_block") {
                    setBody(MultiPartFormDataContent(
                        formData {
                            append("index", block.index)
                            append("prev_hash", block.prev_hash)
                            append("data", block.data)
                            append("nonce", block.nonce!!)
                            append("hash", block.hash!!)
                            append("port", myPort)
                        }
                    ))
                }
            }
        }
    }


    private fun jsonToBlock(json: JsonObject): Blockchain.Block {
        return Blockchain.Block(
            json.get("index").asInt,
            json.get("prev_hash").asString,
            json.get("data").asString,
            json.get("nonce").asInt,
            json.get("hash").asString
        )
    }

    suspend fun correctingChain(port: Int, lastIndex: Int) {
        var curIndex = lastIndex
        val blockStack = ArrayDeque<JsonObject>()
        while (true) {
            val response =
                client.get("http://127.0.0.1:$port/get_block") {
                    url {
                        parameters.append("index", curIndex.toString())
                    }
                }
            val blockText = response.bodyAsText()
            val blockJson = Gson().fromJson(blockText, JsonObject::class.java)
            try {
                if (curIndex == Blockchain.chain.last().index) Blockchain.chain.removeLast()
                Blockchain.addBlockToChain(jsonToBlock(blockJson))
                break
            } catch (e: IllegalArgumentException) {
                blockStack.add(blockJson)
                curIndex--
            }
        }
        while (blockStack.isNotEmpty()) {
            val blockJson = blockStack.removeLast()
            Blockchain.addBlockToChain(jsonToBlock(blockJson))
        }
    }
}
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
        for (host in neighbors) {
            async {
                try {
                    client.post("http://$host/add_block") {
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
                } catch (e: Exception) {
                    println(e)
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

    suspend fun correctingChain(host: String, port: Int, lastIndex: Int) {
        var curIndex = lastIndex
        val blockStack = ArrayDeque<JsonObject>()
        val tempChain = Blockchain.chain
        lateinit var blockJson: JsonObject
        while (curIndex >= 1) {
            try {
                val response =
                    client.get("http://$host:$port/get_block") {
                        url {
                            parameters.append("index", curIndex.toString())
                        }
                    }
                val blockText = response.bodyAsText()
                blockJson = Gson().fromJson(blockText, JsonObject::class.java)

                if (Blockchain.chain.size != 0 && curIndex == Blockchain.chain.last().index) Blockchain.chain.removeLast()
                Blockchain.addBlockToChain(jsonToBlock(blockJson))
                break
            } catch (e: Exception) {
                when (e) {
                    is IllegalIndexException, is NotActualBlockException -> {
                        blockStack.add(blockJson)
                        curIndex--
                    }

                    else -> {
                        Blockchain.chain = tempChain
                        println(e)
                        return
                    }
                }
            }
        }
        try {
            while (blockStack.isNotEmpty()) {
                blockJson = blockStack.removeLast()
                println("added")
                Blockchain.addBlockToChain(jsonToBlock(blockJson))
            }
        } catch (e: Exception) {
            Blockchain.chain = tempChain
        }
    }
}
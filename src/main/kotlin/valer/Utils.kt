package valer

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
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

    suspend fun distributeBlock(block: Blockchain.Block): Boolean {
        for (port in neighbors) {
            val response = client.post("http://127.0.0.1:$port/add_block") {
                setBody(MultiPartFormDataContent(
                    formData {
                        append("index", block.index)
                        append("prev_hash", block.prev_hash)
                        append("data", block.data)
                        append("nonce", block.nonce!!)
                        append("hash", block.hash!!)
                    }
                ))
            }
            if (response.bodyAsText() == "er") return false
        }
        return true
    }
}
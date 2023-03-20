package valer

import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.testing.*
import kotlinx.coroutines.*
import org.junit.Test
import valer.plugins.configureRouting


class ApplicationTest {

    @Test
    fun testBlock() = runBlocking {
        var block: Blockchain.Block? = null
        val job = launch {
            block = Blockchain.createBlock()
            println("finish: " + block?.hash)
        }
        println("creating block")
        println("hash is:")
        var i = 0
        while (block == null && i < 16) {
            delay(1)
            println("waiting " + i++)
            if (i == 3) {
                job.cancel()
            }
        }
    }

    @Test
    fun testNonceMode() {
        Blockchain.mode = System.getenv("NONCE_MODE")
        println(Blockchain.mode)
    }


    @Test
    fun testRoot() = testApplication {
        application {
            configureRouting()
        }

        val block = Blockchain.createBlock()
        println(block.hash)
        val response = client.post("/add_block") {
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
        //        val response = khttp.post(
//            "http://127.0.0.1:8080/add_block2",
//            headers= mapOf("Content-Type" to "application/x-www-form-urlencoded"),
//            data = mapOf(
//                "index" to block.index,
//                "prev_hash" to block.prev_hash,
//                "data" to block.data,
//                "nonce" to block.nonce,
//                "hash" to block.hash
//            )
//        )
        val mydata: String = response.bodyAsText()
        println(mydata)

    }

}



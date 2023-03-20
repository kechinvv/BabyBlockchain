package valer

import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
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
        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }
        val block = Blockchain.createBlock()
        println(block.hash)
        val response = client.post("/add_block") {
            contentType(ContentType.Application.Json)
            setBody(block)
        }
        val data: String = response.bodyAsText()
        println(data)

    }


}



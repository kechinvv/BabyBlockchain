package valer

import com.google.gson.Gson
import com.google.gson.JsonObject
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.server.testing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import valer.plugins.configureRouting
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.jvm.isAccessible
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RouteTests {
    @BeforeEach
    fun clearChain() {
        Blockchain.chain = ArrayDeque()
        Blockchain.mode = "0"
    }

    @Test
    fun testRootPostSuccess() = testApplication {
        runBlocking {
            application {
                configureRouting()
            }

            val block = Blockchain.createBlock()
            val job = launch(Dispatchers.Default) {
                client.post("/add_block") {
                    setBody(MultiPartFormDataContent(
                        formData {
                            append("index", block.index)
                            append("prev_hash", block.prev_hash)
                            append("data", block.data)
                            append("nonce", block.nonce!!)
                            append("hash", block.hash!!)
                            append("port",  8080)
                        }
                    ))
                }
            }
            delay(1000)
            assertNotNull(jobGenerator)
            job.cancel()
            jobGenerator?.cancel()
            assertTrue { block in Blockchain.chain }
            assertTrue(jobGenerator?.isCancelled == true)
        }
    }

    @Test
    fun testRootPostExceptions() = testApplication {
        application {
            configureRouting()
        }

        val block = Blockchain.createBlock()
        client.post("/add_block") {
            setBody(MultiPartFormDataContent(
                formData {
                    append("index", 2)
                    append("prev_hash", block.prev_hash)
                    append("data", block.data)
                    append("nonce", block.nonce!!)
                    append("hash", block.hash!!)
                    append("port",  8080)
                }
            ))
        }
        assertNull(jobGenerator)

        Blockchain.createAddDistribute()
        client.post("/add_block") {
            setBody(MultiPartFormDataContent(
                formData {
                    append("index", block.index)
                    append("prev_hash", block.prev_hash)
                    append("data", block.data)
                    append("nonce", block.nonce!!)
                    append("hash", block.hash!!)
                    append("port",  8080)
                }
            ))
        }
        assertNull(jobGenerator)
    }

    @Test
    fun testGetBlock() = testApplication {
        runBlocking {
            application {
                configureRouting()
            }
            repeat(5) {
                Blockchain.createAddDistribute()
            }
            assertDoesNotThrow {
                val response = client.get("/get_block") {
                    url {
                        parameters.append("index", "4")
                    }
                }
                repeat(2) {
                    Blockchain.chain.removeLast()
                }
                val privateMethod = Utils::class.declaredMemberFunctions.filter { it.name == "jsonToBlock" }[0]
                privateMethod.isAccessible = true
                val blockText = response.bodyAsText()
                val blockJson = Gson().fromJson(blockText, JsonObject::class.java)
                Blockchain.addBlockToChain(privateMethod.call(Utils, blockJson) as Blockchain.Block)
            }

            jobCorrector = launch(Dispatchers.Default) { delay(2000) }
            delay(500)
            val response = client.get("/get_block") {
                url {
                    parameters.append("index", "4")
                }
            }
            assertEquals("", response.bodyAsText())
        }
    }
}
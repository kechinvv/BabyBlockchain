package valer

import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Test
import valer.plugins.configureRouting
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class RootTests {

    @Test
    fun testRootPost() = testApplication {
        application {
            configureRouting()
        }

        val block = Blockchain.createBlock()
        val response = client.post("/add_block") {
            setBody(MultiPartFormDataContent(
                formData {
                    append("index", 2)
                    append("prev_hash", block.prev_hash)
                    append("data", block.data)
                    append("nonce", block.nonce!!)
                    append("hash", block.hash!!)
                }
            ))
        }
        assertEquals(HttpStatusCode(404, "Not Found"), response.status)
        assertNull(jobGenerator)

    }
}
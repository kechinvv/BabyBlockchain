package valer

import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.server.testing.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import valer.plugins.configureRouting
import kotlin.test.assertNotNull
import kotlin.test.assertNull


class BlockTests {

    @Test
    fun testCancelJobBlockCreating() = runBlocking {
        var block: Blockchain.Block? = null
        val job = launch {
            block = Blockchain.createBlock()
        }
        var i = 0
        while (block == null && i < 16) {
            delay(10)
            i++
            if (i == 3) {
                job.cancel()
            }
        }
        assertNull(block)
    }

    @Test
    fun testJobBlockCreating(): Unit = runBlocking {
        var block: Blockchain.Block? = null
        launch {
            block = Blockchain.createBlock()
        }
        var i = 0
        while (block == null && i < 16) {
            i++
            delay(100)
        }
        assertNotNull(block)
    }

    @Test
    fun addFirstBlock() {
        TODO()
    }




}



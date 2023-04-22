package valer.integration

import com.google.gson.Gson
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.util.Identity.decode
import io.ktor.util.Identity.encode
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import io.mockk.InternalPlatformDsl.toStr
import io.mockk.clearAllMocks
import io.mockk.core.ValueClassSupport.boxedValue
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import valer.*
import valer.Utils.randomStr
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class UtilsTests {

    @BeforeEach
    fun clearChain() {
        clearAllMocks()
        Blockchain.chain = ArrayDeque()
        Blockchain.mode = "0"
        Utils.client = HttpClient(CIO)
        neighbors = emptyList()
    }

    @Test
    fun testRandomStr() {
        repeat(10) {
            val res = randomStr()
            assertEquals(256, res.length)
        }
    }

    @Test
    fun distributeTest() = runBlocking {
        val mockEngine = MockEngine { request ->
            respond(
                content = ByteReadChannel(""),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        Utils.client = HttpClient(mockEngine)
        neighbors = listOf("a")
        Utils.distributeBlock(Blockchain.createBlock())
        val badBlock = Blockchain.createBlock()
        badBlock.index += 1
        Utils.distributeBlock(badBlock)

    }

    @Test
    fun correctingChainFullChainGetTest() = runBlocking {
        repeat(10) {
            println(Blockchain.chain)
            Blockchain.addBlockToChain(Blockchain.createBlock())
            println(Blockchain.chain)
        }
        val tempChain = Blockchain.chain
        Blockchain.chain = ArrayDeque()
        repeat(3) {
            Blockchain.addBlockToChain(Blockchain.createBlock())
        }

        val mockEngine = MockEngine { request ->
            respond(
                content = ByteReadChannel(Gson().toJson(tempChain[request.url.parameters["index"]!!.toInt() - 1])),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        Utils.client = HttpClient(mockEngine)
        assertNotEquals(tempChain, Blockchain.chain)
        assertNotEquals(tempChain[0], Blockchain.chain[0])

        Utils.correctingChain("a", 0, tempChain.size)
        assertEquals(tempChain, Blockchain.chain)
    }

    @Test
    fun correctingChainPartChainGetTest() = runBlocking {
        repeat(10) {
            Blockchain.addBlockToChain(Blockchain.createBlock())
        }
        val tempChain = Blockchain.chain
        Blockchain.chain = ArrayDeque()
        repeat(3) {
            println(it)
            Blockchain.addBlockToChain(tempChain[it])
        }

        val mockEngine = MockEngine { request ->
            respond(
                content = ByteReadChannel(Gson().toJson(tempChain[request.url.parameters["index"]!!.toInt() - 1])),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        Utils.client = HttpClient(mockEngine)
        assertEquals(tempChain[0], Blockchain.chain[0])
        assertNotEquals(tempChain, Blockchain.chain)

        Utils.correctingChain("a", 0, tempChain.size)
        assertEquals(tempChain, Blockchain.chain)
    }

    @Test
    fun correctingChainBadBlocksTest() = runBlocking {
        repeat(10) {
            Blockchain.addBlockToChain(Blockchain.createBlock())
        }
        val tempChain = Blockchain.chain
        Blockchain.chain = ArrayDeque()
        repeat(3) {
            println(it)
            Blockchain.addBlockToChain(tempChain[it])
        }
        val expectedChain = Blockchain.chain

        val mockEngine = MockEngine { request ->
            respond(
                content = ByteReadChannel(Gson().toJson(tempChain[request.url.parameters["index"]!!.toInt() - 1])),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        tempChain[6].index = 1
        Utils.client = HttpClient(mockEngine)
        assertNotEquals(tempChain, Blockchain.chain)

        Utils.correctingChain("a", 0, tempChain.size)
        assertEquals(expectedChain, Blockchain.chain)

        tempChain[6].index = 7
        tempChain[5].hash += "1"
        Utils.client = HttpClient(mockEngine)
        assertNotEquals(tempChain, Blockchain.chain)

        Utils.correctingChain("a", 0, tempChain.size)
        assertEquals(expectedChain, Blockchain.chain)
    }

    companion object {
        @JvmStatic
        @BeforeAll
        fun stopCor(): Unit {
            runBlocking {
                jobCorrector?.cancel()
                jobCorrector?.join()
                jobGenerator?.cancel()
                jobGenerator?.join()
            }
        }
    }
}
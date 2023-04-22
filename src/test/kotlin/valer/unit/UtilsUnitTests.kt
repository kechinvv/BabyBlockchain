package valer.unit

import com.google.gson.Gson
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.util.*
import io.ktor.utils.io.*
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import valer.*
import valer.Utils.randomStr
import kotlin.test.assertEquals

class UtilsUnitTests {
    @BeforeEach
    fun clearChain() {
        clearAllMocks()
        Blockchain.chain = ArrayDeque()
        Blockchain.mode = "0"
        Utils.client = HttpClient(CIO)

    }

    @RepeatedTest(5)
    fun randStringTest() {
        val res = randomStr()
        assertEquals(256, res.length)
    }

    @Test
    fun distributeBlockTest() {
        neighbors = listOf("a", "b")
        myPort = 0
        val block = mockkClass(Blockchain.Block::class)
        runBlocking { Utils.distributeBlock(block) }
    }

    @Test
    fun correctingChain() {
        mockkObject(Blockchain)
        mockkObject(Utils)
        every { Blockchain.addBlockToChain(any()) } throws NotActualBlockException()
        val spyBlock = spyk(Blockchain.Block(1, "", ""))
        spyBlock.hash = "0000"
        every { spyBlock["calculateHash"]() } returns "0000"
        every { Utils.jsonToBlock(any()) } returns spyBlock
        val block = Blockchain.Block(1, "", "")

        val mockEngine = MockEngine { request ->
            respond(
                content = ByteReadChannel(Gson().toJson(block)),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        Utils.client = HttpClient(mockEngine)

        spyBlock.index = 5
        spyBlock.hash = "0000"
        runBlocking { Utils.correctingChain("a", 0, 5) }
        verify(exactly = 6) { Blockchain.addBlockToChain(any()) }

        spyBlock.index = 1
        runBlocking { Utils.correctingChain("a", 0, 1) }
        verify(exactly = 8) { Blockchain.addBlockToChain(any()) }

        spyBlock.hash = "0001"
        runBlocking { Utils.correctingChain("a", 0, 1) }
        verify(exactly = 10) { Blockchain.addBlockToChain(any()) }

        every { Blockchain.addBlockToChain(any()) } answers { nothing }
        spyBlock.hash = "0000"
        spyBlock.index = 2
        runBlocking { Utils.correctingChain("a", 0, 2) }
        verify(exactly = 11) { Blockchain.addBlockToChain(any()) }
    }


}


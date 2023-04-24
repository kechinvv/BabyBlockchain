package valer.unit

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import valer.*
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ChainUnitTests {

    @BeforeEach
    fun clearChain() = runBlocking {
        clearAllMocks()
        Blockchain.chain = ArrayDeque()
        Blockchain.mode = "0"
        Utils.client = HttpClient(CIO)
        neighbors = emptyList()
        jobCorrector?.cancel()
        jobCorrector?.join()
        jobGenerator?.cancel()
        jobGenerator?.join()
        jobGenerator = null
        jobCorrector = null
    }

    @Test
    fun createBlockTest() {
        mockkObject(Utils)
        every { Utils.randomStr() } returns ""
        runBlocking {
            val block = Blockchain.createBlock()
            assertEquals(0, Blockchain.chain.size)
            assertEquals(1, block.index)
            assertNotEquals("1111", block.hash)
        }

        Blockchain.chain.add(Blockchain.Block(1, "test", ""))
        runBlocking {
            val block = Blockchain.createBlock()
            assertEquals(2, block.index)
            assertNotEquals("1111", block.hash)
            assertEquals(Blockchain.chain.last().hash, block.prev_hash)
        }
    }

    @Test
    fun createAndDistributeTest() {
        mockkObject(Utils)
        mockkObject(Blockchain)
        coEvery { Blockchain.createAddDistribute() } answers { callOriginal() }
        coEvery { Blockchain.createBlock() } returns mockkClass(Blockchain.Block::class)
        every { Blockchain.addBlockToChain(any()) } answers { nothing }
        coEvery { Utils.distributeBlock(any()) } answers { nothing }
        runBlocking { Blockchain.createAddDistribute() }

        coVerifyAll {
            Blockchain.createAddDistribute(); Blockchain.createBlock();
            Blockchain.addBlockToChain(any()); Utils.distributeBlock(any())
        }
    }

    @Test
    fun testAddBlockWithoutConstructor() {
        val block = mockkClass(Blockchain.Block::class, relaxed = true)
        mockkObject(Blockchain)
        coEvery { Blockchain.addBlockToChain(any()) } answers { callOriginal() }
        Blockchain.addBlockToChain(block)
    }

}
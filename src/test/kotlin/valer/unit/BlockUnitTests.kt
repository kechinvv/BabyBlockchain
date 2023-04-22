package valer.unit

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import valer.*
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BlockUnitTests {
    @BeforeEach
    fun clearChain() {
        clearAllMocks()
        Blockchain.chain = ArrayDeque()
        Blockchain.mode = "0"
        Utils.client = HttpClient(CIO)
    }

    @ParameterizedTest
    @MethodSource("getBlockData")
    fun initBlockWithoutHashOrNonceTest(index: Int, prevHash: String, data: String, nonce: Int, hash: String) {
        var block = Blockchain.Block(index, prevHash, data)
        assertEquals(0, block.nonce)
        assertEquals("1111", block.hash)
        assertEquals(index, block.index)
        assertEquals(prevHash, block.prev_hash)
        assertEquals(data, block.data)

        block = Blockchain.Block(index, prevHash, data, hash = hash)
        assertEquals(0, block.nonce)
        assertEquals("1111", block.hash)
        assertEquals(index, block.index)
        assertEquals(prevHash, block.prev_hash)
        assertEquals(data, block.data)

        block = Blockchain.Block(index, prevHash, data, nonce)
        assertEquals(0, block.nonce)
        assertEquals("1111", block.hash)
        assertEquals(index, block.index)
        assertEquals(prevHash, block.prev_hash)
        assertEquals(data, block.data)
    }

    @ParameterizedTest
    @MethodSource("getBlockData")
    fun initBlockWithHashAndNonceTest(index: Int, prevHash: String, data: String, nonce: Int, hash: String) {
        assertThrows<Exception> { Blockchain.Block(index, prevHash, data, nonce, hash) }
    }

    @ParameterizedTest
    @MethodSource("getBlockData")
    fun setValidHashIncModeTest(index: Int, prevHash: String, data: String, nonce: Int, hash: String) {
        Blockchain.mode = "0"
        var block = Blockchain.Block(index, prevHash, data)
        runBlocking {
            block.setValidHash()
        }
        assertTrue { block.hash!!.endsWith("0000") }
        assertTrue { block.nonce!! >= 0 }

        block = Blockchain.Block(index, prevHash, data)
        block.nonce = Int.MAX_VALUE
        runBlocking {
            block.setValidHash()
        }
        assertTrue { block.hash!!.endsWith("0000") }
    }

    @ParameterizedTest
    @MethodSource("getBlockData")
    fun setValidHashDecModeTest(index: Int, prevHash: String, data: String, nonce: Int, hash: String) {
        Blockchain.mode = "1"
        val block = Blockchain.Block(index, prevHash, data)
        runBlocking {
            block.setValidHash()
        }
        assertTrue { block.hash!!.endsWith("0000") }
        assertTrue { block.nonce!! <= 0 }

        block.nonce = Int.MIN_VALUE
        runBlocking {
            block.setValidHash()
        }
        assertTrue { block.hash!!.endsWith("0000") }
    }

    @ParameterizedTest
    @MethodSource("getBlockData")
    fun setValidHashRandModeTest(index: Int, prevHash: String, data: String, nonce: Int, hash: String) {
        Blockchain.mode = "2"
        val block = Blockchain.Block(index, prevHash, data)
        runBlocking {
            block.setValidHash()
        }
        assertTrue { block.hash!!.endsWith("0000") }
    }

    @Test
    fun verifyWithEmptyChainTest() {
        Blockchain.chain = ArrayDeque()
        val block = spyk(Blockchain.Block(1, "", ""), recordPrivateCalls = true)
        every { block.verification() } answers { callOriginal() }

        every { block["calculateHash"]() } returns "1111"
        assertThrows<IllegalHashException> { block.verification() }

        block.hash = "0000"
        assertThrows<IllegalHashException> { block.verification() }

        block.index = 0
        assertThrows<IllegalHashException> { block.verification() }

        every { block["calculateHash"]() } returns "0000"
        block.index = 1
        block.verification()

        block.index = 0
        assertThrows<IllegalIndexException> { block.verification() }

        block.index = 2
        assertThrows<IllegalIndexException> { block.verification() }
    }

    @Test
    fun verifyWithNotEmptyChainTest() {
        Blockchain.chain = ArrayDeque()
        val block1 = spyk(Blockchain.Block(1, "", ""), recordPrivateCalls = true)
        every { block1["calculateHash"]() } returns "0000"
        block1.hash = "0000"
        block1.nonce = 0
        Blockchain.chain.add(block1)


        val block2 = spyk(Blockchain.Block(1, "", ""), recordPrivateCalls = true)
        every { block2.verification() } answers { callOriginal() }
        every { block2["calculateHash"]() } returns "0000"
        block2.hash = "0000"
        assertThrows<IllegalIndexException> { block2.verification() }
        block2.index = 2
        assertThrows<NotActualBlockException> { block2.verification() }
        block1.hash = block2.prev_hash
        block2.verification()
    }

    companion object {
        @JvmStatic
        fun getBlockData(): List<Arguments> {
            return (0..10).map { _ ->
                val index = Random.nextInt()
                val prevHash = Random.nextInt().toString()
                val data = Random.nextDouble().toString()
                val nonce = Random.nextInt().toString()
                val hash = Random.nextInt().toString()
                Arguments.of(
                    index, prevHash, data, nonce, hash
                )
            }
        }
    }
}
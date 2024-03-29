package valer.integration

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.mockk.clearAllMocks
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import valer.*
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.jvm.isAccessible
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


class ChainTests {
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
    fun testJobBlockCreating(): Unit = runBlocking {
        var block: Blockchain.Block? = null
        val job = launch {
            block = Blockchain.createBlock()
        }
        job.join()
        assertNotNull(block)
    }

    @Test
    fun testInvalidHashVerification(): Unit = runBlocking {
        assertThrows<IllegalHashException> {
            val block = Blockchain.createBlock()
            block.hash += "1"
            block.verification()
        }
        assertThrows<IllegalHashException> {
            val block = Blockchain.createBlock()
            block.hash += "0"
            block.verification()
        }
        assertDoesNotThrow {
            val block = Blockchain.createBlock()
            block.verification()
        }
    }

    @Test
    fun testInvalidIndexVerification(): Unit = runBlocking {
        val block = Blockchain.createBlock()
        assertThrows<IllegalIndexException> {
            block.index = 0
            block.verification()
        }
        assertThrows<IllegalIndexException> {
            block.index = 2
            block.verification()
        }
        assertThrows<IllegalIndexException> {
            block.index = -5
            block.verification()
        }
        assertDoesNotThrow {
            block.index = 1
            block.verification()
        }
    }

    @Test
    fun testNotActualBlockVerification(): Unit = runBlocking {
        var block1 = Blockchain.createBlock()
        Blockchain.addBlockToChain(block1)
        val block2 = Blockchain.createBlock()
        assertDoesNotThrow {
            Blockchain.addBlockToChain(block2)
        }

        Blockchain.chain = ArrayDeque()
        block1 = Blockchain.createBlock()
        Blockchain.addBlockToChain(block1)
        assertThrows<NotActualBlockException> {
            Blockchain.addBlockToChain(block2)
        }
    }


    @Test
    fun testNonceOverflow(): Unit = runBlocking {
        val block = Blockchain.createBlock()
        val privateMethod = block::class.declaredMemberFunctions.filter { it.name == "nextNonce" }[0]
        privateMethod.isAccessible = true
        Blockchain.mode = "0"
        var res = privateMethod.call(block, Int.MAX_VALUE)
        assertEquals(Int.MIN_VALUE, res)

        Blockchain.mode = "1"
        res = privateMethod.call(block, Int.MIN_VALUE)
        assertEquals(Int.MAX_VALUE, res)

        Blockchain.mode = "2"
        var result = false
        var lastRes = privateMethod.call(block, 0) as Int
        repeat(10) {
            res = privateMethod.call(block, 0)
            if (res != lastRes + 1 && res != lastRes - 1) result = true
            lastRes = res as Int
        }
        assertTrue(result)
    }


    @Test
    fun testCreateAddDistribution() = runBlocking {
        val expectedLen = 5
        repeat(expectedLen) {
            Blockchain.createAddDistribute()
        }
        assertEquals(expectedLen, Blockchain.chain.size)
    }


    @Test
    fun testAddBlockToChain(): Unit = runBlocking {
        val block = Blockchain.createBlock()
        Blockchain.addBlockToChain(block)
        assertEquals(block, Blockchain.chain.last())
    }

}



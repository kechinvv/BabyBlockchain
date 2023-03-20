package valer

import kotlinx.coroutines.*
import org.junit.Test


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
}

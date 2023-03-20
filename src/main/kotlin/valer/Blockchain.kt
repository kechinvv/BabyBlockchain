package valer

import com.google.common.hash.Hashing
import kotlinx.coroutines.*
import java.lang.IllegalArgumentException
import java.util.LinkedList

object Blockchain {
    val chain = LinkedList<Block>()
    private val charPool = ('a'..'z') + ('A'..'Z') + ('0'..'9')

    data class Block(
        val index: Int,
        val prev_hash: String,
        val data: String,
        var nonce: Int? = null,
        var hash: String? = null
    ) {
        private val mainStr = index.toString() + prev_hash + data

        init {
            if (hash == null || nonce == null) {
                nonce = 0
                hash = "1111"
            } else if (!verification()) throw IllegalArgumentException("Invalid block")
        }

        suspend fun setValidHash() {
            withContext(Dispatchers.Default) {
                while (hash!!.takeLast(4) != "0000" && isActive) {
                    hash = calculateHash()
                    nonce = nonce!! + 1
                }
            }
        }

        private fun verification(): Boolean {
            var hashActual = true
            var validIndex = index == 1
            if (chain.isNotEmpty()) {
                hashActual = prev_hash == chain.last.hash
                validIndex = (index + 1) == chain.last.index
            }
            val validHash = (hash!!.takeLast(4) == "0000") && (hash == calculateHash())
            return hashActual && validHash && validIndex
        }

        private fun calculateHash(): String {
            val hashFunction = Hashing.sha256()
            val hc = hashFunction
                .newHasher()
                .putString(mainStr + nonce.toString(), Charsets.UTF_8)
                .hash()
            return hc.toString()
        }
    }

    suspend fun createBlock(): Block = withContext(Dispatchers.Default) {
        var index = 1
        var prevHash = ""
        val data = randomStr()

        if (chain.isNotEmpty()) {
            val lastBlock = chain.last
            index = lastBlock.index + 1
            prevHash = lastBlock.hash!!
        }

        val block = Block(index, prevHash, data)
        block.setValidHash()
        return@withContext block
    }

    fun addBlockToChain(block: Block) {
        chain.add(block)
    }

    fun addBlockToChain(
        index: Int, prev_hash: String, data: String, nonce: Int, hash: String
    ) {
        val block = Block(index, prev_hash, data, nonce, hash)
        chain.add(block)
    }


    private fun randomStr(): String {
        val randomString = (1..256)
            .map { charPool[kotlin.random.Random.nextInt(0, charPool.size)] }
            .joinToString("");
        return randomString
    }

}
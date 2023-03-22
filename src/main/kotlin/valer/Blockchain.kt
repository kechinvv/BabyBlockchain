package valer

import com.google.common.hash.Hashing
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import valer.Utils.randomStr
import java.lang.IllegalArgumentException
import java.util.LinkedList
import kotlin.random.Random

object Blockchain {
    val chain = LinkedList<Block>()
    var mode = "0"


    @Serializable
    data class Block(
        var index: Int,
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
                    nonce = nextNonce(nonce!!)
                    hash = calculateHash()
                }
            }
        }

        private fun nextNonce(old_nonce: Int): Int {
            return when (mode) {
                "0" -> if (old_nonce < Int.MAX_VALUE) (old_nonce + 1) else Int.MIN_VALUE
                "1" -> if (old_nonce > Int.MIN_VALUE) (old_nonce - 1) else Int.MAX_VALUE
                else -> Random.nextInt()
            }
        }

        private fun verification(): Boolean {
            var hashActual = true
            var validIndex = index == 1
            if (chain.isNotEmpty()) {
                hashActual = prev_hash == chain.last.hash
                validIndex = (index - 1) == chain.last.index
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
        println("Block " + chain.last.index)
        println("prev_hash = "  + chain.last.prev_hash)
        println("hash = "  + chain.last.hash)
    }

    fun addBlockToChain(
        index: Int, prev_hash: String, data: String, nonce: Int, hash: String
    ) {
        val block = Block(index, prev_hash, data, nonce, hash)
        chain.add(block)
    }


}


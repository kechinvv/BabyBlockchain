package valer

import com.google.common.hash.Hashing
import java.util.LinkedList

object Blockchain {
    val chain = LinkedList<Block>()

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
                while (hash!!.takeLast(4) != "0000") {
                    hash = calculateHash()
                }
            }
        }

        private fun verification(): Boolean {
            var hashEqual = true
            if (chain.isNotEmpty()) hashEqual = prev_hash == chain.last.hash
            return (hash == calculateHash()) && hashEqual
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

    fun createBlock(index: Int, prev_hash: String, data: String): Block {
        val block = Block(index, prev_hash, data)
        println(block.hash)
        return block
    }
}
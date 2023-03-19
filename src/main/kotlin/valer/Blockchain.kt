package valer

import com.google.common.hash.Hashing
import java.util.LinkedList

object Blockchain {
    val chain = LinkedList<Block>()

    data class Block(val index: Int, val prev_hash: String, val data: String) {
        var hash = "1111"

        private val mainStr = index.toString() + prev_hash + data

        init {
            calculateHash()
        }

        private fun calculateHash() {
            val hashFunction = Hashing.sha256()
            var nonce = 0
            while (hash.takeLast(4) != "0000") {
                nonce++
                val hc = hashFunction
                    .newHasher()
                    .putString(mainStr + nonce.toString(), Charsets.UTF_8)
                    .hash()
                hash = hc.toString()
            }
        }
    }

    fun createBlock(index: Int, prev_hash: String, data: String) {
        val block = Block(index, prev_hash, data)
        println(block.hash)
        chain.add(block)
    }
}
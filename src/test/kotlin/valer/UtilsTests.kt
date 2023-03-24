package valer

import org.junit.jupiter.api.Test
import valer.Utils.randomStr
import kotlin.test.assertEquals

class UtilsTests {

    @Test
    fun testRandomStr() {
        repeat(10) {
            val res = randomStr()
            assertEquals(256, res.length)
        }
    }
}
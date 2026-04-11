package projson

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProJsonTest {

    @Test
    fun testPrimitive() {
        val json = ProJson().toJson("hello")
        assertEquals("\"hello\"", json.toJsonString())
    }

    @Test
    fun testArray() {
        val json = ProJson().toJson(listOf("a", "b"))
        assertTrue(json.toJsonString().contains("a"))
    }
}
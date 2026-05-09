package projson

import projson.core.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProJsonTest {

    @Test
    fun testPrimitive() {
        val json = JsonPrimitive("hello")
        assertEquals(json, ProJson().toJson("hello"))
    }

    @Test
    fun testArray() {
        val json = ProJson().toJson(listOf("a", "b", "c"))

        assertEquals("[\"a\", \"b\"]" ,json.toJsonString())
    }
}
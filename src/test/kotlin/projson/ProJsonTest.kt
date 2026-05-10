package projson

import projson.core.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

class ProJsonTest {

    @Test
    fun testStringConversion() {

        val expected = JsonPrimitive("Hello")

        val actual = ProJson().toJson("Hello")

        assertEquals(expected, actual)
    }

    @Test
    fun testNumberConversion() {

        val expected = JsonPrimitive(100)

        val actual = ProJson().toJson(100)

        assertEquals(expected, actual)
    }

    @Test
    fun testBooleanConversion() {

        val expected = JsonPrimitive(true)

        val actual = ProJson().toJson(true)

        assertEquals(expected, actual)
    }

    @Test
    fun testNullConversion() {

        val expected = JsonPrimitive(null)

        val actual = ProJson().toJson(null)

        assertEquals(expected, actual)
    }
}
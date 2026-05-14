package projson

import projson.core.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

class ProJsonTest {

    @Test
    fun testJsonPrimitiveConversion() {

//        String conversion
        val expectedString = JsonPrimitive("Hello")
        val actualString = ProJson().toJson("Hello")
        assertEquals(expectedString, actualString)

//        Number conversion
        val expectedNumber = JsonPrimitive(100)
        val actualNumber = ProJson().toJson(100)
        assertEquals(expectedNumber, actualNumber)

//        Boolean conversion
        val expectedBoolean = JsonPrimitive(true)
        val actualBoolean = ProJson().toJson(true)
        assertEquals(expectedBoolean, actualBoolean)

//        Null conversion
        val expectedNull = JsonPrimitive(null)
        val actualNull = ProJson().toJson(null)
        assertEquals(expectedNull, actualNull)
    }

    @Test
    fun testConvertCollection() {
        val expected = "[\"A\", \"B\"]"
        val actual = ProJson()
            .toJson(listOf("A", "B"))
            .toJsonString()

        assertEquals(
            "[\"A\", \"B\"]",
            actual
        )
    }
}
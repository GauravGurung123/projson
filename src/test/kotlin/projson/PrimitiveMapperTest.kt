package projson

import projson.core.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

class PrimitiveMapperTest {

    @Test
    fun testPrimitiveString() {

        val actual = ProJson().toJson("ABC")

        assertEquals(
            JsonPrimitive("ABC"),
            actual
        )
    }

    @Test
    fun testPrimitiveInt() {

        val actual = ProJson().toJson(10)

        assertEquals(
            JsonPrimitive(10),
            actual
        )
    }
}
package projson

import kotlin.test.Test
import kotlin.test.assertEquals

class CollectionMapperTest {

    @Test
    fun testListConversion() {

        val actual = ProJson()
            .toJson(listOf("A", "B"))
            .toJsonString()

        assertEquals(
            "[\"A\", \"B\"]",
            actual
        )
    }
}
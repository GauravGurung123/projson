package projson

import kotlin.test.Test
import kotlin.test.assertTrue

class MapMapperTest {

    @Test
    fun testMapConversion() {

        val json = ProJson()
            .toJson(
                mapOf(
                    "name" to "Gaurav"
                )
            )
            .toJsonString()

        assertTrue(json.contains("name"))
        assertTrue(json.contains("Gaurav"))
    }
}
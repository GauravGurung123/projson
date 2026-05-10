package projson

import kotlin.test.Test
import kotlin.test.assertTrue

class ObjectMapperTest {

    data class User(
        val name: String,
        val age: Int
    )

    @Test
    fun testObjectConversion() {

        val json = ProJson()
            .toJson(User("Gaurav", 25))
            .toJsonString()

        assertTrue(json.contains("Gaurav"))
        assertTrue(json.contains("25"))
        assertTrue(json.contains("\$type"))
    }
}
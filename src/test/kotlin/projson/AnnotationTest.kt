package projson

import projson.annotations.JsonIgnore
import projson.annotations.JsonProperty
import projson.context.SerializationContext
import projson.core.JsonObject
import projson.core.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AnnotationTest {

    data class Task(
        @JsonProperty("desc") val description: String,
        @JsonIgnore val ignore: String,
    )

    @Test
    fun testAnnotations() {
        val task = Task("Test", "hidden")

        val context = SerializationContext()

//        val json = ProJson().toJson(task).toJsonString()
        val json = JsonObject(context)
        json.setProperty("desc", JsonPrimitive("Test"))

        assertEquals(json, ProJson().toJson("desc"))

//        assertTrue(json.contains("desc"))
//        assertFalse(json.contains("ignore"))
    }
}
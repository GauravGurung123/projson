package projson

import projson.annotations.JsonIgnore
import projson.annotations.JsonProperty
import kotlin.test.Test
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

        val json = ProJson().toJson(task).toJsonString()

        assertTrue(json.contains("desc"))
        assertFalse(json.contains("ignore"))
    }
}
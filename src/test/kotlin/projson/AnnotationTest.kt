package projson

import projson.annotations.JsonIgnore
import projson.annotations.JsonProperty
import projson.core.JsonObject
import projson.core.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

class AnnotationTest {

    data class Task(

        @JsonProperty("desc")
        val description: String,

        @JsonIgnore
        val ignore: String
    )

    @Test
    fun testAnnotations() {

        val task = Task("Test", "hidden")

        val actual = ProJson().toJson(task) as JsonObject
        val expected = JsonObject(testContext()).apply {
            setProperty(
                "\$id",
                actual.getProperty("\$id")!!
            )
            setProperty("\$type", JsonPrimitive("Task"))
            setProperty("desc", JsonPrimitive("Test"))
        }

        println(expected.toJsonString())
        println(actual.toJsonString())
        assertEquals(expected.toJsonString(), actual.toJsonString())
    }
}
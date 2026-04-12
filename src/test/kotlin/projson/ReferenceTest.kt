package projson

import projson.annotations.Reference
import kotlin.test.Test
import kotlin.test.assertTrue


class ReferenceTest {
    data class Node(
        val name: String,
        var next: Node?
    )
    @Test
    fun testCircularReference() {
        val n1 = Node("A", null)
        val n2 = Node("B", null)

        // ✅ create real cycle using SAME instances
        n1.next = n2
        n2.next = n1

        val json = ProJson().toJson(n1).toJsonString()

        println(json)

        assertTrue(json.contains("\$ref"))
    }
}
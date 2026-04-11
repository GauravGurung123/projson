package projson

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class ReferenceTest {

    data class Node(val name: String, val next: Node?)

    @Test
    fun testCircularReference() {
        val n1 = Node("A", null)
        val n2 = Node("B", n1)
        val n1Updated = n1.copy(next = n2)

        val json = ProJson().toJson(n1Updated).toJsonString()

        assertTrue(json.contains("\$ref"))
        assertTrue(json.contains("\$id"))
    }
}
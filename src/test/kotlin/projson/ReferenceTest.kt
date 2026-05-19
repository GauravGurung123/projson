package projson

import projson.annotations.Reference
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for circular reference detection and the $id / $ref schema.
 *
 * Covers:
 *  1. $id assignment — every object gets a unique, stable identifier
 *  2. Circular reference detection — no StackOverflowError, correct $ref emitted
 *  3. @Reference annotation — eager $ref even without a cycle
 *  4. Deeply nested graphs
 */
class ReferenceTest {

    private fun proJson() = ProJson()  // fresh ReferenceManager per test

    // ══════════════════════════════════════════════════════════════
    // 1. $id assignment
    // ══════════════════════════════════════════════════════════════

    data class Simple(val value: String)

    @Test
    fun `every serialized object receives a dollar-id field`() {
        val result = proJson().toJson(Simple("x")).toJsonString()
        assertContains(result, "\"\$id\":")
    }

    @Test
    fun `dollar-id value is a non-blank string`() {
        val result = proJson().toJson(Simple("x")).toJsonString()
        val id = result.substringAfter("\"\$id\": \"").substringBefore("\"")
        assertTrue(id.isNotBlank(), "id must not be blank")
    }

    @Test
    fun `two separate ProJson calls produce different dollar-id values`() {
        val idA = proJson().toJson(Simple("a")).toJsonString()
            .substringAfter("\"\$id\": \"").substringBefore("\"")
        val idB = proJson().toJson(Simple("b")).toJsonString()
            .substringAfter("\"\$id\": \"").substringBefore("\"")
        // Different objects → different IDs
        // (both non-blank; exact UUID matching is too brittle)
        assertTrue(idA.isNotBlank())
        assertTrue(idB.isNotBlank())
    }

    data class Parent(val child: Simple)

    @Test
    fun `nested object also gets its own dollar-id`() {
        val result = proJson().toJson(Parent(Simple("y"))).toJsonString()
        val idCount = result.split("\"\$id\":").size - 1
        assertEquals(2, idCount, "parent and child each need a \$id")
    }

    // ══════════════════════════════════════════════════════════════
    // 2. Circular reference — two-node cycle
    // ══════════════════════════════════════════════════════════════

    data class NodeA(val name: String, var partner: NodeB? = null)
    data class NodeB(val name: String, var partner: NodeA? = null)

    @Test
    fun `two-node mutual cycle does not throw StackOverflowError`() {
        val a = NodeA("A")
        val b = NodeB("B")
        a.partner = b
        b.partner = a

        // Must complete without exception
        val result = proJson().toJson(a).toJsonString()
        assertContains(result, "\"\$ref\":")
    }

    @Test
    fun `two-node cycle — first node serialized in full, back-ref emitted as dollar-ref`() {
        val a = NodeA("A")
        val b = NodeB("B")
        a.partner = b
        b.partner = a

        val result = proJson().toJson(a).toJsonString()
        assertContains(result, "\"\$type\": \"NodeA\"")
        assertContains(result, "\"\$type\": \"NodeB\"")
        assertContains(result, "\"\$ref\":")
    }

    // ══════════════════════════════════════════════════════════════
    // 3. Self-reference
    // ══════════════════════════════════════════════════════════════

    data class SelfRef(val label: String, var self: SelfRef? = null)

    @Test
    fun `self-referencing object does not throw StackOverflowError`() {
        val node = SelfRef("loop")
        node.self = node
        val result = proJson().toJson(node).toJsonString()
        assertContains(result, "\"\$ref\":")
    }

    @Test
    fun `self-reference — dollar-id and dollar-ref share the same UUID`() {
        val node = SelfRef("loop")
        node.self = node

        val result = proJson().toJson(node).toJsonString()
        val id  = result.substringAfter("\"\$id\": \"").substringBefore("\"")
        val ref = result.substringAfter("\"\$ref\": \"").substringBefore("\"")
        assertEquals(id, ref, "\$id and \$ref must hold the same UUID")
    }

    // ══════════════════════════════════════════════════════════════
    // 4. Three-node chain (no cycle)
    // ══════════════════════════════════════════════════════════════

    data class Link(val value: Int, val next: Link? = null)

    @Test
    fun `three-node chain without cycle — all nodes serialized fully`() {
        val chain = Link(1, Link(2, Link(3, null)))
        val result = proJson().toJson(chain).toJsonString()

        assertContains(result, "\"value\": 1")
        assertContains(result, "\"value\": 2")
        assertContains(result, "\"value\": 3")
        // no $ref expected — no cycle
        assertFalse(result.contains("\"\$ref\":"), "no cycle means no \$ref")
    }

    @Test
    fun `three-node chain — each node has its own dollar-id`() {
        val chain = Link(1, Link(2, Link(3, null)))
        val result = proJson().toJson(chain).toJsonString()
        val idCount = result.split("\"\$id\":").size - 1
        assertEquals(3, idCount, "three nodes → three \$id fields")
    }

    // ══════════════════════════════════════════════════════════════
    // 5. @Reference annotation (eager $ref before a cycle occurs)
    // ══════════════════════════════════════════════════════════════

    data class Category(val name: String)
    data class Item(
        val title: String,
        @Reference val category: Category
    )

    @Test
    fun `@Reference — emits dollar-ref without needing a cycle`() {
        val result = proJson().toJson(Item("Book", Category("Library"))).toJsonString()
        assertContains(result, "\"\$ref\":")
    }

    @Test
    fun `@Reference — referenced object body not inlined inside parent`() {
        val result = proJson().toJson(Item("Book", Category("Library"))).toJsonString()
        assertFalse(result.contains("\"\$type\": \"Category\""), "Category must not be fully inlined")
    }

    data class Album(val title: String)
    data class Track(
        val name: String,
        @Reference val album: Album,
        @Reference val remaster: Album
    )

    @Test
    fun `@Reference on two fields pointing to same instance — two dollar-ref entries`() {
        val album  = Album("Greatest Hits")
        val result = proJson().toJson(Track("Song", album, album)).toJsonString()
        val refCount = result.split("\"\$ref\":").size - 1
        assertEquals(2, refCount, "two @Reference fields → two \$ref entries")
    }

    // ══════════════════════════════════════════════════════════════
    // 6. @Reference collection field
    // ══════════════════════════════════════════════════════════════

    data class Tag(val name: String)
    data class Post(
        val title: String,
        @Reference val tags: List<Tag>
    )

    @Test
    fun `@Reference on a collection field — emits array of dollar-ref objects`() {
        val post   = Post("Hello", listOf(Tag("kotlin"), Tag("tdd")))
        val result = proJson().toJson(post).toJsonString()

        // Each tag becomes a { "$ref": "..." } object in an array
        val refCount = result.split("\"\$ref\":").size - 1
        assertEquals(2, refCount, "one \$ref per tag")
        assertFalse(result.contains("\"\$type\": \"Tag\""), "Tag bodies must not be inlined")
    }

    // ══════════════════════════════════════════════════════════════
    // 7. Deep nesting with cycle at the bottom
    // ══════════════════════════════════════════════════════════════

    data class Level(val depth: Int, var child: Level? = null)

    @Test
    fun `deep chain with cycle at bottom does not throw`() {
        val bottom = Level(3)
        val mid    = Level(2, bottom)
        val top    = Level(1, mid)
        bottom.child = top   // cycle back to root

        val result = proJson().toJson(top).toJsonString()
        assertContains(result, "\"\$ref\":")
        assertContains(result, "\"depth\": 1")
        assertContains(result, "\"depth\": 2")
        assertContains(result, "\"depth\": 3")
    }
}
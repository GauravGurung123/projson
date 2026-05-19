package projson

import projson.context.MappingContext
import projson.mapper.CollectionMapper
import projson.mapper.MapMapper
import projson.mapper.ObjectMapper
import projson.mapper.PrimitiveMapper
import projson.plugin.PluginManager
import projson.reference.ReferenceManager
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for CollectionMapper.
 *
 * Focus:
 *  - canHandle() covers List, Set, Collection; rejects primitives, maps, objects
 *  - map() produces correct JsonArray output for all element types
 *  - Recursive conversion works for nested collections and objects
 */
class CollectionMapperTest {

    private val mapper = CollectionMapper()

    private fun makeContext() = MappingContext(
        mappers          = listOf(PrimitiveMapper(), CollectionMapper(), MapMapper(), ObjectMapper()),
        referenceManager = ReferenceManager(),
        pluginManager    = PluginManager()
    )

    // ─────────────────────────────────────────────────────────────
    // canHandle — positive
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `canHandle returns true for ArrayList`() {
        assertTrue(mapper.canHandle(arrayListOf(1, 2, 3)))
    }

    @Test
    fun `canHandle returns true for LinkedList`() {
        assertTrue(mapper.canHandle(java.util.LinkedList(listOf("a", "b"))))
    }

    @Test
    fun `canHandle returns true for Set`() {
        assertTrue(mapper.canHandle(setOf("x", "y")))
    }

    @Test
    fun `canHandle returns true for empty list`() {
        assertTrue(mapper.canHandle(emptyList<Any>()))
    }

    @Test
    fun `canHandle returns true for mutable list`() {
        assertTrue(mapper.canHandle(mutableListOf(1, 2)))
    }

    // ─────────────────────────────────────────────────────────────
    // canHandle — negative
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `canHandle returns false for null`() {
        assertFalse(mapper.canHandle(null))
    }

    @Test
    fun `canHandle returns false for String`() {
        assertFalse(mapper.canHandle("hello"))
    }

    @Test
    fun `canHandle returns false for Int`() {
        assertFalse(mapper.canHandle(42))
    }

    @Test
    fun `canHandle returns false for Map`() {
        assertFalse(mapper.canHandle(mapOf("a" to 1)))
    }

    @Test
    fun `canHandle returns false for a custom object`() {
        data class Foo(val x: Int)
        assertFalse(mapper.canHandle(Foo(1)))
    }

    // ─────────────────────────────────────────────────────────────
    // map — primitive element types
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `maps an empty list to an empty JSON array`() {
        val result = mapper.map(emptyList<Any>(), makeContext()).toJsonString()
        assertEquals("[]", result)
    }

    @Test
    fun `maps a list of strings`() {
        val result = mapper.map(listOf("a", "b", "c"), makeContext()).toJsonString()
        assertEquals("[\"a\", \"b\", \"c\"]", result)
    }

    @Test
    fun `maps a list of integers`() {
        val result = mapper.map(listOf(1, 2, 3), makeContext()).toJsonString()
        assertEquals("[1, 2, 3]", result)
    }

    @Test
    fun `maps a list of booleans`() {
        val result = mapper.map(listOf(true, false, true), makeContext()).toJsonString()
        assertEquals("[true, false, true]", result)
    }

    @Test
    fun `maps a list containing null`() {
        val result = mapper.map(listOf("x", null, "y"), makeContext()).toJsonString()
        assertEquals("[\"x\", null, \"y\"]", result)
    }

    @Test
    fun `maps a list of all nulls`() {
        val result = mapper.map(listOf(null, null), makeContext()).toJsonString()
        assertEquals("[null, null]", result)
    }

    @Test
    fun `maps a single-element list`() {
        val result = mapper.map(listOf(42), makeContext()).toJsonString()
        assertEquals("[42]", result)
    }

    // ─────────────────────────────────────────────────────────────
    // map — mixed and nested
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `maps a list of mixed primitives`() {
        val result = mapper.map(listOf("hello", 7, true, null), makeContext()).toJsonString()
        assertEquals("[\"hello\", 7, true, null]", result)
    }

    @Test
    fun `maps a nested list — list of lists`() {
        val result = mapper.map(listOf(listOf(1, 2), listOf(3, 4)), makeContext()).toJsonString()
        assertEquals("[[1, 2], [3, 4]]", result)
    }

    @Test
    fun `maps a list of objects — each element gets dollar-type`() {
        data class Color(val name: String)
        val result = mapper.map(listOf(Color("red"), Color("blue")), makeContext()).toJsonString()
        assertContains(result, "\"name\": \"red\"")
        assertContains(result, "\"name\": \"blue\"")
        assertContains(result, "\"\$type\": \"Color\"")
    }

    // ─────────────────────────────────────────────────────────────
    // Set handling
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `maps a single-element set`() {
        val result = mapper.map(setOf("only"), makeContext()).toJsonString()
        assertEquals("[\"only\"]", result)
    }

    @Test
    fun `maps an empty set to an empty array`() {
        val result = mapper.map(emptySet<Any>(), makeContext()).toJsonString()
        assertEquals("[]", result)
    }

    // ─────────────────────────────────────────────────────────────
    // Element order preservation
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `element order is preserved`() {
        val result = mapper.map(listOf(10, 20, 30, 40, 50), makeContext()).toJsonString()
        assertEquals("[10, 20, 30, 40, 50]", result)
    }

    @Test
    fun `string order is preserved`() {
        val result = mapper.map(listOf("z", "a", "m"), makeContext()).toJsonString()
        assertEquals("[\"z\", \"a\", \"m\"]", result)
    }
}
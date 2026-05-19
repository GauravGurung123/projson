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
 * Unit tests for MapMapper.
 *
 * Focus:
 *  - canHandle() accepts Map<*,*> subtypes; rejects everything else
 *  - map() produces correct JsonObject output
 *  - Key coercion to String
 *  - Value delegation to context.convert()
 */
class MapMapperTest {

    private val mapper = MapMapper()

    private fun makeContext() = MappingContext(
        mappers          = listOf(PrimitiveMapper(), CollectionMapper(), MapMapper(), ObjectMapper()),
        referenceManager = ReferenceManager(),
        pluginManager    = PluginManager()
    )

    // ─────────────────────────────────────────────────────────────
    // canHandle — positive
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `canHandle returns true for HashMap`() {
        assertTrue(mapper.canHandle(hashMapOf("a" to 1)))
    }

    @Test
    fun `canHandle returns true for LinkedHashMap`() {
        assertTrue(mapper.canHandle(linkedMapOf("x" to "y")))
    }

    @Test
    fun `canHandle returns true for an empty map`() {
        assertTrue(mapper.canHandle(emptyMap<String, Any>()))
    }

    @Test
    fun `canHandle returns true for a mutable map`() {
        assertTrue(mapper.canHandle(mutableMapOf("k" to 1)))
    }

    // ─────────────────────────────────────────────────────────────
    // canHandle — negative
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `canHandle returns false for null`() {
        assertFalse(mapper.canHandle(null))
    }

    @Test
    fun `canHandle returns false for a String`() {
        assertFalse(mapper.canHandle("hello"))
    }

    @Test
    fun `canHandle returns false for a List`() {
        assertFalse(mapper.canHandle(listOf(1, 2)))
    }

    @Test
    fun `canHandle returns false for a custom object`() {
        data class Foo(val x: Int)
        assertFalse(mapper.canHandle(Foo(1)))
    }

    // ─────────────────────────────────────────────────────────────
    // map — basic key/value output
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `maps an empty map — output contains braces`() {
        val result = mapper.map(emptyMap<String, Any>(), makeContext()).toJsonString()
        assertTrue(result.contains("{") && result.contains("}"))
    }

    @Test
    fun `maps a single string-string entry`() {
        val result = mapper.map(mapOf("lang" to "kotlin"), makeContext()).toJsonString()
        assertContains(result, "\"lang\": \"kotlin\"")
    }

    @Test
    fun `maps a string-int entry`() {
        val result = mapper.map(mapOf("count" to 42), makeContext()).toJsonString()
        assertContains(result, "\"count\": 42")
    }

    @Test
    fun `maps a string-boolean entry`() {
        val result = mapper.map(mapOf("active" to true), makeContext()).toJsonString()
        assertContains(result, "\"active\": true")
    }

    @Test
    fun `maps a string-null entry`() {
        val result = mapper.map(mapOf("value" to null), makeContext()).toJsonString()
        assertContains(result, "\"value\": null")
    }

    @Test
    fun `maps multiple entries`() {
        val result = mapper.map(
            mapOf("name" to "Gaurav", "age" to 25, "active" to true),
            makeContext()
        ).toJsonString()

        assertContains(result, "\"name\": \"Gaurav\"")
        assertContains(result, "\"age\": 25")
        assertContains(result, "\"active\": true")
    }

    // ─────────────────────────────────────────────────────────────
    // Key coercion — non-String keys become String via toString()
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `integer key is coerced to string`() {
        val result = mapper.map(mapOf(1 to "one"), makeContext()).toJsonString()
        assertContains(result, "\"1\": \"one\"")
    }

    @Test
    fun `boolean key is coerced to string`() {
        val result = mapper.map(mapOf(true to "yes"), makeContext()).toJsonString()
        assertContains(result, "\"true\": \"yes\"")
    }

    // ─────────────────────────────────────────────────────────────
    // Nested values
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `map value that is a list is converted to JSON array`() {
        val result = mapper.map(mapOf("tags" to listOf("a", "b")), makeContext()).toJsonString()
        assertContains(result, "\"tags\": [\"a\", \"b\"]")
    }

    @Test
    fun `map value that is another map is converted to nested JSON object`() {
        val result = mapper.map(
            mapOf("meta" to mapOf("version" to 2)),
            makeContext()
        ).toJsonString()
        assertContains(result, "\"version\": 2")
    }

    @Test
    fun `map value that is a custom object is converted via ObjectMapper`() {
        data class Tag(val label: String)
        val result = mapper.map(mapOf("tag" to Tag("kotlin")), makeContext()).toJsonString()
        assertContains(result, "\"label\": \"kotlin\"")
        assertContains(result, "\"\$type\": \"Tag\"")
    }

    // ─────────────────────────────────────────────────────────────
    // Via ProJson public API
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `ProJson toJson on a map produces correct output`() {
        val result = ProJson().toJson(mapOf("x" to 1, "y" to 2)).toJsonString()
        assertContains(result, "\"x\": 1")
        assertContains(result, "\"y\": 2")
    }

    @Test
    fun `ProJson toJson on an empty map produces an object with no user fields`() {
        val result = ProJson().toJson(emptyMap<String, Any>()).toJsonString()
        assertTrue(result.contains("{"))
        assertTrue(result.contains("}"))
    }
}
package projson

import projson.context.MappingContext
import projson.core.JsonPrimitive
import projson.mapper.CollectionMapper
import projson.mapper.MapMapper
import projson.mapper.ObjectMapper
import projson.mapper.PrimitiveMapper
import projson.plugin.PluginManager
import projson.reference.ReferenceManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for PrimitiveMapper.
 *
 * Focus: canHandle() predicate accuracy + map() output for every
 * scalar type the mapper is responsible for.
 */
class PrimitiveMapperTest {

    private val mapper  = PrimitiveMapper()
    private val context = MappingContext(
        mappers          = listOf(PrimitiveMapper(), CollectionMapper(), MapMapper(), ObjectMapper()),
        referenceManager = ReferenceManager(),
        pluginManager    = PluginManager()
    )

    // ─────────────────────────────────────────────────────────────
    // canHandle — positive cases
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `canHandle returns true for null`() {
        assertTrue(mapper.canHandle(null))
    }

    @Test
    fun `canHandle returns true for String`() {
        assertTrue(mapper.canHandle("hello"))
    }

    @Test
    fun `canHandle returns true for Int`() {
        assertTrue(mapper.canHandle(42))
    }

    @Test
    fun `canHandle returns true for Long`() {
        assertTrue(mapper.canHandle(Long.MAX_VALUE))
    }

    @Test
    fun `canHandle returns true for Double`() {
        assertTrue(mapper.canHandle(3.14))
    }

    @Test
    fun `canHandle returns true for Float`() {
        assertTrue(mapper.canHandle(1.5f))
    }

    @Test
    fun `canHandle returns true for Boolean true`() {
        assertTrue(mapper.canHandle(true))
    }

    @Test
    fun `canHandle returns true for Boolean false`() {
        assertTrue(mapper.canHandle(false))
    }

    // ─────────────────────────────────────────────────────────────
    // canHandle — negative cases
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `canHandle returns false for a custom object`() {
        data class Foo(val x: Int)
        assertFalse(mapper.canHandle(Foo(1)))
    }

    @Test
    fun `canHandle returns false for a List`() {
        assertFalse(mapper.canHandle(listOf(1, 2, 3)))
    }

    @Test
    fun `canHandle returns false for a Map`() {
        assertFalse(mapper.canHandle(mapOf("a" to 1)))
    }

    // ─────────────────────────────────────────────────────────────
    // map — output correctness
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `maps null to JsonPrimitive wrapping null`() {
        assertEquals(JsonPrimitive(null), mapper.map(null, context))
    }

    @Test
    fun `maps String to JsonPrimitive — toJsonString wraps in quotes`() {
        val result = mapper.map("world", context)
        assertEquals("\"world\"", result.toJsonString())
    }

    @Test
    fun `maps Int to JsonPrimitive — toJsonString is the number`() {
        val result = mapper.map(99, context)
        assertEquals("99", result.toJsonString())
    }

    @Test
    fun `maps Long to JsonPrimitive — large value preserved`() {
        val result = mapper.map(9_000_000_000L, context)
        assertEquals("9000000000", result.toJsonString())
    }

    @Test
    fun `maps Double to JsonPrimitive`() {
        val result = mapper.map(2.718, context)
        assertEquals("2.718", result.toJsonString())
    }

    @Test
    fun `maps Boolean true to JsonPrimitive`() {
        val result = mapper.map(true, context)
        assertEquals("true", result.toJsonString())
    }

    @Test
    fun `maps Boolean false to JsonPrimitive`() {
        val result = mapper.map(false, context)
        assertEquals("false", result.toJsonString())
    }

    @Test
    fun `maps empty string correctly`() {
        val result = mapper.map("", context)
        assertEquals("\"\"", result.toJsonString())
    }

    @Test
    fun `maps string with special characters correctly`() {
        val result = mapper.map("hello \"world\"", context)
        assertEquals("\"hello \"world\"\"", result.toJsonString())
    }

    @Test
    fun `maps zero integer correctly`() {
        val result = mapper.map(0, context)
        assertEquals("0", result.toJsonString())
    }

    @Test
    fun `maps negative number correctly`() {
        val result = mapper.map(-42, context)
        assertEquals("-42", result.toJsonString())
    }
}
package projson

import projson.plugin.JsonPlugin
import projson.plugin.PluginManager
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

/**
 * Tests for the plugin layer.
 *
 * Split into two sections:
 *  A) PluginManager unit tests — registration, lookup, precedence
 *  B) Integration tests — plugins driving ProJson end-to-end
 */
class PluginTest {

    // ══════════════════════════════════════════════════════════════
    // A) PluginManager unit tests
    // ══════════════════════════════════════════════════════════════

    private class StringPlugin : JsonPlugin {
        override fun supports(clazz: Class<*>) = clazz == String::class.java
        override fun transform(obj: Any) = "STRING:${obj}"
    }

    private class IntPlugin : JsonPlugin {
        override fun supports(clazz: Class<*>) = clazz == Int::class.javaObjectType
        override fun transform(obj: Any) = "INT:${obj}"
    }

    @Test
    fun `findPlugin returns null when no plugins are registered`() {
        val manager = PluginManager()
        assertNull(manager.findPlugin("hello"))
    }

    @Test
    fun `findPlugin returns null when no plugin supports the type`() {
        val manager = PluginManager()
        manager.register(IntPlugin())
        assertNull(manager.findPlugin("hello"))
    }

    @Test
    fun `findPlugin returns the matching plugin`() {
        val manager = PluginManager()
        val plugin  = StringPlugin()
        manager.register(plugin)
        assertEquals(plugin, manager.findPlugin("anything"))
    }

    @Test
    fun `findPlugin returns correct plugin when multiple are registered`() {
        val manager     = PluginManager()
        val strPlugin   = StringPlugin()
        val intPlugin   = IntPlugin()
        manager.register(strPlugin)
        manager.register(intPlugin)

        assertEquals(strPlugin, manager.findPlugin("hello"))
        assertEquals(intPlugin, manager.findPlugin(42))
    }

    @Test
    fun `first registered plugin wins when two support the same type`() {
        class AlphaPlugin : JsonPlugin {
            override fun supports(clazz: Class<*>) = clazz == String::class.java
            override fun transform(obj: Any) = "ALPHA"
        }
        class BetaPlugin : JsonPlugin {
            override fun supports(clazz: Class<*>) = clazz == String::class.java
            override fun transform(obj: Any) = "BETA"
        }

        val manager = PluginManager()
        manager.register(AlphaPlugin())
        manager.register(BetaPlugin())

        val result = manager.findPlugin("test")!!.transform("test")
        assertEquals("ALPHA", result)
    }

    @Test
    fun `multiple plugins registered — each handles only its own type`() {
        val manager = PluginManager()
        manager.register(StringPlugin())
        manager.register(IntPlugin())

        assertEquals("STRING:hello", manager.findPlugin("hello")!!.transform("hello"))
        assertEquals("INT:7",        manager.findPlugin(7)!!.transform(7))
        assertNull(manager.findPlugin(3.14))
    }

    // ══════════════════════════════════════════════════════════════
    // B) Integration — plugins via ProJson public API
    // ══════════════════════════════════════════════════════════════

    // ── String plugin ─────────────────────────────────────────────

    private class UpperPlugin : JsonPlugin {
        override fun supports(clazz: Class<*>) = clazz == String::class.java
        override fun transform(obj: Any) = (obj as String).uppercase()
    }

    @Test
    fun `String plugin — transforms every string value`() {
        val pj = ProJson()
        pj.registerPlugin(UpperPlugin())
        assertEquals("\"HELLO WORLD\"", pj.toJson("hello world").toJsonString())
    }

    @Test
    fun `String plugin — transforms string fields inside an object`() {
        data class Msg(val text: String, val code: Int)
        val pj = ProJson()
        pj.registerPlugin(UpperPlugin())
        val result = pj.toJson(Msg("hello", 1)).toJsonString()
        assertContains(result, "\"text\": \"HELLO\"")
        assertContains(result, "\"code\": 1")      // Int unaffected
    }

    // ── Boolean plugin ────────────────────────────────────────────

    private class YesNoPlugin : JsonPlugin {
        override fun supports(clazz: Class<*>) = clazz == Boolean::class.javaObjectType
        override fun transform(obj: Any) = if (obj as Boolean) "yes" else "no"
    }

    @Test
    fun `Boolean plugin — true becomes custom label`() {
        val pj = ProJson()
        pj.registerPlugin(YesNoPlugin())
        assertEquals("\"yes\"", pj.toJson(true).toJsonString())
    }

    @Test
    fun `Boolean plugin — false becomes custom label`() {
        val pj = ProJson()
        pj.registerPlugin(YesNoPlugin())
        assertEquals("\"no\"", pj.toJson(false).toJsonString())
    }

    // ── Custom class plugin ───────────────────────────────────────

    data class Rgb(val r: Int, val g: Int, val b: Int)

    private class RgbPlugin : JsonPlugin {
        override fun supports(clazz: Class<*>) = clazz == Rgb::class.java
        override fun transform(obj: Any): String {
            val c = obj as Rgb
            return "rgb(${c.r},${c.g},${c.b})"
        }
    }

    @Test
    fun `custom class plugin — object serialized as string`() {
        val pj = ProJson()
        pj.registerPlugin(RgbPlugin())
        assertEquals("\"rgb(255,0,128)\"", pj.toJson(Rgb(255, 0, 128)).toJsonString())
    }

    @Test
    fun `custom class plugin — field inside parent serialized as string`() {
        data class Theme(val name: String, val primary: Rgb)
        val pj = ProJson()
        pj.registerPlugin(RgbPlugin())
        val result = pj.toJson(Theme("dark", Rgb(0, 0, 0))).toJsonString()
        assertContains(result, "\"primary\": \"rgb(0,0,0)\"")
    }

    // ── Plugin isolation ──────────────────────────────────────────

    @Test
    fun `plugin does not affect types it does not support`() {
        val pj = ProJson()
        pj.registerPlugin(RgbPlugin())
        // Int must pass through PrimitiveMapper unchanged
        assertEquals("99", pj.toJson(99).toJsonString())
    }

    @Test
    fun `plugin registered on one ProJson instance does not affect another`() {
        val pj1 = ProJson()
        val pj2 = ProJson()
        pj1.registerPlugin(UpperPlugin())

        assertEquals("\"HELLO\"", pj1.toJson("hello").toJsonString())
        assertEquals("\"hello\"", pj2.toJson("hello").toJsonString())
    }

    // ── Plugin in list ────────────────────────────────────────────

    @Test
    fun `plugin applies to objects inside a list`() {
        val pj = ProJson()
        pj.registerPlugin(RgbPlugin())
        val result = pj.toJson(listOf(Rgb(1, 2, 3), Rgb(4, 5, 6))).toJsonString()
        assertContains(result, "\"rgb(1,2,3)\"")
        assertContains(result, "\"rgb(4,5,6)\"")
    }

    // ── Plugin beats ObjectMapper ─────────────────────────────────

    @Test
    fun `plugin overrides ObjectMapper for the matching type — no dollar-id emitted`() {
        val pj = ProJson()
        pj.registerPlugin(RgbPlugin())
        val result = pj.toJson(Rgb(10, 20, 30)).toJsonString()
        assertFalse(result.contains("\$id"),   "\$id must not appear — plugin takes precedence")
        assertFalse(result.contains("\$type"), "\$type must not appear — plugin takes precedence")
    }
}
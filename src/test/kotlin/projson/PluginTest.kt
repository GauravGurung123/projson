package projson

import projson.plugin.JsonPlugin
import kotlin.test.Test
import kotlin.test.assertEquals

class PluginTest {

    data class Date(val day: Int, val month: Int, val year: Int)

    class DatePlugin : JsonPlugin {
        override fun supports(clazz: Class<*>) =
            clazz.simpleName == "Date"

        override fun transform(obj: Any): String {
            val d = obj as Date
            return "${d.day}/${d.month}/${d.year}"
        }
    }

    @Test
    fun testPlugin() {
        val proJson = ProJson()
        proJson.registerPlugin(DatePlugin())

        val json = proJson.toJson(Date(1,1,2026)).toJsonString()
        println(json)
        assertEquals("\"1/1/2026\"", json)
    }
}
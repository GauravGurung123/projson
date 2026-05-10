package projson

import app.Date
import app.DateAsText
import kotlin.test.Test
import kotlin.test.assertEquals

class PluginTest {

    @Test
    fun testPluginTransformation() {

        val proJson = ProJson()

        proJson.registerPlugin(DateAsText())

        val date = Date(30, 2, 2026)

        val actual = proJson.toJson(date)
            .toJsonString()

        assertEquals("\"30/2/2026\"", actual)
    }
}
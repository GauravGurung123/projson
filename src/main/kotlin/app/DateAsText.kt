package org.gojson.app

import projson.plugin.JsonPlugin

class DateAsText : JsonPlugin {
    override fun supports(clazz: Class<*>) =
        clazz.simpleName == "DateAsText"

    override fun serialize(obj: Any): String {
        val d = obj as Date
        return "${d.day}/${d.month}/${d.year}"
    }
}
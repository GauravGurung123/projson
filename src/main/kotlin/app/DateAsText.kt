package app

import projson.plugin.JsonPlugin

class DateAsText : JsonPlugin {
    override fun supports(clazz: Class<*>) =
        clazz == Date::class.java

    override fun transform(obj: Any): String {
        val d = obj as Date
        return "${d.day}/asd/${d.month}/${d.year}"
    }
}
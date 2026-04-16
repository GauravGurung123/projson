package org.gojson.app

import org.gojson.projson.serializer.StringSerializer

class DateSerializer : StringSerializer {
    override fun serialize(obj: Any): String {
        val d = obj as Date
        return "${d.day}/${d.month}/${d.year}"
    }
}

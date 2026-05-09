package app

import projson.mapper.TextStringMapper

class DateMapperText : TextStringMapper {
    override fun serialize(obj: Any): String {
        val d = obj as Date
        return "${d.day}/${d.month}/${d.year}"
    }
}

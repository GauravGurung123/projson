package app

import projson.mapper.TextStringMapper

class DateMapperText : TextStringMapper {
    override fun map(obj: Any): String {
        val d = obj as Date
        return "${d.day}/${d.month}/${d.year}"
    }
}

package app

import projson.mapper.TextMapper

class DateMapperText : TextMapper {
    override fun map(obj: Any): String {
        val d = obj as Date
        return "${d.day}/${d.month}/${d.year}"
    }
}

package projson.mapper

import projson.context.MappingContext
import projson.core.JsonArray
import projson.core.JsonElement

class CollectionMapper : JsonMapper {
    override fun canHandle(obj: Any?) = obj is Collection<*>

    override fun map(obj: Any?, context: MappingContext): JsonElement {
        val array = JsonArray(context)
        (obj as Collection<*>).forEach {
            array.add(context.convert(it))
        }
        return array
    }
}
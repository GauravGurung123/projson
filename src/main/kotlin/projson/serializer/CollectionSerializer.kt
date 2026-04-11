package projson.serializer

import projson.context.SerializationContext
import projson.core.JsonArray
import projson.core.JsonElement

class CollectionSerializer : JsonSerializer {
    override fun canHandle(obj: Any?) = obj is Collection<*>

    override fun serialize(obj: Any?, context: SerializationContext): JsonElement {
        val array = JsonArray(context)
        (obj as Collection<*>).forEach {
            array.add(context.serialize(it))
        }
        return array
    }
}
package projson.serializer

import projson.context.SerializationContext
import projson.core.JsonElement
import projson.core.JsonObject

class MapSerializer : JsonSerializer {

    override fun canHandle(obj: Any?) = obj is Map<*, *>

    override fun serialize(obj: Any?, context: SerializationContext): JsonElement {
        val json = JsonObject(context)

        (obj as Map<*, *>).forEach { (key, value) ->
            json.setProperty(key.toString(), value)
        }

        return json
    }
}
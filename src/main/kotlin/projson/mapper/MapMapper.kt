package projson.mapper

import projson.context.MappingContext
import projson.core.JsonElement
import projson.core.JsonObject

class MapMapper : JsonMapper {

    override fun canHandle(obj: Any?) = obj is Map<*, *>

    override fun map(obj: Any?, context: MappingContext): JsonElement {
        val json = JsonObject(context)

        (obj as Map<*, *>).forEach { (key, value) ->
            json.setProperty(key.toString(), value)
        }

        return json
    }
}
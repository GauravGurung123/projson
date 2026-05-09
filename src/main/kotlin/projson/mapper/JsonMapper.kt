package projson.mapper

import projson.context.SerializationContext
import projson.core.JsonElement

interface JsonMapper {
    fun canHandle(obj: Any?): Boolean
    fun serialize(obj: Any?, context: SerializationContext): JsonElement
}
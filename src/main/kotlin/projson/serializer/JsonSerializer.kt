package projson.serializer

import projson.context.SerializationContext
import projson.core.JsonElement

interface JsonSerializer {
    fun canHandle(obj: Any?): Boolean
    fun serialize(obj: Any?, context: SerializationContext): JsonElement
}
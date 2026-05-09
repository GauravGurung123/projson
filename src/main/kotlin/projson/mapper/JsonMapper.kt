package projson.mapper

import projson.context.MappingContext
import projson.core.JsonElement

interface JsonMapper {
    fun canHandle(obj: Any?): Boolean
    fun map(obj: Any?, context: MappingContext): JsonElement
}
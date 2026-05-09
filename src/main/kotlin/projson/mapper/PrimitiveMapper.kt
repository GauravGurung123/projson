package projson.mapper

import projson.context.MappingContext
import projson.core.JsonPrimitive

class PrimitiveMapper : JsonMapper {
    override fun canHandle(obj: Any?) =
        obj == null || obj is String || obj is Number || obj is Boolean

    override fun map(obj: Any?, context: MappingContext) =
        JsonPrimitive(obj)
}
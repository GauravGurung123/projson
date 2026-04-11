package projson.serializer

class PrimitiveSerializer : JsonSerializer {
    override fun canHandle(obj: Any?) =
        obj == null || obj is String || obj is Number || obj is Boolean

    override fun serialize(obj: Any?, context: SerializationContext) =
        JsonPrimitive(obj)
}
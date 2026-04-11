package projson.serializer

class CollectionSerializer : JsonSerializer {
    override fun canHandle(obj: Any?) = obj is Collection<*>

    override fun serialize(obj: Any?, context: SerializationContext): JsonElement {
        val array = JsonArray()
        (obj as Collection<*>).forEach {
            array.add(context.serialize(it))
        }
        return array
    }
}
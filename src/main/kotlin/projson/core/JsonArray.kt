package projson.core

import projson.context.SerializationContext

class JsonArray(
    private val context: SerializationContext
) : JsonElement() {

    private val items = mutableListOf<JsonElement>()

    fun add(value: Any?) {
        items.add(context.serialize(value))
    }

    override fun toJsonString(indent: String): String {
        val inner = items.joinToString(", ") { it.toJsonString(indent) }
        return "[$inner]"
    }
}
package projson.core

import projson.context.MappingContext

data class JsonArray(
    private val context: MappingContext
) : JsonElement() {

    private val items = mutableListOf<JsonElement>()

    fun add(value: Any?) {
        items.add(context.convert(value))
    }

    override fun toJsonString(indent: String): String {
        val inner = items.joinToString(", ") { it.toJsonString(indent) }
        return "[$inner]"
    }
}
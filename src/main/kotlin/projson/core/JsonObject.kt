package projson.core

import projson.context.SerializationContext

data class JsonObject(
    private val context: SerializationContext
) : JsonElement() {

    private val properties = mutableMapOf<String, JsonElement>()

    fun setProperty(key: String, value: Any?) {
        val element = when (value) {
            null -> JsonNull
            is JsonElement -> value
            else -> context.serialize(value)
        }

        properties[key] = element
    }

    override fun toJsonString(indent: String): String {
        val inner = properties.entries.joinToString(",\n") {
            "$indent  \"${it.key}\": ${it.value.toJsonString("$indent  ")}"
        }
        return "{\n$inner\n$indent}"
    }
}
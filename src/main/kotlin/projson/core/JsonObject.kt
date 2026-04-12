package projson.core

import projson.context.SerializationContext

class JsonObject(
    private val context: SerializationContext
) : JsonElement() {

    private val properties = mutableMapOf<String, JsonElement>()

    fun setProperty(key: String, value: Any?) {
        properties[key] = if (value is JsonElement) {
            value
        } else {
            context.serialize(value)
        }
    }

    fun getProperty(key: String): JsonElement? = properties[key]

    fun removeProperty(key: String) {
        properties.remove(key)
    }

    fun keys(): Set<String> = properties.keys

    override fun toJsonString(indent: String): String {
        val inner = properties.entries.joinToString(",\n") {
            "$indent  \"${it.key}\": ${it.value.toJsonString(indent + "  ")}"
        }
        return "{\n$inner\n$indent}"
    }
}
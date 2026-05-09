package projson.core

import projson.context.MappingContext

data class JsonObject(
    private val context: MappingContext
) : JsonElement() {

    private val properties = mutableMapOf<String, JsonElement>()

    fun setProperty(key: String, value: Any?) {
        val element = when (value) {
            null -> JsonNull
            is JsonElement -> value
            else -> context.convert(value)
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
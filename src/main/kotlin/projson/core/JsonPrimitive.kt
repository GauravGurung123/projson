package projson.core
data class JsonPrimitive(private val value: Any?) : JsonElement() {
    override fun toJsonString(indent: String): String {
        return when (value) {
            null -> "null"
            is String -> "\"$value\""
            else -> value.toString()
        }
    }
}
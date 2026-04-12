package projson.core

object JsonNull : JsonElement() {
    override fun toJsonString(indent: String): String = "null"
}
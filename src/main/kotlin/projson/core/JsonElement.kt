package projson.core

sealed class JsonElement {
    abstract fun toJsonString(indent: String = ""): String
}
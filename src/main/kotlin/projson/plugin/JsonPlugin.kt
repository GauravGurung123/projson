package projson.plugin

interface JsonPlugin {
    fun supports(clazz: Class<*>): Boolean
    fun serialize(obj: Any): String
}
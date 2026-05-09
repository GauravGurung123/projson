package projson.plugin

interface JsonPlugin {
    fun supports(clazz: Class<*>): Boolean
    fun transform(obj: Any): String
}
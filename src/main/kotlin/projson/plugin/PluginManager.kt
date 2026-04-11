package projson.plugin

class PluginManager {
    private val plugins = mutableListOf<JsonPlugin>()

    fun register(plugin: JsonPlugin) {
        plugins.add(plugin)
    }

    fun findPlugin(obj: Any): JsonPlugin? {
        return plugins.find { it.supports(obj::class.java) }
    }
}
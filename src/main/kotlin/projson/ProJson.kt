package projson

import projson.context.MappingContext
import projson.core.JsonElement
import projson.plugin.JsonPlugin
import projson.plugin.PluginManager
import projson.reference.ReferenceManager
import projson.mapper.CollectionMapper
import projson.mapper.MapMapper
import projson.mapper.ObjectMapper
import projson.mapper.PrimitiveMapper

class ProJson {

    private val context = MappingContext(
        mappers = listOf(
            PrimitiveMapper(),
            CollectionMapper(),
            MapMapper(),
            ObjectMapper(),
        ),
        referenceManager = ReferenceManager(),
        pluginManager = PluginManager()
    )

    fun registerPlugin(plugin: JsonPlugin) {
        context.pluginManager.register(plugin)
    }

    fun toJson(obj: Any?): JsonElement {
        return context.convert(obj)
    }
}
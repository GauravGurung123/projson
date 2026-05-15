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

/**
 * Main entry point for ProJson.
 *
 * @constructor
 * Initializes a new instance of ProJson with default configuration.
 *
 * @property context The mapping context used for converting objects to JSON.
 *
 * @property referenceManager The reference manager used for managing object references.
 *
 * @property pluginManager The plugin manager used for managing JSON plugins.
 *
 * @property mappers The list of mappers used for converting objects to JSON.
 *
 * @property context The mapping context used for converting objects to JSON.
 */
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
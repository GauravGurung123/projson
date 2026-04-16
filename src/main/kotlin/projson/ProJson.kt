package projson

import projson.context.SerializationContext
import projson.core.JsonElement
import projson.plugin.JsonPlugin
import projson.plugin.PluginManager
import projson.reference.ReferenceManager
import projson.serializer.CollectionSerializer
import projson.serializer.MapSerializer
import projson.serializer.ObjectSerializer
import projson.serializer.PrimitiveSerializer

class ProJson {

    private val context = SerializationContext(
        serializers = listOf(
            PrimitiveSerializer(),
            CollectionSerializer(),
            MapSerializer(),
            ObjectSerializer(),
        ),
        referenceManager = ReferenceManager(),
        pluginManager = PluginManager()
    )

    fun registerPlugin(plugin: JsonPlugin) {
        context.pluginManager.register(plugin)
    }

    fun toJson(obj: Any?): JsonElement {
        return context.serialize(obj)
    }
}
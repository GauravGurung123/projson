package projson.context

import projson.core.JsonElement
import projson.core.JsonPrimitive
import projson.plugin.PluginManager
import projson.reference.ReferenceManager
import projson.serializer.JsonSerializer

class SerializationContext(
    val serializers: List<JsonSerializer>,
    val referenceManager: ReferenceManager,
    val pluginManager: PluginManager
) {
    fun serialize(obj: Any?): JsonElement {

        // Plugin first (Open/Closed)
        if (obj != null) {
            val plugin = pluginManager.findPlugin(obj)
            if (plugin != null) {
                return JsonPrimitive(plugin.serialize(obj))
            }
        }

        val serializer = serializers.first { it.canHandle(obj) }
        return serializer.serialize(obj, this)
    }
}
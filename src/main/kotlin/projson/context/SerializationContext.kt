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

        if (obj == null) return JsonPrimitive(null)

        // ✅ CRITICAL FIX
        if (obj is JsonElement) return obj

        // ✅ Plugin FIRST (single place only)
        pluginManager.findPlugin(obj)?.let {
            return JsonPrimitive(it.serialize(obj))
        }

        val serializer = serializers.first { it.canHandle(obj) }
        return serializer.serialize(obj, this)
    }
}
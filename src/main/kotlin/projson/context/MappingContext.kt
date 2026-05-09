package projson.context

import projson.core.JsonElement
import projson.core.JsonPrimitive
import projson.plugin.PluginManager
import projson.reference.ReferenceManager
import projson.mapper.JsonMapper

class MappingContext(
    val mappers: List<JsonMapper>,
    val referenceManager: ReferenceManager,
    val pluginManager: PluginManager
) {
    fun convert(obj: Any?): JsonElement {

        if (obj == null) return JsonPrimitive(null)

        // ✅ CRITICAL FIX
        if (obj is JsonElement) return obj

        // ✅ Plugin FIRST (single place only)
        pluginManager.findPlugin(obj)?.let {
            return JsonPrimitive(it.transform(obj))
        }

        val mapper = mappers.first { it.canHandle(obj) }
        return mapper.map(obj, this)
    }
}
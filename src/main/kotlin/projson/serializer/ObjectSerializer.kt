package projson.serializer

import projson.annotations.JsonIgnore
import projson.annotations.JsonProperty
import projson.annotations.Reference
import projson.context.SerializationContext
import projson.core.JsonElement
import projson.core.JsonObject
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible
class ObjectSerializer : JsonSerializer {

    override fun canHandle(obj: Any?) = obj != null

    override fun serialize(obj: Any?, context: SerializationContext): JsonElement {

        val refManager = context.referenceManager
        val nonNullObj = obj!!

        val id = refManager.getOrCreateId(nonNullObj)

        // ✅ If already serialized → return $ref
        if (refManager.isSerialized(nonNullObj)) {
            return JsonObject(context).apply {
                setProperty("\$ref", id)
            }
        }

        // ✅ Mark before traversing (prevents infinite recursion)
        refManager.markSerialized(nonNullObj)

        val clazz = nonNullObj::class
        val json = JsonObject(context)

        json.setProperty("\$id", id)
        json.setProperty("\$type", clazz.simpleName!!)

        clazz.memberProperties
            .filterIsInstance<KProperty1<Any, *>>()
            .forEach { prop ->
                prop.isAccessible = true

                // ❌ Ignore field
                if (prop.annotations.any { it is JsonIgnore }) return@forEach

                // ✅ Property name override
                val name = prop.annotations
                    .filterIsInstance<JsonProperty>()
                    .firstOrNull()?.name ?: prop.name

                val value = prop.get(nonNullObj)

                // ✅ Reference annotation handling
                val isAnnotatedRef = prop.annotations.any { it is Reference }

                val isCircularRef = value != null &&
                        refManager.isSerialized(value) &&
                        value !is String &&
                        value !is Number &&
                        value !is Boolean

                val shouldUseRef = isAnnotatedRef || isCircularRef

//                if (prop.annotations.any { it is Reference }) {
                if (shouldUseRef) {
                    when (value) {
                        null -> json.setProperty(name, null)

                        is Collection<*> -> {
                            val array = projson.core.JsonArray(context)
                            value.forEach { item ->
                                if (item == null) {
                                    array.add(null)
                                } else {
                                    val refId = refManager.getOrCreateId(item)
                                    array.add(
                                        JsonObject(context).apply {
                                            setProperty("\$ref", refId)
                                        }
                                    )
                                }
                            }
                            json.setProperty(name, array)
                        }

                        else -> {
                            val refId = refManager.getOrCreateId(value)
                            json.setProperty(name, JsonObject(context).apply {
                                setProperty("\$ref", refId)
                            })
                        }
                    }

                } else {
                    json.setProperty(name, context.serialize(value))
                }
            }

        return json
    }
}
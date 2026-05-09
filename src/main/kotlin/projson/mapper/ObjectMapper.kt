package projson.mapper

import projson.annotations.JsonIgnore
import projson.annotations.JsonProperty
import projson.annotations.Reference
import projson.annotations.JsonString
import projson.context.SerializationContext
import projson.core.JsonElement
import projson.core.JsonObject
import projson.core.JsonPrimitive
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

class ObjectMapper : JsonMapper {

    // IMPORTANT: avoid re-serializing JsonElement
    override fun canHandle(obj: Any?) =
        obj != null && obj !is JsonElement

    override fun serialize(obj: Any?, context: SerializationContext): JsonElement {

        val nonNullObj = obj!!
        val clazz = nonNullObj::class
        val refManager = context.referenceManager

        // STEP 1: @JsonString (highest priority)
        val jsonStringAnnotation = clazz.annotations
            .filterIsInstance<JsonString>()
            .firstOrNull()

        if (jsonStringAnnotation != null) {
            val serializerClass = jsonStringAnnotation.serializer
            val serializer = serializerClass.constructors.first().call()

            val result = serializer.serialize(nonNullObj)

            // 🚫 DO NOT register references for primitive conversion
            return JsonPrimitive(result)
        }

        // STEP 2: Reference handling (only for real objects)
        val id = refManager.getOrCreateId(nonNullObj)

        if (refManager.isSerialized(nonNullObj)) {
            return JsonObject(context).apply {
                setProperty("\$ref", id)
            }
        }

        // mark before traversal (prevents circular recursion)
        refManager.markSerialized(nonNullObj)

        val json = JsonObject(context)

        json.setProperty("\$id", id)
        json.setProperty("\$type", clazz.simpleName!!)

        clazz.memberProperties
            .filterIsInstance<KProperty1<Any, *>>()
            .forEach { prop ->

                prop.isAccessible = true

                // Ignore field
                if (prop.annotations.any { it is JsonIgnore }) return@forEach

                // Resolve name
                val name = prop.annotations
                    .filterIsInstance<JsonProperty>()
                    .firstOrNull()?.name ?: prop.name

                val value = prop.get(nonNullObj)

                // Reference rules
                val isAnnotatedRef = prop.annotations.any { it is Reference }

                val isCircularRef = value != null &&
                        refManager.isSerialized(value) &&
                        value !is String &&
                        value !is Number &&
                        value !is Boolean &&
                        value !is JsonElement

                val shouldUseRef = isAnnotatedRef || isCircularRef

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
                    // Delegate safely
                    json.setProperty(name, context.serialize(value))
                }
            }

        return json
    }
}
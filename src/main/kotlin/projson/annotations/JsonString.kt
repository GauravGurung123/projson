package projson.annotations

import projson.mapper.TextStringMapper
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
annotation class JsonString(val serializer: KClass<out TextStringMapper>)
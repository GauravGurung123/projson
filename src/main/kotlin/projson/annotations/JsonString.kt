package projson.annotations

import projson.mapper.TextMapper
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
annotation class JsonString(val mapper: KClass<out TextMapper>)
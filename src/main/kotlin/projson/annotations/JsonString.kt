package projson.annotations

import projson.plugin.JsonPlugin
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
annotation class JsonString(val plugin: KClass<out JsonPlugin>)
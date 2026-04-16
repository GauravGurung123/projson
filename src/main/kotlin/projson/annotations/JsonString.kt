package projson.annotations

import org.gojson.projson.serializer.StringSerializer
import projson.plugin.JsonPlugin
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
annotation class JsonString(val serializer: KClass<out StringSerializer>)
package projson

import projson.context.MappingContext
import projson.mapper.CollectionMapper
import projson.mapper.MapMapper
import projson.mapper.ObjectMapper
import projson.mapper.PrimitiveMapper
import projson.plugin.PluginManager
import projson.reference.ReferenceManager

fun testContext() = MappingContext(
    mappers = listOf(
        PrimitiveMapper(),
        CollectionMapper(),
        MapMapper(),
        ObjectMapper()
    ),
    referenceManager = ReferenceManager(),
    pluginManager = PluginManager()
)
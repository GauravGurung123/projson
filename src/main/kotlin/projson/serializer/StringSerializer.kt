package org.gojson.projson.serializer

interface StringSerializer {
    fun serialize(obj: Any): String
}
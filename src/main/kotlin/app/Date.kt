package org.gojson.app

import app.DateSerializer
import projson.annotations.JsonString

@JsonString(DateSerializer::class)
data class Date(
    val day: Int,
    val month: Int,
    val year: Int
)
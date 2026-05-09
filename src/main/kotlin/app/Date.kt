package app

import projson.annotations.JsonString

@JsonString(DateMapperText::class)
data class Date(
    val day: Int,
    val month: Int,
    val year: Int
)
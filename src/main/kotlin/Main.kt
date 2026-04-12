package org.gojson
import projson.ProJson
//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {
    val t = listOf<String>("T1","T2","T3")

    val json = ProJson().toJson(t)

    println(json.toJsonString())
    val t1 = mapOf(
        "key1" to mapOf(
            "key1" to "T1",
            "key2" to "T2",
            "key3" to "T3"
        ),
        "key2" to "T2",
        "key3" to "T3"
    )

    val json1 = ProJson().toJson(t1)

    println(json1.toJsonString())
}
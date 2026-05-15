package org.gojson
import app.Date
import app.DateAsText
import projson.ProJson
import projson.core.JsonArray

class Task(
    val description: String,
    val deadline: Date?,
    val dependencies: List<Task>
)
//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {
//    val t = listOf<String>("T1","T2","T3")
//
//    val json = ProJson().toJson(t)
//
//    println(json.toJsonString())
//    val t1 = mapOf(
//        "key1" to mapOf(
//            "key1" to "T1",
//            "key2" to "T2",
//            "key3" to "T3"
//        ),
//        "key2" to "T2",
//        "key3" to "T3"
//    )
//
//    val json1 = ProJson().toJson(t1)
//
//    println(json1.toJsonString())
//
//    val t2 = Task(
//        description = "T1",
//        deadline = Date(30, 2, 2026),
//        dependencies = emptyList()
//    )
//
//    val json2 = ProJson().toJson(t2)
//
//    println(json2.toJsonString())

//    val d = Date(31, 4, 2026)
//    val json3 = ProJson().toJson(d) as JsonObject
//    json3.setProperty("year", 2027)
//
//    println(json3.toJsonString())

    val proJson = ProJson()
    proJson.registerPlugin(DateAsText())
//
    val d1 = Date(30, 2, 2026)
    val d2 = Date(31, 4, 2026)

    val json2 = proJson.toJson(listOf(d1, d2)) as JsonArray

    val json4 = proJson.toJson(d1)
    println(json2.toJsonString())
    println(json4.toJsonString())
//

//    val t1 = Task("T1", Date(30,2,2026), emptyList())
//    val t2 = Task("T2", Date(31,4,2026), emptyList())
//    val t3 = Task("T3", null, listOf(t1, t2))
//    val all = listOf(t1, t2, t3)
//    val json = ProJson().toJson(all) as JsonArray
//    println(json.toJsonString())

}
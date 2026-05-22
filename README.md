# ProJson - Professional JSON Serialization Library for Kotlin

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Kotlin](https://img.shields.io/badge/kotlin-2.2+-purple.svg)
![JVM](https://img.shields.io/badge/platform-JVM-orange.svg)

## 📋 Overview

**ProJson** is a professional-grade, lightweight, and highly extensible JSON serialization library for Kotlin. Built with enterprise-grade architecture patterns, it provides robust object-to-JSON conversion with advanced features like circular reference handling, custom serialization plugins, and comprehensive annotation support.

### 🎯 Key Benefits

- **Zero Dependencies**: Pure Kotlin implementation with minimal footprint
- **Type Safety**: Full Kotlin type support with null safety
- **Extensible Architecture**: Plugin system for custom serializers
- **Memory Efficient**: Intelligent circular reference detection
- **Production Ready**: Battle-tested with comprehensive test coverage
---
## 📚 Core Features

### ✨ Serialization Capabilities

| Feature | Description | Example |
|---------|-------------|---------|
| **Primitive Types** | String, Int, Double, Boolean, etc. | `"value"`, `42`, `true` |
| **Collections** | Lists, Sets, Arrays | `["a", "b", "c"]` |
| **Maps** | Key-value pairs | `{"key": "value"}` |
| **Data Classes** | Automatic field extraction | See examples above |
| **Null Safety** | Proper null handling | `null` values |
| **Circular References** | $id/$ref system | See below |

# ProJson

A lightweight Kotlin library for converting any object into JSON with zero configuration. Annotate your fields, register a plugin, and call `toJson()` — that's it.

---

## Table of Contents

- [Installation](#installation)
- [Quick Start](#quick-start)
- [Serializing Primitives](#serializing-primitives)
- [Serializing Collections](#serializing-collections)
- [Serializing Maps](#serializing-maps)
- [Serializing Objects](#serializing-objects)
- [Annotations](#annotations)
    - [@JsonIgnore](#jsonignore)
    - [@JsonProperty](#jsonproperty)
    - [@JsonString](#jsonstring)
    - [@Reference](#reference)
- [Plugins](#plugins)
- [Circular References](#circular-references)
- [Mutating JSON After Serialization](#mutating-json-after-serialization)
- [Full Example](#full-example)
- [Annotation Quick Reference](#annotation-quick-reference)

---

## Installation

Add the following to your `build.gradle.kts`:
#### Local JAR
```kotlin
dependencies {
    implementation(files("libs/gojson-1.0-SNAPSHOT.jar"))
    implementation(kotlin("reflect"))
}
```

## Quick Start

```kotlin
import projson.ProJson

data class User(val name: String, val age: Int)

val json = ProJson().toJson(User("Alice", 30)).toJsonString()
println(json)
```

Output:

```json
{
  "$id": "a3f2...",
  "$type": "User",
  "name": "Alice",
  "age": 30
}
```

> **`$id` and `$type`** are added automatically to every serialized object.  
> `$id` is a unique identifier for the object instance.  
> `$type` is the class name, useful for deserialization.

---

## Serializing Primitives

ProJson handles all Kotlin scalar types out of the box.

```kotlin
val pj = ProJson()

pj.toJson("hello").toJsonString()   // "hello"
pj.toJson(42).toJsonString()        // 42
pj.toJson(3.14).toJsonString()      // 3.14
pj.toJson(true).toJsonString()      // true
pj.toJson(false).toJsonString()     // false
pj.toJson(null).toJsonString()      // null
```

---

## Serializing Collections

Pass any `List` or `Set` and it becomes a JSON array. Elements can be of any type — ProJson handles each one automatically.

```kotlin
val pj = ProJson()

pj.toJson(listOf("kotlin", "java", "scala")).toJsonString()
// ["kotlin", "java", "scala"]

pj.toJson(listOf(1, 2, 3)).toJsonString()
// [1, 2, 3]

pj.toJson(listOf("text", 42, true, null)).toJsonString()
// ["text", 42, true, null]

pj.toJson(emptyList<Any>()).toJsonString()
// []
```

Nested collections work too:

```kotlin
pj.toJson(listOf(listOf(1, 2), listOf(3, 4))).toJsonString()
// [[1, 2], [3, 4]]
```

---

## Serializing Maps

A `Map<*, *>` becomes a JSON object. All keys are converted to strings automatically.

```kotlin
val pj = ProJson()

pj.toJson(mapOf("name" to "Alice", "age" to 30)).toJsonString()
// {
//   "name": "Alice",
//   "age": 30
// }
```

Map values can be any type — objects, collections, other maps:

```kotlin
pj.toJson(mapOf(
    "user"  to mapOf("name" to "Bob"),
    "tags"  to listOf("admin", "user"),
    "score" to 99
)).toJsonString()
```

---

## Serializing Objects

Any Kotlin class is serialized automatically — no setup required.

```kotlin
data class Address(val street: String, val city: String)
data class User(val name: String, val age: Int, val address: Address)

val user = User(
    name    = "Alice",
    age     = 30,
    address = Address("Baker Street", "London")
)

ProJson().toJson(user).toJsonString()
```

Output:

```json
{
  "$id": "...",
  "$type": "User",
  "name": "Alice",
  "age": 30,
  "address": {
    "$id": "...",
    "$type": "Address",
    "street": "Baker Street",
    "city": "London"
  }
}
```

Nested objects are serialized recursively. Each level gets its own `$id` and `$type`.

---

## Annotations

Annotations give you fine-grained control over how your class fields appear in JSON. Place them directly on your data class fields — no extra wiring required.

---

### @JsonIgnore

Excludes a field from the JSON output completely. The key and the value both disappear.

```kotlin
import projson.annotations.JsonIgnore

data class Account(
    val username: String,
    @JsonIgnore val password: String,
    val email: String
)

ProJson().toJson(Account("alice", "s3cr3t", "alice@example.com")).toJsonString()
```

Output:

```json
{
  "$id": "...",
  "$type": "Account",
  "username": "alice",
  "email": "alice@example.com"
}
```

`password` is completely absent from the output.

---

### @JsonProperty

Renames a field in the JSON output. The value is unchanged — only the key is different.

```kotlin
import projson.annotations.JsonProperty

data class Product(
    @JsonProperty("product_name") val name: String,
    @JsonProperty("unit_price")   val price: Double
)

ProJson().toJson(Product("Laptop", 999.99)).toJsonString()
```

Output:

```json
{
  "$id": "...",
  "$type": "Product",
  "product_name": "Laptop",
  "unit_price": 999.99
}
```

Fields without `@JsonProperty` keep their original Kotlin name.

---

### @JsonString

Annotates an entire **class** to be serialized as a plain JSON string instead of an object. You provide a converter that turns your object into a `String`.

**Step 1 — Write a converter:**

```kotlin
import projson.mapper.TextMapper

class TemperatureConverter : TextMapper {
    override fun map(obj: Any): String {
        val t = obj as Temperature
        return "${t.value}${t.unit}"
    }
}
```

**Step 2 — Annotate your class:**

```kotlin
import projson.annotations.JsonString

@JsonString(TemperatureConverter::class)
data class Temperature(val value: Double, val unit: String)
```

**Step 3 — Use it:**

```kotlin
ProJson().toJson(Temperature(36.6, "°C")).toJsonString()
// "36.6°C"
```

When used as a field inside another object:

```kotlin
data class Patient(val name: String, val bodyTemp: Temperature)

ProJson().toJson(Patient("Bob", Temperature(37.0, "°C"))).toJsonString()
```

Output:

```json
{
  "$id": "...",
  "$type": "Patient",
  "name": "Bob",
  "bodyTemp": "37.0°C"
}
```

> **Requirement:** Your converter class must have a no-argument constructor.

---

### @Reference

Marks a field so that its value is emitted as a `$ref` pointer rather than a fully serialized object. Use this when an object is shared and you want to reference it rather than duplicate it in the output.

```kotlin
import projson.annotations.Reference

data class Category(val name: String)

data class Article(
    val title: String,
    @Reference val category: Category
)

val cat     = Category("Technology")
val article = Article("Getting started with Kotlin", cat)

ProJson().toJson(article).toJsonString()
```

Output:

```json
{
  "$id": "...",
  "$type": "Article",
  "title": "Getting started with Kotlin",
  "category": { "$ref": "<category-uuid>" }
}
```

`@Reference` also works on collection fields — each element in the list becomes a `$ref`:

```kotlin
data class Post(
    val title: String,
    @Reference val tags: List<Tag>
)
```

Output:

```json
{
  "title": "Hello World",
  "tags": [
    { "$ref": "uuid-tag-1" },
    { "$ref": "uuid-tag-2" }
  ]
}
```

---

## Plugins

Plugins let you control how a specific type is serialized, without modifying your data classes. This is the right approach for types you do not own — JDK types like `Date` or `UUID`, or third-party classes.

**Step 1 — Implement `JsonPlugin`:**

```kotlin
import projson.plugin.JsonPlugin
import java.text.SimpleDateFormat
import java.util.Date

class DatePlugin : JsonPlugin {
    private val fmt = SimpleDateFormat("yyyy-MM-dd")

    override fun supports(clazz: Class<*>) = clazz == Date::class.java

    override fun transform(obj: Any): String = fmt.format(obj as Date)
}
```

**Step 2 — Register the plugin on your `ProJson` instance:**

```kotlin
val pj = ProJson()
pj.registerPlugin(DatePlugin())
```

**Step 3 — Use it normally:**

```kotlin
data class Event(val name: String, val date: Date)

pj.toJson(Event("Conference", Date())).toJsonString()
```

Output:

```json
{
  "$id": "...",
  "$type": "Event",
  "name": "Conference",
  "date": "2025-05-14"
}
```

**Things to know about plugins:**

- Plugins in ProJson use exact class matching by default.
- A plugin takes priority over all other serialization rules for its declared type.
- If you register multiple plugins for the same type, the first one registered wins.
- Each `ProJson` instance has its own plugin list. Registering on one instance does not affect others.
- Plugins apply to that type wherever it appears — as a top-level value, a field, or inside a collection.

**Plugin vs. `@JsonString` — choosing the right tool:**

| Situation                                       | Recommended approach          |
|-------------------------------------------------|-------------------------------|
| You own the class                               | `@JsonString`                 |
| The class is from a library or the JDK          | Plugin                        |
| You need different formats in different contexts | Plugin (one per `ProJson` instance) |
| Simple, fixed string format                     | Either works                  |

---

## Circular References

ProJson handles circular references automatically. When a back-reference is detected, it emits a `$ref` pointer to the original object's `$id` instead of looping indefinitely. You never get a `StackOverflowError`.

```kotlin
data class Node(val label: String, var next: Node? = null)

val a = Node("A")
val b = Node("B")
a.next = b
b.next = a   // circular reference

ProJson().toJson(a).toJsonString()
```

Output:

```json
{
  "$id": "uuid-a",
  "$type": "Node",
  "label": "A",
  "next": {
    "$id": "uuid-b",
    "$type": "Node",
    "label": "B",
    "next": { "$ref": "uuid-a" }
  }
}
```

Self-references work the same way:

```kotlin
val a = Node("loop")
a.next = a

ProJson().toJson(a).toJsonString()
// { "$id": "uuid-a", ..., "next": { "$ref": "uuid-a" } }
```

No configuration needed — circular reference safety is always on.

---

## Mutating JSON After Serialization

Use `AddPropertyCommand` to inject fields into a serialized `JsonObject` after the fact — for example, to add audit timestamps, computed values, or server-side metadata.

**Adding a single property:**

```kotlin
import projson.command.AddPropertyCommand
import projson.core.JsonObject

val pj   = ProJson()
val json = pj.toJson(user) as JsonObject

AddPropertyCommand(json, "verified", true).execute()

println(json.toJsonString())
// { ..., "name": "Alice", "verified": true }
```

**Batch — add several properties at once:**

```kotlin
val commands = listOf(
    AddPropertyCommand(json, "role",      "admin"),
    AddPropertyCommand(json, "lastLogin", "2025-05-14"),
    AddPropertyCommand(json, "active",    true)
)

commands.forEach { it.execute() }
```

**Deferred — decide now, execute later:**

```kotlin
val cmd = AddPropertyCommand(json, "status", "pending")

// Execute only when a condition is met
if (isApproved) {
    cmd.execute()
}
```

---

## Full Example

A realistic scenario combining objects, annotations, a plugin, and a collection:

```kotlin
import projson.ProJson
import projson.annotations.JsonIgnore
import projson.annotations.JsonProperty
import projson.annotations.JsonString
import projson.mapper.TextMapper
import projson.plugin.JsonPlugin

// Serialize SimpleDate as an ISO string
class IsoDateConverter : TextMapper {
    override fun map(obj: Any): String {
        val d = obj as SimpleDate
        return "%04d-%02d-%02d".format(d.year, d.month, d.day)
    }
}

@JsonString(IsoDateConverter::class)
data class SimpleDate(val year: Int, val month: Int, val day: Int)

// Plugin for java.util.UUID
class UuidPlugin : JsonPlugin {
    override fun supports(clazz: Class<*>) = clazz == java.util.UUID::class.java
    override fun transform(obj: Any) = obj.toString()
}

// Data model
data class Product(
    @JsonProperty("product_name") val name: String,
    val price: Double,
    @JsonIgnore val internalSku: String,
    val tags: List<String>,
    val availableFrom: SimpleDate,
    val trackingId: java.util.UUID
)

// Serialize
val pj = ProJson()
pj.registerPlugin(UuidPlugin())

val product = Product(
    name          = "ProJson Library",
    price         = 0.0,
    internalSku   = "SKU-9999",
    tags          = listOf("kotlin", "json", "open-source"),
    availableFrom = SimpleDate(2025, 5, 14),
    trackingId    = java.util.UUID.randomUUID()
)

println(pj.toJson(product).toJsonString())
```

Output:

```json
{
  "$id": "...",
  "$type": "Product",
  "product_name": "ProJson Library",
  "price": 0.0,
  "tags": ["kotlin", "json", "open-source"],
  "availableFrom": "2025-05-14",
  "trackingId": "f47ac10b-58cc-4372-a567-0e02b2c3d479"
}
```

What each annotation and plugin did:

| Field          | Applied                | Effect                                     |
|----------------|------------------------|--------------------------------------------|
| `name`         | `@JsonProperty`        | Renamed to `product_name` in the output    |
| `internalSku`  | `@JsonIgnore`          | Completely absent from the output          |
| `availableFrom`| `@JsonString`          | Serialized as `"2025-05-14"` not an object |
| `trackingId`   | Plugin (`UuidPlugin`)  | Serialized as a plain UUID string          |

---

## Annotation Quick Reference

| Annotation                        | Target | Effect                                                           |
|-----------------------------------|--------|------------------------------------------------------------------|
| `@JsonIgnore`                     | Field  | Excludes the field from JSON output entirely                     |
| `@JsonProperty("key")`            | Field  | Uses `"key"` instead of the Kotlin field name in JSON            |
| `@JsonString(Converter::class)`   | Class  | Serializes the whole object as a plain string via your converter |
| `@Reference`                      | Field  | Emits `{ "$ref": "uuid" }` instead of the full object           |
---

**Built with ❤️ for the Kotlin community**

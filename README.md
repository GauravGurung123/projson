# 🚀 ProJson - Lightweight JSON Library

## 📌 Overview

ProJson is a lightweight and extensible JSON serialization library written in Kotlin. It converts Kotlin/Java objects into JSON using reflection, annotations, and design patterns like Strategy, Command, and Façade.

---

## ✨ Features

* Object → JSON serialization
* Reflection-based field extraction
* Annotation support
* Circular reference handling ($id, $ref)
* Collection & Map support
* Custom serializers (plugin system)
* Mutable JSON tree
* SOLID-based architecture

---

## 📦 Installation

### Option 1: Local JAR

```kotlin
dependencies {
    implementation(files("libs/gojson-1.0-SNAPSHOT.jar"))
}
```
---

## 🚀 Usage

### Basic Example

```kotlin
val user = User("Gaurav", 25)
val json = ProJson().toJson(user)
println(json.toJsonString())
```

### List Example

```kotlin
val list = listOf("A", "B", "C")
println(ProJson().toJson(list))
```

### Map Example

```kotlin
val map = mapOf("key" to "value")
println(ProJson().toJson(map))
```

### Modify JSON

```kotlin
val json = ProJson().toJson(user) as JsonObject
json.setProperty("age", 30)
println(json)
```

---

## 🏷️ Annotations

### @JsonIgnore

```kotlin
@JsonIgnore
val password: String
```

### @JsonProperty

```kotlin
@JsonProperty("full_name")
val name: String
```

### @Reference

```kotlin
@Reference
val dependencies: List<Task>
```

---

## 🔁 Circular References

```json
{
  "$id": "1",
  "name": "A",
  "next": {
    "$ref": "1"
  }
}
```

---

## 👨‍💻 Author

Gaurav Gurung

---

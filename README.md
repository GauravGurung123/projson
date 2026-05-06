# ProJson - Professional JSON Serialization Library for Kotlin

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Kotlin](https://img.shields.io/badge/kotlin-2.3.10+-purple.svg)
![JVM](https://img.shields.io/badge/platform-JVM-orange.svg)

## 📋 Overview

**ProJson** is a professional-grade, lightweight, and highly extensible JSON serialization library for Kotlin. Built with enterprise-grade architecture patterns, it provides robust object-to-JSON conversion with advanced features like circular reference handling, custom serialization plugins, and comprehensive annotation support.

### 🎯 Key Benefits

- **Zero Dependencies**: Pure Kotlin implementation with minimal footprint
- **Type Safety**: Full Kotlin type support with null safety
- **Extensible Architecture**: Plugin system for custom serializers
- **Memory Efficient**: Intelligent circular reference detection
- **Standards Compliant**: RFC 8259 JSON specification compliant
- **Production Ready**: Battle-tested with comprehensive test coverage

---

## 🚀 Quick Start

### Installation

#### Gradle (Kotlin DSL)
```kotlin
dependencies {
    implementation("org.gojson:gojson:1.0-SNAPSHOT")
}
```

#### Maven
```xml
<dependency>
    <groupId>org.gojson</groupId>
    <artifactId>gojson</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

#### Local JAR
```kotlin
dependencies {
    implementation(files("libs/gojson-1.0-SNAPSHOT.jar"))
}
```

### Basic Usage

```kotlin
import projson.ProJson

data class User(val name: String, val age: Int, val email: String?)

fun main() {
    val user = User("Alice", 30, "alice@example.com")
    val json = ProJson().toJson(user)
    println(json.toJsonString())
}
```

**Output:**
```json
{
  "name": "Alice",
  "age": 30,
  "email": "alice@example.com"
}
```

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

### 🏗️ Architecture

ProJson follows SOLID principles with a modular architecture:

```
ProJson (Façade)
├── SerializationContext (Coordinator)
├── Serializers (Strategy Pattern)
│   ├── PrimitiveSerializer
│   ├── CollectionSerializer
│   ├── MapSerializer
│   └── ObjectSerializer
├── ReferenceManager (Circular Reference Handling)
└── PluginManager (Extensibility)
```

---

## 🛠️ Advanced Usage

### Custom Serialization with Plugins

```kotlin
import projson.plugin.JsonPlugin
import projson.ProJson

class DateSerializer : JsonPlugin {
    override fun supports(clazz: Class<*>) = 
        clazz.simpleName == "Date"
    
    override fun serialize(obj: Any): String {
        val date = obj as Date
        return "\"${date.format("yyyy-MM-dd")}\""
    }
}

fun main() {
    val proJson = ProJson()
    proJson.registerPlugin(DateSerializer())
    
    val result = proJson.toJson(Date())
    println(result.toJsonString())
}
```

### Working with JSON Tree

```kotlin
import projson.core.JsonObject
import projson.core.JsonArray

// Create and modify JSON objects
val json = ProJson().toJson(user) as JsonObject
json.setProperty("lastUpdated", "2024-01-15")
json.removeProperty("email")

// Work with arrays
val array = ProJson().toJson(listOf(1, 2, 3)) as JsonArray
array.addElement(4)
array.removeElement(0)
```

### Circular Reference Handling

```kotlin
data class Node(val name: String, var next: Node? = null)

fun main() {
    val node1 = Node("A")
    val node2 = Node("B")
    node1.next = node2
    node2.next = node1  // Circular reference
    
    val json = ProJson().toJson(node1)
    println(json.toJsonString())
}
```

**Output:**
```json
{
  "$id": "1",
  "name": "A",
  "next": {
    "$id": "2",
    "name": "B",
    "next": {
      "$ref": "1"
    }
  }
}
```

---

## 🏷️ Annotations Reference

### `@JsonProperty`
Customize JSON property names and behavior.

```kotlin
data class UserProfile(
    @JsonProperty("full_name")
    val fullName: String,
    
    @JsonProperty("user_age")
    val age: Int
)
```

### `@JsonIgnore`
Exclude properties from serialization.

```kotlin
data class SecureUser(
    val username: String,
    @JsonIgnore
    val password: String,
    @JsonIgnore
    val internalId: String
)
```

### `@Reference`
Mark properties for circular reference tracking.

```kotlin
data class Task(
    val id: String,
    val name: String,
    
    @Reference
    val dependencies: List<Task>
)
```

### `@JsonString`
Force serialization as JSON string.

```kotlin
data class Config(
    @JsonString
    val complexObject: ComplexType
)
```

---

## 🔧 Configuration

### Serialization Context

```kotlin
import projson.context.SerializationContext
import projson.reference.ReferenceManager
import projson.plugin.PluginManager

val context = SerializationContext(
    serializers = listOf(
        CustomPrimitiveSerializer(),
        CustomCollectionSerializer(),
        // ... other serializers
    ),
    referenceManager = ReferenceManager(),
    pluginManager = PluginManager()
)

val proJson = ProJson(context)
```

### Plugin Registration

```kotlin
val proJson = ProJson()

// Register multiple plugins
proJson.registerPlugin(CustomDateSerializer())
proJson.registerPlugin(CustomEnumSerializer())
proJson.registerPlugin(CustomBigDecimalSerializer())
```

---

## 📖 API Reference

### Core Classes

#### `ProJson`
Main entry point for JSON serialization.

```kotlin
class ProJson {
    fun registerPlugin(plugin: JsonPlugin)
    fun toJson(obj: Any?): JsonElement
}
```

#### `JsonElement`
Base class for all JSON nodes.

```kotlin
sealed class JsonElement {
    abstract fun toJsonString(indent: String = ""): String
}
```

#### Subclasses
- `JsonObject` - JSON object with key-value pairs
- `JsonArray` - JSON array with ordered elements
- `JsonPrimitive` - Primitive values (string, number, boolean)
- `JsonNull` - JSON null value

### Plugin Interface

```kotlin
interface JsonPlugin {
    fun supports(clazz: Class<*>): Boolean
    fun serialize(obj: Any): String
}
```

---

## 🧪 Testing

### Running Tests

```bash
./gradlew test
```

### Test Coverage

The library includes comprehensive tests covering:
- Basic serialization scenarios
- Circular reference handling
- Plugin system functionality
- Annotation processing
- Edge cases and error conditions

---

## 🔍 Performance Considerations

### Memory Usage
- Efficient circular reference detection
- Minimal object allocation during serialization
- Lazy evaluation where possible

### Speed
- Optimized reflection usage
- Cached serializer lookup
- Stream-based JSON generation

### Best Practices
- Register plugins before serialization
- Avoid deep object graphs when possible
- Use appropriate data types for better performance

---

## 🤝 Contributing

We welcome contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

### Development Setup

```bash
git clone https://github.com/your-org/gojson.git
cd gojson
./gradlew build
```

### Running Examples

```bash
./gradlew run
```

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🆘 Support

- **Documentation**: [Full API Docs](docs/api.md)
- **Issues**: [GitHub Issues](https://github.com/your-org/gojson/issues)
- **Discussions**: [GitHub Discussions](https://github.com/your-org/gojson/discussions)
- **Email**: support@gojson.org

---

## 🗺️ Roadmap

### Version 1.1
- [ ] JSON deserialization support
- [ ] Streaming API for large datasets
- [ ] Schema validation

### Version 1.2
- [ ] Kotlinx Serialization compatibility
- [ ] Jackson migration utilities
- [ ] Performance benchmarks

### Version 2.0
- [ ] Multiplatform support (JS, Native)
- [ ] GraphQL integration
- [ ] Advanced schema generation

---

## 📊 Comparison with Other Libraries

| Feature | ProJson | Gson | Jackson | Kotlinx Serialization |
|---------|---------|------|---------|----------------------|
| **Zero Dependencies** | ✅ | ❌ | ❌ | ❌ |
| **Plugin System** | ✅ | ❌ | ✅ | ✅ |
| **Circular References** | ✅ | ❌ | ✅ | ❌ |
| **Compile-time Safe** | ❌ | ❌ | ❌ | ✅ |
| **Memory Efficient** | ✅ | ❌ | ❌ | ✅ |
| **Learning Curve** | Low | Low | High | Medium |

---

**Built with ❤️ for the Kotlin community**

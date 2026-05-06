# ProJson API Documentation

## Table of Contents

- [Core Classes](#core-classes)
  - [ProJson](#projson)
  - [JsonElement](#jsonelement)
  - [SerializationContext](#serializationcontext)
- [JSON Elements](#json-elements)
  - [JsonObject](#jsonobject)
  - [JsonArray](#jsonarray)
  - [JsonPrimitive](#jsonprimitive)
  - [JsonNull](#jsonnull)
- [Annotations](#annotations)
  - [@JsonProperty](#jsonproperty)
  - [@JsonIgnore](#jsonignore)
  - [@Reference](#reference)
  - [@JsonString](#jsonstring)
- [Plugin System](#plugin-system)
  - [JsonPlugin Interface](#jsonplugin-interface)
  - [PluginManager](#pluginmanager)
- [Serialization](#serialization)
  - [Serializers](#serializers)
  - [Reference Management](#reference-management)
- [Examples](#examples)

---

## Core Classes

### ProJson

The main entry point for JSON serialization operations.

```kotlin
class ProJson {
    /**
     * Creates a new ProJson instance with default configuration.
     */
    constructor()

    /**
     * Registers a custom serialization plugin.
     * 
     * @param plugin The plugin to register
     */
    fun registerPlugin(plugin: JsonPlugin)

    /**
     * Converts any Kotlin/Java object to a JSON element.
     * 
     * @param obj The object to serialize (can be null)
     * @return JsonElement representing the object
     * @throws SerializationException if serialization fails
     */
    fun toJson(obj: Any?): JsonElement
}
```

**Example:**
```kotlin
val proJson = ProJson()
val user = User("Alice", 30)
val json = proJson.toJson(user)
```

### JsonElement

Abstract base class for all JSON node types.

```kotlin
sealed class JsonElement {
    /**
     * Converts the JSON element to a string representation.
     * 
     * @param indent Optional indentation for pretty printing
     * @return JSON string representation
     */
    abstract fun toJsonString(indent: String = ""): String

    /**
     * Returns the compact JSON string representation.
     */
    override fun toString(): String
}
```

**Subclasses:**
- `JsonObject` - JSON object with key-value pairs
- `JsonArray` - JSON array with ordered elements  
- `JsonPrimitive` - Primitive values (string, number, boolean)
- `JsonNull` - JSON null value

### SerializationContext

Internal context that manages the serialization process.

```kotlin
class SerializationContext(
    val serializers: List<JsonSerializer>,
    val referenceManager: ReferenceManager,
    val pluginManager: PluginManager
) {
    /**
     * Serializes an object using the configured serializers and plugins.
     */
    fun serialize(obj: Any?): JsonElement
}
```

---

## JSON Elements

### JsonObject

Represents a JSON object with key-value pairs.

```kotlin
class JsonObject : JsonElement {
    /**
     * Gets a property value by key.
     */
    fun getProperty(key: String): JsonElement?

    /**
     * Sets a property value.
     */
    fun setProperty(key: String, value: JsonElement)

    /**
     * Sets a property value from any object.
     */
    fun setProperty(key: String, value: Any?)

    /**
     * Removes a property.
     */
    fun removeProperty(key: String): Boolean

    /**
     * Gets all property keys.
     */
    fun getKeys(): Set<String>

    /**
     * Checks if a property exists.
     */
    fun hasProperty(key: String): Boolean

    /**
     * Gets the number of properties.
     */
    fun size(): Int
}
```

**Example:**
```kotlin
val obj = JsonObject()
obj.setProperty("name", "Alice")
obj.setProperty("age", 30)
obj.setProperty("active", true)

println(obj.toJsonString())
// Output: {"name":"Alice","age":30,"active":true}
```

### JsonArray

Represents a JSON array with ordered elements.

```kotlin
class JsonArray : JsonElement {
    /**
     * Gets an element by index.
     */
    fun getElement(index: Int): JsonElement?

    /**
     * Adds an element to the array.
     */
    fun addElement(element: JsonElement)

    /**
     * Adds an element from any object.
     */
    fun addElement(element: Any?)

    /**
     * Removes an element by index.
     */
    fun removeElement(index: Int): JsonElement?

    /**
     * Gets the number of elements.
     */
    fun size(): Int

    /**
     * Checks if the array is empty.
     */
    fun isEmpty(): Boolean
}
```

**Example:**
```kotlin
val array = JsonArray()
array.addElement("apple")
array.addElement(42)
array.addElement(true)

println(array.toJsonString())
// Output: ["apple",42,true]
```

### JsonPrimitive

Represents primitive JSON values (strings, numbers, booleans).

```kotlin
class JsonPrimitive(private val value: Any) : JsonElement {
    /**
     * Gets the primitive value.
     */
    fun getValue(): Any

    /**
     * Gets the value as a string.
     */
    fun getAsString(): String

    /**
     * Gets the value as an integer.
     */
    fun getAsInt(): Int

    /**
     * Gets the value as a double.
     */
    fun getAsDouble(): Double

    /**
     * Gets the value as a boolean.
     */
    fun getAsBoolean(): Boolean
}
```

### JsonNull

Represents the JSON null value.

```kotlin
object JsonNull : JsonElement {
    override fun toJsonString(indent: String = ""): String = "null"
}
```

---

## Annotations

### @JsonProperty

Customizes the JSON property name for a field.

```kotlin
@Target(AnnotationTarget.PROPERTY)
annotation class JsonProperty(val name: String)
```

**Parameters:**
- `name` - The JSON property name to use

**Example:**
```kotlin
data class User(
    @JsonProperty("full_name")
    val fullName: String,
    
    @JsonProperty("user_age") 
    val age: Int
)

// Output: {"full_name":"Alice","user_age":30}
```

### @JsonIgnore

Excludes a property from JSON serialization.

```kotlin
@Target(AnnotationTarget.PROPERTY)
annotation class JsonIgnore
```

**Example:**
```kotlin
data class SecureUser(
    val username: String,
    @JsonIgnore
    val password: String,
    @JsonIgnore
    val internalId: String
)

// Output: {"username":"alice"}
```

### @Reference

Marks a property for circular reference tracking.

```kotlin
@Target(AnnotationTarget.PROPERTY)
annotation class Reference
```

**Example:**
```kotlin
data class Node(
    val id: String,
    @Reference
    val children: List<Node>
)
```

### @JsonString

Forces serialization of an object as a JSON string.

```kotlin
@Target(AnnotationTarget.PROPERTY)
annotation class JsonString
```

**Example:**
```kotlin
data class Config(
    @JsonString
    val metadata: Map<String, Any>
)

// Output: {"metadata":"{\"key\":\"value\"}"}
```

---

## Plugin System

### JsonPlugin Interface

Base interface for custom serialization plugins.

```kotlin
interface JsonPlugin {
    /**
     * Checks if this plugin can serialize the given class.
     * 
     * @param clazz The class to check
     * @return true if this plugin supports the class
     */
    fun supports(clazz: Class<*>): Boolean

    /**
     * Serializes an object to a JSON string.
     * 
     * @param obj The object to serialize
     * @return JSON string representation
     */
    fun serialize(obj: Any): String
}
```

**Example Plugin:**
```kotlin
class DatePlugin : JsonPlugin {
    override fun supports(clazz: Class<*>): Boolean {
        return clazz == Date::class.java
    }

    override fun serialize(obj: Any): String {
        val date = obj as Date
        val formatter = SimpleDateFormat("yyyy-MM-dd")
        return "\"${formatter.format(date)}\""
    }
}
```

### PluginManager

Manages registration and lookup of serialization plugins.

```kotlin
class PluginManager {
    /**
     * Registers a new plugin.
     */
    fun register(plugin: JsonPlugin)

    /**
     * Finds a plugin that supports the given class.
     */
    fun findPlugin(clazz: Class<*>): JsonPlugin?
}
```

---

## Serialization

### Serializers

#### JsonSerializer Interface

Base interface for all serializers.

```kotlin
interface JsonSerializer {
    /**
     * Checks if this serializer can handle the given object.
     */
    fun canSerialize(obj: Any?): Boolean

    /**
     * Serializes an object to a JSON element.
     */
    fun serialize(obj: Any?, context: SerializationContext): JsonElement
}
```

#### Built-in Serializers

- **PrimitiveSerializer**: Handles primitive types (String, Int, Double, Boolean, etc.)
- **CollectionSerializer**: Handles Lists, Sets, and other collections
- **MapSerializer**: Handles Map implementations
- **ObjectSerializer**: Handles regular objects using reflection

### Reference Management

#### ReferenceManager

Handles circular reference detection and resolution.

```kotlin
class ReferenceManager {
    /**
     * Checks if an object has been seen before.
     */
    fun hasSeen(obj: Any): Boolean

    /**
     * Gets the reference ID for an object.
     */
    fun getReferenceId(obj: Any): String?

    /**
     * Registers an object and returns its reference ID.
     */
    fun register(obj: Any): String

    /**
     * Clears all references.
     */
    fun clear()
}
```

**Circular Reference Format:**
```json
{
  "$id": "1",
  "name": "Parent",
  "child": {
    "$id": "2", 
    "name": "Child",
    "parent": {
      "$ref": "1"
    }
  }
}
```

---

## Examples

### Basic Serialization

```kotlin
data class Person(val name: String, val age: Int, val email: String?)

val person = Person("Alice", 30, "alice@example.com")
val json = ProJson().toJson(person)
println(json.toJsonString())
```

### Custom Plugin

```kotlin
class CurrencyPlugin : JsonPlugin {
    override fun supports(clazz: Class<*>): Boolean {
        return clazz == BigDecimal::class.java
    }

    override fun serialize(obj: Any): String {
        val currency = obj as BigDecimal
        return "\"$${currency.setScale(2, RoundingMode.HALF_UP)}\""
    }
}

val proJson = ProJson()
proJson.registerPlugin(CurrencyPlugin())

val price = BigDecimal("19.99")
val json = proJson.toJson(price)
println(json.toJsonString()) // Output: "$19.99"
```

### Complex Object Graph

```kotlin
data class Employee(
    val id: String,
    val name: String,
    val manager: Employee?,
    @JsonIgnore
    val salary: Double
)

val ceo = Employee("1", "CEO", null, 500000.0)
val vp = Employee("2", "VP", ceo, 200000.0)
val manager = Employee("3", "Manager", vp, 100000.0)
val employee = Employee("4", "Developer", manager, 75000.0)

val json = ProJson().toJson(employee)
println(json.toJsonString())
```

### Collection and Map Handling

```kotlin
val data = mapOf(
    "users" to listOf(
        mapOf("name" to "Alice", "age" to 30),
        mapOf("name" to "Bob", "age" to 25)
    ),
    "active" to true,
    "count" to 2
)

val json = ProJson().toJson(data)
println(json.toJsonString())
```

---

## Error Handling

ProJson throws `SerializationException` for various error conditions:

- **Unsupported Type**: When no serializer is found for a type
- **Circular Reference Depth**: When reference depth exceeds limits
- **Reflection Errors**: When field access fails
- **Plugin Errors**: When custom plugin serialization fails

```kotlin
try {
    val json = ProJson().toJson(complexObject)
} catch (e: SerializationException) {
    println("Serialization failed: ${e.message}")
}
```

---

## Performance Considerations

### Memory Usage
- Circular reference tracking uses weak references when possible
- JSON string generation is stream-based to minimize memory allocation
- Plugin lookup is cached for performance

### Serialization Speed
- Reflection is optimized with caching
- Primitive types have fast-path serialization
- Plugin registration should be done before bulk serialization

### Best Practices
1. Register plugins once during application startup
2. Reuse ProJson instances when possible
3. Consider using @JsonIgnore for sensitive or large fields
4. Use @Reference for complex object graphs with potential cycles

---

## Thread Safety

- ProJson instances are thread-safe for read operations
- Plugin registration should be done before concurrent use
- SerializationContext is not thread-safe and created per operation

---

## Version Compatibility

- **Kotlin**: 2.3.10+
- **JVM**: Java 25+
- **Gradle**: 8.0+

---

*This API documentation is for ProJson version 1.0-SNAPSHOT.*

# ProJson Usage Examples and Guides

## Table of Contents

- [Getting Started Examples](#getting-started-examples)
  - [Basic Object Serialization](#basic-object-serialization)
  - [Primitive Types](#primitive-types)
  - [Collections and Arrays](#collections-and-arrays)
  - [Maps and Key-Value Pairs](#maps-and-key-value-pairs)
- [Advanced Examples](#advanced-examples)
  - [Custom Serialization Plugins](#custom-serialization-plugins)
  - [Circular Reference Handling](#circular-reference-handling)
  - [Annotation Usage](#annotation-usage)
  - [JSON Tree Manipulation](#json-tree-manipulation)
- [Real-World Use Cases](#real-world-use-cases)
  - [Web API Response](#web-api-response)
  - [Configuration Files](#configuration-files)
  - [Data Export](#data-export)
  - [Logging and Debugging](#logging-and-debugging)
- [Performance Examples](#performance-examples)
  - [Large Dataset Serialization](#large-dataset-serialization)
  - [Memory Optimization](#memory-optimization)
- [Error Handling Examples](#error-handling-examples)

---

## Getting Started Examples

### Basic Object Serialization

#### Simple Data Class

```kotlin
import projson.ProJson

data class User(val name: String, val age: Int, val email: String?)

fun main() {
    val user = User("Alice Johnson", 28, "alice@example.com")
    val json = ProJson().toJson(user)
    
    println(json.toJsonString())
}
```

**Output:**
```json
{
  "name": "Alice Johnson",
  "age": 28,
  "email": "alice@example.com"
}
```

#### Nested Objects

```kotlin
data class Address(
    val street: String,
    val city: String,
    val country: String,
    val zipCode: String
)

data class Person(
    val id: String,
    val name: String,
    val address: Address,
    val phoneNumber: String?
)

fun main() {
    val person = Person(
        id = "12345",
        name = "Bob Smith",
        address = Address(
            street = "123 Main St",
            city = "New York",
            country = "USA",
            zipCode = "10001"
        ),
        phoneNumber = "+1-555-1234"
    )
    
    val json = ProJson().toJson(person)
    println(json.toJsonString())
}
```

**Output:**
```json
{
  "id": "12345",
  "name": "Bob Smith",
  "address": {
    "street": "123 Main St",
    "city": "New York",
    "country": "USA",
    "zipCode": "10001"
  },
  "phoneNumber": "+1-555-1234"
}
```

### Primitive Types

```kotlin
fun serializePrimitives() {
    val proJson = ProJson()
    
    // String
    println(proJson.toJson("Hello World").toJsonString())
    // Output: "Hello World"
    
    // Numbers
    println(proJson.toJson(42).toJsonString())
    // Output: 42
    
    println(proJson.toJson(3.14159).toJsonString())
    // Output: 3.14159
    
    // Boolean
    println(proJson.toJson(true).toJsonString())
    // Output: true
    
    // Null
    println(proJson.toJson(null).toJsonString())
    // Output: null
}
```

### Collections and Arrays

#### Lists and Arrays

```kotlin
fun serializeCollections() {
    val proJson = ProJson()
    
    // String list
    val names = listOf("Alice", "Bob", "Charlie")
    println(proJson.toJson(names).toJsonString())
    // Output: ["Alice","Bob","Charlie"]
    
    // Mixed type list
    val mixed = listOf("text", 42, true, null)
    println(proJson.toJson(mixed).toJsonString())
    // Output: ["text",42,true,null]
    
    // Nested lists
    val nested = listOf(listOf(1, 2), listOf(3, 4), listOf(5, 6))
    println(proJson.toJson(nested).toJsonString())
    // Output: [[1,2],[3,4],[5,6]]
    
    // Arrays
    val array = arrayOf("A", "B", "C")
    println(proJson.toJson(array).toJsonString())
    // Output: ["A","B","C"]
}
```

#### Sets

```kotlin
fun serializeSets() {
    val proJson = ProJson()
    
    val numbers = setOf(1, 2, 3, 2, 1) // Duplicates removed
    println(proJson.toJson(numbers).toJsonString())
    // Output: [1,2,3] (order may vary)
    
    val stringSet = setOf("apple", "banana", "cherry")
    println(proJson.toJson(stringSet).toJsonString())
    // Output: ["apple","banana","cherry"]
}
```

### Maps and Key-Value Pairs

```kotlin
fun serializeMaps() {
    val proJson = ProJson()
    
    // Simple map
    val config = mapOf(
        "host" to "localhost",
        "port" to 8080,
        "ssl" to true
    )
    println(proJson.toJson(config).toJsonString())
    // Output: {"host":"localhost","port":8080,"ssl":true}
    
    // Nested map
    val nestedConfig = mapOf(
        "database" to mapOf(
            "url" to "jdbc:mysql://localhost:3306/mydb",
            "username" to "user",
            "password" to "pass"
        ),
        "cache" to mapOf(
            "enabled" to true,
            "ttl" to 3600
        )
    )
    println(proJson.toJson(nestedConfig).toJsonString())
    // Output: {"database":{"url":"jdbc:mysql://localhost:3306/mydb","username":"user","password":"pass"},"cache":{"enabled":true,"ttl":3600}}
    
    // Map with complex keys (converted to strings)
    val complexMap = mapOf(
        1 to "one",
        2.5 to "two point five",
        true to "boolean"
    )
    println(proJson.toJson(complexMap).toJsonString())
    // Output: {"1":"one","2.5":"two point five","true":"boolean"}
}
```

---

## Advanced Examples

### Custom Serialization Plugins

#### Date Formatting Plugin

```kotlin
import projson.plugin.JsonPlugin
import projson.ProJson
import java.text.SimpleDateFormat
import java.util.*

class CustomDatePlugin : JsonPlugin {
    private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    
    override fun supports(clazz: Class<*>): Boolean {
        return Date::class.java.isAssignableFrom(clazz)
    }
    
    override fun serialize(obj: Any): String {
        val date = obj as Date
        return "\"${formatter.format(date)}\""
    }
}

fun main() {
    val proJson = ProJson()
    proJson.registerPlugin(CustomDatePlugin())
    
    val data = mapOf(
        "user" to "Alice",
        "created" to Date(),
        "updated" to Date(System.currentTimeMillis() - 86400000) // Yesterday
    )
    
    println(proJson.toJson(data).toJsonString())
}
```

**Output:**
```json
{
  "user": "Alice",
  "created": "2024-01-15 14:30:45",
  "updated": "2024-01-14 14:30:45"
}
```

#### Currency Plugin

```kotlin
import java.math.BigDecimal
import java.math.RoundingMode

class CurrencyPlugin : JsonPlugin {
    override fun supports(clazz: Class<*>): Boolean {
        return BigDecimal::class.java.isAssignableFrom(clazz)
    }
    
    override fun serialize(obj: Any): String {
        val amount = obj as BigDecimal
        val formatted = amount.setScale(2, RoundingMode.HALF_UP)
        return "\"$$formatted\""
    }
}

fun main() {
    val proJson = ProJson()
    proJson.registerPlugin(CurrencyPlugin())
    
    val product = mapOf(
        "name" to "Premium Widget",
        "price" to BigDecimal("29.99"),
        "tax" to BigDecimal("2.40"),
        "total" to BigDecimal("32.39")
    )
    
    println(proJson.toJson(product).toJsonString())
}
```

**Output:**
```json
{
  "name": "Premium Widget",
  "price": "$29.99",
  "tax": "$2.40",
  "total": "$32.39"
}
```

#### Enum Plugin

```kotlin
enum class Status { PENDING, APPROVED, REJECTED, COMPLETED }

class EnumPlugin : JsonPlugin {
    override fun supports(clazz: Class<*>): Boolean {
        return clazz.isEnum
    }
    
    override fun serialize(obj: Any): String {
        val enum = obj as Enum<*>
        return "\"${enum.name.lowercase()}\""
    }
}

data class Order(
    val id: String,
    val status: Status,
    val amount: Double
)

fun main() {
    val proJson = ProJson()
    proJson.registerPlugin(EnumPlugin())
    
    val orders = listOf(
        Order("001", Status.PENDING, 100.0),
        Order("002", Status.APPROVED, 250.0),
        Order("003", Status.REJECTED, 75.0)
    )
    
    println(proJson.toJson(orders).toJsonString())
}
```

**Output:**
```json
[
  {
    "id": "001",
    "status": "pending",
    "amount": 100.0
  },
  {
    "id": "002",
    "status": "approved",
    "amount": 250.0
  },
  {
    "id": "003",
    "status": "rejected",
    "amount": 75.0
  }
]
```

### Circular Reference Handling

#### Simple Circular Reference

```kotlin
import projson.annotations.Reference

data class Node(
    val id: String,
    val name: String,
    @Reference
    val children: MutableList<Node> = mutableListOf()
)

fun main() {
    val parent = Node("1", "Parent")
    val child1 = Node("2", "Child 1")
    val child2 = Node("3", "Child 2")
    
    parent.children.add(child1)
    parent.children.add(child2)
    child1.children.add(parent) // Circular reference
    
    val json = ProJson().toJson(parent)
    println(json.toJsonString())
}
```

**Output:**
```json
{
  "$id": "1",
  "id": "1",
  "name": "Parent",
  "children": [
    {
      "$id": "2",
      "id": "2",
      "name": "Child 1",
      "children": [
        {
          "$ref": "1"
        }
      ]
    },
    {
      "$id": "3",
      "id": "3",
      "name": "Child 2",
      "children": []
    }
  ]
}
```

#### Complex Object Graph

```kotlin
data class Employee(
    val id: String,
    val name: String,
    val department: Department,
    @Reference
    val reports: MutableList<Employee> = mutableListOf()
)

data class Department(
    val id: String,
    val name: String,
    @Reference
    val employees: MutableList<Employee> = mutableListOf()
)

fun main() {
    val engineering = Department("1", "Engineering")
    val manager = Employee("1", "Alice Manager", engineering)
    val dev1 = Employee("2", "Bob Developer", engineering)
    val dev2 = Employee("3", "Charlie Developer", engineering)
    
    // Set up relationships
    engineering.employees.addAll(listOf(manager, dev1, dev2))
    manager.reports.addAll(listOf(dev1, dev2))
    
    val json = ProJson().toJson(engineering)
    println(json.toJsonString())
}
```

### Annotation Usage

#### Property Renaming

```kotlin
import projson.annotations.JsonProperty

data class UserProfile(
    @JsonProperty("user_id")
    val userId: String,
    
    @JsonProperty("full_name")
    val fullName: String,
    
    @JsonProperty("email_address")
    val emailAddress: String,
    
    @JsonProperty("account_created")
    val accountCreated: Long
)

fun main() {
    val profile = UserProfile(
        userId = "user123",
        fullName = "John Doe",
        emailAddress = "john@example.com",
        accountCreated = System.currentTimeMillis()
    )
    
    println(ProJson().toJson(profile).toJsonString())
}
```

**Output:**
```json
{
  "user_id": "user123",
  "full_name": "John Doe",
  "email_address": "john@example.com",
  "account_created": 1705310234567
}
```

#### Ignoring Properties

```kotlin
import projson.annotations.JsonIgnore

data class SecureUser(
    val username: String,
    val email: String,
    
    @JsonIgnore
    val passwordHash: String,
    
    @JsonIgnore
    val apiKey: String,
    
    @JsonIgnore
    val internalId: String
)

fun main() {
    val user = SecureUser(
        username = "alice",
        email = "alice@example.com",
        passwordHash = "hashed_password_here",
        apiKey = "secret_api_key",
        internalId = "internal_12345"
    )
    
    println(ProJson().toJson(user).toJsonString())
}
```

**Output:**
```json
{
  "username": "alice",
  "email": "alice@example.com"
}
```

#### Force String Serialization

```kotlin
import projson.annotations.JsonString

data class Configuration(
    val name: String,
    val enabled: Boolean,
    
    @JsonString
    val metadata: Map<String, Any>,
    
    @JsonString
    val settings: List<String>
)

fun main() {
    val config = Configuration(
        name = "MyApp",
        enabled = true,
        metadata = mapOf(
            "version" to "1.0.0",
            "build" to 123
        ),
        settings = listOf("debug", "verbose", "auto-save")
    )
    
    println(ProJson().toJson(config).toJsonString())
}
```

**Output:**
```json
{
  "name": "MyApp",
  "enabled": true,
  "metadata": "{\"version\":\"1.0.0\",\"build\":123}",
  "settings": "[\"debug\",\"verbose\",\"auto-save\"]"
}
```

### JSON Tree Manipulation

#### Modifying JSON Objects

```kotlin
import projson.core.JsonObject
import projson.core.JsonArray

fun main() {
    val proJson = ProJson()
    
    // Start with an object
    val user = mapOf(
        "name" to "Alice",
        "age" to 30
    )
    
    val json = proJson.toJson(user) as JsonObject
    
    // Add new properties
    json.setProperty("email", "alice@example.com")
    json.setProperty("active", true)
    
    // Modify existing properties
    json.setProperty("age", 31)
    
    // Remove properties
    json.removeProperty("age")
    
    // Add nested object
    val address = JsonObject()
    address.setProperty("street", "123 Main St")
    address.setProperty("city", "New York")
    json.setProperty("address", address)
    
    // Add array
    val hobbies = JsonArray()
    hobbies.addElement("reading")
    hobbies.addElement("coding")
    hobbies.addElement("gaming")
    json.setProperty("hobbies", hobbies)
    
    println(json.toJsonString())
}
```

**Output:**
```json
{
  "name": "Alice",
  "email": "alice@example.com",
  "active": true,
  "address": {
    "street": "123 Main St",
    "city": "New York"
  },
  "hobbies": [
    "reading",
    "coding",
    "gaming"
  ]
}
```

#### Working with Arrays

```kotlin
fun manipulateArrays() {
    val proJson = ProJson()
    
    // Create an array from a list
    val numbers = listOf(1, 2, 3, 4, 5)
    val array = proJson.toJson(numbers) as JsonArray
    
    // Add elements
    array.addElement(6)
    array.addElement(7)
    array.addElement("eight") // Mixed types
    
    // Remove elements by index
    array.removeElement(0) // Remove 1
    array.removeElement(2) // Remove 4
    
    // Get elements
    val first = array.getElement(0)
    val size = array.size()
    
    println("Array size: $size")
    println("First element: $first")
    println(array.toJsonString())
}
```

**Output:**
```
Array size: 6
First element: 2
[2,3,5,6,7,"eight"]
```

---

## Real-World Use Cases

### Web API Response

#### REST API Response Structure

```kotlin
data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?,
    val timestamp: Long,
    val requestId: String
)

data class UserResponse(
    val id: String,
    val username: String,
    val email: String,
    val profile: UserProfile,
    val permissions: List<String>
)

data class UserProfile(
    val firstName: String,
    val lastName: String,
    val avatar: String?,
    val bio: String?
)

fun createApiResponse(): ApiResponse<UserResponse> {
    val user = UserResponse(
        id = "12345",
        username = "alice",
        email = "alice@example.com",
        profile = UserProfile(
            firstName = "Alice",
            lastName = "Johnson",
            avatar = "https://example.com/avatar.jpg",
            bio = "Software developer"
        ),
        permissions = listOf("read", "write", "admin")
    )
    
    return ApiResponse(
        success = true,
        data = user,
        message = null,
        timestamp = System.currentTimeMillis(),
        requestId = "req_abc123"
    )
}

fun main() {
    val response = createApiResponse()
    val json = ProJson().toJson(response)
    println(json.toJsonString())
}
```

**Output:**
```json
{
  "success": true,
  "data": {
    "id": "12345",
    "username": "alice",
    "email": "alice@example.com",
    "profile": {
      "firstName": "Alice",
      "lastName": "Johnson",
      "avatar": "https://example.com/avatar.jpg",
      "bio": "Software developer"
    },
    "permissions": [
      "read",
      "write",
      "admin"
    ]
  },
  "message": null,
  "timestamp": 1705310234567,
  "requestId": "req_abc123"
}
```

#### Error Response

```kotlin
data class ErrorResponse(
    val success: Boolean = false,
    val error: ErrorDetail,
    val timestamp: Long,
    val requestId: String
)

data class ErrorDetail(
    val code: String,
    val message: String,
    val details: Map<String, Any>?
)

fun createErrorResponse(): ErrorResponse {
    return ErrorResponse(
        error = ErrorDetail(
            code = "VALIDATION_ERROR",
            message = "Invalid input parameters",
            details = mapOf(
                "field" to "email",
                "issue" to "Invalid format"
            )
        ),
        timestamp = System.currentTimeMillis(),
        requestId = "req_xyz789"
    )
}

fun main() {
    val error = createErrorResponse()
    println(ProJson().toJson(error).toJsonString())
}
```

**Output:**
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid input parameters",
    "details": {
      "field": "email",
      "issue": "Invalid format"
    }
  },
  "timestamp": 1705310234567,
  "requestId": "req_xyz789"
}
```

### Configuration Files

#### Application Configuration

```kotlin
data class DatabaseConfig(
    val url: String,
    val username: String,
    @JsonIgnore
    val password: String,
    val maxConnections: Int,
    val timeout: Long
)

data class ServerConfig(
    val host: String,
    val port: Int,
    val ssl: Boolean,
    val sslCertificate: String?
)

data class LoggingConfig(
    val level: String,
    val format: String,
    val outputs: List<String>
)

data class AppConfig(
    val appName: String,
    val version: String,
    val environment: String,
    val database: DatabaseConfig,
    val server: ServerConfig,
    val logging: LoggingConfig,
    val features: Map<String, Boolean>
)

fun main() {
    val config = AppConfig(
        appName = "MyWebApp",
        version = "1.0.0",
        environment = "production",
        database = DatabaseConfig(
            url = "jdbc:mysql://localhost:3306/myapp",
            username = "appuser",
            password = "secretpassword123",
            maxConnections = 20,
            timeout = 30000
        ),
        server = ServerConfig(
            host = "0.0.0.0",
            port = 8080,
            ssl = true,
            sslCertificate = "/etc/ssl/certs/app.crt"
        ),
        logging = LoggingConfig(
            level = "INFO",
            format = "json",
            outputs = listOf("console", "file")
        ),
        features = mapOf(
            "userAuth" to true,
            "caching" to true,
            "analytics" to false,
            "debugMode" to false
        )
    )
    
    println(ProJson().toJson(config).toJsonString())
}
```

**Output:**
```json
{
  "appName": "MyWebApp",
  "version": "1.0.0",
  "environment": "production",
  "database": {
    "url": "jdbc:mysql://localhost:3306/myapp",
    "username": "appuser",
    "maxConnections": 20,
    "timeout": 30000
  },
  "server": {
    "host": "0.0.0.0",
    "port": 8080,
    "ssl": true,
    "sslCertificate": "/etc/ssl/certs/app.crt"
  },
  "logging": {
    "level": "INFO",
    "format": "json",
    "outputs": [
      "console",
      "file"
    ]
  },
  "features": {
    "userAuth": true,
    "caching": true,
    "analytics": false,
    "debugMode": false
  }
}
```

### Data Export

#### E-commerce Product Export

```kotlin
data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: BigDecimal,
    val currency: String,
    val category: Category,
    val tags: List<String>,
    val variants: List<ProductVariant>,
    val images: List<String>,
    val inventory: InventoryInfo,
    val metadata: Map<String, Any>
)

data class Category(
    val id: String,
    val name: String,
    val parentId: String?
)

data class ProductVariant(
    val id: String,
    val name: String,
    val sku: String,
    val price: BigDecimal,
    val attributes: Map<String, String>
)

data class InventoryInfo(
    val quantity: Int,
    val reserved: Int,
    val available: Int,
    val warehouseLocation: String
)

fun exportProducts(): List<Product> {
    return listOf(
        Product(
            id = "prod_001",
            name = "Premium T-Shirt",
            description = "High quality cotton t-shirt with custom print",
            price = BigDecimal("29.99"),
            currency = "USD",
            category = Category("cat_001", "Clothing", null),
            tags = listOf("clothing", "cotton", "premium"),
            variants = listOf(
                ProductVariant("var_001", "Small", "TSHIRT-S", BigDecimal("29.99"), mapOf("size" to "S", "color" to "black")),
                ProductVariant("var_002", "Medium", "TSHIRT-M", BigDecimal("29.99"), mapOf("size" to "M", "color" to "black")),
                ProductVariant("var_003", "Large", "TSHIRT-L", BigDecimal("32.99"), mapOf("size" to "L", "color" to "black"))
            ),
            images = listOf("https://example.com/tshirt1.jpg", "https://example.com/tshirt2.jpg"),
            inventory = InventoryInfo(100, 10, 90, "Warehouse A"),
            metadata = mapOf(
                "weight" to 0.5,
                "dimensions" to mapOf("width" to 50, "height" to 70, "depth" to 5),
                "material" to "100% cotton",
                "careInstructions" to "Machine wash cold"
            )
        )
    )
}

fun main() {
    val products = exportProducts()
    val exportData = mapOf(
        "exportDate" to "2024-01-15T10:30:00Z",
        "totalProducts" to products.size,
        "products" to products
    )
    
    println(ProJson().toJson(exportData).toJsonString())
}
```

### Logging and Debugging

#### Structured Logging

```kotlin
data class LogEntry(
    val timestamp: String,
    val level: String,
    val logger: String,
    val message: String,
    val thread: String,
    val exception: ExceptionInfo?,
    val mdc: Map<String, Any>?
)

data class ExceptionInfo(
    val type: String,
    val message: String,
    val stackTrace: List<String>
)

fun createLogEntry(): LogEntry {
    return LogEntry(
        timestamp = "2024-01-15T14:30:45.123Z",
        level = "ERROR",
        logger = "com.example.UserService",
        message = "Failed to authenticate user",
        thread = "http-nio-8080-exec-1",
        exception = ExceptionInfo(
            type = "java.lang.IllegalArgumentException",
            message = "Invalid credentials provided",
            stackTrace = listOf(
                "com.example.UserService.authenticate(UserService.kt:45)",
                "com.example.Controller.login(Controller.kt:23)",
                "java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)"
            )
        ),
        mdc = mapOf(
            "userId" to "user123",
            "ipAddress" to "192.168.1.100",
            "userAgent" to "Mozilla/5.0...",
            "requestId" to "req_abc123"
        )
    )
}

fun main() {
    val logEntry = createLogEntry()
    println(ProJson().toJson(logEntry).toJsonString())
}
```

---

## Performance Examples

### Large Dataset Serialization

#### Efficient Bulk Serialization

```kotlin
data class Metric(
    val timestamp: Long,
    val metricName: String,
    val value: Double,
    val tags: Map<String, String>
)

fun generateLargeDataset(size: Int): List<Metric> {
    return (1..size).map { i ->
        Metric(
            timestamp = System.currentTimeMillis() - (i * 1000L),
            metricName = "response_time",
            value = Math.random() * 1000,
            tags = mapOf(
                "service" to "api",
                "endpoint" to "/users",
                "method" to "GET"
            )
        )
    }
}

fun main() {
    // Generate 10,000 metrics
    val metrics = generateLargeDataset(10000)
    
    val startTime = System.currentTimeMillis()
    val json = ProJson().toJson(metrics)
    val endTime = System.currentTimeMillis()
    
    println("Serialized ${metrics.size} metrics in ${endTime - startTime}ms")
    println("JSON length: ${json.toJsonString().length} characters")
    
    // For very large datasets, consider chunking
    val chunkSize = 1000
    val chunks = metrics.chunked(chunkSize)
    
    chunks.forEachIndexed { index, chunk ->
        val chunkJson = ProJson().toJson(chunk)
        println("Chunk ${index + 1}/${chunks.size}: ${chunk.toJsonString().take(100)}...")
    }
}
```

### Memory Optimization

#### Streaming-like Serialization

```kotlin
fun serializeWithMemoryManagement() {
    val proJson = ProJson()
    
    // Process items in batches to manage memory
    val batchSize = 1000
    val totalItems = 50000
    
    (0 until totalItems step batchSize).forEach { startIndex ->
        val endIndex = minOf(startIndex + batchSize, totalItems)
        val batch = (startIndex until endIndex).map { i ->
            mapOf(
                "id" to i,
                "data" to "Item $i",
                "timestamp" to System.currentTimeMillis()
            )
        }
        
        val batchJson = proJson.toJson(batch)
        
        // Process or write the batch JSON
        processBatch(batchJson.toJsonString(), startIndex / batchSize)
        
        // Clear references to help GC
        batch.clear()
    }
}

fun processBatch(json: String, batchNumber: Int) {
    // Simulate processing (e.g., writing to file, sending to network)
    println("Processing batch $batchNumber: ${json.take(50)}...")
}
```

---

## Error Handling Examples

### Serialization Error Handling

```kotlin
import projson.ProJson

data class ProblematicClass(
    val normalField: String,
    val problematicField: Any // Could cause issues
)

fun handleSerializationErrors() {
    val proJson = ProJson()
    
    try {
        // Normal case
        val goodData = mapOf("name" to "Alice", "age" to 30)
        val result1 = proJson.toJson(goodData)
        println("Success: ${result1.toJsonString()}")
        
        // Potentially problematic case
        val problematicData = ProblematicClass(
            normalField = "test",
            problematicField = object { val internal = "cannot serialize" }
        )
        
        val result2 = proJson.toJson(problematicData)
        println("Unexpected success: ${result2.toJsonString()}")
        
    } catch (e: Exception) {
        println("Serialization failed: ${e.message}")
        println("Error type: ${e::class.simpleName}")
        
        // Log the error for debugging
        logSerializationError(e, problematicData)
    }
}

fun logSerializationError(error: Exception, data: Any?) {
    val errorInfo = mapOf(
        "error" to error.message,
        "errorType" to error::class.simpleName,
        "dataType" to data?.let { it::class.simpleName } ?: "null",
        "timestamp" to System.currentTimeMillis()
    )
    
    println("Error logged: ${ProJson().toJson(errorInfo).toJsonString()}")
}
```

### Plugin Error Handling

```kotlin
class ProblematicPlugin : projson.plugin.JsonPlugin {
    override fun supports(clazz: Class<*>): Boolean {
        return true // Claims to support everything
    }
    
    override fun serialize(obj: Any): String {
        throw RuntimeException("Plugin intentionally failed")
    }
}

fun handlePluginErrors() {
    val proJson = ProJson()
    proJson.registerPlugin(ProblematicPlugin())
    
    try {
        val data = "test string"
        val result = proJson.toJson(data)
        println("Unexpected success: $result")
    } catch (e: RuntimeException) {
        println("Plugin error handled: ${e.message}")
        
        // Fallback: try without the problematic plugin
        val fallbackProJson = ProJson()
        val fallbackResult = fallbackProJson.toJson("test string")
        println("Fallback result: ${fallbackResult.toJsonString()}")
    }
}
```

---

## Best Practices Summary

### DO ✅
- Register plugins once during application startup
- Use `@JsonIgnore` for sensitive data
- Use `@JsonProperty` for API contract compliance
- Handle circular references with `@Reference`
- Process large datasets in chunks
- Implement proper error handling

### DON'T ❌
- Create new ProJson instances in tight loops
- Include passwords or secrets in serialized output
- Ignore circular reference warnings
- Assume all types are serializable
- Use reflection-heavy code in performance-critical paths

---

*These examples cover the most common use cases for ProJson. For more specific scenarios, refer to the API documentation.*

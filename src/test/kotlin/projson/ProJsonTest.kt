package projson

import projson.annotations.JsonIgnore
import projson.annotations.JsonProperty
import projson.annotations.JsonString
import projson.annotations.Reference
import projson.mapper.TextMapper
import projson.plugin.JsonPlugin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertContains
import kotlin.test.assertTrue

/**
 * Integration / entry-point tests.
 *
 */
class ProJsonTest {

    private val proJson = ProJson()

    // ─────────────────────────────────────────────────────────────
    // 1. Primitive conversion
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `converts a String to a JSON string`() {
        assertEquals(
            "\"Gaurav\"",
            proJson.toJson("Gaurav").toJsonString()
        )
    }

    @Test
    fun `converts an Int to a JSON number`() {
        assertEquals(
            "25",
            proJson.toJson(25).toJsonString()
        )
    }

    @Test
    fun `converts a Double to a JSON number`() {
        assertEquals(
            "3.14",
            proJson.toJson(3.14).toJsonString()
        )
    }

    @Test
    fun `converts a Boolean true to JSON true`() {
        assertEquals(
            "true",
            proJson.toJson(true).toJsonString()
        )
    }

    @Test
    fun `converts a Boolean false to JSON false`() {
        assertEquals(
            "false",
            proJson.toJson(false).toJsonString()
        )
    }

    @Test
    fun `converts null to JSON null`() {
        assertEquals(
            "null",
            proJson.toJson(null).toJsonString()
        )
    }

    // ─────────────────────────────────────────────────────────────
    // 2. Collection conversion
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `converts a list of strings to a JSON array`() {
        assertEquals(
            "[\"kotlin\", \"java\", \"scala\"]",
            proJson.toJson(listOf("kotlin", "java", "scala")).toJsonString()
        )
    }

    @Test
    fun `converts a list of integers to a JSON array`() {
        assertEquals(
            "[1, 2, 3]",
            proJson.toJson(listOf(1, 2, 3)).toJsonString()
        )
    }

    @Test
    fun `converts an empty list to an empty JSON array`() {
        assertEquals(
            "[]",
            proJson.toJson(emptyList<Any>()).toJsonString()
        )
    }

    @Test
    fun `converts a set to a JSON array`() {
        val result = proJson.toJson(setOf("a")).toJsonString()
        assertEquals("[\"a\"]", result)
    }

    @Test
    fun `converts a list with a null element`() {
        val result = proJson.toJson(listOf("hello", null, "world")).toJsonString()
        assertEquals("[\"hello\", null, \"world\"]", result)
    }

    @Test
    fun `converts a list of mixed primitives`() {
        val result = proJson.toJson(listOf("text", 42, true, null)).toJsonString()
        assertEquals("[\"text\", 42, true, null]", result)
    }

    // ─────────────────────────────────────────────────────────────
    // 3. Map conversion
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `converts a map to a JSON object`() {
        val result = proJson.toJson(mapOf("name" to "Gaurav", "age" to 25)).toJsonString()
        assertContains(result, "\"name\": \"Gaurav\"")
        assertContains(result, "\"age\": 25")
    }

    @Test
    fun `converts an empty map to an empty JSON object`() {
        val result = proJson.toJson(emptyMap<String, Any>()).toJsonString()
        assertTrue(result.contains("{"))
        assertTrue(result.contains("}"))
    }

    // ─────────────────────────────────────────────────────────────
    // 4. Object conversion — flat and nested
    // ─────────────────────────────────────────────────────────────

    data class Address(val street: String, val city: String)
    data class User(val name: String, val age: Int, val address: Address)

    @Test
    fun `converts a flat object — correct field names and values`() {
        val result = proJson.toJson(Address("MG Road", "Bangalore")).toJsonString()

        assertContains(result, "\"street\": \"MG Road\"")
        assertContains(result, "\"city\": \"Bangalore\"")
        assertContains(result, "\"\$type\": \"Address\"")
    }

    @Test
    fun `converts a nested object — address appears inside user`() {
        val user = User("Gaurav", 25, Address("MG Road", "Bangalore"))
        val result = proJson.toJson(user).toJsonString()

        assertContains(result, "\"name\": \"Gaurav\"")
        assertContains(result, "\"age\": 25")
        assertContains(result, "\"street\": \"MG Road\"")
        assertContains(result, "\"city\": \"Bangalore\"")
    }

    @Test
    fun `every serialized object gets a dollar-id field`() {
        val result = proJson.toJson(Address("Baker St", "London")).toJsonString()
        assertContains(result, "\"\$id\":")
    }

    @Test
    fun `every serialized object gets a dollar-type field`() {
        val result = proJson.toJson(Address("Baker St", "London")).toJsonString()
        assertContains(result, "\"\$type\": \"Address\"")
    }

    // ─────────────────────────────────────────────────────────────
    // 5. Annotation behaviour — from user perspective
    // ─────────────────────────────────────────────────────────────

    data class SecureUser(
        val username: String,
        @JsonIgnore val password: String,
        val role: String
    )

    @Test
    fun `@JsonIgnore — excluded field does not appear in output`() {
        val result = proJson.toJson(SecureUser("gaurav", "s3cr3t", "admin")).toJsonString()

        assertTrue(!result.contains("password"), "field name should be absent")
        assertTrue(!result.contains("s3cr3t"),   "field value should be absent")
        assertContains(result, "\"username\": \"gaurav\"")
        assertContains(result, "\"role\": \"admin\"")
    }

    data class ApiUser(
        @JsonProperty("full_name") val name: String,
        @JsonProperty("user_age")  val age: Int
    )

    @Test
    fun `@JsonProperty — field appears under the custom key name`() {
        val result = proJson.toJson(ApiUser("Gaurav", 25)).toJsonString()

        assertContains(result, "\"full_name\": \"Gaurav\"")
        assertContains(result, "\"user_age\": 25")
        assertTrue(!result.contains("\"name\":"),  "original key should be absent")
        assertTrue(!result.contains("\"age\":"),   "original key should be absent")
    }

    class HexColorMapper : TextMapper {
        override fun map(obj: Any): String {
            val c = obj as RgbColor
            return "#%02X%02X%02X".format(c.r, c.g, c.b)
        }
    }

    @JsonString(HexColorMapper::class)
    data class RgbColor(val r: Int, val g: Int, val b: Int)

    @Test
    fun `@JsonString — object is serialized as a plain string`() {
        val result = proJson.toJson(RgbColor(255, 87, 51)).toJsonString()
        assertEquals("\"#FF5733\"", result)
    }

    data class PostWithRef(
        val title: String,
        @Reference val author: ApiUser
    )

    @Test
    fun `@Reference — field emits a dollar-ref pointer instead of full object`() {
        val author = ApiUser("Gaurav", 25)
        val post   = PostWithRef("Hello World", author)
        val result = proJson.toJson(post).toJsonString()

        assertContains(result, "\"\$ref\":")
        // ApiUser object body must NOT be inlined inside the post
        val typeCount = result.split("\"\$type\": \"ApiUser\"").size - 1
        assertEquals(0, typeCount, "ApiUser should not be fully inlined inside post")
    }

    // ─────────────────────────────────────────────────────────────
    // 6. Plugin usage
    // ─────────────────────────────────────────────────────────────

    class UpperCaseStringPlugin : JsonPlugin {
        override fun supports(clazz: Class<*>) = clazz == String::class.java
        override fun transform(obj: Any) = (obj as String).uppercase()
    }

    @Test
    fun `registered plugin overrides default String serialization`() {
        val pj = ProJson()
        pj.registerPlugin(UpperCaseStringPlugin())
        assertEquals("\"HELLO\"", pj.toJson("hello").toJsonString())
    }

    class BooleanLabelPlugin : JsonPlugin {
        override fun supports(clazz: Class<*>) = clazz == Boolean::class.javaObjectType
        override fun transform(obj: Any) = if (obj as Boolean) "YES" else "NO"
    }

    @Test
    fun `plugin transforms booleans to custom labels`() {
        val pj = ProJson()
        pj.registerPlugin(BooleanLabelPlugin())
        assertEquals("\"YES\"", pj.toJson(true).toJsonString())
        assertEquals("\"NO\"",  pj.toJson(false).toJsonString())
    }

    @Test
    fun `plugin only affects its declared type — other types pass through unchanged`() {
        val pj = ProJson()
        pj.registerPlugin(UpperCaseStringPlugin())
        assertEquals("42", pj.toJson(42).toJsonString())
    }

    // ─────────────────────────────────────────────────────────────
    // 7. Circular reference safety
    // ─────────────────────────────────────────────────────────────

    data class Node(val label: String, var next: Node? = null)

    @Test
    fun `circular reference does not cause a StackOverflowError`() {
        val a = Node("A")
        val b = Node("B")
        a.next = b
        b.next = a   // circular

        val result = proJson.toJson(a).toJsonString()
        assertContains(result, "\"\$ref\":")
    }

    @Test
    fun `self-referencing object emits a dollar-ref for the cycle`() {
        val a = Node("self")
        a.next = a

        val result = proJson.toJson(a).toJsonString()
        assertContains(result, "\"\$ref\":")
        assertContains(result, "\"\$id\":")
    }

    // ─────────────────────────────────────────────────────────────
    // 8. Real-world composite scenario
    // ─────────────────────────────────────────────────────────────

    data class FakeDate(val year: Int, val month: Int, val day: Int)

    class IsoDatePlugin : JsonPlugin {
        override fun supports(clazz: Class<*>) = clazz == FakeDate::class.java
        override fun transform(obj: Any): String {
            val d = obj as FakeDate
            return "%04d-%02d-%02d".format(d.year, d.month, d.day)
        }
    }

    data class Product(
        @JsonProperty("product_name") val name: String,
        val price: Double,
        @JsonIgnore val internalCode: String,
        val tags: List<String>,
        val releaseDate: FakeDate
    )

    @Test
    fun `real-world scenario — product with plugin, annotation, and collection`() {
        val pj = ProJson()
        pj.registerPlugin(IsoDatePlugin())

        val product = Product(
            name         = "ProJson Library",
            price        = 0.0,
            internalCode = "SKU-9999",
            tags         = listOf("kotlin", "json", "serialization"),
            releaseDate  = FakeDate(2025, 5, 14)
        )

        val result = pj.toJson(product).toJsonString()

        assertContains(result, "\"product_name\": \"ProJson Library\"")
        assertContains(result, "\"price\": 0.0")
        assertTrue(!result.contains("internalCode"), "@JsonIgnore field must be absent")
        assertTrue(!result.contains("SKU-9999"),     "@JsonIgnore value must be absent")
        assertContains(result, "[\"kotlin\", \"json\", \"serialization\"]")
        assertContains(result, "\"releaseDate\": \"2025-05-14\"")
    }
}
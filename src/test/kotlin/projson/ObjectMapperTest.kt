package projson

import projson.annotations.JsonIgnore
import projson.annotations.JsonProperty
import projson.annotations.JsonString
import projson.annotations.Reference
import projson.context.MappingContext
import projson.mapper.CollectionMapper
import projson.mapper.MapMapper
import projson.mapper.ObjectMapper
import projson.mapper.PrimitiveMapper
import projson.mapper.TextMapper
import projson.plugin.PluginManager
import projson.reference.ReferenceManager
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for ObjectMapper.
 *
 * Focus: reflection accuracy, annotation processing,
 * $id/$type emission, and correct delegation to context.convert()
 * for nested values.
 */
class ObjectMapperTest {

    private fun makeContext() = MappingContext(
        mappers          = listOf(PrimitiveMapper(), CollectionMapper(), MapMapper(), ObjectMapper()),
        referenceManager = ReferenceManager(),
        pluginManager    = PluginManager()
    )

    private val mapper = ObjectMapper()

    // ─────────────────────────────────────────────────────────────
    // canHandle
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `canHandle returns true for a custom data class`() {
        data class Foo(val x: Int)
        assertTrue(mapper.canHandle(Foo(1)))
    }

    @Test
    fun `canHandle returns false for null`() {
        assertFalse(mapper.canHandle(null))
    }

    @Test
    fun `canHandle returns false for String`() {
        assertFalse(mapper.canHandle("hello"))
    }

    @Test
    fun `canHandle returns false for Int`() {
        assertFalse(mapper.canHandle(42))
    }

    @Test
    fun `canHandle returns false for a List`() {
        assertFalse(mapper.canHandle(listOf(1, 2)))
    }

    // ─────────────────────────────────────────────────────────────
    // $id and $type emission
    // ─────────────────────────────────────────────────────────────

    data class Point(val x: Int, val y: Int)

    @Test
    fun `emits dollar-id field for every object`() {
        val result = mapper.map(Point(1, 2), makeContext()).toJsonString()
        assertContains(result, "\"\$id\":")
    }

    @Test
    fun `emits dollar-type equal to the simple class name`() {
        val result = mapper.map(Point(3, 4), makeContext()).toJsonString()
        assertContains(result, "\"\$type\": \"Point\"")
    }

    @Test
    fun `dollar-id is a non-blank string`() {
        val json = mapper.map(Point(0, 0), makeContext()).toJsonString()
        // extract the $id value — it sits after "$id": "
        val id = json.substringAfter("\"\$id\": \"").substringBefore("\"")
        assertTrue(id.isNotBlank())
    }

    // ─────────────────────────────────────────────────────────────
    // Basic field mapping
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `maps all fields of a simple data class`() {
        val result = mapper.map(Point(10, 20), makeContext()).toJsonString()
        assertContains(result, "\"x\": 10")
        assertContains(result, "\"y\": 20")
    }

    data class Person(val name: String, val age: Int, val active: Boolean)

    @Test
    fun `maps String, Int, and Boolean fields correctly`() {
        val result = mapper.map(Person("Alice", 30, true), makeContext()).toJsonString()
        assertContains(result, "\"name\": \"Alice\"")
        assertContains(result, "\"age\": 30")
        assertContains(result, "\"active\": true")
    }

    data class Wrapper(val value: Any?)

    @Test
    fun `maps a null field to JSON null`() {
        val result = mapper.map(Wrapper(null), makeContext()).toJsonString()
        assertContains(result, "\"value\": null")
    }

    // ─────────────────────────────────────────────────────────────
    // Nested objects
    // ─────────────────────────────────────────────────────────────

    data class Address(val street: String, val city: String)
    data class Employee(val name: String, val address: Address)

    @Test
    fun `recursively maps nested object`() {
        val emp    = Employee("Bob", Address("Park Lane", "London"))
        val result = mapper.map(emp, makeContext()).toJsonString()

        assertContains(result, "\"name\": \"Bob\"")
        assertContains(result, "\"street\": \"Park Lane\"")
        assertContains(result, "\"city\": \"London\"")
    }

    @Test
    fun `nested object also gets dollar-type`() {
        val emp    = Employee("Bob", Address("Park Lane", "London"))
        val result = mapper.map(emp, makeContext()).toJsonString()
        assertContains(result, "\"\$type\": \"Address\"")
    }

    // ─────────────────────────────────────────────────────────────
    // List field inside object
    // ─────────────────────────────────────────────────────────────

    data class Team(val name: String, val members: List<String>)

    @Test
    fun `maps a list field to a JSON array`() {
        val result = mapper.map(Team("Dev", listOf("Alice", "Bob")), makeContext()).toJsonString()
        assertContains(result, "[\"Alice\", \"Bob\"]")
    }

    // ─────────────────────────────────────────────────────────────
    // @JsonIgnore
    // ─────────────────────────────────────────────────────────────

    data class Account(
        val username: String,
        @JsonIgnore val password: String,
        val email: String
    )

    @Test
    fun `@JsonIgnore — field name absent from output`() {
        val result = mapper.map(Account("alice", "secret", "alice@x.com"), makeContext()).toJsonString()
        assertFalse(result.contains("password"), "field name must be absent")
    }

    @Test
    fun `@JsonIgnore — field value absent from output`() {
        val result = mapper.map(Account("alice", "secret", "alice@x.com"), makeContext()).toJsonString()
        assertFalse(result.contains("secret"), "field value must be absent")
    }

    @Test
    fun `@JsonIgnore — other fields still present`() {
        val result = mapper.map(Account("alice", "secret", "alice@x.com"), makeContext()).toJsonString()
        assertContains(result, "\"username\": \"alice\"")
        assertContains(result, "\"email\": \"alice@x.com\"")
    }

    // ─────────────────────────────────────────────────────────────
    // @JsonProperty
    // ─────────────────────────────────────────────────────────────

    data class ApiResponse(
        @JsonProperty("status_code") val code: Int,
        @JsonProperty("response_message") val message: String
    )

    @Test
    fun `@JsonProperty — custom key appears in output`() {
        val result = mapper.map(ApiResponse(200, "OK"), makeContext()).toJsonString()
        assertContains(result, "\"status_code\": 200")
        assertContains(result, "\"response_message\": \"OK\"")
    }

    @Test
    fun `@JsonProperty — original Kotlin field name absent`() {
        val result = mapper.map(ApiResponse(200, "OK"), makeContext()).toJsonString()
        assertFalse(result.contains("\"code\":"),    "original key must be absent")
        assertFalse(result.contains("\"message\":"), "original key must be absent")
    }

    // ─────────────────────────────────────────────────────────────
    // @JsonString (class-level)
    // ─────────────────────────────────────────────────────────────

    class MoneyMapper : TextMapper {
        override fun map(obj: Any): String {
            val m = obj as Money
            return "$${m.amount} ${m.currency}"
        }
    }

    @JsonString(MoneyMapper::class)
    data class Money(val amount: Double, val currency: String)

    @Test
    fun `@JsonString — object serialized as a plain string primitive`() {
        val result = mapper.map(Money(9.99, "USD"), makeContext()).toJsonString()
        assertEquals("\"$9.99 USD\"", result)
    }

    @Test
    fun `@JsonString — no dollar-id emitted (treated as primitive)`() {
        val result = mapper.map(Money(9.99, "USD"), makeContext()).toJsonString()
        assertFalse(result.contains("\$id"), "\$id must not appear for @JsonString objects")
    }

    // ─────────────────────────────────────────────────────────────
    // @Reference
    // ─────────────────────────────────────────────────────────────

    data class Comment(val body: String)
    data class Article(
        val title: String,
        @Reference val featured: Comment
    )

    @Test
    fun `@Reference — field emits dollar-ref not full object`() {
        val comment = Comment("Great article!")
        val article = Article("TDD in Kotlin", comment)
        val result  = mapper.map(article, makeContext()).toJsonString()

        assertContains(result, "\"\$ref\":")
        // Comment object body must not be inlined inside article
        assertFalse(result.contains("\"\$type\": \"Comment\""))
    }

    // ─────────────────────────────────────────────────────────────
    // Multiple annotations on same class
    // ─────────────────────────────────────────────────────────────

    data class Mixed(
        @JsonProperty("first_name") val firstName: String,
        @JsonIgnore val ssn: String,
        val age: Int
    )

    @Test
    fun `@JsonProperty and @JsonIgnore work together on the same class`() {
        val result = mapper.map(Mixed("John", "123-45-6789", 40), makeContext()).toJsonString()

        assertContains(result, "\"first_name\": \"John\"")
        assertContains(result, "\"age\": 40")
        assertFalse(result.contains("\"firstName\":"), "original name must be absent")
        assertFalse(result.contains("ssn"),            "@JsonIgnore field must be absent")
        assertFalse(result.contains("123-45-6789"),    "@JsonIgnore value must be absent")
    }
}
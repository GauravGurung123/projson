package projson

import projson.annotations.JsonIgnore
import projson.annotations.JsonProperty
import projson.annotations.JsonString
import projson.annotations.Reference
import projson.mapper.TextMapper
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * End-to-end annotation behaviour tests driven through the public ProJson API.
 *
 * Each annotation gets its own section covering:
 *  - basic
 *  - edge cases
 *  - combination with other annotations
 */
class AnnotationTest {

    private val proJson = ProJson()

    // ══════════════════════════════════════════════════════════════
    // @JsonIgnore
    // ══════════════════════════════════════════════════════════════

    data class BasicIgnore(
        val visible: String,
        @JsonIgnore val hidden: String
    )

    @Test
    fun `@JsonIgnore — annotated field name is absent`() {
        val result = proJson.toJson(BasicIgnore("show", "hide")).toJsonString()
        assertFalse(result.contains("hidden"), "field name must not appear")
    }

    @Test
    fun `@JsonIgnore — annotated field value is absent`() {
        val result = proJson.toJson(BasicIgnore("show", "hide")).toJsonString()
        assertFalse(result.contains("hide"), "field value must not appear")
    }

    @Test
    fun `@JsonIgnore — non-annotated field remains present`() {
        val result = proJson.toJson(BasicIgnore("show", "hide")).toJsonString()
        assertContains(result, "\"visible\": \"show\"")
    }

    data class AllIgnored(
        @JsonIgnore val a: String,
        @JsonIgnore val b: Int
    )

    @Test
    fun `@JsonIgnore on every field — output contains only dollar-id and dollar-type`() {
        val result = proJson.toJson(AllIgnored("x", 1)).toJsonString()
        assertFalse(result.contains("\"a\":"))
        assertFalse(result.contains("\"b\":"))
        assertContains(result, "\"\$id\":")
        assertContains(result, "\"\$type\":")
    }

    data class IgnoreNumeric(
        val name: String,
        @JsonIgnore val internalId: Int,
        @JsonIgnore val score: Double
    )

    @Test
    fun `@JsonIgnore works on numeric fields`() {
        val result = proJson.toJson(IgnoreNumeric("Alice", 99, 8.5)).toJsonString()
        assertFalse(result.contains("internalId"))
        assertFalse(result.contains("score"))
        assertContains(result, "\"name\": \"Alice\"")
    }

    data class IgnoreBool(
        val label: String,
        @JsonIgnore val flag: Boolean
    )

    @Test
    fun `@JsonIgnore works on boolean fields`() {
        val result = proJson.toJson(IgnoreBool("test", true)).toJsonString()
        assertFalse(result.contains("flag"))
        assertContains(result, "\"label\": \"test\"")
    }

    // ══════════════════════════════════════════════════════════════
    // @JsonProperty
    // ══════════════════════════════════════════════════════════════

    data class Renamed(
        @JsonProperty("first_name") val firstName: String,
        @JsonProperty("last_name")  val lastName: String
    )

    @Test
    fun `@JsonProperty — custom key appears in output`() {
        val result = proJson.toJson(Renamed("John", "Doe")).toJsonString()
        assertContains(result, "\"first_name\": \"John\"")
        assertContains(result, "\"last_name\": \"Doe\"")
    }

    @Test
    fun `@JsonProperty — original Kotlin name is absent`() {
        val result = proJson.toJson(Renamed("John", "Doe")).toJsonString()
        assertFalse(result.contains("\"firstName\":"))
        assertFalse(result.contains("\"lastName\":"))
    }

    data class SnakeCase(
        @JsonProperty("user_age") val userAge: Int,
        @JsonProperty("is_active") val isActive: Boolean
    )

    @Test
    fun `@JsonProperty snake_case renaming`() {
        val result = proJson.toJson(SnakeCase(25, true)).toJsonString()
        assertContains(result, "\"user_age\": 25")
        assertContains(result, "\"is_active\": true")
    }

    data class MixedRename(
        @JsonProperty("alias") val name: String,
        val age: Int   // no annotation — uses Kotlin field name
    )

    @Test
    fun `@JsonProperty on some fields — un-annotated fields keep their name`() {
        val result = proJson.toJson(MixedRename("Bob", 30)).toJsonString()
        assertContains(result, "\"alias\": \"Bob\"")
        assertContains(result, "\"age\": 30")
        assertFalse(result.contains("\"name\":"))
    }

    // ══════════════════════════════════════════════════════════════
    // @JsonString
    // ══════════════════════════════════════════════════════════════

    class TemperatureMapper : TextMapper {
        override fun map(obj: Any): String {
            val t = obj as Temperature
            return "${t.value}${t.unit}"
        }
    }

    @JsonString(TemperatureMapper::class)
    data class Temperature(val value: Double, val unit: String)

    @Test
    fun `@JsonString — class serialized as a plain JSON string`() {
        val result = proJson.toJson(Temperature(36.6, "°C")).toJsonString()
        assertEquals("\"36.6°C\"", result)
    }

    @Test
    fun `@JsonString — no dollar-id or dollar-type emitted`() {
        val result = proJson.toJson(Temperature(100.0, "°F")).toJsonString()
        assertFalse(result.contains("\$id"),   "\$id must not appear")
        assertFalse(result.contains("\$type"), "\$type must not appear")
    }

    class CoordMapper : TextMapper {
        override fun map(obj: Any): String {
            val c = obj as Coordinate
            return "(${c.lat}, ${c.lng})"
        }
    }

    @JsonString(CoordMapper::class)
    data class Coordinate(val lat: Double, val lng: Double)

    data class Location(val name: String, val coord: Coordinate)

    @Test
    fun `@JsonString field inside a parent object is a string value`() {
        val result = proJson.toJson(Location("HQ", Coordinate(12.97, 77.59))).toJsonString()
        assertContains(result, "\"coord\": \"(12.97, 77.59)\"")
    }

    // ══════════════════════════════════════════════════════════════
    // @Reference
    // ══════════════════════════════════════════════════════════════

    data class Tag(val label: String)
    data class Note(
        val title: String,
        @Reference val tag: Tag
    )

    @Test
    fun `@Reference — emits dollar-ref for the referenced object`() {
        val result = proJson.toJson(Note("My Note", Tag("important"))).toJsonString()
        assertContains(result, "\"\$ref\":")
    }

    @Test
    fun `@Reference — referenced object body not inlined in parent`() {
        val result = proJson.toJson(Note("My Note", Tag("important"))).toJsonString()
        // Tag must not be fully serialized inside Note
        assertFalse(result.contains("\"\$type\": \"Tag\""))
    }

    data class Folder(val name: String)
    data class FileItem(
        val filename: String,
        @Reference val folder: Folder,
        @Reference val backup: Folder
    )

    @Test
    fun `@Reference on two fields — both emit dollar-ref`() {
        val f      = Folder("docs")
        val result = proJson.toJson(FileItem("readme.md", f, f)).toJsonString()
        val refCount = result.split("\"\$ref\":").size - 1
        assertEquals(2, refCount, "expected two \$ref entries")
    }

    // ══════════════════════════════════════════════════════════════
    // Annotation combinations
    // ══════════════════════════════════════════════════════════════

    data class Combined(
        @JsonProperty("pub_name") val name: String,
        @JsonIgnore val secret: String,
        val plain: Int
    )

    @Test
    fun `@JsonProperty and @JsonIgnore coexist correctly`() {
        val result = proJson.toJson(Combined("Alice", "hidden", 7)).toJsonString()

        assertContains(result, "\"pub_name\": \"Alice\"")
        assertContains(result, "\"plain\": 7")
        assertFalse(result.contains("\"name\":"))
        assertFalse(result.contains("secret"))
        assertFalse(result.contains("hidden"))
    }

    class CurrencyMapper : TextMapper {
        override fun map(obj: Any): String {
            val c = obj as Currency
            return "${c.symbol}${c.code}"
        }
    }

    @JsonString(CurrencyMapper::class)
    data class Currency(val code: String, val symbol: String)

    data class Invoice(
        @JsonProperty("invoice_id") val id: String,
        @JsonIgnore val rawTotal: Double,
        val currency: Currency
    )

    @Test
    fun `@JsonString, @JsonProperty, and @JsonIgnore all work together`() {
        val result = proJson.toJson(
            Invoice("INV-001", 999.99, Currency("USD", "$"))
        ).toJsonString()

        assertContains(result, "\"invoice_id\": \"INV-001\"")
        assertContains(result, "\"currency\": \"\$USD\"")
        assertFalse(result.contains("rawTotal"))
        assertFalse(result.contains("999.99"))
        assertFalse(result.contains("\"id\":"))
    }
}
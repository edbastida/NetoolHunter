package com.netoolhunter.app.data

import com.netoolhunter.app.domain.Category
import com.netoolhunter.app.domain.Tool
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

@Serializable
private data class CatalogPayload(
    val version: Int = 1,
    val tools: List<Tool>
)

/**
 * Validates the bundled `assets/catalog.json` (the source of truth at runtime).
 * The earlier `ToolsCatalog` Kotlin object was deleted in v1.1.0 — this test
 * now parses the JSON and runs the same invariants against it.
 */
class ToolsCatalogTest {

    // The blueprint header says "95 tools" but the actual section-6 tables list 92.
    // Tables are the source of truth. Update this number deliberately when adding tools.
    private val EXPECTED_TOOL_COUNT = 92

    private val tools: List<Tool> by lazy {
        val raw = File("src/main/assets/catalog.json").readText()
        val json = Json {
            classDiscriminator = "type"
            ignoreUnknownKeys = true
        }
        json.decodeFromString(CatalogPayload.serializer(), raw).tools
    }

    @Test
    fun `bundled catalog has expected number of tools`() {
        assertEquals(EXPECTED_TOOL_COUNT, tools.size)
    }

    @Test
    fun `all tool ids are unique`() {
        val ids = tools.map { it.id }
        assertEquals(ids.size, ids.distinct().size)
    }

    @Test
    fun `all categories have at least one tool`() {
        Category.entries.forEach { cat ->
            val countForCat = tools.count { it.category == cat }
            assertTrue("Category $cat has 0 tools", countForCat >= 1)
        }
    }

    @Test
    fun `tool ids are lowercase and slug-safe`() {
        tools.forEach { tool ->
            assertEquals("id should be lowercase: ${tool.id}", tool.id.lowercase(), tool.id)
            assertTrue(
                "id should not contain spaces: ${tool.id}",
                ' ' !in tool.id
            )
        }
    }
}

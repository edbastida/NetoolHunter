package com.netoolhunter.app.data

import com.netoolhunter.app.domain.Category
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ToolsCatalogTest {

    // The blueprint header says "95 tools" but the actual section-6 tables list 92.
    // Tables are the source of truth. Update this number deliberately when adding tools.
    private val EXPECTED_TOOL_COUNT = 92

    @Test
    fun `catalog has expected number of tools`() {
        assertEquals(EXPECTED_TOOL_COUNT, ToolsCatalog.ALL.size)
    }

    @Test
    fun `all tool ids are unique`() {
        val ids = ToolsCatalog.ALL.map { it.id }
        assertEquals(ids.size, ids.distinct().size)
    }

    @Test
    fun `all categories have at least one tool`() {
        Category.entries.forEach { cat ->
            val countForCat = ToolsCatalog.ALL.count { it.category == cat }
            assertTrue("Category $cat has 0 tools", countForCat >= 1)
        }
    }

    @Test
    fun `tool ids are lowercase and slug-safe`() {
        ToolsCatalog.ALL.forEach { tool ->
            assertEquals("id should be lowercase: ${tool.id}", tool.id.lowercase(), tool.id)
            assertTrue(
                "id should not contain spaces: ${tool.id}",
                ' ' !in tool.id
            )
        }
    }

    @Test
    fun `byId lookup matches each tool`() {
        ToolsCatalog.ALL.forEach { tool ->
            assertEquals(tool, ToolsCatalog.byId[tool.id])
        }
    }
}

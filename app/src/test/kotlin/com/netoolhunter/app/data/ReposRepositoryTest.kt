package com.netoolhunter.app.data

import com.netoolhunter.app.domain.Repo
import com.netoolhunter.app.shell.ShellExecutor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure-logic tests for sources.list rendering. We can't easily test DataStore here
 * without instrumentation, so we exercise renderSourcesList() directly.
 */
class ReposRepositoryTest {

    private fun repo(id: String, line: String, enabled: Boolean) =
        Repo(id = id, name = id, sourceLine = line, enabled = enabled)

    private fun newRepository(): ReposRepository =
        // dataStore + shell are unused by renderSourcesList so we pass empty stubs.
        ReposRepository(dataStore = emptyDataStore(), shell = ShellExecutor())

    @Test
    fun `rendered file starts with generated-by header`() {
        val out = newRepository().renderSourcesList(emptyList(), timestamp = "2026-05-01")
        assertTrue(out.startsWith("# Generado por NetoolHunter — 2026-05-01"))
    }

    @Test
    fun `only enabled repos are written`() {
        val r1 = repo("a", "deb http://a kali-rolling main", enabled = true)
        val r2 = repo("b", "deb http://b kali-rolling main", enabled = false)
        val r3 = repo("c", "deb http://c kali-rolling main", enabled = true)
        val out = newRepository().renderSourcesList(listOf(r1, r2, r3), timestamp = "X")

        assertTrue(out.contains("deb http://a"))
        assertTrue(out.contains("deb http://c"))
        assertTrue(!out.contains("deb http://b"))
    }

    @Test
    fun `each enabled repo appears on its own line`() {
        val r1 = repo("a", "deb http://a kali-rolling main", enabled = true)
        val r2 = repo("b", "deb http://b kali-rolling main", enabled = true)
        val out = newRepository().renderSourcesList(listOf(r1, r2), timestamp = "X")
        val lines = out.lines().filter { it.isNotBlank() && !it.startsWith("#") }
        assertEquals(2, lines.size)
    }

    /** Returns a DataStore exposing an empty Preferences flow — enough for renderSourcesList tests. */
    private fun emptyDataStore(): androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences> {
        val empty = androidx.datastore.preferences.core.emptyPreferences()
        return object : androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences> {
            override val data: kotlinx.coroutines.flow.Flow<androidx.datastore.preferences.core.Preferences> =
                kotlinx.coroutines.flow.flowOf(empty)

            override suspend fun updateData(
                transform: suspend (androidx.datastore.preferences.core.Preferences) -> androidx.datastore.preferences.core.Preferences
            ): androidx.datastore.preferences.core.Preferences = empty
        }
    }
}

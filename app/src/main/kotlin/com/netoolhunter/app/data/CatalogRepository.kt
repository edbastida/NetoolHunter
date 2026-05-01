package com.netoolhunter.app.data

import android.content.Context
import com.netoolhunter.app.domain.Category
import com.netoolhunter.app.domain.Tool
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.net.URL

@Serializable
private data class CatalogPayload(
    val version: Int = 1,
    val tools: List<Tool>
)

class CatalogRepository(private val context: Context) {

    private val json = Json {
        classDiscriminator = "type"
        ignoreUnknownKeys = true
    }

    private val cachedFile: File by lazy { File(context.filesDir, "catalog.json") }

    private val _tools = MutableStateFlow(loadInitial())
    val tools: StateFlow<List<Tool>> = _tools.asStateFlow()

    fun byId(id: String): Tool? = _tools.value.firstOrNull { it.id == id }
    fun byCategory(category: Category): List<Tool> = _tools.value.filter { it.category == category }
    fun countByCategory(category: Category): Int = _tools.value.count { it.category == category }

    private fun loadInitial(): List<Tool> {
        val raw = if (cachedFile.exists()) {
            runCatching { cachedFile.readText() }.getOrNull() ?: loadBundled()
        } else loadBundled()
        return parseOrEmpty(raw)
    }

    private fun loadBundled(): String =
        context.assets.open(BUNDLED_ASSET).bufferedReader().use { it.readText() }

    private fun parseOrEmpty(raw: String): List<Tool> = runCatching {
        json.decodeFromString(CatalogPayload.serializer(), raw).tools
    }.getOrDefault(emptyList())

    sealed class UpdateResult {
        data class Success(val toolCount: Int) : UpdateResult()
        data class Failed(val reason: String) : UpdateResult()
    }

    /**
     * One-shot, user-triggered refresh from a remote URL. Validates by parsing
     * before persisting — a corrupt/incomplete download won't replace the cache.
     * The bundled assets/catalog.json is never overwritten; this writes to filesDir.
     */
    suspend fun updateFromUrl(url: String = DEFAULT_URL): UpdateResult = withContext(Dispatchers.IO) {
        try {
            val raw = URL(url).openConnection().apply {
                connectTimeout = 10_000
                readTimeout = 15_000
            }.getInputStream().bufferedReader().use { it.readText() }
            val parsed = json.decodeFromString(CatalogPayload.serializer(), raw)
            cachedFile.writeText(raw)
            _tools.value = parsed.tools
            UpdateResult.Success(parsed.tools.size)
        } catch (t: Throwable) {
            UpdateResult.Failed(t.message ?: "Error desconocido")
        }
    }

    /** Drop the user-downloaded cache and revert to the bundled catalog. */
    fun resetToBundled(): Boolean {
        val deleted = cachedFile.exists() && cachedFile.delete()
        if (deleted) _tools.value = parseOrEmpty(loadBundled())
        return deleted
    }

    fun usingDownloadedCatalog(): Boolean = cachedFile.exists()

    companion object {
        const val DEFAULT_URL =
            "https://raw.githubusercontent.com/edbastida/NetoolHunter/main/catalog.json"
        private const val BUNDLED_ASSET = "catalog.json"
    }
}

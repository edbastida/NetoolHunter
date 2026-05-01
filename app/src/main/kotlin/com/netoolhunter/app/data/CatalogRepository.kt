package com.netoolhunter.app.data

import android.content.Context
import com.netoolhunter.app.domain.Category
import com.netoolhunter.app.domain.Tool
import com.netoolhunter.app.shell.KaliEntryPoint
import com.netoolhunter.app.shell.ShellExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
private data class CatalogPayload(
    val version: Int = 1,
    val tools: List<Tool>
)

class CatalogRepository(
    private val context: Context,
    private val shell: ShellExecutor
) {

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
     * One-shot, user-triggered refresh. Downloads the JSON via curl/wget INSIDE
     * the Kali chroot (which has working network — same path that runs
     * `apt-get update`). The app's own JVM doesn't make any network call; this
     * is what lets us keep the manifest free of `INTERNET` permission.
     *
     * Validates by parsing before persisting — a corrupt or partial download
     * won't replace the cache. Bundled assets/catalog.json is never overwritten;
     * this writes to filesDir.
     */
    suspend fun updateFromUrl(url: String = DEFAULT_URL): UpdateResult = withContext(Dispatchers.IO) {
        try {
            val safeUrl = url.replace("'", "'\\''")
            val fetchCmd = "curl -fsSL --max-time 30 '$safeUrl' " +
                "|| wget -qO- --timeout=30 '$safeUrl'"
            val result = shell.execCapture(KaliEntryPoint.wrap(fetchCmd))
            if (result.exit != 0) {
                return@withContext UpdateResult.Failed(
                    "Descarga falló (exit=${result.exit}). ${result.stderr.take(200)}"
                )
            }
            val raw = result.stdout
            if (raw.isBlank()) {
                return@withContext UpdateResult.Failed("Respuesta vacía de la URL")
            }
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

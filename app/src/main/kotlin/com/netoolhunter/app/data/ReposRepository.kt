package com.netoolhunter.app.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.netoolhunter.app.domain.InstallEvent
import com.netoolhunter.app.domain.Repo
import com.netoolhunter.app.shell.ShellExecutor
import com.netoolhunter.app.util.DataStoreKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class ReposRepository(
    private val dataStore: DataStore<Preferences>,
    private val shell: ShellExecutor
) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    /** All repos = defaults (with current enabled state) + custom. */
    val repos: Flow<List<Repo>> = dataStore.data.map { prefs ->
        val enabledIds: Set<String> = prefs[DataStoreKeys.ENABLED_REPOS] ?: defaultEnabledIds()
        val customJson: String = prefs[DataStoreKeys.CUSTOM_REPOS_JSON] ?: "[]"
        val customRepos: List<Repo> = runCatching {
            json.decodeFromString(ListSerializer(Repo.serializer()), customJson)
        }.getOrDefault(emptyList())

        val defaults = DefaultRepos.ALL.map { it.copy(enabled = it.id in enabledIds) }
        val customs = customRepos.map { it.copy(enabled = it.id in enabledIds, isCustom = true) }
        defaults + customs
    }

    suspend fun setEnabled(id: String, enabled: Boolean) {
        dataStore.edit { prefs ->
            val current = prefs[DataStoreKeys.ENABLED_REPOS] ?: defaultEnabledIds()
            prefs[DataStoreKeys.ENABLED_REPOS] = if (enabled) current + id else current - id
        }
    }

    suspend fun addCustom(name: String, sourceLine: String) {
        val id = "custom-${System.currentTimeMillis()}"
        val newRepo = Repo(id = id, name = name, sourceLine = sourceLine, enabled = true, isCustom = true)

        dataStore.edit { prefs ->
            val customJson = prefs[DataStoreKeys.CUSTOM_REPOS_JSON] ?: "[]"
            val current = runCatching {
                json.decodeFromString(ListSerializer(Repo.serializer()), customJson)
            }.getOrDefault(emptyList())
            prefs[DataStoreKeys.CUSTOM_REPOS_JSON] =
                json.encodeToString(ListSerializer(Repo.serializer()), current + newRepo)

            val enabled = prefs[DataStoreKeys.ENABLED_REPOS] ?: defaultEnabledIds()
            prefs[DataStoreKeys.ENABLED_REPOS] = enabled + id
        }
    }

    suspend fun removeCustom(id: String) {
        dataStore.edit { prefs ->
            val customJson = prefs[DataStoreKeys.CUSTOM_REPOS_JSON] ?: "[]"
            val current = runCatching {
                json.decodeFromString(ListSerializer(Repo.serializer()), customJson)
            }.getOrDefault(emptyList())
            prefs[DataStoreKeys.CUSTOM_REPOS_JSON] =
                json.encodeToString(ListSerializer(Repo.serializer()), current.filter { it.id != id })

            val enabled = prefs[DataStoreKeys.ENABLED_REPOS] ?: defaultEnabledIds()
            prefs[DataStoreKeys.ENABLED_REPOS] = enabled - id
        }
    }

    private suspend fun buildEnabledRepos(): List<Repo> = repos.first().filter { it.enabled }

    /**
     * Apply enabled repos to /etc/apt/sources.list inside the chroot.
     * Pipeline (sequential via &&; first failure short-circuits and emits non-zero Exit):
     *   cp sources.list sources.list.bak
     *   cat > sources.list <<EOF ... EOF
     *   apt-get update
     */
    suspend fun applyAndStream(): Flow<InstallEvent> {
        val enabled = buildEnabledRepos()
        val now = nowIso()
        val body = buildString {
            appendLine("# Generado por NetoolHunter — $now")
            enabled.forEach { appendLine(it.sourceLine) }
        }
        val backup = "cp /etc/apt/sources.list /etc/apt/sources.list.bak 2>/dev/null || true"
        val write = "cat > /etc/apt/sources.list <<'NETOOLHUNTER_EOF'\n${body}NETOOLHUNTER_EOF"
        val update = "apt-get update"
        val cmd = "$backup && $write && $update"
        return shell.execInKali(cmd)
    }

    /** Generates the sources.list contents the way applyAndStream() would (used in tests). */
    fun renderSourcesList(repos: List<Repo>, timestamp: String = nowIso()): String = buildString {
        appendLine("# Generado por NetoolHunter — $timestamp")
        repos.filter { it.enabled }.forEach { appendLine(it.sourceLine) }
    }

    private fun defaultEnabledIds(): Set<String> =
        DefaultRepos.ALL.filter { it.enabled }.map { it.id }.toSet()

    private fun nowIso(): String {
        val now = java.time.LocalDate.now()
        return "${now.year}-${"%02d".format(now.monthValue)}-${"%02d".format(now.dayOfMonth)}"
    }
}

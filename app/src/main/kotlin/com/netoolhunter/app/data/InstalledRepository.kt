package com.netoolhunter.app.data

import com.netoolhunter.app.domain.Tool
import com.netoolhunter.app.shell.KaliCommand
import com.netoolhunter.app.shell.ShellExecutor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.Dispatchers

class InstalledRepository(
    private val shell: ShellExecutor
) {
    private val _installedIds = MutableStateFlow<Set<String>>(emptySet())
    val installedIds: StateFlow<Set<String>> = _installedIds.asStateFlow()

    /**
     * Re-runs detection across the full catalog. Concurrency 4 = empirically a balance
     * between throughput and not saturating su/nh. Returns the resulting set so callers
     * can await completion.
     */
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    suspend fun scan(): Set<String> {
        val tools = ToolsCatalog.ALL
        val installed: Set<String> = tools.asFlow()
            .flatMapMerge(concurrency = 4) { tool ->
                flow {
                    val cmd = KaliCommand.isInstalled(tool)
                    val ok = shell.execInKaliBlocking(cmd) == 0
                    if (ok) emit(tool.id)
                }
            }
            .flowOn(Dispatchers.IO)
            .fold(mutableSetOf<String>()) { acc, id -> acc.also { it.add(id) } }
        _installedIds.value = installed
        return installed
    }

    fun isInstalled(toolId: String): Boolean = toolId in _installedIds.value

    fun installedTools(): List<Tool> =
        ToolsCatalog.ALL.filter { it.id in _installedIds.value }

    /** Optimistically mark a tool as (un)installed without re-scanning. */
    fun markInstalled(toolId: String, installed: Boolean) {
        _installedIds.value = if (installed) {
            _installedIds.value + toolId
        } else {
            _installedIds.value - toolId
        }
    }
}

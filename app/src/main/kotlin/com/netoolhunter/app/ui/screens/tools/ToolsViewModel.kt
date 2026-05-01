package com.netoolhunter.app.ui.screens.tools

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.netoolhunter.app.NetoolHunterApp
import com.netoolhunter.app.data.ToolsCatalog
import com.netoolhunter.app.domain.Category
import com.netoolhunter.app.domain.Tool
import com.netoolhunter.app.shell.InstallForegroundService
import com.netoolhunter.app.shell.KaliCommand
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ToolsUiState(
    val tools: List<Tool> = ToolsCatalog.ALL,
    val query: String = "",
    val selectedCategories: Set<Category> = emptySet(),
    val installedIds: Set<String> = emptySet(),
    val scanning: Boolean = false
) {
    val visibleTools: List<Tool>
        get() {
            val q = query.trim().lowercase()
            return tools.asSequence()
                .filter { tool ->
                    selectedCategories.isEmpty() || tool.category in selectedCategories
                }
                .filter { tool ->
                    if (q.isEmpty()) true else
                        tool.name.lowercase().contains(q) ||
                            tool.description.lowercase().contains(q) ||
                            tool.tags.any { it.lowercase().contains(q) }
                }
                .toList()
        }
}

class ToolsViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as NetoolHunterApp
    private val _state = MutableStateFlow(ToolsUiState())
    val state: StateFlow<ToolsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            app.installed.installedIds.collect { ids ->
                _state.update { it.copy(installedIds = ids) }
            }
        }
    }

    fun setInitialCategory(categoryId: String?) {
        if (categoryId != null) {
            runCatching { Category.valueOf(categoryId) }.getOrNull()?.let { cat ->
                _state.update { it.copy(selectedCategories = setOf(cat)) }
            }
        }
    }

    fun onQueryChange(query: String) = _state.update { it.copy(query = query) }

    fun toggleCategory(category: Category) = _state.update { current ->
        current.copy(
            selectedCategories = if (category in current.selectedCategories) {
                current.selectedCategories - category
            } else {
                current.selectedCategories + category
            }
        )
    }

    fun clearFilters() = _state.update { it.copy(selectedCategories = emptySet(), query = "") }

    fun install(tool: Tool) {
        val cmd = KaliCommand.install(tool.installer)
        InstallForegroundService.start(getApplication(), cmd, tool.name)
    }

    fun uninstall(tool: Tool) {
        val cmd = KaliCommand.uninstall(tool)
        InstallForegroundService.start(getApplication(), cmd, tool.name)
        // Optimistic mark; a re-scan from InstalledScreen will reconcile.
        app.installed.markInstalled(tool.id, installed = false)
    }

    fun rescan() {
        _state.update { it.copy(scanning = true) }
        viewModelScope.launch {
            withContext(Dispatchers.IO) { app.installed.scan() }
            _state.update { it.copy(scanning = false) }
        }
    }
}

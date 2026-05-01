package com.netoolhunter.app.ui.screens.installed

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.netoolhunter.app.NetoolHunterApp
import com.netoolhunter.app.data.ToolsCatalog
import com.netoolhunter.app.domain.Tool
import com.netoolhunter.app.shell.InstallForegroundService
import com.netoolhunter.app.shell.KaliCommand
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InstalledUiState(
    val installedTools: List<Tool> = emptyList(),
    val scanning: Boolean = true,
    val pendingUninstall: Tool? = null
)

class InstalledViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as NetoolHunterApp
    private val _state = MutableStateFlow(InstalledUiState())
    val state: StateFlow<InstalledUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            app.installed.installedIds.collect { ids ->
                _state.update {
                    it.copy(installedTools = ToolsCatalog.ALL.filter { tool -> tool.id in ids })
                }
            }
        }
        rescan()
    }

    fun rescan() {
        _state.update { it.copy(scanning = true) }
        viewModelScope.launch {
            app.installed.scan()
            _state.update { it.copy(scanning = false) }
        }
    }

    fun requestUninstall(tool: Tool) = _state.update { it.copy(pendingUninstall = tool) }
    fun cancelUninstall() = _state.update { it.copy(pendingUninstall = null) }

    fun confirmUninstall() {
        val tool = _state.value.pendingUninstall ?: return
        val cmd = KaliCommand.uninstall(tool)
        InstallForegroundService.start(getApplication(), cmd, tool.name)
        app.installed.markInstalled(tool.id, installed = false)
        _state.update { it.copy(pendingUninstall = null) }
    }
}

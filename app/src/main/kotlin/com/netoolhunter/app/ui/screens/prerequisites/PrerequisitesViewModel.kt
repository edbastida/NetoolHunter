package com.netoolhunter.app.ui.screens.prerequisites

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.datastore.preferences.core.edit
import com.netoolhunter.app.NetoolHunterApp
import com.netoolhunter.app.data.PrerequisiteStatus
import com.netoolhunter.app.domain.RootManager
import com.netoolhunter.app.shell.InstallForegroundService
import com.netoolhunter.app.shell.TerminalBus
import com.netoolhunter.app.util.DataStoreKeys
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PrereqsUiState(
    val status: PrerequisiteStatus? = null,
    val rootManager: RootManager = RootManager.Unknown,
    val checking: Boolean = true,
    val showInstallConfirm: Boolean = false,
    val installing: Boolean = false,
    val completed: Boolean = false
)

class PrerequisitesViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as NetoolHunterApp
    private val _state = MutableStateFlow(PrereqsUiState())
    val state: StateFlow<PrereqsUiState> = _state.asStateFlow()

    init {
        _state.update { it.copy(rootManager = app.rootManagerDetector.detect()) }
        recheck()
    }

    fun recheck() {
        _state.update { it.copy(checking = true) }
        viewModelScope.launch {
            val status = app.prereqs.check()
            _state.update { it.copy(status = status, checking = false) }
        }
    }

    fun showInstallConfirm() = _state.update { it.copy(showInstallConfirm = true) }
    fun hideInstallConfirm() = _state.update { it.copy(showInstallConfirm = false) }

    fun installBasePackages() {
        _state.update { it.copy(showInstallConfirm = false, installing = true) }
        TerminalBus.reset()
        InstallForegroundService.start(
            getApplication(),
            app.prereqs.installBasePackagesCommand(),
            "Prerequisitos"
        )
        viewModelScope.launch {
            // Wait for the active command to leave RUNNING (i.e. DONE or FAILED).
            TerminalBus.activeCommand.first { active ->
                active != null && active.status != TerminalBus.Status.RUNNING
            }
            _state.update { it.copy(installing = false) }
            recheck()
        }
    }

    fun markCompleted() {
        viewModelScope.launch {
            app.dataStore.edit { prefs ->
                prefs[DataStoreKeys.PREREQS_COMPLETED] = true
            }
            _state.update { it.copy(completed = true) }
        }
    }
}

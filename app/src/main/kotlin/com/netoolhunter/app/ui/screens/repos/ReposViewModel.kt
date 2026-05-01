package com.netoolhunter.app.ui.screens.repos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.netoolhunter.app.NetoolHunterApp
import com.netoolhunter.app.domain.Repo
import com.netoolhunter.app.shell.TerminalBus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReposUiState(
    val repos: List<Repo> = emptyList(),
    val showAddDialog: Boolean = false,
    val showApplyConfirm: Boolean = false,
    val applyInFlight: Boolean = false
)

class ReposViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as NetoolHunterApp
    private val _state = MutableStateFlow(ReposUiState())
    val state: StateFlow<ReposUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            app.repos.repos.collect { list ->
                _state.update { it.copy(repos = list) }
            }
        }
    }

    fun toggle(repo: Repo) {
        viewModelScope.launch { app.repos.setEnabled(repo.id, !repo.enabled) }
    }

    fun showAddDialog() = _state.update { it.copy(showAddDialog = true) }
    fun hideAddDialog() = _state.update { it.copy(showAddDialog = false) }

    fun addCustom(name: String, sourceLine: String) {
        viewModelScope.launch {
            app.repos.addCustom(name, sourceLine)
            _state.update { it.copy(showAddDialog = false) }
        }
    }

    fun deleteCustom(repo: Repo) {
        viewModelScope.launch { app.repos.removeCustom(repo.id) }
    }

    fun showApplyConfirm() = _state.update { it.copy(showApplyConfirm = true) }
    fun hideApplyConfirm() = _state.update { it.copy(showApplyConfirm = false) }

    fun applyChanges() {
        _state.update { it.copy(showApplyConfirm = false, applyInFlight = true) }
        viewModelScope.launch {
            app.repos.applyAndStream().collect { event ->
                TerminalBus.emit(event)
            }
            _state.update { it.copy(applyInFlight = false) }
        }
    }
}

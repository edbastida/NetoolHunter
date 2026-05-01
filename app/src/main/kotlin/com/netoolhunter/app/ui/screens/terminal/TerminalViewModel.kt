package com.netoolhunter.app.ui.screens.terminal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.netoolhunter.app.shell.TerminalBus
import com.netoolhunter.app.ui.components.TerminalLine
import com.netoolhunter.app.ui.components.toLine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TerminalUiState(
    val lines: List<TerminalLine> = emptyList(),
    val activeCommand: TerminalBus.ActiveCommand? = null
)

class TerminalViewModel : ViewModel() {
    private val _state = MutableStateFlow(TerminalUiState())
    val state: StateFlow<TerminalUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            TerminalBus.events.collect { event ->
                _state.update { it.copy(lines = it.lines + event.toLine()) }
            }
        }
        viewModelScope.launch {
            TerminalBus.activeCommand.collect { active ->
                _state.update { it.copy(activeCommand = active) }
            }
        }
    }

    /** Clears local UI state. Does not touch the global TerminalBus. */
    fun clear() = _state.update { TerminalUiState() }
}

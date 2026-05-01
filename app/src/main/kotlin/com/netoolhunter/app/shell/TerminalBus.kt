package com.netoolhunter.app.shell

import com.netoolhunter.app.domain.InstallEvent
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Single global bus for shell output. Survives screen changes and lets any
 * screen kick off an install. The 1024 buffer + DROP_OLDEST means a slow
 * collector won't suspend the producer and freeze the install.
 */
object TerminalBus {
    private val _events = MutableSharedFlow<InstallEvent>(
        replay = 0,
        extraBufferCapacity = 1024,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<InstallEvent> = _events.asSharedFlow()

    private val _activeCommand = MutableStateFlow<ActiveCommand?>(null)
    val activeCommand: StateFlow<ActiveCommand?> = _activeCommand.asStateFlow()

    suspend fun emit(event: InstallEvent) {
        _events.emit(event)
        when (event) {
            is InstallEvent.Started -> _activeCommand.value = ActiveCommand(event.command, Status.RUNNING)
            is InstallEvent.Exit -> _activeCommand.value = _activeCommand.value?.copy(
                status = if (event.code == 0) Status.DONE else Status.FAILED
            )
            is InstallEvent.Error -> _activeCommand.value = _activeCommand.value?.copy(status = Status.FAILED)
            else -> Unit
        }
    }

    fun reset() {
        _activeCommand.value = null
    }

    enum class Status { RUNNING, DONE, FAILED }
    data class ActiveCommand(val command: String, val status: Status)
}

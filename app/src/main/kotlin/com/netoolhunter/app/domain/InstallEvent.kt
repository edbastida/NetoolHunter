package com.netoolhunter.app.domain

sealed class InstallEvent {
    data class Started(val command: String) : InstallEvent()
    data class Stdout(val line: String) : InstallEvent()
    data class Stderr(val line: String) : InstallEvent()
    data class Exit(val code: Int) : InstallEvent()
    data class Error(val message: String) : InstallEvent()
}

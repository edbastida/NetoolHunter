package com.netoolhunter.app.domain

sealed class Installer {
    abstract val kindLabel: String

    data class Apt(val pkg: String) : Installer() {
        override val kindLabel = "apt"
    }

    data class Go(val module: String) : Installer() {
        override val kindLabel = "go"
    }

    data class Pip(val pkg: String) : Installer() {
        override val kindLabel = "pip"
    }

    data class Pipx(val source: String) : Installer() {
        override val kindLabel = "pipx"
    }

    data class Git(
        val repo: String,
        val cloneTo: String? = null,
        val postInstall: List<String> = emptyList()
    ) : Installer() {
        override val kindLabel = "git"
    }

    data class Docker(val image: String, val runArgs: String = "") : Installer() {
        override val kindLabel = "docker"
    }

    data class Script(val curlUrl: String) : Installer() {
        override val kindLabel = "script"
    }
}

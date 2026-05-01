package com.netoolhunter.app.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class Installer {
    abstract val kindLabel: String

    @Serializable
    @SerialName("apt")
    data class Apt(val pkg: String) : Installer() {
        override val kindLabel = "apt"
    }

    @Serializable
    @SerialName("go")
    data class Go(val module: String) : Installer() {
        override val kindLabel = "go"
    }

    @Serializable
    @SerialName("pip")
    data class Pip(val pkg: String) : Installer() {
        override val kindLabel = "pip"
    }

    @Serializable
    @SerialName("pipx")
    data class Pipx(val source: String) : Installer() {
        override val kindLabel = "pipx"
    }

    @Serializable
    @SerialName("git")
    data class Git(
        val repo: String,
        val cloneTo: String? = null,
        val postInstall: List<String> = emptyList()
    ) : Installer() {
        override val kindLabel = "git"
    }

    @Serializable
    @SerialName("docker")
    data class Docker(val image: String, val runArgs: String = "") : Installer() {
        override val kindLabel = "docker"
    }

    @Serializable
    @SerialName("script")
    data class Script(val curlUrl: String) : Installer() {
        override val kindLabel = "script"
    }
}

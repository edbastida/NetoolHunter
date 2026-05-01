package com.netoolhunter.app.domain

import kotlinx.serialization.Serializable

@Serializable
data class Repo(
    val id: String,
    val name: String,
    val sourceLine: String,
    val enabled: Boolean,
    val isCustom: Boolean = false
)

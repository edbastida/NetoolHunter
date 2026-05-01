package com.netoolhunter.app.domain

data class Tool(
    val id: String,
    val name: String,
    val description: String,
    val category: Category,
    val emoji: String,
    val installer: Installer,
    val tags: List<String> = emptyList()
)

package com.netoolhunter.app.domain

import androidx.compose.ui.graphics.Color
import com.netoolhunter.app.ui.theme.KaliBlue

enum class Category(
    val emoji: String,
    val label: String,
    val accent: Color
) {
    RECON("🔍", "Reconocimiento", KaliBlue),
    OSINT("🕵️", "OSINT", Color(0xFF9C27B0)),
    WEB("🌐", "Web", Color(0xFF4CAF50)),
    WIRELESS("📡", "Wireless", Color(0xFFFF9800)),
    EXPLOITATION("💣", "Explotación", Color(0xFFF44336)),
    AD("🖥️", "Red / AD", Color(0xFF00BCD4)),
    PASSWORDS("🔑", "Contraseñas", Color(0xFFFFEB3B)),
    ANDROID("📱", "Android / Reversing", Color(0xFF8BC34A)),
    FORENSICS("🔬", "Forense", Color(0xFF607D8B)),
    WORDLISTS("📦", "Wordlists / Payloads", Color(0xFF795548))
}

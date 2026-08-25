package com.example.ui.theme

import androidx.compose.ui.graphics.Color

enum class AccentTheme(
    val title: String,
    val primary: Color,
    val primaryMuted: Color,
    val hexCode: String
) {
    WHITE("Minimalist White", Color(0xFFFFFFFF), Color(0x33FFFFFF), "#FFFFFF"),
    PLATINUM("Platinum Silver", Color(0xFFE4E4E7), Color(0x33E4E4E7), "#E4E4E7"),
    WARM_WHITE("Warm White", Color(0xFFFAFAF9), Color(0x33FAFAF9), "#FAFAF9"),
    ICE_WHITE("Ice White", Color(0xFFF0F9FF), Color(0x33F0F9FF), "#F0F9FF");

    companion object {
        fun fromName(name: String?): AccentTheme {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: WHITE
        }
    }
}


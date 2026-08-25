package com.example.ui.theme

import androidx.compose.ui.graphics.Color

enum class AccentTheme(
    val title: String,
    val primary: Color,
    val primaryMuted: Color,
    val hexCode: String
) {
    CYAN("Neon Cyan", Color(0xFF00E5FF), Color(0x3300E5FF), "#00E5FF"),
    ELECTRIC_BLUE("Electric Blue", Color(0xFF3B82F6), Color(0x333B82F6), "#3B82F6"),
    PURPLE("Cosmic Purple", Color(0xFFA855F7), Color(0x33A855F7), "#A855F7"),
    VIOLET("Deep Violet", Color(0xFF8B5CF6), Color(0x338B5CF6), "#8B5CF6"),
    EMERALD("Emerald Green", Color(0xFF10B981), Color(0x3310B981), "#10B981"),
    NEON_GREEN("Neon Lime", Color(0xFF22C55E), Color(0x3322C55E), "#22C55E"),
    AMBER("Golden Amber", Color(0xFFFFB020), Color(0x33FFB020), "#FFB020"),
    ORANGE("Solar Orange", Color(0xFFFF7A00), Color(0x33FF7A00), "#FF7A00"),
    ROSE("Crimson Rose", Color(0xFFF43F5E), Color(0x33F43F5E), "#F43F5E"),
    MAGENTA("Neon Magenta", Color(0xFFEC4899), Color(0x33EC4899), "#EC4899"),
    MINT("Fresh Mint", Color(0xFF2DD4BF), Color(0x332DD4BF), "#2DD4BF"),
    WHITE("Minimalist White", Color(0xFFF8FAFC), Color(0x33F8FAFC), "#FFFFFF");

    companion object {
        fun fromName(name: String?): AccentTheme {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: CYAN
        }
    }
}

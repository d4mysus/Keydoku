package com.keydoku.game

data class GameSettings(
    val mistakeChecking: Boolean = true,
    val highlightRelated: Boolean = true,
    val highlightMatchingDigits: Boolean = true,
    val haptics: Boolean = false,
    val sound: Boolean = false,
    val appearance: Appearance = Appearance.SYSTEM,
)

enum class Appearance(val displayName: String) {
    SYSTEM("System"), LIGHT("Light"), DARK("Dark"),
}

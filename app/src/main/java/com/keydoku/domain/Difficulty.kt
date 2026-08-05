package com.keydoku.domain

enum class Difficulty(val displayName: String, val targetGivens: Int) {
    EASY("Easy", 40),
    MEDIUM("Medium", 34),
    HARD("Hard", 28),
    EXPERT("Expert", 24);

    companion object {
        fun fromName(name: String): Difficulty =
            entries.firstOrNull { it.name == name } ?: EASY
    }
}

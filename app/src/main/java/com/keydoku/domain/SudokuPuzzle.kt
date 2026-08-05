package com.keydoku.domain

data class SudokuPuzzle(
    val puzzle: SudokuBoard,
    val solution: SudokuBoard,
    val difficulty: Difficulty,
)

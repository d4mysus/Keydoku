# Sudoku Engine

The engine is pure Kotlin and lives in `com.keydoku.domain`.

## Board Format

`SudokuBoard` stores 81 integers:

- `0` means empty.
- `1`-`9` are filled values.
- Index is `row * 9 + column`.

Serialized boards are 81-character strings using digits. Empty cells serialize as `0`; parsing also accepts `.`.

## Solving

`SudokuSolver` uses backtracking with a minimum-remaining-values cell choice. That keeps solving fast enough for generation and tests.

Important APIs:

- `solve(board)`: returns one solution or `null`.
- `countSolutions(board, limit)`: counts up to `limit`, usually `2` for uniqueness.
- `candidates(board, index)`: returns legal values for one empty cell.

## Generation

`SudokuGenerator`:

1. Creates a fully solved board by solving an empty board with randomized candidate order.
2. Shuffles all cell indexes.
3. Removes clues while the puzzle still has exactly one solution.
4. Stops when it reaches the difficulty target or exhausts the candidate removals.

Difficulty currently uses clue targets:

- Easy: 40 givens
- Medium: 34 givens
- Hard: 28 givens
- Expert: 24 givens

Targets are not guarantees: a puzzle may retain more clues to preserve a unique
solution. Difficulty currently reflects clue targets, not a human-solving rating.
To improve scoring later, add a human-solving rating step after uniqueness validation.

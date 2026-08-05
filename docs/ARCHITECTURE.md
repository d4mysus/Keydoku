# Architecture

Keydoku uses a small layered structure so the Sudoku rules stay easy to understand and test.

## Layers

`domain`

Pure Kotlin Sudoku logic. It has no Android imports and can be unit tested quickly.

- `SudokuBoard`: immutable 81-cell board.
- `SudokuValidator`: row, column, box, and conflict checks.
- `SudokuSolver`: backtracking solver and solution counter.
- `SudokuGenerator`: solved-board creation and clue removal with unique-solution checks.
- `Difficulty`: target clue counts.

`game`

Game session rules.

- `GameState`: current puzzle, notes, selection, settings, timer value, completion state.
- `GameController`: actions such as digit entry, notes, hints, undo, redo, pause, and selection.
- `GameStats`: per-difficulty started/completed counts and time stats.

`data`

Android persistence using `SharedPreferences`. It serializes the active game, settings, and stats as JSON. This is intentionally simple because the app stores one active game and a small stats object.

`ui`

Android Views.

- `MainActivity`: app shell, dialogs, keypad handling, timer, persistence.
- `SudokuBoardView`: custom drawn board with touch selection and compact visual states.

## Data Flow

`MainActivity` receives a touch/key/button event, calls `GameController`, then renders the new `GameState` into `SudokuBoardView` and saves it through `GameRepository`.

The board view does not mutate game rules. It only draws state and reports selected cells.

## Extension Rule

When adding a feature, put Sudoku correctness in `domain`, session behavior in `game`, storage in `data`, and only drawing/input code in `ui`.

# Input Model

Keydoku must be playable with touch or physical keys. Touch only selects cells; all game actions are available through physical keys.

## Physical Keys

| Key | Action |
| --- | --- |
| `1`-`9` | Enter a digit in the selected cell, or toggle that note in notes mode |
| `0`, clear, delete | Clear the selected editable cell |
| D-pad up/down/left/right | Move selected cell |
| D-pad center / enter | Focus the board |
| `*` | Toggle notes mode |
| `#` | Fill a hint |
| Volume down | Undo |
| Volume up | Redo |
| Back, menu, soft-left, soft-right | Open the game menu |

The mapping lives in `MainActivity.onKeyUp`. Android's hardware-key guidance recommends handling physical keys through `KeyEvent` callbacks and not depending on soft keyboard events.

## Menu and Difficulty

Open the full-screen game menu with Back, menu, soft-left, or soft-right.

Menu shortcuts:

- `1` Continue
- `2` New Puzzle
- `3` Undo
- `4` Redo
- `5` Stats
- `6` Settings
- `7` Donate
- `0` Controls

Donate shortcuts:

- `1` Bitcoin
- `2` Buy Me a Coffee
- `0` Back

Choose `New Puzzle`, then choose `Easy`, `Medium`, `Hard`, or `Expert`.

When a puzzle is completed, Keydoku opens a completion dialog with `New game`, `Stats`, and `Board` actions.

The main board screen includes a small status strip below the puzzle:

- selected cell, such as `R5 C4`
- current mode, `Entry` or `Notes`
- candidate notes in the selected cell
- key prompts for notes, hints, and the menu

## Touch

`SudokuBoardView.onTouchEvent` maps the touch coordinate to a row and column, then calls `onCellSelected`. The main screen intentionally has no persistent touch buttons because the target device has physical keys.

## Changing Keys

To change a key:

1. Update `MainActivity.onKeyUp`.
2. Update this document.
3. Add or adjust an instrumentation test under `app/src/androidTest`.

Keep all essential actions reachable from the keypad.

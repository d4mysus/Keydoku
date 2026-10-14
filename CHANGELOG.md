# Changelog

## Unreleased

- Updated launcher and website artwork from the supplied SVG icon pack.
- Adaptive icons for Android 8+ and monochrome themed icons for Android 13+.

## 0.1.1

### Fixed

- Undo and redo preserve elapsed time, settings, and completion history.
- Finishing a puzzle again after undo no longer counts it twice in statistics.
- Hints do not modify paused games.
- Timer updates preserve time between actions and before backgrounding.
- Layouts account for system bars and display cutouts.
- Touch selection reports click actions to Android accessibility services.
- Saved progress is excluded from cloud backup and device-to-device transfer.

### Added

- Signed GitHub APK release workflow with checksum generation.
- Obtainium update instructions and F-Droid listing text.
- GPL-3.0-only licensing and bundled third-party notices.

### Installation

Requires Android 6.0 or newer and a physical keypad or keyboard.
Version code: 2. This version uses the permanent key for future releases;
the older 0.1.0 preview uses a different key.

## 0.1.0 — Initial release

- Offline Sudoku with Easy, Medium, Hard, and Expert puzzles.
- Physical keypad navigation and digit entry, with touch cell selection.
- Notes, hints, undo/redo, timer, and local statistics.
- Optional sound, haptic feedback, and developer donations.
- No ads, accounts, analytics, or network permission.

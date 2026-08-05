# Development Guide

## Local Setup

Install:

- JDK 17 or newer. Android Studio's bundled JDK 21 works.
- Android Studio or Android command-line SDK
- Android SDK platform 37
- Android SDK Build Tools 36.0.0
- Gradle 9.4.1, or generate a Gradle wrapper

The project uses Android Gradle Plugin 9.2.0. Kotlin Android support is provided by AGP, so there is no separate Kotlin Android Gradle plugin in this project.

## Common Commands

```sh
./gradlew test
./gradlew connectedAndroidTest
./gradlew assembleDebug
./gradlew assembleRelease
```

## Release APK

Do not commit keystores.

Create a local keystore:

```sh
keytool -genkeypair -v -keystore keydoku-release.jks -alias keydoku -keyalg RSA -keysize 4096 -validity 10000
```

Add signing config locally or through Android Studio's Generate Signed Bundle / APK flow. Keep passwords outside Git, for example in `~/.gradle/gradle.properties`.

## Testing on Dumber Mini

1. Enable developer options and USB debugging.
2. Build `app-debug.apk`.
3. Install with `adb install -r`.
4. Confirm:
   - The board fits on the 640x480 display.
   - A puzzle can be completed with only hardware keys.
   - Touch selection still works.
   - Back toggles pause.
   - `*` toggles notes and `#` gives a hint.

## Adding Features

- New Sudoku rule: add it in `domain` with unit tests.
- New player action: add it in `GameController`, expose it from `MainActivity`, document it in `INPUT_MODEL.md`.
- New persisted field: update `GameRepository` encode/decode with a safe default.
- New board visual state: add drawing to `SudokuBoardView`; keep text readable at 640x480.

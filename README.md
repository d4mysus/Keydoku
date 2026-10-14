# Keydoku

Keydoku is a native Android Sudoku app designed for keypad-first dumbphones, especially the Dumber Mini. The Dumber Mini product page describes an AOSP 14 / LineageOS 21 based phone with a 2.8 inch 640x480 capacitive touchscreen and 21 physical keys, so the app is built around both touch and hardware key input.

## Project Shape

- Kotlin + Android Views, no Compose.
- Single `:app` module.
- Offline only: no network permission, no ads, no accounts.
- Pure Kotlin Sudoku engine under `com.keydoku.domain`.
- Android UI and persistence kept separate from the engine.

## Downloads and updates

Signed APKs will be distributed through [GitHub Releases](https://github.com/d4mysus/Keydoku/releases).
Obtainium can track this repository once the first APK release is published.
F-Droid submission is planned; there is no confirmed listing yet.
See [distribution and signing instructions](docs/DISTRIBUTION.md). A physical
keypad or keyboard is required for entering digits; touch selects cells and menus.

Keydoku is licensed under GPL-3.0-only. Optional donations support development
and do not unlock features.

## Building from source

This workspace did not have Gradle or an Android SDK installed when the project was scaffolded. Install Android Studio or the command-line SDK first, then build with:

```sh
./gradlew test
./gradlew assembleDebug
```

If you do not have a Gradle wrapper yet, open the project in Android Studio and let it sync, or install Gradle 9.4.1 and run:

```sh
gradle wrapper --gradle-version 9.4.1
./gradlew assembleDebug
```

The debug APK will be at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Install on a connected Android device:

```sh
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Documentation

- [Architecture](docs/ARCHITECTURE.md)
- [Input model](docs/INPUT_MODEL.md)
- [Sudoku engine](docs/SUDOKU_ENGINE.md)
- [Development guide](docs/DEVELOPMENT.md)

## Device Target

The first target is the Dumber Mini. Keep all UI changes usable on a 640x480 screen and fully playable without touch.

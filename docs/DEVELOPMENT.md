# Development guide

## Toolchain

Install JDK 21 and the Android command-line SDK or Android Studio.
The repository includes its Gradle wrapper and uses:

| Component | Version |
| --- | --- |
| Gradle | 9.4.1 |
| Android Gradle Plugin | 9.2.0 |
| Android SDK platform | 37.0 (compile and target API 37) |
| Android Build Tools | 36.0.0 |
| Minimum Android version | Android 6.0 / API 23 |

AGP supplies Kotlin Android support; there is no separate Kotlin Android plugin.

Set `JAVA_HOME` to your JDK and `ANDROID_HOME` to your SDK directory.
Install the SDK packages with:

```sh
sdkmanager 'platforms;android-37.0' 'build-tools;36.0.0' 'platform-tools'
```

Alternatively, select the same packages in Android Studio's SDK Manager.
Use `./gradlew` from the repository root; do not regenerate the wrapper.

## Build and test

```sh
./gradlew test
./gradlew lintRelease
./gradlew assembleDebug
```

Install the development build on a device with USB debugging enabled:

```sh
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

The debug and release APKs have the same package name but different signing keys.
Android cannot install one over the other; use a separate test device or uninstall
the existing build first. Uninstalling removes local progress.

Run the Android tests on a connected device or emulator:

```sh
./gradlew connectedDebugAndroidTest
```

Unit tests cover the engine, controller, completion history, and timer.
Android tests cover keypad/menu input and saved-state migration.

## Device acceptance checks

Test the signed release candidate as well as the development build:

- Confirm the board, status strip, and every menu fit the target screen.
- Complete a puzzle using physical keys; verify touch selection and menu taps.
- Check notes (`*`), hints (`#`), clear, and volume-key undo/redo.
- Confirm Back opens the menu and returns through submenus.
- Check Back gestures on modern Android versions.
- Verify undo/redo preserves elapsed time and counts a completed puzzle only once.
- Background the app and reopen it; confirm progress, settings, and statistics restore.
- Verify time spent in menus or away from the app is excluded.
- Start a new puzzle at each difficulty and check the replacement confirmation.
- Test sound, haptic feedback, optional donation links, and clipboard actions.
- If an older APK uses the same release key, test an update without uninstalling.

Automated builds do not replace device acceptance testing.

## Release builds

Follow [distribution and signing](DISTRIBUTION.md). Signing reads the supported
environment variables; passwords in arbitrary local Gradle properties are not
automatically consumed by this project.

An unsigned source build is available with:

```sh
./gradlew test lintRelease assembleRelease
```

Without signing variables, the output is
`app/build/outputs/apk/release/app-release-unsigned.apk`; it is not installable.

## Working on features

- Put Sudoku correctness in `domain`, session behavior in `game`, and persistence in `data`.
- Update [controls](INPUT_MODEL.md) and Android tests when changing key mappings.
- Add backward-compatible defaults when changing saved fields.
- Keep gameplay usable without touch and layouts readable on the small target screen.

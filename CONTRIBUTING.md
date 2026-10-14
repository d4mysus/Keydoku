# Contributing to Keydoku

Keydoku is designed for small Android keypad phones. Keep every essential
game action reachable through physical keys, and avoid adding network services
or tracking to the app.

## Bugs and suggestions

[Open an issue](https://github.com/d4mysus/Keydoku/issues) with your phone model,
Android version, Keydoku version, input method, and steps to reproduce.
For a visual issue, attach a screenshot if possible. Avoid posting personal
information, keystores, passwords, or unrelated logs.

## Development

Follow the [development guide](docs/DEVELOPMENT.md). Use the included wrapper:

```sh
./gradlew test lintRelease assembleDebug
```

Run the relevant Android tests and device acceptance checks for input, layout,
or persistence changes. Describe any checks you could not run in your pull request.

Keep changes focused, add regression coverage for behavior fixes, and update
controls, privacy information, or release documentation when behavior changes.
Contributions are distributed under the project's [GPL-3.0-only license](LICENSE).

# GitHub, Obtainium, and F-Droid

## GitHub releases

Distribute a signed release APK, not a debug APK or an Android App Bundle.
Keep the same signing key for every GitHub release: Android will reject an
update signed by a different key. Back up the key and its passwords securely.
Increment `versionCode` and `versionName` in `app/build.gradle.kts` for every
update; make a matching Git tag such as `v0.1.0`.

The manual GitHub Actions workflow `GitHub APK release` builds an existing tag,
runs unit tests and release lint, verifies the APK signature, and creates a
**draft** GitHub release with `keydoku.apk` and `SHA256SUMS` attached.
Review the draft and test its APK on the target phone before publishing it.
The workflow has not been executed on GitHub yet.

The APK bundles GPLv3 and third-party license notices in `assets/licenses/`.
Keep published source tags available so recipients can obtain the source
corresponding to each APK.

Configure these repository Actions secrets:

- `KEYDOKU_KEYSTORE_BASE64`: the base64-encoded private release keystore.
- `KEYDOKU_STORE_PASSWORD`: its store password.
- `KEYDOKU_KEY_ALIAS`: the signing key alias.
- `KEYDOKU_KEY_PASSWORD`: its key password.

Create your permanent release key locally with `keytool -genkeypair` using an
RSA key, then enter the values through GitHub's secret settings. Never commit
the keystore, passwords, or an encoded copy of the keystore.

For a local signed build, set `KEYDOKU_KEYSTORE` to the keystore's absolute
path, plus the three password/alias variables listed above, then run:

```sh
./gradlew --no-daemon test lintRelease assembleRelease
```

Without signing variables, Gradle intentionally produces an unsigned release
APK, useful for independent source builds. Do not distribute that APK.

## Obtainium

Obtainium can follow the public GitHub repository:

```text
https://github.com/d4mysus/Keydoku
```

Add that URL in Obtainium once a release containing `keydoku.apk` is published.
GitHub release tags provide update versions, and the APK's `versionCode`
provides Android's update ordering. Keep one universal APK per release and
the same signing certificate. Draft releases are not public update sources.

## F-Droid

Keydoku is licensed GPL-3.0-only, has no proprietary runtime SDK, and can be
built from source. This makes a submission plausible, not an accepted listing.
The Apache-2.0 ZXing dependency is compatible with GPLv3.

After a tested release, request packaging through F-Droid's official
submission process. Provide the source repository, release tag, license,
description, screenshots, and reproducible build instructions. F-Droid must
review dependencies, tooling availability, and its current donation policy.
The pinned AGP 9.2.0 / Gradle 9.4.1 / SDK 37 toolchain must be supported by its
builders; support has not been verified here.

Listing text is prepared in `fastlane/metadata/android/en-US/`. Screenshots
are available in `keydoku_production/assets/screenshots/`. No F-Droid build
recipe or listing is claimed to be accepted or tested yet.

F-Droid normally builds and signs apps itself. Its APK may use a different
certificate from the GitHub APK; users should not expect to switch channels
without uninstalling, which deletes this app's local progress. Use one update
channel unless reproducible-build signing arrangements are established.

Google Play enrollment, App Bundles, and Play Billing are not required for
this distribution plan. Optional donations do not unlock game features.

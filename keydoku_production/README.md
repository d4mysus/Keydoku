# Keydoku Production Pack

The current distribution plan is signed APKs on GitHub Releases, with updates
through Obtainium and a possible F-Droid submission. See
[distribution instructions](../docs/DISTRIBUTION.md).

This folder contains website assets, privacy policies, and archived Google
Play preparation materials. Google Play publication is deferred.

Store assets:

- App icon: `assets/play-store-icon-512.png`
- Feature graphic: `assets/feature-graphic-1024x500.jpg`
- Screenshots: `assets/screenshots/`
- Privacy policy page: `privacy/index.html`
- Static website for Cloudflare Pages: `website/`

Build the APK from the project root after configuring signing as documented:

```sh
./gradlew --no-daemon --max-workers=2 test lintRelease assembleRelease
```

The signed APK is at:

```text
app/build/outputs/apk/release/app-release.apk
```

Without signing configuration, Gradle produces `app-release-unsigned.apk`,
which cannot be installed or distributed.

Do not commit release keystores or passwords.

# Keydoku Production Pack

This folder contains the Google Play release checklist, listing copy, privacy policy text, and data-safety answers for Keydoku.

Store assets:

- App icon: `assets/play-store-icon-512.png`
- Feature graphic: `assets/feature-graphic-1024x500.jpg`
- Screenshots: `assets/screenshots/`
- Privacy policy page: `privacy/index.html`
- Static website for Cloudflare Pages: `website/`

Build the Play upload artifact from the project root:

```sh
./gradlew --no-daemon --max-workers=1 bundleRelease
```

The generated local release bundle is at:

```text
app/build/outputs/bundle/release/app-release.aab
```

For Play upload, sign the bundle with your Play upload key first. See `release_signing.md`.

Do not commit release keystores or passwords.

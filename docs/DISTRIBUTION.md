# Distribution and signing

Keydoku ships signed APKs through GitHub Releases. Obtainium follows those releases.
F-Droid submission is planned; Google Play distribution is deferred.

## Release artifacts

Each current-format public release contains:

- `keydoku.apk`: the universal signed APK.
- `SHA256SUMS`: the APK's SHA-256 checksum.
- GitHub's source archives for the corresponding version tag.

Tags use `v<versionName>`, such as `v0.1.1`. Each update must also increment
`versionCode` in `app/build.gradle.kts` so Android recognizes it as newer.

The `prod` tag is the older 0.1.0 release and uses `com.keydoku.apk`.
Version 0.1.1 establishes the permanent signing key for future GitHub releases.

## Signing key

Use the same key for every release. Keep an encrypted backup of the keystore,
its alias, and both passwords. Losing the key prevents normal updates for
existing installations.

The release certificate's SHA-256 fingerprint is:

```text
b4c1a225da706f8a6ff6094f25dd030bffe5c8afcfb1a41c049ba19b285efd88
```

The older `prod` APK uses a different key. An old test installation must be
removed once before installing a current-key APK; that clears its local data.

Configure these GitHub repository Actions secrets:

| Secret | Purpose |
| --- | --- |
| `KEYDOKU_KEYSTORE_BASE64` | Base64-encoded release keystore |
| `KEYDOKU_STORE_PASSWORD` | Keystore password |
| `KEYDOKU_KEY_ALIAS` | Signing-key alias |
| `KEYDOKU_KEY_PASSWORD` | Signing-key password |

These are already configured for the verified 0.1.1 workflow. Preserve them
when preparing later releases. Never commit the keystore, encoded keystore,
or passwords.

For local signing, use `KEYDOKU_KEYSTORE` for the absolute keystore path,
plus the three password/alias variables above. Gradle signs only when all
four values are present and rejects a partial configuration.

## Release procedure

1. Merge the intended changes, increment the version, update [CHANGELOG.md](../CHANGELOG.md),
   and write user-facing notes in `docs/releases/<versionName>.md`.
2. Run unit tests, release lint, and device acceptance checks from the [development guide](DEVELOPMENT.md).
3. Create an annotated tag for the release commit and push that tag.
4. Open [GitHub Actions](https://github.com/d4mysus/Keydoku/actions/workflows/release.yml),
   select **GitHub APK release**, and run it with the matching tag.
5. Review the generated draft, verify its checksum and signing certificate,
   and test the attached APK on the target phone.
6. Review the release notes and publish the draft as the latest stable release.

The workflow uses Ubuntu 24.04's preinstalled Android SDK, runs tests and lint,
builds with the signing secrets, verifies the APK, checks the version against
the tag, and creates a draft with both release assets. It has been exercised
successfully for version 0.1.1.
It uses the version's maintained release-notes file when present and falls back
to GitHub-generated notes otherwise.

The workflow currently expects a new release tag. If a draft for that tag
already exists, do not blindly rerun the complete publication step or overwrite
a public release. Review and handle the existing draft before retrying.

To verify a downloaded release locally:

```sh
sha256sum -c SHA256SUMS
apksigner verify --verbose --print-certs keydoku.apk
```

Keep source tags available. GPLv3 and third-party notices are bundled in
`assets/licenses/` inside the APK.

## Obtainium

Add this source URL in Obtainium:

```text
https://github.com/d4mysus/Keydoku
```

Each public stable release contains one APK. GitHub tags identify releases;
the APK's version code controls Android update ordering. Drafts are not public
update sources. Keep the same signing certificate so updates preserve data.

## F-Droid

The GPL-3.0-only license and open-source runtime dependencies make submission
plausible. F-Droid listing text is in `fastlane/metadata/android/en-US/`, with
screenshots in `keydoku_production/assets/screenshots/`.

A listing still requires F-Droid review and a tested build recipe. Its builders
must support the pinned AGP, Gradle, and SDK versions. Acceptance, toolchain
support, and donation-policy review are not yet confirmed.

F-Droid normally builds and signs packages itself. Its APK may use a different
certificate from the GitHub APK, so users cannot assume they can switch channels
without uninstalling. Keep a single update channel unless compatible signing
has been established.

## Website

The static website is in `keydoku_production/website/`.
See its [hosting instructions](../keydoku_production/website/README.md).

# Release signing

Keydoku's release signing is configured in `app/build.gradle.kts` and the
manual [GitHub release workflow](../.github/workflows/release.yml).

Follow [distribution and signing](../docs/DISTRIBUTION.md) for the key fingerprint,
GitHub secrets, local environment variables, release tags, and artifact checks.
Use the permanent release keystore for every update; do not generate a new key
for each version.

## Local signed APK

Set `KEYDOKU_KEYSTORE` to the keystore's absolute path, and supply
`KEYDOKU_STORE_PASSWORD`, `KEYDOKU_KEY_ALIAS`, and `KEYDOKU_KEY_PASSWORD`
through your local environment.

```sh
./gradlew test lintRelease assembleRelease
```

The signed output is `app/build/outputs/apk/release/app-release.apk`.
With no signing configuration, Gradle produces `app-release-unsigned.apk`;
a partial configuration fails instead of silently producing a distributable APK.

Never commit secrets or put them in release notes. Back up the permanent key
and credentials securely.

Google Play upload signing is a separate future workflow and is not required
for GitHub or Obtainium.

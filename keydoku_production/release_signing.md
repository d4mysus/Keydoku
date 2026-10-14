# Release Signing

For the current GitHub/Obtainium release plan, follow
[distribution and signing instructions](../docs/DISTRIBUTION.md). The Play
upload-key instructions below are retained for a possible future Play release.

Use Play App Signing in Google Play Console.

## Recommended Path

1. In Play Console, create the Keydoku app.
2. Enroll in Play App Signing.
3. Let Google manage the app signing key.
4. Create a local upload key for signing uploads.

```sh
keytool -genkeypair -v \
  -keystore keydoku-upload.jks \
  -alias keydoku-upload \
  -keyalg RSA \
  -keysize 4096 \
  -validity 10000
```

Keep `keydoku-upload.jks` and passwords out of Git.

## Build

Use Android Studio's Generate Signed Bundle flow, or add signing values through local Gradle properties and run:

```sh
./gradlew --no-daemon --max-workers=1 bundleRelease
```

Upload the signed bundle:

```text
app/build/outputs/bundle/release/app-release.aab
```

The checked-in production pack may include an unsigned bundle for build verification. Do not upload an unsigned bundle to Play Console.

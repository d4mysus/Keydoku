# Privacy and distribution review

Reviewed against the current repository on 14 October 2026.

## Verified in source

- No permissions are declared, including no Internet permission.
- No ads, analytics, accounts, or user-generated content.
- Puzzle progress, notes, settings, timer, and statistics are stored locally.
- Cloud backups and device-to-device transfer are explicitly disabled.
- ZXing core generates donation QR codes locally.
- Bitcoin support displays an address and QR code.
- Buy Me a Coffee opens an external browser only when requested.
- Donations do not unlock features, content, or hints.

## Current distribution

Signed APKs are prepared through GitHub Releases; Obtainium can follow public
releases. The signed 0.1.1 pipeline has passed tests, lint, signing, and artifact
verification. Physical-device acceptance testing is a separate release check.

The project is licensed GPL-3.0-only and includes notices for its Apache-2.0
dependencies.

## Review boundaries

This source review is not approval from an app store. F-Droid acceptance,
builder compatibility, and donation-policy review remain pending.
Google Play is deferred, and its current policy requirements have not been
certified here.

Revisit the privacy policy and this review before adding network access,
telemetry, accounts, paid features, or another third-party SDK.

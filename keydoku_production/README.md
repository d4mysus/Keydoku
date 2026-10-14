# Release and website assets

This directory contains Keydoku's public website, privacy policy, listing copy,
and visual assets. Signed APKs are distributed through GitHub Releases, with
Obtainium support and a planned F-Droid submission.

## Contents

| Path | Purpose |
| --- | --- |
| [website/](website/README.md) | Standalone static website |
| [privacy_policy.md](privacy_policy.md) | App privacy policy |
| [privacy/index.html](privacy/index.html) | Standalone privacy page for separate hosting |
| [store_listing.md](store_listing.md) | Public listing copy |
| [data_safety.md](data_safety.md) | Data-handling reference |
| [policy_audit.md](policy_audit.md) | Distribution and privacy review scope |
| [release_signing.md](release_signing.md) | Signing guide |
| `assets/screenshots/` | Gameplay, menu, and donation screenshots |
| `assets/play-store-icon-512.png` | 512-pixel app icon |
| `assets/feature-graphic-1024x500.jpg` | Feature graphic |

The icon's filename is retained for compatibility; it is also usable outside
Google Play. No Google Play listing is currently planned.
[PLAY_STORE_CHECKLIST.md](PLAY_STORE_CHECKLIST.md) records deferred requirements
only.

## Release workflow

Follow [distribution and signing](../docs/DISTRIBUTION.md).
Public releases contain `keydoku.apk` and `SHA256SUMS`; APKs are not stored in Git
or copied into the website.

Use gameplay and menu screenshots for the main listing. Donation screenshots
document optional support options; donations never unlock features.

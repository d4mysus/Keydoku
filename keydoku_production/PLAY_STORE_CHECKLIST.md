# Google Play Release Checklist

## Current Status

- App name: Keydoku
- Package name: `com.keydoku`
- Version: `0.1.0`, code `1`
- Target SDK: `37`
- Permissions: none declared
- Network permission: none
- Ads: none
- Accounts: none
- Data collection by app: none
- Android backup: disabled

## Before Upload

- Create a Google Play Developer account.
- Create the Keydoku app in Play Console.
- Enroll in Play App Signing.
- Build and upload a signed `app-release.aab`.
- Upload `assets/play-store-icon-512.png` as the app icon.
- Upload `assets/feature-graphic-1024x500.jpg` as the feature graphic.
- Replace the placeholder support email in `privacy_policy.md` and `privacy/index.html`.
- Host `privacy/index.html` publicly and paste that URL into Play Console.
- Or host `website/` on Cloudflare Pages and use `/privacy.html` as the privacy policy URL.
- Complete the Data safety form using `data_safety.md`.
- Complete Content rating as a puzzle/game app with no violence, gambling, user content, or location.
- Set target audience to adults/general audience, not designed for children.
- Use screenshots from `assets/screenshots`.
- Prefer gameplay/menu screenshots for the public listing; keep donation QR screenshots for review/reference unless you intentionally want donations shown on the listing page.

## Policy Notes

- Donate is allowed only as creator support. It must not unlock features, content, badges, extra hints, ad removal, or subscriptions.
- If donations ever unlock anything digital, remove Bitcoin/Buy Me a Coffee or implement Google Play Billing.
- Keep the app free, offline, and ad-free unless this checklist is reviewed again.

## Release Command

```sh
./gradlew --no-daemon --max-workers=1 test bundleRelease
```

The local unsigned bundle in `release/app-release.aab` proves the release build works. It is not the final Play upload artifact until signed with your upload key.

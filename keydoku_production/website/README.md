# Keydoku website

A standalone static site with no build step, tracking scripts, or external
font dependencies.

## Hosting

Publish `keydoku_production/website` as the site root on Cloudflare Pages,
GitHub Pages, or another static host. No APK needs to be copied into the site.

- Home page: `index.html`
- Privacy policy: `privacy.html`
- Shared styles: `styles.css`
- Images and favicon: `assets/`

The download link opens GitHub Releases so it works across release filenames
and does not point to an unpublished draft asset. Obtainium uses the repository
URL rather than this website.

## Local preview

From the repository root:

```sh
python3 -m http.server 8000 --directory keydoku_production/website
```

Check the home page and privacy page at narrow and wide viewport sizes.
Preserve the physical-keypad requirement and keep the public privacy copy
consistent with `../privacy_policy.md`.

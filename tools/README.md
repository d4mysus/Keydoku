# Artwork sources

`art/icon.svg` is the supplied launcher and website icon master. Keep the
Android vector layers in `app/src/main/res/drawable/` aligned with it.

Android 8+ uses adaptive launcher resources from `mipmap-anydpi-v26`;
Android 13+ supporting launchers use the included monochrome layer for themed
icons; older Android versions ignore that layer.
Older Android versions use the generated density-specific PNGs.
Adaptive foreground layers include centered padding to compensate for Android's
overscan and keep the grid and digits visible under launcher masks.

## Render legacy icons and PNG fallbacks

With Node.js and Playwright/Chromium available:

```sh
node tools/render_launcher_icons.cjs
```

If using an existing Chromium executable, set `ICON_CHROMIUM_PATH` to its path.
The script renders the SVG unchanged at the five Android launcher sizes and
512px for the production icon and website PNG fallback. Legacy round icons
use a circular background variant with the same foreground artwork. This is an optional
artwork-maintenance tool; Android builds do not require Node.js or Playwright.

The standalone website uses the same SVG and PNG fallback. Synchronize those
assets in `d4mysus/Keydoku_Website` after changing the master.

`generate_feature_graphic.swift` is a macOS/AppKit helper for the existing
feature graphic. It is separate from the launcher artwork.

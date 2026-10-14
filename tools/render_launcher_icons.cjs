// Render the SVG master without changing its artwork. Requires Playwright/Chromium.
const fs = require('node:fs/promises');
const path = require('node:path');
const { chromium } = require('playwright');

async function main() {
  const root = path.resolve(__dirname, '..');
  const svg = await fs.readFile(path.join(root, 'art/icon.svg'), 'utf8');
  const roundSvg = svg.replace('<rect x="0" y="0" width="108" height="108" rx="24" fill="url(#bg)"/>', '<circle cx="54" cy="54" r="54" fill="url(#bg)"/>');
  if (roundSvg === svg) throw new Error('SVG background changed; review the round-icon renderer.');
  const options = { headless: true };
  if (process.env.ICON_CHROMIUM_PATH) options.executablePath = process.env.ICON_CHROMIUM_PATH;
  const browser = await chromium.launch(options);
  try {
    const page = await browser.newPage({ deviceScaleFactor: 1 });
    await page.route('**/*', route => route.abort());
    for (const [density, size] of [['mdpi', 48], ['hdpi', 72], ['xhdpi', 96], ['xxhdpi', 144], ['xxxhdpi', 192], ['master', 512]]) {
      await page.setViewportSize({ width: size, height: size });
      const render = async artwork => {
        await page.setContent(`<html><head><style>html,body{margin:0;background:transparent}svg{display:block;width:${size}px;height:${size}px}</style></head><body>${artwork}</body></html>`);
        return page.locator('svg').screenshot({ omitBackground: true });
      };
      const png = await render(svg);
      const roundPng = density === 'master' ? null : await render(roundSvg);
      const outputs = density === 'master'
        ? ['keydoku_production/assets/play-store-icon-512.png', 'keydoku_production/website/assets/keydoku-icon.png']
        : [`app/src/main/res/mipmap-${density}/ic_launcher.png`, `app/src/main/res/mipmap-${density}/ic_launcher_round.png`];
      for (const output of outputs) {
        await fs.mkdir(path.dirname(path.join(root, output)), { recursive: true });
        await fs.writeFile(path.join(root, output), output.endsWith('ic_launcher_round.png') ? roundPng : png);
        console.log(output);
      }
    }
  } finally {
    await browser.close();
  }
}

main().catch(error => { console.error(error.message); process.exitCode = 1; });

import puppeteer from 'puppeteer';
import { readFileSync, writeFileSync, existsSync, unlinkSync } from 'fs';
import { fileURLToPath } from 'url';
import path from 'path';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const config = JSON.parse(readFileSync(path.join(__dirname, 'config.json'), 'utf8'));

const missing = config.devices
  .map(d => d.screenshot)
  .filter(s => !existsSync(path.join(__dirname, 'ui-screenshots', s)));

if (missing.length > 0) {
  console.error('Missing screenshots:\n  ' + missing.join('\n  '));
  process.exit(1);
}

function positionCss(pos) {
  return Object.entries(pos).map(([k, v]) => `${k}: ${v}px`).join('; ');
}

function renderMacbook(device) {
  const frameClass = device.theme === 'light' ? ' light-frame' : '';
  return `
  <div id="${device.id}" class="macbook" style="${positionCss(device.position)}; transform: rotate(${device.rotate}deg); z-index: ${device.zIndex};">
    <div class="macbook-screen${frameClass}" style="width: ${device.screenWidth}px;">
      <div class="macbook-titlebar">
        <span class="tb-red"></span><span class="tb-yellow"></span><span class="tb-green"></span>
      </div>
      <img src="ui-screenshots/${device.screenshot}">
    </div>
    <div class="macbook-base"></div>
    <div class="macbook-hinge"></div>
  </div>`;
}

function renderIphone(device) {
  return `
  <div id="${device.id}" class="iphone" style="${positionCss(device.position)}; transform: rotate(${device.rotate}deg); z-index: ${device.zIndex};">
    <div class="iphone-body ${device.theme}" style="width: ${device.bodyWidth}px;">
      <img src="ui-screenshots/${device.screenshot}">
    </div>
  </div>`;
}

const dots = [
  { cls: 'purple', size: 10, top: 25, left: 200 },
  { cls: 'blue', size: 8, top: 12, left: 520 },
  { cls: 'purple', size: 11, top: 55, right: 280 },
  { cls: 'blue', size: 7, top: 660, left: 90 },
  { cls: 'purple', size: 9, top: 640, left: 550 },
  { cls: 'blue', size: 11, top: 380, right: 10 },
  { cls: 'purple', size: 6, top: 685, left: 900 },
  { cls: 'blue', size: 9, top: 680, right: 130 },
  { cls: 'purple', size: 8, top: 320, left: 8 },
  { cls: 'blue', size: 7, top: 18, right: 40 },
  { cls: 'purple', size: 7, top: 500, left: 700 },
  { cls: 'blue', size: 8, top: 150, left: 380 },
];

const dotsHtml = dots.map(d => {
  const pos = d.left !== undefined ? `left:${d.left}px` : `right:${d.right}px`;
  return `  <div class="dot dot-${d.cls}" style="width:${d.size}px;height:${d.size}px;top:${d.top}px;${pos};"></div>`;
}).join('\n');

const devicesHtml = config.devices
  .map(d => d.type === 'macbook' ? renderMacbook(d) : renderIphone(d))
  .join('\n');

const html = `<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<style>
  * { margin: 0; padding: 0; box-sizing: border-box; }

  body {
    width: ${config.canvas.width}px;
    height: ${config.canvas.height}px;
    background: #fafafc;
    position: relative;
    overflow: hidden;
    font-family: -apple-system, BlinkMacSystemFont, sans-serif;
  }

  .dot {
    position: absolute;
    border-radius: 50%;
    pointer-events: none;
  }
  .dot-purple { background: rgba(124, 58, 237, 0.45); }
  .dot-blue { background: rgba(59, 130, 246, 0.5); }

  .macbook {
    position: absolute;
    filter: drop-shadow(0 16px 32px rgba(0,0,0,0.14));
  }
  .macbook-screen {
    border-radius: 10px 10px 0 0;
    overflow: hidden;
    border: 3px solid #2a2a2e;
    background: #1a1625;
  }
  .macbook-screen.light-frame {
    border-color: #c0bcc8;
    background: #f5f5f7;
  }
  .macbook-titlebar {
    height: 26px;
    background: #1e1e22;
    display: flex;
    align-items: center;
    padding-left: 11px;
    gap: 7px;
  }
  .macbook-screen.light-frame .macbook-titlebar {
    background: #e8e6ec;
  }
  .macbook-titlebar span {
    width: 9px; height: 9px;
    border-radius: 50%;
    display: inline-block;
  }
  .tb-red { background: #ff5f57; }
  .tb-yellow { background: #febc2e; }
  .tb-green { background: #28c840; }
  .macbook-screen img { width: 100%; display: block; }
  .macbook-base {
    height: 11px;
    background: linear-gradient(to bottom, #c8c8cd, #b0b0b5);
    border-radius: 0 0 5px 5px;
    margin: 0 -5px;
  }
  .macbook-hinge {
    height: 4px;
    background: linear-gradient(to bottom, #d8d8dc, #c0c0c5);
    margin: 0 20px;
    border-radius: 0 0 2px 2px;
  }

  .iphone {
    position: absolute;
    filter: drop-shadow(0 12px 28px rgba(0,0,0,0.18));
  }
  .iphone-body {
    border-radius: 24px;
    overflow: hidden;
    padding: 5px;
  }
  .iphone-body.dark { background: #2a2a2e; }
  .iphone-body.light { background: #d0ccd8; }
  .iphone-body img {
    width: 100%;
    display: block;
    border-radius: 19px;
  }
</style>
</head>
<body>
${dotsHtml}
${devicesHtml}
</body>
</html>`;

const { width, height } = config.canvas;
const tmpHtml = path.join(__dirname, '_hero.html');
writeFileSync(tmpHtml, html);

const browser = await puppeteer.launch({ args: ['--no-sandbox'] });
const page = await browser.newPage();
await page.setViewport({ width, height, deviceScaleFactor: 2 });
await page.goto(`file://${tmpHtml}`, { waitUntil: 'networkidle0' });
await page.screenshot({
  path: path.join(__dirname, 'hero.webp'),
  type: 'webp',
  quality: 90,
  clip: { x: 0, y: 0, width, height }
});
await browser.close();
unlinkSync(tmpHtml);
console.log('Done: hero.webp');

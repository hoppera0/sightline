# Sightline

A drawing and reference aid. Open a picture and it draws the analysis over the top:
composition guides, perspective and vanishing points, figure pose, and a value /
block-in breakdown for painting from it.

**Use it:** https://hoppera0.github.io/sightline/

On Android, Chrome's menu offers **Install app** / **Add to Home screen**. It then opens
full screen with no browser bar.

## How it works

One self-contained HTML page. Everything is inlined — the app, TensorFlow.js and the
MoveNet pose weights — so after the first load it runs with no network at all.

**Your pictures never leave your device.** There is no upload, no API call and no
analytics. Images are read locally in the browser and the pose model runs on-device.
The service worker only ever caches this page's own files back from this same origin.

## Why this repo is public

GitHub Pages needs a public repository on the free plan, and this is a personal drawing
tool with nothing private in it. If that ever stops being true, make it private — Pages
then needs a paid plan, or the page moves elsewhere.

## Contents

This repository holds the built site only. `index.html` is generated; it is not the
place to edit anything.

| File | What |
|---|---|
| `index.html` | The built app, single file |
| `manifest.json` | Web app manifest — name, icons, standalone display |
| `sw.js` | Service worker. Cache-first over a fixed file list, versioned by build hash |
| `icon-*.png` | App icons, including a maskable one for Android's adaptive shape |
| `pwa-check.html` | A small health page that reports what the browser has registered |

## Credits and licences

- [TensorFlow.js](https://github.com/tensorflow/tfjs) — Apache License 2.0
- [MoveNet SinglePose Lightning](https://www.kaggle.com/models/google/movenet) — Apache License 2.0

Both are bundled inside `index.html`. Their licence text ships with them in that file.

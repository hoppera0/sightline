# Sightline

A drawing and reference aid. Open a picture and it draws the analysis over the top:
composition guides, perspective and vanishing points, figure pose, a Loomis head
construction fitted to every face it finds, and a value / block-in breakdown for
painting from it.

**Use it:** https://hoppera0.github.io/sightline/

On Android, Chrome's menu offers **Install app** / **Add to Home screen**. It then opens
full screen with no browser bar.

### Studio: more phone apps

[`apps/`](apps/) is a folder of small creative apps for the phone, in the same spirit: one
file each, nothing uploaded, all working offline once visited. Open
https://hoppera0.github.io/sightline/apps/ and add **Studio** to the home screen.

- **Glow**: soft gradient wallpapers and posters, saved at full phone resolution.
- **Loop**: photos (or just a gradient) into a short moving video, recorded on the phone.

[HOME.md](HOME.md) covers running all of it from a home machine over Tailscale.

### Android app

There is also a real Android app (Android 10 or later): open the
[latest release](https://github.com/hoppera0/sightline/releases/latest) on your phone,
download **Sightline.apk** and open it. Android asks once to allow installs from your
browser. Each new build installs over the last one.

It is the same page as the website, packaged with no internet permission at all. Open,
Save PNG (to Downloads) and sharing a photo to Sightline from the gallery all work. The
APK is built by GitHub Actions from `android/` and `index.html` whenever either changes on
`main`. The signing key in `android/` is committed on purpose so updates install cleanly;
it is fine for sideloading, not for the Play Store.

## How it works

One self-contained HTML page. Everything is inlined — the app, TensorFlow.js, the
MoveNet pose weights and the BlazeFace / FaceMesh face weights — so after the first load
it runs with no network at all.

**Your pictures never leave your device.** There is no upload, no API call and no
analytics. Images are read locally in the browser and the pose and face models run on-device.
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
| `android/` | The Android app: a WebView around `index.html`, with file picking, saving and sharing |
| `.github/workflows/android.yml` | Builds the signed APK and publishes it as a release |

## Credits and licences

- [TensorFlow.js](https://github.com/tensorflow/tfjs) — Apache License 2.0
- [MoveNet SinglePose Lightning](https://www.kaggle.com/models/google/movenet) — Apache License 2.0
- [MediaPipe BlazeFace and FaceMesh](https://github.com/google/mediapipe) — Apache License 2.0.
  TF.js conversions from [@vladmandic/human-models](https://github.com/vladmandic/human-models) — MIT

All of them are bundled inside `index.html`. The TensorFlow.js and MoveNet licence text ships
with them in that file; the face models carry a licence note beside their weights.

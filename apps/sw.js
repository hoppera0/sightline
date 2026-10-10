/* Studio service worker: every app in this folder works offline after one visit.
   Bump VERSION whenever any file in SHELL changes, or phones keep the old copy. */
const VERSION = 'studio-1';
const SHELL = ['./', 'index.html', 'ui.css', 'manifest.json', 'glow.html', 'loop.html',
               'icons/studio.png', 'icons/studio-192.png', 'icons/studio-maskable.png',
               'icons/glow.png', 'icons/glow-192.png', 'icons/loop.png', 'icons/loop-192.png'];

self.addEventListener('install', (e) => {
  e.waitUntil(caches.open(VERSION).then((c) => c.addAll(SHELL)).then(() => self.skipWaiting()));
});
self.addEventListener('activate', (e) => {
  e.waitUntil(caches.keys()
    .then((keys) => Promise.all(keys.filter((k) => k.startsWith('studio-') && k !== VERSION).map((k) => caches.delete(k))))
    .then(() => self.clients.claim()));
});
// Network first, so a new version shows up as soon as there is signal; cache when offline.
self.addEventListener('fetch', (e) => {
  if (e.request.method !== 'GET' || new URL(e.request.url).origin !== location.origin) return;
  e.respondWith(fetch(e.request).then((res) => {
    if (res.ok) { const copy = res.clone(); caches.open(VERSION).then((c) => c.put(e.request, copy)); }
    return res;
  }).catch(() => caches.match(e.request, { ignoreSearch: true })
    .then((hit) => hit || (e.request.mode === 'navigate' ? caches.match('index.html') : Response.error()))));
});

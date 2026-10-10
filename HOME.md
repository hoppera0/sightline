# Running it all from home

Everything here is static files, so there are three ways to reach it. None of them needs
the work machine.

## 1. Anywhere: GitHub Pages (default)

| | |
|---|---|
| Studio (all apps) | https://hoppera0.github.io/sightline/apps/ |
| Glow | https://hoppera0.github.io/sightline/apps/glow.html |
| Loop | https://hoppera0.github.io/sightline/apps/loop.html |
| Sightline | https://hoppera0.github.io/sightline/ |
| Sightline Android APK | https://github.com/hoppera0/sightline/releases/latest |

Open a link on the phone and use **Add to home screen** (Chrome menu ⋮, or Safari
Share). After one visit they work offline.

## 2. Private: your home machine over Tailscale

If GitHub Pages is ever blocked, or you want a private copy:

1. On the home machine: install [Tailscale](https://tailscale.com/download) and sign in
   with the same account as your phone. In the Tailscale admin console, under **DNS**,
   turn on **MagicDNS** and **HTTPS certificates** (one time).
2. Clone the repository and start serving:
   ```sh
   git clone https://github.com/hoppera0/sightline.git
   cd sightline
   ./tools/serve-home.sh
   ```
3. It prints two links like `https://home-pc.tail1234.ts.net/apps/`. Open them on the
   phone with Tailscale switched on, and add them to the home screen.

Only devices on your tailnet can reach it. The script pulls the latest from GitHub each
time it starts (`NO_PULL=1` to skip that).

To keep it running after a reboot, run the script from a login item (macOS), Task
Scheduler (Windows, via WSL or Git Bash), or a `systemd --user` service (Linux).

## 3. Keep building without the work machine

The repository on GitHub is the single source of truth, so the building side moves with
you:

- **Claude Code on the web** (claude.ai/code) works on this repository from any browser,
  including the phone. Nothing depends on a particular computer.
- **Claude Code on the home machine**: install it, clone the repository as above, and run
  `claude` inside it. The same pull requests, the same Actions build for the Android app.
- Changes merged to `main` go live on GitHub Pages automatically, and the home machine
  picks them up the next time `serve-home.sh` starts.

Keep anything personal (keys, client work) out of this repository: it is public, because
GitHub Pages on the free plan needs that.

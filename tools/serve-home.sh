#!/usr/bin/env bash
# Serve this repository from your home machine to your own devices over Tailscale.
# Your phone then opens https://<this-machine>.<your-tailnet>.ts.net/apps/ — private to
# your tailnet, real HTTPS, so the apps install and work offline just like on GitHub Pages.
#
#   ./tools/serve-home.sh            # pull the latest, serve on port 8080
#   PORT=9000 ./tools/serve-home.sh  # another local port
#   NO_PULL=1 ./tools/serve-home.sh  # serve exactly what is checked out
set -euo pipefail
cd "$(dirname "$0")/.."
PORT="${PORT:-8080}"

command -v tailscale >/dev/null || { echo "Tailscale is not installed: https://tailscale.com/download"; exit 1; }
[ -n "${NO_PULL:-}" ] || git pull --ff-only || echo "(could not pull; serving what is here)"

python3 -m http.server "$PORT" --bind 127.0.0.1 >/dev/null 2>&1 &
SRV=$!
cleanup() { kill "$SRV" 2>/dev/null || true; tailscale serve --https=443 off >/dev/null 2>&1 || true; }
trap cleanup EXIT INT TERM

tailscale serve --bg "$PORT"
HOST="$(tailscale status --json | python3 -c 'import json,sys; print(json.load(sys.stdin)["Self"]["DNSName"].rstrip("."))')"
echo
echo "  Studio:    https://$HOST/apps/"
echo "  Sightline: https://$HOST/"
echo
echo "Open those on your phone (Tailscale on). Ctrl+C stops serving."
wait "$SRV"

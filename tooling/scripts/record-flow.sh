#!/usr/bin/env bash
# Records an emulator while a Maestro flow drives the app, then makes the showcase media (plan, Phase 6):
#   docs/showcase/recordings/<flow>.mp4   H.264, 540 px wide, for the README and the docs site
#   docs/showcase/recordings/<flow>.gif   animated, 270 px wide, the fallback where video does not play
#
# Usage: tooling/scripts/record-flow.sh <flow>   (a file in .maestro/showcase/ or .maestro/, without .yaml)
# Needs: a running emulator with the debug app installed, maestro (~/.maestro/bin or on PATH), ffmpeg.
set -euo pipefail

flow="${1:?usage: record-flow.sh <flow>}"
root="$(cd "$(dirname "$0")/../.." && pwd)"
file="$root/.maestro/showcase/$flow.yaml"
[[ -f "$file" ]] || file="$root/.maestro/$flow.yaml"
[[ -f "$file" ]] || { echo "No flow named $flow" >&2; exit 1; }
maestro="$(command -v maestro || echo "$HOME/.maestro/bin/maestro")"
out="$root/docs/showcase/recordings"
work="$(mktemp -d)"
trap 'rm -rf "$work"' EXIT
mkdir -p "$out"

# A flow's setup (<flow>-setup.yaml beside it) runs first, unrecorded, so the reel starts where the story does.
setup="$(dirname "$file")/$flow-setup.yaml"
[[ -f "$setup" ]] && "$maestro" test "$setup" >/dev/null

# screenrecord stops by itself after 3 minutes; the showcase flows are well under that.
adb shell rm -f /sdcard/flow.mp4
adb shell screenrecord --bit-rate 8000000 /sdcard/flow.mp4 &
recorder=$!
sleep 1
"$maestro" test "$file"
sleep 1
adb shell pkill -INT screenrecord || true
wait "$recorder" || true
sleep 1
adb pull /sdcard/flow.mp4 "$work/raw.mp4" >/dev/null

ffmpeg -loglevel error -y -i "$work/raw.mp4" \
  -vf "scale=540:-2:flags=lanczos,fps=30" -c:v libx264 -preset slow -crf 26 -pix_fmt yuv420p \
  -movflags +faststart -an "$out/$flow.mp4"
# A palette made from the clip keeps the GIF sharp and small.
ffmpeg -loglevel error -y -i "$work/raw.mp4" \
  -vf "fps=8,scale=270:-2:flags=lanczos,split[a][b];[a]palettegen=max_colors=96[p];[b][p]paletteuse=dither=bayer:bayer_scale=4" \
  -loop 0 "$out/$flow.gif"

ls -lh "$out/$flow.mp4" "$out/$flow.gif" | awk '{print $5, $9}'

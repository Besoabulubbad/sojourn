#!/usr/bin/env bash
# CI (main.yml): installs the debug app on the running emulator and runs every Maestro flow in .maestro/, writing a
# JUnit report. Locally: start an emulator, ./gradlew :app:android:assembleDebug, then run this.
set -euo pipefail
root="$(cd "$(dirname "$0")/../.." && pwd)"
maestro="$(command -v maestro || echo "$HOME/.maestro/bin/maestro")"
adb install -r "$root/app/android/build/outputs/apk/debug/android-debug.apk"
"$maestro" test "$root/.maestro/" --format junit --output "$root/build/maestro-report.xml"

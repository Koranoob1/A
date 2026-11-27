#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
APP="$1"
APK="$(realpath "$2")"
CLI="$(realpath "$3")"

WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT

case "$APP" in
  proton-pass)
    CLASS='Lproton/android/pass/commonui/api/PassPalette;'
    ;;
  thunderbird)
    CLASS='Lnet/thunderbird/components/ui/bolt/theme/thunderbird/ThemeColorsKt;'
    ;;
  *)
    echo "Unsupported app: $APP"
    exit 1
    ;;
esac

cd "$WORK"

cp "$CLI" morphe-desktop.jar

sha256sum -c "$ROOT/scripts/baseline/cli.sha256"

javac -cp morphe-desktop.jar \
  -d . "$ROOT/scripts/DexCheck.java"

java -cp .:morphe-desktop.jar DexCheck \
  "$APK" "$CLASS" > "$APP.structure"

grep -F "  $APP.structure" \
  "$ROOT/scripts/baseline/structure.sha256" |
  sha256sum -c -

if [ "$APP" = thunderbird ]; then
  unzip -p "$APK" resources.arsc > thunderbird.arsc
  sha256sum -c "$ROOT/scripts/baseline/resources.sha256"
fi

echo "PASS: $APP compatibility baseline matched"

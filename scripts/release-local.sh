#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

"$ROOT_DIR/scripts/build-wasm.sh"

cd "$ROOT_DIR"

if command -v java >/dev/null 2>&1; then
  ./gradlew clean build fatJar centralPortalBundle
elif command -v powershell.exe >/dev/null 2>&1 && [[ -f "$ROOT_DIR/gradlew.bat" ]] && command -v wslpath >/dev/null 2>&1; then
  WINDOWS_ROOT_DIR="$(wslpath -w "$ROOT_DIR")"
  powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "Set-Location -LiteralPath '$WINDOWS_ROOT_DIR'; .\\gradlew.bat clean build fatJar centralPortalBundle"
elif [[ -f "$ROOT_DIR/gradlew.bat" ]]; then
  ./gradlew.bat clean build fatJar centralPortalBundle
else
  echo "Java was not found in PATH and no compatible Gradle wrapper was available." >&2
  exit 1
fi

echo "Built Maven Central bundle at $ROOT_DIR/build/central-portal/central-bundle.zip"

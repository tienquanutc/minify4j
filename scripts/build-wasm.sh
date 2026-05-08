#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RUST_DIR="$ROOT_DIR/minify4j-wasm"
JAVA_WASM_RESOURCES="$ROOT_DIR/src/main/resources/wasm"

cd "$RUST_DIR"

# getrandom 0.3 does not support wasm32-unknown-unknown by default.
# For this demo we do not need runtime randomness, so use the unsupported backend
# instead of wasm_js. wasm_js would add JS/wasm-bindgen imports that Chicory
# cannot satisfy in a plain JVM runtime.
RUSTFLAGS='--cfg getrandom_backend="unsupported"' \
  cargo build --release --target wasm32-unknown-unknown

mkdir -p "$JAVA_WASM_RESOURCES"
cp "$RUST_DIR/target/wasm32-unknown-unknown/release/minify4j_wasm.wasm" \
  "$JAVA_WASM_RESOURCES/minify4j.wasm"

echo "Copied wasm to $JAVA_WASM_RESOURCES/minify4j.wasm"

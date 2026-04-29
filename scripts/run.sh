#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BUILD_DIR="$ROOT_DIR/build/classes"

rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR"

find "$ROOT_DIR/src/main/java" -name "*.java" -print0 | xargs -0 javac -d "$BUILD_DIR"

cd "$ROOT_DIR"
java -cp "$BUILD_DIR" com.codex.fsd.App

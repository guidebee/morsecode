#!/usr/bin/env bash
# Offline, dev-only tool - not part of the Android build.
#
# Compiles and runs LevelConverter, which turns the original GTGE Levels.*
# classes (in this directory's src/, copied from C:\workspace\Mario - see
# docs/MARIO_PORT_PLAN.md Step 1) into JSON under
# app/src/main/assets/mario/levels/.
#
# Run from the repo root:
#   bash tools/mario-level-converter/convert.sh

set -euo pipefail
cd "$(dirname "$0")"

rm -rf out
mkdir -p out

find src -name "*.java" > out/sources.txt
javac -d out @out/sources.txt

cd ../..
java -cp tools/mario-level-converter/out LevelConverter app/src/main/assets/mario/levels

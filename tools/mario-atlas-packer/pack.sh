#!/usr/bin/env bash
# Offline, dev-only tool - not part of the Android build.
#
# Compiles and runs PackMarioAtlas, which packs the World-1 subset of the
# original game's PNGs (in C:\workspace\Mario\SandBox) into a libGDX-format
# TextureAtlas under app/src/main/assets/ - see docs/MARIO_PORT_PLAN.md Step 2.
#
# Reskin (docs/mario/MARIO_RESKIN_EXECUTION.md): any file present under the
# third argument (default docs/assets/mario-sprites/reskin-source) is used
# instead of the matching file in C:\workspace\Mario\SandBox, per asset -
# lets the reskin land incrementally without needing every asset replaced.
#
# Run from the repo root:
#   bash tools/mario-atlas-packer/pack.sh

set -euo pipefail
cd "$(dirname "$0")"

rm -rf out
mkdir -p out

javac -d out src/PackMarioAtlas.java

cd ../..
java -cp tools/mario-atlas-packer/out PackMarioAtlas "C:/workspace/Mario/SandBox" app/src/main/assets \
    docs/assets/mario-sprites/reskin-source

"""
Step R.5 checklist: Explosion (the puff FireBall/Fireworks both reuse).

Explosion.java confirms: "explosion" is tileSize x tileSize per frame (32x32),
plays a fixed-length strip once via plain nextFrame() (no custom
setFrameSequence call - unlike Axe), so the AssetSpec's declared 3 frames
(96x32) IS the full playthrough length, growing then gone.

Source: other/explode-Sheet[64height64wide].png (512x64, 8 frames of 64x64) -
already an actual bespoke explosion animation from the same Robot Master
Series pack the rest of this reskin draws from (Boss/BossFire), not a
procedural stand-in. A per-frame getbbox() scan (see
docs/mario/MARIO_RESKIN_EXECUTION.md's entry for this task) confirms frames
0-6 grow steadily from a small spark to a large scorch burst, frame 7 is
empty (fully faded/removed) - so frames 0, 3, 6 give a clean small -> medium
-> large 3-stage burst read, matching Explosion.java's own
"plays once, nextFrame() 0->1->2 then removed" behavior.
"""
import os
import sys

from PIL import Image

sys.path.insert(0, "C:/workspace/morsecode/docs/assets/mario-sprites/ampere-staging")
from sprite_tools import build_sheet, place_content, verified_crop

OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"
PACK = "C:/workspace/robot_series_base_pack"


def build():
    print("Explosion (verified crops):")
    sheet = Image.open(f"{PACK}/other/explode-Sheet[64height64wide].png").convert("RGBA")
    fw = 64

    def frame(i):
        return verified_crop(sheet, (i * fw, 0, (i + 1) * fw, 64), f"explosion frame {i}")

    frames = [place_content(frame(i), 32, 32, fill=0.9) for i in (0, 3, 6)]
    build_sheet(32, 32, frames, 3, 1, f"{OUT}/Explosion.png")


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    build()

"""
Step R.4 Phase 7.

Covers:
- Explosion (explosion) - 3 frames, 32x32 each.
"""
from PIL import Image
import os
import sys

sys.path.insert(0, "C:/workspace/morsecode/docs/assets/mario-sprites/ampere-staging")
from sprite_tools import verified_crop, place_content, build_sheet

PACK = "C:/workspace/robot_series_base_pack"
OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"


def build_explosion():
    sheet = Image.open(f"{PACK}/other/explode-Sheet[64height64wide].png").convert("RGBA")
    frame_w, frame_h = 64, 64
    frames = []
    for i in (1, 2, 3):
        crop = verified_crop(sheet, (i * frame_w, 0, (i + 1) * frame_w, frame_h), f"explosion{i}")
        frames.append(place_content(crop, 32, 32, fill=0.95, anchor="center"))
    build_sheet(32, 32, frames, 3, 1, f"{OUT}/Explosion.png")


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    build_explosion()

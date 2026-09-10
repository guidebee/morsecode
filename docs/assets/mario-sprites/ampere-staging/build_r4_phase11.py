"""
Step R.4 Phase 11.

Covers:
- FishyWater variants (fish_grey, fish_red) - 2x1, 32x32 frames.
"""
from PIL import Image
import os
import sys

sys.path.insert(0, "C:/workspace/morsecode/docs/assets/mario-sprites/ampere-staging")
from sprite_tools import verified_crop, place_content, tint, build_sheet

PACK = "C:/workspace/robot_series_base_pack"
OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"


def build_fish():
    sheet = Image.open(f"{PACK}/enemy1/enemy1_flyingeffect[32height32wide].png").convert("RGBA")
    frame_w, frame_h = 32, 32
    base_frames = []
    for i in range(2):
        crop = verified_crop(sheet, (i * frame_w, 0, (i + 1) * frame_w, frame_h), f"fish{i}")
        base_frames.append(place_content(crop, 32, 32, fill=0.9, anchor="center"))

    build_sheet(32, 32, base_frames, 2, 1, f"{OUT}/FishGrey.png")
    red_frames = [tint(frame, (210, 70, 70), 0.4) for frame in base_frames]
    build_sheet(32, 32, red_frames, 2, 1, f"{OUT}/FishRed.png")


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    build_fish()

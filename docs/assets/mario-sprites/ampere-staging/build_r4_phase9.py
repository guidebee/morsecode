"""
Step R.4 Phase 9.

Covers:
- bw_hammer (CloudsNight/Hammer) - 4 frames, 28x28 each.
"""
from PIL import Image
import os
import sys

sys.path.insert(0, "C:/workspace/morsecode/docs/assets/mario-sprites/ampere-staging")
from sprite_tools import verified_crop, place_content, build_sheet

PACK = "C:/workspace/robot_series_base_pack"
OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"


def build_hammer():
    sheet = Image.open(f"{PACK}/robot1/robo1/bulletimpact-Sheet[32height32wide].png").convert("RGBA")
    frame_w, frame_h = 32, 32
    frames = []
    for i in range(4):
        crop = verified_crop(sheet, (i * frame_w, 0, (i + 1) * frame_w, frame_h), f"hammer{i}")
        frames.append(place_content(crop, 28, 28, fill=0.95, anchor="center"))
    build_sheet(28, 28, frames, 4, 1, f"{OUT}/Hammer.png")


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    build_hammer()

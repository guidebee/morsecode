"""
Step R.4 Phase 6.

Covers:
- FireBall (fire_ball) - 4 frames, 16x16 each.
"""
from PIL import Image
import os
import sys

sys.path.insert(0, "C:/workspace/morsecode/docs/assets/mario-sprites/ampere-staging")
from sprite_tools import verified_crop, place_content, build_sheet

PACK = "C:/workspace/robot_series_base_pack"
OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"


def build_fireball():
    sheet = Image.open(f"{PACK}/robot1/robo1/chargedbullet-Sheet[32height32wide].png").convert("RGBA")
    frame_w, frame_h = 32, 32
    frames = []
    for i in range(4):
        crop = verified_crop(sheet, (i * frame_w, 0, (i + 1) * frame_w, frame_h), f"fireball{i}")
        frames.append(place_content(crop, 16, 16, fill=0.95, anchor="center"))
    build_sheet(16, 16, frames, 4, 1, f"{OUT}/FireBall.png")


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    build_fireball()

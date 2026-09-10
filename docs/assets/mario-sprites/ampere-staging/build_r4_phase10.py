"""
Step R.4 Phase 10.

Covers:
- Monkey (monkey) - 3x2, 32x48 frames.
"""
from PIL import Image
import os
import sys

sys.path.insert(0, "C:/workspace/morsecode/docs/assets/mario-sprites/ampere-staging")
from sprite_tools import verified_crop, place_content, mirror, build_sheet

PACK = "C:/workspace/robot_series_base_pack"
OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"


def build_monkey():
    sheet = Image.open(f"{PACK}/enemy1/enemy1all.png").convert("RGBA")
    cell = 32
    row = 0

    right_raw = [
        verified_crop(sheet, (cell * i, cell * row, cell * (i + 1), cell * (row + 1)), f"monkey_walk{i}")
        for i in (0, 1)
    ]
    right_frames = [place_content(frame, 32, 48, fill=0.85, anchor="bottom") for frame in right_raw]
    left_frames = [mirror(frame) for frame in right_frames]

    frames = [
        left_frames[0],
        left_frames[1],
        left_frames[0],
        right_frames[0],
        right_frames[0],
        right_frames[1],
    ]
    build_sheet(32, 48, frames, 3, 2, f"{OUT}/Monkey.png")


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    build_monkey()

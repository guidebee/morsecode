"""
Step R.4 Phase 8.

Covers:
- Lava (lava) - 1x1
- LavaBall (lava_ball) - 2x1
- Water (water) - 1x1
"""
from PIL import Image
import os
import sys

sys.path.insert(0, "C:/workspace/morsecode/docs/assets/mario-sprites/ampere-staging")
from sprite_tools import place_content, glow_pulse, build_sheet

KENNEY = "C:/workspace/Kenney_Game_Assets_All/2D assets/Voxel Pack/PNG/Tiles"
OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"


def build_lava_and_water():
    lava_src = Image.open(f"{KENNEY}/lava.png").convert("RGBA")
    water_src = Image.open(f"{KENNEY}/water.png").convert("RGBA")
    lava = lava_src.resize((32, 32), Image.NEAREST)
    water = water_src.resize((32, 32), Image.NEAREST)
    lava.save(f"{OUT}/Lava.png")
    water.save(f"{OUT}/Water.png")
    print("wrote", f"{OUT}/Lava.png")
    print("wrote", f"{OUT}/Water.png")


def build_lava_ball():
    lava_src = Image.open(f"{KENNEY}/lava.png").convert("RGBA")
    crop = lava_src.crop((32, 32, 96, 96))
    base = place_content(crop, 32, 32, fill=0.9, anchor="center")
    frames = [base, glow_pulse(base, 0.35)]
    build_sheet(32, 32, frames, 2, 1, f"{OUT}/LavaBall.png")


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    build_lava_and_water()
    build_lava_ball()

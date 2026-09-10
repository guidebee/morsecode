"""
Step R.4 Phase 15.

Covers backdrops:
- mountain (Mountain.png)
- clouds (Clouds.png)
- cloudsnight (CloudsNight.png)
- fence (Fence.png)
- fence2 (Fence2.png)
- sea_background (SeaBackground.png)

Mapped-source update (2026-09-10):
- Backdrops are sourced from Kenney Background Elements and Robot Master
  Series `platform.png` instead of procedural drawings.
"""
from PIL import Image
import os
import sys

sys.path.insert(0, "C:/workspace/morsecode/docs/assets/mario-sprites/ampere-staging")
from sprite_tools import tint

OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"
KENNEY_BG = "C:/workspace/Kenney_Game_Assets_All/2D assets/Background Elements/PNG"
ROBOT_PLATFORM = "C:/workspace/robot_series_base_pack/other/platform.png"


def load_png(name):
    return Image.open(f"{KENNEY_BG}/{name}").convert("RGBA")


def save_32x32(image, out_name):
    frame = image.resize((32, 32), Image.NEAREST)
    frame.save(f"{OUT}/{out_name}")
    print("wrote", f"{OUT}/{out_name}")


def build_mountain():
    save_32x32(load_png("piramid.png"), "Mountain.png")


def build_clouds():
    save_32x32(load_png("cloud6.png"), "Clouds.png")


def build_cloudsnight():
    night = tint(load_png("cloud6.png"), (35, 45, 70), strength=0.65)
    save_32x32(night, "CloudsNight.png")


def build_fence():
    save_32x32(load_png("fence.png"), "Fence.png")


def build_fence2():
    save_32x32(load_png("fence_piece.png"), "Fence2.png")


def build_sea_background():
    platform = Image.open(ROBOT_PLATFORM).convert("RGBA")
    sea = platform.crop((0, 128, 320, 288))
    save_32x32(sea, "SeaBackground.png")


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    build_mountain()
    build_clouds()
    build_cloudsnight()
    build_fence()
    build_fence2()
    build_sea_background()

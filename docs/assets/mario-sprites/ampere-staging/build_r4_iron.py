"""
Step R.4 Task 1: Iron, the permanent block left after an item block is used.

The output is a 4x1 sheet of 32x32 cells in the order required by Iron.java:
Sea, Ground, UnderGround, Castle.
"""
import os
import sys

from PIL import Image

sys.path.insert(0, "C:/workspace/morsecode/docs/assets/mario-sprites/ampere-staging")
from sprite_tools import build_sheet, is_tileable_texture, place_content, tint, verified_crop


OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"
SOURCE = (
    "C:/workspace/Kenney_Game_Assets_All/2D assets/"
    "Pixel Platformer Industrial Expansion/Tilemap/tilemap.png"
)

THEMES = [
    ("Sea", (47, 99, 119), 0.58),
    ("Ground", (138, 151, 166), 0.52),
    ("UnderGround", (47, 74, 65), 0.62),
    ("Castle", (107, 47, 47), 0.38),
]


def build_iron():
    sheet = Image.open(SOURCE).convert("RGBA")

    # Industrial Expansion uses 18px tiles separated by a 1px gutter.
    # Index 0 is the riveted brown block already used by the Fortress terrain.
    source_tile = verified_crop(sheet, (0, 0, 18, 18), "industrial block index 0")
    print("  source is tileable:", is_tileable_texture(source_tile))

    world_tile = place_content(source_tile, 32, 32, fill=1.0)
    frames = []
    for name, color, strength in THEMES:
        frame = tint(world_tile, color, strength)
        assert frame.getbbox() == (0, 0, 32, 32), f"{name} Iron frame does not fill its cell"
        frames.append(frame)

    os.makedirs(OUT, exist_ok=True)
    output = f"{OUT}/Iron.png"
    iron_sheet = build_sheet(32, 32, frames, 4, 1, output)
    assert iron_sheet.size == (128, 32)


if __name__ == "__main__":
    build_iron()

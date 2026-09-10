from PIL import Image
import sys

sys.path.insert(0, "C:/workspace/morsecode/docs/assets/mario-sprites/ampere-staging")
from sprite_tools import build_sheet, mirror, place_content, tint, verified_crop

PACK = "C:/workspace/Kenney_Game_Assets_All/2D assets/Pixel Platformer Industrial Expansion/Tiles"
OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"


if __name__ == "__main__":
    # Use Industrial Expansion tool/arm tiles as the source family, then map
    # into Axe.java's 4-frame shimmer loop.
    tool0 = Image.open(f"{PACK}/tile_0080.png").convert("RGBA")
    tool1 = Image.open(f"{PACK}/tile_0081.png").convert("RGBA")
    tool2 = Image.open(f"{PACK}/tile_0082.png").convert("RGBA")

    f0 = place_content(verified_crop(tool0, (0, 0, 18, 18), "axe src 0"), 32, 32, fill=0.92, anchor="center")
    f1 = place_content(verified_crop(tool1, (0, 0, 18, 18), "axe src 1"), 32, 32, fill=0.92, anchor="center")
    f2 = place_content(verified_crop(tool2, (0, 0, 18, 18), "axe src 2"), 32, 32, fill=0.92, anchor="center")
    f3 = mirror(f1)

    frames = [
        tint(f0, (255, 146, 88), 0.10),
        tint(f1, (255, 166, 98), 0.16),
        tint(f2, (255, 136, 82), 0.14),
        tint(f3, (255, 126, 76), 0.12),
    ]
    build_sheet(32, 32, frames, 4, 1, f"{OUT}/Axe.png")
    print("wrote Axe.png")

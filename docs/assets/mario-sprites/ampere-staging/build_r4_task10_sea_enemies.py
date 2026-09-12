from PIL import Image
import sys

sys.path.insert(0, "C:/workspace/morsecode/docs/assets/mario-sprites/ampere-staging")
from sprite_tools import build_sheet, place_content, tint, verified_crop

PACK = "C:/workspace/Kenney_Game_Assets_All/2D assets/New Platformer Pack/Sprites/Enemies/Default"
OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"


def fit_32x32(path, label):
    src = Image.open(path).convert("RGBA")
    crop = verified_crop(src, (0, 0, src.width, src.height), label)
    return place_content(crop, 32, 32, fill=0.88, anchor="center")


def fit_32x48(path, label):
    src = Image.open(path).convert("RGBA")
    crop = verified_crop(src, (0, 0, src.width, src.height), label)
    return place_content(crop, 32, 48, fill=0.86, anchor="center")


if __name__ == "__main__":
    # Fish: 2x1 (frame 0/1 swim cycle)
    fish_grey = [
        tint(fit_32x32(f"{PACK}/fish_blue_swim_a.png", "fish_grey swim a"), (122, 140, 164), 0.45),
        tint(fit_32x32(f"{PACK}/fish_blue_swim_b.png", "fish_grey swim b"), (122, 140, 164), 0.45),
    ]
    fish_red = [
        tint(fit_32x32(f"{PACK}/fish_yellow_swim_a.png", "fish_red swim a"), (198, 86, 78), 0.45),
        tint(fit_32x32(f"{PACK}/fish_yellow_swim_b.png", "fish_red swim b"), (198, 86, 78), 0.45),
    ]

    # OctoPussy: 2x1 in a tall 32x48 frame.
    octo = [
        fit_32x48(f"{PACK}/fish_purple_down.png", "octopussy frame 0"),
        fit_32x48(f"{PACK}/fish_purple_up.png", "octopussy frame 1"),
    ]

    build_sheet(32, 32, fish_grey, 2, 1, f"{OUT}/FishGrey.png")
    build_sheet(32, 32, fish_red, 2, 1, f"{OUT}/FishRed.png")
    build_sheet(32, 48, octo, 2, 1, f"{OUT}/OctoPussy.png")
    print("wrote FishGrey.png, FishRed.png, Octopussy.png")

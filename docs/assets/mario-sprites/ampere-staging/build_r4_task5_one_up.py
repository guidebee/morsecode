from PIL import Image
import sys

sys.path.insert(0, "C:/workspace/morsecode/docs/assets/mario-sprites/ampere-staging")
from sprite_tools import build_sheet, place_content, verified_crop

SRC = "C:/workspace/Robot_Platform_Pack/Tileset&Items.png"
OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source/1UP.png"


if __name__ == "__main__":
    src = Image.open(SRC).convert("RGBA")

    # Spare chassis fallback (Task 5): use the two top-row heart variants from
    # Robot Platform Pack's tileset and present them as a 2-frame idle pulse.
    # Coordinates verified against the real source image, not guessed.
    left = verified_crop(src, (114, 51, 124, 62), "one_up frame left")
    right = verified_crop(src, (130, 51, 140, 62), "one_up frame right")

    f0 = place_content(left, 32, 32, fill=0.9, anchor="center")
    f1 = place_content(right, 32, 32, fill=0.9, anchor="center")

    build_sheet(32, 32, [f0, f1], 2, 1, OUT)

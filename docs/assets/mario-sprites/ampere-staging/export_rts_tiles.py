"""
Export a small grid of 32x32 tiles from the RTS Sci-fi tilesheet for selection.
"""
from PIL import Image
import os

SRC = "C:/workspace/Kenney_Game_Assets_All/2D assets/RTS Sci-fi/Tilesheet/scifi_tilesheet.png"
OUT = "C:/Users/james/AppData/Local/Temp/opencode/rts_candidates"


def export_tiles(start_col, start_row, cols, rows):
    os.makedirs(OUT, exist_ok=True)
    sheet = Image.open(SRC).convert("RGBA")
    cell = 32
    for r in range(start_row, start_row + rows):
        for c in range(start_col, start_col + cols):
            crop = sheet.crop((c * cell, r * cell, (c + 1) * cell, (r + 1) * cell))
            crop.save(f"{OUT}/tile_r{r}_c{c}.png")


if __name__ == "__main__":
    export_tiles(start_col=20, start_row=0, cols=16, rows=6)
    print(OUT)

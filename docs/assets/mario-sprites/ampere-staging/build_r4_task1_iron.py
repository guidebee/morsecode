from PIL import Image, ImageDraw
import sys

sys.path.insert(0, "C:/workspace/morsecode/docs/assets/mario-sprites/ampere-staging")
from sprite_tools import build_sheet, tint, verified_crop

OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source/Iron.png"
KENNEY = "C:/workspace/Kenney_Game_Assets_All/2D assets"


def tile_from_grid(sheet_path, tile_px, cols, index, label):
    im = Image.open(sheet_path).convert("RGBA")
    pitch = tile_px + 1
    row, col = divmod(index, cols)
    x0 = col * pitch
    y0 = row * pitch
    return verified_crop(im, (x0, y0, x0 + tile_px, y0 + tile_px), label)


def to_iron_frame(tile):
    frame = tile.resize((32, 32), Image.NEAREST)
    draw = ImageDraw.Draw(frame)
    draw.rectangle((0, 0, 31, 31), outline=(28, 28, 32, 255), width=1)
    draw.rectangle((4, 4, 27, 27), outline=(210, 210, 210, 120), width=1)
    draw.rectangle((13, 13, 18, 18), fill=(36, 36, 40, 255))
    draw.rectangle((14, 14, 17, 17), fill=(146, 146, 156, 255))
    return frame


if __name__ == "__main__":
    industrial = f"{KENNEY}/Pixel Platformer Industrial Expansion/Tilemap/tilemap.png"
    tiny = f"{KENNEY}/Tiny Dungeon/Tilemap/tilemap.png"
    rogue = f"{KENNEY}/Roguelike Dungeon Pack/Spritesheet/roguelikeDungeon_transparent.png"

    sea = to_iron_frame(tile_from_grid(rogue, 16, 29, 290, "iron sea base idx290"))
    ground = to_iron_frame(tile_from_grid(industrial, 18, 16, 0, "iron ground base idx0"))
    underground_base = tile_from_grid(tiny, 16, 12, 57, "iron underground base idx57")
    underground = to_iron_frame(tint(underground_base, (53, 208, 160), 0.28))
    castle = to_iron_frame(tile_from_grid(industrial, 18, 16, 4, "iron castle base idx4"))

    build_sheet(32, 32, [sea, ground, underground, castle], 4, 1, OUT)

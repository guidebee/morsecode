"""
Replaces the Step R.3 procedural stone/chocolate/brick tiles for Fortress
(Castle), Substrate (UnderGround), and Flooded Sector (Sea) with real
hand-drawn pixel art pulled from the local Kenney "Game Assets All-in-1"
bundle, per docs/mario/KENNEY_ALL_IN_ONE_INDEX.md Sec1's recommendation.
Ground (Surface) stays on the procedural tile - lowest priority, already
reads acceptably.

Sources (tile grid indices confirmed by overlaying a labeled grid on each
pack's tilemap and reading it visually - see the index doc for the process):
  - Fortress:       Pixel Platformer Industrial Expansion, 18x18 native tiles,
                     16 cols x 7 rows, 19px pitch (1px spacing).
                     stone=idx0 (rock/rust block), chocolate=idx4 (orange
                     girder), brick=idx25 (yellow/black hazard-stripe block -
                     already reads as "caution/breakable").
  - Substrate:       Tiny Dungeon, 16x16 native tiles, 12 cols x 11 rows,
                     17px pitch. stone=idx57 (clean grey stone-block wall),
                     chocolate=idx36 (horizontal-coursed stone variant),
                     brick=idx79 (cage/bar pattern, tinted - reads as a
                     circuit-cage "breakable" panel).
  - Flooded Sector:  Roguelike Dungeon Pack, 16x16 native tiles, 29 cols x 18
                     rows, 17px pitch. stone=idx290 (clean blue block),
                     chocolate=idx296 (clean teal block), brick=idx292
                     (blue block with a corner rivet detail - distinct enough
                     to read as breakable), stone_Castle_Sea=idx289.

bw_stone (CloudsNight) reuses Tiny Dungeon's idx57 without the Substrate
tint - already grey/desaturated, matching the original bw_* convention.
"""
from PIL import Image, ImageOps
import os

OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"
KENNEY = "C:/workspace/Kenney_Game_Assets_All/2D assets"


def tile_from_grid(sheet_path, tile_px, cols, index):
    pitch = tile_px + 1
    im = Image.open(sheet_path).convert("RGBA")
    row, col = divmod(index, cols)
    x0, y0 = col * pitch, row * pitch
    return im.crop((x0, y0, x0 + tile_px, y0 + tile_px))


def to_world_tile(tile, size=32):
    return tile.resize((size, size), Image.NEAREST)


def tint(frame, color, strength=0.3):
    overlay = Image.new("RGBA", frame.size, color + (255,))
    blended = Image.blend(frame.convert("RGBA"), overlay, strength)
    r, g, b, a = frame.split()
    blended.putalpha(a)
    return blended


def industrial_expansion(index):
    sheet = f"{KENNEY}/Pixel Platformer Industrial Expansion/Tilemap/tilemap.png"
    return to_world_tile(tile_from_grid(sheet, 18, 16, index))


def tiny_dungeon(index):
    sheet = f"{KENNEY}/Tiny Dungeon/Tilemap/tilemap.png"
    return to_world_tile(tile_from_grid(sheet, 16, 12, index))


def roguelike_dungeon(index):
    sheet = f"{KENNEY}/Roguelike Dungeon Pack/Spritesheet/roguelikeDungeon_transparent.png"
    return to_world_tile(tile_from_grid(sheet, 16, 29, index))


if __name__ == "__main__":
    # Fortress (Castle) - used close to as-authored, already warm/rust-toned.
    industrial_expansion(0).save(f"{OUT}/stone_Castle.png")
    industrial_expansion(4).save(f"{OUT}/chocolate_Castle.png")
    industrial_expansion(25).save(f"{OUT}/brick_Castle.png")

    # Substrate (UnderGround) - Tiny Dungeon's stone is neutral grey; tint
    # toward the established dark-teal-green Substrate accent for consistency
    # with the Scuttler/BrickPeaces colors already shipped for this theme.
    substrate_tint = (53, 208, 160)
    tint(tiny_dungeon(57), substrate_tint, 0.28).save(f"{OUT}/stone_UnderGround.png")
    tint(tiny_dungeon(36), substrate_tint, 0.28).save(f"{OUT}/chocolate_UnderGround.png")
    tint(tiny_dungeon(79), substrate_tint, 0.35).save(f"{OUT}/brick_UnderGround.png")

    # bw_stone (CloudsNight) - Tiny Dungeon's stone as-is, no tint (matches
    # the original bw_* convention of a plain desaturated look).
    tiny_dungeon(57).save(f"{OUT}/CloudsNight/stone.png")

    # Flooded Sector (Sea) - Roguelike Dungeon Pack's blue/teal blocks are
    # already exactly on-palette, no tint needed.
    roguelike_dungeon(290).save(f"{OUT}/stone_Sea.png")
    roguelike_dungeon(296).save(f"{OUT}/chocolate_Sea.png")
    roguelike_dungeon(292).save(f"{OUT}/brick_Sea.png")
    roguelike_dungeon(289).save(f"{OUT}/stone_Castle_Sea.png")

    print("Fortress, Substrate, Flooded Sector terrain replaced with sourced pixel art.")
    print("Ground (Surface) left on the procedural tile - lowest priority per the index doc.")

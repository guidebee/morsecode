"""
Step R.5 checklist: the remaining plain structural/scenery pieces -
WoodenBridge, Wall, BridgeBloks, WhiteLine. All four are simple static
metal-panel textures (no animation, no per-theme attribute variant), built
procedurally to match this reskin's established industrial/riveted-metal
language (Iron.png, Bouncer.png) rather than any single source crop.

Class confirmations:
  WoodenBridge.java - solid, indestructible static platform, 1 frame 32x32
    (like Iron - HitFromDown is a no-op in the original).
  LevelLoader.spawnWall - "wall" AssetSpec is 2x1 (64x32): frame 0 = the top
    cap of a vertical strip, frame 1 = every row below it (a repeating body
    segment) - confirmed directly from spawnWall's own
    `frames[0][dy == 0 ? 0 : 1]` split.
  MarioTileRegistry's "BridgeBloks" case - a plain Brick using region
    "bridge_blocks" (NOT attribute-suffixed, unlike brick/stone/chocolate),
    1 frame 32x32 - one fixed look regardless of level theme.
  Scenery's own 4-arg constructor doc - "WhiteLine" is a single 32x32 image
    STRETCHED (not tiled/sliced) to 32x(13*32) by a plain draw() call, so it
    must read fine as a big vertical stretch, not depend on repeating detail
    the way Lava/Water's hazard column (Step R.5 hazards) does. Built as a
    plain glowing vertical stripe on a dark panel, which survives a 13x
    vertical stretch cleanly (a busier texture would streak/blur).
"""
import os
import sys

from PIL import Image, ImageDraw

sys.path.insert(0, "C:/workspace/morsecode/docs/assets/mario-sprites/ampere-staging")
from sprite_tools import build_sheet

OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"


def panel(size, plate_color, bolt_color, edge_color):
    """A riveted metal plate - same visual family as Iron/Bouncer."""
    im = Image.new("RGBA", (size, size), plate_color)
    draw = ImageDraw.Draw(im)
    draw.rectangle((0, 0, size - 1, size - 1), outline=edge_color, width=2)
    bolt_r = 2
    for bx, by in ((5, 5), (size - 6, 5), (5, size - 6), (size - 6, size - 6)):
        draw.ellipse((bx - bolt_r, by - bolt_r, bx + bolt_r, by + bolt_r), fill=bolt_color)
    return im


def build_wooden_bridge():
    im = panel(32, (90, 96, 104, 255), (40, 44, 50, 255), (60, 66, 74, 255))
    draw = ImageDraw.Draw(im)
    # Horizontal grating slats (a metal-plank platform rather than wood).
    for y in (10, 16, 22):
        draw.line((3, y, 28, y), fill=(60, 66, 74, 255), width=2)
    build_sheet(32, 32, [im], 1, 1, f"{OUT}/WoodenBridge.png")


def build_wall():
    cap = panel(32, (74, 82, 92, 255), (150, 210, 220, 255), (48, 54, 62, 255))
    draw = ImageDraw.Draw(cap)
    draw.rectangle((0, 0, 31, 6), fill=(58, 66, 76, 255))
    draw.rectangle((0, 0, 31, 6), outline=(150, 210, 220, 255))
    body = panel(32, (64, 70, 78, 255), (110, 150, 160, 255), (44, 50, 58, 255))
    build_sheet(32, 32, [cap, body], 2, 1, f"{OUT}/Wall.png")


def build_bridge_blocks():
    im = panel(32, (80, 60, 50, 255), (200, 150, 90, 255), (54, 40, 34, 255))
    draw = ImageDraw.Draw(im)
    draw.line((0, 16, 31, 16), fill=(54, 40, 34, 255), width=2)
    draw.line((16, 0, 16, 31), fill=(54, 40, 34, 255), width=2)
    build_sheet(32, 32, [im], 1, 1, f"{OUT}/BridgeBloks.png")


def build_white_line():
    im = Image.new("RGBA", (32, 32), (20, 24, 30, 255))
    draw = ImageDraw.Draw(im)
    draw.rectangle((13, 0, 18, 31), fill=(160, 235, 250, 255))
    draw.rectangle((15, 0, 16, 31), fill=(255, 255, 255, 255))
    build_sheet(32, 32, [im], 1, 1, f"{OUT}/WhiteLine.png")


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    build_wooden_bridge()
    build_wall()
    build_bridge_blocks()
    build_white_line()

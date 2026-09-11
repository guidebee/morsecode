"""
Step R.5 checklist: FireBall (Fire Mario's own projectile, also reused by
OrbitingFireball) + the Lava/LavaBall/Water hazard textures.

FireBall.java confirms: "fire_ball" is 16x16 per frame (64x16, 4 frames) -
smaller than most sprites here (confirmed against source, per that class's
own doc). Built procedurally (no existing reskin art fits a tiny 16x16
"spinning projectile" cell) as a small glowing plasma orb with a highlight
that rotates between frames to read as spinning, matching this reskin's
existing "energy/plasma" visual language (BossFire, the Charge coil glow).

LavaBall.java confirms: "lava_ball" is 32x32 per frame (64x32, 2 frames),
frame 0 = rising (gravity <= 0), frame 1 = falling - built as a bigger,
brighter version of the same plasma-orb language, with frame 1 trailing a
short motion streak (falling) that frame 0 doesn't have (rising/apex).

MarioTileRegistry confirms "lava"/"water" are each a SINGLE tall (32x128)
whole-image Scenery, drawn unsliced at native size (Scenery's 2-arg
constructor) - not a themed terrain tile, so both are built as a vertically
repeating (top-to-bottom tileable, checked with is_tileable_texture) hazard
column rather than a discrete icon.
"""
import math
import os
import sys

from PIL import Image, ImageDraw

sys.path.insert(0, "C:/workspace/morsecode/docs/assets/mario-sprites/ampere-staging")
from sprite_tools import build_sheet, is_tileable_texture

OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"


def plasma_orb(size, base_color, core_color, angle_deg, ring_color=None):
    im = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(im)
    r = size // 2 - 1
    cx = cy = size // 2
    draw.ellipse((cx - r, cy - r, cx + r, cy + r), fill=base_color,
                  outline=ring_color or base_color)
    core_r = max(2, r - r // 3)
    draw.ellipse((cx - core_r, cy - core_r, cx + core_r, cy + core_r), fill=core_color)
    # rotating highlight - a small bright dot offset from center, orbiting
    # between frames so the strip reads as a spinning orb.
    hl_r = max(1, r // 4)
    hx = cx + int((r - hl_r) * 0.5 * math.cos(math.radians(angle_deg)))
    hy = cy + int((r - hl_r) * 0.5 * math.sin(math.radians(angle_deg)))
    draw.ellipse((hx - hl_r, hy - hl_r, hx + hl_r, hy + hl_r), fill=(255, 255, 255, 220))
    return im


def build_fire_ball():
    frames = [plasma_orb(16, (214, 90, 30, 255), (255, 200, 110, 255), angle)
              for angle in (0, 90, 180, 270)]
    build_sheet(16, 16, frames, 4, 1, f"{OUT}/FireBall.png")


def build_lava_ball():
    rising = plasma_orb(32, (214, 60, 30, 255), (255, 190, 90, 255), 45,
                         ring_color=(120, 20, 10, 255))
    falling = plasma_orb(32, (214, 60, 30, 255), (255, 190, 90, 255), 225,
                          ring_color=(120, 20, 10, 255))
    draw = ImageDraw.Draw(falling)
    draw.polygon([(10, 2), (14, 2), (16, 10), (12, 10)], fill=(255, 150, 60, 140))
    build_sheet(32, 32, [rising, falling], 2, 1, f"{OUT}/LavaBall.png")


def hazard_column(base_color, glow_color, unit_h=32, units=4, width=32):
    """A vertically repeating (top/bottom seamless) hazard texture - each
    `unit_h` band alternates a darker base and a brighter glowing seam so it
    reads as a rippling column when tiled/stretched, per Scenery's own
    stretch-to-13-tiles use for white_line and the plain 32x128 use for
    lava/water."""
    h = unit_h * units
    im = Image.new("RGBA", (width, h), base_color)
    draw = ImageDraw.Draw(im)
    for u in range(units):
        y0 = u * unit_h
        # a bright horizontal seam at the TOP of each unit so it tiles
        # top-to-bottom with the unit above/below (seam sits at boundary).
        draw.rectangle((0, y0, width - 1, y0 + 2), fill=glow_color)
        mid = y0 + unit_h // 2
        draw.rectangle((2, mid - 1, width - 3, mid + 1), fill=glow_color)
    return im


def build_lava():
    im = hazard_column((120, 30, 10, 255), (255, 150, 40, 255))
    assert is_tileable_texture(im), "lava hazard column must touch all 4 edges"
    im.save(f"{OUT}/Lava.png")
    print("wrote", f"{OUT}/Lava.png", im.size)


def build_water():
    im = hazard_column((10, 45, 90, 235), (70, 180, 220, 235))
    assert is_tileable_texture(im), "water hazard column must touch all 4 edges"
    im.save(f"{OUT}/Water.png")
    print("wrote", f"{OUT}/Water.png", im.size)


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    build_fire_ball()
    build_lava_ball()
    build_lava()
    build_water()

"""
Step R.4 Task 4: The Warden (Boss) + its BossFire projectile.

Boss.java's own doc + updateLookAtMarioFrame() confirm the frame layout
(re-verified here, not trusted from the guide's own "not re-verified"
caveat): "boss" is 3 cols x 2 rows, 64x64 cells, linear index = row*3+col:
  idx0/1 (row0, col0/1) = look-left idle cycle
  idx2   (row0, col2)   = fixed "spitting fire" pose
  idx3   (row1, col0)   = unused (never set by any code path)
  idx4/5 (row1, col1/2) = look-right idle cycle

Source: Robot Master Series' miniboss1 folder (the R.0-decided source for
this asset). miniboss1_base[...] is 11 frames of 144x80 - confirmed via
getbbox() that all 11 frames share an identical content bbox (a green
tank/hovercraft with a dragon-like turret head), i.e. this sheet is a subtle
idle-detail loop, not a walk/pose cycle - so "idle A" and "idle B" are two
frames of that same loop, and a red spark (cropped from
miniboss1_laser_attacking) is composited onto the head for the fire pose to
make it visually distinct, per the class's own "spitting fire" description.

boss_fire (the drifting flame projectile, 48x16, 2 frames) is built
procedurally as a compact energy bolt - the source pack's laser/spark
material is either a flat health-bar icon (miniboss1_laser.png) or too small
an abstract streak to read clearly at 48x16, so a simple bolt (same
"energy/plasma" visual language as fire_ball/lava_ball elsewhere in this
reskin) is the more reliable choice than forcing an ambiguous crop.
"""
import os
import sys

from PIL import Image, ImageDraw

sys.path.insert(0, "C:/workspace/morsecode/docs/assets/mario-sprites/ampere-staging")
from sprite_tools import build_sheet, mirror, place_content, verified_crop

OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"
PACK = "C:/workspace/robot_series_base_pack"


def build_boss():
    print("Boss (verified crops):")
    base_sheet = Image.open(f"{PACK}/miniboss1/miniboss1_base[80height144wide].png").convert("RGBA")
    fw, fh = 144, 80
    n = base_sheet.width // fw
    assert n == 11, f"expected 11 miniboss frames, got {n}"

    def frame(i):
        return verified_crop(base_sheet, (i * fw, 0, (i + 1) * fw, fh), f"miniboss frame {i}")

    idle_a_raw = frame(0)
    idle_b_raw = frame(5)

    # Fire pose: composite a red spark (from the laser-attacking sheet) onto
    # a copy of the base pose, near the head (top-right of the source art),
    # to visually distinguish "spitting fire" from plain idle. alpha_composite
    # order is base-then-overlay (Mistake #3's fix) - the base body is the
    # first argument so only the spark's own opaque pixels are added on top.
    spark_sheet = Image.open(f"{PACK}/miniboss1/miniboss1_laser_attacking[32height112wide].png").convert("RGBA")
    spark_icon = verified_crop(spark_sheet, (81, 10, 97, 22), "boss fire spark")
    fire_raw = frame(8).copy()
    spark_big = spark_icon.resize((spark_icon.width * 3, spark_icon.height * 3), Image.NEAREST)
    paste_x, paste_y = 95, 6
    fire_raw.alpha_composite(spark_big, (paste_x, paste_y))

    cell = 64
    idle_a = place_content(idle_a_raw, cell, cell, fill=0.85, anchor="bottom")
    idle_b = place_content(idle_b_raw, cell, cell, fill=0.85, anchor="bottom")
    fire_pose = place_content(fire_raw, cell, cell, fill=0.85, anchor="bottom")

    left_a, left_b = mirror(idle_a), mirror(idle_b)

    frames_by_cell = {
        (0, 0): left_a,
        (1, 0): left_b,
        (2, 0): fire_pose,
        (0, 1): left_a,  # idx3 - unused by Boss.java, filled to avoid a blank cell
        (1, 1): idle_a,
        (2, 1): idle_b,
    }
    build_sheet(cell, cell, frames_by_cell, 3, 2, f"{OUT}/Boss.png")


def bolt_frame(length_px, cell_w=48, cell_h=16):
    """A compact horizontal energy bolt - a red/orange lozenge with a bright
    core, drawn from scratch (same procedural approach as this reskin's
    terrain/coin art) rather than forced from an ambiguous source crop."""
    im = Image.new("RGBA", (cell_w, cell_h), (0, 0, 0, 0))
    draw = ImageDraw.Draw(im)
    h = 8
    x0 = (cell_w - length_px) // 2
    y0 = (cell_h - h) // 2
    draw.ellipse((x0, y0, x0 + length_px, y0 + h), fill=(214, 60, 30, 255), outline=(120, 20, 10, 255))
    core_len = max(4, length_px - 8)
    cx0 = x0 + (length_px - core_len) // 2
    draw.ellipse((cx0, y0 + 2, cx0 + core_len, y0 + h - 2), fill=(255, 176, 90, 255))
    return im


def build_boss_fire():
    frames = [bolt_frame(30), bolt_frame(26)]
    build_sheet(48, 16, frames, 2, 1, f"{OUT}/BossFire.png")


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    build_boss()
    build_boss_fire()

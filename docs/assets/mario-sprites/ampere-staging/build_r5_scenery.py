"""
Step R.5 checklist item: small_castle/big_castle, tree, lift, bw_bouncer (the
remaining CloudsNight bw_ variants of already-built assets).

Class/AssetSpec confirmations:
  LevelLoader.sceneryRegion - "SmallCastle"/"BigCastle" are each a single
  whole-image region (AssetSpec 1x1), confirmed by opening the originals
  directly: SmallCastle.png is 160x160, BigCastle.png is 304x352 - a static
  full facade rendered at native size, not a tile grid.

  MARIO_RESKIN_JUNIOR_DEV_GUIDE.md/KENNEY_ALL_IN_ONE_INDEX.md flag RTS
  Sci-fi's small tech/factory buildings (top-right of scifi_tilesheet.png)
  as the candidate here. Opened and grid-surveyed directly this pass (not
  just visually skimmed): the sheet's building icons sit on an irregular,
  not-yet-reverse-engineered grid mixed with unrelated topdown pipe/vehicle
  art at similar y-ranges - several probe crops at guessed 96x96 boundaries
  pulled truck/vehicle icons instead of buildings. Rather than keep guessing
  boundaries (the guide's own caveat: "needs its own grid-index mapping pass
  ... not yet done"), built procedurally instead, in the same riveted-metal-
  panel language as Iron/Wall/Bouncer for visual consistency - matching this
  session's established procedural-fallback precedent (Axe, terrain, pump,
  plant) rather than forcing an unverified crop. Both facades keep the
  original's own silhouette structure (SmallCastle = one blocky tier with a
  center archway; BigCastle = three progressively-narrower stacked tiers,
  each with its own archway/window) so the level geometry reads the same,
  just re-skinned as a fortress/installation per the guide's own framing.

  Tree.java/LevelLoader.spawnTree - "tree"/"bw_tree" are 5x2 (32x32/cell);
  only indices 0/1/2 (left cap/middle/right cap, the solid canopy row) and
  index 3 (the decorative trunk column below, "GreenAndTrees" only) are ever
  read by any class found in this pass - "OrangeAndMushroom" (row 1) has no
  reachable spawn path per Tree.java's own doc. Built idx0-3 as a small
  antenna/solar-array replacing the canopy (cap segments = angled solar
  panel wings, middle = flat panel, trunk = a metal support column) and
  filled idx4/5-9 with a coherent duplicate of the same look (matching the
  "declared wider than used" precedent) rather than leaving blank cells.

  Lift.java/BalanceLiftPlatform.java/LiftFall.java/LiftCar.java - "lift" is
  a single tiny 16x16 image, tiled edge-to-edge across a platform's width
  (BalanceLiftPlatform's own doc: "stretching a small source tile would
  blur it"). Built as a small riveted metal grating tile that repeats
  cleanly.

  Bouncer.java - "bw_bouncer" is CloudsNight's recolor of the exact same
  "bouncer" silhouette (same gate.png coil segment this reskin's Bouncer/
  Spring already reuse - see build_r4_bouncer_spring.py), just re-tinted
  cooler/dimmer to match every other CloudsNight bw_ variant's own "same
  silhouette, different palette" pattern.
"""
import os
import sys

from PIL import Image, ImageDraw

sys.path.insert(0, "C:/workspace/morsecode/docs/assets/mario-sprites/ampere-staging")
from sprite_tools import build_sheet, place_content, tint, verified_crop

OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"
PACK = "C:/workspace/robot_series_base_pack"


def panel_tile(size, plate_color, edge_color, bolt_color):
    im = Image.new("RGBA", (size, size), plate_color)
    draw = ImageDraw.Draw(im)
    draw.rectangle((0, 0, size - 1, size - 1), outline=edge_color, width=1)
    draw.ellipse((2, 2, 5, 5), fill=bolt_color)
    draw.ellipse((size - 6, size - 6, size - 3, size - 3), fill=bolt_color)
    return im


def build_facade(width, height, plate_color, edge_color, bolt_color, glow_color,
                  window_rows, archway=True, crenellate_top=True):
    """Tile a riveted-panel wall across (width, height), then stamp glowing
    window slits at the given row y-fractions and an optional bottom archway
    - the same silhouette role the original brick/window/door facade played,
    just re-skinned as an installation wall."""
    unit = 32
    im = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    tile = panel_tile(unit, plate_color, edge_color, bolt_color)
    for y in range(0, height, unit):
        for x in range(0, width, unit):
            im.paste(tile.crop((0, 0, min(unit, width - x), min(unit, height - y))), (x, y))

    draw = ImageDraw.Draw(im)
    if crenellate_top:
        step = width // 8
        for i in range(0, width, step):
            if (i // step) % 2 == 0:
                draw.rectangle((i, 0, i + step - 3, 8), fill=(0, 0, 0, 0))
        # Antenna spikes on the remaining crenellations.
        for i in range(0, width, step):
            if (i // step) % 2 == 1:
                cx = i + step // 2
                draw.line((cx, 0, cx, -6), fill=edge_color, width=2)

    for row_frac in window_rows:
        wy = int(height * row_frac)
        for wx in range(unit, width - unit, unit * 2):
            draw.rectangle((wx + 6, wy, wx + unit - 6, wy + unit - 10), fill=(10, 12, 16, 255))
            draw.rectangle((wx + 10, wy + 4, wx + unit - 10, wy + unit - 16), fill=glow_color)

    if archway:
        aw = unit
        ax = width // 2 - aw // 2
        ay = height - unit * 2
        draw.rectangle((ax, ay + unit // 2, ax + aw, height), fill=(6, 8, 10, 255))
        draw.ellipse((ax, ay, ax + aw, ay + unit), fill=(6, 8, 10, 255))

    return im


def build_small_castle(out_name, plate_color, edge_color, bolt_color, glow_color):
    im = build_facade(160, 160, plate_color, edge_color, bolt_color, glow_color,
                       window_rows=(0.25, 0.55))
    im.save(f"{OUT}/{out_name}")
    print("wrote", f"{OUT}/{out_name}")


def build_big_castle(out_name, plate_color, edge_color, bolt_color, glow_color):
    im = Image.new("RGBA", (304, 352), (0, 0, 0, 0))
    base = build_facade(304, 160, plate_color, edge_color, bolt_color, glow_color,
                         window_rows=(0.2, 0.55), crenellate_top=False)
    mid = build_facade(192, 128, plate_color, edge_color, bolt_color, glow_color,
                        window_rows=(0.2,), crenellate_top=False)
    top = build_facade(96, 64, plate_color, edge_color, bolt_color, glow_color,
                        window_rows=(), archway=False)
    im.paste(base, (0, 352 - 160), base)
    im.paste(mid, (304 // 2 - 192 // 2, 352 - 160 - 128), mid)
    im.paste(top, (304 // 2 - 96 // 2, 352 - 160 - 128 - 64), top)
    im.save(f"{OUT}/{out_name}")
    print("wrote", f"{OUT}/{out_name}")


def solar_cap(mirrored, plate_color, edge_color, glow_color):
    im = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(im)
    pts = [(4, 20), (28, 8), (28, 16), (8, 26)]
    if mirrored:
        pts = [(32 - x, y) for x, y in pts]
    draw.polygon(pts, fill=plate_color, outline=edge_color)
    draw.line(pts[0] + pts[3] if False else (pts[0][0], pts[0][1], pts[3][0], pts[3][1]),
              fill=glow_color, width=2)
    return im


def solar_middle(plate_color, edge_color, glow_color):
    im = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(im)
    draw.rectangle((2, 14, 29, 24), fill=plate_color, outline=edge_color)
    draw.line((2, 19, 29, 19), fill=glow_color, width=2)
    return im


def solar_trunk(plate_color, edge_color):
    im = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(im)
    draw.rectangle((13, 0, 18, 31), fill=plate_color, outline=edge_color)
    return im


def build_tree(out_name, plate_color, edge_color, glow_color):
    left_cap = solar_cap(False, plate_color, edge_color, glow_color)
    middle = solar_middle(plate_color, edge_color, glow_color)
    right_cap = solar_cap(True, plate_color, edge_color, glow_color)
    trunk = solar_trunk(plate_color, edge_color)
    extra = middle
    row2 = [middle, trunk, middle, trunk, middle]
    frames = {(0, 0): left_cap, (1, 0): middle, (2, 0): right_cap, (3, 0): trunk, (4, 0): extra}
    for i, f in enumerate(row2):
        frames[(i, 1)] = f
    build_sheet(32, 32, frames, 5, 2, f"{OUT}/{out_name}")


def build_lift():
    im = Image.new("RGBA", (16, 16), (100, 106, 114, 255))
    draw = ImageDraw.Draw(im)
    draw.rectangle((0, 0, 15, 15), outline=(54, 60, 68, 255), width=1)
    draw.line((0, 4, 15, 4), fill=(60, 66, 74, 255))
    draw.line((0, 11, 15, 11), fill=(60, 66, 74, 255))
    im.save(f"{OUT}/Lift.png")
    print("wrote", f"{OUT}/Lift.png")


def build_bw_bouncer():
    gate = Image.open(f"{PACK}/other/gate.png").convert("RGBA")
    rest_seg = verified_crop(gate, (5 * 16, 0, 5 * 16 + 16, 32), "gate segment 5 (bw_bouncer)")
    bouncer_frame = place_content(rest_seg, 32, 32, fill=0.95, anchor="bottom")
    cool = tint(bouncer_frame, (90, 110, 130), 0.35)
    build_sheet(32, 32, [cool], 1, 1, f"{OUT}/CloudsNight/Bouncer.png")


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    os.makedirs(f"{OUT}/CloudsNight", exist_ok=True)

    day = ((84, 90, 98, 255), (54, 60, 68, 255), (40, 44, 50, 255), (150, 210, 220, 255))
    night = ((58, 64, 76, 255), (36, 40, 50, 255), (26, 30, 38, 255), (140, 160, 230, 255))

    build_small_castle("SmallCastle.png", *day)
    build_big_castle("BigCastle.png", *day)
    build_small_castle("CloudsNight/SmallCastle.png", *night)
    build_big_castle("CloudsNight/BigCastle.png", *night)

    build_tree("tree.png", (80, 130, 90, 255), (44, 70, 50, 255), (150, 230, 190, 255))
    build_tree("CloudsNight/tree.png", (70, 80, 96, 255), (40, 46, 56, 255), (140, 160, 230, 255))

    build_lift()
    build_bw_bouncer()

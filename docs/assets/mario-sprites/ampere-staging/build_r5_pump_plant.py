"""
Step R.5 checklist item: pump/pump_top (+_castle/_sea variants) and
plant/plant_dark.

Class/AssetSpec confirmations:
  Pump.java - a purely-decorative static InteractiveBrick (no HitFromDown
    override, no animation). Ground/UnderGround share plain "pump"/
    "pump_top"; Castle and Sea each get their own recolor of the exact same
    silhouette, per Pump.regionFor()'s own attribute switch. Source
    dimensions confirmed by opening the original art directly: pump.png/
    pump Castle.png/pump Sea.png are all 64x32 (2 tiles wide, 1 tall - the
    pipe BODY, a repeatable straight segment); pump top*.png are all 64x64
    (2x2 tiles - the rounded CAP that sits on top of a stack of body
    segments). AssetSpec declares all six as 1x1 (the whole image is one
    region, not a sub-grid), so build_sheet isn't used here - each is saved
    directly as its full-size single frame.

  PiranhaPlant.java - a 2-frame open/closed biting-mouth animation
    (`showingFirstFrame` toggling every 0.3s via setFrame(0/1)), rising out
    of its own pump/pipe on a fixed travel range. AssetSpec declares
    plant/plant_dark as 2x1 (64x48, i.e. two 32x48 cells).

  MARIO_RESKIN_JUNIOR_DEV_GUIDE.md flags RTS Sci-fi's tilesheet as the
  "documented candidate" for pump art, but its winding pipe-network art
  (confirmed by opening scifi_tilesheet.png directly) is topdown-perspective
  organic terrain paths with no straight rectangular segment - it doesn't
  crop into a side-view 64x32 vertical pipe body without visibly breaking
  the art's own perspective. Built procedurally instead, in the same
  riveted-metal-panel visual family as Wall/BridgeBloks/WoodenBridge (Step
  R.5 structural batch) for consistency, matching the guide's own
  established procedural-fallback precedent (Axe, terrain) rather than
  forcing a bad crop.

  plant/plant_dark: guide flags this "no pack match found... AI-gen
  candidate" with no AI-gen tool available in this environment. Built
  procedurally as a small mechanical sentry head (glowing eye + a pair of
  articulated claw pincers) that opens/closes for its 2 frames - keeps the
  "hostile thing rising out of a pipe to bite" read PiranhaPlant.java
  requires, in Ampere's robot-enemy visual language rather than an organic
  plant.
"""
import os
import sys

from PIL import Image, ImageDraw

sys.path.insert(0, "C:/workspace/morsecode/docs/assets/mario-sprites/ampere-staging")
from sprite_tools import build_sheet, is_tileable_texture

OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"


def panel_strip(w, h, plate_color, bolt_color, edge_color, rivet_rows):
    im = Image.new("RGBA", (w, h), plate_color)
    draw = ImageDraw.Draw(im)
    draw.rectangle((0, 0, w - 1, h - 1), outline=edge_color, width=2)
    # A vertical seam down the middle (two tiles wide - reads as a pipe, not
    # a flat wall).
    draw.line((w // 2, 0, w // 2, h - 1), fill=edge_color, width=2)
    bolt_r = 2
    for ry in rivet_rows:
        for bx in (6, w // 2 + 6, w - 7):
            draw.ellipse((bx - bolt_r, ry - bolt_r, bx + bolt_r, ry + bolt_r), fill=bolt_color)
    return im


def build_pump_pair(suffix, plate_color, bolt_color, edge_color, glow_color):
    # Body: a straight repeatable pipe segment, 64x32.
    body = panel_strip(64, 32, plate_color, bolt_color, edge_color, rivet_rows=(9, 23))
    assert is_tileable_texture(body), f"pump{suffix} body must tile top/bottom"
    body.save(f"{OUT}/pump{suffix}.png")
    print("wrote", f"{OUT}/pump{suffix}.png")

    # Cap: rounded valve top, 64x64 - same plate/rivet language, plus a
    # domed valve hub with a glowing vent ring on top.
    top = panel_strip(64, 64, plate_color, bolt_color, edge_color, rivet_rows=(50,))
    draw = ImageDraw.Draw(top)
    draw.ellipse((12, 6, 51, 40), fill=edge_color, outline=bolt_color, width=2)
    draw.ellipse((22, 14, 41, 30), fill=glow_color)
    draw.ellipse((28, 18, 35, 24), fill=(255, 255, 255, 220))
    top.save(f"{OUT}/pump top{suffix}.png".replace(" ", " "))
    print("wrote", f"{OUT}/pump top{suffix}.png")


def build_pumps():
    # Ground/UnderGround plain look - cyan glow, matches Wall.png's accent.
    build_pump_pair("", (74, 82, 92, 255), (48, 54, 62, 255), (54, 60, 68, 255), (150, 210, 220, 255))
    # Castle - warm orange glow, matches BridgeBloks/lava's palette.
    build_pump_pair(" Castle", (70, 58, 50, 255), (44, 34, 28, 255), (54, 42, 34, 255), (235, 150, 70, 255))
    # Sea - cool blue glow, matches Water.png's palette.
    build_pump_pair(" Sea", (40, 56, 78, 255), (26, 38, 54, 255), (30, 44, 62, 255), (90, 190, 230, 255))


def sentry_head(open_mouth):
    """A small mechanical sentry head - closed (idle) or open (biting) claw
    pincers around a glowing eye, rising out of its pipe."""
    im = Image.new("RGBA", (32, 48), (0, 0, 0, 0))
    draw = ImageDraw.Draw(im)
    body_color = (90, 96, 104, 255)
    edge_color = (54, 60, 68, 255)
    eye_color = (230, 60, 50, 255)
    # Neck stalk (bottom half, stays inside its pipe most of the time).
    draw.rectangle((13, 24, 18, 47), fill=body_color, outline=edge_color)
    # Head dome.
    draw.ellipse((6, 6, 25, 28), fill=body_color, outline=edge_color, width=2)
    # Eye.
    draw.ellipse((12, 13, 19, 20), fill=eye_color)
    draw.ellipse((14, 15, 16, 17), fill=(255, 255, 255, 220))
    # Pincers either splayed open (biting) or closed together (idle).
    if open_mouth:
        draw.polygon([(6, 20), (0, 8), (4, 4), (11, 16)], fill=body_color, outline=edge_color)
        draw.polygon([(25, 20), (31, 8), (27, 4), (20, 16)], fill=body_color, outline=edge_color)
    else:
        draw.polygon([(6, 24), (2, 20), (8, 16), (12, 22)], fill=body_color, outline=edge_color)
        draw.polygon([(25, 24), (29, 20), (23, 16), (19, 22)], fill=body_color, outline=edge_color)
    return im


def build_plants():
    closed = sentry_head(False)
    opened = sentry_head(True)
    build_sheet(32, 48, [closed, opened], 2, 1, f"{OUT}/plant.png")

    dark_closed = sentry_head(False)
    dark_opened = sentry_head(True)
    for frame in (dark_closed, dark_opened):
        px = frame.load()
        for y in range(frame.height):
            for x in range(frame.width):
                r, g, b, a = px[x, y]
                if a:
                    px[x, y] = (int(r * 0.55), int(g * 0.7), int(b * 0.85), a)
    build_sheet(32, 48, [dark_closed, dark_opened], 2, 1, f"{OUT}/plantdark.png")


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    build_pumps()
    build_plants()

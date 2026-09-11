"""
Step R.4 Task 2: QuestionMark / Bank family.

QuestionMark.java's own region lookup (read directly, not assumed):
  regionFor(attribute) -> "question_mark_grey" for UnderGround/Castle,
                          "question_mark" otherwise (Ground/Sea).
Both are 3-frame idle-bob sheets (QuestionMark.java's own
IDLE_FRAMES = {0, 0, 1, 2, 1, 0} cycles those 3 unique frames: rest, mid,
peak brightness/highlight - not a vertical bob, since the Java class never
offsets Y for the idle animation, only cycles frame index).

IMPORTANT, resolved during this task (was flagged ambiguous in the junior
guide): Bank.java and BankWithItem.java do NOT use "question_mark" at all -
both call `MarioResourceManager.themedRegion("brick", attribute)`, i.e. the
plain themed brick art already built in build_terrain_and_common.py. So this
task only needs to build the two question_mark region files; Bank/BankWithItem
need no new art.

Built procedurally (beveled panel + glowing "?" core), per the guide's own
Task 2 suggestion, rather than sourced from a pack - no clean "?" glyph
source exists in either robot pack or the Kenney bundle, and a hand-built
glyph reads better than forcing a mismatched icon.
"""
import os
import sys

from PIL import Image, ImageDraw

sys.path.insert(0, "C:/workspace/morsecode/docs/assets/mario-sprites/ampere-staging")
from sprite_tools import build_sheet, glow_pulse

OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"

# (panel_base, panel_dark, panel_light, glyph_core)
NORMAL = ((186, 140, 40), (92, 66, 16), (232, 190, 96), (255, 224, 130))
GREY = ((120, 120, 128), (58, 58, 64), (176, 176, 184), (214, 214, 224))

# A 5x7 bitmap "?" glyph (1 = lit pixel), scaled up 3x when pasted.
GLYPH_BITS = [
    "01110",
    "10001",
    "10001",
    "00010",
    "00100",
    "00000",
    "00100",
]


def rect(draw_img, x0, y0, x1, y1, color):
    for y in range(y0, y1):
        for x in range(x0, x1):
            draw_img.putpixel((x, y), color + (255,))


def panel_base(base, dark, light):
    im = Image.new("RGBA", (32, 32), base + (255,))
    rect(im, 0, 0, 32, 2, dark)
    rect(im, 0, 30, 32, 32, dark)
    rect(im, 0, 0, 2, 32, dark)
    rect(im, 30, 0, 32, 32, dark)
    rect(im, 2, 2, 30, 4, light)
    rect(im, 2, 2, 4, 30, light)
    return im


def paste_glyph(panel, core_color, scale=3):
    glyph_w, glyph_h = len(GLYPH_BITS[0]) * scale, len(GLYPH_BITS) * scale
    x0 = (32 - glyph_w) // 2
    y0 = (32 - glyph_h) // 2
    draw = ImageDraw.Draw(panel)
    for row, bits in enumerate(GLYPH_BITS):
        for col, bit in enumerate(bits):
            if bit == "1":
                px0 = x0 + col * scale
                py0 = y0 + row * scale
                draw.rectangle((px0, py0, px0 + scale - 1, py0 + scale - 1),
                                fill=core_color + (255,))
    return panel


def build_variant(colors, out_name):
    base, dark, light, core = colors
    rest = paste_glyph(panel_base(base, dark, light), core)
    # 3 unique frames matching IDLE_FRAMES' rest/mid/peak semantics - a
    # brightening highlight pulse across the whole panel, not a duplicate.
    frames = [rest, glow_pulse(rest, 0.18), glow_pulse(rest, 0.4)]
    build_sheet(32, 32, frames, 3, 1, f"{OUT}/{out_name}")


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    build_variant(NORMAL, "QuestionMark.png")
    build_variant(GREY, "QuestionMarkGrey.png")

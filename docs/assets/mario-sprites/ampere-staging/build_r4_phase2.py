"""
Step R.4 Phase 2.

Currently covers:
- Task 1: Iron / the used-up block.
- Task 2: QuestionMark / QuestionMarkGrey.

Per MARIO_RESKIN_JUNIOR_DEV_GUIDE.md:
- ART_SCALE stays 1.
- Import sprite_tools; do not re-implement shared crop/scale/tint helpers.
"""
from PIL import Image, ImageDraw
import os
import sys

sys.path.insert(0, "C:/workspace/morsecode/docs/assets/mario-sprites/ampere-staging")
from sprite_tools import build_sheet, tint

OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"

IRON_FRAMES = [
    ("Sea", "stone_Sea.png", (127, 224, 232)),
    ("Ground", "stone.png", (199, 210, 219)),
    ("UnderGround", "stone_UnderGround.png", (53, 208, 160)),
    ("Castle", "stone_Castle.png", (217, 140, 74)),
]

QUESTION_PALETTES = {
    "question_mark": {
        "base": (194, 132, 34),
        "dark": (102, 64, 16),
        "light": (255, 220, 120),
        "glow": (255, 244, 170),
        "glyph": (74, 42, 8),
    },
    "question_mark_grey": {
        "base": (122, 130, 142),
        "dark": (60, 68, 78),
        "light": (200, 212, 224),
        "glow": (232, 240, 248),
        "glyph": (52, 56, 64),
    },
}

QUESTION_GLYPH = [
    ".####.",
    "##..##",
    "....##",
    "...##.",
    "..##..",
    "..##..",
    "......",
    "..##..",
    "..##..",
]


def clamp(v):
    return max(0, min(255, int(v)))


def darken(color, factor):
    return tuple(clamp(c * factor) for c in color)


def lighten(color, amount):
    return tuple(clamp(c + (255 - c) * amount) for c in color)


def load_base(name):
    path = f"{OUT}/{name}"
    im = Image.open(path).convert("RGBA")
    assert im.size == (32, 32), f"Expected 32x32 base tile for {name}, got {im.size}"
    return im


def make_iron(base, accent):
    frame = tint(base, (160, 172, 184), 0.14)
    draw = ImageDraw.Draw(frame, "RGBA")

    outer_dark = darken(accent, 0.45)
    outer_light = lighten(accent, 0.12)
    plate_fill = darken(accent, 0.72)
    plate_shadow = darken(accent, 0.35)
    rivet_fill = lighten(accent, 0.28)

    draw.rectangle((3, 3, 28, 28), outline=outer_dark + (255,), width=1)
    draw.line((4, 4, 27, 4), fill=outer_light + (220,), width=1)
    draw.line((4, 4, 4, 27), fill=outer_light + (220,), width=1)

    draw.rectangle((7, 7, 24, 24), fill=plate_fill + (120,), outline=plate_shadow + (220,), width=1)
    draw.line((8, 8, 23, 8), fill=outer_light + (200,), width=1)
    draw.line((8, 8, 8, 23), fill=outer_light + (200,), width=1)

    draw.rectangle((11, 15, 20, 16), fill=plate_shadow + (235,))
    draw.rectangle((15, 11, 16, 20), fill=plate_shadow + (235,))

    for x, y in ((9, 9), (22, 9), (9, 22), (22, 22)):
        draw.rectangle((x - 1, y - 1, x + 1, y + 1), fill=rivet_fill + (255,), outline=outer_dark + (255,))

    return frame


QUESTION_PULSE = [0.10, 0.20, 0.34]
QUESTION_LIFT = [0, -1, -2]
QUESTION_PANEL_SHIFT = [0, -1, -1]


def draw_question_glyph(draw, origin_x, origin_y, color):
    pixel = 2
    for row, pattern in enumerate(QUESTION_GLYPH):
        for col, ch in enumerate(pattern):
            if ch != "#":
                continue
            x0 = origin_x + col * pixel
            y0 = origin_y + row * pixel
            draw.rectangle((x0, y0, x0 + pixel - 1, y0 + pixel - 1), fill=color + (255,))


def make_question_frame(palette, frame_index):
    base = palette["base"]
    dark = palette["dark"]
    light = palette["light"]
    glow = palette["glow"]
    glyph = palette["glyph"]

    pulse = QUESTION_PULSE[frame_index]
    lift = QUESTION_LIFT[frame_index]
    panel_shift = QUESTION_PANEL_SHIFT[frame_index]

    frame = Image.new("RGBA", (32, 32), base + (255,))
    draw = ImageDraw.Draw(frame, "RGBA")

    outer_dark = darken(dark, 0.82)
    inner_fill = darken(base, 0.82)
    inner_glow = lighten(glow, pulse)
    inner_shadow = darken(dark, 0.58)
    bolt = lighten(light, 0.10 + pulse * 0.25)

    draw.rectangle((0, 0, 31, 31), outline=outer_dark + (255,), width=1)
    draw.line((1, 1, 30, 1), fill=lighten(light, 0.10) + (255,), width=1)
    draw.line((1, 1, 1, 30), fill=lighten(light, 0.10) + (255,), width=1)

    draw.rectangle((4, 4 + panel_shift, 27, 27 + panel_shift), fill=inner_fill + (255,), outline=inner_shadow + (255,), width=1)
    draw.line((5, 5 + panel_shift, 26, 5 + panel_shift), fill=inner_glow + (235,), width=1)
    draw.line((5, 5 + panel_shift, 5, 26 + panel_shift), fill=inner_glow + (235,), width=1)

    draw.rectangle((9, 9 + panel_shift, 22, 22 + panel_shift), fill=lighten(base, 0.08 + pulse * 0.18) + (180,), outline=inner_shadow + (210,), width=1)

    for x, y in ((7, 7), (24, 7), (7, 24), (24, 24)):
        draw.rectangle((x - 1, y - 1 + panel_shift, x + 1, y + 1 + panel_shift), fill=bolt + (255,), outline=outer_dark + (255,))

    draw_question_glyph(draw, 10, 7 + lift, glyph)
    return frame


def build_iron():
    sheet_frames = []
    for label, file_name, accent in IRON_FRAMES:
        frame = make_iron(load_base(file_name), accent)
        sheet_frames.append(frame)
        print(f"built Iron frame for {label} from {file_name}")
    build_sheet(32, 32, sheet_frames, 4, 1, f"{OUT}/Iron.png")


def build_question_marks():
    for region_name, palette in QUESTION_PALETTES.items():
        frames = [make_question_frame(palette, i) for i in range(3)]
        build_sheet(32, 32, frames, 3, 1, f"{OUT}/{'QuestionMark.png' if region_name == 'question_mark' else 'QuestionMarkGrey.png'}")
        print(f"built {region_name} 3-frame idle strip")


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    build_iron()
    build_question_marks()

"""
Step R.4 Task 5: Spare chassis (1-Up).

Life.java's own region lookup is simple: region("one_up"), 32x32, 2 frames
(2x1) - a plain nextFrame() wobble while it walks/falls, same animation
shape as Mushroom, no left/right frame split (confirmed: no setFrame() call
anywhere in Life.java besides the base class's nextFrame()).

Per KENNEY_ALL_IN_ONE_INDEX.md Sec2/Sec12.2: no clean thematic match exists in
either robot pack or the Kenney bundle for a 1-Up analog - the documented
pragmatic fallback is Robot Platform Pack's heart icon, but the guide itself
recommends hand-pixeling a small chassis/head icon instead, since it fits
the mechanical identity better and is a single small sprite either way.
Built procedurally here (same rect()-based technique as this reskin's
terrain/QuestionMark art) rather than using the heart fallback.
"""
import os
import sys

from PIL import Image, ImageDraw

sys.path.insert(0, "C:/workspace/morsecode/docs/assets/mario-sprites/ampere-staging")
from sprite_tools import build_sheet, glow_pulse

OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"

CHASSIS_BASE = (196, 201, 209)
CHASSIS_DARK = (108, 113, 122)
CHASSIS_LIGHT = (232, 236, 240)
LIFE_CORE = (120, 235, 150)


def rect(draw_img, x0, y0, x1, y1, color):
    for y in range(y0, y1):
        for x in range(x0, x1):
            draw_img.putpixel((x, y), color + (255,))


def chassis_frame():
    """A small detached robot head/chassis - a domed housing with a single
    glowing green "spare life" core, standing in for the discarded heart
    icon fallback."""
    im = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(im)
    # Domed housing.
    draw.ellipse((6, 6, 25, 24), fill=CHASSIS_BASE + (255,), outline=CHASSIS_DARK + (255,))
    draw.ellipse((8, 8, 23, 16), fill=CHASSIS_LIGHT + (255,))
    # Two small side antennae/mounts, reads as "detached chassis" not "full robot".
    rect(im, 4, 20, 7, 24, CHASSIS_DARK)
    rect(im, 24, 20, 27, 24, CHASSIS_DARK)
    # Glowing core (the "extra life" indicator).
    draw.ellipse((12, 13, 19, 20), fill=LIFE_CORE + (255,), outline=(40, 110, 60, 255))
    return im


def build_one_up():
    rest = chassis_frame()
    pulsed = glow_pulse(rest, 0.25)
    build_sheet(32, 32, [rest, pulsed], 2, 1, f"{OUT}/1UP.png")


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    build_one_up()

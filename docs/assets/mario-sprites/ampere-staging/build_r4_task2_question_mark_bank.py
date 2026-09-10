from PIL import Image, ImageDraw
import sys

sys.path.insert(0, "C:/workspace/morsecode/docs/assets/mario-sprites/ampere-staging")
from sprite_tools import build_sheet

OUT_DIR = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"


def draw_q_block(base, dark, light, glyph, glow, bob_px):
    frame = Image.new("RGBA", (32, 32), base + (255,))
    draw = ImageDraw.Draw(frame)

    # Beveled frame.
    draw.rectangle((0, 0, 31, 31), outline=dark + (255,), width=1)
    draw.rectangle((2, 2, 29, 29), outline=light + (180,), width=1)
    draw.rectangle((4, 4, 27, 27), fill=tuple(min(255, c + 8) for c in base) + (255,))

    # Corner bolts.
    for cx, cy in ((6, 6), (25, 6), (6, 25), (25, 25)):
        draw.rectangle((cx - 1, cy - 1, cx + 1, cy + 1), fill=dark + (255,))
        draw.point((cx, cy), fill=light + (255,))

    # Question-glyph with a tiny bob/pulse across frames.
    y0 = 8 + bob_px
    x0 = 10
    pts = [
        (x0 + 0, y0 + 0), (x0 + 4, y0 + 0), (x0 + 7, y0 + 2), (x0 + 7, y0 + 5),
        (x0 + 5, y0 + 7), (x0 + 4, y0 + 8), (x0 + 4, y0 + 10), (x0 + 2, y0 + 10),
        (x0 + 2, y0 + 7), (x0 + 3, y0 + 6), (x0 + 5, y0 + 4), (x0 + 5, y0 + 3),
        (x0 + 4, y0 + 2), (x0 + 0, y0 + 2),
    ]
    draw.polygon(pts, fill=glyph + (255,))
    draw.rectangle((x0 + 2, y0 + 13, x0 + 4, y0 + 15), fill=glyph + (255,))

    # Animated sheen (frame 0 -> 1 -> 2 grows then falls via QuestionMark.IDLE_FRAMES).
    draw.rectangle((7, 6 + bob_px, 13, 8 + bob_px), fill=glow + (120,))
    draw.rectangle((15, 10 + bob_px, 18, 11 + bob_px), fill=glow + (140,))
    return frame


def make_sheet(path, palette):
    base, dark, light, glyph = palette
    frames = [
        draw_q_block(base, dark, light, glyph, light, 0),
        draw_q_block(tuple(min(255, c + 10) for c in base), dark, light, glyph, light, -1),
        draw_q_block(tuple(min(255, c + 20) for c in base), dark, light, glyph, tuple(min(255, c + 24) for c in light), -2),
    ]
    build_sheet(32, 32, frames, 3, 1, path)


if __name__ == "__main__":
    make_sheet(
        f"{OUT_DIR}/QuestionMark.png",
        ((171, 110, 38), (82, 48, 12), (240, 194, 105), (47, 27, 8)),
    )
    make_sheet(
        f"{OUT_DIR}/QuestionMarkGrey.png",
        ((110, 114, 124), (50, 54, 62), (175, 180, 193), (26, 29, 34)),
    )

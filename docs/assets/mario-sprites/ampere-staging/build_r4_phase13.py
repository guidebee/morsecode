"""
Step R.4 Phase 13.

Covers:
- bridge_blocks (BridgeBloks.png) - 1x1
- axe (Axe.png) - 4x1
- rocket_launcher (RocketLauncher.png) - 1x4
- bouncer (Bouncer.png) - 1x1
- spring (Spring.png) - 3x1, 32x64 frames
- wooden_bridge (WoodenBridge.png) - 1x1
- white_line (WhiteLine.png) - 1x1
- chain (Chain.png) - 4x1
- rope (Rope.png) - 1x1
"""
from PIL import Image, ImageDraw
import os

OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"


def new_cell(w=32, h=32):
    return Image.new("RGBA", (w, h), (0, 0, 0, 0))


def build_bridge_blocks():
    frame = new_cell()
    draw = ImageDraw.Draw(frame, "RGBA")
    draw.rectangle((2, 2, 29, 29), fill=(120, 110, 100, 255), outline=(50, 45, 40, 255))
    draw.rectangle((6, 6, 25, 25), outline=(80, 70, 60, 255))
    draw.line((4, 10, 27, 10), fill=(170, 80, 40, 255), width=2)
    draw.line((4, 22, 27, 22), fill=(170, 80, 40, 255), width=2)
    frame.save(f"{OUT}/BridgeBloks.png")
    print("wrote", f"{OUT}/BridgeBloks.png")


def draw_axe(frame, phase=0):
    draw = ImageDraw.Draw(frame, "RGBA")
    glow = [(190, 80, 40), (210, 120, 50), (230, 170, 60), (210, 120, 50)][phase]
    draw.rectangle((14, 6, 18, 26), fill=(90, 60, 40, 255))
    draw.rectangle((10, 8, 22, 14), fill=glow + (255,), outline=(60, 40, 30, 255))
    draw.rectangle((9, 9, 11, 13), fill=(200, 200, 200, 255))
    draw.rectangle((21, 9, 23, 13), fill=(200, 200, 200, 255))


def build_axe():
    frames = []
    for phase in range(4):
        frame = new_cell()
        draw_axe(frame, phase)
        frames.append(frame)
    sheet = Image.new("RGBA", (32 * 4, 32), (0, 0, 0, 0))
    for i, frame in enumerate(frames):
        sheet.paste(frame, (i * 32, 0), frame)
    sheet.save(f"{OUT}/Axe.png")
    print("wrote", f"{OUT}/Axe.png")


def draw_launcher_frame(kind):
    frame = new_cell()
    draw = ImageDraw.Draw(frame, "RGBA")
    draw.rectangle((6, 4, 26, 28), fill=(60, 80, 100, 255), outline=(30, 45, 60, 255))
    if kind == "head":
        draw.rectangle((2, 10, 14, 16), fill=(80, 110, 130, 255), outline=(30, 45, 60, 255))
        draw.rectangle((10, 13, 14, 13), fill=(255, 90, 70, 255))
    elif kind == "mid":
        draw.rectangle((8, 8, 24, 24), fill=(70, 95, 120, 255))
    elif kind == "base":
        draw.rectangle((8, 18, 24, 26), fill=(90, 120, 150, 255))
    return frame


def build_rocket_launcher():
    frames = [
        draw_launcher_frame("head"),
        draw_launcher_frame("mid"),
        draw_launcher_frame("mid"),
        draw_launcher_frame("base"),
    ]
    sheet = Image.new("RGBA", (32, 32 * 4), (0, 0, 0, 0))
    for i, frame in enumerate(frames):
        sheet.paste(frame, (0, i * 32), frame)
    sheet.save(f"{OUT}/RocketLauncher.png")
    print("wrote", f"{OUT}/RocketLauncher.png")


def build_bouncer():
    frame = new_cell()
    draw = ImageDraw.Draw(frame, "RGBA")
    draw.rectangle((4, 12, 27, 27), fill=(80, 120, 140, 255), outline=(40, 60, 80, 255))
    draw.rectangle((6, 16, 25, 24), fill=(120, 180, 200, 255))
    frame.save(f"{OUT}/Bouncer.png")
    print("wrote", f"{OUT}/Bouncer.png")


def draw_spring_frame(height):
    frame = new_cell(32, 64)
    draw = ImageDraw.Draw(frame, "RGBA")
    top = 8 + (24 - height)
    bottom = top + height
    draw.rectangle((8, top, 24, bottom), outline=(50, 80, 90, 255))
    for y in range(top + 2, bottom - 1, 4):
        draw.line((10, y, 22, y), fill=(120, 170, 190, 255), width=2)
    draw.rectangle((6, bottom, 26, bottom + 6), fill=(70, 100, 120, 255))
    return frame


def build_spring():
    heights = [24, 14, 18]
    frames = [draw_spring_frame(h) for h in heights]
    sheet = Image.new("RGBA", (32 * 3, 64), (0, 0, 0, 0))
    for i, frame in enumerate(frames):
        sheet.paste(frame, (i * 32, 0), frame)
    sheet.save(f"{OUT}/Spring.png")
    print("wrote", f"{OUT}/Spring.png")


def build_wooden_bridge():
    frame = new_cell()
    draw = ImageDraw.Draw(frame, "RGBA")
    draw.rectangle((2, 10, 29, 22), fill=(130, 90, 60, 255), outline=(80, 55, 35, 255))
    for x in range(4, 28, 6):
        draw.line((x, 11, x, 21), fill=(100, 70, 50, 255), width=1)
    frame.save(f"{OUT}/WoodenBridge.png")
    print("wrote", f"{OUT}/WoodenBridge.png")


def build_white_line():
    frame = new_cell()
    draw = ImageDraw.Draw(frame, "RGBA")
    draw.line((16, 0, 16, 31), fill=(235, 240, 245, 255), width=2)
    frame.save(f"{OUT}/WhiteLine.png")
    print("wrote", f"{OUT}/WhiteLine.png")


def build_chain():
    frames = []
    for offset in (0, 1, 0, -1):
        frame = new_cell()
        draw = ImageDraw.Draw(frame, "RGBA")
        for y in range(4, 28, 8):
            draw.ellipse((12 + offset, y, 20 + offset, y + 6), outline=(90, 110, 130, 255), width=2)
        frames.append(frame)
    sheet = Image.new("RGBA", (32 * 4, 32), (0, 0, 0, 0))
    for i, frame in enumerate(frames):
        sheet.paste(frame, (i * 32, 0), frame)
    sheet.save(f"{OUT}/Chain.png")
    print("wrote", f"{OUT}/Chain.png")


def build_rope():
    frame = new_cell()
    draw = ImageDraw.Draw(frame, "RGBA")
    draw.line((16, 0, 16, 31), fill=(150, 120, 90, 255), width=2)
    for y in range(2, 30, 6):
        draw.line((14, y, 18, y + 2), fill=(120, 90, 70, 255), width=1)
    frame.save(f"{OUT}/Rope.png")
    print("wrote", f"{OUT}/Rope.png")


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    build_bridge_blocks()
    build_axe()
    build_rocket_launcher()
    build_bouncer()
    build_spring()
    build_wooden_bridge()
    build_white_line()
    build_chain()
    build_rope()

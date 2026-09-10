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

Mapped-source update (2026-09-10):
- Bouncer/Spring now use Robot Master Series `other/gate.png` coil segments
  (per KENNEY_ALL_IN_ONE_INDEX.md §3) instead of purely procedural drawing.
"""
from PIL import Image, ImageDraw
import os
import sys

sys.path.insert(0, "C:/workspace/morsecode/docs/assets/mario-sprites/ampere-staging")
from sprite_tools import build_sheet, place_content, verified_crop

OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"
RTS_TILESET = "C:/workspace/Kenney_Game_Assets_All/2D assets/RTS Sci-fi/Tilesheet/scifi_tilesheet.png"
ROBOT_GATE = "C:/workspace/robot_series_base_pack/other/gate.png"


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
    return None


def build_rocket_launcher():
    tilesheet = Image.open(RTS_TILESET).convert("RGBA")
    cell = 32
    def tile(col, row):
        return tilesheet.crop((col * cell, row * cell, (col + 1) * cell, (row + 1) * cell))

    frames = [
        tile(30, 0),
        tile(30, 1),
        tile(31, 1),
        tile(32, 1),
    ]
    sheet = Image.new("RGBA", (32, 32 * 4), (0, 0, 0, 0))
    for i, frame in enumerate(frames):
        sheet.paste(frame, (0, i * 32), frame)
    sheet.save(f"{OUT}/RocketLauncher.png")
    print("wrote", f"{OUT}/RocketLauncher.png")


def build_bouncer():
    gate = Image.open(ROBOT_GATE).convert("RGBA")
    coil = verified_crop(gate, (88, 0, 120, 32), "bouncer_coil")
    frame = place_content(coil, 32, 32, fill=0.95, anchor="center")
    frame.save(f"{OUT}/Bouncer.png")
    print("wrote", f"{OUT}/Bouncer.png")


def draw_spring_frame(height):
    gate = Image.open(ROBOT_GATE).convert("RGBA")
    coil = verified_crop(gate, (88, 0, 120, 32), f"spring_coil_h{height}")
    frame = new_cell(32, 64)
    scaled = coil.resize((20, max(8, height)), Image.NEAREST)
    x = (32 - scaled.width) // 2
    y = 58 - scaled.height
    frame.paste(scaled, (x, y), scaled)
    base = coil.resize((24, 6), Image.NEAREST)
    frame.paste(base, (4, 58), base)
    return frame


def build_spring():
    heights = [24, 14, 18]
    frames = [draw_spring_frame(h) for h in heights]
    build_sheet(32, 64, frames, 3, 1, f"{OUT}/Spring.png")
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

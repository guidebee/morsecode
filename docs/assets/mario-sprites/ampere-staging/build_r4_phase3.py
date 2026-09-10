"""
Step R.4 Phase 3.

Covers:
- Task 3: Helmet + HelmetShell (Plater family).
- Task 4: Boss + BossFire (The Warden).
- Task 5: 1UP (Spare chassis fallback from Robot Platform Pack).
"""
from PIL import Image
import os
import sys

sys.path.insert(0, "C:/workspace/morsecode/docs/assets/mario-sprites/ampere-staging")
from sprite_tools import verified_crop, place_content, tint, mirror, glow_pulse, build_sheet

PACK = "C:/workspace/robot_series_base_pack"
PLATFORM_PACK = "C:/workspace/Robot_Platform_Pack"
OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"

HELMET_TINTS = {
    "helmet": None,
    "helmet_dark": ((70, 86, 100), 0.38),
    "helmet_white": ((220, 230, 240), 0.28),
}


def build_helmet():
    source = Image.open(f"{PACK}/enemy3/enemy3all.png").convert("RGBA")
    cell = 32
    row = 2

    right_raw = [
        verified_crop(source, (cell * i, cell * row, cell * (i + 1), cell * (row + 1)), f"helmet_walk{i}")
        for i in (1, 2)
    ]
    right_frames = [place_content(frame, cell, cell, fill=0.9, anchor="bottom") for frame in right_raw]
    left_frames = [mirror(frame) for frame in right_frames]

    frames = [left_frames[0], left_frames[1], right_frames[0], right_frames[1]]

    for name, tint_info in HELMET_TINTS.items():
        if tint_info is None:
            themed = frames
        else:
            color, strength = tint_info
            themed = [tint(frame, color, strength) for frame in frames]
        build_sheet(cell, cell, themed, 4, 1, f"{OUT}/{name}.png")

    shell_base = place_content(right_raw[0], cell, cell, fill=0.8, anchor="bottom")
    for name, tint_info in HELMET_TINTS.items():
        if name == "helmet":
            target = "helmet_shell"
        elif name == "helmet_dark":
            target = "helmet_shell_dark"
        else:
            target = "helmet_shell_white"
        if tint_info is None:
            shell_frame = shell_base
        else:
            color, strength = tint_info
            shell_frame = tint(shell_base, color, strength)
        shell_frame.save(f"{OUT}/{target}.png")
        print("wrote", f"{OUT}/{target}.png")


def build_boss():
    source = Image.open(f"{PACK}/miniboss1/miniboss1_base[80height144wide].png").convert("RGBA")
    frame_w, frame_h = 144, 80

    raw = [
        verified_crop(source, (frame_w * i, 0, frame_w * (i + 1), frame_h), f"boss_base{i}")
        for i in (0, 1, 2)
    ]

    right_idle0 = place_content(raw[0], 64, 64, fill=0.9, anchor="bottom")
    right_idle1 = place_content(raw[1], 64, 64, fill=0.9, anchor="bottom")
    right_fire = place_content(raw[2], 64, 64, fill=0.9, anchor="bottom")

    left_idle0 = mirror(right_idle0)
    left_idle1 = mirror(right_idle1)
    left_fire = mirror(right_fire)

    frames = [
        left_idle0,
        left_idle1,
        left_fire,
        right_idle0,
        right_idle0,
        right_idle1,
    ]
    build_sheet(64, 64, frames, 3, 2, f"{OUT}/Boss.png")


def build_boss_fire():
    base = Image.open(f"{PACK}/miniboss1/miniboss1_laser.png").convert("RGBA")
    base_frame = place_content(base, 48, 16, fill=0.9, anchor="center")
    frames = [base_frame, glow_pulse(base_frame, 0.35)]
    build_sheet(48, 16, frames, 2, 1, f"{OUT}/BossFire.png")


def build_one_up():
    sheet = Image.open(f"{PLATFORM_PACK}/Tileset&Items.png").convert("RGBA")
    cell = 32
    cols, rows = sheet.width // cell, sheet.height // cell
    candidates = []
    for row in range(rows):
        for col in range(cols):
            crop = sheet.crop((col * cell, row * cell, (col + 1) * cell, (row + 1) * cell))
            pixels = list(crop.getdata())
            red = sum(1 for r, g, b, a in pixels if a > 0 and r > 200 and g < 100 and b < 100)
            alpha = sum(1 for _, _, _, a in pixels if a > 0)
            if red > 20:
                candidates.append((red, alpha, col, row, crop))
    candidates.sort(reverse=True)
    if len(candidates) < 2:
        raise ValueError("Unable to locate two red-heart cells in Tileset&Items.png")

    frames = []
    for red, alpha, col, row, crop in candidates[:2]:
        print(f"picked heart cell col={col} row={row} redPixels={red} alphaPixels={alpha}")
        frame = place_content(crop, 32, 32, fill=0.9, anchor="center")
        frames.append(frame)
    build_sheet(32, 32, frames, 2, 1, f"{OUT}/1UP.png")


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    build_helmet()
    build_boss()
    build_boss_fire()
    build_one_up()

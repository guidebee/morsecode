"""
Step R.4 Phase 12.

Covers:
- SonOfABuitch (son_of_a_buitch) - 2x1, 32x32 frames.
- SpikeyEgg (spikey_egg) - 2x1, 32x32 frames.
- Spikey (spikey) - 4x1, 32x32 frames.
- OctoPussy (octopussy) - 2x1, 32x32 frames.
- Plant / PlantDark (plant, plant_dark) - 2x1, 32x32 frames.
"""
from PIL import Image, ImageDraw
import os
import sys

sys.path.insert(0, "C:/workspace/morsecode/docs/assets/mario-sprites/ampere-staging")
from sprite_tools import tint, build_sheet

OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"


def new_cell(w=32, h=32):
    return Image.new("RGBA", (w, h), (0, 0, 0, 0))


def draw_drone(frame, bob=0):
    draw = ImageDraw.Draw(frame, "RGBA")
    y = 12 + bob
    draw.ellipse((8, y, 24, y + 12), fill=(120, 150, 190, 255), outline=(40, 60, 90, 255))
    draw.rectangle((14, y + 4, 18, y + 8), fill=(255, 80, 80, 255))
    draw.rectangle((6, y + 5, 8, y + 7), fill=(60, 80, 110, 255))
    draw.rectangle((24, y + 5, 26, y + 7), fill=(60, 80, 110, 255))
    draw.rectangle((14, y - 4, 18, y), fill=(140, 170, 210, 255))
    draw.rectangle((10, y - 6, 22, y - 4), fill=(80, 100, 130, 255))


def build_son_of_a_buitch():
    frames = []
    for bob in (0, 1):
        frame = new_cell()
        draw_drone(frame, bob=bob)
        frames.append(frame)
    build_sheet(32, 32, frames, 2, 1, f"{OUT}/SonOfABuitch.png")


def draw_spikey_egg(frame, wobble=0):
    draw = ImageDraw.Draw(frame, "RGBA")
    cx, cy = 16 + wobble, 18
    draw.ellipse((cx - 7, cy - 9, cx + 7, cy + 7), fill=(200, 180, 120, 255), outline=(110, 90, 50, 255))
    for dx in (-6, -2, 2, 6):
        draw.polygon([(cx + dx, cy - 10), (cx + dx - 2, cy - 6), (cx + dx + 2, cy - 6)], fill=(120, 70, 40, 255))


def build_spikey_egg():
    frames = []
    for wobble in (-1, 1):
        frame = new_cell()
        draw_spikey_egg(frame, wobble=wobble)
        frames.append(frame)
    build_sheet(32, 32, frames, 2, 1, f"{OUT}/SpikeyEgg.png")


def draw_spikey(frame, pose=0):
    draw = ImageDraw.Draw(frame, "RGBA")
    cx, cy = 16, 18
    body = [(cx - 8, cy - 6, cx + 8, cy + 8)]
    draw.ellipse(body[0], fill=(90, 110, 130, 255), outline=(40, 60, 80, 255))
    spikes = [(-10, -2), (-8, -8), (-2, -10), (6, -9), (10, -4), (9, 4), (2, 9), (-6, 8)]
    for dx, dy in spikes:
        draw.polygon([(cx + dx, cy + dy), (cx + dx - 3, cy + dy - 2), (cx + dx - 1, cy + dy + 2)], fill=(180, 70, 60, 255))
    eye_offset = -2 if pose % 2 == 0 else 2
    draw.rectangle((cx + eye_offset - 2, cy - 1, cx + eye_offset, cy + 1), fill=(255, 80, 80, 255))


def build_spikey():
    frames = []
    for pose in range(4):
        frame = new_cell()
        draw_spikey(frame, pose)
        frames.append(frame)
    build_sheet(32, 32, frames, 4, 1, f"{OUT}/Spikey.png")


def draw_octo(frame, phase=0):
    draw = ImageDraw.Draw(frame, "RGBA")
    cx, cy = 16, 16
    draw.ellipse((cx - 7, cy - 6, cx + 7, cy + 6), fill=(120, 170, 200, 255), outline=(50, 80, 110, 255))
    for i, dx in enumerate((-6, -2, 2, 6)):
        sway = -1 if (phase + i) % 2 == 0 else 1
        draw.line((cx + dx, cy + 5, cx + dx + sway, cy + 12), fill=(60, 100, 130, 255), width=2)
    draw.rectangle((cx - 3, cy - 1, cx - 1, cy + 1), fill=(255, 80, 80, 255))
    draw.rectangle((cx + 1, cy - 1, cx + 3, cy + 1), fill=(255, 80, 80, 255))


def build_octopussy():
    frames = []
    for phase in (0, 1):
        frame = new_cell()
        draw_octo(frame, phase)
        frames.append(frame)
    build_sheet(32, 32, frames, 2, 1, f"{OUT}/OctoPussy.png")


def draw_plant(frame, open_mouth=True):
    draw = ImageDraw.Draw(frame, "RGBA")
    stem = (14, 26, 18, 46)
    draw.rectangle(stem, fill=(60, 140, 80, 255))
    head_y = 10
    draw.ellipse((8, head_y, 24, head_y + 16), fill=(90, 190, 120, 255), outline=(40, 100, 60, 255))
    if open_mouth:
        draw.rectangle((11, head_y + 7, 21, head_y + 11), fill=(30, 50, 40, 255))
    else:
        draw.line((11, head_y + 10, 21, head_y + 10), fill=(30, 50, 40, 255), width=2)


def build_plants():
    frames = []
    for open_mouth in (True, False):
        frame = new_cell(32, 48)
        draw_plant(frame, open_mouth=open_mouth)
        frames.append(frame)
    build_sheet(32, 48, frames, 2, 1, f"{OUT}/plant.png")
    dark_frames = [tint(frame, (40, 80, 60), 0.35) for frame in frames]
    build_sheet(32, 48, dark_frames, 2, 1, f"{OUT}/plantdark.png")


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    build_son_of_a_buitch()
    build_spikey_egg()
    build_spikey()
    build_octopussy()
    build_plants()

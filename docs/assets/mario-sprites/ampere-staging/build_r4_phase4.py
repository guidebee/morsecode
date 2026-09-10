"""
Step R.4 Phase 4.

Covers:
- EnemyTurtlePatrol (enemy_turtle_patrol).
- FlyingTurtle family (flying_turtle, flying_turtle_dark, flying_turtle_patrol).

All frames are 32x48, 4 columns x 1 row per AssetSpec.
"""
from PIL import Image, ImageDraw
import os

OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"


def split_sheet(path, frame_w, frame_h, cols):
    sheet = Image.open(path).convert("RGBA")
    frames = []
    for i in range(cols):
        x0 = i * frame_w
        frames.append(sheet.crop((x0, 0, x0 + frame_w, frame_h)))
    return frames


def add_thruster(frame, intensity=0):
    out = frame.copy()
    draw = ImageDraw.Draw(out, "RGBA")
    cx = out.width // 2
    base_y = out.height - 3

    if intensity == 0:
        flame = [(cx, base_y + 1), (cx - 1, base_y), (cx + 1, base_y)]
    else:
        flame = [(cx, base_y + 1), (cx - 1, base_y), (cx + 1, base_y), (cx, base_y - 1)]

    for x, y in flame:
        draw.rectangle((x, y, x, y), fill=(255, 176, 64, 255))
    draw.rectangle((cx, base_y, cx, base_y), fill=(255, 232, 140, 255))
    return out


def build_enemy_turtle_patrol():
    frames = split_sheet(f"{OUT}/turtle.png", 32, 48, 4)
    sheet = Image.new("RGBA", (32 * 4, 48), (0, 0, 0, 0))
    for i, frame in enumerate(frames):
        sheet.paste(frame, (i * 32, 0), frame)
    sheet.save(f"{OUT}/EnemyTurtlePatrol.png")
    print("wrote", f"{OUT}/EnemyTurtlePatrol.png")


def build_flying_variants():
    def build_variant(src, dest):
        base = split_sheet(src, 32, 48, 4)
        frames = [add_thruster(frame, 0 if i % 2 == 0 else 1) for i, frame in enumerate(base)]
        sheet = Image.new("RGBA", (32 * 4, 48), (0, 0, 0, 0))
        for i, frame in enumerate(frames):
            sheet.paste(frame, (i * 32, 0), frame)
        sheet.save(dest)
        print("wrote", dest)

    build_variant(f"{OUT}/turtle.png", f"{OUT}/FlyingTurtle.png")
    build_variant(f"{OUT}/turtle_dark.png", f"{OUT}/FlyingTurtledark.png")
    build_variant(f"{OUT}/turtle.png", f"{OUT}/FlyingTurtlePatrol.png")


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    build_enemy_turtle_patrol()
    build_flying_variants()

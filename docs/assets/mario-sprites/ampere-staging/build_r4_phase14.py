"""
Step R.4 Phase 14.

Covers:
- wall (Wall.png) - 2x1
- lift (Lift.png) - 1x1
- small_castle (SmallCastle.png) - 1x1
- big_castle (BigCastle.png) - 1x1
- tree (tree.png) - 5x2
- CloudsNight variants: bw_tree, bw_small_castle, bw_big_castle, bw_bouncer, bw_rocket_launcher
"""
from PIL import Image, ImageDraw, ImageOps
import os

OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"


def new_cell(w=32, h=32):
    return Image.new("RGBA", (w, h), (0, 0, 0, 0))


def build_wall():
    frames = []
    for variant in (0, 1):
        frame = new_cell()
        draw = ImageDraw.Draw(frame, "RGBA")
        draw.rectangle((2, 2, 29, 29), fill=(80, 90 + 10 * variant, 110, 255), outline=(40, 50, 70, 255))
        for y in range(6, 28, 6):
            draw.line((4, y, 28, y), fill=(60, 70, 90, 255), width=1)
        frames.append(frame)
    sheet = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    for i, frame in enumerate(frames):
        sheet.paste(frame, (i * 32, 0), frame)
    sheet.save(f"{OUT}/Wall.png")
    print("wrote", f"{OUT}/Wall.png")


def build_lift():
    frame = new_cell()
    draw = ImageDraw.Draw(frame, "RGBA")
    draw.rectangle((2, 12, 29, 22), fill=(90, 110, 130, 255), outline=(40, 60, 80, 255))
    draw.rectangle((5, 14, 26, 20), fill=(140, 170, 190, 255))
    frame.save(f"{OUT}/Lift.png")
    print("wrote", f"{OUT}/Lift.png")


def build_castle(name, base_color):
    frame = new_cell()
    draw = ImageDraw.Draw(frame, "RGBA")
    draw.rectangle((4, 10, 27, 27), fill=base_color + (255,), outline=(40, 50, 70, 255))
    draw.rectangle((10, 6, 14, 10), fill=base_color + (255,))
    draw.rectangle((18, 6, 22, 10), fill=base_color + (255,))
    draw.rectangle((14, 14, 18, 20), fill=(30, 40, 50, 255))
    frame.save(f"{OUT}/{name}.png")
    print("wrote", f"{OUT}/{name}.png")
    return frame


def build_tree():
    frames = []
    for i in range(10):
        frame = new_cell()
        draw = ImageDraw.Draw(frame, "RGBA")
        draw.ellipse((6, 6, 26, 20), fill=(70, 150, 90, 255), outline=(40, 90, 60, 255))
        draw.rectangle((14, 18, 18, 28), fill=(90, 70, 50, 255))
        if i % 5 == 0:
            draw.rectangle((4, 12, 10, 16), fill=(60, 130, 80, 255))
        if i % 5 == 2:
            draw.rectangle((22, 12, 28, 16), fill=(60, 130, 80, 255))
        frames.append(frame)
    sheet = Image.new("RGBA", (32 * 5, 32 * 2), (0, 0, 0, 0))
    for i, frame in enumerate(frames):
        x = (i % 5) * 32
        y = (i // 5) * 32
        sheet.paste(frame, (x, y), frame)
    sheet.save(f"{OUT}/tree.png")
    print("wrote", f"{OUT}/tree.png")
    return sheet


def bw_variant(image):
    gray = ImageOps.grayscale(image)
    return Image.merge("RGBA", (gray, gray, gray, image.split()[3]))


def build_bw_variants(tree_sheet, small_castle, big_castle):
    bw_dir = f"{OUT}/CloudsNight"
    os.makedirs(bw_dir, exist_ok=True)
    bw_tree = bw_variant(tree_sheet)
    bw_tree.save(f"{bw_dir}/tree.png")
    bw_variant(small_castle).save(f"{bw_dir}/SmallCastle.png")
    bw_variant(big_castle).save(f"{bw_dir}/BigCastle.png")
    bw_variant(Image.open(f"{OUT}/Bouncer.png")).save(f"{bw_dir}/Bouncer.png")
    bw_variant(Image.open(f"{OUT}/RocketLauncher.png")).save(f"{bw_dir}/RocketLauncher.png")
    print("wrote CloudsNight variants")


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    build_wall()
    build_lift()
    small_castle = build_castle("SmallCastle", (90, 110, 130))
    big_castle = build_castle("BigCastle", (70, 90, 120))
    tree_sheet = build_tree()
    build_bw_variants(tree_sheet, small_castle, big_castle)

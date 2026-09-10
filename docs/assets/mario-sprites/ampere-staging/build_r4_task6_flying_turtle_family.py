from PIL import Image, ImageDraw
import sys

sys.path.insert(0, "C:/workspace/morsecode/docs/assets/mario-sprites/ampere-staging")
from sprite_tools import build_sheet, mirror

SRC = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"
OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"


def split_4x1(path, frame_h):
    im = Image.open(path).convert("RGBA")
    return [im.crop((i * 32, 0, (i + 1) * 32, frame_h)) for i in range(4)]


def winged(frame, facing, flap):
    out = frame.copy()
    w = Image.new("RGBA", out.size, (0, 0, 0, 0))
    d = ImageDraw.Draw(w)

    # Small pixel wing silhouette; flap toggles between two poses.
    if facing == "left":
        if flap == 0:
            d.polygon([(11, 11), (5, 8), (3, 12), (7, 16), (11, 14)], fill=(232, 236, 242, 255))
            d.polygon([(11, 14), (8, 17), (10, 19), (13, 16)], fill=(184, 193, 206, 255))
        else:
            d.polygon([(11, 11), (6, 5), (3, 8), (7, 14), (11, 14)], fill=(232, 236, 242, 255))
            d.polygon([(11, 14), (8, 16), (10, 18), (13, 16)], fill=(184, 193, 206, 255))
    else:
        base = Image.new("RGBA", out.size, (0, 0, 0, 0))
        bd = ImageDraw.Draw(base)
        if flap == 0:
            bd.polygon([(20, 11), (26, 8), (28, 12), (24, 16), (20, 14)], fill=(232, 236, 242, 255))
            bd.polygon([(20, 14), (23, 17), (21, 19), (18, 16)], fill=(184, 193, 206, 255))
        else:
            bd.polygon([(20, 11), (25, 5), (28, 8), (24, 14), (20, 14)], fill=(232, 236, 242, 255))
            bd.polygon([(20, 14), (23, 16), (21, 18), (18, 16)], fill=(184, 193, 206, 255))
        w = base

    out.alpha_composite(w)
    return out


if __name__ == "__main__":
    turtle = split_4x1(f"{SRC}/turtle.png", 48)
    turtle_dark = split_4x1(f"{SRC}/turtle_dark.png", 48)

    flying = [
        winged(turtle[0], "left", 0),
        winged(turtle[1], "left", 1),
        winged(turtle[2], "right", 0),
        winged(turtle[3], "right", 1),
    ]
    flying_dark = [
        winged(turtle_dark[0], "left", 0),
        winged(turtle_dark[1], "left", 1),
        winged(turtle_dark[2], "right", 0),
        winged(turtle_dark[3], "right", 1),
    ]

    # Patrol variant reuses the same winged silhouette, with a slightly brighter
    # wing so it reads as a distinct subtype at a glance.
    patrol = []
    for i, fr in enumerate(flying):
        p = fr.copy()
        glow = Image.new("RGBA", p.size, (0, 0, 0, 0))
        gd = ImageDraw.Draw(glow)
        if i < 2:
            gd.rectangle((6, 7, 12, 9), fill=(248, 250, 255, 120))
        else:
            gd.rectangle((20, 7, 26, 9), fill=(248, 250, 255, 120))
        p.alpha_composite(glow)
        patrol.append(p)

    enemy_patrol = [turtle[0], turtle[1], turtle[2], turtle[3]]

    build_sheet(32, 48, flying, 4, 1, f"{OUT}/FlyingTurtle.png")
    build_sheet(32, 48, flying_dark, 4, 1, f"{OUT}/FlyingTurtledark.png")
    build_sheet(32, 48, patrol, 4, 1, f"{OUT}/FlyingTurtlePatrol.png")
    build_sheet(32, 48, enemy_patrol, 4, 1, f"{OUT}/EnemyTurtlePatrol.png")

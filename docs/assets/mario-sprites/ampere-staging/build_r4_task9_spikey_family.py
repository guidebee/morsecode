from PIL import Image, ImageDraw
import sys

sys.path.insert(0, "C:/workspace/morsecode/docs/assets/mario-sprites/ampere-staging")
from sprite_tools import build_sheet, place_content, tint, verified_crop

POSES = "C:/workspace/Kenney_Game_Assets_All/2D assets/Toon Characters/Robot/PNG/Poses"
SRC = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"
OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"


def split_4x1(path, frame_h):
    im = Image.open(path).convert("RGBA")
    return [im.crop((i * 32, 0, (i + 1) * 32, frame_h)) for i in range(4)]


def add_hover_pad(frame, hot=False):
    out = frame.copy()
    pad = Image.new("RGBA", out.size, (0, 0, 0, 0))
    d = ImageDraw.Draw(pad)
    base = (74, 86, 104, 210)
    glow = (138, 212, 255, 150 if hot else 90)
    d.ellipse((3, 33, 29, 46), fill=base)
    d.ellipse((8, 37, 24, 44), fill=glow)
    out.alpha_composite(pad)
    return out


def son_frames():
    idle_src = Image.open(f"{POSES}/character_robot_walk0.png").convert("RGBA")
    throw_src = Image.open(f"{POSES}/character_robot_attack0.png").convert("RGBA")

    idle = place_content(
        verified_crop(idle_src, (0, 0, idle_src.width, idle_src.height), "son idle"),
        32,
        48,
        fill=0.9,
        anchor="bottom",
    )
    throw = place_content(
        verified_crop(throw_src, (0, 0, throw_src.width, throw_src.height), "son throw"),
        32,
        48,
        fill=0.9,
        anchor="bottom",
    )

    return [add_hover_pad(idle, hot=False), add_hover_pad(throw, hot=True)]


def spikey_frames():
    turtle = split_4x1(f"{SRC}/turtle.png", 48)
    # Crop the lower 32x32 chunk from turtle walk frames to keep the grounded
    # robot-shell silhouette, then add spikes.
    base = [fr.crop((0, 16, 32, 48)) for fr in turtle]

    out = []
    for i, fr in enumerate(base):
        s = tint(fr, (194, 84, 74), 0.3)
        layer = Image.new("RGBA", s.size, (0, 0, 0, 0))
        d = ImageDraw.Draw(layer)
        xoff = 0 if i % 2 == 0 else 1
        teeth = [
            (4 + xoff, 8, 7 + xoff, 1, 10 + xoff, 8),
            (11 + xoff, 7, 14 + xoff, 0, 17 + xoff, 7),
            (18 + xoff, 8, 21 + xoff, 1, 24 + xoff, 8),
        ]
        for p in teeth:
            d.polygon(p, fill=(242, 244, 246, 255))
        d.rectangle((7, 14, 10, 16), fill=(255, 220, 120, 230))
        d.rectangle((21, 14, 24, 16), fill=(255, 220, 120, 230))
        s.alpha_composite(layer)
        out.append(s)
    return out


def egg_frames(spikey):
    core = spikey[0].copy()
    layer = Image.new("RGBA", core.size, (0, 0, 0, 0))
    d = ImageDraw.Draw(layer)
    d.ellipse((6, 8, 26, 28), fill=(86, 116, 158, 245))
    d.arc((8, 12, 24, 24), 210, 330, fill=(214, 232, 252, 230), width=2)
    d.rectangle((14, 16, 18, 19), fill=(242, 248, 255, 210))
    egg0 = Image.alpha_composite(core, layer)

    pulse = Image.new("RGBA", core.size, (0, 0, 0, 0))
    p = ImageDraw.Draw(pulse)
    p.ellipse((8, 10, 24, 26), fill=(126, 196, 255, 60))
    egg1 = Image.alpha_composite(egg0, pulse)
    return [egg0, egg1]


if __name__ == "__main__":
    son = son_frames()
    spikey = spikey_frames()
    egg = egg_frames(spikey)

    build_sheet(32, 48, son, 2, 1, f"{OUT}/SonOfABuitch.png")
    build_sheet(32, 32, egg, 2, 1, f"{OUT}/SpikeyEgg.png")
    build_sheet(32, 32, spikey, 4, 1, f"{OUT}/Spikey.png")
    print("wrote SonOfABuitch.png, SpikeyEgg.png, Spikey.png")

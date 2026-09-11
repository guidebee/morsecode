"""R.5 -- end-of-level scenery/UI: flag/flag_top/flag_sphere(+_fence variants)/
flag_win, the sea-only `bubble` swim particle, and the two baked end-game
text screens `another_castle_message`/`quest_complete`.

Consumers read (see MARIO_RESKIN_EXECUTION.md for the full citations):
  - `MarioTileRegistry`'s "Flag" case: `flag`/`flag_fence` is a thin,
    never-moving 4x288 rod (`Flag.png` native dims) spawned once per level;
    `flag_sphere`/`flag_sphere_fence` is a static 32x32 ornament above it.
    Both recolor to "_fence" for CloudsNight or "Fence" backgrounds.
  - `FlagPole.java`: reads `flag_top` (32x32) -- the one part that actually
    slides, drawn stretched across the pole's full tile.
  - `FlagWinBanner.java`: reads `flag_win` (32x32) -- the small banner that
    rises beside the castle at the *true* end-of-level checkpoint.
  - `Bubble.java`: reads `bubble`, a 4x1 sheet of 8x14 frames, ambient swim
    particle rising underwater (frame sequence 0,1,2,3,2,1,0 = grow/shrink
    pulse, confirmed from its own FRAME_SEQUENCE constant).
  - `MarioGameScreen.beginAnotherCastleMessage`: spawns a single whole-image
    Scenery of `another_castle_message` (the "WhyYouDOThis" checkpoint) or
    `quest_complete` (the "Princess" checkpoint) -- both 384x128 baked text
    images (black background, white pixel text), not runtime font
    rendering. Rewritten here to match the "Ampere's Run" identity's
    "Signal Core" objective (MARIO_RESKIN_PLAN.md Sec.2): the fake-out
    checkpoint reads "SIGNAL LOST / TRY ANOTHER SECTOR." and the real
    ending reads "SIGNAL RESTORED / MISSION COMPLETE." This double-counts as
    the "Rewrite the 3 user-facing strings" R.5 checklist item's asset-side
    half; the strings.xml/AndroidManifest/MarioMenuScreen title text is a
    separate small code change, not an asset swap.

No plausible pack match for either the flag or the message screens after
scanning the Kenney bundle and RTS Sci-fi (both only have flat UI banners,
no thin-rod/pennant assets and no baked dialogue-screen art) -- built
procedurally in the same riveted-panel/glow language as every other R.4/R.5
asset, consistent with the established fallback pattern.
"""
import os
from PIL import Image, ImageDraw, ImageFont
from sprite_tools import tint, glow_pulse

OUT = os.path.join(os.path.dirname(__file__), "..", "reskin-source")
os.makedirs(os.path.join(OUT, "CloudsNight"), exist_ok=True)

CYAN = (90, 220, 255)
CYAN_DIM = (30, 90, 110)
ORANGE = (255, 150, 60)
ORANGE_DIM = (110, 60, 20)
GREEN = (110, 255, 140)
METAL = (70, 78, 92)
METAL_DK = (40, 46, 56)


def save(im, *parts):
    path = os.path.join(OUT, *parts)
    im.save(path)
    print("wrote", path)


# ---------------------------------------------------------------- flag rod
def build_rod(w, h, core_color, edge_color):
    im = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    px = im.load()
    for y in range(h):
        for x in range(w):
            if w <= 4:
                # thin antenna mast: bright core column(s), dim edge column(s)
                bright = x in (w // 2, w // 2 - 1) if w > 1 else True
            else:
                bright = x >= w * 0.35 and x <= w * 0.65
            px[x, y] = core_color if bright else edge_color
    return im


flag = build_rod(4, 288, CYAN + (255,), CYAN_DIM + (255,))
flag_fence = build_rod(4, 288, ORANGE + (255,), ORANGE_DIM + (255,))
save(flag, "Flag.png")
save(flag_fence, "FlagFence.png")


# ------------------------------------------------------------- flag_sphere
def build_sphere(color):
    im = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    d.ellipse((3, 3, 28, 28), fill=(20, 24, 30, 255), outline=(10, 12, 16, 255), width=2)
    d.ellipse((7, 7, 24, 24), fill=color + (255,))
    d.ellipse((11, 10, 18, 16), fill=(255, 255, 255, 140))
    return im


save(build_sphere(CYAN), "FlagSphere.png")
save(build_sphere(ORANGE), "FlagSphereFence.png")


# ---------------------------------------------------------------- flag_top
def build_pennant(color):
    im = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    # small triangular tech pennant flying off a short stub of rod
    d.rectangle((0, 6, 3, 26), fill=METAL + (255,))
    d.polygon([(3, 4), (30, 12), (30, 18), (3, 22)], fill=(24, 28, 36, 255))
    d.polygon([(3, 4), (30, 12), (30, 18), (3, 22)], outline=color + (255,))
    d.line((8, 9, 24, 13), fill=color + (255,), width=1)
    d.line((8, 17, 24, 15), fill=color + (255,), width=1)
    return im


save(build_pennant(CYAN), "FlagTop.png")


# --------------------------------------------------------------- flag_win
def build_win_banner():
    im = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    # rising "signal acquired" burst -- a ringed beacon pulse, distinct
    # silhouette from the pole ornament above so the two don't read as
    # the same asset when both are on screen at once.
    d.ellipse((10, 10, 21, 21), fill=GREEN + (255,))
    for r, a in ((6, 200), (10, 130), (14, 70)):
        d.ellipse((15 - r, 15 - r, 15 + r, 15 + r), outline=GREEN + (a,), width=1)
    d.line((15, 2, 15, 8), fill=GREEN + (255,), width=1)
    d.line((15, 22, 15, 28), fill=GREEN + (255,), width=1)
    d.line((2, 15, 8, 15), fill=GREEN + (255,), width=1)
    d.line((22, 15, 28, 15), fill=GREEN + (255,), width=1)
    return im


save(build_win_banner(), "FlagWin.png")


# ------------------------------------------------------------------ bubble
def build_bubble_frame(radius):
    im = Image.new("RGBA", (8, 14), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    cx, cy = 4, 7
    d.ellipse((cx - radius, cy - radius, cx + radius, cy + radius),
              outline=(150, 230, 255, 220), width=1)
    if radius >= 3:
        d.point((cx - 1, cy - 1), fill=(255, 255, 255, 200))
    return im


bubble_frames = [build_bubble_frame(r) for r in (1, 2, 3, 3)]
sheet = Image.new("RGBA", (32, 14), (0, 0, 0, 0))
for i, f in enumerate(bubble_frames):
    sheet.paste(f, (i * 8, 0), f)
save(sheet, "Bubble.png")


# ------------------------------------------------------- baked text screens
def load_font(size):
    try:
        return ImageFont.load_default(size=size)
    except TypeError:
        return ImageFont.load_default()


def build_message(lines, accent):
    im = Image.new("RGBA", (384, 128), (4, 4, 8, 255))
    d = ImageDraw.Draw(im)
    title_font = load_font(26)
    body_font = load_font(20)
    y = 14
    for i, (text, font, color) in enumerate(lines):
        bbox = d.textbbox((0, 0), text, font=font)
        w = bbox[2] - bbox[0]
        d.text(((384 - w) / 2, y), text, font=font, fill=color)
        y += (bbox[3] - bbox[1]) + 18
    return im


another_castle_message = build_message(
    [
        ("SIGNAL LOST", load_font(26), (255, 255, 255, 255)),
        ("TRY ANOTHER SECTOR.", load_font(22), (255, 150, 60, 255)),
    ],
    ORANGE,
)
quest_complete = build_message(
    [
        ("SIGNAL RESTORED", load_font(26), (255, 255, 255, 255)),
        ("MISSION COMPLETE.", load_font(22), (110, 255, 140, 255)),
    ],
    GREEN,
)
save(another_castle_message, "AnotherCastleMessage.png")
save(quest_complete, "QuestComplete.png")

print("done")

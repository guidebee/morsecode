"""
Step R.4, tier-3/4 table: monkey, son_of_a_buitch/spikey_egg/spikey,
fish_grey/fish_red/octopussy (docs/mario/MARIO_RESKIN_JUNIOR_DEV_GUIDE.md Sec5).

Per that guide's own note, these are "good AI-gen candidates ... if no pack
source fits". Real constraint hit while executing this task: the R.0-picked
AI tool (Retro Diffusion) was never actually purchased/activated (still
tracked as an open item in MARIO_RESKIN_CREDITS.md) and isn't reachable from
this environment either way - no image-generation tool is available here.
Checked the obvious local fallback first (Kenney's Pixel Shmup ship
sprites, Pixel Platformer's small character heads - see the montage review
in chat history) and neither fits: the ships are top-down silhouettes with
no clear left/right walk-facing read, and the character heads are (per
KENNEY_ALL_IN_ONE_INDEX.md Sec5's own verdict) "too generic/blank to read as
a named enemy". Substituting hand-drawn procedural pixel art instead - the
same technique already used for Iron/QuestionMark/1UP/BossFire - rather than
force a bad-fit source or leave these on stale Nintendo pixels.

Consuming classes checked directly for every dimension/frame-convention claim
below (see each function's own comment).
"""
import sys
sys.path.insert(0, "C:/workspace/morsecode/docs/assets/mario-sprites/ampere-staging")
from sprite_tools import tint, mirror, build_sheet, glow_pulse
from PIL import Image, ImageDraw
import os

OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"


def canvas(w, h):
    return Image.new("RGBA", (w, h), (0, 0, 0, 0))


# --------------------------------------------------------------- monkey

def build_monkey():
    print("Monkey (hammer-thrower -> wrench-throwing sentry bot, procedural):")
    # Monkey.java: "monkey" region 32x48/frame, 3 cols x 2 rows (96x96) -
    # linear index 0/1=look-left idle, 4/5=look-right idle (2/3 unused),
    # same layout convention as Boss.java.
    def pose(arm_up):
        im = canvas(32, 48)
        d = ImageDraw.Draw(im)
        # squat sentry body, sits low in the cell (it's a perched turret, not
        # a full-height biped)
        d.rounded_rectangle((6, 26, 26, 44), radius=4, fill=(96, 104, 112, 255), outline=(50, 56, 62, 255), width=2)
        d.ellipse((9, 14, 23, 28), fill=(120, 128, 136, 255), outline=(50, 56, 62, 255), width=2)  # head
        d.ellipse((13, 18, 19, 24), fill=(214, 158, 40, 255))  # single amber eye
        # arm + wrench, raised when about to throw
        if arm_up:
            d.line((22, 24, 27, 10), fill=(96, 104, 112, 255), width=3)
            d.rectangle((24, 6, 30, 12), fill=(150, 158, 166, 255), outline=(50, 56, 62, 255))
        else:
            d.line((22, 30, 29, 34), fill=(96, 104, 112, 255), width=3)
            d.rectangle((27, 32, 33, 38), fill=(150, 158, 166, 255), outline=(50, 56, 62, 255))
        return im

    left_a, left_b = pose(False), pose(True)
    right_a, right_b = mirror(left_a), mirror(left_b)
    frames_by_cell = {
        (0, 0): left_a, (1, 0): left_b, (2, 0): left_a,
        (0, 1): right_a, (1, 1): right_a, (2, 1): right_b,
    }
    build_sheet(32, 48, frames_by_cell, 3, 2, f"{OUT}/Monkey.png")


# --------------------------------------------------- son_of_a_buitch family

def build_son_of_a_buitch():
    print("SonOfABuitch (Lakitu-analog -> hovering egg-drone, procedural):")
    # SonOfABuitch.java: "son_of_a_buitch" region 32x48/frame, 2 frames
    # (64x48 total) - frame0=idle float, frame1="rearing back" pre-throw.
    def pose(rearing):
        im = canvas(32, 48)
        d = ImageDraw.Draw(im)
        # saucer body
        d.ellipse((3, 6, 29, 20), fill=(90, 100, 112, 255), outline=(45, 50, 58, 255), width=2)
        d.ellipse((10, 3, 22, 13), fill=(150, 200, 210, 220), outline=(45, 50, 58, 255))
        # dangling clamp/arm holding the egg pod
        pod_y = 30 if not rearing else 24
        d.line((16, 18, 16, pod_y), fill=(70, 78, 86, 255), width=2)
        d.ellipse((9, pod_y, 23, pod_y + 14), fill=(196, 90, 60, 255), outline=(90, 40, 25, 255), width=2)
        return im

    build_sheet(32, 48, [pose(False), pose(True)], 2, 1, f"{OUT}/SonOfABuitch.png")


def build_spikey_egg():
    print("SpikeyEgg (procedural, 2-frame wobble):")
    # SpikeyEgg.java: "spikey_egg" region 32x32/frame, 2 frames (64x32).
    def pose(tilt):
        im = canvas(32, 32)
        d = ImageDraw.Draw(im)
        cx = 16 + (2 if tilt else -2)
        d.ellipse((cx - 9, 8, cx + 9, 28), fill=(196, 90, 60, 255), outline=(90, 40, 25, 255), width=2)
        for ang in range(0, 360, 60):
            import math
            x = cx + round(9 * math.cos(math.radians(ang)))
            y = 18 + round(10 * math.sin(math.radians(ang)))
            d.line((cx, 18, x, y), fill=(70, 78, 86, 255), width=2)
        return im

    build_sheet(32, 32, [pose(False), pose(True)], 2, 1, f"{OUT}/SpikeyEgg.png")


def build_spikey():
    print("Spikey (procedural, 4-frame walk, spiky-topped bot):")
    # Spikey.java: "spikey" region 32x32/frame, 4 frames (128x32),
    # cols 0-1=left-facing walk pair, 2-3=right-facing (same convention as
    # EnemyTurtle/Helmet, confirmed by its own setFrame() call).
    def pose(step):
        im = canvas(32, 32)
        d = ImageDraw.Draw(im)
        leg_off = 2 if step else -2
        d.rounded_rectangle((7, 12, 25, 26), radius=3, fill=(196, 90, 60, 255), outline=(90, 40, 25, 255), width=2)
        for sx in (10, 16, 22):
            d.polygon([(sx - 3, 12), (sx + 3, 12), (sx, 5)], fill=(150, 158, 166, 255))
        d.line((11, 26, 11 + leg_off, 31), fill=(70, 78, 86, 255), width=2)
        d.line((21, 26, 21 - leg_off, 31), fill=(70, 78, 86, 255), width=2)
        d.ellipse((13, 16, 19, 22), fill=(214, 158, 40, 255))
        return im

    right_a, right_b = pose(False), pose(True)
    left_a, left_b = mirror(right_a), mirror(right_b)
    build_sheet(32, 32, [left_a, left_b, right_a, right_b], 4, 1, f"{OUT}/Spikey.png")


# --------------------------------------------------------------- sea family

def build_fish(color, out_name):
    print(f"{out_name} (procedural torpedo-drone, 2-frame tail swish):")
    # FishyWater/FishyGround: "fish_grey"/"fish_red" region 32x32/frame, 2 frames (64x32).
    def pose(tail_up):
        im = canvas(32, 32)
        d = ImageDraw.Draw(im)
        d.ellipse((6, 11, 24, 21), fill=color + (255,), outline=(20, 30, 40, 255), width=2)
        d.ellipse((10, 13, 15, 18), fill=(230, 240, 245, 230))  # canopy/eye window
        tail_y0, tail_y1 = (6, 16) if tail_up else (16, 26)
        d.polygon([(6, 16), (0, tail_y0), (0, tail_y1)], fill=color + (255,), outline=(20, 30, 40, 255))
        return im

    build_sheet(32, 32, [pose(False), pose(True)], 2, 1, f"{OUT}/{out_name}")


def build_octopussy():
    print("OctoPussy (procedural round drone with tentacle-wires, 2-frame):")
    # OctoPussy.java: "octopussy" region 32x48/frame, 2 frames (64x48).
    def pose(legs_up):
        im = canvas(32, 48)
        d = ImageDraw.Draw(im)
        d.ellipse((5, 4, 27, 26), fill=(107, 70, 130, 255), outline=(50, 30, 62, 255), width=2)
        d.ellipse((11, 11, 21, 21), fill=(214, 158, 40, 255), outline=(50, 30, 62, 255))
        leg_bottom = 34 if legs_up else 42
        for lx in (9, 14, 18, 23):
            d.line((lx, 24, lx, leg_bottom), fill=(80, 55, 96, 255), width=2)
        return im

    build_sheet(32, 48, [pose(False), pose(True)], 2, 1, f"{OUT}/OctoPussy.png")


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    build_monkey()
    build_son_of_a_buitch()
    build_spikey_egg()
    build_spikey()
    build_fish((150, 156, 162), "FishGrey.png")
    build_fish((196, 90, 60), "FishRed.png")
    build_octopussy()

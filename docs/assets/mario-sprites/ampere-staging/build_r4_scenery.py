"""
Step R.4, tier-3/4 table: scenery/parallax backdrops
(docs/mario/MARIO_RESKIN_JUNIOR_DEV_GUIDE.md Sec5).

Covers: small_castle/big_castle(+bw), tree/bw_tree (reframed as an
antenna/pylon per KENNEY_ALL_IN_ONE_INDEX.md Sec4's own recommendation -
no style-matched tree source exists anywhere scanned), lift,
mountain/clouds/cloudsnight/fence/fence2 (each a full standalone 1536x448
backdrop - confirmed via BackgroundBand.java's own doc, "each source PNG is
confirmed 1536x448"; sizes here re-confirmed by reading the actual
Nintendo-derived fallback files directly, not assumed from the guide),
sea_background (32x96, tiled every 32px per BackgroundBand's second
constructor), stone_clowd, and the flag/banner family.

All procedural. Parallax backdrops use a period that evenly divides 1536 so
the pattern wraps seamlessly when BackgroundBand tiles this image 10 times
side by side (confirmed no visible seam by construction: every repeating
element is placed at x % period, and a period dividing 1536 exactly means
the content at the image's right edge is identical in phase to its left
edge). font/info/info2 are skipped - grep-confirmed dead code (never loaded
via region() anywhere), matching the guide's own flagged suspicion.
"""
import sys
sys.path.insert(0, "C:/workspace/morsecode/docs/assets/mario-sprites/ampere-staging")
from sprite_tools import tint, build_sheet
from PIL import Image, ImageDraw, ImageFont
import os
import math

OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"

METAL = (150, 158, 166)
METAL_DARK = (80, 88, 96)
METAL_DARKER = (45, 50, 56)


def canvas(w, h, color=(0, 0, 0, 0)):
    return Image.new("RGBA", (w, h), color)


def rect(im, x0, y0, x1, y1, color):
    ImageDraw.Draw(im).rectangle((x0, y0, x1 - 1, y1 - 1), fill=color + (255,) if len(color) == 3 else color)


# ------------------------------------------------------- small/big castle

def fortress(w, h, base, dark, light, accent):
    """A layered fortress/installation silhouette - drawn directly at
    target resolution (not scaled from a smaller source) so it stays crisp
    at these unusually large canvas sizes (160x160 / 304x352)."""
    im = canvas(w, h)
    d = ImageDraw.Draw(im)
    ground_y = h - 6
    # side towers
    tower_w = w // 5
    for tx in (0, w - tower_w):
        d.rectangle((tx, h * 0.35, tx + tower_w, ground_y), fill=dark + (255,), outline=(0, 0, 0, 0))
        d.rectangle((tx + 3, h * 0.35 + 3, tx + tower_w - 3, ground_y - 3), fill=base + (255,))
        for wy in range(int(h * 0.45), int(ground_y) - 10, 22):
            d.rectangle((tx + 6, wy, tx + tower_w - 6, wy + 8), fill=accent + (255,))
    # central keep, taller
    keep_x0, keep_x1 = w * 0.28, w * 0.72
    d.rectangle((keep_x0, h * 0.12, keep_x1, ground_y), fill=dark + (255,), outline=(0, 0, 0, 0))
    d.rectangle((keep_x0 + 4, h * 0.12 + 4, keep_x1 - 4, ground_y - 4), fill=base + (255,))
    # crenellations
    crenel_w = (keep_x1 - keep_x0) / 6
    for i in range(6):
        if i % 2 == 0:
            cx0 = keep_x0 + i * crenel_w
            d.rectangle((cx0, h * 0.08, cx0 + crenel_w, h * 0.14), fill=dark + (255,))
    for wy in range(int(h * 0.22), int(ground_y) - 10, 26):
        d.rectangle((keep_x0 + 8, wy, keep_x1 - 8, wy + 10), fill=accent + (255,))
    # antenna/comms mast on the keep (mechanical identity signature)
    mast_x = (keep_x0 + keep_x1) / 2
    d.line((mast_x, h * 0.12, mast_x, h * 0.02), fill=light + (255,), width=max(2, w // 60))
    d.ellipse((mast_x - 4, h * 0.0, mast_x + 4, h * 0.05), fill=(214, 158, 40, 255))
    # base trim
    d.rectangle((0, ground_y, w, h), fill=dark + (255,))
    return im


def build_castles():
    print("SmallCastle/BigCastle(+bw) (procedural fortress, drawn at native target resolution):")
    base, dark, light, accent = (107, 47, 47), (58, 23, 23), (217, 140, 74), (217, 140, 74)
    fortress(160, 160, base, dark, light, accent).save(f"{OUT}/SmallCastle.png")
    print("wrote", f"{OUT}/SmallCastle.png")
    fortress(304, 352, base, dark, light, accent).save(f"{OUT}/BigCastle.png")
    print("wrote", f"{OUT}/BigCastle.png")

    os.makedirs(f"{OUT}/CloudsNight", exist_ok=True)
    bw = (90, 90, 90), (44, 44, 44), (184, 184, 184), (184, 184, 184)
    fortress(160, 160, *bw).save(f"{OUT}/CloudsNight/SmallCastle.png")
    print("wrote", f"{OUT}/CloudsNight/SmallCastle.png")
    fortress(304, 352, *bw).save(f"{OUT}/CloudsNight/BigCastle.png")
    print("wrote", f"{OUT}/CloudsNight/BigCastle.png")


# --------------------------------------------------------------- tree/bw_tree

def build_tree(out_path, base, dark, light):
    # Tree.java: 32x32/frame, 5 cols x 2 rows - row0 cols0-2 = left-cap/
    # middle/right-cap of the solid canopy row (GreenAndTrees, the only
    # palette any World-1 level uses), col3 = the vertical trunk decoration
    # piece (LevelLoader.spawnTree reads frames[0][3] directly), col4
    # unused. Row1 (OrangeAndMushroom) is never read by any ported level -
    # filled with a dimmer recolor of the same design for completeness.
    def dish_cap(side):
        im = canvas(32, 32)
        d = ImageDraw.Draw(im)
        rect(im, 0, 20, 32, 32, dark)
        cx = 4 if side == "left" else (28 if side == "right" else 16)
        d.pieslice((cx - 14, 6, cx + 14, 26), 200, 340, fill=base + (255,), outline=dark + (255,))
        d.ellipse((cx - 2, 10, cx + 2, 14), fill=light + (255,))
        return im

    def middle():
        return dish_cap("mid")

    def trunk():
        im = canvas(32, 32)
        d = ImageDraw.Draw(im)
        rect(im, 13, 0, 19, 32, dark)
        rect(im, 14, 0, 18, 32, base)
        d.ellipse((12, 14, 20, 18), fill=light + (255,))
        return im

    left_cap, mid, right_cap, trunk_piece, unused = dish_cap("left"), middle(), dish_cap("right"), trunk(), canvas(32, 32)
    row0 = {(0, 0): left_cap, (1, 0): mid, (2, 0): right_cap, (3, 0): trunk_piece, (4, 0): unused}
    dim = tuple(max(0, c - 30) for c in base)
    row1 = {(0, 1): tint(left_cap, dim, 0.4), (1, 1): tint(mid, dim, 0.4), (2, 1): tint(right_cap, dim, 0.4),
            (3, 1): tint(trunk_piece, dim, 0.4), (4, 1): unused}
    build_sheet(32, 32, {**row0, **row1}, 5, 2, out_path)


# ------------------------------------------------------------------------ lift

def build_lift():
    print("Lift (16x16, tiled platform texture):")
    im = canvas(16, 16)
    rect(im, 0, 0, 16, 16, METAL_DARK)
    rect(im, 1, 1, 15, 6, METAL)
    rect(im, 1, 1, 15, 3, (199, 210, 219))
    rect(im, 1, 9, 15, 15, METAL_DARKER)
    im.save(f"{OUT}/Lift.png")
    print("wrote", f"{OUT}/Lift.png")


# --------------------------------------------------------- sea_background

def build_sea_background():
    print("sea_background (32x96, tiled every 32px):")
    im = canvas(32, 96)
    rect(im, 0, 0, 32, 96, (26, 60, 90))
    d = ImageDraw.Draw(im)
    for y in (10, 30, 50, 70, 88):
        d.arc((-4, y - 5, 14, y + 5), 200, 340, fill=(95, 178, 200, 220), width=2)
        d.arc((12, y - 4, 30, y + 6), 20, 160, fill=(20, 45, 70, 255), width=2)
        d.arc((26, y - 5, 44, y + 5), 200, 340, fill=(95, 178, 200, 220), width=2)
    im.save(f"{OUT}/Sea.png")
    print("wrote", f"{OUT}/Sea.png")


# --------------------------------------------------------------- stone_clowd

def build_stone_clowd():
    print("stone_clowd (32x32, separate Clowd-attribute tile):")
    base, dark, light = (100, 104, 112), (52, 56, 62), (168, 174, 182)
    im = canvas(32, 32, base + (255,))
    rect(im, 0, 0, 32, 1, dark); rect(im, 0, 31, 32, 32, dark)
    rect(im, 0, 0, 1, 32, dark); rect(im, 31, 0, 32, 32, dark)
    rect(im, 8, 8, 24, 24, dark)
    rect(im, 10, 10, 22, 22, tuple(min(255, c + 12) for c in base))
    for cx, cy in ((8, 8), (8, 22), (22, 8), (22, 22)):
        rect(im, cx - 1, cy - 1, cx + 1, cy + 1, light)
    im.save(f"{OUT}/stone_Clowd.png")
    print("wrote", f"{OUT}/stone_Clowd.png")


# ---------------------------------------------------------------- parallax

def sky_gradient(w, h, top, bottom):
    im = canvas(w, h)
    for y in range(h):
        t = y / max(1, h - 1)
        c = tuple(round(top[i] + (bottom[i] - top[i]) * t) for i in range(3))
        ImageDraw.Draw(im).line((0, y, w, y), fill=c + (255,))
    return im


def build_mountain():
    print("Mountain (1536x448, periodic skyline silhouette):")
    w, h = 1536, 448
    im = sky_gradient(w, h, (30, 40, 70), (110, 130, 160))
    d = ImageDraw.Draw(im)
    period = 256
    for x0 in range(0, w, period):
        peak_h = 170
        d.polygon([(x0, h), (x0 + period * 0.5, h - peak_h), (x0 + period, h)], fill=(48, 56, 78, 255))
        d.polygon([(x0 + period * 0.25, h), (x0 + period * 0.75, h - peak_h * 0.65), (x0 + period * 1.1, h)],
                   fill=(66, 76, 100, 255))
        # a few lit windows on the mid-ground silhouette - sells the "distant installation" read
        for wx in range(int(x0 + 20), int(x0 + period - 20), 40):
            d.point((wx, h - 40), fill=(214, 158, 40, 200))
    im.save(f"{OUT}/Mountain.png")
    print("wrote", f"{OUT}/Mountain.png")


def build_clouds(out_path, sky_top, sky_bottom, cloud_color):
    w, h = 1536, 448
    im = sky_gradient(w, h, sky_top, sky_bottom)
    d = ImageDraw.Draw(im)
    period = 192
    for x0 in range(0, w, period):
        cy = 90
        d.ellipse((x0 + 10, cy, x0 + 90, cy + 34), fill=cloud_color)
        d.ellipse((x0 + 40, cy - 14, x0 + 120, cy + 26), fill=cloud_color)
        cy2 = 220
        d.ellipse((x0 + 100, cy2, x0 + 170, cy2 + 28), fill=cloud_color)
    im.save(out_path)
    print("wrote", out_path)


def build_fence(out_path, base, dark, sky_top, sky_bottom):
    w, h = 1536, 448
    im = sky_gradient(w, h, sky_top, sky_bottom)
    d = ImageDraw.Draw(im)
    ground_y = h - 60
    d.rectangle((0, ground_y, w, h), fill=dark + (255,))
    period = 64
    for x0 in range(0, w, period):
        d.rectangle((x0 + 26, ground_y - 90, x0 + 34, ground_y + 4), fill=base + (255,))
    rail_y = ground_y - 60
    d.rectangle((0, rail_y, w, rail_y + 6), fill=base + (255,))
    im.save(out_path)
    print("wrote", out_path)


# ------------------------------------------------------------------- flags

def build_flags():
    print("Flag family (pole/ornament/banner set):")
    def pole(w=4, h=288):
        im = canvas(w, h, METAL + (255,))
        return im

    pole().save(f"{OUT}/Flag.png")
    print("wrote", f"{OUT}/Flag.png")
    tint(pole(), (90, 96, 104), 0.4).save(f"{OUT}/FlagFence.png")
    print("wrote", f"{OUT}/FlagFence.png")

    def sphere(color):
        im = canvas(32, 32)
        d = ImageDraw.Draw(im)
        d.ellipse((6, 6, 26, 26), fill=color + (255,), outline=METAL_DARK + (255,), width=2)
        d.ellipse((10, 9, 16, 15), fill=(255, 255, 255, 160))
        return im

    sphere((214, 158, 40)).save(f"{OUT}/FlagSphere.png")
    print("wrote", f"{OUT}/FlagSphere.png")
    sphere((127, 224, 232)).save(f"{OUT}/FlagSphereFence.png")
    print("wrote", f"{OUT}/FlagSphereFence.png")

    top = canvas(32, 32)
    d = ImageDraw.Draw(top)
    d.polygon([(4, 4), (28, 10), (4, 20)], fill=(53, 208, 160, 255), outline=METAL_DARK + (255,))
    top.save(f"{OUT}/FlagTop.png")
    print("wrote", f"{OUT}/FlagTop.png")

    win = canvas(32, 32)
    d2 = ImageDraw.Draw(win)
    cell = 8
    for row in range(4):
        for col in range(4):
            if (row + col) % 2 == 0:
                d2.rectangle((col * cell, row * cell, col * cell + cell, row * cell + cell), fill=(20, 22, 26, 255))
            else:
                d2.rectangle((col * cell, row * cell, col * cell + cell, row * cell + cell), fill=(230, 234, 238, 255))
    win.save(f"{OUT}/FlagWin.png")
    print("wrote", f"{OUT}/FlagWin.png")


def banner(text, sub):
    w, h = 384, 128
    im = canvas(w, h, (18, 22, 28, 255))
    d = ImageDraw.Draw(im)
    d.rectangle((4, 4, w - 5, h - 5), outline=(214, 158, 40, 255), width=3)
    try:
        font = ImageFont.truetype("arial.ttf", 28)
        font_sub = ImageFont.truetype("arial.ttf", 16)
    except OSError:
        font = ImageFont.load_default()
        font_sub = font
    tw = d.textlength(text, font=font)
    d.text(((w - tw) / 2, 40), text, fill=(230, 234, 238, 255), font=font)
    sw = d.textlength(sub, font=font_sub)
    d.text(((w - sw) / 2, 80), sub, fill=(127, 224, 232, 255), font=font_sub)
    return im


def build_bubble():
    print("Bubble (8x14/frame x4 - ambient swim particle, per Bubble.java):")
    def frame(size_frac):
        im = canvas(8, 14)
        d = ImageDraw.Draw(im)
        w = max(2, round(6 * size_frac))
        h = max(2, round(6 * size_frac))
        x0, y0 = (8 - w) // 2, (14 - h) // 2
        d.ellipse((x0, y0, x0 + w, y0 + h), outline=(180, 225, 235, 220), width=1)
        return im

    build_sheet(8, 14, [frame(f) for f in (0.6, 0.8, 1.0, 0.8)], 4, 1, f"{OUT}/Bubble.png")


def build_banners():
    print("another_castle_message / quest_complete (384x128 banners):")
    banner("SIGNAL LOST", "The Warden was a decoy - press on").save(f"{OUT}/AnotherCastleMessage.png")
    print("wrote", f"{OUT}/AnotherCastleMessage.png")
    banner("SIGNAL RESTORED", "Sector cleared").save(f"{OUT}/QuestComplete.png")
    print("wrote", f"{OUT}/QuestComplete.png")


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    os.makedirs(f"{OUT}/CloudsNight", exist_ok=True)
    build_castles()
    build_tree(f"{OUT}/tree.png", (138, 151, 166), (77, 88, 102), (199, 210, 219))
    build_tree(f"{OUT}/CloudsNight/tree.png", (100, 100, 100), (50, 50, 50), (190, 190, 190))
    build_lift()
    build_sea_background()
    build_stone_clowd()
    build_mountain()
    build_clouds(f"{OUT}/Clouds.png", (120, 160, 200), (200, 220, 235), (235, 240, 245, 235))
    build_clouds(f"{OUT}/CloudsNight.png", (10, 12, 30), (35, 40, 60), (70, 74, 90, 200))
    build_fence(f"{OUT}/Fence.png", METAL, METAL_DARKER, (90, 110, 140), (170, 190, 205))
    build_fence(f"{OUT}/Fence2.png", (110, 100, 90), (54, 48, 42), (90, 110, 140), (170, 190, 205))
    build_flags()
    build_bubble()
    build_banners()

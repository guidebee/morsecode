"""
Step R.4, Junior Dev Guide Tasks 1-5 (docs/mario/MARIO_RESKIN_JUNIOR_DEV_GUIDE.md Sec4).

Task 1 - Iron ("used up" block): procedural, reusing the same per-theme
    palette as stone/chocolate/brick (build_terrain_and_common.py's THEMES),
    but flatter/duller ("depleted") - no light-accent corners, single inset
    panel instead of the raised-center look stone_tile uses.
Task 2 - QuestionMark / QuestionMarkGrey: procedural beveled block + a
    hand-drawn pixel "?" glyph (bitmap font below), glowing brighter across
    the 3 idle-bob frames. Bank confirmed (by reading Bank.java) to reuse
    the plain "brick" themed region, NOT question_mark - no new art needed
    for Bank itself.
Task 3 - Helmet/HelmetShell family: Robot Master Series' enemy3 (a
    clam-like creature that opens/closes - confirmed via enemy3all.png and
    per-frame getbbox() scan, NOT a legged walker like enemy1/enemy2). Its
    closed frame doubles naturally as "helmet_shell" (a shut shell), open
    frames as the "helmet" walk animation - a better thematic fit than
    forcing a legged-walk read onto a shell creature.
Task 4 - The Warden (Boss/BossFire): Robot Master Series' miniboss1 (a
    tank-style robot, confirmed 144x80/frame via
    miniboss1_base[80height144wide].png, 11 near-identical left-facing
    frames - the bracket naming is [[height]height[width]wide], confirmed
    against actual PIL .size, not assumed). No boss-body "spitting fire"
    pose exists in the source (the laser sheets are beam VFX, not body
    poses) - built via glow_pulse on the idle pose instead. BossFire (the
    thrown projectile, 48x16/frame) has no source match either - procedural
    flame/ember, same technique already used for the coin.
Task 5 - Spare chassis (one_up/Life.java): no clean thematic match found in
    either pack (confirmed by KENNEY_ALL_IN_ONE_INDEX.md Sec2's own research) -
    hand-pixelled small chassis/antenna icon per the guide's own recommended
    fallback, cheaper and more on-identity than importing a heart icon.

Every crop is verified via getbbox() before use - printed at generation time.
"""
import sys
sys.path.insert(0, "C:/workspace/morsecode/docs/assets/mario-sprites/ampere-staging")
from sprite_tools import verified_crop, place_content, tint, glow_pulse, mirror, build_sheet, is_tileable_texture
from PIL import Image, ImageDraw
import os

OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"
ROBOT_PACK = "C:/workspace/robot_series_base_pack"

# Same palette already established for stone/chocolate/brick (Ground/
# Substrate/Fortress/Flooded Sector) - reused here so Iron/QuestionMark read
# as "part of this world" rather than a mismatched fifth material.
THEMES = {
    "":              ((138, 151, 166), (77, 88, 102), (199, 210, 219)),
    "_UnderGround":  ((47, 74, 65),    (22, 36, 31),   (53, 208, 160)),
    "_Castle":       ((107, 47, 47),   (58, 23, 23),   (217, 140, 74)),
    "_Sea":          ((47, 99, 119),   (22, 53, 64),   (127, 224, 232)),
}


def rect(im, x0, y0, x1, y1, color):
    for y in range(y0, y1):
        for x in range(x0, x1):
            im.putpixel((x, y), color + (255,))


# ------------------------------------------------------------- Task 1: Iron

def iron_tile(base, dark):
    """A flatter, duller variant of stone_tile - single inset panel, no
    light-accent corners - reads as "depleted/used up" next to the
    brighter, highlight-cornered stone/chocolate/brick tiles."""
    im = Image.new("RGBA", (32, 32), dark + (255,))
    rect(im, 2, 2, 30, 30, base)
    rect(im, 4, 4, 28, 28, dark)
    rect(im, 6, 6, 26, 26, tuple(max(0, c - 10) for c in base))
    return im


def build_iron():
    print("Iron (procedural, per-theme):")
    # AssetSpec: iron -> Iron.png, 4 cols x 1 row. Frame 0=Sea (unused in
    # World 1 but still drawn for completeness), 1=Ground, 2=UnderGround,
    # 3=Castle (per Iron.java's FRAME_GROUND/FRAME_UNDERGROUND/FRAME_CASTLE).
    order = ["_Sea", "", "_UnderGround", "_Castle"]
    frames = [iron_tile(THEMES[k][0], THEMES[k][1]) for k in order]
    build_sheet(32, 32, frames, 4, 1, f"{OUT}/Iron.png")


# ------------------------------------------------------- Task 2: QuestionMark

# A compact 5x7 pixel "?" glyph (1=filled). Hand-drawn, not extracted.
GLYPH_QMARK = [
    "01110",
    "10001",
    "00001",
    "00010",
    "00100",
    "00000",
    "00100",
]


def glyph_mask(glyph, scale):
    gw, gh = len(glyph[0]), len(glyph)
    im = Image.new("L", (gw, gh), 0)
    for y, row in enumerate(glyph):
        for x, c in enumerate(row):
            if c == "1":
                im.putpixel((x, y), 255)
    return im.resize((gw * scale, gh * scale), Image.NEAREST)


def question_block(base, dark, light, glow, glow_strength):
    im = Image.new("RGBA", (32, 32), base + (255,))
    rect(im, 0, 0, 32, 1, dark)
    rect(im, 0, 31, 32, 32, dark)
    rect(im, 0, 0, 1, 32, dark)
    rect(im, 31, 0, 32, 32, dark)
    for cx, cy in ((2, 2), (2, 29), (29, 2), (29, 29)):
        rect(im, cx - 1, cy - 1, cx + 1, cy + 1, dark)
    mask = glyph_mask(GLYPH_QMARK, 3)
    mw, mh = mask.size
    gx, gy = (32 - mw) // 2, (32 - mh) // 2 - 1
    glyph_color = tuple(min(255, int(c + (255 - c) * glow_strength)) for c in glow)
    glyph_layer = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    glyph_layer.paste(Image.new("RGBA", mask.size, glyph_color + (255,)), (gx, gy), mask)
    im.alpha_composite(glyph_layer)
    return im


def build_question_mark():
    print("QuestionMark / QuestionMarkGrey (procedural, 3-frame bob):")
    # question_mark (Ground/Sea contexts): amber block, bright cyan-white glyph.
    base, dark, light = (191, 156, 51), (107, 82, 20), (247, 214, 130)
    glow = (255, 244, 200)
    frames = [question_block(base, dark, light, glow, s) for s in (0.0, 0.45, 1.0)]
    build_sheet(32, 32, frames, 3, 1, f"{OUT}/QuestionMark.png")

    # question_mark_grey (UnderGround/Castle): muted grey block, dimmer glow.
    base_g, dark_g, light_g = (90, 96, 102), (48, 52, 58), (150, 156, 162)
    glow_g = (200, 214, 220)
    frames_g = [question_block(base_g, dark_g, light_g, glow_g, s) for s in (0.0, 0.4, 0.85)]
    build_sheet(32, 32, frames_g, 3, 1, f"{OUT}/QuestionMarkGrey.png")


# --------------------------------------------------------- Task 3: Helmet

def build_helmet():
    print("Helmet / HelmetShell family (Robot Master Series enemy3, verified crops):")
    sheet = Image.open(f"{ROBOT_PACK}/enemy3/enemy3attack-Sheet[32height32wide].png").convert("RGBA")
    assert sheet.size == (352, 32), f"unexpected enemy3attack sheet size {sheet.size}"
    n = sheet.width // 32
    frames = [verified_crop(sheet, (i * 32, 0, (i + 1) * 32, 32), f"enemy3_f{i}") for i in range(n)]

    # frame 0 = fully closed shell (content bbox height ~7px) - use directly
    # for helmet_shell (a shut shell is exactly what that state should look
    # like). frames 2 and 5 = two distinct mid-open poses, used as the
    # "walk" pair (this creature has no leg-walk animation - an opening/
    # closing shell reads as its equivalent of movement).
    shell_raw = frames[0]
    walk_a_raw = frames[2]
    walk_b_raw = frames[5]

    shell_base = place_content(shell_raw, 32, 32, fill=0.8, anchor="bottom")
    walk_a = place_content(walk_a_raw, 32, 32, fill=0.85, anchor="bottom")
    walk_b = place_content(walk_b_raw, 32, 32, fill=0.85, anchor="bottom")

    # Source is a green shell / red interior, already close to a "normal"
    # palette - dark and white variants are tint-recolors of the same art,
    # matching how Scuttler/Roller's theme recolors already work.
    palettes = {
        "": (None, None),                      # normal - no tint, use source colors as-is
        "dark": ((25, 35, 30), 0.65),           # dark, mossy
        "white": ((225, 230, 232), 0.55),      # pale/white
    }
    for suffix, (color, strength) in palettes.items():
        def maybe_tint(frame):
            return tint(frame, color, strength) if color else frame

        left_a, left_b = mirror(maybe_tint(walk_a)), mirror(maybe_tint(walk_b))
        right_a, right_b = maybe_tint(walk_a), maybe_tint(walk_b)
        # Helmet.java: cols 0-1 = left-facing walk pair, cols 2-3 = right-facing walk pair.
        frames_out = [left_a, left_b, right_a, right_b]
        name = f"Helmet{suffix}.png" if not suffix else f"Helmet{suffix}.png"
        # Match AssetSpec exact filenames: Helmet.png / Helmetdark.png / Helmetwhite.png
        fname = {"": "Helmet.png", "dark": "Helmetdark.png", "white": "Helmetwhite.png"}[suffix]
        build_sheet(32, 32, frames_out, 4, 1, f"{OUT}/{fname}")

        shell_fname = {"": "HelmetShell.png", "dark": "HelmetShelldark.png", "white": "HelmetShellwhite.png"}[suffix]
        maybe_tint(shell_base).save(f"{OUT}/{shell_fname}")
        print("wrote", f"{OUT}/{shell_fname}")


# ----------------------------------------------------- Task 4: The Warden

def build_boss():
    print("The Warden / Boss + BossFire (Robot Master Series miniboss1, verified crops):")
    base_sheet = Image.open(f"{ROBOT_PACK}/miniboss1/miniboss1_base[80height144wide].png").convert("RGBA")
    fw, fh = 144, 80
    assert base_sheet.size[1] == fh and base_sheet.size[0] % fw == 0, f"unexpected miniboss1 base sheet size {base_sheet.size}"
    idle_a_raw = verified_crop(base_sheet, (0 * fw, 0, 1 * fw, fh), "miniboss_idle_a")
    idle_b_raw = verified_crop(base_sheet, (1 * fw, 0, 2 * fw, fh), "miniboss_idle_b")

    # Boss.java: super(region, tileSize*2, tileSize*2, ...) - 64x64 cell, and
    # its own doc: "boss" region is 64x64/frame, 3 cols x 2 rows.
    cell = 64
    idle_a = place_content(idle_a_raw, cell, cell, fill=0.92, anchor="bottom")
    idle_b = place_content(idle_b_raw, cell, cell, fill=0.92, anchor="bottom")
    fire_pose = glow_pulse(idle_a, 0.45)  # no distinct "spitting fire" body pose exists in source; a bright flash reads as "active/firing"

    left_a, left_b, left_fire = idle_a, idle_b, fire_pose
    right_a, right_b, right_fire = mirror(idle_a), mirror(idle_b), mirror(fire_pose)

    # Linear frame index = row * cols + col (row-major, confirmed by the
    # existing player/enemy/terrain sheets' own row convention). Per
    # Boss.java's doc: 0/1=look-left idle, 2=fire pose, 4/5=look-right idle
    # (3 unused - filled with the mirrored fire pose for completeness).
    frames_by_cell = {
        (0, 0): left_a, (1, 0): left_b, (2, 0): left_fire,
        (0, 1): right_fire, (1, 1): right_a, (2, 1): right_b,
    }
    build_sheet(cell, cell, frames_by_cell, 3, 2, f"{OUT}/Boss.png")

    # BossFire.java: "boss_fire" region is 48x16/frame, 2 frames (96x16
    # total). No source match in either pack (the laser sheets are beam
    # VFX, not a drifting-flame shape) - procedural ember, same technique
    # already used for the Bolt/gear coin.
    def flame_frame(flicker):
        im = Image.new("RGBA", (48, 16), (0, 0, 0, 0))
        draw = ImageDraw.Draw(im)
        w = 34 + flicker
        h = 10 + flicker // 2
        x0, y0 = (48 - w) // 2, (16 - h) // 2
        draw.ellipse((x0, y0, x0 + w, y0 + h), fill=(217, 90, 40, 255), outline=(140, 40, 20, 255))
        hi_w, hi_h = max(2, w // 3), max(2, h // 3)
        draw.ellipse((x0 + 4, y0 + 2, x0 + 4 + hi_w, y0 + 2 + hi_h), fill=(255, 200, 120, 230))
        return im

    build_sheet(48, 16, [flame_frame(0), flame_frame(4)], 2, 1, f"{OUT}/BossFire.png")


# ------------------------------------------------------- Task 5: Spare chassis

def build_one_up():
    print("Spare chassis / 1UP (hand-pixelled, no source match found):")
    # A small chassis/head icon: rounded grey-blue head shape with a single
    # green "life" light - distinct from the Battery cell (orange glow) and
    # Overclock chip (green gem) so it doesn't read as either of those.
    def chassis_frame(lit):
        im = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
        draw = ImageDraw.Draw(im)
        draw.rounded_rectangle((7, 6, 24, 23), radius=4, fill=(180, 188, 196, 255), outline=(90, 96, 104, 255), width=2)
        # small antenna
        draw.line((15, 6, 15, 2), fill=(120, 128, 136, 255), width=2)
        draw.ellipse((13, 0, 17, 4), fill=(120, 128, 136, 255))
        # single eye/life-light, brighter in the "lit" frame
        eye_color = (86, 232, 140, 255) if lit else (54, 158, 96, 255)
        draw.ellipse((12, 11, 19, 18), fill=eye_color, outline=(30, 60, 40, 255))
        return im

    build_sheet(32, 32, [chassis_frame(False), chassis_frame(True)], 2, 1, f"{OUT}/1UP.png")


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    build_iron()
    build_question_mark()
    build_helmet()
    build_boss()
    build_one_up()

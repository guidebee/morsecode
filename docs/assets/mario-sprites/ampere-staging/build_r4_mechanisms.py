"""
Step R.4, tier-3/4 table: mechanisms/hazards/connective scenery
(docs/mario/MARIO_RESKIN_JUNIOR_DEV_GUIDE.md Sec5).

Covers: fire_ball, lava/lava_ball/water, axe, wall, rocket_launcher(+bw),
bouncer(+bw)/spring, wooden_bridge/white_line/chain/rope/bridge_blocks,
pump family (6 theme variants), plant/plant_dark, hori_image, explosion,
bw_hammer.

Every dimension below is taken from reading the actual consuming Java class
(FireBall/LavaBall/Axe/LevelLoader.spawnWall/spawnRocketLauncher/
Spring/Pump/PiranhaPlant/MarioTileRegistry's HoriImage handler/Explosion/
Hammer), not assumed from the guide table alone - see chat history for the
exact grep/read-through. explosion reuses Robot Master Series' own
explode-Sheet (a real source match, not procedural, per the guide's own
recommendation for that one asset). Everything else here has no clean
source-pack match (pipes/wall/bridge pieces are simple connective geometry,
not the kind of thing a character-focused robot pack ships) - procedural,
consistent with the panel/rivet motif already established for terrain.
"""
import sys
sys.path.insert(0, "C:/workspace/morsecode/docs/assets/mario-sprites/ampere-staging")
from sprite_tools import tint, mirror, build_sheet, glow_pulse, verified_crop, place_content
from PIL import Image, ImageDraw
import os

OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"
ROBOT_PACK = "C:/workspace/robot_series_base_pack"

METAL = (150, 158, 166)
METAL_DARK = (80, 88, 96)
METAL_DARKER = (45, 50, 56)

# Reuse the exact per-theme accents already established for terrain (base,
# dark, light) - keyed the same way pump's regionFor() picks a theme.
THEME_ACCENT = {
    "": (138, 151, 166),
    "_castle": (217, 140, 74),
    "_sea": (127, 224, 232),
}


def canvas(w, h):
    return Image.new("RGBA", (w, h), (0, 0, 0, 0))


def rect(im, x0, y0, x1, y1, color):
    d = ImageDraw.Draw(im)
    d.rectangle((x0, y0, x1 - 1, y1 - 1), fill=color + (255,))


# --------------------------------------------------------------- fire_ball

def build_fire_ball():
    print("FireBall (projectile, 16x16/frame x4 - verified against FireBall.java):")
    def frame(i):
        im = canvas(16, 16)
        d = ImageDraw.Draw(im)
        wob = [0, 1, 0, -1][i % 4]
        d.ellipse((3 - wob, 3, 13 - wob, 13), fill=(217, 90, 40, 255), outline=(140, 40, 20, 255))
        d.ellipse((6 - wob, 5, 10 - wob, 9), fill=(255, 200, 120, 230))
        return im

    build_sheet(16, 16, [frame(i) for i in range(4)], 4, 1, f"{OUT}/FireBall.png")


# -------------------------------------------------------- lava/lava_ball/water

def build_lava():
    print("Lava/LavaBall/Water (procedural hazard tiles):")
    im = canvas(32, 32)
    d = ImageDraw.Draw(im)
    rect(im, 0, 0, 32, 32, (196, 70, 30))
    for i, y in enumerate((6, 16, 24)):
        d.arc((2, y - 4, 14, y + 4), 200, 340, fill=(255, 180, 90, 255), width=2)
        d.arc((16, y - 3, 30, y + 5), 20, 160, fill=(140, 40, 20, 255), width=2)
    im.save(f"{OUT}/Lava.png")
    print("wrote", f"{OUT}/Lava.png")

    def ball(rising):
        b = canvas(32, 32)
        d2 = ImageDraw.Draw(b)
        c = (255, 200, 90, 255) if rising else (217, 90, 40, 255)
        d2.ellipse((7, 7, 25, 25), fill=c, outline=(140, 40, 20, 255), width=2)
        d2.ellipse((11, 10, 17, 16), fill=(255, 230, 170, 220))
        return b

    build_sheet(32, 32, [ball(True), ball(False)], 2, 1, f"{OUT}/LavaBall.png")

    water = canvas(32, 32)
    d3 = ImageDraw.Draw(water)
    rect(water, 0, 0, 32, 32, (47, 99, 119))
    for y in (5, 15, 25):
        d3.arc((-4, y - 4, 12, y + 4), 200, 340, fill=(127, 224, 232, 220), width=2)
        d3.arc((10, y - 3, 26, y + 5), 20, 160, fill=(22, 53, 64, 255), width=2)
        d3.arc((24, y - 4, 40, y + 4), 200, 340, fill=(127, 224, 232, 220), width=2)
    water.save(f"{OUT}/Water.png")
    print("wrote", f"{OUT}/Water.png")


# --------------------------------------------------------------------- axe

def build_axe():
    print("Axe (bridge-cut lever -> console/switch icon, 32x32 x4, per KENNEY_ALL_IN_ONE_INDEX.md Sec3's own recommendation):")
    def frame(pulled):
        im = canvas(32, 32)
        d = ImageDraw.Draw(im)
        d.rounded_rectangle((4, 10, 28, 28), radius=3, fill=METAL_DARK + (255,), outline=METAL_DARKER + (255,), width=2)
        lever_x = 20 if pulled else 12
        d.line((16, 22, lever_x, 6), fill=METAL + (255,), width=3)
        d.ellipse((lever_x - 3, 3, lever_x + 3, 9), fill=(214, 158, 40, 255))
        d.ellipse((13, 19, 19, 25), fill=(90, 96, 104, 255))
        return im

    build_sheet(32, 32, [frame(False), frame(True), frame(True), frame(False)], 4, 1, f"{OUT}/Axe.png")


# --------------------------------------------------------------------- wall

def build_wall():
    print("Wall (decorative vertical strip, 32x32 x2 - cap + repeating body):")
    cap = canvas(32, 32)
    rect(cap, 0, 0, 32, 32, METAL_DARK)
    rect(cap, 2, 2, 30, 10, METAL)
    rect(cap, 2, 2, 30, 4, (199, 210, 219))
    body = canvas(32, 32)
    rect(body, 0, 0, 32, 32, METAL_DARK)
    for y in (4, 16, 28):
        rect(body, 2, y, 30, y + 3, METAL_DARKER)
    build_sheet(32, 32, [cap, body], 2, 1, f"{OUT}/Wall.png")


# ------------------------------------------------------------ rocket launcher

def build_rocket_launcher(out_name, base_color, dark_color, accent):
    print(f"{out_name} (turret, 32x32 x4 rows - rows 0-2 used per LevelLoader.spawnRocketLauncher):")
    head = canvas(32, 32)
    d = ImageDraw.Draw(head)
    rect(head, 4, 16, 28, 32, dark_color)
    d.rounded_rectangle((8, 6, 24, 20), radius=3, fill=base_color + (255,), outline=dark_color + (255,), width=2)
    d.ellipse((13, 10, 19, 16), fill=accent + (255,))
    body_a = canvas(32, 32)
    rect(body_a, 4, 0, 28, 32, dark_color)
    rect(body_a, 6, 4, 26, 28, base_color)
    body_b = canvas(32, 32)
    rect(body_b, 4, 0, 28, 32, dark_color)
    rect(body_b, 6, 2, 26, 30, tuple(max(0, c - 15) for c in base_color))
    unused = body_b
    build_sheet(32, 32, [head, body_a, body_b, unused], 1, 4, f"{OUT}/{out_name}")


# -------------------------------------------------------- bouncer / spring

def build_bouncer_and_spring():
    print("Bouncer/bw_bouncer (launch pad, 32x32) + Spring (coil, 32x64 x3 - reuses Charge coil motif):")
    def pad(base, dark):
        im = canvas(32, 32)
        rect(im, 2, 20, 30, 30, dark)
        rect(im, 2, 12, 30, 20, base)
        rect(im, 2, 12, 30, 15, tuple(min(255, c + 40) for c in base))
        return im

    pad(METAL, METAL_DARK).save(f"{OUT}/Bouncer.png")
    print("wrote", f"{OUT}/Bouncer.png")
    os.makedirs(f"{OUT}/CloudsNight", exist_ok=True)
    pad((110, 110, 110), (50, 50, 50)).save(f"{OUT}/CloudsNight/Bouncer.png")
    print("wrote", f"{OUT}/CloudsNight/Bouncer.png")

    gate = Image.open(f"{ROBOT_PACK}/other/gate.png").convert("RGBA")
    coil_base = place_content(verified_crop(gate, (0, 8, 16, 26), "spring_coil"), 32, 32, fill=0.8, anchor="bottom")

    def squish(h_frac):
        # Squash the coil vertically into the bottom of a 32x64 cell -
        # simulates a compressed spring at rest (0) growing back up (1).
        src_h = round(32 * h_frac)
        scaled = coil_base.resize((32, max(4, src_h)), Image.NEAREST)
        cell = canvas(32, 64)
        cell.paste(scaled, (0, 64 - scaled.height), scaled)
        return cell

    build_sheet(32, 64, [squish(0.5), squish(0.75), squish(1.0)], 3, 1, f"{OUT}/Spring.png")


# --------------------------------------- wooden_bridge / white_line / chain / rope / bridge_blocks

def build_connective_pieces():
    print("WoodenBridge/WhiteLine/Chain/Rope/BridgeBloks (procedural connective geometry):")
    bridge = canvas(32, 32)
    rect(bridge, 0, 0, 32, 32, METAL_DARK)
    for x in (4, 14, 24):
        rect(bridge, x, 2, x + 6, 30, METAL)
    bridge.save(f"{OUT}/WoodenBridge.png")
    print("wrote", f"{OUT}/WoodenBridge.png")

    # Thin vertical energy-line tile, stretched by the engine to 13 tiles tall.
    line = canvas(32, 32)
    d = ImageDraw.Draw(line)
    d.rectangle((14, 0, 17, 31), fill=(127, 224, 232, 230))
    d.rectangle((15, 0, 16, 31), fill=(230, 250, 252, 255))
    line.save(f"{OUT}/WhiteLine.png")
    print("wrote", f"{OUT}/WhiteLine.png")

    def link(offset):
        im = canvas(32, 32)
        d2 = ImageDraw.Draw(im)
        d2.ellipse((6, 4 + offset, 26, 16 + offset), outline=METAL_DARK + (255,), width=4)
        return im

    build_sheet(32, 32, [link(0), link(4), link(0), link(-4)], 4, 1, f"{OUT}/Chain.png")

    rope = canvas(32, 32)
    d3 = ImageDraw.Draw(rope)
    for y in range(2, 32, 6):
        d3.line((4, y, 28, y + 3), fill=(120, 90, 60, 255), width=3)
    rope.save(f"{OUT}/Rope.png")
    print("wrote", f"{OUT}/Rope.png")

    grate = canvas(32, 32)
    rect(grate, 0, 0, 32, 32, METAL_DARKER)
    for x in range(2, 30, 6):
        rect(grate, x, 2, x + 3, 30, METAL_DARK)
    rect(grate, 0, 0, 32, 3, METAL)
    grate.save(f"{OUT}/BridgeBloks.png")
    print("wrote", f"{OUT}/BridgeBloks.png")


# ------------------------------------------------------------- pump family

def pump_body(base, dark):
    im = canvas(32, 32)
    rect(im, 4, 0, 28, 32, dark)
    rect(im, 7, 0, 25, 32, base)
    for y in (6, 16, 26):
        rect(im, 7, y, 25, y + 2, dark)
    return im


def pump_top(base, dark):
    im = canvas(32, 32)
    rect(im, 2, 10, 30, 32, dark)
    rect(im, 5, 12, 27, 32, base)
    rect(im, 0, 4, 32, 12, dark)
    rect(im, 2, 6, 30, 12, base)
    return im


def build_pumps():
    print("Pump family (procedural riveted pipe, 6 theme variants + hori_image):")
    themes = {
        "": (METAL, METAL_DARK),
        " Castle": ((217, 140, 74), (107, 47, 47)),
        " Sea": ((127, 224, 232), (47, 99, 119)),
    }
    for suffix, (base, dark) in themes.items():
        pump_body(base, dark).save(f"{OUT}/pump{suffix}.png")
        print("wrote", f"{OUT}/pump{suffix}.png")
        pump_top(base, dark).save(f"{OUT}/pump top{suffix}.png")
        print("wrote", f"{OUT}/pump top{suffix}.png")

    # HoriImage: two 2-tile (64x64) pieces side by side (MarioTileRegistry's
    # own HoriImage handler splits at pieceSize=tileSize*2) - a wider pipe
    # opening built from the same motif, scaled up.
    left = pump_top(METAL, METAL_DARK).resize((64, 64), Image.NEAREST)
    right = pump_body(METAL, METAL_DARK).resize((64, 64), Image.NEAREST)
    build_sheet(64, 64, [left, right], 2, 1, f"{OUT}/HoriImage.png")


# --------------------------------------------------------------- plant family

def build_plants():
    print("Plant/plant_dark (pipe creature, 32x48 x2 - procedural):")
    def pose(color, open_mouth):
        im = canvas(32, 48)
        d = ImageDraw.Draw(im)
        d.ellipse((6, 20, 26, 46), fill=color + (255,), outline=(30, 40, 30, 255), width=2)
        mouth_h = 10 if open_mouth else 4
        d.ellipse((11, 24, 21, 24 + mouth_h), fill=(196, 90, 60, 255))
        d.line((16, 20, 16, 4), fill=(70, 90, 70, 255), width=3)
        d.ellipse((10, 0, 22, 12), fill=color + (255,), outline=(30, 40, 30, 255), width=2)
        return im

    build_sheet(32, 48, [pose((90, 160, 90), False), pose((90, 160, 90), True)], 2, 1, f"{OUT}/plant.png")
    build_sheet(32, 48, [pose((70, 90, 130), False), pose((70, 90, 130), True)], 2, 1, f"{OUT}/plantdark.png")


# --------------------------------------------------------------- explosion

def build_explosion():
    print("Explosion (Robot Master Series explode sheet, verified crops):")
    sheet = Image.open(f"{ROBOT_PACK}/other/explode-Sheet[64height64wide].png").convert("RGBA")
    fw, fh = 64, 64
    n = sheet.width // fw
    assert n >= 3, f"expected >=3 explosion frames, got {n}"
    # Frame n-1 is fully faded out (getbbox()==None, confirmed while writing
    # this) - picking growth frames only (skip the empty tail) so all 3 are
    # visible content, matching the Golden Rule's own warning about not
    # trusting an index without checking its actual crop.
    picks = [0, (n - 2) // 2, n - 2]
    frames = [
        place_content(verified_crop(sheet, (i * fw, 0, (i + 1) * fw, fh), f"explode_f{i}"), 32, 32, fill=0.9)
        for i in picks
    ]
    build_sheet(32, 32, frames, 3, 1, f"{OUT}/Explosion.png")


# --------------------------------------------------------------- bw_hammer

def build_bw_hammer():
    print("bw_hammer (thrown wrench, 28x28 x4 - matches Monkey's wrench prop):")
    def frame(angle_step):
        im = canvas(28, 28)
        d = ImageDraw.Draw(im)
        cx, cy = 14, 14
        import math
        ang = angle_step * 90
        dx = round(8 * math.cos(math.radians(ang)))
        dy = round(8 * math.sin(math.radians(ang)))
        d.line((cx - dx, cy - dy, cx + dx, cy + dy), fill=METAL + (255,), width=3)
        d.rectangle((cx + dx - 3, cy + dy - 3, cx + dx + 3, cy + dy + 3), fill=(150, 158, 166, 255), outline=METAL_DARK + (255,))
        return im

    build_sheet(28, 28, [frame(i) for i in range(4)], 4, 1, f"{OUT}/CloudsNight/Hammer.png")


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    os.makedirs(f"{OUT}/CloudsNight", exist_ok=True)
    build_fire_ball()
    build_lava()
    build_axe()
    build_wall()
    build_rocket_launcher("RocketLauncher.png", METAL, METAL_DARK, (214, 158, 40))
    build_rocket_launcher("CloudsNight/RocketLauncher.png", (120, 120, 120), (60, 60, 60), (200, 200, 200))
    build_bouncer_and_spring()
    build_connective_pieces()
    build_pumps()
    build_plants()
    build_explosion()
    build_bw_hammer()

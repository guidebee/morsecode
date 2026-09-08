"""
Step R.3: priority-1 world assets - terrain (stone/chocolate x5 themes,
brick x4 themes + brick_peaces fragments), the Bolt/gear coin, and the
Scuttler enemy (EnemyMashroom-equivalent). Native 32px/tile (ART_SCALE=1
permanently, per docs/mario/MARIO_RESKIN_EXECUTION.md's R.0 update).

Terrain/brick tiles are procedurally drawn (a beveled sci-fi armor-panel
motif) rather than extracted from either robot pack, since neither pack's
tile art is meant as small repeating 32x32 floor tiles (Robot Master
Series' "platform" sheet is large scenic set-pieces; Robot Platform Pack's
tileset is tightly-packed multi-cell illustrations, not isolated tiles) -
per MARIO_GAME_MECHANICS.md Sec16.5's own note that a per-theme palette
variation from one base design is a reasonable scope reduction. The coin
IS extracted (a clean, isolated icon in Robot Platform Pack's tileset).
The Scuttler enemy is extracted+recolored from Robot Master Series' enemy1.
"""
from PIL import Image, ImageOps
import os

OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"
PACK = "C:/workspace/robot_series_base_pack"
PLATFORM_PACK = "C:/workspace/Robot_Platform_Pack"

# name -> (base, dark, light)
THEMES = {
    "":              ((138, 151, 166), (77, 88, 102), (199, 210, 219)),   # Ground/Surface
    "_UnderGround":  ((47, 74, 65),    (22, 36, 31),   (53, 208, 160)),   # Substrate (teal glow accent)
    "_Castle":       ((107, 47, 47),   (58, 23, 23),   (217, 140, 74)),   # Fortress (warm amber accent)
    "_Sea":          ((47, 99, 119),   (22, 53, 64),   (127, 224, 232)),  # Flooded Sector (cyan accent)
}
BW_STONE = ((90, 90, 90), (44, 44, 44), (184, 184, 184))


def rect(draw_img, x0, y0, x1, y1, color):
    for y in range(y0, y1):
        for x in range(x0, x1):
            draw_img.putpixel((x, y), color + (255,))


def stone_tile(base, dark, light):
    im = Image.new("RGBA", (32, 32), base + (255,))
    rect(im, 0, 0, 32, 1, dark)
    rect(im, 0, 31, 32, 32, dark)
    rect(im, 0, 0, 1, 32, dark)
    rect(im, 31, 0, 32, 32, dark)
    rect(im, 8, 8, 24, 24, dark)
    rect(im, 10, 10, 22, 22, tuple(min(255, c + 12) for c in base))
    for cx, cy in ((8, 8), (8, 22), (22, 8), (22, 22)):
        rect(im, cx - 1, cy - 1, cx + 1, cy + 1, light)
    return im


def chocolate_tile(base, dark, light):
    accent = tuple(min(255, int(b * 0.85)) for b in base)
    im = Image.new("RGBA", (32, 32), accent + (255,))
    rect(im, 0, 0, 32, 1, dark)
    rect(im, 0, 31, 32, 32, dark)
    rect(im, 0, 0, 1, 32, dark)
    rect(im, 31, 0, 32, 32, dark)
    for gy in (9, 16, 23):
        rect(im, 3, gy, 29, gy + 2, dark)
        rect(im, 3, gy - 1, 29, gy, light)
    return im


def brick_tile(base, dark, light):
    im = Image.new("RGBA", (32, 32), dark + (255,))
    for row in range(2):
        for col in range(2):
            x0, y0 = col * 16 + 2, row * 16 + 2
            x1, y1 = col * 16 + 14, row * 16 + 14
            rect(im, x0, y0, x1, y1, base)
            rect(im, x0, y0, x1, y0 + 1, light)
            rect(im, x0, y0, x0 + 1, y1, light)
    return im


def fragment_pair(base, dark, light):
    frames = []
    for i in range(2):
        im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
        shade = base if i == 0 else tuple(min(255, c + 20) for c in base)
        rect(im, 2, 2, 14, 14, shade)
        rect(im, 2, 2, 14, 3, light)
        rect(im, 2, 2, 3, 14, dark)
        frames.append(im)
    return frames


def build_sheet(cell_w, cell_h, frames_by_cell, cols, rows, out_path):
    sheet = Image.new("RGBA", (cell_w * cols, cell_h * rows), (0, 0, 0, 0))
    for (col, row), frame in frames_by_cell.items():
        sheet.paste(frame, (col * cell_w, row * cell_h), frame)
    sheet.save(out_path)
    print("wrote", out_path, sheet.size)


def build_terrain():
    for suffix, (base, dark, light) in THEMES.items():
        stone_tile(base, dark, light).save(f"{OUT}/stone{suffix}.png")
        chocolate_tile(base, dark, light).save(f"{OUT}/chocolate{suffix}.png")
        brick_tile(base, dark, light).save(f"{OUT}/brick{suffix}.png" if suffix else f"{OUT}/brick.png")
        print("wrote stone/chocolate/brick", suffix or "(Ground)")
    # Sea's second variant (stone_castle_sea) - reuse the Sea palette.
    base, dark, light = THEMES["_Sea"]
    stone_tile(base, dark, light).save(f"{OUT}/stone_Castle_Sea.png")
    # bw_stone (CloudsNight) - separate greyscale look; bw_chocolate reuses
    # chocolate_Castle.png already (no separate file needed, per PackMarioAtlas).
    os.makedirs(f"{OUT}/CloudsNight", exist_ok=True)
    stone_tile(*BW_STONE).save(f"{OUT}/CloudsNight/stone.png")
    print("wrote stone_Castle_Sea, CloudsNight/stone")


def build_brick_peaces():
    # row0 unused, row1=Ground/Sea, row2=UnderGround, row3=Castle (16x16 cells, 2 cols).
    row_themes = [None, THEMES[""], THEMES["_UnderGround"], THEMES["_Castle"]]
    frames_by_cell = {}
    for row, theme in enumerate(row_themes):
        if theme is None:
            continue
        base, dark, light = theme
        f0, f1 = fragment_pair(base, dark, light)
        frames_by_cell[(0, row)] = f0
        frames_by_cell[(1, row)] = f1
    build_sheet(16, 16, frames_by_cell, 2, 4, f"{OUT}/BrickPeaces.png")


def place_content(frame, cell=32, fill=0.85, anchor="center"):
    """Crop to the frame's real (tight) alpha bounding box first, THEN scale
    to fill a fraction of the cell - fixes the bug where scaling a frame that
    was already cropped to a fixed, padded canvas (not its own content
    bbox) by a flat factor caused wildly inconsistent overflow/clipping
    per-frame once each frame's actual ink extent differed."""
    bbox = frame.getbbox()
    if bbox is None:
        return Image.new("RGBA", (cell, cell), (0, 0, 0, 0))
    content = frame.crop(bbox)
    cw, ch = content.size
    scale = (fill * cell) / max(cw, ch)
    nw, nh = max(1, round(cw * scale)), max(1, round(ch * scale))
    scaled = content.resize((nw, nh), Image.NEAREST)
    canvas = Image.new("RGBA", (cell, cell), (0, 0, 0, 0))
    x = (cell - nw) // 2
    y = (cell - nh) // 2 if anchor == "center" else (cell - nh)
    canvas.paste(scaled, (x, y), scaled)
    return canvas


def coin_frame(width_frac, cell=32):
    """Procedurally-drawn Bolt/gear coin: a filled ellipse with a rim and a
    highlight, width_frac (0..1) simulating a spin (1.0=face-on, small=edge-on).
    Drawn from scratch rather than extracted from the platform pack's tileset -
    that sheet packs icons edge-to-edge with zero padding, and a tight
    alpha-bbox crop kept bleeding a sliver of the adjacent sprite in, corrupting
    the shape (this is what caused the reported "coin looks vertically wrong").
    A from-scratch draw avoids the extraction fragility entirely."""
    from PIL import ImageDraw
    im = Image.new("RGBA", (cell, cell), (0, 0, 0, 0))
    draw = ImageDraw.Draw(im)
    h = round(cell * 0.8)
    w = max(3, round(h * width_frac))
    x0, y0 = (cell - w) // 2, (cell - h) // 2
    x1, y1 = x0 + w, y0 + h
    draw.ellipse((x0, y0, x1, y1), fill=(214, 158, 40, 255), outline=(120, 84, 16, 255))
    if w > 6:
        hi_w = max(2, w // 3)
        draw.ellipse((x0 + w // 6, y0 + h // 6, x0 + w // 6 + hi_w, y0 + h // 3),
                     fill=(255, 224, 140, 255))
    return im


def build_coin():
    spin = [1.0, 0.55, 0.15, 0.55]  # face-on -> edge-on -> face-on
    placed = [coin_frame(f) for f in spin]
    build_sheet(32, 32, {(0, 0): placed[0], (1, 0): placed[1], (2, 0): placed[2]}, 3, 1, f"{OUT}/Coin.png")
    build_sheet(32, 32, {(0, 0): placed[0], (1, 0): placed[1], (2, 0): placed[2], (3, 0): placed[3]},
                4, 1, f"{OUT}/CoinAnim.png")


def build_scuttler():
    src = Image.open(f"{PACK}/enemy1/enemy1[32height32wide].png").convert("RGBA")
    n = src.width // 32
    frames = [src.crop((i * 32, 0, (i + 1) * 32, 32)) for i in range(n)]
    # Source content is only ~15x14px inside its 32x32 canvas (confirmed via
    # getbbox()) - scale it up to actually fill most of the tile instead of
    # copying it 1:1, which read as "too small" next to a full-size tile.
    raw_pair = [frames[0], frames[1] if n > 1 else frames[0]]
    pair = [place_content(f, cell=32, fill=0.85, anchor="bottom") for f in raw_pair]

    def tint(frame, base):
        r, g, b, a = frame.split()
        overlay = Image.new("RGBA", frame.size, base + (255,))
        blended = Image.blend(frame.convert("RGBA"), overlay, 0.55)
        blended.putalpha(a)
        return blended

    # row0=Sea, row1=Ground, row2=UnderGround, row3=Castle (2 cols x 4 rows).
    row_order = ["_Sea", "", "_UnderGround", "_Castle"]
    frames_by_cell = {}
    for row, key in enumerate(row_order):
        base, dark, light = THEMES[key] if key else THEMES[""]
        frames_by_cell[(0, row)] = tint(pair[0], base)
        frames_by_cell[(1, row)] = tint(pair[1], base)
    build_sheet(32, 32, frames_by_cell, 2, 4, f"{OUT}/enemy.png")


if __name__ == "__main__":
    build_terrain()
    build_brick_peaces()
    build_coin()
    build_scuttler()

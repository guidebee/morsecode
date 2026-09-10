from PIL import Image, ImageDraw, ImageFont
import os
import sys

sys.path.insert(0, "C:/workspace/morsecode/docs/assets/mario-sprites/ampere-staging")
from sprite_tools import build_sheet, place_content, tint, verified_crop

OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"
K2D = "C:/workspace/Kenney_Game_Assets_All/2D assets"
ROBOT = "C:/workspace/robot_series_base_pack/other"


def ensure_parent(path):
    os.makedirs(os.path.dirname(path), exist_ok=True)


def save(im, rel):
    path = f"{OUT}/{rel}"
    ensure_parent(path)
    im.save(path)


def scifi_tile(sheet, idx):
    cols, tile = 36, 32
    row, col = divmod(idx, cols)
    return verified_crop(sheet, (col * tile, row * tile, (col + 1) * tile, (row + 1) * tile), f"scifi {idx}")


def industrial_tile(idx):
    p = f"{K2D}/Pixel Platformer Industrial Expansion/Tiles/tile_{idx:04d}.png"
    return Image.open(p).convert("RGBA")


def make_bubble():
    fish = f"{K2D}/Fish Pack/PNG/Default"
    src = [
        Image.open(f"{fish}/bubble_a.png").convert("RGBA"),
        Image.open(f"{fish}/bubble_b.png").convert("RGBA"),
        Image.open(f"{fish}/bubble_c.png").convert("RGBA"),
        Image.open(f"{fish}/bubble_b.png").convert("RGBA"),
    ]
    frames = [place_content(s, 8, 14, fill=0.92, anchor="center") for s in src]
    build_sheet(8, 14, frames, 4, 1, f"{OUT}/Bubble.png")


def make_plants():
    # 2x1, 32x48 each frame.
    p0 = Image.open(f"{K2D}/New Platformer Pack/Sprites/Enemies/Default/fish_purple_up.png").convert("RGBA")
    p1 = Image.open(f"{K2D}/New Platformer Pack/Sprites/Enemies/Default/fish_purple_down.png").convert("RGBA")
    f0 = place_content(verified_crop(p0, (0, 0, p0.width, p0.height), "plant0"), 32, 48, fill=0.9, anchor="bottom")
    f1 = place_content(verified_crop(p1, (0, 0, p1.width, p1.height), "plant1"), 32, 48, fill=0.9, anchor="bottom")
    build_sheet(32, 48, [f0, f1], 2, 1, f"{OUT}/plant.png")
    build_sheet(32, 48, [tint(f0, (66, 94, 108), 0.45), tint(f1, (66, 94, 108), 0.45)], 2, 1, f"{OUT}/plantdark.png")


def make_hori_image():
    pump_top = Image.open(f"{OUT}/pump top.png").convert("RGBA")
    left = verified_crop(pump_top, (0, 0, 64, 64), "hori left")
    right = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    right.alpha_composite(left)
    right = tint(right, (120, 190, 250), 0.18)
    sheet = Image.new("RGBA", (128, 64), (0, 0, 0, 0))
    sheet.paste(left, (0, 0), left)
    sheet.paste(right, (64, 0), right)
    save(sheet, "HoriImage.png")


def make_explosion():
    ex = Image.open(f"{ROBOT}/explode-Sheet[64height64wide].png").convert("RGBA")
    frames = []
    for i in (0, 2, 4):
        raw = verified_crop(ex, (i * 64, 0, (i + 1) * 64, 64), f"explosion{i}")
        frames.append(place_content(raw, 32, 32, fill=0.95, anchor="center"))
    build_sheet(32, 32, frames, 3, 1, f"{OUT}/Explosion.png")


def make_aliases_and_bw():
    # Filename aliases expected by PackMarioAtlas.
    Image.open(f"{OUT}/turtle_dark.png").convert("RGBA").save(f"{OUT}/turtledark.png")
    Image.open(f"{OUT}/turtle_shell.png").convert("RGBA").save(f"{OUT}/TurtelShell.png")
    Image.open(f"{OUT}/turtle_shell_dark.png").convert("RGBA").save(f"{OUT}/TurtelShelldark.png")

    # CloudsNight hammer from existing axe strip, resized to 4x(28x28).
    axe = Image.open(f"{OUT}/Axe.png").convert("RGBA")
    frames = [verified_crop(axe, (i * 32, 0, (i + 1) * 32, 32), f"hammer{i}") for i in range(4)]
    bw = [tint(place_content(fr, 28, 28, fill=0.9), (132, 146, 162), 0.55) for fr in frames]
    ensure_parent(f"{OUT}/CloudsNight/Hammer.png")
    build_sheet(28, 28, bw, 4, 1, f"{OUT}/CloudsNight/Hammer.png")


def make_castles_tree_lift():
    castle = Image.open(f"{K2D}/Background Elements/PNG/castle_grey.png").convert("RGBA")
    small = place_content(castle, 160, 160, fill=0.96, anchor="bottom")
    big = place_content(castle, 304, 352, fill=0.96, anchor="bottom")
    save(small, "SmallCastle.png")
    save(big, "BigCastle.png")
    save(tint(small, (118, 128, 146), 0.45), "CloudsNight/SmallCastle.png")
    save(tint(big, (118, 128, 146), 0.45), "CloudsNight/BigCastle.png")

    # 5x2 tree sheet as antenna/pylon motifs from robot platform tiles.
    platform = Image.open(f"{ROBOT}/platform.png").convert("RGBA")
    pyl_a = verified_crop(platform, (6 * 32, 11 * 32, 7 * 32, 12 * 32), "pylon a")
    pyl_b = verified_crop(platform, (7 * 32, 11 * 32, 8 * 32, 12 * 32), "pylon b")
    frames = [pyl_a, pyl_b, pyl_a, pyl_b, pyl_a, pyl_b, pyl_a, pyl_b, pyl_a, pyl_b]
    tree_frames = [place_content(fr, 32, 32, fill=0.96, anchor="center") for fr in frames]
    tree = Image.new("RGBA", (160, 64), (0, 0, 0, 0))
    for i, fr in enumerate(tree_frames):
        x = (i % 5) * 32
        y = (i // 5) * 32
        tree.paste(fr, (x, y), fr)
    save(tree, "tree.png")
    save(tint(tree, (126, 132, 144), 0.48), "CloudsNight/tree.png")

    lift_src = industrial_tile(41)
    lift = place_content(lift_src, 16, 16, fill=0.95, anchor="center")
    save(lift, "Lift.png")


def make_backgrounds():
    peaks = Image.open(f"{K2D}/Background Elements/Samples/uncolored_peaks.png").convert("RGBA")
    plain = Image.open(f"{K2D}/Background Elements/Samples/uncolored_plain.png").convert("RGBA")
    forest = Image.open(f"{K2D}/Background Elements/Samples/uncolored_forest.png").convert("RGBA")
    castle = Image.open(f"{K2D}/Background Elements/Samples/uncolored_castle.png").convert("RGBA")

    mountain = peaks.resize((1536, 448), Image.NEAREST)
    clouds = plain.resize((1536, 448), Image.NEAREST)
    cloudsnight = tint(clouds, (92, 110, 150), 0.35)
    fence = forest.resize((1536, 448), Image.NEAREST)
    fence2 = castle.resize((1536, 448), Image.NEAREST)
    save(mountain, "Mountain.png")
    save(clouds, "Clouds.png")
    save(cloudsnight, "CloudsNight.png")
    save(fence, "Fence.png")
    save(fence2, "Fence2.png")

    # Sea backdrop: from robot series BACKGROUND 2 band.
    platform = Image.open(f"{ROBOT}/platform.png").convert("RGBA")
    sea_band = verified_crop(platform, (0, 128, 64, 320), "sea band")
    sea = place_content(sea_band, 32, 96, fill=1.0, anchor="center")
    save(sea, "Sea.png")


def make_clowd_and_flags():
    stone = Image.open(f"{OUT}/stone_Sea.png").convert("RGBA")
    save(tint(stone, (180, 186, 194), 0.28), "stone_Clowd.png")

    ind75 = place_content(industrial_tile(75), 4, 288, fill=1.0, anchor="center")
    save(ind75, "Flag.png")
    save(tint(ind75, (154, 162, 178), 0.4), "FlagFence.png")

    orb = place_content(industrial_tile(83), 32, 32, fill=0.9, anchor="center")
    save(orb, "FlagSphere.png")
    save(tint(orb, (154, 162, 178), 0.4), "FlagSphereFence.png")

    top = place_content(industrial_tile(41), 32, 32, fill=0.9, anchor="center")
    save(top, "FlagTop.png")

    flagwin = place_content(industrial_tile(21), 32, 32, fill=0.95, anchor="center")
    save(flagwin, "FlagWin.png")


def make_info_font_and_messages():
    font_path = "C:/workspace/Kenney_Game_Assets_All/Other/Fonts/Kenney Blocks.ttf"
    fnt = ImageFont.truetype(font_path, 14)

    # 16x3 cells, 16x16 each.
    sheet = Image.new("RGBA", (256, 48), (0, 0, 0, 0))
    draw = ImageDraw.Draw(sheet)
    chars = [chr(c) for c in range(32, 32 + 48)]
    for i, ch in enumerate(chars):
        x = (i % 16) * 16
        y = (i // 16) * 16
        draw.text((x + 2, y + 1), ch, font=fnt, fill=(226, 236, 248, 255))
    save(sheet, "Font.png")

    bg = Image.open(f"{K2D}/Background Elements/Samples/uncolored_castle.png").convert("RGBA")
    info = bg.resize((640, 480), Image.NEAREST)
    info2 = tint(info, (108, 144, 188), 0.22)
    save(info, "Info.png")
    save(info2, "Info2.png")

    panel = Image.new("RGBA", (384, 128), (24, 36, 52, 255))
    d = ImageDraw.Draw(panel)
    d.text((24, 42), "SYSTEM ONLINE", font=ImageFont.truetype(font_path, 26), fill=(220, 238, 255, 255))
    save(panel, "AnotherCastleMessage.png")

    panel2 = Image.new("RGBA", (384, 128), (30, 44, 64, 255))
    d2 = ImageDraw.Draw(panel2)
    d2.text((44, 42), "MISSION CLEAR", font=ImageFont.truetype(font_path, 26), fill=(240, 248, 255, 255))
    save(panel2, "QuestComplete.png")


def make_cloudsnight_aliases():
    save(Image.open(f"{OUT}/Bouncer.png").convert("RGBA"), "CloudsNight/Bouncer.png")
    save(Image.open(f"{OUT}/RocketLauncher.png").convert("RGBA"), "CloudsNight/RocketLauncher.png")


if __name__ == "__main__":
    make_bubble()
    make_plants()
    make_hori_image()
    make_explosion()
    make_aliases_and_bw()
    make_castles_tree_lift()
    make_backgrounds()
    make_clowd_and_flags()
    make_info_font_and_messages()
    make_cloudsnight_aliases()
    print("wrote remaining source-mapped assets")

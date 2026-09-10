from PIL import Image
import sys

sys.path.insert(0, "C:/workspace/morsecode/docs/assets/mario-sprites/ampere-staging")
from sprite_tools import place_content, tint, verified_crop

OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"
SCIFI = "C:/workspace/Kenney_Game_Assets_All/2D assets/RTS Sci-fi/Tilesheet/scifi_tilesheet.png"


def scifi_tile(sheet, idx):
    cols, tile = 36, 32
    row, col = divmod(idx, cols)
    return verified_crop(sheet, (col * tile, row * tile, (col + 1) * tile, (row + 1) * tile), f"scifi tile {idx}")


def compose_pump(sheet, top_l, top_r, body_l, body_r, tint_rgb=None, tint_strength=0.0):
    body = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    bl = place_content(scifi_tile(sheet, body_l), 32, 32, fill=1.0)
    br = place_content(scifi_tile(sheet, body_r), 32, 32, fill=1.0)
    body.paste(bl, (0, 0), bl)
    body.paste(br, (32, 0), br)

    top = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    tl = place_content(scifi_tile(sheet, top_l), 32, 32, fill=1.0)
    tr = place_content(scifi_tile(sheet, top_r), 32, 32, fill=1.0)
    top.paste(tl, (0, 0), tl)
    top.paste(tr, (32, 0), tr)
    top.paste(bl, (0, 32), bl)
    top.paste(br, (32, 32), br)

    if tint_rgb is not None and tint_strength > 0:
        body = tint(body, tint_rgb, tint_strength)
        top = tint(top, tint_rgb, tint_strength)
    return body, top


if __name__ == "__main__":
    scifi = Image.open(SCIFI).convert("RGBA")

    # RTS Sci-fi conduits per KENNEY index doc §3/§12.3.
    ground_body, ground_top = compose_pump(
        scifi,
        top_l=436,
        top_r=437,
        body_l=472,
        body_r=473,
    )
    castle_body, castle_top = compose_pump(
        scifi,
        top_l=436,
        top_r=437,
        body_l=472,
        body_r=473,
        tint_rgb=(176, 186, 204),
        tint_strength=0.22,
    )
    sea_body, sea_top = compose_pump(
        scifi,
        top_l=438,
        top_r=439,
        body_l=474,
        body_r=475,
    )

    ground_body.save(f"{OUT}/pump.png")
    ground_top.save(f"{OUT}/pump top.png")
    castle_body.save(f"{OUT}/pump Castle.png")
    castle_top.save(f"{OUT}/pump top Castle.png")
    sea_body.save(f"{OUT}/pump Sea.png")
    sea_top.save(f"{OUT}/pump top Sea.png")
    print("wrote pump family (ground/castle/sea)")

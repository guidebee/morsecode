from PIL import Image
import sys

sys.path.insert(0, "C:/workspace/morsecode/docs/assets/mario-sprites/ampere-staging")
from sprite_tools import place_content, verified_crop

OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"
IND = "C:/workspace/Kenney_Game_Assets_All/2D assets/Pixel Platformer Industrial Expansion/Tiles"
SCIFI = "C:/workspace/Kenney_Game_Assets_All/2D assets/RTS Sci-fi/Tilesheet/scifi_tilesheet.png"
ROBOT = "C:/workspace/robot_series_base_pack/other"


def scifi_tile(sheet, idx):
    cols, tile = 36, 32
    row, col = divmod(idx, cols)
    return verified_crop(sheet, (col * tile, row * tile, (col + 1) * tile, (row + 1) * tile), f"scifi tile {idx}")


def scifi_pair_to32(sheet, left_idx, right_idx, label):
    left = scifi_tile(sheet, left_idx)
    right = scifi_tile(sheet, right_idx)
    pair = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    pair.paste(left, (0, 0), left)
    pair.paste(right, (32, 0), right)
    return place_content(verified_crop(pair, (0, 0, 64, 32), label), 32, 32, fill=1.0, anchor="center")


if __name__ == "__main__":
    scifi = Image.open(SCIFI).convert("RGBA")

    # Wall (2x1): cap + body from Industrial Expansion tiles.
    wall_cap = place_content(Image.open(f"{IND}/tile_0016.png").convert("RGBA"), 32, 32, fill=1.0)
    wall_body = place_content(Image.open(f"{IND}/tile_0000.png").convert("RGBA"), 32, 32, fill=1.0)
    wall = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    wall.paste(wall_cap, (0, 0), wall_cap)
    wall.paste(wall_body, (32, 0), wall_body)

    # Rocket launcher (1x4): RTS Sci-fi turret/building stack (index-guided from the sheet).
    # Row mapping expected by LevelLoader.spawnRocketLauncher:
    # 0=head, 1=neck, 2=body, 3+=deep body.
    rl_pairs = [(68, 69), (70, 71), (106, 107), (142, 143)]
    rocket = Image.new("RGBA", (32, 128), (0, 0, 0, 0))
    for i, (lidx, ridx) in enumerate(rl_pairs):
        tile = scifi_pair_to32(scifi, lidx, ridx, f"rocket row {i}")
        rocket.paste(tile, (0, i * 32), tile)

    # Coil motif from Robot Master Series gate.png (per index doc) for bouncer/spring.
    gate = Image.open(f"{ROBOT}/gate.png").convert("RGBA")
    coils = [
        verified_crop(gate, (0, 0, 32, 32), "coil frame0"),
        verified_crop(gate, (8, 0, 40, 32), "coil frame1"),
        verified_crop(gate, (16, 0, 48, 32), "coil frame2"),
    ]
    bridge_base = Image.open(f"{IND}/tile_0004.png").convert("RGBA")

    # Keep bouncer visually as a lower pad, matching original occupancy.
    bouncer_full = place_content(bridge_base, 32, 32, fill=1.0)
    bouncer = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    bouncer_strip = verified_crop(bouncer_full, (0, 14, 32, 32), "bouncer lower strip")
    bouncer.paste(bouncer_strip, (0, 14), bouncer_strip)

    spring = Image.new("RGBA", (96, 64), (0, 0, 0, 0))
    spring_scales = [1.0, 0.84, 0.64]
    for i, scale in enumerate(spring_scales):
        fr = Image.new("RGBA", (32, 64), (0, 0, 0, 0))
        coil_frame = place_content(coils[i], 32, 48, fill=scale, anchor="center")
        fr.paste(coil_frame, (0, 0), coil_frame)
        fr.paste(bouncer, (0, 32), bouncer)
        spring.paste(fr, (i * 32, 0), fr)

    wall.save(f"{OUT}/Wall.png")
    rocket.save(f"{OUT}/RocketLauncher.png")
    bouncer.save(f"{OUT}/Bouncer.png")
    spring.save(f"{OUT}/Spring.png")
    print("wrote Wall.png, RocketLauncher.png, Bouncer.png, Spring.png")

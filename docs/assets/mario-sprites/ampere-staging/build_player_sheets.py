"""
Staging script for Step R.2 (docs/mario/MARIO_RESKIN_EXECUTION.md) - assembles
Ampere's player/BigPlayer/FirePlayer atlas source sheets from the Robot Master
Series - Base Asset Pack, matching MARIO_GAME_MECHANICS.md Sec 13.3's frame-index
convention (4 cols x 7 rows), at native 32px/tile resolution.

ART_SCALE stays 1 permanently for this reskin (decided 2026-09-08, reversing
the earlier ART_SCALE=2/64px-per-tile plan) - see MARIO_RESKIN_EXECUTION.md's
R.0 update for why. Writes directly into
docs/assets/mario-sprites/reskin-source/, which PackMarioAtlas's reskin
overlay reads from.
"""
from PIL import Image, ImageOps
import os

PACK = "C:/workspace/robot_series_base_pack"
OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"
COLS, ROWS = 4, 7

SMALL_CELL = (32, 32)
BIG_CELL = (32, 64)


def load_row_frames(path, frame_w, frame_h):
    im = Image.open(path).convert("RGBA")
    n = im.width // frame_w
    return [im.crop((i * frame_w, 0, (i + 1) * frame_w, frame_h)) for i in range(n)]


def fit_into(frame, cell_w, cell_h, anchor="center"):
    """Nearest-neighbour scale preserving aspect ratio, then pad onto a
    transparent cell_w x cell_h canvas. anchor='center' or 'bottom'."""
    fw, fh = frame.size
    scale = min(cell_w / fw, cell_h / fh)
    nw, nh = max(1, round(fw * scale)), max(1, round(fh * scale))
    scaled = frame.resize((nw, nh), Image.NEAREST)
    canvas = Image.new("RGBA", (cell_w, cell_h), (0, 0, 0, 0))
    x = (cell_w - nw) // 2
    y = (cell_h - nh) // 2 if anchor == "center" else (cell_h - nh)
    canvas.paste(scaled, (x, y), scaled)
    return canvas


def mirror(frame):
    return ImageOps.mirror(frame)


def fit_fill_height(frame, cell_w, cell_h, fill=0.92):
    """Like fit_into, but scales to fill a *fraction of the cell's height*
    directly rather than the aspect-preserving min(w_ratio, h_ratio) - that
    approach silently caps at 1.0x (no upscale at all) whenever the source is
    already as wide as the cell, which is exactly wrong for Big/Fire: a
    32x32-native source going into a 32x64 (twice as tall) cell needs to
    actually get bigger, not just get repositioned in a taller transparent
    canvas. Uniform scale (no distortion) means the result can be wider than
    cell_w - paste() clips that overflow at the cell edges, which reads fine
    for pixel art (a visual bounding box a bit wider than the hitbox is
    normal) rather than shrinking the whole character back down to fit."""
    fw, fh = frame.size
    scale = (fill * cell_h) / fh
    nw, nh = max(1, round(fw * scale)), max(1, round(fh * scale))
    scaled = frame.resize((nw, nh), Image.NEAREST)
    canvas = Image.new("RGBA", (cell_w, cell_h), (0, 0, 0, 0))
    x = (cell_w - nw) // 2
    y = cell_h - nh
    canvas.paste(scaled, (x, y), scaled)
    return canvas


def squash_duck(frame, cell_w, cell_h, anchor="center", fit_fn=None):
    """Approximate a crouch pose from a standing pose: scale the source
    down vertically before fitting, so the character reads as compressed/
    crouched rather than stretched. Placeholder until real duck art exists."""
    fw, fh = frame.size
    squashed = frame.resize((fw, max(1, round(fh * 0.62))), Image.NEAREST)
    canvas_src = Image.new("RGBA", (fw, fh), (0, 0, 0, 0))
    canvas_src.paste(squashed, (0, fh - squashed.height), squashed)
    if fit_fn is not None:
        return fit_fn(canvas_src, cell_w, cell_h)
    return fit_into(canvas_src, cell_w, cell_h, anchor)


def warm_tint(frame, strength=0.35):
    """Blend a warm orange tint over opaque pixels only - used to
    differentiate the Fire/'charged' state from Big/'reinforced' without
    new source art."""
    r, g, b, a = frame.split()
    overlay = Image.new("RGBA", frame.size, (255, 140, 30, 255))
    blended = Image.blend(frame.convert("RGBA"), overlay, strength)
    blended.putalpha(a)
    return blended


def build_sheet(cell_w, cell_h, frame_map, out_path):
    sheet = Image.new("RGBA", (cell_w * COLS, cell_h * ROWS), (0, 0, 0, 0))
    for idx, frame in frame_map.items():
        row, col = divmod(idx, COLS)
        sheet.paste(frame, (col * cell_w, row * cell_h), frame)
    sheet.save(out_path)
    print("wrote", out_path, sheet.size)


def build_small():
    cw, ch = SMALL_CELL
    idle = load_row_frames(f"{PACK}/robot1/robot1.png", 32, 32)
    run = load_row_frames(f"{PACK}/robot1/robo1/robo1run-Sheet[32height32wide].png", 32, 32)
    jump = load_row_frames(f"{PACK}/robot1/robo1/robo1jump-Sheet[48height32wide].png", 32, 48)
    dash = load_row_frames(f"{PACK}/robot1/robo1/robot1dash-Sheet[32height32wide].png", 32, 32)

    idle_r = fit_into(idle[0], cw, ch)
    idle_l = mirror(idle_r)
    air_r = fit_into(jump[3], cw, ch)
    air_l = mirror(air_r)
    walk_r = [fit_into(run[i], cw, ch) for i in (0, 2, 4)]
    walk_l = [mirror(f) for f in walk_r]
    skid_r = fit_into(dash[2], cw, ch)
    skid_l = mirror(skid_r)
    swim_sink_r1 = fit_into(jump[1], cw, ch)
    swim_sink_r2 = fit_into(jump[5], cw, ch)
    swim_rise_r1 = fit_into(jump[2], cw, ch)
    swim_rise_r2 = fit_into(jump[6], cw, ch)
    duck_r = squash_duck(idle[0], cw, ch)
    duck_l = mirror(duck_r)

    frames = {
        0: idle_r, 1: idle_l,
        2: air_r, 3: air_l,
        4: walk_r[0], 5: walk_r[1], 6: walk_r[2],
        7: skid_r,
        8: walk_l[0], 9: walk_l[1], 10: walk_l[2],
        11: skid_l,
        16: swim_sink_r1, 17: mirror(swim_sink_r1),
        18: swim_sink_r2, 19: mirror(swim_sink_r2),
        20: swim_rise_r1, 21: mirror(swim_rise_r1),
        22: swim_rise_r2, 23: mirror(swim_rise_r2),
        24: duck_r, 25: duck_l,
    }
    build_sheet(cw, ch, frames, f"{OUT}/player.png")

    # small_dead_mario: single static pose, robot1's death frame 0 (black
    # "powered down" silhouette) - reads well thematically for a robot.
    death = load_row_frames(f"{PACK}/robot1/robo1/robot1death-Sheet[32height64wide].png", 64, 32)
    dead = fit_into(death[0], cw, ch)
    dead.save(f"{OUT}/SmallDeadMario.png")
    print("wrote", f"{OUT}/SmallDeadMario.png", dead.size)


def build_big_or_fire(tint, out_name):
    cw, ch = BIG_CELL
    idle = load_row_frames(f"{PACK}/robot3/robot3.png", 32, 32)
    run = load_row_frames(f"{PACK}/robot3/robo3/robo3run-Sheet[32height32wide].png", 32, 32)
    jump = load_row_frames(f"{PACK}/robot3/robo3/robo3jump-Sheet[48height32wide].png", 32, 48)
    dash = load_row_frames(f"{PACK}/robot3/robo3/robot3dash-Sheet[32height32wide].png", 32, 32)

    def prep(f):
        out = fit_fill_height(f, cw, ch)
        return warm_tint(out, 0.35) if tint else out

    idle_r = prep(idle[0])
    idle_l = mirror(idle_r)
    air_r = prep(jump[3])
    air_l = mirror(air_r)
    walk_r = [prep(run[i]) for i in (0, 2, 4)]
    walk_l = [mirror(f) for f in walk_r]
    skid_r = prep(dash[2])
    skid_l = mirror(skid_r)
    swim_sink_r1 = prep(jump[1])
    swim_sink_r2 = prep(jump[5])
    swim_rise_r1 = prep(jump[2])
    swim_rise_r2 = prep(jump[6])
    duck_r = squash_duck(idle[0], cw, ch, fit_fn=fit_fill_height)
    if tint:
        duck_r = warm_tint(duck_r, 0.35)
    duck_l = mirror(duck_r)

    frames = {
        0: idle_r, 1: idle_l,
        2: air_r, 3: air_l,
        4: walk_r[0], 5: walk_r[1], 6: walk_r[2],
        7: skid_r,
        8: walk_l[0], 9: walk_l[1], 10: walk_l[2],
        11: skid_l,
        16: swim_sink_r1, 17: mirror(swim_sink_r1),
        18: swim_sink_r2, 19: mirror(swim_sink_r2),
        20: swim_rise_r1, 21: mirror(swim_rise_r1),
        22: swim_rise_r2, 23: mirror(swim_rise_r2),
        24: duck_r, 25: duck_l,
    }
    build_sheet(cw, ch, frames, f"{OUT}/{out_name}")


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    build_small()
    build_big_or_fire(tint=False, out_name="BigPlayer.png")
    build_big_or_fire(tint=True, out_name="FirePlayer.png")

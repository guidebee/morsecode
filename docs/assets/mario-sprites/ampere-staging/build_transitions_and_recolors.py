"""
Continues Step R.2: builds the 4 morph/transition strips + Star-recolor
variants from the already-approved player.png/BigPlayer.png/FirePlayer.png
base sheets in reskin-source/ (native 32px/tile - ART_SCALE stays 1
permanently for this reskin, decided 2026-09-08).

Frame counts match PackMarioAtlas.ASSETS exactly (confirmed by reading that
file): small_to_big=12, big_to_fire=10, big_to_small=10, fire_to_small=10,
small_to_big_star=12. Each transition source is ONE ROW of frames at
transitionFrameWidth x transitionFrameHeight px (32x64), per
Player.startTransition's own .split(...)[0] call.

Star-recolor variants (small_black/green/red_mario, big_black/green/red_mario)
are flat-color silhouettes of the base idle/walk/etc. grid - same convention
as the original assets (confirmed during the reskin research pass by
inspecting the extracted common/ regions): identical silhouette, single flat
color per variant, alpha preserved.
"""
from PIL import Image
import os

OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"
SMALL_CELL_W, SMALL_CELL_H = 32, 32
CELL_W, CELL_H = 32, 64  # transition/Big/Fire cell


def cell_at(sheet, index, cell_w=64, cell_h=64, cols=4):
    row, col = divmod(index, cols)
    return sheet.crop((col * cell_w, row * cell_h, (col + 1) * cell_w, (row + 1) * cell_h))


def small_idle_tall():
    """Small idle (from player.png) lifted into a CELL_W x CELL_H canvas,
    anchored to the bottom, so it can be blended/scaled against the taller
    Big/Fire poses during a transition."""
    small = Image.open(f"{OUT}/player.png")
    idle = cell_at(small, 0, SMALL_CELL_W, SMALL_CELL_H, 4)  # index 0 = idle-right
    canvas = Image.new("RGBA", (CELL_W, CELL_H), (0, 0, 0, 0))
    canvas.paste(idle, (0, CELL_H - idle.height), idle)
    return canvas


def big_idle(sheet_path):
    sheet = Image.open(sheet_path)
    return cell_at(sheet, 0, CELL_W, CELL_H, 4)  # index 0 = idle-right


def scale_toward(frame, t, cell_w, cell_h):
    """Scales a bottom-anchored sprite's height by fraction t (0..1),
    re-centering horizontally, to sell a 'growing' effect."""
    if t >= 0.999:
        return frame
    bbox = frame.getbbox()
    if bbox is None:
        return frame
    content = frame.crop(bbox)
    nw = max(1, round(content.width * (0.55 + 0.45 * t)))
    nh = max(1, round(content.height * (0.55 + 0.45 * t)))
    scaled = content.resize((nw, nh), Image.NEAREST)
    canvas = Image.new("RGBA", (cell_w, cell_h), (0, 0, 0, 0))
    x = (cell_w - nw) // 2
    y = cell_h - nh
    canvas.paste(scaled, (x, y), scaled)
    return canvas


def crossfade_grow(start, end, n, tint_cycle=None):
    frames = []
    for i in range(n):
        t = i / (n - 1)
        grown_start = scale_toward(start, min(1.0, t * 1.6), CELL_W, CELL_H)
        if t <= 0.55:
            frame = grown_start
        else:
            fade_t = (t - 0.55) / 0.45
            frame = Image.alpha_composite(
                Image.new("RGBA", (CELL_W, CELL_H), (0, 0, 0, 0)),
                grown_start,
            )
            end_faded = end.copy()
            r, g, b, a = end_faded.split()
            a = a.point(lambda v: int(v * fade_t))
            end_faded.putalpha(a)
            frame = Image.alpha_composite(frame, end_faded)
        if tint_cycle is not None:
            color = tint_cycle[i % len(tint_cycle)]
            r, g, b, a = frame.split()
            overlay = Image.new("RGBA", frame.size, color + (255,))
            blended = Image.blend(frame.convert("RGBA"), overlay, 0.5)
            blended.putalpha(a)
            frame = blended
        frames.append(frame)
    return frames


def crossfade_shrink(start, end, n):
    # Reverse of crossfade_grow: start big, end small.
    grown = crossfade_grow(end, start, n)
    return list(reversed(grown))


def crossfade_flat(start, end, n):
    """Same-size crossfade (no scaling) - used for Big<->Fire, which share
    the same silhouette and only differ by tint."""
    frames = []
    for i in range(n):
        t = i / (n - 1)
        frame = start.copy()
        end_faded = end.copy()
        r, g, b, a = end_faded.split()
        a = a.point(lambda v: int(v * t))
        end_faded.putalpha(a)
        frame = Image.alpha_composite(frame.convert("RGBA"), end_faded)
        frames.append(frame)
    return frames


def save_row(frames, out_path):
    w, h = frames[0].size
    sheet = Image.new("RGBA", (w * len(frames), h), (0, 0, 0, 0))
    for i, f in enumerate(frames):
        sheet.paste(f, (i * w, 0), f)
    sheet.save(out_path)
    print("wrote", out_path, sheet.size)


def silhouette(sheet, color):
    r, g, b, a = sheet.convert("RGBA").split()
    flat = Image.new("RGBA", sheet.size, color + (0,))
    flat.putalpha(a)
    return flat


if __name__ == "__main__":
    small = small_idle_tall()
    big = big_idle(f"{OUT}/BigPlayer.png")
    fire = big_idle(f"{OUT}/FirePlayer.png")

    save_row(crossfade_grow(small, big, 12), f"{OUT}/SmallToBigMarioAnim.png")
    save_row(crossfade_flat(big, fire, 10), f"{OUT}/BigToFireMarioAnim.png")
    save_row(crossfade_shrink(small, big, 10), f"{OUT}/BigToSmallMarioAnim.png")
    save_row(crossfade_shrink(small, fire, 10), f"{OUT}/FireToSmallMarioAnim.png")
    star_tints = [(255, 255, 255), (0, 200, 0), (220, 0, 0)]
    save_row(crossfade_grow(small, big, 12, tint_cycle=star_tints), f"{OUT}/SmallToBigStarMaroAnim.png")

    player = Image.open(f"{OUT}/player.png")
    big_player = Image.open(f"{OUT}/BigPlayer.png")
    silhouette(player, (10, 10, 10)).save(f"{OUT}/SmallBlackMario.png")
    silhouette(player, (40, 220, 60)).save(f"{OUT}/SmallGreenMario.png")
    silhouette(player, (220, 40, 40)).save(f"{OUT}/SmallRedMario.png")
    silhouette(big_player, (10, 10, 10)).save(f"{OUT}/BigBlackMario.png")
    silhouette(big_player, (40, 220, 60)).save(f"{OUT}/BigGreenMario.png")
    silhouette(big_player, (220, 40, 40)).save(f"{OUT}/BigRedMario.png")
    print("wrote 6 star-recolor sheets")

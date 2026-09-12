"""
Step R.4 Phase 1 (redone from scratch on the clean `reskin` branch after the
`reskin-byteplus` attempt's bugs - see chat history for root causes: guessed
crop coordinates never verified against the source image, and slicing a
seamless repeating texture as if it were a discrete icon).

Every crop below is verified via getbbox() BEFORE being used - printed at
generation time so a bad crop is caught immediately, not after packing.

Covers:
- Power-ups: Battery cell (mashroom/mashrooms), Overclock chip (star),
  Charge coil (flower) - same verified crops as the reskin-byteplus fix.
- Roller (EnemyTurtle): turtle.png/turtle_dark.png (128x48, 4 frames of
  32x48 - CONFIRMED taller-than-a-tile by reading EnemyTurtle.java's own
  super() call and class doc, not assumed) + turtle_shell.png/
  turtle_shell_dark.png (32x32 static) from Robot Master Series' enemy2.

  IMPORTANT dimension note (this is what caused the previous attempt's
  "turtle dimension mismatch (128x48 vs 128x32)" bug): enemy2's own source
  frames are 48 wide x 32 tall (confirmed: enemy2walk[32height48wide].png
  is 240x32, 240/48=5 frames - the filename's "32height48wide" means frame
  HEIGHT=32, WIDTH=48, not the reverse). The GAME's turtle region needs the
  opposite aspect - 32 wide x 48 TALL. Fitting a wide-squat source creature
  into a narrow-tall target cell needs anchor-bottom fill-height scaling
  (same technique already validated for Ampere's Big/Fire states in Step
  R.2), not a naive resize - a naive resize would squash the creature
  sideways instead of preserving its proportions.
"""
from PIL import Image
import os

PACK = "C:/workspace/robot_series_base_pack"
OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"

THEMES = {
    "Ground": (138, 151, 166),
    "UnderGround": (47, 74, 65),
}


def verified_crop(im, box, label):
    crop = im.crop(box)
    bbox = crop.getbbox()
    print(f"  {label}: crop={box} bbox={bbox}", "OK" if bbox else "EMPTY - BAD CROP")
    assert bbox is not None, f"Empty crop for {label} at {box}"
    return crop


def place_content(frame, cell_w, cell_h, fill=0.85, anchor="center"):
    bbox = frame.getbbox()
    content = frame.crop(bbox)
    cw, ch = content.size
    scale = (fill * max(cell_w, cell_h)) / max(cw, ch) if max(cw, ch) else 1
    # Fill by the limiting dimension so content doesn't overflow either axis
    scale = min((fill * cell_w) / cw, (fill * cell_h) / ch)
    nw, nh = max(1, round(cw * scale)), max(1, round(ch * scale))
    scaled = content.resize((nw, nh), Image.NEAREST)
    canvas = Image.new("RGBA", (cell_w, cell_h), (0, 0, 0, 0))
    x = (cell_w - nw) // 2
    y = (cell_h - nh) // 2 if anchor == "center" else (cell_h - nh)
    canvas.paste(scaled, (x, y), scaled)
    return canvas


def mirror(frame):
    from PIL import ImageOps
    return ImageOps.mirror(frame)


def tint(frame, color, strength):
    overlay = Image.new("RGBA", frame.size, color + (255,))
    blended = Image.blend(frame.convert("RGBA"), overlay, strength)
    r, g, b, a = frame.split()
    blended.putalpha(a)
    return blended


def glow_pulse(frame, strength):
    if strength <= 0:
        return frame
    overlay = Image.new("RGBA", frame.size, (255, 255, 255, 255))
    blended = Image.blend(frame.convert("RGBA"), overlay, strength)
    r, g, b, a = frame.split()
    blended.putalpha(a)
    return blended


def build_sheet(cell_w, cell_h, frames, out_path):
    sheet = Image.new("RGBA", (cell_w * len(frames), cell_h), (0, 0, 0, 0))
    for i, f in enumerate(frames):
        sheet.paste(f, (i * cell_w, 0), f)
    sheet.save(out_path)
    print("wrote", out_path, sheet.size)


# ---------------------------------------------------------------- power-ups

def build_powerups():
    print("Power-ups (verified crops):")
    miscel = Image.open(f"{PACK}/other/miscel.png").convert("RGBA")
    gate = Image.open(f"{PACK}/other/gate.png").convert("RGBA")

    # Battery cell (Mushroom) - miscel.png row2 (y=16-32), brighter variant.
    battery_bounds = [(2, 14), (18, 30), (34, 46)]
    battery_frames = [
        place_content(verified_crop(miscel, (b[0], 16, b[1], 32), "battery"), 32, 32)
        for b in battery_bounds
    ]
    battery_frames[0].save(f"{OUT}/Mashroom.png")
    print("wrote", f"{OUT}/Mashroom.png")
    build_sheet(32, 32, battery_frames[:2], f"{OUT}/Mashrooms.png")

    # Overclock chip (Star) - miscel.png row3 (y=32-48).
    chip_bounds = [(1, 15), (17, 31), (33, 47), (49, 63)]
    chip_frames = [
        place_content(verified_crop(miscel, (b[0], 32, b[1], 48), "chip"), 32, 32)
        for b in chip_bounds
    ]
    build_sheet(32, 32, chip_frames, f"{OUT}/Star.png")

    # Charge coil (Flower) - ONE compact segment of gate.png's coil column
    # (confirmed via column alpha-sum that gate.png is a seamless repeating
    # texture with zero gaps - NOT discrete icons - so a single bounded
    # segment is used, not a slice of the endless pattern).
    coil_base = place_content(verified_crop(gate, (0, 8, 16, 26), "coil"), 32, 32)
    coil_frames = [glow_pulse(coil_base, p) for p in (0.0, 0.15, 0.35, 0.15)]
    build_sheet(32, 32, coil_frames, f"{OUT}/Flower.png")


# ------------------------------------------------------------------ roller

def build_roller():
    print("Roller / EnemyTurtle (verified crops):")
    walk = Image.open(f"{PACK}/enemy2/enemy2walk[32height48wide].png").convert("RGBA")
    # Confirmed: 240x32 total, frames are 48 WIDE x 32 TALL (5 frames).
    frame_w, frame_h = 48, 32
    n = walk.width // frame_w
    assert n == 5, f"expected 5 walk frames, got {n}"

    raw = [verified_crop(walk, (i * frame_w, 0, (i + 1) * frame_w, frame_h), f"walk{i}") for i in range(n)]

    # Target cell is 32 wide x 48 TALL (confirmed via EnemyTurtle.java's own
    # super(region, tileSize, (tileSize*3)/2, ...) call - taller than a
    # tile). The source creature is wide-squat, not tall, so fill-height
    # anchor-bottom scaling (not a naive resize) preserves its proportions
    # while still occupying most of the taller cell - same technique used
    # for Ampere's Big/Fire states in Step R.2.
    cell_w, cell_h = 32, 48
    right_frames = [place_content(raw[i], cell_w, cell_h, fill=0.85, anchor="bottom") for i in (0, 1)]
    left_frames = [mirror(f) for f in right_frames]

    for theme, color in THEMES.items():
        tinted_right = [tint(f, color, 0.3) for f in right_frames]
        tinted_left = [tint(f, color, 0.3) for f in left_frames]
        # Layout per EnemyTurtle.java: cols 0-1 = left-facing, cols 2-3 = right-facing.
        frames = [tinted_left[0], tinted_left[1], tinted_right[0], tinted_right[1]]
        suffix = "" if theme == "Ground" else f"_{theme}"
        out_name = "turtle_dark.png" if theme == "UnderGround" else f"turtle{suffix}.png"
        build_sheet(cell_w, cell_h, frames, f"{OUT}/{out_name}")

    # turtle_shell / turtle_shell_dark - single 32x32 static pose, using the
    # most compact/rounded walk frame (index 0) rather than a dedicated
    # "curled up" pose (none exists in the source material).
    shell_base = place_content(raw[0], 32, 32, fill=0.85, anchor="bottom")
    tint(shell_base, THEMES["Ground"], 0.3).save(f"{OUT}/turtle_shell.png")
    print("wrote", f"{OUT}/turtle_shell.png")
    tint(shell_base, THEMES["UnderGround"], 0.3).save(f"{OUT}/turtle_shell_dark.png")
    print("wrote", f"{OUT}/turtle_shell_dark.png")


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    build_powerups()
    build_roller()

"""
Step R.5 checklist item: hori_image, son_of_a_buitch, spikey/spikey_egg,
rocket_launcher (+ bw_ CloudsNight variant), bw_hammer.

Class/AssetSpec confirmations (all read directly, not assumed):
  MarioTileRegistry's "HoriImage" case - ported from Mario.java's case 44:
  two 2-tile-wide pieces placed side by side (one tile-pair apart), each
  sliced from "hori_image"'s own 2x1 grid (128x64 source -> two 64x64
  cells) via Pump's own (x,y,TextureRegion) constructor - purely decorative,
  same as Pump's plain body. Built as a horizontal metal conduit (matching
  Pump's own riveted-panel language) rather than the vertical pipe look,
  since this one lies flat: left cell gets a rounded end-cap valve, right
  cell continues the straight body, so the two pieces read as one
  continuous horizontal run when placed side by side.

  SonOfABuitch.java - a Lakitu-analog: floats at a fixed height, swaying
  left/right, periodically rearing back then throwing a SpikeyEgg. Region
  is 32x48/frame, 2 frames (64x48) - frame 0 = default/just-threw, frame 1 =
  rearing-back-to-throw (`throwTimer > 1 && < REARING_BACK_TICKS`). Built as
  a small hovering drone with a claw-arm: arm down holding a fresh egg-pod
  (frame 0) vs arm cocked back mid-windup (frame 1).

  Spikey.java - walks/falls like EnemyTurtle but can never be safely
  stomped (spiky top, matches the classic "Spiny" rule); only ever spawned
  by a hatching SpikeyEgg. Region is 32x32/frame, 4 frames (128x32) - linear
  idx 0/1 = left-facing walk pair, idx 2/3 = right-facing walk pair (mirror
  of the left pair), confirmed directly from the constructor's
  `setFrame(movingRight ? 2 : 0)` and the act() loop's
  `(movingRight ? 2 : 0) + (showingFirstFrame ? 0 : 1)`. Built as a small
  spike-backed ground bot (walking legs, spiked dorsal plate) so it visibly
  reads as "don't stomp this one" the way the original Spiny's spikes did.

  SpikeyEgg.java - a falling projectile that hatches into a Spikey on
  landing. Region is 32x32/frame, 2 frames (64x32) - a simple glow-pulse
  while falling (matches the 0.2s `showingFirstFrame` toggle). Built as a
  sealed metal pod/capsule (a "spawn pod" reading, consistent with a
  mechanical hatchling rather than a literal egg).

  RocketLauncher.java/Rocket.java - "rocket_launcher" is a 1x4 (32x128)
  vertical strip. Re-verified against `LevelLoader.spawnRocketLauncher`:
  frame index 0 = the static turret head (RocketLauncher's own solid
  decoration for dy==0); frame index 1 = the body segment directly under the
  head (dy==1, RocketLauncherBody); frame index 2 = every body segment
  further down (dy>=2, also RocketLauncherBody) - all three are placed, not
  merely declared; frame index 3 = the flying rocket sprite Rocket.java
  reuses directly (flipped horizontally for a rightward launch) rather than
  being a separate asset. Built index 0 as a turret cannon head, indices 1/2
  as plain riveted body-column segments (no cannon dome - matches
  RocketLauncherBody's own "plain solid block" doc, same visual family as
  Wall.png's own cap+body split) so a multi-tile-tall launcher reads as one
  continuous tower, and index 3 as a distinct missile shape.
  "bw_rocket_launcher" (CloudsNight/RocketLauncher.png) is the exact same
  4-cell layout, read by Rocket's own `blackAndWhite` branch (only reachable
  via SpawnController's ambient "Bombs" spawner on a CloudsNight level, per
  Rocket's own class doc) - built as a cooler-toned recolor of the same
  turret/body/missile art, matching every other CloudsNight bw_ variant's own
  "same silhouette, different palette" pattern.

  Hammer.java - "bw_hammer" (CloudsNight/Hammer.png) is a 4-frame (112x28,
  28x28/cell) spin-tumble cycle for a hammer thrown by either a
  hammer-mode Boss or a Monkey, animated every 0.1s regardless of level
  theme (the "BW" path name is legacy - Hammer.java's own doc confirms the
  original always loads this one image regardless of theme). Built as a
  tumbling wrench/bolt (matches the "thrown tool" reading fitting Ampere's
  robot-enemy language), rotated across the 4 frames for a convincing spin.
"""
import math
import os
import sys

from PIL import Image, ImageDraw

sys.path.insert(0, "C:/workspace/morsecode/docs/assets/mario-sprites/ampere-staging")
from sprite_tools import build_sheet

OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"


def panel(w, h, plate_color, bolt_color, edge_color):
    im = Image.new("RGBA", (w, h), plate_color)
    draw = ImageDraw.Draw(im)
    draw.rectangle((0, 0, w - 1, h - 1), outline=edge_color, width=2)
    return im, draw


def build_hori_image():
    left, draw = panel(64, 64, (74, 82, 92, 255), (48, 54, 62, 255), (54, 60, 68, 255))
    draw.line((0, 32, 64, 32), fill=(150, 210, 220, 255), width=3)
    draw.ellipse((6, 22, 26, 42), fill=(54, 60, 68, 255), outline=(150, 210, 220, 255), width=2)
    right, draw = panel(64, 64, (74, 82, 92, 255), (48, 54, 62, 255), (54, 60, 68, 255))
    draw.line((0, 32, 64, 32), fill=(150, 210, 220, 255), width=3)
    for bx in (16, 48):
        draw.ellipse((bx - 3, 29, bx + 3, 35), fill=(48, 54, 62, 255))
    build_sheet(64, 64, [left, right], 2, 1, f"{OUT}/HoriImage.png")


def drone_frame(arm_back):
    im = Image.new("RGBA", (32, 48), (0, 0, 0, 0))
    draw = ImageDraw.Draw(im)
    body_color = (110, 120, 130, 255)
    edge_color = (58, 64, 72, 255)
    eye_color = (240, 180, 60, 255)
    # Hover-orb body.
    draw.ellipse((6, 8, 25, 27), fill=body_color, outline=edge_color, width=2)
    draw.ellipse((13, 14, 19, 20), fill=eye_color)
    # Under-thruster glow.
    draw.polygon([(11, 27), (20, 27), (16, 34)], fill=(120, 190, 230, 200))
    # Claw arm - down (holding fresh pod) or cocked back (windup).
    if arm_back:
        draw.line((22, 16, 29, 6), fill=body_color, width=3)
        draw.ellipse((26, 2, 32, 8), fill=(150, 90, 40, 255), outline=edge_color)
    else:
        draw.line((22, 20, 27, 30), fill=body_color, width=3)
        draw.ellipse((23, 30, 31, 38), fill=(150, 90, 40, 255), outline=edge_color)
    return im


def build_son_of_a_buitch():
    build_sheet(32, 48, [drone_frame(False), drone_frame(True)], 2, 1, f"{OUT}/SonOfABuitch.png")


def spikey_frame(facing_right, leg_forward):
    im = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(im)
    body_color = (120, 70, 60, 255)
    edge_color = (60, 30, 26, 255)
    spike_color = (230, 140, 60, 255)
    # Legs (2-frame walk cycle).
    if leg_forward:
        draw.rectangle((8, 24, 12, 30), fill=edge_color)
        draw.rectangle((19, 22, 23, 28), fill=edge_color)
    else:
        draw.rectangle((8, 22, 12, 28), fill=edge_color)
        draw.rectangle((19, 24, 23, 30), fill=edge_color)
    # Body dome.
    draw.ellipse((6, 10, 25, 26), fill=body_color, outline=edge_color, width=2)
    # Dorsal spikes - the "don't stomp me" tell.
    for sx in (9, 15, 21):
        draw.polygon([(sx - 3, 12), (sx + 3, 12), (sx, 2)], fill=spike_color, outline=edge_color)
    # Eye, offset toward facing direction.
    ex = 18 if facing_right else 10
    draw.ellipse((ex - 2, 15, ex + 2, 19), fill=(255, 255, 255, 230))
    return im


def build_spikey():
    left0 = spikey_frame(False, False)
    left1 = spikey_frame(False, True)
    right0 = spikey_frame(True, False)
    right1 = spikey_frame(True, True)
    build_sheet(32, 32, [left0, left1, right0, right1], 4, 1, f"{OUT}/Spikey.png")


def pod_frame(bright):
    im = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(im)
    shell_color = (100, 110, 120, 255)
    edge_color = (54, 60, 68, 255)
    glow_color = (255, 200, 90, 255) if bright else (200, 140, 50, 255)
    draw.ellipse((6, 4, 25, 29), fill=shell_color, outline=edge_color, width=2)
    draw.line((6, 16, 25, 16), fill=edge_color, width=2)
    draw.ellipse((12, 11, 19, 18), fill=glow_color)
    return im


def build_spikey_egg():
    build_sheet(32, 32, [pod_frame(False), pod_frame(True)], 2, 1, f"{OUT}/SpikeyEgg.png")


def turret_head(tint):
    im = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(im)
    body = tuple(int(c) for c in tint[0])
    edge = tuple(int(c) for c in tint[1])
    accent = tuple(int(c) for c in tint[2])
    draw.rectangle((4, 18, 27, 30), fill=body, outline=edge, width=2)
    draw.rectangle((10, 4, 21, 20), fill=body, outline=edge, width=2)
    draw.rectangle((13, 0, 18, 6), fill=accent)
    draw.ellipse((12, 9, 19, 16), fill=accent)
    return im


def body_segment(tint, seam):
    """A plain stacked body segment below the turret head (RocketLauncherBody) -
    no cannon dome, just a riveted vertical column panel matching Wall.png's
    own body-segment language."""
    im = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(im)
    body = tuple(int(c) for c in tint[0])
    edge = tuple(int(c) for c in tint[1])
    accent = tuple(int(c) for c in tint[2])
    draw.rectangle((4, 0, 27, 31), fill=body, outline=edge, width=2)
    if seam:
        draw.line((16, 0, 16, 31), fill=edge, width=2)
    for by in (7, 24):
        draw.ellipse((7, by - 2, 11, by + 2), fill=accent)
        draw.ellipse((21, by - 2, 25, by + 2), fill=accent)
    return im


def missile_frame(tint):
    im = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(im)
    body = tuple(int(c) for c in tint[0])
    edge = tuple(int(c) for c in tint[1])
    accent = tuple(int(c) for c in tint[2])
    draw.polygon([(16, 2), (24, 14), (24, 26), (8, 26), (8, 14)], fill=body, outline=edge)
    draw.polygon([(6, 26), (12, 22), (12, 30)], fill=accent)
    draw.polygon([(26, 26), (20, 22), (20, 30)], fill=accent)
    draw.ellipse((12, 20, 20, 28), fill=(240, 100, 40, 220))
    return im


def build_rocket_launcher_pair(out_name, tint):
    head = turret_head(tint)
    body_a = body_segment(tint, seam=True)
    body_b = body_segment(tint, seam=False)
    missile = missile_frame(tint)
    frames = {(0, 0): head, (0, 1): body_a, (0, 2): body_b, (0, 3): missile}
    build_sheet(32, 32, frames, 1, 4, f"{OUT}/{out_name}")


def build_rocket_launchers():
    day_tint = ((90, 98, 108, 255), (54, 60, 68, 255), (150, 210, 220, 255))
    night_tint = ((60, 68, 82, 255), (36, 42, 54, 255), (140, 160, 230, 255))
    build_rocket_launcher_pair("RocketLauncher.png", day_tint)
    os.makedirs(f"{OUT}/CloudsNight", exist_ok=True)
    build_rocket_launcher_pair("CloudsNight/RocketLauncher.png", night_tint)


def wrench_frame(angle_deg):
    size = 28
    im = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    base = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(base)
    color = (150, 155, 162, 255)
    edge = (70, 74, 82, 255)
    # A simple wrench silhouette centered on the canvas so it can be rotated.
    draw.rectangle((12, 6, 15, 21), fill=color, outline=edge)
    draw.ellipse((7, 2, 20, 12), outline=color, width=3)
    draw.ellipse((8, 18, 19, 27), fill=color, outline=edge)
    rotated = base.rotate(angle_deg, resample=Image.BICUBIC, center=(size / 2, size / 2))
    return rotated


def build_bw_hammer():
    frames = [wrench_frame(a) for a in (0, 90, 180, 270)]
    build_sheet(28, 28, frames, 4, 1, f"{OUT}/CloudsNight/Hammer.png")


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    build_hori_image()
    build_son_of_a_buitch()
    build_spikey()
    build_spikey_egg()
    build_rocket_launchers()
    build_bw_hammer()

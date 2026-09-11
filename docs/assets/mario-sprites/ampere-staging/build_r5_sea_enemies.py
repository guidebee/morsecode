"""
Step R.5 checklist: Sea enemies - fish_grey, fish_red, octopussy.

Class confirmations:
  FishyWater.java - "fish_grey"/"fish_red" are tileSize x tileSize (32x32),
    2 frames (2x1) each, a plain 2-frame swim wobble (frame 0/1 toggled on a
    timer, never anything fancier).
  OctoPussy.java - "octopussy" is tileSize x tileSize*1.5 (32x48), 2 frames
    (2x1): frame 0 = normal/resting, frame 1 = a brief "blink" shown only
    while waiting at a rest point (downFrame > 5 and < 20).

octopussy source: Robot Master Series' enemy3 (`enemy3attack-Sheet
[32height32wide].png`, 11 frames of 32x32) - the SAME sheet rejected for
Plater/Helmet (Task 3) because it's a stationary clam/turret, not a walker.
That rejection reasoning doesn't apply here: OctoPussy never walks either
(it drifts/darts toward target points, not a footstep animation), so the
stationary "closed shell" -> "open red eye" motif fits an aquatic
mine/creature well. A per-frame getbbox() scan confirmed frame 0 is fully
closed (smallest bbox) and frame 7 is fully open (largest, red eye visible)
- used as the idle/blink pair.

fish_grey/fish_red: no comparably-fitting small aquatic sprite exists
elsewhere in this pack (checked enemy1/enemy2/enemy3/robot1-3/miniboss1 -
all already used or the wrong shape/size), so built procedurally as a small
torpedo-shaped probe/drone with a 2-frame fin wobble, in this reskin's
existing grey/red enemy-tinting convention (turtle/turtle_dark already
separates by tint, not by different source art).
"""
import os
import sys

from PIL import Image, ImageDraw

sys.path.insert(0, "C:/workspace/morsecode/docs/assets/mario-sprites/ampere-staging")
from sprite_tools import build_sheet, place_content, verified_crop

OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"
PACK = "C:/workspace/robot_series_base_pack"


def build_octopussy():
    print("OctoPussy (verified crops):")
    sheet = Image.open(f"{PACK}/enemy3/enemy3attack-Sheet[32height32wide].png").convert("RGBA")

    def frame(i):
        return verified_crop(sheet, (i * 32, 0, (i + 1) * 32, 32), f"enemy3 frame {i}")

    idle_raw = frame(0)
    blink_raw = frame(7)
    idle = place_content(idle_raw, 32, 48, fill=0.9, anchor="bottom")
    blink = place_content(blink_raw, 32, 48, fill=0.9, anchor="bottom")
    build_sheet(32, 48, [idle, blink], 2, 1, f"{OUT}/OctoPussy.png")


def probe_frame(fin_offset, color):
    """A small torpedo-shaped probe/drone: oval body + a tail fin whose
    vertical offset alternates between frames to read as a swim wobble."""
    im = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(im)
    body_color, dark_color, eye_color = color
    draw.ellipse((6, 11, 26, 21), fill=body_color, outline=dark_color)
    draw.ellipse((20, 13, 25, 19), fill=eye_color)
    draw.polygon([(6, 16), (0, 16 + fin_offset), (6, 20)], fill=dark_color)
    draw.polygon([(6, 12), (0, 12 - fin_offset), (6, 16)], fill=dark_color)
    return im


def build_fish():
    grey = (
        (150, 160, 170, 255), (70, 78, 88, 255), (230, 60, 40, 255),
    )
    red = (
        (200, 70, 60, 255), (110, 30, 24, 255), (255, 220, 90, 255),
    )
    build_sheet(32, 32, [probe_frame(2, grey), probe_frame(-2, grey)], 2, 1, f"{OUT}/FishGrey.png")
    build_sheet(32, 32, [probe_frame(2, red), probe_frame(-2, red)], 2, 1, f"{OUT}/FishRed.png")


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    build_octopussy()
    build_fish()

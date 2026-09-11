"""
Step R.5 checklist: Monkey (the hammer-throwing enemy).

Monkey.java's own class doc (re-verified here, matching the guide's explicit
warning not to assume the 3x2/6-cell grid's meaning without checking) confirms:
  - "monkey" is 32x48 per frame (taller than a tile, like EnemyTurtle), 3
    cols x 2 rows (96x96 total) - same linear index convention as Boss
    (idx = row*3 + col).
  - updateLookAtMarioFrame(): idx0/1 (row0, col0/1) = look-left idle cycle,
    idx4/5 (row1, col1/2) = look-right idle cycle - an exact match for
    Boss's own layout, just a different pixel size. idx2 (row0, col2) and
    idx3 (row1, col0) are never set by any code path - same "unused cell"
    situation as Boss's own idx3.

Source: Robot Master Series' robot2 folder - deliberately NOT enemy1/enemy2
(already used for Scuttler/turtle_shell_dark) or enemy3 (rejected for
Plater/Helmet - a stationary clam, not a walker), and NOT robot1/robot3
(already the two player skins). robot2 is a distinct, still-unused purple
trooper-style biped in this same pack, fitting the guide's "don't force
enemy1/enemy2 to double up" instruction from the opposite direction: there
IS a genuinely fitting, unused source here, unlike Plater/Helmet's actual
gap. `robo2jump-Sheet[48height32wide].png` (224x48, 7 frames of 32x48) is
already the right cell size (32x48) - frames 0 and 3 (upright vs. leaping)
picked as an "idle A"/"idle B" pair, the same 2-distinct-frames-from-one-
loop approach already used for Boss's idle cycle. The sheet's native facing
is right (confirmed by eye against the zoomed preview - the visor points
right, the front leg leads right in the leaping frames), so left-facing
frames are `mirror()` of these, matching Boss's own left/right convention.
"""
import os
import sys

from PIL import Image

sys.path.insert(0, "C:/workspace/morsecode/docs/assets/mario-sprites/ampere-staging")
from sprite_tools import build_sheet, mirror, place_content, verified_crop

OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"
PACK = "C:/workspace/robot_series_base_pack"


def build():
    print("Monkey (verified crops):")
    sheet = Image.open(f"{PACK}/robot2/robo2/robo2jump-Sheet[48height32wide].png").convert("RGBA")
    fw, fh = 32, 48

    def frame(i):
        return verified_crop(sheet, (i * fw, 0, (i + 1) * fw, fh), f"robo2 jump frame {i}")

    idle_a_raw = frame(0)
    idle_b_raw = frame(3)

    cell_w, cell_h = 32, 48
    idle_a = place_content(idle_a_raw, cell_w, cell_h, fill=0.95, anchor="bottom")
    idle_b = place_content(idle_b_raw, cell_w, cell_h, fill=0.95, anchor="bottom")
    left_a, left_b = mirror(idle_a), mirror(idle_b)

    frames_by_cell = {
        (0, 0): left_a,
        (1, 0): left_b,
        (2, 0): left_a,   # idx2 - unused by Monkey.java, filled to avoid a blank cell
        (0, 1): idle_a,   # idx3 - unused by Monkey.java, filled to avoid a blank cell
        (1, 1): idle_a,
        (2, 1): idle_b,
    }
    build_sheet(cell_w, cell_h, frames_by_cell, 3, 2, f"{OUT}/Monkey.png")


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    build()

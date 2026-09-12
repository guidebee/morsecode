"""
Step R.5 checklist: Helmet/Plater family - closes a real, live gap.

This branch's own MARIO_RESKIN_EXECUTION.md flagged Task 3 (Plater/Helmet)
as an investigated-and-rejected gap: enemy3
(enemy3attack-Sheet[32height32wide].png, 11 frames of 32x32, confirmed via a
per-frame getbbox() scan to be a clam/shell creature that opens and closes,
NOT a legged walker) was rejected as "not a walker" and left un-reskinned,
correctly per the guide's own §6 ("stop and flag rather than force a bad
fit") - but Helmet is a real, live enemy type (not dead code), so leaving it
meant six files stayed on unreskinned original Nintendo art in an otherwise-
complete reskin.

Re-examined: a creature whose "movement" reads as opening/closing its own
shell is a reasonable fit for Helmet specifically, since Helmet.java's own
onStomped() already turns it into a HelmetShell (a retracted/closed pose) -
frame 0 (fully shut, smallest content bbox) doubles directly as
"helmet_shell", and two mid-open frames (2 and 5) work as the "walk" pair.
This exact approach was already built, packed, compiled, and visually
confirmed on a separate reskin attempt (the `reskin` branch) - reused here
to close this branch's own flagged gap.

Note this branch's own build_r5_sea_enemies.py already uses this same
enemy3 sheet (frames 0/7, untinted, 32x48) for OctoPussy - to avoid Helmet
and OctoPussy reading as the same creature, ALL THREE Helmet palettes here
are tinted (including "normal", which would otherwise stay the source's
native green and look identical to OctoPussy in Ground-attribute levels).
"Plater" (this asset's identity per MARIO_RESKIN_PLAN.md) is described as
an "armored variant" - a blue-grey metal tone fits that reading and reads
as a distinct creature from OctoPussy's plain green.
"""
import os
import sys

sys.path.insert(0, "C:/workspace/morsecode/docs/assets/mario-sprites/ampere-staging")
from sprite_tools import build_sheet, mirror, place_content, tint, verified_crop
from PIL import Image

OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"
PACK = "C:/workspace/robot_series_base_pack"


def build():
    print("Helmet/HelmetShell family (verified crops):")
    sheet = Image.open(f"{PACK}/enemy3/enemy3attack-Sheet[32height32wide].png").convert("RGBA")
    n = sheet.width // 32

    def frame(i):
        return verified_crop(sheet, (i * 32, 0, (i + 1) * 32, 32), f"enemy3 frame {i}")

    frames = [frame(i) for i in range(n)]
    shell_raw, walk_a_raw, walk_b_raw = frames[0], frames[2], frames[5]

    shell_base = place_content(shell_raw, 32, 32, fill=0.8, anchor="bottom")
    walk_a = place_content(walk_a_raw, 32, 32, fill=0.85, anchor="bottom")
    walk_b = place_content(walk_b_raw, 32, 32, fill=0.85, anchor="bottom")

    palettes = {
        "": ((120, 132, 150), 0.55),      # Ground/Sea - "Plater" armored blue-grey
        "dark": ((25, 35, 30), 0.65),      # UnderGround/Castle - dark mossy
        "white": ((225, 230, 232), 0.55),  # pale/white variant
    }
    for suffix, (color, strength) in palettes.items():
        def maybe_tint(f, color=color, strength=strength):
            return tint(f, color, strength)

        left_a, left_b = mirror(maybe_tint(walk_a)), mirror(maybe_tint(walk_b))
        right_a, right_b = maybe_tint(walk_a), maybe_tint(walk_b)
        fname = {"": "Helmet.png", "dark": "Helmetdark.png", "white": "Helmetwhite.png"}[suffix]
        build_sheet(32, 32, [left_a, left_b, right_a, right_b], 4, 1, f"{OUT}/{fname}")

        shell_fname = {"": "HelmetShell.png", "dark": "HelmetShelldark.png", "white": "HelmetShellwhite.png"}[suffix]
        maybe_tint(shell_base).save(f"{OUT}/{shell_fname}")
        print("wrote", shell_fname)


if __name__ == "__main__":
    build()

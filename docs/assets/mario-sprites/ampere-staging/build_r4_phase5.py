"""
Step R.4 Phase 5.

Covers the remaining turtle shell variants:
- turtle_shell_red
- turtle_shell_flip
- turtle_shell_flip_dark
- turtle_shell_flip_red
"""
from PIL import ImageOps, Image
import os
import sys

sys.path.insert(0, "C:/workspace/morsecode/docs/assets/mario-sprites/ampere-staging")
from sprite_tools import tint

OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"


def build_shell_variants():
    base = Image.open(f"{OUT}/turtle_shell.png").convert("RGBA")
    dark = Image.open(f"{OUT}/turtle_shell_dark.png").convert("RGBA")

    red = tint(base, (210, 70, 70), 0.35)
    red.save(f"{OUT}/TurtelShellRed.png")

    ImageOps.mirror(base).save(f"{OUT}/TurtelShellFilp.png")
    ImageOps.mirror(dark).save(f"{OUT}/TurtelShellFilpdark.png")
    ImageOps.mirror(red).save(f"{OUT}/TurtelShellFilpRed.png")
    print("wrote turtle shell variants")


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    build_shell_variants()

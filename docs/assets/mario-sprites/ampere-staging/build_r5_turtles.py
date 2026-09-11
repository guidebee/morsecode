"""
Step R.5 checklist: the remaining turtle-family variants - flying_turtle(+
_dark), flying_turtle_patrol, enemy_turtle_patrol, and the pre-flipped shell
statics (turtle_shell_red/turtle_shell_flip/_flip_dark/_flip_red).

Class confirmations:
  FlyingTurtle.java - "flying_turtle"/"flying_turtle_dark" are 32x48 per
    frame, 4 frames (128x48): idx0/1 = left-facing pair, idx2/3 =
    right-facing pair (movingRight?2:0 + firstFrame?0:1) - the EXACT same
    layout/tint split as "turtle"/"turtle_dark" (Step R.4 Phase 1), just
    airborne. Reuses that same base creature art plus a small wing/thruster
    addition, rather than a different source - it's meant to read as the
    same Roller enemy, just flying.
  FlyingTurtlePatrol.java - "flying_turtle_patrol" is declared 4x1 (128x48)
    by AssetSpec even though the class only ever calls setFrame(0 or 1) -
    frames 2/3 are unused by any code path (same "declared wider than
    actually used" situation as Boss/Monkey's own spare cells), no
    directional split, ALWAYS the plain/green palette (constructor takes no
    color param) - reuses the right-facing "turtle.png" frames (Ground
    tint) plus the same wing addition.
  EnemyTurtlePatrol.java - "enemy_turtle_patrol" is 32x48, 4 frames (128x48),
    "Always the plain green palette regardless of level theme, even when
    stomped" (confirmed straight from the class doc) - this is pixel-for-
    pixel the SAME art as "turtle.png" (Ground tint), just needed under its
    own region name since MarioResourceManager reads regions by name, not by
    file identity.
  turtle_shell_red / turtle_shell_flip / turtle_shell_flip_dark /
    turtle_shell_flip_red - PackMarioAtlas's own doc: the flip variants are
    pre-flipped STATIC art for whichever mechanic wants it without a
    runtime TextureRegion.flip() call (several classes already do that flip
    themselves at runtime instead, e.g. FlyingTurtle/FlyingTurtlePatrol's own
    onDefeatedByProjectile) - built here as plain horizontal mirrors of the
    already-built turtle_shell/turtle_shell_dark, plus a red tint variant,
    for whatever call site does end up reading these pre-flipped statics.
"""
import os
import sys

from PIL import Image, ImageDraw

sys.path.insert(0, "C:/workspace/morsecode/docs/assets/mario-sprites/ampere-staging")
from sprite_tools import build_sheet, mirror, tint

OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"

THEMES = {
    "Ground": (138, 151, 166),
    "UnderGround": (47, 74, 65),
}
RED = (200, 60, 50)


def add_wings(frame):
    """A small pair of thruster fins added to the turtle's back, so the
    flying variant reads as airborne rather than an identical copy."""
    im = frame.copy()
    draw = ImageDraw.Draw(im)
    draw.polygon([(2, 20), (10, 16), (10, 26)], fill=(190, 210, 220, 230))
    draw.polygon([(2, 20), (10, 16), (10, 26)], outline=(90, 110, 120, 255))
    return im


def build():
    turtle = Image.open(f"{OUT}/turtle.png").convert("RGBA")
    cells = [turtle.crop((i * 32, 0, (i + 1) * 32, 48)) for i in range(4)]  # left0,left1,right0,right1

    # enemy_turtle_patrol - identical art to turtle.png, own region name.
    build_sheet(32, 48, cells, 4, 1, f"{OUT}/EnemyTurtlePatrol.png")

    # flying_turtle / flying_turtle_dark - same 4-cell layout, wings added,
    # retinted per theme (matching turtle.png/turtle_dark.png's own tint).
    winged = [add_wings(c) for c in cells]
    for theme_name, out_name in (("Ground", "FlyingTurtle.png"), ("UnderGround", "FlyingTurtledark.png")):
        color = THEMES[theme_name]
        tinted = [tint(c, color, 0.3) for c in winged]
        build_sheet(32, 48, tinted, 4, 1, f"{OUT}/{out_name}")

    # flying_turtle_patrol - AssetSpec is actually 4x1 (128x48) even though
    # FlyingTurtlePatrol.java only ever calls setFrame(0 or 1) - frames 2/3
    # are unused by any code path (same "declared wider than actually used"
    # situation as Boss/Monkey's own spare cells). 2 frames non-directional,
    # always plain/green - reuse the right-facing pair (cells 2/3 of
    # turtle.png) with wings, Ground tint; frames 2/3 filled with the same
    # pair to avoid a blank cell.
    patrol_a = tint(add_wings(cells[2]), THEMES["Ground"], 0.3)
    patrol_b = tint(add_wings(cells[3]), THEMES["Ground"], 0.3)
    build_sheet(32, 48, [patrol_a, patrol_b, patrol_a, patrol_b], 4, 1, f"{OUT}/FlyingTurtlePatrol.png")

    # Pre-flipped shell statics.
    shell = Image.open(f"{OUT}/TurtelShell.png").convert("RGBA")
    shell_dark = Image.open(f"{OUT}/TurtelShelldark.png").convert("RGBA")
    shell_red = tint(shell, RED, 0.45)
    shell_red.save(f"{OUT}/TurtelShellRed.png")
    print("wrote", f"{OUT}/TurtelShellRed.png", shell_red.size)
    mirror(shell).save(f"{OUT}/TurtelShellFilp.png")
    print("wrote", f"{OUT}/TurtelShellFilp.png")
    mirror(shell_dark).save(f"{OUT}/TurtelShellFilpdark.png")
    print("wrote", f"{OUT}/TurtelShellFilpdark.png")
    mirror(shell_red).save(f"{OUT}/TurtelShellFilpRed.png")
    print("wrote", f"{OUT}/TurtelShellFilpRed.png")


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    build()

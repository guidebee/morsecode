"""
Step R.4, tier-3/4 table row: flying_turtle / flying_turtle_dark /
flying_turtle_patrol / enemy_turtle_patrol / turtle_shell_red /
turtle_shell_flip family (docs/mario/MARIO_RESKIN_JUNIOR_DEV_GUIDE.md Sec5).

Per the guide's own suggestion, reuses Roller's already-built turtle art
(turtle.png/turtle_dark.png, 32x48/frame, cols 0-1=left 2-3=right - confirmed
identical to FlyingTurtle.java's own setFrame() convention by reading the
class) rather than sourcing something new, with a small propeller/wing prop
added on top for the "flying" read.

Consuming classes checked directly (not assumed):
- FlyingTurtle.java: "flying_turtle"/"flying_turtle_dark" 32x48/frame, 4x1,
  same cols convention as EnemyTurtle.
- FlyingTurtlePatrol.java: "flying_turtle_patrol" 32x48/frame, 4x1, but only
  frames 0/1 are ever set (single palette, no left/right distinction - it
  never moves horizontally).
- EnemyTurtlePatrol.java: "enemy_turtle_patrol" 32x48/frame, 4x1, "always the
  plain green palette regardless of level theme" (per its own doc) - same
  convention as EnemyTurtle, so a direct reuse of the Ground-theme turtle
  frames is correct, not a new asset.
- TurtleShell.java's regionFor() only ever picks turtle_shell/turtle_shell_dark;
  turtle_shell_red is used by FlyingTurtlePatrol.onDefeatedByProjectile()
  (confirmed via grep). turtle_shell_flip/_flip_dark/_flip_red are NOT
  referenced anywhere in the Java source (grep-confirmed, zero hits) - genuine
  dead code, not just "rare" - given lowest priority per the guide's own
  note, built here anyway (cheap - a straight recolor of already-built art)
  for completeness rather than left on old Nintendo pixels.
"""
import sys
sys.path.insert(0, "C:/workspace/morsecode/docs/assets/mario-sprites/ampere-staging")
from sprite_tools import tint, mirror, build_sheet
from PIL import Image, ImageDraw
import os

OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"


def add_wings(frame):
    """Overlay a small propeller/rotor prop centered above the creature's
    shell, on a copy of the frame (doesn't mutate the source)."""
    im = frame.copy()
    draw = ImageDraw.Draw(im)
    cx = im.width // 2
    top = 2
    # mast
    draw.line((cx, top + 6, cx, top), fill=(90, 96, 104, 255), width=2)
    # rotor blades (a flattened ellipse crossing the mast)
    draw.ellipse((cx - 9, top - 2, cx + 9, top + 2), outline=(210, 218, 224, 230), width=1)
    draw.ellipse((cx - 2, top - 2, cx + 2, top + 2), fill=(150, 158, 166, 255))
    return im


def load_frames(path):
    im = Image.open(path).convert("RGBA")
    fw, fh = 32, 48
    n = im.width // fw
    return [im.crop((i * fw, 0, (i + 1) * fw, fh)) for i in range(n)]


def build_flying_turtle():
    print("FlyingTurtle / FlyingTurtleDark (Roller art + propeller):")
    for src_name, out_name in (("turtle.png", "FlyingTurtle.png"), ("turtle_dark.png", "FlyingTurtledark.png")):
        frames = load_frames(f"{OUT}/{src_name}")
        winged = [add_wings(f) for f in frames]
        build_sheet(32, 48, winged, 4, 1, f"{OUT}/{out_name}")


def build_flying_turtle_patrol():
    print("FlyingTurtlePatrol (single palette, bob in place):")
    frames = load_frames(f"{OUT}/turtle.png")
    winged = [add_wings(f) for f in frames]
    # Only frames 0/1 are ever shown (no left/right distinction - it never
    # moves horizontally) - cols 2/3 duplicate 0/1 so the sheet is still a
    # valid 4-frame strip matching the AssetSpec's 4x1.
    build_sheet(32, 48, [winged[0], winged[1], winged[0], winged[1]], 4, 1, f"{OUT}/FlyingTurtlePatrol.png")


def build_enemy_turtle_patrol():
    print("EnemyTurtlePatrol (always plain Ground palette - direct reuse):")
    frames = load_frames(f"{OUT}/turtle.png")
    build_sheet(32, 48, frames, 4, 1, f"{OUT}/EnemyTurtlePatrol.png")


def build_shell_variants():
    print("turtle_shell_red / turtle_shell_flip family (recolors of the existing shell):")
    base_shell = Image.open(f"{OUT}/turtle_shell.png").convert("RGBA")
    # turtle_shell_red: used by FlyingTurtlePatrol - a warning-red recolor.
    tint(base_shell, (196, 48, 40), 0.55).save(f"{OUT}/TurtelShellRed.png")
    print("wrote", f"{OUT}/TurtelShellRed.png")

    # turtle_shell_flip family: dead code (grep-confirmed unreferenced), but
    # cheap to fill in rather than leave on stale Nintendo art - a vertically
    # mirrored ("flipped") version of each existing shell tone.
    from PIL import ImageOps
    flipped = ImageOps.flip(base_shell)
    flipped.save(f"{OUT}/TurtelShellFilp.png")
    print("wrote", f"{OUT}/TurtelShellFilp.png")
    ImageOps.flip(Image.open(f"{OUT}/turtle_shell_dark.png").convert("RGBA")).save(f"{OUT}/TurtelShellFilpdark.png")
    print("wrote", f"{OUT}/TurtelShellFilpdark.png")
    ImageOps.flip(Image.open(f"{OUT}/TurtelShellRed.png").convert("RGBA")).save(f"{OUT}/TurtelShellFilpRed.png")
    print("wrote", f"{OUT}/TurtelShellFilpRed.png")


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    build_flying_turtle()
    build_flying_turtle_patrol()
    build_enemy_turtle_patrol()
    build_shell_variants()

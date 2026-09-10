from PIL import Image
import sys

sys.path.insert(0, "C:/workspace/morsecode/docs/assets/mario-sprites/ampere-staging")
from sprite_tools import build_sheet, mirror, place_content, tint, verified_crop

PACK = "C:/workspace/robot_series_base_pack"
OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"


def walk_frames():
    src = Image.open(f"{PACK}/enemy3/enemy3attack-Sheet[32height32wide].png").convert("RGBA")

    # Enemy3 has no dedicated walk strip in this pack; use two adjacent core
    # body frames from the attack sheet and mirror for facing.
    f0 = verified_crop(src, (0, 0, 32, 32), "helmet base frame 0")
    f1 = verified_crop(src, (32, 0, 64, 32), "helmet base frame 1")

    right0 = place_content(f0, 32, 32, fill=0.96, anchor="bottom")
    right1 = place_content(f1, 32, 32, fill=0.96, anchor="bottom")
    left0 = mirror(right0)
    left1 = mirror(right1)
    return left0, left1, right0, right1


def shells(base_frame):
    shell = place_content(base_frame, 32, 32, fill=0.8, anchor="bottom")
    return shell


if __name__ == "__main__":
    l0, l1, r0, r1 = walk_frames()

    normal_walk = [l0, l1, r0, r1]
    dark_walk = [tint(f, (56, 86, 78), 0.38) for f in normal_walk]
    white_walk = [tint(f, (214, 220, 232), 0.35) for f in normal_walk]

    build_sheet(32, 32, normal_walk, 4, 1, f"{OUT}/Helmet.png")
    build_sheet(32, 32, dark_walk, 4, 1, f"{OUT}/Helmetdark.png")
    build_sheet(32, 32, white_walk, 4, 1, f"{OUT}/Helmetwhite.png")

    base_shell = shells(r0)
    dark_shell = tint(base_shell, (56, 86, 78), 0.38)
    white_shell = tint(base_shell, (214, 220, 232), 0.35)

    base_shell.save(f"{OUT}/HelmetShell.png")
    dark_shell.save(f"{OUT}/HelmetShelldark.png")
    white_shell.save(f"{OUT}/HelmetShellwhite.png")

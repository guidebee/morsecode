from PIL import Image
import sys

sys.path.insert(0, "C:/workspace/morsecode/docs/assets/mario-sprites/ampere-staging")
from sprite_tools import build_sheet, mirror, place_content, verified_crop

PACK = "C:/workspace/Kenney_Game_Assets_All/2D assets/Toon Characters/Robot/PNG/Poses"
OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"


def monkey_frames():
    # Use Kenney Toon Characters robot poses as the monkey replacement family:
    # two walk poses + one attack pose.
    walk0 = Image.open(f"{PACK}/character_robot_walk0.png").convert("RGBA")
    walk1 = Image.open(f"{PACK}/character_robot_walk1.png").convert("RGBA")
    attack = Image.open(f"{PACK}/character_robot_attack1.png").convert("RGBA")

    raw0 = verified_crop(walk0, (0, 0, walk0.width, walk0.height), "monkey base frame 0")
    raw1 = verified_crop(walk1, (0, 0, walk1.width, walk1.height), "monkey base frame 1")
    raw2 = verified_crop(attack, (0, 0, attack.width, attack.height), "monkey throw frame")

    right0 = place_content(raw0, 32, 48, fill=0.96, anchor="bottom")
    right1 = place_content(raw1, 32, 48, fill=0.96, anchor="bottom")
    right_throw = place_content(raw2, 32, 48, fill=0.96, anchor="bottom")

    left0 = mirror(right0)
    left1 = mirror(right1)
    left_throw = mirror(right_throw)

    # 3x2 row-major grid expected by Monkey.java:
    # 0/1 = left idle cycle, 2 = left throw pose,
    # 3 = right throw pose, 4/5 = right idle cycle.
    #
    # BUG FIX (same root cause as Boss.png - see that file's own note):
    # build_sheet's flat-list shorthand only fills row 0. A flat 6-item
    # list here silently dropped the entire row-1 intent (right_throw,
    # right0, right1 - Monkey.java's own look-right idle pair), making the
    # monkey invisible whenever facing right. Return the explicit
    # {(col, row): frame} mapping instead.
    return {
        (0, 0): left0, (1, 0): left1, (2, 0): left_throw,
        (0, 1): right_throw, (1, 1): right0, (2, 1): right1,
    }


if __name__ == "__main__":
    build_sheet(32, 48, monkey_frames(), 3, 2, f"{OUT}/Monkey.png")
    print("wrote Monkey.png")

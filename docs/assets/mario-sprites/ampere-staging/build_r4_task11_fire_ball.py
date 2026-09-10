from PIL import Image
import sys

sys.path.insert(0, "C:/workspace/morsecode/docs/assets/mario-sprites/ampere-staging")
from sprite_tools import build_sheet, place_content, tint, verified_crop

PACK = "C:/workspace/robot_series_base_pack/enemy1"
OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"


if __name__ == "__main__":
    fx = Image.open(f"{PACK}/enemy1_attack_effect[32height32wide].png").convert("RGBA")
    frame_w, frame_h = 32, 32
    count = fx.width // frame_w
    assert count >= 4, f"expected >= 4 source frames, got {count}"

    frames = []
    pulse_tints = [
        ((238, 98, 76), 0.20),
        ((248, 122, 82), 0.28),
        ((255, 152, 92), 0.36),
        ((240, 92, 70), 0.24),
    ]
    for i in range(4):
        raw = verified_crop(
            fx,
            (i * frame_w, 0, (i + 1) * frame_w, frame_h),
            f"fire_ball src frame {i}",
        )
        fr = place_content(raw, 16, 16, fill=0.92, anchor="center")
        tint_rgb, alpha = pulse_tints[i]
        frames.append(tint(fr, tint_rgb, alpha))

    build_sheet(16, 16, frames, 4, 1, f"{OUT}/FireBall.png")
    print("wrote FireBall.png")

from PIL import Image
import sys

sys.path.insert(0, "C:/workspace/morsecode/docs/assets/mario-sprites/ampere-staging")
from sprite_tools import build_sheet, glow_pulse, mirror, place_content, verified_crop

PACK = "C:/workspace/robot_series_base_pack/miniboss1"
OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"


def boss_frames():
    base = Image.open(f"{PACK}/miniboss1_base[80height144wide].png").convert("RGBA")
    frame_w, frame_h = 144, 80
    count = base.width // frame_w
    assert count == 11, f"expected 11 miniboss base frames, got {count}"

    # Use adjacent base animation frames for idle, and a dedicated "spit"
    # variant built by adding a small muzzle pulse at the cannon tip.
    raw0 = verified_crop(base, (0, 0, frame_w, frame_h), "boss base frame 0")
    raw1 = verified_crop(base, (frame_w, 0, frame_w * 2, frame_h), "boss base frame 1")

    right0 = place_content(raw0, 64, 64, fill=0.96, anchor="bottom")
    right1 = place_content(raw1, 64, 64, fill=0.96, anchor="bottom")

    spit = right0.copy()
    pulse = Image.new("RGBA", (12, 6), (240, 70, 40, 255))
    pulse = glow_pulse(pulse, 0.35)
    # Right-facing muzzle at the front-right of the tank body.
    spit.alpha_composite(pulse, (51, 35))

    left0 = mirror(right0)
    left1 = mirror(right1)
    left_spit = mirror(spit)

    # 3x2 layout, row-major indices:
    # 0/1 left idle, 2 spit pose, 4/5 right idle (Boss.java).
    # Index 3 is currently unused by code; keep a coherent mirrored spit pose.
    #
    # BUG FIX (found by a reskin-source content-bbox audit - row 1 was
    # entirely empty, getbbox()==None for all 3 cells): build_sheet's flat-
    # list shorthand only fills row 0 ({(i, 0): frame for i, frame in
    # enumerate(list)}, per its own docstring) - it does NOT wrap into
    # additional rows just because `rows=2` is passed. Returning a flat
    # 6-item list here silently pasted frames 3/4/5 at columns 3/4/5 of row
    # 0, entirely outside the 192px-wide (3-column) canvas, dropping the
    # ENTIRE look-right idle pair Boss.java actually reads (frames 4/5) -
    # the boss was invisible whenever facing right. Return the proper
    # {(col, row): frame} mapping instead of a flat list.
    return {
        (0, 0): left0, (1, 0): left1, (2, 0): spit,
        (0, 1): left_spit, (1, 1): right0, (2, 1): right1,
    }


def boss_fire_frames():
    # miniboss laser core is 16x16; fit into BossFire's 48x16 frame shape.
    laser = Image.open(f"{PACK}/miniboss1_laser.png").convert("RGBA")
    core = verified_crop(laser, (0, 0, 16, 16), "boss_fire core")

    frame0 = place_content(core, 48, 16, fill=0.82, anchor="center")
    frame1 = glow_pulse(frame0, 0.3)
    return [frame0, frame1]


if __name__ == "__main__":
    build_sheet(64, 64, boss_frames(), 3, 2, f"{OUT}/Boss.png")
    build_sheet(48, 16, boss_fire_frames(), 2, 1, f"{OUT}/BossFire.png")

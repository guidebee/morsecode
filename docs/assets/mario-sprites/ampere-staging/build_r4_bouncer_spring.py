"""
Step R.5 (guide S5 checklist): Bouncer + its decorative Spring.

Bouncer.java confirms: a plain solid launch-pad block, 1 frame, tileSize x
tileSize (32x32) - "bouncer" AssetSpec is 1x1.

Spring.java confirms: a squish-and-recover strip, 3 frames (SQUISH_FRAMES =
{0,1,2,2,1,0}), rendered tileSize wide x tileSize*2 tall (32x64, TWO tiles
tall) - "spring" AssetSpec is 3x1 (three 32x64 cells laid out horizontally).

Source: other/gate.png (208x32), the SAME sheet this reskin's Charge coil
(flower.png) already uses one small window of. A column-density scan (see
docs/mario/MARIO_RESKIN_EXECUTION.md's entry for this task) shows it's
actually built from thirteen 16px-wide segments: segments 0-5 are a full,
dense 6-ring stacked-disc coil (fully "extended"), and segments 6-12
progressively lose rings from the TOP, i.e. it's already a bottom-anchored
spring-compression sequence baked into the source art - not just a texture
to slice arbitrarily (contrast with the Charge coil's use of this same file,
which explicitly used is_tileable_texture's warning to justify a small
single-window crop instead of arbitrary slicing; here we deliberately use
several of its own distinct, already-meaningful segments instead).

Frame mapping (bottom-anchored, matches Spring.java's own SQUISH_FRAMES[0]
"rest" being the tallest/least-compressed):
  frame 0 (rest, fully extended)   -> segment 5 (last full 6-ring segment)
  frame 1 (partial squish)         -> segment 9 (mid-compression)
  frame 2 (max squish)             -> segment 12 (final, most-compressed)

Each 16x32 segment is a plain 2x NEAREST upscale directly onto the 32x64
cell (16*2=32, 32*2=64 - an exact fit, no place_content scale-to-fit
needed/wanted here since that would re-anchor/distort the deliberately
bottom-fixed compression look).

Bouncer reuses the SAME rest-segment (5) as a static single 32x32 tile (the
solid launch pad sitting under the Spring), so the two read as one
consistent piece of hardware.
"""
import os
import sys

from PIL import Image

sys.path.insert(0, "C:/workspace/morsecode/docs/assets/mario-sprites/ampere-staging")
from sprite_tools import build_sheet, place_content, verified_crop

OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"
PACK = "C:/workspace/robot_series_base_pack"


def gate_segment(gate, index):
    x0 = index * 16
    return verified_crop(gate, (x0, 0, x0 + 16, 32), f"gate segment {index}")


def build():
    print("Bouncer/Spring (verified crops):")
    gate = Image.open(f"{PACK}/other/gate.png").convert("RGBA")

    rest_seg = gate_segment(gate, 5)
    mid_seg = gate_segment(gate, 9)
    max_seg = gate_segment(gate, 12)

    def upscale_2x(seg):
        return seg.resize((seg.width * 2, seg.height * 2), Image.NEAREST)

    spring_frames = [upscale_2x(rest_seg), upscale_2x(mid_seg), upscale_2x(max_seg)]
    build_sheet(32, 64, spring_frames, 3, 1, f"{OUT}/Spring.png")

    bouncer_frame = place_content(rest_seg, 32, 32, fill=0.95, anchor="bottom")
    build_sheet(32, 32, [bouncer_frame], 1, 1, f"{OUT}/Bouncer.png")


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    build()

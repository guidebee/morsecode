"""
Step R.5 checklist: Axe (the castle-bridge lever at the end of a boss level).

Axe.java confirms: "axe" is tileSize x tileSize per frame (32x32, 4 frames,
128x32), cycled 0,1,2,3,2,1 by setFrameSequence - a back-and-forth motion,
not a one-way strip. Built procedurally as a mechanical lever/switch icon
(fits "chop the rope" bridge-collapse trigger thematically as well as a
literal axe would, and matches this reskin's console/switch visual language
already used for QuestionMark's beveled panel) - 4 frames sweeping the
lever handle from left to right.
"""
import math
import os
import sys

from PIL import Image, ImageDraw

sys.path.insert(0, "C:/workspace/morsecode/docs/assets/mario-sprites/ampere-staging")
from sprite_tools import build_sheet

OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"


def lever_frame(angle_deg):
    im = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(im)
    # Base console.
    draw.rectangle((4, 22, 27, 29), fill=(70, 76, 84, 255), outline=(40, 44, 50, 255))
    draw.ellipse((13, 20, 19, 26), fill=(50, 56, 64, 255))
    pivot = (16, 23)
    length = 16
    rad = math.radians(angle_deg)
    tip = (pivot[0] + length * math.sin(rad), pivot[1] - length * math.cos(rad))
    draw.line((pivot, tip), fill=(200, 60, 40, 255), width=4)
    draw.ellipse((tip[0] - 3, tip[1] - 3, tip[0] + 3, tip[1] + 3), fill=(255, 210, 90, 255))
    draw.ellipse((pivot[0] - 3, pivot[1] - 3, pivot[0] + 3, pivot[1] + 3), fill=(30, 34, 40, 255))
    return im


def build():
    frames = [lever_frame(a) for a in (-30, -10, 10, 30)]
    build_sheet(32, 32, frames, 4, 1, f"{OUT}/Axe.png")


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    build()

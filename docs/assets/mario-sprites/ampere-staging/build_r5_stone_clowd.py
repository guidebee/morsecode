"""
stone_clowd - the separate solid-terrain tile for "Clowd" bonus-area levels
(distinct AssetSpec region from bw_stone/CloudsNight). Left un-reskinned in
this branch (a real, if minor, gap - closed here). A cheap recolor of the
same procedural stone_tile design already used for every other theme in
build_terrain_and_common.py, at a neutral steel-grey tone distinct from the
existing 4 theme palettes and from bw_stone's own greyscale.
"""
import os
import sys

sys.path.insert(0, "C:/workspace/morsecode/docs/assets/mario-sprites/ampere-staging")
from build_terrain_and_common import stone_tile

OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"

if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    base, dark, light = (100, 104, 112), (52, 56, 62), (168, 174, 182)
    stone_tile(base, dark, light).save(f"{OUT}/stone_Clowd.png")
    print("wrote stone_Clowd.png")

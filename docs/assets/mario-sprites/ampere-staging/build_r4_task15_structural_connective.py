from PIL import Image
import sys

sys.path.insert(0, "C:/workspace/morsecode/docs/assets/mario-sprites/ampere-staging")
from sprite_tools import place_content, tint, verified_crop

OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"
IND = "C:/workspace/Kenney_Game_Assets_All/2D assets/Pixel Platformer Industrial Expansion/Tiles"


def tile(name, label):
    im = Image.open(f"{IND}/{name}").convert("RGBA")
    return place_content(verified_crop(im, (0, 0, 18, 18), label), 32, 32, fill=1.0)


if __name__ == "__main__":
    # Source-only replacements from Industrial Expansion tiles (no procedural draw).
    bridge_blocks = tile("tile_0004.png", "bridge_blocks")
    wooden_bridge = tile("tile_0020.png", "wooden_bridge")
    white_line = tile("tile_0075.png", "white_line")
    rope = tile("tile_0074.png", "rope")

    chain_frames = [
        tile("tile_0071.png", "chain0"),
        tile("tile_0072.png", "chain1"),
        tile("tile_0071.png", "chain2"),
        tint(tile("tile_0072.png", "chain3"), (170, 190, 214), 0.16),
    ]
    chain = Image.new("RGBA", (128, 32), (0, 0, 0, 0))
    for i, fr in enumerate(chain_frames):
        chain.paste(fr, (i * 32, 0), fr)

    bridge_blocks.save(f"{OUT}/BridgeBloks.png")
    wooden_bridge.save(f"{OUT}/WoodenBridge.png")
    white_line.save(f"{OUT}/WhiteLine.png")
    chain.save(f"{OUT}/Chain.png")
    rope.save(f"{OUT}/Rope.png")
    print("wrote BridgeBloks.png, WoodenBridge.png, WhiteLine.png, Chain.png, Rope.png")

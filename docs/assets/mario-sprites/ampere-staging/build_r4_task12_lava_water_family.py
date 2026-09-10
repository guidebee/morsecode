from PIL import Image, ImageDraw
import sys

sys.path.insert(0, "C:/workspace/morsecode/docs/assets/mario-sprites/ampere-staging")
from sprite_tools import place_content, tint, verified_crop

SRC = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"
OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"


def make_column(fill_rgb, crest_rgb, foam_rgb):
    # Preserve original asset geometry from C:/workspace/Mario/SandBox:
    # Lava/Water are 32x128 single-frame textures.
    im = Image.new("RGBA", (32, 128), (*fill_rgb, 255))
    d = ImageDraw.Draw(im)

    # Crest silhouette near the top edge.
    crest = [
        (0, 8), (4, 4), (8, 7), (12, 3), (16, 8), (20, 4), (24, 6), (28, 2), (31, 7),
        (31, 18), (0, 18),
    ]
    d.polygon(crest, fill=(*crest_rgb, 255))

    # Foam highlights to keep the top edge readable against dark backgrounds.
    d.polygon([(1, 7), (4, 5), (7, 8), (9, 7), (9, 10), (1, 10)], fill=(*foam_rgb, 255))
    d.polygon([(12, 4), (14, 3), (17, 7), (14, 10), (12, 9)], fill=(*foam_rgb, 255))
    d.polygon([(21, 5), (24, 4), (27, 5), (25, 8), (21, 8)], fill=(*foam_rgb, 255))

    # Subtle vertical shading bands.
    for y in range(20, 128):
        if y % 10 < 5:
            d.line((0, y, 31, y), fill=(0, 0, 0, 12))

    return im


def make_lava_ball():
    # Reuse the fireball family conceptually, but scale to LavaBall's 32x32 frames.
    fire = Image.open(f"{SRC}/FireBall.png").convert("RGBA")
    f0 = verified_crop(fire, (0, 0, 16, 16), "lava_ball base fire frame 0")
    f1 = verified_crop(fire, (16, 0, 32, 16), "lava_ball base fire frame 1")

    core0 = place_content(f0, 32, 32, fill=0.9, anchor="center")
    core1 = place_content(f1, 32, 32, fill=0.9, anchor="center")

    halo0 = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    h0 = ImageDraw.Draw(halo0)
    h0.ellipse((3, 3, 28, 28), fill=(255, 118, 72, 74))
    h0.ellipse((7, 7, 24, 24), fill=(255, 168, 96, 92))

    halo1 = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    h1 = ImageDraw.Draw(halo1)
    h1.ellipse((2, 2, 29, 29), fill=(255, 124, 76, 90))
    h1.ellipse((6, 6, 25, 25), fill=(255, 182, 110, 108))

    frame0 = Image.alpha_composite(halo0, tint(core0, (255, 120, 82), 0.15))
    frame1 = Image.alpha_composite(halo1, tint(core1, (255, 146, 96), 0.22))

    sheet = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    sheet.paste(frame0, (0, 0), frame0)
    sheet.paste(frame1, (32, 0), frame1)
    return sheet


if __name__ == "__main__":
    lava = make_column(fill_rgb=(186, 54, 20), crest_rgb=(132, 34, 18), foam_rgb=(245, 224, 146))
    water = make_column(fill_rgb=(40, 146, 212), crest_rgb=(30, 104, 166), foam_rgb=(226, 246, 255))
    lava_ball = make_lava_ball()

    lava.save(f"{OUT}/Lava.png")
    water.save(f"{OUT}/Water.png")
    lava_ball.save(f"{OUT}/LavaBall.png")
    print("wrote Lava.png, Water.png, LavaBall.png")

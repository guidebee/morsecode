from PIL import Image, ImageDraw

OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"


def axe_frame(phase):
    im = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)

    blade = [
        (5, 7), (13, 3), (19, 5), (25, 11), (23, 17), (17, 19),
        (15, 16), (18, 12), (14, 10), (10, 14), (6, 13),
    ]
    d.polygon(blade, fill=(188, 206, 224, 255))
    d.polygon([(8, 9), (13, 6), (16, 7), (12, 11), (8, 11)], fill=(236, 246, 255, 235))

    # Core pulse gives a 4-frame shimmer for Axe.java's animation strip.
    pulse = [(255, 170, 96, 210), (255, 186, 108, 240), (255, 152, 90, 220), (255, 138, 82, 200)][phase]
    d.ellipse((11, 10, 16, 15), fill=pulse)

    d.rectangle((14, 15, 17, 31), fill=(106, 64, 42, 255))
    d.rectangle((15, 16, 16, 31), fill=(156, 100, 64, 220))
    d.rectangle((12, 21, 19, 24), fill=(188, 106, 58, 255))
    d.rectangle((13, 22, 18, 23), fill=(230, 150, 92, 220))
    return im


if __name__ == "__main__":
    sheet = Image.new("RGBA", (128, 32), (0, 0, 0, 0))
    for i in range(4):
        fr = axe_frame(i)
        sheet.paste(fr, (i * 32, 0), fr)
    sheet.save(f"{OUT}/Axe.png")
    print("wrote Axe.png")

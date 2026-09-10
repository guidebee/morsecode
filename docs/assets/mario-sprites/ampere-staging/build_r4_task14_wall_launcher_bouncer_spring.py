from PIL import Image, ImageDraw

OUT = "C:/workspace/morsecode/docs/assets/mario-sprites/reskin-source"


def wall_sheet():
    # 2x1, 32x32 per frame: cap + body.
    sheet = Image.new("RGBA", (64, 32), (0, 0, 0, 0))

    cap = Image.new("RGBA", (32, 32), (56, 64, 78, 255))
    d = ImageDraw.Draw(cap)
    d.rectangle((0, 0, 31, 5), fill=(94, 106, 124, 255))
    d.rectangle((1, 7, 30, 30), fill=(72, 82, 98, 255))
    d.rectangle((3, 9, 28, 28), outline=(120, 136, 156, 210), width=1)

    body = Image.new("RGBA", (32, 32), (58, 66, 80, 255))
    b = ImageDraw.Draw(body)
    for y in (4, 12, 20, 28):
        b.line((0, y, 31, y), fill=(44, 50, 62, 255))
    for x in (8, 20):
        b.line((x, 4, x, 11), fill=(44, 50, 62, 255))
        b.line((x + 4, 12, x + 4, 19), fill=(44, 50, 62, 255))
        b.line((x - 3, 20, x - 3, 27), fill=(44, 50, 62, 255))

    sheet.paste(cap, (0, 0), cap)
    sheet.paste(body, (32, 0), body)
    return sheet


def rocket_launcher_sheet():
    # 1x4, 32x32 rows: head, neck, body, deep body.
    sheet = Image.new("RGBA", (32, 128), (0, 0, 0, 0))
    d = ImageDraw.Draw(sheet)

    # Head (row 0)
    d.rectangle((4, 4, 27, 29), fill=(46, 52, 66, 255))
    d.rectangle((6, 6, 25, 27), fill=(66, 76, 94, 255))
    d.ellipse((10, 11, 21, 22), fill=(18, 24, 34, 255))
    d.ellipse((13, 14, 18, 19), fill=(244, 152, 86, 220))
    d.rectangle((9, 25, 22, 27), fill=(100, 114, 138, 220))

    # Neck (row 1)
    d.rectangle((8, 40, 23, 63), fill=(58, 66, 82, 255))
    d.rectangle((11, 42, 20, 63), fill=(80, 92, 112, 240))

    # Body (row 2)
    d.rectangle((6, 68, 25, 95), fill=(52, 60, 74, 255))
    for y in (72, 79, 86, 93):
        d.line((7, y, 24, y), fill=(34, 40, 50, 230))

    # Deep body (row 3, currently mostly unused but kept coherent).
    d.rectangle((5, 100, 26, 127), fill=(42, 48, 60, 255))
    for y in (104, 111, 118, 125):
        d.line((6, y, 25, y), fill=(28, 32, 40, 230))

    return sheet


def bouncer_frame():
    im = Image.new("RGBA", (32, 32), (62, 72, 90, 255))
    d = ImageDraw.Draw(im)
    d.rectangle((0, 0, 31, 5), fill=(208, 72, 34, 255))
    d.rectangle((0, 6, 31, 8), fill=(246, 120, 62, 255))
    for y in (14, 22, 30):
        d.line((0, y, 31, y), fill=(44, 52, 66, 255))
    for x in (8, 20):
        d.line((x, 9, x, 13), fill=(44, 52, 66, 255))
        d.line((x + 4, 15, x + 4, 21), fill=(44, 52, 66, 255))
        d.line((x - 3, 23, x - 3, 29), fill=(44, 52, 66, 255))
    return im


def spring_sheet():
    # 3x1, 32x64 per frame for Spring.java's tileSize x tileSize*2.
    sheet = Image.new("RGBA", (96, 64), (0, 0, 0, 0))
    top_colors = [(250, 132, 68, 255), (244, 118, 58, 255), (236, 106, 52, 255)]

    # Coil bounding boxes by frame (rest -> squish -> compressed).
    boxes = [(6, 8, 25, 50), (6, 16, 25, 46), (6, 24, 25, 40)]
    for i in range(3):
        fr = Image.new("RGBA", (32, 64), (0, 0, 0, 0))
        d = ImageDraw.Draw(fr)
        d.rectangle((0, 6, 31, 10), fill=top_colors[i])

        x0, y0, x1, y1 = boxes[i]
        d.ellipse((x0, y0, x1, y1), outline=(214, 224, 238, 255), width=3)
        d.ellipse((x0 + 3, y0 + 3, x1 - 3, y1 - 3), outline=(122, 142, 166, 210), width=1)

        d.rectangle((0, 58, 31, 63), fill=(62, 72, 90, 255))
        d.rectangle((0, 56, 31, 58), fill=(208, 72, 34, 255))
        sheet.paste(fr, (i * 32, 0), fr)
    return sheet


if __name__ == "__main__":
    wall_sheet().save(f"{OUT}/Wall.png")
    rocket_launcher_sheet().save(f"{OUT}/RocketLauncher.png")
    bouncer_frame().save(f"{OUT}/Bouncer.png")
    spring_sheet().save(f"{OUT}/Spring.png")
    print("wrote Wall.png, RocketLauncher.png, Bouncer.png, Spring.png")

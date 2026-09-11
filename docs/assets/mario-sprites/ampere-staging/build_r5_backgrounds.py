"""R.5 -- parallax backdrops: mountain/clouds/cloudsnight/fence/fence2 (each
1536x448, tiled 10x side-by-side by `BackgroundBand`, transparent except for
a handful of small silhouette blobs) and sea_background (Sea.png, 32x96,
tiled every 32px as a full-height backdrop for Sea-attribute levels).

Consumers (see MARIO_RESKIN_EXECUTION.md for full citations):
  - `MarioGameScreen.backgroundBandRegion` maps a level's `backgroundImage`
    string (Mountain/Clouds/CloudsNight/Fence/Fence2) to the matching
    region name; `BackgroundBand` repeats the 1536x448 image 10x starting
    at (0, 32) -- confirmed 1536x448 native so 10 repeats tile exactly.
  - `BackgroundBand`'s second constructor (the Sea branch) repeats
    `sea_background` every 32px (not the image's own width) starting at
    (0, 0) across the level's actual length -- Sea.png is 32x96 so this is
    an edge-to-edge tile, not a sparse repeat.

Approach: rather than hand-guess replacement art, each source PNG's small
opaque silhouette blobs (mountain fill, cloud puffs, fence post + ball
ornament) are found programmatically (flood-fill connected-component scan
over the alpha channel, no scipy available so a plain BFS is used) and each
bounding box is redrawn in-place as a themed shape -- keeping the exact
same footprint/position rhythm the original had (important since these
tile 10x; a shifted silhouette would visibly seam/repeat wrong), just with
sci-fi silhouettes: the mountain fill becomes an angular tech ridge with a
few lit window dots, the small white/cyan cloud puffs become drifting
drone/satellite silhouettes (cyan glow underside, matching the original's
own cyan-vs-red day/night split for Clouds/CloudsNight), and the fence
post+ball becomes a panel post with a glowing beacon orb (reusing the same
orb language as `flag_sphere`). No plausible pack match for tileable
1536x448 parallax art scanned in Kenney/RTS Sci-fi (both ship fixed-size
scene compositions, not seamless repeat strips), so built procedurally,
consistent with the established fallback pattern.
"""
import os
from collections import deque
from PIL import Image, ImageDraw
import numpy as np

SRC = r"C:\workspace\Mario\SandBox"
OUT = os.path.join(os.path.dirname(__file__), "..", "reskin-source")


def find_components(im):
    """BFS connected-component scan over alpha>0 pixels; returns list of
    (bbox, avg_rgb) tuples. No scipy available in this environment."""
    arr = np.array(im)
    alpha = arr[:, :, 3]
    visited = np.zeros_like(alpha, dtype=bool)
    h, w = alpha.shape
    comps = []
    for y in range(h):
        for x in range(w):
            if alpha[y, x] > 10 and not visited[y, x]:
                q = deque([(x, y)])
                visited[y, x] = True
                minx = maxx = x
                miny = maxy = y
                pixels = []
                while q:
                    cx, cy = q.popleft()
                    pixels.append((cx, cy))
                    minx, maxx = min(minx, cx), max(maxx, cx)
                    miny, maxy = min(miny, cy), max(maxy, cy)
                    for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                        nx, ny = cx + dx, cy + dy
                        if 0 <= nx < w and 0 <= ny < h and not visited[ny, nx] and alpha[ny, nx] > 10:
                            visited[ny, nx] = True
                            q.append((nx, ny))
                rgb = arr[[p[1] for p in pixels], [p[0] for p in pixels], :3].mean(axis=0)
                comps.append(((minx, miny, maxx + 1, maxy + 1), tuple(rgb)))
    return comps


def classify(rgb, area, bbox):
    r, g, b = rgb
    w = bbox[2] - bbox[0]
    h_ = bbox[3] - bbox[1]
    if g > r + 15 and g > b + 15:
        return "mountain"
    # a fence post's ball-cap + rod is tall/narrow regardless of palette
    # (confirmed by direct component dump: 28-32 wide, 124-156 tall) --
    # shape-based, not color-based, so it still matches both the
    # tan-and-cream original and any recolor.
    if h_ > 100 and w < 40:
        return "post"
    # the woven wood-lattice fence panel is brown/orange (r > g > b by a
    # wide margin); everything else left (the small drifting cloud puffs,
    # in both their day cyan-white and night red-white palettes) falls
    # through to "cloud".
    if r > g > b and (r - b) > 60:
        return "lattice"
    return "cloud"


def draw_ridge(im, d, bbox, night):
    x0, y0, x1, y1 = bbox
    base = (54, 60, 74, 255) if not night else (34, 38, 50, 255)
    d.polygon([(x0, y1), ((x0 + x1) // 2, y0), (x1, y1)], fill=base)
    # angular tech notches down each slope
    steps = 5
    for i in range(steps):
        t = i / steps
        px = x0 + t * (x1 - x0)
        py = y1 - t * (y1 - y0) * 0.5
        d.line((px, py, px + (x1 - x0) / steps * 0.4, py), fill=(20, 22, 30, 255), width=1)
    # a couple of lit window dots
    cx = (x0 + x1) // 2
    cy = y0 + (y1 - y0) // 2
    glow = (90, 220, 255, 255) if not night else (255, 90, 90, 255)
    d.point((cx - 2, cy + 4), fill=glow)
    d.point((cx + 3, cy + 8), fill=glow)


def draw_drone(im, d, bbox, night):
    x0, y0, x1, y1 = bbox
    body = (210, 216, 224, 255)
    glow = (110, 220, 255, 255) if not night else (255, 90, 90, 255)
    w = x1 - x0
    h_ = y1 - y0
    d.ellipse((x0, y0, x1, y0 + h_ * 0.7), fill=body, outline=(140, 146, 156, 255))
    d.rectangle((x0 + w * 0.2, y0 + h_ * 0.55, x1 - w * 0.2, y1), fill=glow)


def draw_lattice(im, d, bbox, night):
    x0, y0, x1, y1 = bbox
    w, h_ = x1 - x0, y1 - y0
    panel = (30, 34, 42, 255)
    glow = (255, 150, 60, 255)
    tile = Image.new("RGBA", (w, h_), panel)
    td = ImageDraw.Draw(tile)
    step = 8
    for x in range(-h_, w + step, step):
        td.line((x, 0, x + h_, h_), fill=glow, width=1)
        td.line((x + h_, 0, x, h_), fill=glow, width=1)
    im.paste(tile, (x0, y0))


def draw_post(im, d, bbox, night):
    x0, y0, x1, y1 = bbox
    w = x1 - x0
    orb_h = min(w, 10)
    post_color = (110, 116, 128, 255) if not night else (70, 74, 86, 255)
    glow = (110, 255, 200, 255) if not night else (255, 150, 90, 255)
    d.rectangle((x0 + w * 0.35, y0 + orb_h, x1 - w * 0.35, y1), fill=post_color)
    d.ellipse((x0, y0, x0 + orb_h * 1.4, y0 + orb_h * 1.4), fill=glow)


def rebuild(name, night, drawers):
    im = Image.open(os.path.join(SRC, name)).convert("RGBA")
    out = Image.new("RGBA", im.size, (0, 0, 0, 0))
    d = ImageDraw.Draw(out)
    comps = find_components(im)
    for bbox, rgb in comps:
        area = (bbox[2] - bbox[0]) * (bbox[3] - bbox[1])
        kind = classify(rgb, area, bbox)
        drawers[kind](out, d, bbox, night)
    return out


def save(im, *parts):
    path = os.path.join(OUT, *parts)
    im.save(path)
    print("wrote", path, im.size)


drawers = {"mountain": draw_ridge, "cloud": draw_drone, "post": draw_post, "lattice": draw_lattice}

save(rebuild("Mountain.png", False, drawers), "Mountain.png")
save(rebuild("Clouds.png", False, drawers), "Clouds.png")
save(rebuild("CloudsNight.png", True, drawers), "CloudsNight.png")
save(rebuild("Fence.png", False, drawers), "Fence.png")
save(rebuild("Fence2.png", False, drawers), "Fence2.png")


# --------------------------------------------------------- sea_background
def build_sea():
    w, h = 32, 96
    im = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    top = (30, 140, 190, 255)
    bottom = (10, 40, 90, 255)
    for y in range(h):
        t = y / (h - 1)
        color = tuple(int(top[i] * (1 - t) + bottom[i] * t) for i in range(3)) + (255,)
        d.line((0, y, w, y), fill=color)
    # circuit-ripple surface line replacing the original wave crest, same
    # rough silhouette height/position (a jagged band near the top quarter)
    surface_y = 18
    points = [(0, surface_y + 6), (6, surface_y), (10, surface_y + 4), (16, surface_y - 2),
              (22, surface_y + 5), (26, surface_y - 1), (w, surface_y + 3)]
    d.line(points, fill=(140, 230, 255, 230), width=2)
    for px, py in points[1:-1]:
        d.point((px, py - 2), fill=(200, 250, 255, 255))
    return im


save(build_sea(), "Sea.png")
print("done")

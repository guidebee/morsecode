"""
Shared, tested helpers for building Mario reskin assets.

ALWAYS import from here instead of re-writing crop/scale/tint/composite logic
inline in a new build script. Every function below exists because an earlier,
inline, one-off version of it caused a real shipped bug - see each
docstring's "History" note. Copy-pasting crop math instead of importing this
module is exactly how those bugs happened a second time.

Usage:
    import sys
    sys.path.insert(0, "C:/workspace/morsecode/docs/assets/mario-sprites/ampere-staging")
    from sprite_tools import verified_crop, place_content, tint, glow_pulse, mirror, build_sheet
"""
from PIL import Image, ImageOps


def verified_crop(im, box, label):
    """Crop `box` out of `im` and assert the result isn't empty, printing the
    crop and its content bbox either way.

    ALWAYS wrap a crop in this while you're picking coordinates for a new
    asset, even if you're confident - delete the wrapping only once you've
    looked at the actual output image and confirmed it's correct.

    History: the reskin-byteplus branch's power-up build guessed crop
    coordinates from a written description ("approximate positions based on
    doc description") instead of checking them - the mushroom/flower crops
    landed on the wrong pixels entirely and were never caught before packing,
    because nothing verified the crop wasn't empty/garbage.
    """
    crop = im.crop(box)
    bbox = crop.getbbox()
    status = "OK" if bbox else "EMPTY - BAD CROP, fix the coordinates"
    print(f"  {label}: crop={box} content_bbox={bbox} [{status}]")
    assert bbox is not None, f"Empty crop for {label} at {box} - the coordinates are wrong"
    return crop


def is_tileable_texture(im, edge_tolerance=1):
    """Returns True if `im`'s content touches (near enough) all four edges of
    its own canvas - a signal it's a seamless repeating texture, not a
    discrete icon/sprite, and should NOT be cropped into arbitrary windows
    and treated as a standalone item.

    History: gate.png (a coil/pillar texture) was sliced into 32px windows
    and used as the Charge coil power-up - it read as wall texture, not an
    item, because nothing checked whether the source was actually a discrete
    sprite first. Run this check before using ANY new source image as an
    item/icon.
    """
    bbox = im.getbbox()
    if bbox is None:
        return False
    x0, y0, x1, y1 = bbox
    w, h = im.size
    return (x0 <= edge_tolerance and y0 <= edge_tolerance
            and x1 >= w - edge_tolerance and y1 >= h - edge_tolerance)


def place_content(frame, cell_w, cell_h, fill=0.85, anchor="center"):
    """Crop `frame` to its own real (tight) alpha bounding box, THEN scale
    that content to fill `fill` fraction of the target cell (uniform scale,
    limited by whichever dimension is tighter), then paste it centered
    (anchor="center") or bottom-anchored (anchor="bottom", for anything that
    should look like it's standing on the tile floor: enemies, the player).

    History: two distinct bugs this fixes at once -
    (1) Scuttler (Step R.3) was copied 1:1 from a source frame whose real
        content was only 15x14px inside a 32x32 canvas - it rendered "too
        small" because nothing scaled the tight content up to fill the cell.
    (2) Ampere's Big/Fire states (Step R.2) used a naive aspect-preserving
        `min(w_ratio, h_ratio)` scale, which caps at 1.0x whenever the
        source is already as wide as the cell - a 32x32 source going into a
        32x64 cell never actually got taller, it just got repositioned in a
        transparent canvas. This function scales by the LIMITING dimension
        instead, so it always grows content to actually fill the cell.
    """
    bbox = frame.getbbox()
    if bbox is None:
        return Image.new("RGBA", (cell_w, cell_h), (0, 0, 0, 0))
    content = frame.crop(bbox)
    cw, ch = content.size
    scale = min((fill * cell_w) / cw, (fill * cell_h) / ch)
    nw, nh = max(1, round(cw * scale)), max(1, round(ch * scale))
    scaled = content.resize((nw, nh), Image.NEAREST)
    canvas = Image.new("RGBA", (cell_w, cell_h), (0, 0, 0, 0))
    x = (cell_w - nw) // 2
    y = (cell_h - nh) // 2 if anchor == "center" else (cell_h - nh)
    canvas.paste(scaled, (x, y), scaled)
    return canvas


def mirror(frame):
    """Horizontal flip - use for a left-facing frame from a right-facing
    source (or vice versa). Check the actor's own frame-index convention
    (see MISTAKES.md / the guide doc) for which columns need which facing -
    don't assume "even columns = right" without checking the Java source."""
    return ImageOps.mirror(frame)


def tint(frame, color, strength=0.3):
    """Alpha-preserving color blend toward `color` (an (r,g,b) tuple) by
    `strength` (0..1). Only affects opaque pixels - the alpha channel is
    copied back from the original frame afterward, so transparent background
    stays transparent. Use this for per-theme recoloring (Ground/UnderGround/
    Castle/Sea)."""
    overlay = Image.new("RGBA", frame.size, color + (255,))
    blended = Image.blend(frame.convert("RGBA"), overlay, strength)
    _, _, _, a = frame.split()
    blended.putalpha(a)
    return blended


def glow_pulse(frame, strength):
    """Brighten toward white by `strength` (0..1), alpha preserved - use for
    a "charged/glowing" animation cycle across frames of the SAME base image
    (pass strength=0 for the resting frame, higher for the "pulsed" ones).

    History: the original Charge coil script used
    `Image.alpha_composite(glow_rect, coil)` to fake a glow - composited in
    that order, the glow rectangle (the *base* image) showed through
    wherever the coil was transparent, tinting the background around the
    icon instead of just the icon. This function only ever touches the
    frame's own opaque pixels, never the surrounding transparency.
    """
    if strength <= 0:
        return frame
    overlay = Image.new("RGBA", frame.size, (255, 255, 255, 255))
    blended = Image.blend(frame.convert("RGBA"), overlay, strength)
    _, _, _, a = frame.split()
    blended.putalpha(a)
    return blended


def build_sheet(cell_w, cell_h, frames_by_cell, cols, rows, out_path):
    """Composite a dict of {(col, row): frame} (or a flat list, for a single
    row) into one sprite sheet and save it. `frames_by_cell` may be a plain
    list for a 1-row sheet (equivalent to {(i, 0): frame for i, frame in
    enumerate(list)})."""
    if isinstance(frames_by_cell, (list, tuple)):
        frames_by_cell = {(i, 0): f for i, f in enumerate(frames_by_cell)}
    sheet = Image.new("RGBA", (cell_w * cols, cell_h * rows), (0, 0, 0, 0))
    for (col, row), frame in frames_by_cell.items():
        sheet.paste(frame, (col * cell_w, row * cell_h), frame)
    sheet.save(out_path)
    print("wrote", out_path, sheet.size)
    return sheet

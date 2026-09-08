package au.com.guidebee.morsetoolkit.platformer.core;

/**
 * Clamps a scroll position to a level's bounds, for a "camera window" onto
 * the world sized {@code MarioConfiguration.VIEWPORT_WIDTH/HEIGHT} scaled by
 * {@link #setZoom} (see {@code MarioGameScreen}'s "Pinch-to-zoom" section).
 * The resulting {@link #getX()}/{@link #getY()} feed directly into
 * {@code LayerManager.draw(x, y)}, which translates the camera by exactly
 * that offset for one frame (see {@code LayerManager.draw(int, int)}) - the
 * same mechanism Battle City uses via its constant {@code battleFieldX/Y}.
 *
 * <p>Clamping against {@link #getEffectiveWidth()}/{@link #getEffectiveHeight()}
 * (the zoomed span), not the nominal {@code viewportWidth/Height}, is what
 * keeps the ground flush with the screen's bottom edge at any zoom level
 * whenever the player is standing on it - the same clamp that already pinned
 * the window's bottom edge to the level's bottom edge at zoom 1 now does so
 * at the zoomed size too - and what keeps the player always inside the
 * window - {@link #centerOn} re-centers on the player using that same
 * effective size every frame, so it never leaves the (possibly zoomed-in,
 * hence smaller) visible area the way it would if the clamp still used the
 * nominal, un-zoomed size.
 *
 * <p>Step 3 has no player yet, so {@code MarioGameScreen} drives this with a
 * temporary debug auto-scroll to exercise it end-to-end; Step 4 replaces that
 * with {@code centerOn(player.getX(), player.getY())} every frame.
 */
public class CameraFollow {

    private final int viewportWidth;
    private final int viewportHeight;
    private final int levelWidthPx;
    private final int levelHeightPx;

    private float zoom = 1f;
    private float x;
    private float y;

    public CameraFollow(int viewportWidth, int viewportHeight,
                         int levelWidthPx, int levelHeightPx) {
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        this.levelWidthPx = levelWidthPx;
        this.levelHeightPx = levelHeightPx;
    }

    /** @param zoom the shared camera's current zoom - see {@code MarioGameScreen}'s "Pinch-to-zoom" section. */
    public void setZoom(float zoom) {
        this.zoom = zoom;
    }

    /** Centers the camera window on a world point, clamped to the level's bounds - see the class doc. */
    public void centerOn(float worldX, float worldY) {
        float effectiveWidth = getEffectiveWidth();
        float effectiveHeight = getEffectiveHeight();
        x = clamp(worldX - effectiveWidth / 2f, 0, Math.max(0, levelWidthPx - effectiveWidth));
        y = clamp(worldY - effectiveHeight / 2f, 0, Math.max(0, levelHeightPx - effectiveHeight));
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public int getX() {
        return (int) x;
    }

    public int getY() {
        return (int) y;
    }

    /** The camera window's actual current width, in world pixels, at {@link #setZoom}'s zoom. */
    public float getEffectiveWidth() {
        return viewportWidth * zoom;
    }

    /** The camera window's actual current height, in world pixels, at {@link #setZoom}'s zoom. */
    public float getEffectiveHeight() {
        return viewportHeight * zoom;
    }
}

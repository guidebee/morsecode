package au.com.guidebee.morsetoolkit.activity.mario.world;

/**
 * Clamps a scroll position to a level's bounds, for a fixed-size "camera
 * window" onto the world (see {@code MarioConfiguration.VIEWPORT_WIDTH/HEIGHT}).
 * The resulting {@link #getX()}/{@link #getY()} feed directly into
 * {@code LayerManager.draw(x, y)}, which translates the camera by exactly
 * that offset for one frame (see {@code LayerManager.draw(int, int)}) - the
 * same mechanism Battle City uses via its constant {@code battleFieldX/Y}.
 *
 * <p>Step 3 has no player yet, so {@code MarioGameScreen} drives this with a
 * temporary debug auto-scroll to exercise it end-to-end; Step 4 replaces that
 * with {@code centerOn(player.getX(), player.getY())} every frame.
 */
public class CameraController {

    private final int viewportWidth;
    private final int viewportHeight;
    private final int levelWidthPx;
    private final int levelHeightPx;

    private float x;
    private float y;

    public CameraController(int viewportWidth, int viewportHeight,
                             int levelWidthPx, int levelHeightPx) {
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        this.levelWidthPx = levelWidthPx;
        this.levelHeightPx = levelHeightPx;
    }

    /** Centers the camera window on a world point, clamped to the level's bounds. */
    public void centerOn(float worldX, float worldY) {
        x = clamp(worldX - viewportWidth / 2f, 0, Math.max(0, levelWidthPx - viewportWidth));
        y = clamp(worldY - viewportHeight / 2f, 0, Math.max(0, levelHeightPx - viewportHeight));
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
}

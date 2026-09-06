package au.com.guidebee.morsetoolkit.activity.mario;

/**
 * Shared constants for the Mario port: tile size and the TiledLayer cell-index
 * mapping for World 1's static terrain. See docs/MARIO_PORT_PLAN.md Step 3.1.
 *
 * <p>The TILE_* indices below must stay in lockstep with
 * {@code tools/mario-atlas-packer}'s {@code TILE_SHEET_ORDER} - both list the
 * same 6 tiles (stone/chocolate x Ground/UnderGround/Castle) in the same
 * order, 1-based (TiledLayer reserves cell value 0 for "empty").
 *
 * <p>Two tile types that look like static terrain are deliberately NOT here:
 * <ul>
 *   <li>Pipes ("pump") - the original pump.png assets are 64x32 (2 tiles
 *   wide, freely-positioned/overlapping sprites in the original engine),
 *   which doesn't fit TiledLayer's uniform grid.
 *   <li>Brick - it turned out to be breakable (Step 5), so it needed to be
 *   a real actor that can deactivate itself, not a permanent tile cell.
 * </ul>
 * Both are Sprite actors instead - see {@code actors.bricks.Pump}/{@code Brick}.
 */
public final class MarioConfiguration {

    private MarioConfiguration() {
    }

    public static final int TILE_SIZE = 32;

    public static final int TILE_STONE = 1;
    public static final int TILE_STONE_UNDERGROUND = 2;
    public static final int TILE_STONE_CASTLE = 3;
    public static final int TILE_CHOCOLATE = 4;
    public static final int TILE_CHOCOLATE_UNDERGROUND = 5;
    public static final int TILE_CHOCOLATE_CASTLE = 6;

    /**
     * The default "camera window" size, in world pixels - see
     * {@code CameraController}. 20x15 tiles, matching the original desktop
     * game's own window ({@code GameLoader.setup(..., new Dimension(640, 480),
     * ...)} in {@code MarioRun}/{@code WholeGame}) tile-for-tile, rather than
     * an arbitrary, much more zoomed-in 12x7 - on a phone screen the old
     * narrower window blew up every tile/sprite far larger relative to the
     * screen than the desktop game ever looked, leaving too little of the
     * level visible at once to play comfortably.
     *
     * <p>The on-screen joystick/buttons are deliberately exempt from this -
     * unlike world tiles/sprites, they need to stay a constant, comfortably
     * tappable size no matter how far this "camera window" is zoomed in or
     * out, so {@code MarioResourceManager}'s {@code CONTROLLER_TEXTURES}
     * scales them to compensate for whatever this constant is currently set
     * to, keeping their on-screen footprint fixed instead of shrinking or
     * growing along with the world.
     */
    public static final int VIEWPORT_WIDTH = 20 * TILE_SIZE;
    public static final int VIEWPORT_HEIGHT = 15 * TILE_SIZE;

    /**
     * The largest per-frame delta any physics code should ever act on, in
     * seconds. A screen's first frame can carry a much larger delta than any
     * frame after it - real wall-clock time keeps ticking during that
     * screen's own construction (asset lookups, spawning every brick/enemy),
     * so by the time the first render() call fires, "time since last frame"
     * can be a large fraction of a second instead of ~1/60. Every collision
     * check here is discrete (final-position-only, not swept), so an
     * uncapped delta on that first frame can move an actor clean through a
     * thin solid (e.g. gravity pulling Mario through the two-tile-thick
     * ground in one step) before it ever gets a chance to collide with it.
     * Clamping delta once, centrally, before it reaches any actor's act()
     * avoids that regardless of what causes the stall.
     */
    public static final float MAX_DELTA_SECONDS = 1f / 30f;
}

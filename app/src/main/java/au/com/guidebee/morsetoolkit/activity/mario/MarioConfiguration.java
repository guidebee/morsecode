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

    /** The default "camera window" size, in world pixels - see {@code CameraController}. */
    public static final int VIEWPORT_WIDTH = 12 * TILE_SIZE;
    public static final int VIEWPORT_HEIGHT = 7 * TILE_SIZE;
}

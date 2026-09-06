package au.com.guidebee.morsetoolkit.activity.mario;

/**
 * Shared constants for the Mario port: tile size and the TiledLayer cell-index
 * mapping for World 1's static terrain. See docs/MARIO_PORT_PLAN.md Step 3.
 *
 * <p>The TILE_* indices below must stay in lockstep with
 * {@code tools/mario-atlas-packer}'s {@code TILE_SHEET_ORDER} - both list the
 * same 9 tiles (brick/stone/chocolate x Ground/UnderGround/Castle) in the same
 * order, 1-based (TiledLayer reserves cell value 0 for "empty").
 *
 * <p>Pipes ("pump") are deliberately not tile indices here - the original
 * pump.png assets are 64x32 (2 tiles wide, freely-positioned/overlapping
 * sprites in the original engine), which doesn't fit TiledLayer's uniform
 * grid. They become a Sprite actor in Step 5, like Bank/QuestionMark.
 */
public final class MarioConfiguration {

    private MarioConfiguration() {
    }

    public static final int TILE_SIZE = 32;

    public static final int TILE_BRICK = 1;
    public static final int TILE_BRICK_UNDERGROUND = 2;
    public static final int TILE_BRICK_CASTLE = 3;
    public static final int TILE_STONE = 4;
    public static final int TILE_STONE_UNDERGROUND = 5;
    public static final int TILE_STONE_CASTLE = 6;
    public static final int TILE_CHOCOLATE = 7;
    public static final int TILE_CHOCOLATE_UNDERGROUND = 8;
    public static final int TILE_CHOCOLATE_CASTLE = 9;

    /** The default "camera window" size, in world pixels - see {@code CameraController}. */
    public static final int VIEWPORT_WIDTH = 12 * TILE_SIZE;
    public static final int VIEWPORT_HEIGHT = 7 * TILE_SIZE;
}

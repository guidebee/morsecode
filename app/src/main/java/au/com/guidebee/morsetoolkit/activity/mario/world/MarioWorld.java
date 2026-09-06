package au.com.guidebee.morsetoolkit.activity.mario.world;

import com.guidebee.game.microedition.TiledLayer;

import au.com.guidebee.morsetoolkit.activity.mario.MarioConfiguration;
import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;

/**
 * The static-terrain grid for one level - a {@code TiledLayer} sized to that
 * level's tile extent, drawn from the "tiles" composite region in mario.atlas
 * (see {@code tools/mario-atlas-packer}). See docs/MARIO_PORT_PLAN.md Step 3.1.
 *
 * <p>Populated by {@code LevelLoader}, not here - this class only owns the
 * grid's shape and pixel-size helpers.
 */
public class MarioWorld extends TiledLayer {

    public MarioWorld(int cols, int rows) {
        super(cols, rows, MarioResourceManager.region("tiles"),
                MarioConfiguration.TILE_SIZE, MarioConfiguration.TILE_SIZE);
    }

    public int getWidthPx() {
        return getColumns() * MarioConfiguration.TILE_SIZE;
    }

    public int getHeightPx() {
        return getRows() * MarioConfiguration.TILE_SIZE;
    }
}

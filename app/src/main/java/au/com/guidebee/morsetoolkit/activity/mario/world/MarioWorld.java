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

    /**
     * Whether the given pixel rectangle overlaps any non-empty (solid) cell.
     * Every populated cell in this Step-3/4 world is solid terrain (brick/
     * stone/chocolate) - there's no "decorative but walkable" tile yet - so
     * "populated" and "impassable" are the same test for now. Same technique
     * as Battle City's {@code BattleField.containsImpassableArea}.
     */
    public boolean containsImpassableArea(int x, int y, int width, int height) {
        int tileSize = MarioConfiguration.TILE_SIZE;

        int columnMin = Math.max(0, x / tileSize);
        int columnMax = Math.min(getColumns() - 1, (x + width - 1) / tileSize);
        int rowMin = Math.max(0, y / tileSize);
        int rowMax = Math.min(getRows() - 1, (y + height - 1) / tileSize);

        for (int row = rowMin; row <= rowMax; row++) {
            for (int column = columnMin; column <= columnMax; column++) {
                if (getCell(column, row) != 0) {
                    return true;
                }
            }
        }
        return false;
    }
}

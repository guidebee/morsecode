package au.com.guidebee.morsetoolkit.activity.mario.world;

import com.guidebee.game.microedition.TiledLayer;

import java.util.ArrayList;
import java.util.List;

import au.com.guidebee.morsetoolkit.activity.mario.MarioConfiguration;
import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.actors.bricks.InteractiveBrick;
import au.com.guidebee.morsetoolkit.activity.mario.actors.items.Collectible;

/**
 * The static-terrain grid for one level - a {@code TiledLayer} sized to that
 * level's tile extent, drawn from the "tiles" composite region in mario.atlas
 * (see {@code tools/mario-atlas-packer}). See docs/MARIO_PORT_PLAN.md Step 3.1.
 *
 * <p>Also tracks the level's interactive bricks and collectible items (Step
 * 5) so both count as "solid"/"pickupable" from one place - see
 * {@link #containsImpassableArea} and {@link #getCollectibles()}. Populated
 * by {@code LevelLoader} and by actors spawning their own follow-ups (an
 * exhausted Bank spawning an Iron, a reveal effect spawning a Mushroom) via
 * {@link MarioContext}.
 */
public class MarioWorld extends TiledLayer {

    private final List<InteractiveBrick> bricks = new ArrayList<>();
    private final List<Collectible> collectibles = new ArrayList<>();

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

    public void addBrick(InteractiveBrick brick) {
        bricks.add(brick);
    }

    public List<InteractiveBrick> getBricks() {
        return bricks;
    }

    public void addCollectible(Collectible collectible) {
        collectibles.add(collectible);
    }

    public List<Collectible> getCollectibles() {
        return collectibles;
    }

    /**
     * Whether the given pixel rectangle overlaps any non-empty (solid) cell
     * OR any active interactive brick (bricks live outside the tile grid
     * since Step 5 - see {@code InteractiveBrick}'s class doc for why). Same
     * technique as Battle City's {@code BattleField.containsImpassableArea}.
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

        for (InteractiveBrick brick : bricks) {
            if (brick.isActive() && brick.overlaps(x, y, width, height)) {
                return true;
            }
        }
        return false;
    }

    /** The active brick overlapping this rectangle, or null - used to route a hit-from-below. */
    public InteractiveBrick findActiveBrickAt(int x, int y, int width, int height) {
        for (InteractiveBrick brick : bricks) {
            if (brick.isActive() && brick.overlaps(x, y, width, height)) {
                return brick;
            }
        }
        return null;
    }
}

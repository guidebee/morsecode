package au.com.guidebee.morsetoolkit.platformer.core;

import com.guidebee.game.graphics.TextureRegion;
import com.guidebee.game.microedition.TiledLayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generic tile-grid world: a {@code TiledLayer} sized to a level's tile
 * extent, plus a type-keyed registry of "everything else" a game's actors
 * need to look up as a group (enemies, collectibles, ...) - see
 * PLATFORMER_ENGINE_ARCHITECTURE.md §3.1. A game's own world subclass (e.g.
 * {@code MarioWorld}) wraps {@link #register}/{@link #listFor} calls in its
 * own named accessors purely for call-site readability - this class itself
 * only ever queries the registry generically, via {@link SolidTile}, so it
 * never needs to know what a specific game's own actor types are called.
 */
public class TileWorld extends TiledLayer implements TileCollisionSource {

    private final Map<Class<?>, List<Object>> actorLists = new HashMap<>();
    private final int tileSize;

    public TileWorld(int cols, int rows, TextureRegion tilesRegion, TileMetrics metrics) {
        super(cols, rows, tilesRegion, metrics.tileSize, metrics.tileSize);
        this.tileSize = metrics.tileSize;
    }

    @Override
    public int tileSize() {
        return tileSize;
    }

    public int getWidthPx() {
        return getColumns() * tileSize;
    }

    public int getHeightPx() {
        return getRows() * tileSize;
    }

    public <T> void register(Class<T> type, T actor) {
        listFor(type).add(actor);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> listFor(Class<T> type) {
        return (List<T>) (List<?>) actorLists.computeIfAbsent(type, k -> new ArrayList<>());
    }

    /**
     * A hair narrower than a tile, used to pull a rectangle's far/bottom
     * edge back inside the tile it's exactly flush against. Without this,
     * a rectangle sitting exactly on a tile boundary (e.g. an actor resting
     * with feet at y=384.0, tile size 32, so the tile below starts at
     * y=416.0) is graded as "just barely into the next tile" once gravity
     * nudges it down by even a sub-pixel amount, then rounds back onto the
     * boundary once the correction snaps it back - a visible ground/airborne
     * flicker every frame while standing still.
     */
    private static final float EPSILON = 0.001f;

    /**
     * Whether the given pixel rectangle overlaps any non-empty (solid) cell
     * OR any active {@link SolidTile} (a brick-like actor living outside the
     * tile grid - see that interface's own doc for why).
     */
    @Override
    public boolean containsImpassableArea(float x, float y, int width, int height) {
        return containsImpassableArea(x, y, width, height, Float.NEGATIVE_INFINITY);
    }

    /**
     * @param duckAboveY while ducking, a player actor may pass its own head
     *                   line here so a {@link SolidTile} sitting entirely
     *                   above it (i.e. over a crouching character's head)
     *                   doesn't block it. Every other caller passes
     *                   {@link Float#NEGATIVE_INFINITY} via the no-arg
     *                   overload, so this never applies to them.
     */
    @Override
    public boolean containsImpassableArea(float x, float y, int width, int height, float duckAboveY) {
        int columnMin = Math.max(0, (int) Math.floor(x / tileSize));
        int columnMax = Math.min(getColumns() - 1, (int) Math.floor((x + width - EPSILON) / tileSize));
        int rowMin = Math.max(0, (int) Math.floor(y / tileSize));
        int rowMax = Math.min(getRows() - 1, (int) Math.floor((y + height - EPSILON) / tileSize));

        for (int row = rowMin; row <= rowMax; row++) {
            for (int column = columnMin; column <= columnMax; column++) {
                if (getCell(column, row) != 0) {
                    return true;
                }
            }
        }

        for (SolidTile solid : listFor(SolidTile.class)) {
            if (solid.isActive() && solid.overlaps(x, y, width, height)
                    && solid.getY() + solid.getHeight() >= duckAboveY) {
                return true;
            }
        }
        return false;
    }

    /**
     * The active {@link SolidTile} overlapping this rectangle the most, or
     * null - used to route a hit-from-below. Picking the largest overlap
     * (rather than just the first match in registration order) matters
     * whenever the rectangle isn't tile-aligned and straddles two adjacent
     * solid tiles (e.g. a player jumping up under a "?" block sandwiched
     * between two plain bricks): the tile the player is mostly under should
     * win, not whichever tile happened to load first.
     */
    public SolidTile findActiveBrickAt(float x, float y, int width, int height) {
        SolidTile best = null;
        float bestOverlapArea = 0;
        for (SolidTile solid : listFor(SolidTile.class)) {
            if (!solid.isActive() || !solid.overlaps(x, y, width, height)) {
                continue;
            }
            float overlapWidth = Math.min(x + width, solid.getX() + solid.getWidth()) - Math.max(x, solid.getX());
            float overlapHeight = Math.min(y + height, solid.getY() + solid.getHeight()) - Math.max(y, solid.getY());
            float overlapArea = overlapWidth * overlapHeight;
            if (best == null || overlapArea > bestOverlapArea) {
                best = solid;
                bestOverlapArea = overlapArea;
            }
        }
        return best;
    }
}

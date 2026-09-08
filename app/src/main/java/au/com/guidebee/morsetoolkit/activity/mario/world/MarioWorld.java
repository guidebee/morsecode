package au.com.guidebee.morsetoolkit.activity.mario.world;

import com.guidebee.game.graphics.TextureRegion;
import com.guidebee.game.microedition.TiledLayer;

import java.util.ArrayList;
import java.util.List;

import au.com.guidebee.morsetoolkit.activity.mario.actors.bricks.Axe;
import au.com.guidebee.morsetoolkit.activity.mario.actors.bricks.InteractiveBrick;
import au.com.guidebee.morsetoolkit.activity.mario.actors.enemies.Enemy;
import au.com.guidebee.morsetoolkit.activity.mario.actors.hazards.Hazard;
import au.com.guidebee.morsetoolkit.activity.mario.actors.items.Collectible;
import au.com.guidebee.morsetoolkit.activity.mario.actors.lifts.LiftSurface;
import au.com.guidebee.morsetoolkit.activity.mario.actors.projectiles.FireBall;
import au.com.guidebee.morsetoolkit.platformer.core.TileCollisionSource;
import au.com.guidebee.morsetoolkit.platformer.core.TileMetrics;

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
public class MarioWorld extends TiledLayer implements TileCollisionSource {

    private final List<InteractiveBrick> bricks = new ArrayList<>();
    private final List<Collectible> collectibles = new ArrayList<>();
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<FireBall> fireBalls = new ArrayList<>();
    private final List<LiftSurface> lifts = new ArrayList<>();
    private final List<Hazard> hazards = new ArrayList<>();
    private final List<Axe> axes = new ArrayList<>();

    private final int tileSize;

    /**
     * @param tilesRegion the composite "tiles"-shaped region to draw this
     *                     level's static terrain from - normally whichever
     *                     theme atlas's own "tiles" ({@code
     *                     MarioResourceManager.region("tiles")}), but
     *                     {@code LevelLoader.createWorld} picks a different,
     *                     COMMON-theme one instead for CloudsNight/"Clowd"
     *                     levels, whose *look* is independent of the level's
     *                     real {@code attribute} - see {@code
     *                     tools.mario-atlas-packer}'s own TERRAIN_TILES doc.
     */
    public MarioWorld(int cols, int rows, TextureRegion tilesRegion, TileMetrics metrics) {
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

    public void addEnemy(Enemy enemy) {
        enemies.add(enemy);
    }

    /** Not treated as solid terrain (see {@link #containsImpassableArea}) - enemies only react via {@code EnemyCollisionResolver}. */
    public List<Enemy> getEnemies() {
        return enemies;
    }

    public void addFireBall(FireBall fireBall) {
        fireBalls.add(fireBall);
    }

    public List<FireBall> getFireBalls() {
        return fireBalls;
    }

    public void addLift(LiftSurface lift) {
        lifts.add(lift);
    }

    /** Not treated as solid terrain (see {@link #containsImpassableArea}) - a lift only ever catches a rider via {@code LiftCollisionResolver}. */
    public List<LiftSurface> getLifts() {
        return lifts;
    }

    public void addHazard(Hazard hazard) {
        hazards.add(hazard);
    }

    /** Contact-damage obstacles that aren't enemies - see {@code HazardCollisionResolver}. */
    public List<Hazard> getHazards() {
        return hazards;
    }

    public void addAxe(Axe axe) {
        axes.add(axe);
    }

    /** Invisible walls Mario can't walk past - see {@code AxeResolver}. */
    public List<Axe> getAxes() {
        return axes;
    }

    /**
     * A hair narrower than a tile, used to pull a rectangle's far/bottom
     * edge back inside the tile it's exactly flush against. Without this,
     * a rectangle sitting exactly on a tile boundary (e.g. a player resting
     * with feet at y=384.0, tile size 32, so the tile below starts at
     * y=416.0) is graded as "just barely into the next tile" once gravity
     * nudges it down by even a sub-pixel amount, then rounds back onto the
     * boundary once the correction snaps it back - a visible ground/airborne
     * flicker every frame while standing still. See the git history for the
     * original bug report and diagnostic logs that pinned this down.
     */
    private static final float EPSILON = 0.001f;

    /**
     * Whether the given pixel rectangle overlaps any non-empty (solid) cell
     * OR any active interactive brick (bricks live outside the tile grid
     * since Step 5 - see {@code InteractiveBrick}'s class doc for why). Same
     * technique as Battle City's {@code BattleField.containsImpassableArea},
     * adapted to floating-point position (Battle City's tanks only ever
     * move in whole-tile steps, so truncating to {@code int} up front never
     * lost any meaningful precision there the way it did here).
     */
    public boolean containsImpassableArea(float x, float y, int width, int height) {
        return containsImpassableArea(x, y, width, height, Float.NEGATIVE_INFINITY);
    }

    /**
     * @param duckAboveY while ducking, {@code Player} passes its own
     *                   {@code getY() + 32} here so an {@link InteractiveBrick}
     *                   sitting entirely above that line (i.e. over a crouching
     *                   Big/Fire Mario's head) doesn't block him - ported from
     *                   the original's {@code Player_Brick} collision pair,
     *                   which checked exactly this (see {@code Player#ducking}'s
     *                   doc). Every other caller (enemies/items/the static
     *                   tile grid check) passes {@link Float#NEGATIVE_INFINITY}
     *                   via the no-arg overload, so this never applies to them -
     *                   only {@code Player}'s own movement ducks.
     */
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

        for (InteractiveBrick brick : bricks) {
            if (brick.isActive() && brick.overlaps(x, y, width, height)
                    && brick.getY() + brick.getHeight() >= duckAboveY) {
                return true;
            }
        }
        return false;
    }

    /** The active brick overlapping this rectangle, or null - used to route a hit-from-below. */
    public InteractiveBrick findActiveBrickAt(float x, float y, int width, int height) {
        for (InteractiveBrick brick : bricks) {
            if (brick.isActive() && brick.overlaps(x, y, width, height)) {
                return brick;
            }
        }
        return null;
    }
}

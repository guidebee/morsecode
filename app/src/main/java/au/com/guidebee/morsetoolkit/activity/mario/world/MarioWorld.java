package au.com.guidebee.morsetoolkit.activity.mario.world;

import com.guidebee.game.microedition.TiledLayer;

import java.util.ArrayList;
import java.util.List;

import au.com.guidebee.morsetoolkit.activity.mario.MarioConfiguration;
import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.actors.bricks.InteractiveBrick;
import au.com.guidebee.morsetoolkit.activity.mario.actors.enemies.Enemy;
import au.com.guidebee.morsetoolkit.activity.mario.actors.items.Collectible;
import au.com.guidebee.morsetoolkit.activity.mario.actors.lifts.Lift;
import au.com.guidebee.morsetoolkit.activity.mario.actors.projectiles.FireBall;

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
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<FireBall> fireBalls = new ArrayList<>();
    private final List<Lift> lifts = new ArrayList<>();

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

    public void addLift(Lift lift) {
        lifts.add(lift);
    }

    /** Not treated as solid terrain (see {@link #containsImpassableArea}) - a lift only ever catches a rider via {@code LiftCollisionResolver}. */
    public List<Lift> getLifts() {
        return lifts;
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
        int tileSize = MarioConfiguration.TILE_SIZE;

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
            if (brick.isActive() && brick.overlaps(x, y, width, height)) {
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

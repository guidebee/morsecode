package au.com.guidebee.morsetoolkit.activity.mario.world;

import com.guidebee.game.graphics.TextureRegion;

import java.util.List;

import au.com.guidebee.morsetoolkit.activity.mario.actors.bricks.Axe;
import au.com.guidebee.morsetoolkit.activity.mario.actors.bricks.InteractiveBrick;
import au.com.guidebee.morsetoolkit.activity.mario.actors.enemies.Enemy;
import au.com.guidebee.morsetoolkit.activity.mario.actors.hazards.Hazard;
import au.com.guidebee.morsetoolkit.activity.mario.actors.items.Collectible;
import au.com.guidebee.morsetoolkit.activity.mario.actors.lifts.LiftSurface;
import au.com.guidebee.morsetoolkit.activity.mario.actors.projectiles.FireBall;
import au.com.guidebee.morsetoolkit.activity.mario.actors.scenery.FlagPole;
import au.com.guidebee.morsetoolkit.platformer.core.SolidTile;
import au.com.guidebee.morsetoolkit.platformer.core.TileMetrics;
import au.com.guidebee.morsetoolkit.platformer.core.TileWorld;

/**
 * The static-terrain grid for one level - a thin {@link TileWorld} subclass
 * sized to that level's tile extent, drawn from the "tiles" composite region
 * in mario.atlas (see {@code tools/mario-atlas-packer}). See
 * docs/MARIO_PORT_PLAN.md Step 3.1.
 *
 * <p>Every {@code add*`/`get*} method below is a one-line wrapper around
 * {@link TileWorld#register}/{@link TileWorld#listFor} - kept as named
 * methods purely for call-site readability across the rest of
 * {@code activity/mario}, which never needed to change. Bricks are
 * additionally registered under {@link SolidTile} (see that interface's own
 * doc) so {@link TileWorld#containsImpassableArea} folds them into the same
 * "is this pixel rectangle blocked" query as the tile grid, without
 * `TileWorld` itself needing to know what a "brick" is. Populated by
 * {@code LevelLoader} and by actors spawning their own follow-ups (an
 * exhausted Bank spawning an Iron, a reveal effect spawning a Mushroom) via
 * {@link MarioContext}.
 */
public class MarioWorld extends TileWorld {

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
        super(cols, rows, tilesRegion, metrics);
    }

    public void addBrick(InteractiveBrick brick) {
        register(InteractiveBrick.class, brick);
        register(SolidTile.class, brick);
    }

    public List<InteractiveBrick> getBricks() {
        return listFor(InteractiveBrick.class);
    }

    public void addCollectible(Collectible collectible) {
        register(Collectible.class, collectible);
    }

    public List<Collectible> getCollectibles() {
        return listFor(Collectible.class);
    }

    public void addEnemy(Enemy enemy) {
        register(Enemy.class, enemy);
    }

    /** Not treated as solid terrain (see {@code TileWorld#containsImpassableArea}) - enemies only react via {@code EnemyCollisionResolver}. */
    public List<Enemy> getEnemies() {
        return listFor(Enemy.class);
    }

    public void addFireBall(FireBall fireBall) {
        register(FireBall.class, fireBall);
    }

    public List<FireBall> getFireBalls() {
        return listFor(FireBall.class);
    }

    public void addLift(LiftSurface lift) {
        register(LiftSurface.class, lift);
    }

    /** Not treated as solid terrain (see {@code TileWorld#containsImpassableArea}) - a lift only ever catches a rider via {@code LiftCollisionResolver}. */
    public List<LiftSurface> getLifts() {
        return listFor(LiftSurface.class);
    }

    public void addHazard(Hazard hazard) {
        register(Hazard.class, hazard);
    }

    /** Contact-damage obstacles that aren't enemies - see {@code HazardCollisionResolver}. */
    public List<Hazard> getHazards() {
        return listFor(Hazard.class);
    }

    public void addAxe(Axe axe) {
        register(Axe.class, axe);
    }

    /** Invisible walls Mario can't walk past - see {@code AxeResolver}. */
    public List<Axe> getAxes() {
        return listFor(Axe.class);
    }

    /** The active brick overlapping this rectangle, or null - used to route a hit-from-below. */
    @Override
    public InteractiveBrick findActiveBrickAt(float x, float y, int width, int height) {
        return (InteractiveBrick) super.findActiveBrickAt(x, y, width, height);
    }

    private FlagPole flagPole;

    /**
     * Set once by whichever "Flag" tile handler spawns this level's
     * {@link FlagPole} (at most one per level) - a plain field rather than
     * the generic registry since cardinality here is "zero or one," not a
     * list. {@code MarioGameScreen} reads it back via {@link #getFlagPole}
     * once {@code MarioTileRegistry}'s spawn pass finishes, replacing the
     * old {@code LevelLoader.spawnScenery}'s own direct return value now
     * that spawning is table-driven rather than one dedicated method per
     * category.
     */
    public void setFlagPole(FlagPole flagPole) {
        this.flagPole = flagPole;
    }

    public FlagPole getFlagPole() {
        return flagPole;
    }
}

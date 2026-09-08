package au.com.guidebee.morsetoolkit.activity.mario.level;

import com.guidebee.game.graphics.TextureRegion;

import au.com.guidebee.morsetoolkit.activity.mario.MarioConfiguration;
import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.actors.bricks.InteractiveBrick;
import au.com.guidebee.morsetoolkit.activity.mario.actors.bricks.RocketLauncher;
import au.com.guidebee.morsetoolkit.activity.mario.actors.bricks.RocketLauncherBody;
import au.com.guidebee.morsetoolkit.activity.mario.actors.bricks.Tree;
import au.com.guidebee.morsetoolkit.activity.mario.actors.enemies.Enemy;
import au.com.guidebee.morsetoolkit.activity.mario.actors.enemies.OrbitingFireball;
import au.com.guidebee.morsetoolkit.activity.mario.actors.lifts.BalanceLiftPlatform;
import au.com.guidebee.morsetoolkit.activity.mario.actors.lifts.Lift;
import au.com.guidebee.morsetoolkit.activity.mario.actors.scenery.Scenery;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioContext;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioWorld;
import au.com.guidebee.morsetoolkit.platformer.core.TileMetrics;
import au.com.guidebee.morsetoolkit.platformer.level.TileTypeRegistry;

/**
 * Turns a {@link LevelDefinition}'s tile list into a level's world content.
 * Two phases, called at different points in {@code MarioGameScreen}'s setup
 * (see {@link #createWorld} and {@link #spawnBricks} for why they're split):
 *
 * <ul>
 *   <li>Static terrain (stone/chocolate) becomes {@code TiledLayer} cells -
 *   see docs/MARIO_PORT_PLAN.md Step 3.1 and {@code MarioConfiguration}.
 *   <li>Interactive bricks (Brick/Bank/QuestionMark/BrickWithStar/
 *   InvisibleBrck/Iron/Pump) become Sprite actors - see
 *   docs/MARIO_PORT_PLAN.md Step 5.1 and {@code InteractiveBrick}.
 *   <li>Ground-walking enemies (EnemyMashroom/EnemyTurtle) become Sprite
 *   actors too - see docs/MARIO_PORT_PLAN.md Step 6.1 and {@code Enemy}.
 *   <li>Moving platforms ({@code Lift_*}/{@code LiftUP}/{@code LiftDown})
 *   and the level-end flagpole/castles become Sprite actors - see
 *   docs/MARIO_PORT_PLAN.md Step 7.1, {@link #spawnLifts} and
 *   {@link #spawnScenery}. Checkpoints (level-end/pipe-entry triggers)
 *   aren't tiles at all - they're read straight off {@code LevelDefinition}
 *   by {@code CheckpointResolver}.
 * </ul>
 *
 * Every other tile type (decorative background art, flying/patrol enemies,
 * Level 14's boss-castle hazards...) is still left untouched here - later
 * steps.
 */
public final class LevelLoader {

    private LevelLoader() {
    }

    /** Builds a {@link MarioWorld} sized to the level's full tile extent and fills its static terrain. */
    public static MarioWorld createWorld(LevelDefinition level) {
        int cols = 1;
        int rows = 1;
        for (LevelDefinition.Tile tile : level.tiles) {
            cols = Math.max(cols, tile.x + tile.lengthX);
            rows = Math.max(rows, tile.y + tile.lengthY);
        }
        MarioWorld world = new MarioWorld(cols, rows, staticTilesRegion(level),
                new TileMetrics(MarioConfiguration.TILE_SIZE));
        populateStaticGeometry(world, level);
        return world;
    }

    /**
     * CloudsNight overrides a level's static-terrain *look* independently of
     * its real {@code attribute} (World 6's Level_63 is still attribute
     * "Ground", confirmed by reading its own source); "Clowd" attribute
     * levels need their own look too, but never a whole theme atlas (see
     * {@code MarioResourceManager#loadTheme}'s own "Clowd" doc) - both
     * composites live in the COMMON atlas instead, see
     * {@code tools.mario-atlas-packer}'s own TERRAIN_TILES doc.
     */
    private static TextureRegion staticTilesRegion(LevelDefinition level) {
        if ("CloudsNight".equals(level.backgroundImage)) {
            return MarioResourceManager.region("tiles_cloudsnight");
        }
        if ("Clowd".equals(level.attribute)) {
            return MarioResourceManager.region("tiles_clowd");
        }
        if ("Sea".equals(level.attribute)) {
            // Ported from Mario.java's own case 6 ("stone"): every Sea level
            // except World 8's Level_844 uses the generic "stone_Sea" look;
            // that one level alone special-cases `LevelNumber==844` to
            // "stone_Castle_Sea" instead (its approach to the underwater
            // castle, confirmed by reading the source) - both composites
            // share the SEA theme atlas, see PackMarioAtlas's own doc.
            return level.levelNumber == 844
                    ? MarioResourceManager.region("tiles_castle_sea")
                    : MarioResourceManager.region("tiles_sea");
        }
        return MarioResourceManager.region("tiles");
    }

    private static void populateStaticGeometry(MarioWorld world, LevelDefinition level) {
        for (LevelDefinition.Tile tile : level.tiles) {
            int index = staticTileIndex(tile.type);
            if (index == 0) {
                continue;
            }
            for (int dx = 0; dx < tile.lengthX; dx++) {
                for (int dy = 0; dy < tile.lengthY; dy++) {
                    world.setCell(tile.x + dx, tile.y + dy, index);
                }
            }
        }
    }

    /**
     * @return a {@code MarioConfiguration.TILE_*} index, or 0 if this type
     * isn't static terrain. No longer attribute-dependent - since
     * docs/MARIO_PORT_PLAN_PHASE2.md Step P2.1.1's per-theme atlas split,
     * index 1 always means "stone" and index 2 always means "chocolate";
     * which actual texture that draws comes from whichever theme atlas
     * {@code MarioResourceManager} has loaded for this level, not from a
     * different index per attribute (see {@code MarioConfiguration}'s doc).
     */
    private static int staticTileIndex(String type) {
        switch (type) {
            case "stone":
                return MarioConfiguration.TILE_STONE;
            case "chocolate":
                return MarioConfiguration.TILE_CHOCOLATE;
            default:
                return 0;
        }
    }

    private static final TileTypeRegistry BRICK_REGISTRY = MarioTileRegistry.buildBricks();

    /**
     * Spawns every interactive brick. Requires {@link MarioContext} to
     * already be initialized (bricks register themselves into
     * {@code MarioContext.world()} and append themselves via
     * {@code MarioContext.spawn(...)}) - call this after
     * {@code MarioContext.init(layerManager, world)}, not before.
     */
    public static void spawnBricks(LevelDefinition level) {
        BRICK_REGISTRY.spawnAll(level, MarioContext.world().tileSize());
    }

    /** How many {@code EnemyFireBall}-equivalents ring a "FireBar"/"BigFireBar" pivot, and their radius step - see {@code Mario.java}'s own case 28/29. */
    static final int FIRE_BAR_COUNT = 6;
    static final int BIG_FIRE_BAR_COUNT = 12;
    static final int FIRE_BAR_RADIUS_STEP = 16;

    /**
     * Spawns every ground-walking enemy, plus every other {@code Enemy}
     * subclass that reads/writes {@code MarioWorld}'s enemy list (patrol
     * turtles, fire-bar rings, the boss) - see docs/MARIO_PORT_PLAN.md
     * Step 6.1 and docs/MARIO_PORT_PLAN_PHASE2.md Step P2.0.
     */
    private static final TileTypeRegistry ENEMY_REGISTRY = MarioTileRegistry.buildEnemies();

    public static void spawnEnemies(LevelDefinition level) {
        ENEMY_REGISTRY.spawnAll(level, MarioContext.world().tileSize());
    }

    /** Ported from {@code Mario.java}'s case 27 ("Helmet") - the palette isn't level data, it's picked from the level's own attribute at spawn time. */
    static String helmetColor(String attribute) {
        if ("UnderGround".equals(attribute)) {
            return "dark";
        }
        if ("Castle".equals(attribute)) {
            return "white";
        }
        return "normal";
    }

    /** Ported from {@code Mario.java}'s case 28/29 - {@code count} {@code OrbitingFireball}s around one pivot, spaced {@code FIRE_BAR_RADIUS_STEP}px apart. */
    static void spawnFireBar(LevelDefinition.Tile tile, int count) {
        int tileSize = MarioContext.world().tileSize();
        float centerX = tile.x * tileSize + 8;
        float centerY = tile.y * tileSize + 8;
        boolean clockwise = "CW".equals(tile.extraInfo);
        for (int j = 0; j < count; j++) {
            addEnemy(new OrbitingFireball(centerX, centerY, j * FIRE_BAR_RADIUS_STEP, clockwise));
        }
    }

    private static final TileTypeRegistry HAZARD_REGISTRY = MarioTileRegistry.buildHazards();

    /**
     * Spawns every hazard that isn't an {@code Enemy} (an axe's invisible
     * wall, a static drifting {@code BossFire}) - see docs/MARIO_PORT_PLAN_PHASE2.md
     * Step P2.0. Same {@link MarioContext} requirement as {@link #spawnBricks}.
     */
    public static void spawnHazards(LevelDefinition level) {
        HAZARD_REGISTRY.spawnAll(level, MarioContext.world().tileSize());
    }

    private static final TileTypeRegistry ITEM_REGISTRY = MarioTileRegistry.buildItems();

    /**
     * Spawns every placed (as opposed to dispensed-from-a-hit-brick)
     * collectible - just "Coin" for now, the only such type any World-1
     * level places directly. Same {@link MarioContext} requirement as
     * {@link #spawnBricks}.
     */
    public static void spawnItems(LevelDefinition level) {
        ITEM_REGISTRY.spawnAll(level, MarioContext.world().tileSize());
    }

    private static final TileTypeRegistry LIFT_REGISTRY = MarioTileRegistry.buildLifts();

    /**
     * Spawns every moving platform. Same {@link MarioContext} requirement as
     * {@link #spawnBricks}. A tile's {@code patrolLength} field carries the
     * original's "Points" constructor argument - not a patrol distance for
     * this tile type, but how many source tiles wide to build the platform
     * (see {@code Lift}'s class doc) - see docs/MARIO_PORT_PLAN.md Step 7.1.
     */
    public static void spawnLifts(LevelDefinition level) {
        LIFT_REGISTRY.spawnAll(level, MarioContext.world().tileSize());
    }

    static Lift.Motion liftMotion(String type) {
        switch (type) {
            case "Lift_UpDown":
                return Lift.Motion.UP_DOWN;
            case "Lift_LeftRight":
                return Lift.Motion.LEFT_RIGHT;
            case "Lift_LeftRightInvert":
                return Lift.Motion.LEFT_RIGHT_INVERT;
            case "LiftUP":
                return Lift.Motion.UP;
            case "LiftDown":
                return Lift.Motion.DOWN;
            default:
                return null;
        }
    }

    /**
     * Ported from {@code Lifts/BalenceLiftParent.java}'s own constructor -
     * the child spawns {@code bridgeLength} tiles right of and 2 tiles below
     * the parent (see {@code BalanceLiftPlatform}'s class doc for why the
     * pair's shared physics live on one linked object instead of two).
     */
    static void spawnBalanceLift(LevelDefinition.Tile tile) {
        int tileSize = MarioContext.world().tileSize();
        BalanceLiftPlatform parent = new BalanceLiftPlatform(tile.x * tileSize, tile.y * tileSize, tileSize);
        BalanceLiftPlatform child = new BalanceLiftPlatform(
                (tile.x + tile.bridgeLength) * tileSize, tile.y * tileSize + 2 * tileSize, tileSize);
        BalanceLiftPlatform.link(parent, child);
        addBalanceLiftPlatform(parent);
        addBalanceLiftPlatform(child);
    }

    private static final TileTypeRegistry SCENERY_REGISTRY = MarioTileRegistry.buildScenery();

    /**
     * Spawns the level-end flagpole, castles, and lava - all purely
     * decorative and uncollided (see {@code Scenery}'s class doc) except the
     * flagpole's own cloth. A tree's own solid canopy + decorative trunk are
     * handled separately by {@link #spawnTree} (called from {@link
     * #spawnBricks} - the canopy row is a real, solid {@code
     * InteractiveBrick}, not decoration). A level's own scrolling backdrop
     * (mountain/clouds/...) is a whole separate, non-tile field ({@code
     * backgroundImage}) - see {@code MarioGameScreen}'s own {@code
     * BackgroundBand} wiring, not this method.
     *
     * <p>Unlike the other five {@code spawn*} methods, this one used to
     * return the level's own {@link
     * au.com.guidebee.morsetoolkit.activity.mario.actors.scenery.FlagPole}
     * (or null) directly, since it was the one category with anything to
     * report back to {@code MarioGameScreen}. Now that spawning is
     * table-driven ({@code MarioTileRegistry}'s own "Flag" handler), the
     * flagpole is set on the world instead - read it back via {@code
     * MarioContext.world().getFlagPole()} after this call, not a return
     * value.
     */
    public static void spawnScenery(LevelDefinition level) {
        SCENERY_REGISTRY.spawnAll(level, MarioContext.world().tileSize());
    }

    /** Ported from Mario.java's own literal {@code ImageUtil.resize(..., 32, 13*32)} - fixed regardless of the placing tile's own {@code lengthY}. */
    static final int WHITE_LINE_HEIGHT_TILES = 13;

    /**
     * Ported from {@code Mario.java}'s case 18 ("Wall") - a purely decorative,
     * non-collided vertical strip: the top row uses frame 0, every row below
     * uses frame 1 (no left/right cap distinction, unlike {@link #spawnTree}'s
     * own trunk decoration).
     */
    static void spawnWall(LevelDefinition.Tile tile) {
        int tileSize = MarioContext.world().tileSize();
        TextureRegion[][] frames = MarioResourceManager.region("wall").split(tileSize, tileSize);
        for (int dx = 0; dx < tile.lengthX; dx++) {
            for (int dy = 0; dy < tile.lengthY; dy++) {
                TextureRegion frame = frames[0][dy == 0 ? 0 : 1];
                MarioContext.spawn(new Scenery((tile.x + dx) * tileSize, (tile.y + dy) * tileSize, frame));
            }
        }
    }

    /** @param blackAndWhite CloudsNight (see this class's own "Bouncer"/tree cases) also swaps SmallCastle/BigCastle for their "bw_"-prefixed variants. */
    static String sceneryRegion(String type, boolean blackAndWhite) {
        switch (type) {
            case "SmallCastle":
                return blackAndWhite ? "bw_small_castle" : "small_castle";
            case "BigCastle":
                return blackAndWhite ? "bw_big_castle" : "big_castle";
            default:
                return null;
        }
    }

    /**
     * Ported from {@code Mario.java}'s case 19 - the first row is the
     * shooting turret head, every row after it a plain solid body segment
     * (row 1 and row 2+ use different frames of the same strip, matching
     * the original's own {@code y==1}/{@code else} split).
     */
    static void spawnRocketLauncher(LevelDefinition.Tile tile) {
        int tileSize = MarioContext.world().tileSize();
        TextureRegion[][] frames = MarioResourceManager.region("rocket_launcher").split(tileSize, tileSize);
        for (int dy = 0; dy < tile.lengthY; dy++) {
            float x = tile.x * tileSize;
            float y = (tile.y + dy) * tileSize;
            if (dy == 0) {
                add(new RocketLauncher(x, y, frames[0][0], tileSize));
            } else {
                add(new RocketLauncherBody(x, y, frames[dy == 1 ? 1 : 2][0]));
            }
        }
    }

    interface CellSpawner {
        void spawn(float x, float y);
    }

    static void forEachCell(LevelDefinition.Tile tile, CellSpawner spawner) {
        int tileSize = MarioContext.world().tileSize();
        for (int dx = 0; dx < tile.lengthX; dx++) {
            for (int dy = 0; dy < tile.lengthY; dy++) {
                spawner.spawn((tile.x + dx) * tileSize, (tile.y + dy) * tileSize);
            }
        }
    }

    /**
     * Ported from {@code Mario.java}'s case 17 ("tree"), "GreenAndTrees"
     * branch only (see {@code Tree}'s own class doc for why): the top row
     * (dy=0) is a solid, cap-selected {@link Tree} brick per column; every
     * row below is purely decorative, and only in the strip's *middle*
     * columns (matching the original's own {@code x==0}/
     * {@code x==lengthX-1} exclusion for the trunk) - drawn via a
     * pre-sliced single frame (index 3) of the "tree"/"bw_tree" region handed
     * to {@code Scenery}, rather than adding a frame-slicing constructor to
     * that class for one caller.
     */
    static void spawnTree(LevelDefinition.Tile tile, boolean blackAndWhite) {
        int tileSize = MarioContext.world().tileSize();
        int lastColumn = tile.lengthX - 1;
        TextureRegion trunkFrame = MarioResourceManager.region(blackAndWhite ? "bw_tree" : "tree")
                .split(tileSize, tileSize)[0][3];
        for (int dx = 0; dx < tile.lengthX; dx++) {
            float x = (tile.x + dx) * tileSize;
            add(new Tree(x, tile.y * tileSize, dx, lastColumn, blackAndWhite, tileSize));
            if (dx == 0 || dx == lastColumn) {
                continue;
            }
            for (int dy = 1; dy < tile.lengthY; dy++) {
                MarioContext.spawn(new Scenery(x, (tile.y + dy) * tileSize, trunkFrame));
            }
        }
    }

    static void add(InteractiveBrick brick) {
        MarioContext.world().addBrick(brick);
        MarioContext.spawn(brick);
    }

    static void addEnemy(Enemy enemy) {
        MarioContext.world().addEnemy(enemy);
        MarioContext.spawn(enemy);
    }

    static void addLift(Lift lift) {
        MarioContext.world().addLift(lift);
        MarioContext.spawn(lift);
    }

    static void addBalanceLiftPlatform(BalanceLiftPlatform platform) {
        MarioContext.world().addLift(platform);
        MarioContext.spawn(platform);
    }
}

package au.com.guidebee.morsetoolkit.activity.mario.level;

import com.guidebee.game.graphics.TextureRegion;

import au.com.guidebee.morsetoolkit.activity.mario.MarioConfiguration;
import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.actors.bricks.Axe;
import au.com.guidebee.morsetoolkit.activity.mario.actors.bricks.Bank;
import au.com.guidebee.morsetoolkit.activity.mario.actors.bricks.Brick;
import au.com.guidebee.morsetoolkit.activity.mario.actors.bricks.BrickWithStar;
import au.com.guidebee.morsetoolkit.activity.mario.actors.bricks.InteractiveBrick;
import au.com.guidebee.morsetoolkit.activity.mario.actors.bricks.InvisibleBrck;
import au.com.guidebee.morsetoolkit.activity.mario.actors.bricks.Iron;
import au.com.guidebee.morsetoolkit.activity.mario.actors.bricks.Pump;
import au.com.guidebee.morsetoolkit.activity.mario.actors.bricks.QuestionMark;
import au.com.guidebee.morsetoolkit.activity.mario.actors.bricks.Tree;
import au.com.guidebee.morsetoolkit.activity.mario.actors.enemies.Boss;
import au.com.guidebee.morsetoolkit.activity.mario.actors.enemies.Enemy;
import au.com.guidebee.morsetoolkit.activity.mario.actors.enemies.EnemyMashroom;
import au.com.guidebee.morsetoolkit.activity.mario.actors.enemies.EnemyTurtle;
import au.com.guidebee.morsetoolkit.activity.mario.actors.enemies.EnemyTurtlePatrol;
import au.com.guidebee.morsetoolkit.activity.mario.actors.enemies.FlyingTurtlePatrol;
import au.com.guidebee.morsetoolkit.activity.mario.actors.enemies.OrbitingFireball;
import au.com.guidebee.morsetoolkit.activity.mario.actors.items.Coin;
import au.com.guidebee.morsetoolkit.activity.mario.actors.lifts.Lift;
import au.com.guidebee.morsetoolkit.activity.mario.actors.projectiles.BossFire;
import au.com.guidebee.morsetoolkit.activity.mario.actors.scenery.Scenery;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioContext;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioWorld;

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
        MarioWorld world = new MarioWorld(cols, rows);
        populateStaticGeometry(world, level);
        return world;
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

    /**
     * Spawns every interactive brick. Requires {@link MarioContext} to
     * already be initialized (bricks register themselves into
     * {@code MarioContext.world()} and append themselves via
     * {@code MarioContext.spawn(...)}) - call this after
     * {@code MarioContext.init(layerManager, world)}, not before.
     */
    public static void spawnBricks(LevelDefinition level) {
        int tileSize = MarioConfiguration.TILE_SIZE;
        for (LevelDefinition.Tile tile : level.tiles) {
            switch (tile.type) {
                case "Brick":
                    forEachCell(tile, (x, y) -> add(new Brick(x, y, level.attribute)));
                    break;
                case "Bank":
                    forEachCell(tile, (x, y) -> add(new Bank(x, y, level.attribute)));
                    break;
                case "QuestionMark":
                    forEachCell(tile, (x, y) -> add(new QuestionMark(x, y, level.attribute, "CoinInside")));
                    break;
                case "QuestionMarkWithMushroom":
                    forEachCell(tile, (x, y) -> add(new QuestionMark(x, y, level.attribute, "Mashroom")));
                    break;
                case "BrickWithStar":
                    forEachCell(tile, (x, y) -> add(new BrickWithStar(x, y, level.attribute)));
                    break;
                case "InvisibleBrckWith1Up":
                    forEachCell(tile, (x, y) -> add(new InvisibleBrck(x, y, level.attribute, "1UP")));
                    break;
                case "InvisibleBrckWithCoin":
                    forEachCell(tile, (x, y) -> add(new InvisibleBrck(x, y, level.attribute, "CoinInside")));
                    break;
                case "Iron":
                    forEachCell(tile, (x, y) -> add(new Iron(x, y, level.attribute)));
                    break;
                case "BridgeBloks":
                    forEachCell(tile, (x, y) -> add(new Brick(x, y,
                            MarioResourceManager.region("bridge_blocks"), level.attribute)));
                    break;
                case "tree":
                    spawnTree(tile);
                    break;
                case "pump":
                    for (int dy = 0; dy < tile.lengthY; dy++) {
                        boolean top = dy == 0;
                        add(new Pump(tile.x * tileSize, (tile.y + dy) * tileSize, level.attribute, top));
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /** How many {@code EnemyFireBall}-equivalents ring a "FireBar"/"BigFireBar" pivot, and their radius step - see {@code Mario.java}'s own case 28/29. */
    private static final int FIRE_BAR_COUNT = 6;
    private static final int BIG_FIRE_BAR_COUNT = 12;
    private static final int FIRE_BAR_RADIUS_STEP = 16;

    /**
     * Spawns every ground-walking enemy, plus every other {@code Enemy}
     * subclass that reads/writes {@code MarioWorld}'s enemy list (patrol
     * turtles, fire-bar rings, the boss) - see docs/MARIO_PORT_PLAN.md
     * Step 6.1 and docs/MARIO_PORT_PLAN_PHASE2.md Step P2.0.
     */
    public static void spawnEnemies(LevelDefinition level) {
        int tileSize = MarioConfiguration.TILE_SIZE;
        for (LevelDefinition.Tile tile : level.tiles) {
            switch (tile.type) {
                case "EnemyMushroom":
                    forEachCell(tile, (x, y) -> addEnemy(new EnemyMashroom(x, y, level.attribute)));
                    break;
                case "EnemyTurtle":
                    forEachCell(tile, (x, y) -> addEnemy(new EnemyTurtle(x, y, level.attribute)));
                    break;
                case "EnemyTurtlePatrol":
                    addEnemy(new EnemyTurtlePatrol(tile.x * tileSize, tile.y * tileSize, tile.patrolLength));
                    break;
                case "FlyingTurtlePatrol":
                    addEnemy(new FlyingTurtlePatrol(tile.x * tileSize, tile.y * tileSize, tile.patrolLength));
                    break;
                case "FireBar":
                    spawnFireBar(tile, FIRE_BAR_COUNT);
                    break;
                case "BigFireBar":
                    spawnFireBar(tile, BIG_FIRE_BAR_COUNT);
                    break;
                case "Boss":
                    addEnemy(new Boss(tile.x * tileSize, tile.y * tileSize, tile.patrolLength * tileSize, false));
                    break;
                case "BossHammer":
                    addEnemy(new Boss(tile.x * tileSize, tile.y * tileSize, tile.patrolLength * tileSize, true));
                    break;
                default:
                    break;
            }
        }
    }

    /** Ported from {@code Mario.java}'s case 28/29 - {@code count} {@code OrbitingFireball}s around one pivot, spaced {@code FIRE_BAR_RADIUS_STEP}px apart. */
    private static void spawnFireBar(LevelDefinition.Tile tile, int count) {
        int tileSize = MarioConfiguration.TILE_SIZE;
        float centerX = tile.x * tileSize + 8;
        float centerY = tile.y * tileSize + 8;
        boolean clockwise = "CW".equals(tile.extraInfo);
        for (int j = 0; j < count; j++) {
            addEnemy(new OrbitingFireball(centerX, centerY, j * FIRE_BAR_RADIUS_STEP, clockwise));
        }
    }

    /**
     * Spawns every hazard that isn't an {@code Enemy} (an axe's invisible
     * wall, a static drifting {@code BossFire}) - see docs/MARIO_PORT_PLAN_PHASE2.md
     * Step P2.0. Same {@link MarioContext} requirement as {@link #spawnBricks}.
     */
    public static void spawnHazards(LevelDefinition level) {
        int tileSize = MarioConfiguration.TILE_SIZE;
        for (LevelDefinition.Tile tile : level.tiles) {
            switch (tile.type) {
                case "Axe":
                    Axe axe = new Axe(tile.x * tileSize, tile.y * tileSize);
                    MarioContext.world().addAxe(axe);
                    MarioContext.spawn(axe);
                    break;
                case "BossFire":
                    BossFire fire = new BossFire(tile.x * tileSize, tile.y * tileSize);
                    MarioContext.world().addHazard(fire);
                    MarioContext.spawn(fire);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Spawns every placed (as opposed to dispensed-from-a-hit-brick)
     * collectible - just "Coin" for now, the only such type any World-1
     * level places directly. Same {@link MarioContext} requirement as
     * {@link #spawnBricks}.
     */
    public static void spawnItems(LevelDefinition level) {
        for (LevelDefinition.Tile tile : level.tiles) {
            if ("Coin".equals(tile.type)) {
                forEachCell(tile, (x, y) -> {
                    Coin coin = new Coin(x, y);
                    MarioContext.world().addCollectible(coin);
                    MarioContext.spawn(coin);
                });
            }
        }
    }

    /**
     * Spawns every moving platform. Same {@link MarioContext} requirement as
     * {@link #spawnBricks}. A tile's {@code patrolLength} field carries the
     * original's "Points" constructor argument - not a patrol distance for
     * this tile type, but how many source tiles wide to build the platform
     * (see {@code Lift}'s class doc) - see docs/MARIO_PORT_PLAN.md Step 7.1.
     */
    public static void spawnLifts(LevelDefinition level) {
        int tileSize = MarioConfiguration.TILE_SIZE;
        for (LevelDefinition.Tile tile : level.tiles) {
            Lift.Motion motion = liftMotion(tile.type);
            if (motion == null) {
                continue;
            }
            addLift(new Lift(tile.x * tileSize, tile.y * tileSize, motion, tile.patrolLength));
        }
    }

    private static Lift.Motion liftMotion(String type) {
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
     * Spawns the level-end flagpole, castles, and lava - purely decorative,
     * no collision of their own (see {@code Scenery}'s class doc; a lava pit
     * kills Mario only because falling into one means falling out of the
     * level's bottom, which {@code Player}'s own fall-out check already
     * handles - the original's own "Lava" tile is likewise added to a
     * non-collided background sprite group, see docs/MARIO_PORT_PLAN_PHASE2.md
     * Step P2.0). A tree's own solid canopy + decorative trunk are handled
     * separately by {@link #spawnTree} (called from {@link #spawnBricks} -
     * the canopy row is a real, solid {@code InteractiveBrick}, not
     * decoration). Every *other* background tile type (mountain/clouds) is
     * still left unrendered - visual polish outside Step 7.1's "world
     * mechanics" scope.
     */
    public static void spawnScenery(LevelDefinition level) {
        int tileSize = MarioConfiguration.TILE_SIZE;
        for (LevelDefinition.Tile tile : level.tiles) {
            if ("Lava".equals(tile.type)) {
                forEachCell(tile, (x, y) -> MarioContext.spawn(new Scenery(x, y, MarioResourceManager.region("lava"))));
                continue;
            }
            String regionName = sceneryRegion(tile.type);
            if (regionName == null) {
                continue;
            }
            MarioContext.spawn(new Scenery(tile.x * tileSize, tile.y * tileSize,
                    MarioResourceManager.region(regionName)));
        }
    }

    private static String sceneryRegion(String type) {
        switch (type) {
            case "Flag":
                return "flag";
            case "SmallCastle":
                return "small_castle";
            case "BigCastle":
                return "big_castle";
            default:
                return null;
        }
    }

    private interface CellSpawner {
        void spawn(float x, float y);
    }

    private static void forEachCell(LevelDefinition.Tile tile, CellSpawner spawner) {
        int tileSize = MarioConfiguration.TILE_SIZE;
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
     * pre-sliced single frame (index 3) of the "tree" region handed to
     * {@code Scenery}, rather than adding a frame-slicing constructor to
     * that class for one caller.
     */
    private static void spawnTree(LevelDefinition.Tile tile) {
        int tileSize = MarioConfiguration.TILE_SIZE;
        int lastColumn = tile.lengthX - 1;
        TextureRegion trunkFrame = MarioResourceManager.region("tree")
                .split(tileSize, tileSize)[0][3];
        for (int dx = 0; dx < tile.lengthX; dx++) {
            float x = (tile.x + dx) * tileSize;
            add(new Tree(x, tile.y * tileSize, dx, lastColumn));
            if (dx == 0 || dx == lastColumn) {
                continue;
            }
            for (int dy = 1; dy < tile.lengthY; dy++) {
                MarioContext.spawn(new Scenery(x, (tile.y + dy) * tileSize, trunkFrame));
            }
        }
    }

    private static void add(InteractiveBrick brick) {
        MarioContext.world().addBrick(brick);
        MarioContext.spawn(brick);
    }

    private static void addEnemy(Enemy enemy) {
        MarioContext.world().addEnemy(enemy);
        MarioContext.spawn(enemy);
    }

    private static void addLift(Lift lift) {
        MarioContext.world().addLift(lift);
        MarioContext.spawn(lift);
    }
}

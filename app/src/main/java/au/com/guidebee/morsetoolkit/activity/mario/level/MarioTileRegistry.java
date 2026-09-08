package au.com.guidebee.morsetoolkit.activity.mario.level;

import com.guidebee.game.graphics.TextureRegion;

import au.com.guidebee.morsetoolkit.activity.mario.MarioConfiguration;
import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.actors.bricks.Axe;
import au.com.guidebee.morsetoolkit.activity.mario.actors.bricks.Bank;
import au.com.guidebee.morsetoolkit.activity.mario.actors.bricks.BankWithItem;
import au.com.guidebee.morsetoolkit.activity.mario.actors.bricks.Brick;
import au.com.guidebee.morsetoolkit.activity.mario.actors.bricks.Bouncer;
import au.com.guidebee.morsetoolkit.activity.mario.actors.bricks.BrickWithStar;
import au.com.guidebee.morsetoolkit.activity.mario.actors.bricks.InvisibleBrck;
import au.com.guidebee.morsetoolkit.activity.mario.actors.bricks.Iron;
import au.com.guidebee.morsetoolkit.activity.mario.actors.bricks.Pump;
import au.com.guidebee.morsetoolkit.activity.mario.actors.bricks.QuestionMark;
import au.com.guidebee.morsetoolkit.activity.mario.actors.bricks.WoodenBridge;
import au.com.guidebee.morsetoolkit.activity.mario.actors.enemies.Boss;
import au.com.guidebee.morsetoolkit.activity.mario.actors.enemies.EnemyMashroom;
import au.com.guidebee.morsetoolkit.activity.mario.actors.enemies.EnemyTurtle;
import au.com.guidebee.morsetoolkit.activity.mario.actors.enemies.EnemyTurtlePatrol;
import au.com.guidebee.morsetoolkit.activity.mario.actors.enemies.FishyWater;
import au.com.guidebee.morsetoolkit.activity.mario.actors.enemies.FlyingTurtle;
import au.com.guidebee.morsetoolkit.activity.mario.actors.enemies.FlyingTurtlePatrol;
import au.com.guidebee.morsetoolkit.activity.mario.actors.enemies.Helmet;
import au.com.guidebee.morsetoolkit.activity.mario.actors.enemies.Monkey;
import au.com.guidebee.morsetoolkit.activity.mario.actors.enemies.OctoPussy;
import au.com.guidebee.morsetoolkit.activity.mario.actors.enemies.PiranhaPlant;
import au.com.guidebee.morsetoolkit.activity.mario.actors.enemies.SonOfABuitch;
import au.com.guidebee.morsetoolkit.activity.mario.actors.items.Coin;
import au.com.guidebee.morsetoolkit.activity.mario.actors.lifts.BalanceLiftPlatform;
import au.com.guidebee.morsetoolkit.activity.mario.actors.lifts.Lift;
import au.com.guidebee.morsetoolkit.activity.mario.actors.lifts.LiftCar;
import au.com.guidebee.morsetoolkit.activity.mario.actors.lifts.LiftFall;
import au.com.guidebee.morsetoolkit.activity.mario.actors.projectiles.BossFire;
import au.com.guidebee.morsetoolkit.activity.mario.actors.scenery.FlagPole;
import au.com.guidebee.morsetoolkit.activity.mario.actors.scenery.Scenery;
import au.com.guidebee.morsetoolkit.activity.mario.actors.scenery.Spring;
import au.com.guidebee.morsetoolkit.activity.mario.fx.LavaBall;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioContext;
import au.com.guidebee.morsetoolkit.platformer.level.TileHandler;
import au.com.guidebee.morsetoolkit.platformer.level.TileTypeRegistry;

/**
 * Mario's own {@link TileTypeRegistry} - one table reproducing every case
 * {@link LevelLoader}'s old six {@code spawn*} switch statements used to
 * handle, one-for-one (see docs/MARIO_GAME_MECHANICS.md §8 for the reference
 * table this was built against). Each handler body is the old case's body,
 * unchanged, reading {@code tileSize} from the parameter {@link
 * LevelLoader#spawnBricks} etc. used to read off a local variable.
 *
 * <p>Two cases needed real (not just mechanical) restructuring to fit a
 * table instead of a switch, both because a plain {@code switch} lets a case
 * fall through into the next one and a per-type table can't:
 * <ul>
 *   <li>{@code "pump"}/{@code "PumpWarp"} - the old switch's {@code "pump"}
 *   case ran its own plant-spawn check, then fell through into
 *   {@code "PumpWarp"}'s shared pump-body loop. Reproduced here as
 *   {@link #spawnPumpBody} called from both registrations, {@code "pump"}'s
 *   own handler additionally running the plant check first.
 *   <li>{@code "SmallCastle"}/{@code "BigCastle"} - previously the {@code
 *   default} branch of {@code spawnScenery}'s own if-chain, resolved via
 *   {@link LevelLoader#sceneryRegion}. Registered directly under both exact
 *   type strings here instead, calling the same {@code sceneryRegion} helper
 *   so the CloudsNight/{@code bw_*} swap logic isn't duplicated.
 * </ul>
 *
 * <p>One genuine behavior-shape change, not just a mechanical port: the old
 * {@code spawnScenery} returned the level's {@link FlagPole} (or null)
 * directly, since it was the one {@code spawn*} method with anything to
 * report back. A single {@code TileTypeRegistry.spawnAll} call has no
 * equivalent per-category return value, so the {@code "Flag"} handler below
 * instead calls {@code MarioContext.world().setFlagPole(...)}, and {@code
 * MarioGameScreen} now reads it back via {@code world.getFlagPole()} after
 * the spawn pass finishes rather than from a method's return value.
 */
public final class MarioTileRegistry {

    private MarioTileRegistry() {
    }

    /**
     * Six separate registries, not one combined table - deliberate, and not
     * what PLATFORMER_ENGINE_ARCHITECTURE.md §3.3's own single-{@code
     * TileTypeRegistry} sketch shows. {@code MarioGameScreen} calls
     * {@link LevelLoader#spawnScenery}/{@code spawnBricks}/{@code
     * spawnEnemies}/{@code spawnLifts}/{@code spawnHazards}/{@code
     * spawnItems} as six separate passes, in that fixed order, over the
     * level's full tile list - each pass appends every actor of its own
     * category to {@code MarioContext}'s {@code layerManager} before the
     * next category's pass starts (see that class's own "Scenery goes first
     * so bricks/enemies/the player draw in front of it" comment). A single
     * combined registry with one {@code spawnAll} pass would instead append
     * actors in whatever order their tiles happen to appear in a level's own
     * JSON - interleaving scenery/brick/enemy/etc. appends by file position
     * instead of by category, which would silently change on-screen draw
     * order (z-index) for any level whose tile list doesn't already happen
     * to group tiles by category. Six registries preserves the exact same
     * six-pass structure - and therefore the exact same append order - while
     * still replacing each pass's own switch statement with a table.
     */
    public static TileTypeRegistry buildBricks() {
        TileTypeRegistry registry = new TileTypeRegistry();
        registerBricks(registry);
        return registry;
    }

    public static TileTypeRegistry buildEnemies() {
        TileTypeRegistry registry = new TileTypeRegistry();
        registerEnemies(registry);
        return registry;
    }

    public static TileTypeRegistry buildHazards() {
        TileTypeRegistry registry = new TileTypeRegistry();
        registerHazards(registry);
        return registry;
    }

    public static TileTypeRegistry buildItems() {
        TileTypeRegistry registry = new TileTypeRegistry();
        registerItems(registry);
        return registry;
    }

    public static TileTypeRegistry buildLifts() {
        TileTypeRegistry registry = new TileTypeRegistry();
        registerLifts(registry);
        return registry;
    }

    public static TileTypeRegistry buildScenery() {
        TileTypeRegistry registry = new TileTypeRegistry();
        registerScenery(registry);
        return registry;
    }

    // ------------------------------------------------------------------
    // Interactive bricks - see LevelLoader.spawnBricks's old switch.
    // ------------------------------------------------------------------
    private static void registerBricks(TileTypeRegistry registry) {
        registry.register("Brick", (tile, level, tileSize) ->
                LevelLoader.forEachCell(tile, (x, y) -> LevelLoader.add(new Brick(x, y, level.attribute))));
        registry.register("Bank", (tile, level, tileSize) ->
                LevelLoader.forEachCell(tile, (x, y) -> LevelLoader.add(new Bank(x, y, level.attribute, tileSize))));
        registry.register("QuestionMark", (tile, level, tileSize) ->
                LevelLoader.forEachCell(tile, (x, y) ->
                        LevelLoader.add(new QuestionMark(x, y, level.attribute, "CoinInside", tileSize))));
        registry.register("QuestionMarkWithMushroom", (tile, level, tileSize) ->
                LevelLoader.forEachCell(tile, (x, y) ->
                        LevelLoader.add(new QuestionMark(x, y, level.attribute, "Mashroom", tileSize))));
        registry.register("BrickWithStar", (tile, level, tileSize) ->
                LevelLoader.forEachCell(tile, (x, y) -> LevelLoader.add(new BrickWithStar(x, y, level.attribute, tileSize))));
        registry.register("InvisibleBrckWith1Up", (tile, level, tileSize) ->
                LevelLoader.forEachCell(tile, (x, y) ->
                        LevelLoader.add(new InvisibleBrck(x, y, level.attribute, "1UP", tileSize))));
        registry.register("InvisibleBrckWithCoin", (tile, level, tileSize) ->
                LevelLoader.forEachCell(tile, (x, y) ->
                        LevelLoader.add(new InvisibleBrck(x, y, level.attribute, "CoinInside", tileSize))));
        registry.register("BrickWithMushroom", (tile, level, tileSize) ->
                LevelLoader.forEachCell(tile, (x, y) ->
                        LevelLoader.add(new BankWithItem(x, y, level.attribute, "Mashroom", tileSize))));
        registry.register("BrickWith1UP", (tile, level, tileSize) ->
                LevelLoader.forEachCell(tile, (x, y) ->
                        LevelLoader.add(new BankWithItem(x, y, level.attribute, "1UP", tileSize))));
        registry.register("BrickWithCoin", (tile, level, tileSize) ->
                LevelLoader.forEachCell(tile, (x, y) ->
                        LevelLoader.add(new BankWithItem(x, y, level.attribute, "CoinInside", tileSize))));
        registry.register("WoodenBridge", (tile, level, tileSize) ->
                LevelLoader.forEachCell(tile, (x, y) -> LevelLoader.add(new WoodenBridge(x, y))));
        registry.register("Iron", (tile, level, tileSize) ->
                LevelLoader.forEachCell(tile, (x, y) -> LevelLoader.add(new Iron(x, y, level.attribute, tileSize))));
        registry.register("BridgeBloks", (tile, level, tileSize) ->
                LevelLoader.forEachCell(tile, (x, y) -> LevelLoader.add(new Brick(x, y,
                        MarioResourceManager.region("bridge_blocks"), level.attribute))));
        registry.register("tree", (tile, level, tileSize) ->
                LevelLoader.spawnTree(tile, "CloudsNight".equals(level.backgroundImage)));
        registry.register("pump", (tile, level, tileSize) -> {
            // Ported from Mario.java's own case 12: a plant spawns *before*
            // the pump brick below (matching the original's own
            // PlantGroup-before-BrickGroup draw order), so it renders behind
            // the pipe and stays hidden while retracted instead of poking
            // out in front of it. Deliberately NOT "PumpWarp" too - see
            // PiranhaPlant's own class doc on that exclusion. Excludes this
            // level's own "OrangePump" bonus area, and any pipe shorter than
            // 3 tiles (see PiranhaPlant's own class doc for why).
            if (!"OrangePump".equals(level.levelName) && tile.lengthY >= 3) {
                String plantRegion = "Ground".equals(level.attribute) ? "plant" : "plant_dark";
                LevelLoader.addEnemy(new PiranhaPlant(tile.x * tileSize + tileSize / 2f,
                        tile.y * tileSize + (tileSize * 3) / 2f, plantRegion, tileSize));
            }
            spawnPumpBody(tile, level, tileSize);
        });
        registry.register("PumpWarp", MarioTileRegistry::spawnPumpBody);
        registry.register("HoriImage", (tile, level, tileSize) -> {
            // Ported from Mario.java's case 44 - two 2-tile pieces side by side, one tile-pair apart.
            int pieceSize = tileSize * 2;
            TextureRegion[][] frames = MarioResourceManager.region("hori_image")
                    .split(pieceSize * MarioConfiguration.ART_SCALE, pieceSize * MarioConfiguration.ART_SCALE);
            float x = tile.x * tileSize;
            float y = tile.y * tileSize;
            LevelLoader.add(new Pump(x, y, frames[0][0]));
            LevelLoader.add(new Pump(x + pieceSize, y, frames[0][1]));
        });
        registry.register("PumpImage", (tile, level, tileSize) ->
                LevelLoader.add(new Pump(tile.x * tileSize, tile.y * tileSize, MarioResourceManager.region("pump"))));
        registry.register("RocketLauncher", (tile, level, tileSize) -> LevelLoader.spawnRocketLauncher(tile));
        registry.register("Bouncer", (tile, level, tileSize) -> {
            Bouncer bouncer = new Bouncer(tile.x * tileSize, tile.y * tileSize, "CloudsNight".equals(level.backgroundImage));
            LevelLoader.add(bouncer);
            // The decorative Spring sits one tile above - see Bouncer's own
            // class doc for how the two are linked so it squishes on every launch.
            Spring spring = new Spring(tile.x * tileSize, tile.y * tileSize - tileSize, tileSize);
            MarioContext.spawn(spring);
            bouncer.setSpring(spring);
        });
    }

    /** Shared by "pump" and "PumpWarp" - see this class's own doc on the fall-through both used to share. */
    private static void spawnPumpBody(LevelDefinition.Tile tile, LevelDefinition level, int tileSize) {
        for (int dy = 0; dy < tile.lengthY; dy++) {
            boolean top = dy == 0;
            LevelLoader.add(new Pump(tile.x * tileSize, (tile.y + dy) * tileSize, level.attribute, top));
        }
    }

    // ------------------------------------------------------------------
    // Enemies - see LevelLoader.spawnEnemies's old switch.
    // ------------------------------------------------------------------
    private static void registerEnemies(TileTypeRegistry registry) {
        registry.register("EnemyMushroom", (tile, level, tileSize) ->
                LevelLoader.forEachCell(tile, (x, y) -> LevelLoader.addEnemy(new EnemyMashroom(x, y, level.attribute, tileSize))));
        registry.register("EnemyTurtle", (tile, level, tileSize) ->
                LevelLoader.forEachCell(tile, (x, y) -> LevelLoader.addEnemy(new EnemyTurtle(x, y, level.attribute, tileSize))));
        registry.register("Helmet", (tile, level, tileSize) ->
                LevelLoader.forEachCell(tile, (x, y) ->
                        LevelLoader.addEnemy(new Helmet(x, y, LevelLoader.helmetColor(level.attribute), tileSize))));
        registry.register("Monkey", (tile, level, tileSize) ->
                LevelLoader.forEachCell(tile, (x, y) -> LevelLoader.addEnemy(new Monkey(x, y, tileSize))));
        registry.register("FlyingTurtle", (tile, level, tileSize) ->
                // Ported from Mario.java's case 20: "normal" only for Ground, "dark" for everything else.
                LevelLoader.forEachCell(tile, (x, y) -> LevelLoader.addEnemy(new FlyingTurtle(x, y,
                        "Ground".equals(level.attribute) ? "normal" : "dark", tileSize))));
        registry.register("SonOfABuitch", (tile, level, tileSize) ->
                LevelLoader.forEachCell(tile, (x, y) -> LevelLoader.addEnemy(new SonOfABuitch(x, tileSize))));
        registry.register("EnemyTurtlePatrol", (tile, level, tileSize) ->
                LevelLoader.addEnemy(new EnemyTurtlePatrol(tile.x * tileSize, tile.y * tileSize, tile.patrolLength, tileSize)));
        registry.register("FlyingTurtlePatrol", (tile, level, tileSize) ->
                LevelLoader.addEnemy(new FlyingTurtlePatrol(tile.x * tileSize, tile.y * tileSize, tile.patrolLength, tileSize)));
        registry.register("FireBar", (tile, level, tileSize) -> LevelLoader.spawnFireBar(tile, LevelLoader.FIRE_BAR_COUNT));
        registry.register("BigFireBar", (tile, level, tileSize) -> LevelLoader.spawnFireBar(tile, LevelLoader.BIG_FIRE_BAR_COUNT));
        registry.register("Boss", (tile, level, tileSize) ->
                LevelLoader.addEnemy(new Boss(tile.x * tileSize, tile.y * tileSize, tile.patrolLength * tileSize, false, tileSize)));
        registry.register("BossHammer", (tile, level, tileSize) ->
                LevelLoader.addEnemy(new Boss(tile.x * tileSize, tile.y * tileSize, tile.patrolLength * tileSize, true, tileSize)));
        // Sea-level water enemies - ported from Mario.java's cases 54-57/58,
        // FishyWater's own Type argument (1=grey straight, 2=grey up-down,
        // 3=red straight, 4=red up-down).
        registry.register("FishGrey", (tile, level, tileSize) ->
                LevelLoader.addEnemy(new FishyWater(tile.x * tileSize, tile.y * tileSize, 1, tileSize)));
        registry.register("FishGreyUpDown", (tile, level, tileSize) ->
                LevelLoader.addEnemy(new FishyWater(tile.x * tileSize, tile.y * tileSize, 2, tileSize)));
        registry.register("FishRed", (tile, level, tileSize) ->
                LevelLoader.addEnemy(new FishyWater(tile.x * tileSize, tile.y * tileSize, 3, tileSize)));
        registry.register("FishRedUpDown", (tile, level, tileSize) ->
                LevelLoader.addEnemy(new FishyWater(tile.x * tileSize, tile.y * tileSize, 4, tileSize)));
        registry.register("OctoPussy", (tile, level, tileSize) ->
                LevelLoader.addEnemy(new OctoPussy(tile.x * tileSize, tile.y * tileSize, tileSize)));
    }

    // ------------------------------------------------------------------
    // Hazards - see LevelLoader.spawnHazards's old switch.
    // ------------------------------------------------------------------
    private static void registerHazards(TileTypeRegistry registry) {
        registry.register("Axe", (tile, level, tileSize) -> {
            Axe axe = new Axe(tile.x * tileSize, tile.y * tileSize, tileSize);
            MarioContext.world().addAxe(axe);
            MarioContext.spawn(axe);
        });
        registry.register("BossFire", (tile, level, tileSize) -> {
            BossFire fire = new BossFire(tile.x * tileSize, tile.y * tileSize, tileSize);
            MarioContext.world().addHazard(fire);
            MarioContext.spawn(fire);
        });
    }

    // ------------------------------------------------------------------
    // Items - see LevelLoader.spawnItems's old (non-switch) loop.
    // ------------------------------------------------------------------
    private static void registerItems(TileTypeRegistry registry) {
        registry.register("Coin", (tile, level, tileSize) ->
                LevelLoader.forEachCell(tile, (x, y) -> {
                    Coin coin = new Coin(x, y, tileSize);
                    MarioContext.world().addCollectible(coin);
                    MarioContext.spawn(coin);
                }));
    }

    // ------------------------------------------------------------------
    // Lifts - see LevelLoader.spawnLifts's old if-chain.
    // ------------------------------------------------------------------
    private static void registerLifts(TileTypeRegistry registry) {
        registry.register("BalenceLift", (tile, level, tileSize) -> LevelLoader.spawnBalanceLift(tile));
        registry.register("LiftFall", (tile, level, tileSize) -> {
            LiftFall fall = new LiftFall(tile.x * tileSize, tile.y * tileSize, tile.patrolLength, tileSize);
            MarioContext.world().addLift(fall);
            MarioContext.spawn(fall);
        });
        registry.register("LiftCar", (tile, level, tileSize) -> {
            LiftCar car = new LiftCar(tile.x * tileSize, tile.y * tileSize, tile.patrolLength, tileSize);
            MarioContext.world().addLift(car);
            MarioContext.spawn(car);
        });
        TileHandler plainLift = (tile, level, tileSize) ->
                LevelLoader.addLift(new Lift(tile.x * tileSize, tile.y * tileSize,
                        LevelLoader.liftMotion(tile.type), tile.patrolLength, tileSize));
        registry.register("Lift_UpDown", plainLift);
        registry.register("Lift_LeftRight", plainLift);
        registry.register("Lift_LeftRightInvert", plainLift);
        registry.register("LiftUP", plainLift);
        registry.register("LiftDown", plainLift);
    }

    // ------------------------------------------------------------------
    // Scenery - see LevelLoader.spawnScenery's old if-chain.
    // ------------------------------------------------------------------
    private static void registerScenery(TileTypeRegistry registry) {
        registry.register("Lava", (tile, level, tileSize) ->
                LevelLoader.forEachCell(tile, (x, y) ->
                        MarioContext.spawn(new Scenery(x, y, MarioResourceManager.region("lava")))));
        registry.register("LavaBall", (tile, level, tileSize) ->
                MarioContext.spawn(new LavaBall(tile.x * tileSize, tileSize)));
        registry.register("Water", (tile, level, tileSize) ->
                LevelLoader.forEachCell(tile, (x, y) ->
                        MarioContext.spawn(new Scenery(x, y, MarioResourceManager.region("water")))));
        registry.register("Wall", (tile, level, tileSize) -> LevelLoader.spawnWall(tile));
        registry.register("WhiteLine", (tile, level, tileSize) -> {
            float height = LevelLoader.WHITE_LINE_HEIGHT_TILES * tileSize;
            LevelLoader.forEachCell(tile, (x, y) -> MarioContext.spawn(new Scenery(x, y,
                    MarioResourceManager.region("white_line"), tileSize, height)));
        });
        registry.register("Flag", (tile, level, tileSize) -> {
            // Ported from Mario.java's case 32: the rod ("flag", a thin
            // 4x288 strip centered in its tile - the "+14" below) and the
            // ball ornament above it ("flag_sphere") never move; FlagPole
            // itself is the one part (the cloth) that slides once touched.
            // Both recolor to "_fence" variants for "CloudsNight" or
            // (literally) "Fence" backgrounds - not "Fence2".
            boolean fenceFlag = "CloudsNight".equals(level.backgroundImage) || "Fence".equals(level.backgroundImage);
            MarioContext.spawn(new Scenery(tile.x * tileSize + 14, tile.y * tileSize,
                    MarioResourceManager.region(fenceFlag ? "flag_fence" : "flag")));
            MarioContext.spawn(new Scenery(tile.x * tileSize, tile.y * tileSize - tileSize,
                    MarioResourceManager.region(fenceFlag ? "flag_sphere_fence" : "flag_sphere")));
            FlagPole flagPole = new FlagPole(tile.x, tile.y, tileSize);
            MarioContext.spawn(flagPole);
            MarioContext.world().setFlagPole(flagPole);
        });
        TileHandler castle = (tile, level, tileSize) -> {
            String regionName = LevelLoader.sceneryRegion(tile.type, "CloudsNight".equals(level.backgroundImage));
            MarioContext.spawn(new Scenery(tile.x * tileSize, tile.y * tileSize, MarioResourceManager.region(regionName)));
        };
        registry.register("SmallCastle", castle);
        registry.register("BigCastle", castle);
    }
}

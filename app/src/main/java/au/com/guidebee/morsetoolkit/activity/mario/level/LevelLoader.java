package au.com.guidebee.morsetoolkit.activity.mario.level;

import au.com.guidebee.morsetoolkit.activity.mario.MarioConfiguration;
import au.com.guidebee.morsetoolkit.activity.mario.actors.bricks.Bank;
import au.com.guidebee.morsetoolkit.activity.mario.actors.bricks.Brick;
import au.com.guidebee.morsetoolkit.activity.mario.actors.bricks.BrickWithStar;
import au.com.guidebee.morsetoolkit.activity.mario.actors.bricks.InteractiveBrick;
import au.com.guidebee.morsetoolkit.activity.mario.actors.bricks.InvisibleBrck;
import au.com.guidebee.morsetoolkit.activity.mario.actors.bricks.Iron;
import au.com.guidebee.morsetoolkit.activity.mario.actors.bricks.Pump;
import au.com.guidebee.morsetoolkit.activity.mario.actors.bricks.QuestionMark;
import au.com.guidebee.morsetoolkit.activity.mario.actors.enemies.Enemy;
import au.com.guidebee.morsetoolkit.activity.mario.actors.enemies.EnemyMashroom;
import au.com.guidebee.morsetoolkit.activity.mario.actors.enemies.EnemyTurtle;
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
 * </ul>
 *
 * Every other tile type (checkpoints, decorations, flying/patrol enemies...)
 * is still left untouched here - later steps.
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
            int index = staticTileIndex(tile.type, level.attribute);
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

    /** @return a {@code MarioConfiguration.TILE_*} index, or 0 if this type isn't static terrain. */
    private static int staticTileIndex(String type, String attribute) {
        switch (type) {
            case "stone":
                return themed(attribute, MarioConfiguration.TILE_STONE,
                        MarioConfiguration.TILE_STONE_UNDERGROUND, MarioConfiguration.TILE_STONE_CASTLE);
            case "chocolate":
                return themed(attribute, MarioConfiguration.TILE_CHOCOLATE,
                        MarioConfiguration.TILE_CHOCOLATE_UNDERGROUND, MarioConfiguration.TILE_CHOCOLATE_CASTLE);
            default:
                return 0;
        }
    }

    private static int themed(String attribute, int ground, int underGround, int castle) {
        if ("UnderGround".equals(attribute)) {
            return underGround;
        }
        if ("Castle".equals(attribute)) {
            return castle;
        }
        return ground;
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

    /**
     * Spawns every ground-walking enemy. Same {@link MarioContext}
     * requirement as {@link #spawnBricks}. World 1 never places a patrol
     * turtle or flying turtle (all {@code patrolLength} values are 0 in its
     * level data), so only the plain walkers are handled here - see
     * docs/MARIO_PORT_PLAN.md Step 6.1.
     */
    public static void spawnEnemies(LevelDefinition level) {
        for (LevelDefinition.Tile tile : level.tiles) {
            switch (tile.type) {
                case "EnemyMushroom":
                    forEachCell(tile, (x, y) -> addEnemy(new EnemyMashroom(x, y, level.attribute)));
                    break;
                case "EnemyTurtle":
                    forEachCell(tile, (x, y) -> addEnemy(new EnemyTurtle(x, y, level.attribute)));
                    break;
                default:
                    break;
            }
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

    private static void add(InteractiveBrick brick) {
        MarioContext.world().addBrick(brick);
        MarioContext.spawn(brick);
    }

    private static void addEnemy(Enemy enemy) {
        MarioContext.world().addEnemy(enemy);
        MarioContext.spawn(enemy);
    }
}

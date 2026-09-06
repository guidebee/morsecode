package au.com.guidebee.morsetoolkit.activity.mario.level;

import au.com.guidebee.morsetoolkit.activity.mario.MarioConfiguration;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioWorld;

/**
 * Turns a {@link LevelDefinition}'s tile list into a {@link MarioWorld}'s
 * {@code TiledLayer} cells. Step 3 scope only covers static terrain - brick,
 * stone and chocolate (see docs/MARIO_PORT_PLAN.md Step 3.1 and
 * {@code MarioConfiguration}'s note on why pipes aren't included). Every
 * other tile type (interactive bricks, pipes, enemies, items, checkpoints...)
 * becomes a Sprite actor in later steps and is left as an empty cell here.
 */
public final class LevelLoader {

    private LevelLoader() {
    }

    /** Builds a {@link MarioWorld} sized to the level's full tile extent and populates it. */
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

    public static void populateStaticGeometry(MarioWorld world, LevelDefinition level) {
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
            case "Brick":
                return themed(attribute, MarioConfiguration.TILE_BRICK,
                        MarioConfiguration.TILE_BRICK_UNDERGROUND, MarioConfiguration.TILE_BRICK_CASTLE);
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
}

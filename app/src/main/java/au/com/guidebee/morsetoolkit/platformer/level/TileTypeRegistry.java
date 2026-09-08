package au.com.guidebee.morsetoolkit.platformer.level;

import java.util.HashMap;
import java.util.Map;

import au.com.guidebee.morsetoolkit.activity.mario.level.LevelDefinition;

/**
 * A type-string-keyed table of {@link TileHandler}s, replacing what used to
 * be a hardcoded {@code switch} statement per spawn category - see
 * PLATFORMER_ENGINE_ARCHITECTURE.md §3.3. A game builds one of these (see
 * {@code MarioTileRegistry}) and calls {@link #spawnAll} once per level load;
 * a tile type with no registered handler is silently ignored, matching every
 * {@code spawn*} switch's own {@code default: break} today.
 */
public final class TileTypeRegistry {

    private final Map<String, TileHandler> handlers = new HashMap<>();

    /** @return this, so a game's registry setup reads as one fluent block. */
    public TileTypeRegistry register(String type, TileHandler handler) {
        handlers.put(type, handler);
        return this;
    }

    public void spawnAll(LevelDefinition level, int tileSize) {
        for (LevelDefinition.Tile tile : level.tiles) {
            TileHandler handler = handlers.get(tile.type);
            if (handler != null) {
                handler.spawn(tile, level, tileSize);
            }
        }
    }
}

package au.com.guidebee.morsetoolkit.activity.mario.level;

import com.guidebee.game.GameEngine;

import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

/**
 * Loads {@link LevelDefinition}s from {@code assets/mario/levels/level_<N>.json}
 * by level number, caching each after first load. Level numbers match the
 * original engine's exactly, including the char-literal-derived 97/98 for the
 * two World-1 bonus areas (see {@link LevelDefinition.Checkpoint}).
 */
public final class LevelCatalog {

    private static final Map<Integer, LevelDefinition> CACHE = new HashMap<>();

    private LevelCatalog() {
    }

    public static LevelDefinition load(int levelNumber) {
        LevelDefinition cached = CACHE.get(levelNumber);
        if (cached != null) {
            return cached;
        }

        String path = "mario/levels/level_" + levelNumber + ".json";
        String json = GameEngine.files.internal(path).readString();
        LevelDefinition definition;
        try {
            definition = LevelDefinition.parse(json);
        } catch (JSONException e) {
            throw new IllegalStateException("Malformed level asset: " + path, e);
        }

        CACHE.put(levelNumber, definition);
        return definition;
    }
}

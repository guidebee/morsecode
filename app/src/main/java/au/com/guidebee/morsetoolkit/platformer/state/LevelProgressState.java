package au.com.guidebee.morsetoolkit.platformer.state;

import com.guidebee.game.GameEngine;

/**
 * Tracks which levels have been cleared, backed by {@code Preferences}
 * (Android {@code SharedPreferences} under the hood) - see
 * PLATFORMER_ENGINE_ARCHITECTURE.md §2.2. Generalized from Mario's own
 * {@code MarioSaveState}: the only game-specific part was the hardcoded
 * preferences-file name, now a constructor parameter.
 *
 * <p>Deliberately just one boolean per level - "cleared" - not a separate
 * "reached" flag: a level-select screen only needs to know which level
 * buttons to unlock, and a level is unlockable the moment the *previous* one
 * is cleared, so a finer "reached but not cleared" state would never
 * actually be read by anything.
 */
public class LevelProgressState {

    private final String prefName;

    public LevelProgressState(String prefName) {
        this.prefName = prefName;
    }

    /** Called once a level successfully completes - see the relevant game screen's own "advance to next level" logic. */
    public void markCleared(int levelNumber) {
        GameEngine.app.getPreferences(prefName)
                .putBoolean(key(levelNumber), true)
                .flush();
    }

    public boolean isCleared(int levelNumber) {
        return GameEngine.app.getPreferences(prefName).getBoolean(key(levelNumber), false);
    }

    private static String key(int levelNumber) {
        return "cleared_" + levelNumber;
    }
}

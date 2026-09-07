package au.com.guidebee.morsetoolkit.activity.mario.state;

import com.guidebee.game.GameEngine;
import com.guidebee.game.Preferences;

/**
 * Tracks which main levels have been cleared, backed by {@code Preferences}
 * (Android {@code SharedPreferences} under the hood) - the same persistence
 * mechanism Flappy Bird's own {@code config.Configuration} already uses for
 * its high scores (see that class's {@code readConfiguration}/
 * {@code saveConfiguration}), matched here rather than introducing a second
 * pattern. See docs/MARIO_PORT_PLAN_PHASE2.md Step P2.2.1.
 *
 * <p>Deliberately just one boolean per level - "cleared" - not a separate
 * "reached" flag: {@code MarioMenuScreen} only needs to know which level
 * buttons to unlock, and a level is unlockable the moment the *previous* one
 * in its world (or the previous world's last level) is cleared, so a finer
 * "reached but not cleared" state would never actually be read by anything
 * (see this step's own risk note: "resist adding more than MarioMenuScreen
 * actually needs to render").
 */
public final class MarioSaveState {

    private static final String PREF_NAME = "mario_save_state";

    private MarioSaveState() {
    }

    /** Called once a level's checkpoint successfully advances to another level - see {@code MarioGameScreen#advanceToNextLevel}. */
    public static void markCleared(int levelNumber) {
        GameEngine.app.getPreferences(PREF_NAME)
                .putBoolean(key(levelNumber), true)
                .flush();
    }

    public static boolean isCleared(int levelNumber) {
        return GameEngine.app.getPreferences(PREF_NAME).getBoolean(key(levelNumber), false);
    }

    private static String key(int levelNumber) {
        return "cleared_" + levelNumber;
    }
}

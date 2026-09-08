package au.com.guidebee.morsetoolkit.activity.mario.state;

import au.com.guidebee.morsetoolkit.platformer.state.LevelProgressState;

/**
 * Tracks which main levels have been cleared - see docs/MARIO_PORT_PLAN_PHASE2.md
 * Step P2.2.1. A thin wrapper (not a subclass - same reasoning as {@code
 * MarioContext}'s own wrapper around {@code GameContext}, though this class
 * has no static-hides-instance conflict to worry about; composition is just
 * consistent with that precedent) around one private {@link LevelProgressState}
 * instance, namespaced under this game's own preferences file.
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

    private static final LevelProgressState STATE = new LevelProgressState("mario_save_state");

    private MarioSaveState() {
    }

    /** Called once a level's checkpoint successfully advances to another level - see {@code MarioGameScreen#advanceToNextLevel}. */
    public static void markCleared(int levelNumber) {
        STATE.markCleared(levelNumber);
    }

    public static boolean isCleared(int levelNumber) {
        return STATE.isCleared(levelNumber);
    }
}

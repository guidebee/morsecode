package au.com.guidebee.morsetoolkit.activity.mario.screen;

import au.com.guidebee.morsetoolkit.activity.BuildConfig;
import au.com.guidebee.morsetoolkit.activity.mario.MarioConfiguration;
import au.com.guidebee.morsetoolkit.activity.mario.MarioGamePlay;
import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.level.LevelNumbering;
import au.com.guidebee.morsetoolkit.activity.mario.state.MarioSaveState;
import au.com.guidebee.morsetoolkit.platformer.screen.WorldLevelSelectScreen;

/**
 * World-select -> per-world level-select, replacing the original engine's
 * {@code LevelNumber == 10} special-cased start screen (see
 * docs/MARIO_PORT_PLAN.md's Step 8 survey: the original re-purposed its own
 * gameplay loop for this, gating {@code initResources}/{@code update}/
 * {@code render} on that one magic level number and mutating two plain ints -
 * {@code StartWorld}/{@code StartLevel} - as its "menu").
 *
 * <p>Now a thin construction call into {@link WorldLevelSelectScreen} (see
 * PLATFORMER_ENGINE_ARCHITECTURE.md §3.6) - this class supplies only what's
 * genuinely Mario's own: {@link LevelNumbering#WORLD_LEVELS}, the title, and
 * {@link #isUnlocked}, the lock policy (a level is unlocked once the
 * previous level in its world - or, for a world's first level, the previous
 * world's last level - is cleared, per {@link MarioSaveState}; World 1's own
 * first level is always unlocked, there being no earlier level to require).
 * In a debug build ({@link BuildConfig#DEBUG}), every level is selectable
 * instead, per docs/MARIO_PORT_PLAN_PHASE2.md §8.3.1's warp tooling -
 * {@link MarioSaveState} itself is never written any differently either way.
 * Deliberately no lock on *entering* a world's level list itself (browsing
 * ahead is harmless) - only individual level buttons gate on progress.
 */
public class MarioMenuScreen extends WorldLevelSelectScreen {

    public MarioMenuScreen(MarioGamePlay gamePlay) {
        super(LevelNumbering.WORLD_LEVELS, MarioMenuScreen::isUnlocked, LevelNumbering::label,
                "SUPER MARIO BROS", MarioConfiguration.VIEWPORT_WIDTH, MarioConfiguration.VIEWPORT_HEIGHT,
                MarioResourceManager.uiSkin(), gamePlay::startLevel, gamePlay::finish, BuildConfig.DEBUG);
    }

    /** World 1's own first level needs no prior clear; every other level needs the one immediately before it (in its own world, or the previous world's last level) cleared. */
    private static boolean isUnlocked(int world, int levelNumber) {
        int[] levels = LevelNumbering.WORLD_LEVELS[world];
        if (levelNumber == levels[0]) {
            if (world == 0) {
                return true;
            }
            int[] previousWorldLevels = LevelNumbering.WORLD_LEVELS[world - 1];
            return MarioSaveState.isCleared(previousWorldLevels[previousWorldLevels.length - 1]);
        }
        for (int i = 1; i < levels.length; i++) {
            if (levels[i] == levelNumber) {
                return MarioSaveState.isCleared(levels[i - 1]);
            }
        }
        return false;
    }
}

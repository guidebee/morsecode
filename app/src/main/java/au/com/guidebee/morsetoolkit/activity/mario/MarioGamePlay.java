package au.com.guidebee.morsetoolkit.activity.mario;

import com.guidebee.game.GamePlay;

import au.com.guidebee.morsetoolkit.activity.mario.screen.MarioGameScreen;

/**
 * Mario Game Play. Owns cross-screen state and shared asset loading, following
 * the same pattern as {@code FlappyBirdGamePlay}/{@code BattleCityGamePlay}.
 *
 * The real level-select menu ({@code MarioMenuScreen}) lands in Step 8; for now
 * this always opens Level 11 directly, per Step 3.3's vertical slice.
 */
public class MarioGamePlay extends GamePlay {

    private final MarioGameActivity gameActivity;

    public MarioGamePlay(MarioGameActivity activity) {
        gameActivity = activity;
    }

    @Override
    public void create() {
        MarioResourceManager.load();
        setScreen(new MarioGameScreen(11, this));
    }

    public void finish() {
        gameActivity.backToMainActivity();
    }

    /**
     * Switches to another level at a specific spawn tile - how a checkpoint
     * (level-end flag, pipe entrance) actually finishes the transition it
     * starts; see {@code MarioGameScreen}'s level-completion state machine
     * and docs/MARIO_PORT_PLAN.md Step 7.1.
     *
     * <p>Level 12's own data includes checkpoints pointing at other worlds'
     * warp-zone secrets (levels 21/31/41) that v1 doesn't ship any JSON for
     * (see docs/MARIO_PORT_PLAN.md's World-1-only scope) - loading such a
     * level throws, which is caught here so finding one of those pipes just
     * silently does nothing instead of crashing.
     *
     * @return true if the switch happened
     */
    public boolean goToLevel(int levelNumber, int spawnTileX, int spawnTileY) {
        MarioGameScreen next;
        try {
            next = new MarioGameScreen(levelNumber, this, spawnTileX, spawnTileY);
        } catch (RuntimeException e) {
            return false;
        }
        setScreen(next);
        return true;
    }
}

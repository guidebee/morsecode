package au.com.guidebee.morsetoolkit.activity.mario;

import com.guidebee.game.GamePlay;

import au.com.guidebee.morsetoolkit.activity.mario.screen.MarioGameScreen;
import au.com.guidebee.morsetoolkit.activity.mario.screen.MarioMenuScreen;
import au.com.guidebee.morsetoolkit.activity.mario.state.GameStateController;

/**
 * Mario Game Play. Owns cross-screen state and shared asset loading, following
 * the same pattern as {@code FlappyBirdGamePlay}/{@code BattleCityGamePlay}.
 *
 * <p>{@link #gameState} is the one piece of state that survives a level
 * swap within a single playthrough (score/coins/lives/pause - see
 * {@code GameStateController}) - {@link #goToLevel} (checkpoint transitions)
 * carries it forward as-is, while {@link #startLevel} (a fresh pick from
 * {@link MarioMenuScreen}) resets it.
 */
public class MarioGamePlay extends GamePlay {

    private final MarioGameActivity gameActivity;
    private final GameStateController gameState = new GameStateController();

    public MarioGamePlay(MarioGameActivity activity) {
        gameActivity = activity;
    }

    @Override
    public void create() {
        MarioResourceManager.load();
        setScreen(new MarioMenuScreen(this));
    }

    public void finish() {
        gameActivity.backToMainActivity();
    }

    /** Score/coins/lives/pause for whichever level is currently active - see the class doc. */
    public GameStateController gameState() {
        return gameState;
    }

    /** Starts a brand-new game at the given level - see {@code MarioMenuScreen}. */
    public void startLevel(int levelNumber) {
        gameState.reset();
        setScreen(new MarioGameScreen(levelNumber, this));
    }

    /** Back to the level-select menu - a paused game's "quit", or a game over. */
    public void goToMenu() {
        setScreen(new MarioMenuScreen(this));
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

package au.com.guidebee.morsetoolkit.activity.mario.state;

import au.com.guidebee.morsetoolkit.platformer.state.ScoreLivesState;

/**
 * Mario's own score/coins/lives tuning - a thin subclass of {@link
 * ScoreLivesState} (see PLATFORMER_ENGINE_ARCHITECTURE.md §2.2) fixing this
 * game's own starting lives, coin-to-life threshold, and score-per-coin.
 *
 * <p>The original engine never actually finished this: its own
 * {@code DrawScore}'s SCORE/TIME lines are commented out, and
 * {@code Player.IncreaseLife()}'s body is commented out too (`Life` is
 * declared but never incremented) - see docs/MARIO_PORT_PLAN.md's Step 8
 * survey. This fills that gap in rather than porting the original's
 * half-finished state as-is: score/coins/lives are real and tracked, coins
 * wrap into an extra life at the classic 100-coin threshold, and running out
 * of lives is a real (if simple) game over instead of the original's
 * infinite-retry respawn.
 */
public class GameStateController extends ScoreLivesState {

    private static final int STARTING_LIVES = 3;
    private static final int COINS_PER_LIFE = 100;
    /** Classic NES value for a single coin. */
    private static final int SCORE_PER_COIN = 200;

    /** Starts a brand-new game - see {@code MarioMenuScreen}/{@code MarioGamePlay#startLevel}. */
    public GameStateController() {
        super(STARTING_LIVES, COINS_PER_LIFE, SCORE_PER_COIN);
    }
}

package au.com.guidebee.morsetoolkit.activity.mario.state;

/**
 * Cross-level game state: score, coins, lives, and whether the current level
 * is paused. Owned by {@code MarioGamePlay} (persists across the
 * checkpoint-driven level-to-level {@code MarioGameScreen} swaps within one
 * playthrough - see {@code MarioGamePlay#goToLevel}), reset fresh whenever
 * {@code MarioMenuScreen} starts a new game.
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
public class GameStateController {

    private static final int STARTING_LIVES = 3;
    private static final int COINS_PER_LIFE = 100;
    /** Classic NES value for a single coin. */
    private static final int SCORE_PER_COIN = 200;

    private int score;
    private int coins;
    private int lives;
    private boolean paused;

    public GameStateController() {
        reset();
    }

    /** Starts a brand-new game - see {@code MarioMenuScreen}/{@code MarioGamePlay#startLevel}. */
    public void reset() {
        score = 0;
        coins = 0;
        lives = STARTING_LIVES;
        paused = false;
    }

    public void addScore(int points) {
        score += points;
    }

    /** A single coin pickup - see {@code CoinPopEffect}. Also credits score and, every 100 coins, an extra life. */
    public void addCoin() {
        coins++;
        addScore(SCORE_PER_COIN);
        if (coins >= COINS_PER_LIFE) {
            coins -= COINS_PER_LIFE;
            addLife();
        }
    }

    /** A 1UP pickup - see {@code Life#onCollected}. */
    public void addLife() {
        lives++;
    }

    /**
     * Charges one life for a death - see {@code Player#consumeDeath}.
     *
     * @return true if that was the last life (lives now 0) - the caller
     * (@code MarioGameScreen}) should end the game instead of respawning.
     */
    public boolean loseLife() {
        lives = Math.max(0, lives - 1);
        return lives == 0;
    }

    public void pause() {
        paused = true;
    }

    public void resume() {
        paused = false;
    }

    public boolean isPaused() {
        return paused;
    }

    public int getScore() {
        return score;
    }

    public int getCoins() {
        return coins;
    }

    public int getLives() {
        return lives;
    }
}

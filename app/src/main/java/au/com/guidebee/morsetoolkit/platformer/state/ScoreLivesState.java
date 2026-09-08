package au.com.guidebee.morsetoolkit.platformer.state;

/**
 * Cross-level game state: score, coins, lives, and whether the current level
 * is paused - see PLATFORMER_ENGINE_ARCHITECTURE.md §2.2. Generalized from
 * Mario's own {@code GameStateController}: starting lives, the coin-to-life
 * threshold, and the score awarded per coin are all this game's own tuning,
 * threaded as constructor parameters instead of baked-in constants.
 */
public class ScoreLivesState {

    private final int startingLives;
    private final int coinsPerLife;
    private final int scorePerCoin;

    private int score;
    private int coins;
    private int lives;
    private boolean paused;

    public ScoreLivesState(int startingLives, int coinsPerLife, int scorePerCoin) {
        this.startingLives = startingLives;
        this.coinsPerLife = coinsPerLife;
        this.scorePerCoin = scorePerCoin;
        reset();
    }

    /** Starts a brand-new game. */
    public void reset() {
        score = 0;
        coins = 0;
        lives = startingLives;
        paused = false;
    }

    public void addScore(int points) {
        score += points;
    }

    /** A single coin pickup. Also credits score and, every {@code coinsPerLife} coins, an extra life. */
    public void addCoin() {
        coins++;
        addScore(scorePerCoin);
        if (coins >= coinsPerLife) {
            coins -= coinsPerLife;
            addLife();
        }
    }

    /** A 1UP-equivalent pickup. */
    public void addLife() {
        lives++;
    }

    /**
     * Charges one life for a death.
     *
     * @return true if that was the last life (lives now 0) - the caller
     * should end the game instead of respawning.
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

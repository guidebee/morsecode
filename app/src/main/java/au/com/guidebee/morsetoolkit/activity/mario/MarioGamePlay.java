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
        setScreen(new MarioGameScreen(11));
    }

    public void finish() {
        gameActivity.backToMainActivity();
    }
}

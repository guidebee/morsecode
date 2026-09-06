package au.com.guidebee.morsetoolkit.activity.mario;

import com.guidebee.game.GamePlay;

import au.com.guidebee.morsetoolkit.activity.mario.screen.MarioPlaceholderScreen;

/**
 * Mario Game Play. Owns cross-screen state and shared asset loading, following
 * the same pattern as {@code FlappyBirdGamePlay}/{@code BattleCityGamePlay}.
 *
 * The real menu/gameplay screens land in later steps of docs/MARIO_PORT_PLAN.md;
 * for now this loads World-1's assets (Step 2) and shows a placeholder screen.
 */
public class MarioGamePlay extends GamePlay {

    private final MarioGameActivity gameActivity;

    public MarioGamePlay(MarioGameActivity activity) {
        gameActivity = activity;
    }

    @Override
    public void create() {
        MarioResourceManager.load();
        setScreen(new MarioPlaceholderScreen());
    }

    public void finish() {
        gameActivity.backToMainActivity();
    }
}

package au.com.guidebee.morsetoolkit.activity.mario.screen;

import com.guidebee.game.GameEngine;
import com.guidebee.game.ScreenAdapter;

/**
 * Step-0 scaffolding only: proves the Activity -&gt; GamePlay -&gt; Screen chain
 * launches. Delete this class once {@code MarioMenuScreen} (docs/MARIO_PORT_PLAN.md
 * step 8) exists and replace the reference in {@code MarioGamePlay}.
 */
public class MarioPlaceholderScreen extends ScreenAdapter {

    /** Mario's classic sky-blue background, matching the original's BackGroundColor. */
    private static final float SKY_R = 92f / 255f;
    private static final float SKY_G = 148f / 255f;
    private static final float SKY_B = 252f / 255f;

    @Override
    public void render(float delta) {
        GameEngine.graphics.clearScreen(SKY_R, SKY_G, SKY_B, 1f);
    }
}

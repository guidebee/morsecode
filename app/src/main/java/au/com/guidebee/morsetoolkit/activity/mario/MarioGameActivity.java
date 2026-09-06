package au.com.guidebee.morsetoolkit.activity.mario;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.guidebee.game.Configuration;
import com.guidebee.game.activity.GameActivity;

/**
 * Mario Game Activity. Entry point for the Mario mini-game, following the
 * same Activity -&gt; GamePlay -&gt; Screen chain as
 * {@link au.com.guidebee.morsetoolkit.activity.flappybird.FlappyBirdGameActivity}
 * and {@link au.com.guidebee.morsetoolkit.activity.battlecity.BattleCityGameActivity}.
 *
 * See docs/MARIO_PORT_PLAN.md for the full port plan this scaffolding follows.
 */
public class MarioGameActivity extends GameActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration config = new Configuration();
        config.useAccelerometer = false;
        config.useCompass = false;
        config.useImmersiveMode = true;
        config.hideStatusBar = true;

        View gameView = initializeForView(new MarioGamePlay(this), config);
        RelativeLayout mainLayout = new RelativeLayout(this);
        mainLayout.addView(gameView);

        setContentView(mainLayout);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    public void backToMainActivity() {
        finish();
    }
}

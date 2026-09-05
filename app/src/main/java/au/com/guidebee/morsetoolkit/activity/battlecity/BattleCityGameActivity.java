package au.com.guidebee.morsetoolkit.activity.battlecity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.guidebee.game.Configuration;
import com.guidebee.game.activity.GameActivity;

import au.com.guidebee.morsetoolkit.ConfigInfo;
import au.com.guidebee.morsetoolkit.activity.flappybird.FlappyBirdGamePlay;
import au.com.guidebee.morsetoolkit.helper.MorseEncoder;


public class BattleCityGameActivity extends GameActivity {

    protected MorseEncoder morseEncoder = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        morseEncoder = new MorseEncoder(ConfigInfo.morseReceiveWPM);

        Configuration config = new Configuration();

        config.useAccelerometer = false;
        config.useCompass = false;

        View gameView = initializeForView(new BattleCityGamePlay(), config);
        RelativeLayout mainLayout = new RelativeLayout(this);
        mainLayout.addView(gameView);

        setContentView(mainLayout);
        au.com.guidebee.morsetoolkit.activity.flappybird.config.Configuration.gameActivity = this;
    }

    @Override
    public void onStart(){
        super.onStart();
        Runnable runnable= () -> morseEncoder.playMorseCode("sos");
        new Thread(runnable).start();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}

package au.com.guidebee.morsetoolkit.activity.battlecity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import com.guidebee.game.Configuration;
import au.com.guidebee.morsetoolkit.ads.AdSupportedGameActivity;

import au.com.guidebee.morsetoolkit.ConfigInfo;
import au.com.guidebee.morsetoolkit.helper.MorseEncoder;


public class BattleCityGameActivity extends AdSupportedGameActivity {

    protected MorseEncoder morseEncoder = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        morseEncoder = new MorseEncoder(ConfigInfo.morseReceiveWPM);

        Configuration config = new Configuration();

        config.useAccelerometer = false;
        config.useCompass = false;
        config.useImmersiveMode = true;
        config.hideStatusBar = true;

        View gameView = initializeForView(new BattleCityGamePlay(), config);
        // 3ec5b31 (2023-11-04): ALIGN_PARENT_TOP, despite its "bottom" comment.
        setGameContent(gameView, true);
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

package au.com.guidebee.morsetoolkit.activity.battlecity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.guidebee.game.Configuration;
import com.guidebee.game.activity.GameActivity;

import au.com.guidebee.morsetoolkit.ConfigInfo;
import au.com.guidebee.morsetoolkit.activity.flappybird.FlappyBirdGamePlay;
import au.com.guidebee.morsetoolkit.helper.MorseEncoder;


public class BattleCityGameActivity extends GameActivity {

    private AdView adView;
    protected MorseEncoder morseEncoder = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        morseEncoder = new MorseEncoder(ConfigInfo.morseReceiveWPM);
        // Create and load the AdView.
        adView = new AdView(this);
        adView.setAdUnitId("ca-app-pub-1370558989807131/4913862806");
        adView.setAdSize(AdSize.BANNER);
        // Add adView to the bottom of the screen.
        RelativeLayout.LayoutParams adParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        adParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);

        Configuration config = new Configuration();

        config.useAccelerometer = false;
        config.useCompass = false;

        View gameView = initializeForView(new BattleCityGamePlay(), config);
        RelativeLayout mainLayout = new RelativeLayout(this);
        mainLayout.addView(gameView);
        mainLayout.addView(adView, adParams);

        setContentView(mainLayout);
        au.com.guidebee.morsetoolkit.activity.flappybird.config.Configuration.gameActivity = this;
        if (adView != null) {
            if (ConfigInfo.showAds) {
                if (ConfigInfo.adRequest != null) {
                    adView.loadAd(ConfigInfo.adRequest);
                }
            }
        }


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

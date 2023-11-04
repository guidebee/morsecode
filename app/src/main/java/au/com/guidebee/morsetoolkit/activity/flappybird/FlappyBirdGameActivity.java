/*******************************************************************************
 * Copyright 2015 See AUTHORS file.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
//--------------------------------- PACKAGE ------------------------------------
package au.com.guidebee.morsetoolkit.activity.flappybird;

//--------------------------------- IMPORTS ------------------------------------

import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.guidebee.game.Configuration;
import com.guidebee.game.activity.GameActivity;

import au.com.guidebee.morsetoolkit.ConfigInfo;

//[------------------------------ MAIN CLASS ----------------------------------]

/**
 * Flappy bird Game Activity.
 * @author James Shen <james.shen@guidebee.com>
 */
public class FlappyBirdGameActivity extends GameActivity {
    private AdView adView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Create and load the AdView.
        adView = new AdView(this);
        adView.setAdUnitId("ca-app-pub-1370558989807131/4913862806");
        adView.setAdSize(AdSize.BANNER);
        // Add adView to the bottom of the screen.
        RelativeLayout.LayoutParams adParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        adParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        Configuration config = new Configuration();

        config.useAccelerometer = false;
        config.useCompass = false;

        View gameView = initializeForView(new FlappyBirdGamePlay(this), config);
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


    public void showBanner() {
        adView.postDelayed(() -> adView.setVisibility(View.VISIBLE), 1500);

    }

    public void backToMainActivity() {
        finish();
    }

    public void hideBanner() {
        adView.postDelayed(() -> adView.setVisibility(View.GONE), 1500);

    }
}

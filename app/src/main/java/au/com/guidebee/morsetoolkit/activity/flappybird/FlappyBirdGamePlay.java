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

import com.guidebee.game.GamePlay;
import com.guidebee.game.audio.Music;
import com.guidebee.game.audio.Sound;
import com.guidebee.game.graphics.TextureAtlas;
import com.guidebee.game.graphics.TextureRegion;

import au.com.guidebee.morsetoolkit.activity.flappybird.config.Configuration;
import au.com.guidebee.morsetoolkit.activity.flappybird.ui.MainWindow;

import static com.guidebee.game.GameEngine.assetManager;

//[------------------------------ MAIN CLASS ----------------------------------]

/**
 * Flappy bird Game Play.
 * @author James Shen <james.shen@guidebee.com>
 */
public class FlappyBirdGamePlay extends GamePlay {

    FlappyBirdGameActivity gameActivity;

    public FlappyBirdGamePlay(FlappyBirdGameActivity activity) {
        gameActivity = activity;
    }

    @Override
    public void create() {
        loadAssets();
        MainWindow mainWindow = new MainWindow(this);
        setScreen(mainWindow);
    }

    @Override
    public void dispose() {
        assetManager.dispose();
    }

    /**
     * Load game assets - sound effect and images.
     */
    private void loadAssets() {
        assetManager.load("birdmenu.atlas", TextureAtlas.class);
        assetManager.load("flappybird.atlas", TextureAtlas.class);

        assetManager.load("morsecode.atlas", TextureAtlas.class);
        assetManager.load("music.mp3", Music.class);
        assetManager.load("sfx_wing.ogg", Sound.class);
        assetManager.load("sfx_die.ogg", Sound.class);
        assetManager.load("sfx_hit.ogg", Sound.class);
        assetManager.load("sfx_point.ogg", Sound.class);
        assetManager.load("powerup.wav", Sound.class);
        assetManager.finishLoading();
        TextureAtlas textureAtlas = assetManager.get("flappybird.atlas",
                TextureAtlas.class);
        TextureRegion groundTextRegion = textureAtlas.findRegion("ground");
        Configuration.groundHeight = groundTextRegion.getRegionHeight();
    }

    public void showBanner() {
        gameActivity.showBanner();
    }

    public void hideBannder() {
        gameActivity.hideBanner();
    }

    public void finish() {
        gameActivity.backToMainActivity();
    }
}

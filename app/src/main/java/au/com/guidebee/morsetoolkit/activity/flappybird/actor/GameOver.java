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
package au.com.guidebee.morsetoolkit.activity.flappybird.actor;

//--------------------------------- IMPORTS ------------------------------------

import com.guidebee.game.graphics.TextureAtlas;
import com.guidebee.game.graphics.TextureRegion;
import com.guidebee.game.scene.Actor;

import au.com.guidebee.morsetoolkit.activity.flappybird.config.Configuration;

import static com.guidebee.game.GameEngine.assetManager;

//[------------------------------ MAIN CLASS ----------------------------------]

/**
 * Game over image.
 * @author James Shen <james.shen@guidebee.com>
 */
public class GameOver extends Actor {

    /**
     * Constructor.
     */
    public GameOver() {
        super("GameOver");

        TextureAtlas textureAtlas = assetManager.get("flappybird.atlas",
                TextureAtlas.class);
        TextureRegion buttonTextRegion = textureAtlas.findRegion("gameover");
        setTextureRegion(buttonTextRegion);
        //initial position.
        setPosition((Configuration.SCREEN_WIDTH
                        - buttonTextRegion.getRegionWidth()) / 2,
                (Configuration.SCREEN_HEIGHT) / 2
                        + buttonTextRegion.getRegionHeight());
        setVisible(false);
    }
}

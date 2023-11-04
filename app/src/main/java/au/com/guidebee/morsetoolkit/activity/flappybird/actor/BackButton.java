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
import com.guidebee.math.Vector3;

import au.com.guidebee.morsetoolkit.activity.flappybird.FlappyBirdStage;
import au.com.guidebee.morsetoolkit.activity.flappybird.config.Configuration;

import static com.guidebee.game.GameEngine.assetManager;
import static com.guidebee.game.GameEngine.input;

//[------------------------------ MAIN CLASS ----------------------------------]

/**
 * Back game button.
 * @author James Shen <james.shen@guidebee.com>
 */
public class BackButton extends Actor {

    private final FlappyBirdStage stage;

    /**
     * Constructor
     * @param stage  the game stage.
     */
    public BackButton(FlappyBirdStage stage) {
        super("BackButton");
        this.stage = stage;
        TextureAtlas textureAtlas = assetManager.get("flappybird.atlas",
                TextureAtlas.class);
        TextureRegion buttonTextRegion = textureAtlas.findRegion("back");
        TextureRegion buttonTextRegion1 = new TextureRegion(buttonTextRegion, 0,
                0, buttonTextRegion.getRegionWidth() / 2,
                buttonTextRegion.getRegionHeight());
        setTextureRegion(buttonTextRegion1);
        //show in the center of the screen.
        setPosition(Configuration.SCREEN_WIDTH
                - buttonTextRegion.getRegionWidth(), 16);
        setVisible(false);


    }


    @Override
    public void act(float delta) {
        if (isVisible()) {
            if (input.isTouched()) {
                //handel touch event
                Vector3 touchPos = new Vector3();
                touchPos.set(input.getX(), input.getY(), 0);
                getStage().getCamera().unproject(touchPos);
                if (getBoundingAABB().contains(touchPos.x, touchPos.y)) {
                    stage.returnToMainMenu();
                }
            }
        }
    }
}

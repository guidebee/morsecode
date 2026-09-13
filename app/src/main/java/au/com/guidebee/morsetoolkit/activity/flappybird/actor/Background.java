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

import com.guidebee.game.graphics.Batch;
import com.guidebee.game.graphics.TextureAtlas;
import com.guidebee.game.graphics.TextureRegion;
import com.guidebee.game.scene.Actor;

import au.com.guidebee.morsetoolkit.activity.flappybird.config.Configuration;

import static com.guidebee.game.GameEngine.assetManager;
import static com.guidebee.game.GameEngine.graphics;

//[------------------------------ MAIN CLASS ----------------------------------]

/**
 * Game background, i.e. the remote buildings and trees.
 *
 * @author James Shen <james.shen@guidebee.com>
 */
public class Background extends Actor {

    private final TextureRegion backgroundTextureRegion;
    private final TextureRegion skyTextureRegion;
    /**
     * the background move a bit slow because it's far way.
     */
    private final int moveStep = 1;
    private int offset;
    private boolean stopMoving = false;
    /**
     * offset was originally advanced once per rendered frame, implicitly
     * assuming ~60fps. Scaling by delta*REFERENCE_FPS and accumulating the
     * fractional remainder keeps the same parallax speed regardless of the
     * display's actual refresh rate.
     */
    private static final float REFERENCE_FPS = 60f;
    private float moveAccumulator = 0f;


    /**
     * Constructor
     */
    public Background() {
        super("Background");
        TextureAtlas textureAtlas = assetManager.get("flappybird.atlas",
                TextureAtlas.class);
        backgroundTextureRegion = textureAtlas.findRegion("bg");
        skyTextureRegion = textureAtlas.findRegion("sky");

        setSize(Configuration.SCREEN_WIDTH,
                Configuration.SCREEN_HEIGHT);

    }


    @Override
    public void draw(Batch batch, float parentAlpha) {
        int backWidth = backgroundTextureRegion.getRegionWidth();
        int skyHeight = skyTextureRegion.getRegionHeight();
        int widthSize = Configuration.SCREEN_WIDTH / backWidth;
        int remainSize = Configuration.SCREEN_WIDTH
                - Configuration.groundHeight
                - backgroundTextureRegion.getRegionHeight();
        int skySize = 0;
        if (remainSize > 0) {
            skySize = remainSize / skyHeight;
            if (skySize * skyHeight < remainSize) skySize++;
        }
        if (widthSize * backWidth < Configuration.SCREEN_WIDTH) widthSize++;


        /**
         * animation -- moving slowly.
         */
        if (!stopMoving) {
            moveAccumulator += moveStep * graphics.getDeltaTime() * REFERENCE_FPS;
            int step = (int) moveAccumulator;
            moveAccumulator -= step;
            offset += step;
            offset %= backWidth;
        }
        for (int i = 0; i < widthSize + 1; i++) {
            batch.draw(backgroundTextureRegion, -offset + i * backWidth,
                    Configuration.groundHeight);
            for (int j = 0; j < skyHeight; j++) {
                batch.draw(skyTextureRegion, -offset + i * backWidth,
                        Configuration.groundHeight
                                + backgroundTextureRegion.getRegionHeight()
                                + j * skyHeight);
            }
        }
    }

    /**
     * Set the stop moving flag,if the flag is set,the background stop moving.
     * It happens when bird dies.
     *
     * @param stop
     */
    public void setStopMoving(boolean stop) {
        stopMoving = stop;
    }
}

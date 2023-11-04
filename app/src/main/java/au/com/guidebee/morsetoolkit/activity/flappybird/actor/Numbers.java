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
import com.guidebee.utils.collections.Array;

import static com.guidebee.game.GameEngine.assetManager;

//[------------------------------ MAIN CLASS ----------------------------------]

/**
 * Display numbers.
 * @author James Shen <james.shen@guidebee.com>
 */
public class Numbers {

    /**
     * Texture Region to store 10 number images.
     */
    private final Array<TextureRegion> textureRegions = new Array<TextureRegion>();


    /**
     * Constructor. default it's white color.
     */
    public Numbers() {
        TextureAtlas textureAtlas = assetManager.get("flappybird.atlas",
                TextureAtlas.class);
        for (int i = 0; i < 10; i++) {
            textureRegions.add(textureAtlas.findRegion(String.valueOf(i)));
        }
    }

    /**
     * Constructor. it uses green color and bigger size.
     * @param prefix
     */
    public Numbers(String prefix) {
        TextureAtlas textureAtlas = assetManager.get("birdmenu.atlas",
                TextureAtlas.class);
        for (int i = 0; i < 10; i++) {
            textureRegions.add(textureAtlas.findRegion(prefix + i));
        }
    }

    /**
     * Get the size of the number image.
     * @return get the size of the image.
     */
    public int getNumberHeight() {
        return textureRegions.get(0).getRegionHeight();
    }

    /**
     * Get the width of the number image.
     * @return
     */
    public int getNumberWidth() {
        return textureRegions.get(0).getRegionWidth();
    }

    /**
     * Draw the number in given position.
     * @param batch spriteBatch to be drawn on.
     * @param value the value of the number.
     * @param posX  x coordinate.
     * @param posY  y coordinate.
     */
    public void drawNumber(Batch batch, int value, int posX, int posY) {
        int newScore = value % 10000;
        int thousandsNum = newScore / 1000;

        int hundredsNum = (newScore % 1000) / 100;
        int tensNum = (newScore % 100) / 10;
        int unitsNum = (newScore % 10);

        int startX = posX;
        if (thousandsNum != 0) {
            batch.draw(textureRegions.get(thousandsNum), startX, posY);
            startX += 28;
        }
        if ((thousandsNum + hundredsNum) != 0) {
            batch.draw(textureRegions.get(hundredsNum), startX, posY);
            startX += 28;
        }
        if ((thousandsNum + hundredsNum + tensNum) != 0) {
            batch.draw(textureRegions.get(tensNum), startX, posY);
            startX += 28;
        }

        batch.draw(textureRegions.get(unitsNum), startX, posY);


    }

}

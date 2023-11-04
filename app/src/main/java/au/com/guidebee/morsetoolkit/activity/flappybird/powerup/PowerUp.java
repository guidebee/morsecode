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
package au.com.guidebee.morsetoolkit.activity.flappybird.powerup;

//--------------------------------- IMPORTS ------------------------------------

import com.guidebee.game.graphics.Batch;
import com.guidebee.game.graphics.TextureAtlas;
import com.guidebee.game.graphics.TextureRegion;
import com.guidebee.math.geometry.Rectangle;

import static com.guidebee.game.GameEngine.assetManager;

//[------------------------------ MAIN CLASS ----------------------------------]

/**
 * Base class for all power ups.
 * @author James Shen <james.shen@guidebee.com>
 */
public abstract class PowerUp {

    protected final TextureRegion textureRegion;
    protected final Rectangle boundRect = new Rectangle();
    /**
     * x coordinates of the power up.
     */
    public int posX;
    /**
     * y coordinate of the power up.
     */
    public int posY;
    protected String name;

    /**
     * Constructor.
     * @param name name of the power up.
     * @param x x coords.
     * @param y y coords.
     */
    public PowerUp(String name, int x, int y) {
        TextureAtlas textureAtlas = assetManager.get("flappybird.atlas",
                TextureAtlas.class);
        textureRegion = textureAtlas.findRegion(name);
        boundRect.width = textureRegion.getRegionWidth();
        boundRect.height = textureRegion.getRegionHeight();
        boundRect.x = x;
        boundRect.y = y;
        this.name = name;
        posX = x;
        posY = y;

    }

    /**
     * Get the power up type.
     * @return
     */
    public abstract Type getType();

    /**
     * Get the name of power up.
     * @return
     */
    public String getName() {
        return name;
    }

    public void draw(Batch batch) {
        batch.draw(textureRegion, posX, posY);
    }

    /**
     * Get the bound rectangle of the power up.
     * @return the bound rectangle.
     */
    public Rectangle getBoundRect() {
        boundRect.x = posX;
        return boundRect;
    }

    /**
     * power up types.
     * mushroom --grow bigger
     * tortoise -- slower speed
     * lightning -- speedup.
     */
    public enum Type {
        None,
        Mushroom,
        Tortoise,
        Lightning
    }

}

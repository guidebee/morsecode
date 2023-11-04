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
package au.com.guidebee.morsetoolkit.activity.flappybird.ui;

//--------------------------------- IMPORTS ------------------------------------

import com.guidebee.game.graphics.Batch;
import com.guidebee.game.ui.Image;

import au.com.guidebee.morsetoolkit.activity.flappybird.actor.Numbers;

//[------------------------------ MAIN CLASS ----------------------------------]

/**
 * Number image, use to display score and slider value.
 * @author James Shen <james.shen@guidebee.com>
 */
public class NumberImage extends Image {

    private final Numbers numbers;
    private int value;

    /**
     * Constructor.
     * @param value number value.
     * @param score  true use white color.false use bigger green color.
     */
    public NumberImage(int value, boolean score) {
        if (score) {
            numbers = new Numbers();
        } else {
            numbers = new Numbers("levelnum");
        }
        this.value = value;

    }

    /**
     * Set the number value.
     * @param value value of the number.
     */
    public void setValue(int value) {
        this.value = value;
    }


    @Override
    public void draw(Batch batch, float parentAlpha) {
        int offsetY = (int) ((getPrefHeight() - getImageHeight()) / 2);
        int offsetX = (int) ((getPrefWidth() - getImageWidth()) / 2);
        numbers.drawNumber(batch, value, (int) getX() + offsetX,
                (int) getY() + offsetY);
    }

    @Override
    public float getImageHeight() {
        return numbers.getNumberHeight();
    }

    @Override
    public float getImageWidth() {
        return String.valueOf(value).length()
                * numbers.getNumberWidth();
    }

}

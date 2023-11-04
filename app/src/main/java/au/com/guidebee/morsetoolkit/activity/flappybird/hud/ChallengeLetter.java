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
package au.com.guidebee.morsetoolkit.activity.flappybird.hud;

//--------------------------------- IMPORTS ------------------------------------

import com.guidebee.game.graphics.Batch;
import com.guidebee.game.graphics.TextureAtlas;
import com.guidebee.game.graphics.TextureRegion;
import com.guidebee.game.ui.HorizontalGroup;
import com.guidebee.game.ui.Image;
import com.guidebee.game.ui.Table;
import com.guidebee.game.ui.drawable.TextureRegionDrawable;

import au.com.guidebee.morsetoolkit.ConfigInfo;
import au.com.guidebee.morsetoolkit.activity.flappybird.config.Configuration;
import au.com.guidebee.morsetoolkit.helper.MorseHelper;

import static com.guidebee.game.GameEngine.assetManager;

//[------------------------------ MAIN CLASS ----------------------------------]

/**
 * Game morsecode challenge HUD component. Displayed at top left of the screen.
 * @author James Shen <james.shen@guidebee.com>
 */
public class ChallengeLetter extends Table {

    private final TextureRegion imageBackground;
    private final MorseImage[] imageLetters;

    private final TextureRegionDrawable[] numberDrawables;
    private final TextureRegionDrawable[] letterDrawables;
    private final TextureRegionDrawable[] dotDashDrawables;


    /**
     * Constructor.
     */
    public ChallengeLetter() {
        TextureAtlas textureAtlas = assetManager.get("morsecode.atlas",
                TextureAtlas.class);
        imageBackground = textureAtlas.findRegion("bombbackground");
        letterDrawables = new TextureRegionDrawable[26];
        numberDrawables = new TextureRegionDrawable[10];
        dotDashDrawables = new TextureRegionDrawable[2];
        imageLetters = new MorseImage[5];
        for (char i = 'a'; i <= 'z'; i++) {
            String regionName = "letter" + i;
            letterDrawables[i - 'a'] = new TextureRegionDrawable(textureAtlas.findRegion(regionName));
        }
        for (char i = '0'; i <= '9'; i++) {
            String regionName = "number" + i;
            numberDrawables[i - '0'] = new TextureRegionDrawable(textureAtlas.findRegion(regionName));
        }

        dotDashDrawables[0] = new TextureRegionDrawable(textureAtlas.findRegion("dot"));
        dotDashDrawables[1] = new TextureRegionDrawable(textureAtlas.findRegion("dash"));

        for (int i = 0; i < imageLetters.length; i++) {
            HorizontalGroup space = new HorizontalGroup();
            space.padLeft(10);
            imageLetters[i] = new MorseImage('#', null);
            add(imageLetters[i]);
            add(space);
        }

        setSize(200, 48);
        setPosition(10, Configuration.SCREEN_HEIGHT - 60);
    }

    public void setMorseCode(char[] letters) {
        int length = Math.min(letters.length, 5);
        if (!Configuration.userSettings.gameEncode) {

            for (int i = 0; i < 5; i++) {
                imageLetters[i].setLetterOrNumber('#');
            }
            String morseString = MorseHelper.morseCodeData.get(letters[0]);
            length = morseString.length();
            for (int i = 0; i < length; i++) {
                imageLetters[i].setLetterOrNumber(morseString.charAt(i));
            }
        } else {
            for (int i = 0; i < length; i++) {
                imageLetters[i].setLetterOrNumber(letters[i]);
            }
        }

    }

    class MorseImage extends Image {
        public char LetterOrNumber;

        public MorseImage(char letterOrNumber, TextureRegionDrawable textureRegion) {
            super(textureRegion);
            this.LetterOrNumber = letterOrNumber;
        }

        public void setLetterOrNumber(char letterOrNumber) {
            this.LetterOrNumber = letterOrNumber;
            if (letterOrNumber >= 'a' && letterOrNumber <= 'z') {
                setDrawable(letterDrawables[letterOrNumber - 'a']);
            } else if (letterOrNumber >= '0' && letterOrNumber <= '9') {
                setDrawable(numberDrawables[letterOrNumber - '0']);
            } else if (letterOrNumber=='.'){
                setDrawable(dotDashDrawables[0]);
            }else if(letterOrNumber=='-'){

                setDrawable(dotDashDrawables[1]);
            }
            else {
                LetterOrNumber = '#';
            }
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {

            if (LetterOrNumber != '#') {
                batch.draw(imageBackground, getX() - 9, getY() - 3);
                super.draw(batch, parentAlpha);
            }
        }
    }


}


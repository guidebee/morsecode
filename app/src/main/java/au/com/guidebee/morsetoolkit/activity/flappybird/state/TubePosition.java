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
package au.com.guidebee.morsetoolkit.activity.flappybird.state;

//--------------------------------- IMPORTS ------------------------------------

import com.guidebee.game.graphics.TextureAtlas;
import com.guidebee.game.graphics.TextureRegion;
import com.guidebee.utils.collections.Array;

import java.util.Random;

import au.com.guidebee.morsetoolkit.activity.flappybird.config.Configuration;
import au.com.guidebee.morsetoolkit.activity.flappybird.powerup.Lightning;
import au.com.guidebee.morsetoolkit.activity.flappybird.powerup.Mushroom;
import au.com.guidebee.morsetoolkit.activity.flappybird.powerup.PowerUp;
import au.com.guidebee.morsetoolkit.activity.flappybird.powerup.Tortoise;

import static com.guidebee.game.GameEngine.assetManager;

//[------------------------------ MAIN CLASS ----------------------------------]

/**
 *  Tube position.
 * @author James Shen <james.shen@guidebee.com>
 */
public class TubePosition {

    /**
     * Block size
     */
    public final static int BLOCK_SIZE = 3; //pixels
    private final static Random random = new Random();
    private static int tubeLength;
    private static int initialSpace;
    private static int maxTopTubeHeight;
    public static Array<Character> challengeLetters = new Array<>();

    static {
        tubeLength = Configuration.SCREEN_HEIGHT
                - Configuration.groundHeight;
        initialSpace = Configuration.SCREEN_WIDTH * 2;
        TextureAtlas textureAtlas = assetManager.get("flappybird.atlas",
                TextureAtlas.class);

        TextureRegion topTubeTextRegion = textureAtlas.findRegion("toptube");
        maxTopTubeHeight = topTubeTextRegion.getRegionHeight();
    }

    /**
     * tube x location
     */
    public int posX;
    /**
     * top tube height
     */
    public int topTubeHeight;
    /**
     * button tube height
     */
    public int bottomTubeHeight;
    /**
     * The distance between this and last tube.
     */
    public int width;
    /**
     * draw number or not
     */
    public boolean drawNumber = false;
    /**
     * power up type.
     */
    public PowerUp powerUp = null;
    public char letterOfMorseCode = '0';
    public boolean deleted = false;

    /**
     * Get block size if given in length. block size is used for self designed tubes.
     * @param len length
     * @return block size.
     */
    public static int getBlockSize(int len) {
        return len / BLOCK_SIZE;
    }

    /**
     * Get distace block size.
     * @param len
     * @return
     */
    public static int getDistanceBlockSize(int len) {
        return (len - Configuration.SCREEN_WIDTH / 4) / BLOCK_SIZE + 50;
    }

    /**
     * Add one tube position.
     * @param tubePositionArray
     * @param topTubeBlocks
     * @param bottomTubeBlocks
     * @param distanceBlocks
     */
    public static void addTubePosition(Array<TubePosition> tubePositionArray,
                                       int topTubeBlocks, int bottomTubeBlocks,
                                       int distanceBlocks) {
        TubePosition lastTubePosition;
        if (tubePositionArray.size == 0) {
            lastTubePosition = new TubePosition();
            lastTubePosition.posX = initialSpace;

        } else {
            lastTubePosition = tubePositionArray.get(tubePositionArray.size - 1);
        }

        TubePosition tubePosition = new TubePosition();

        tubePosition.topTubeHeight = topTubeBlocks * BLOCK_SIZE;
        if (tubePosition.topTubeHeight
                > maxTopTubeHeight - 10) {
            tubePosition.topTubeHeight
                    = maxTopTubeHeight - 10;
        }

        tubePosition.bottomTubeHeight = bottomTubeBlocks * BLOCK_SIZE;
        if (tubePosition.bottomTubeHeight < 0) {
            tubePosition.bottomTubeHeight = 10;
        }

        tubePosition.width = (distanceBlocks - 50) * BLOCK_SIZE + Configuration.SCREEN_WIDTH / 4;
        int deltaX = Math.abs(lastTubePosition.topTubeHeight
                - tubePosition.topTubeHeight);

        int minDistance = deltaX * Configuration.MOVE_SPEED / 3;
        if (tubePosition.width < minDistance) {
            tubePosition.width = minDistance;
        }
        tubePosition.posX = lastTubePosition.posX + tubePosition.width;

        if (tubeLength - tubePosition.topTubeHeight
                - tubePosition.bottomTubeHeight
                < Configuration.MIN_GAP) {
            tubePosition.topTubeHeight = 100 - random.nextInt(20);
            tubePosition.bottomTubeHeight = 120 + random.nextInt(30);
        }
        tubePositionArray.add(tubePosition);

    }

    /**
     * Reset tube position, make sure the first tube is in the right postion.
     * @param tubePositionArray
     */
    public static void resetTubePosition(Array<TubePosition> tubePositionArray) {
        if (tubePositionArray.size > 0) {
            TubePosition firstTubePosition = tubePositionArray.first();
            if (firstTubePosition != null) {
                int offsetX = 1000 - firstTubePosition.posX;
                for (int i = 0; i < tubePositionArray.size; i++) {
                    TubePosition tubePosition = tubePositionArray.get(i);
                    tubePosition.posX += offsetX;
                }
            }
        }
    }

    /**
     * Clone one tube.
     * @param tubePosition
     * @return
     */
    public static TubePosition clone(TubePosition tubePosition) {
        TubePosition newTubePosition = new TubePosition();
        newTubePosition.bottomTubeHeight = tubePosition.bottomTubeHeight;
        newTubePosition.posX = tubePosition.posX;
        newTubePosition.topTubeHeight = tubePosition.topTubeHeight;
        newTubePosition.width = tubePosition.width;
        newTubePosition.drawNumber = false;
        newTubePosition.letterOfMorseCode = tubePosition.letterOfMorseCode;
        if (random.nextInt(100) < Configuration.POWERUP_PERCENTAGE) {

            int type = random.nextInt(4) % 4;
            PowerUp.Type powerUpType = PowerUp.Type.values()[type];
            int powerX = tubePosition.posX + tubePosition.width / 2;
            int powerY = random.nextInt(200) + Configuration.groundHeight + 100;
            switch (powerUpType) {
                case None:
                case Mushroom:
                    newTubePosition.powerUp = new Mushroom(
                            powerX,
                            powerY);
                    break;
                case Tortoise:
                    newTubePosition.powerUp = new Tortoise(
                            powerX,
                            powerY);
                    break;
                case Lightning:
                    newTubePosition.powerUp = new Lightning(
                            powerX,
                            powerY);
                    break;

            }

        }
        return newTubePosition;


    }

    /**
     * Generate random tubes.
     * @param howMany
     * @return
     */
    public static Array<TubePosition> randomTubes(int howMany) {
        return randomTubes(howMany, true);
    }

    /**
     * Generate random tubes.whether to generate power ups too.
     * @param howMany
     * @param generatePowerups
     * @return
     */
    public static Array<TubePosition> randomTubes(int howMany, boolean generatePowerups) {
        Array<TubePosition> tubePositionArray = new Array<TubePosition>();
        challengeLetters.clear();

        int lastPosition = 0;
        TubePosition lastTubePosition = new TubePosition();
        for (int i = 0; i < howMany; i++) {
            TubePosition tubePosition = new TubePosition();
            tubePosition.posX = Configuration.SCREEN_WIDTH / 2 * i
                    + random.nextInt(BLOCK_SIZE * 5) + initialSpace;
            tubePosition.width = tubePosition.posX - lastPosition;
            lastPosition = tubePosition.posX;
            tubePosition.topTubeHeight = random.nextInt(tubeLength);
            if (tubePosition.topTubeHeight
                    > maxTopTubeHeight - 10) {
                tubePosition.topTubeHeight
                        = maxTopTubeHeight - 10;
            }
            tubePosition.bottomTubeHeight = tubeLength
                    - tubePosition.topTubeHeight
                    - Configuration.MIN_GAP - random.nextInt(30);
            if (tubePosition.bottomTubeHeight < 0) {
                tubePosition.bottomTubeHeight = 10;
            }

            if (tubeLength - tubePosition.topTubeHeight
                    - tubePosition.bottomTubeHeight
                    < Configuration.MIN_GAP) {
                tubePosition.topTubeHeight = 100 - random.nextInt(20);
                tubePosition.bottomTubeHeight = 120 + random.nextInt(30);
            }

            int deltaX = Math.abs(lastTubePosition.topTubeHeight
                    - tubePosition.topTubeHeight);

            int minDistance = deltaX * Configuration.MOVE_SPEED / 3;
            if (tubePosition.width < minDistance) {
                tubePosition.width = minDistance;
                tubePosition.posX = lastTubePosition.posX + tubePosition.width;
            }


            if (generatePowerups) {
                if (random.nextInt(100) < Configuration.POWERUP_PERCENTAGE) {

                    int type = random.nextInt(4) % 4;
                    PowerUp.Type powerUpType = PowerUp.Type.values()[type];
                    int powerX = tubePosition.posX + tubePosition.width / 2;
                    int powerY = random.nextInt(200) + Configuration.groundHeight + 100;
                    switch (powerUpType) {
                        case None:
                        case Mushroom:
                            tubePosition.powerUp = new Mushroom(
                                    powerX,
                                    powerY);
                            break;
                        case Tortoise:
                            tubePosition.powerUp = new Tortoise(
                                    powerX,
                                    powerY);
                            break;
                        case Lightning:
                            tubePosition.powerUp = new Lightning(
                                    powerX,
                                    powerY);
                            break;

                    }

                }
            }
            randomGenerateLetter(tubePosition);
            tubePositionArray.add(tubePosition);
            lastTubePosition = tubePosition;
        }
        return tubePositionArray;
    }

    private static void randomGenerateLetter(TubePosition tubePosition) {
        int letter = random.nextInt(36);
        if (letter < 26) {
            tubePosition.letterOfMorseCode = (char) ('a' + letter);
        } else {
            letter = letter - 26;
            tubePosition.letterOfMorseCode = (char) ('0' + letter);
        }
        challengeLetters.add(tubePosition.letterOfMorseCode);
    }

    @Override
    public String toString() {
        return posX + "," + topTubeHeight + "," + bottomTubeHeight + "," + width;
    }


}

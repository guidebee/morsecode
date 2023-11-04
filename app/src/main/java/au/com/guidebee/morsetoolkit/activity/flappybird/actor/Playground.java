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

import com.guidebee.game.audio.Sound;
import com.guidebee.game.graphics.Batch;
import com.guidebee.game.graphics.TextureAtlas;
import com.guidebee.game.graphics.TextureRegion;
import com.guidebee.game.scene.Actor;
import com.guidebee.math.geometry.Rectangle;
import com.guidebee.utils.collections.Array;

import java.util.Random;

import au.com.guidebee.morsetoolkit.ConfigInfo;
import au.com.guidebee.morsetoolkit.activity.flappybird.config.Configuration;
import au.com.guidebee.morsetoolkit.activity.flappybird.state.TubePosition;
import au.com.guidebee.morsetoolkit.helper.MorseHelper;

import static com.guidebee.game.GameEngine.assetManager;
import static com.guidebee.game.GameEngine.graphics;

//[------------------------------ MAIN CLASS ----------------------------------]

/**
 * Playground actor. This actor actually is a combined actor. it is in charge of
 * tubes and the ground and power ups.
 *
 * @author James Shen <james.shen@guidebee.com>
 */
public class Playground extends Actor {

    /**
     * Default power up period 10s.
     */
    private final static int POWERUP_PERIOD = 10;
    private final TextureRegion groundTextRegion;
    private final TextureRegion bottomTubeTextRegion;
    private final TextureRegion topTubeTextRegion;
    private final TextureRegion[] numberDrawables;
    private final TextureRegion[] letterDrawables;
    private final TextureRegion[] dotDashDrawables;
    private final TextureRegion imageBackground;
    private final TextureRegion imageDotAndDashBackground;
    private final TextureRegion imagePlus5;
    private final Random random = new Random();
    private final Rectangle topRect = new Rectangle();
    private final Rectangle bottomRect = new Rectangle();
    private final Rectangle scoreRect = new Rectangle();
    private final Sound hitSound;
    private final Sound pointSound;
    private final Sound powerUpSound;
    /**
     * Number used to draw tube number.
     */
    private final Numbers numbers = new Numbers();
    private char[] challengeLetters = new char[5];
    /**
     * move speed.
     */
    private int moveStep;
    private int offset;
    private int currentTubeIndex = 0;
    /**
     * all tube positions.
     */
    private Array<TubePosition> tubePositionArray = new Array<TubePosition>();
    private boolean stopMoving = false;
    /**
     * Score of the player.
     */
    private int tubeCount = -1;
    private int extraScore = 0;
    private float powerUpPeriod = POWERUP_PERIOD;

    /**
     * Constructor.
     */
    public Playground() {
        super("Playground");
        TextureAtlas textureAtlas = assetManager.get("flappybird.atlas",
                TextureAtlas.class);
        groundTextRegion = textureAtlas.findRegion("ground");
        bottomTubeTextRegion = textureAtlas.findRegion("bottomtube");
        topTubeTextRegion = textureAtlas.findRegion("toptube");
        hitSound = assetManager.get("sfx_hit.ogg", Sound.class);
        pointSound = assetManager.get("sfx_point.ogg", Sound.class);
        powerUpSound = assetManager.get("powerup.wav", Sound.class);
        setSize(Configuration.SCREEN_WIDTH,
                groundTextRegion.getRegionHeight());
        setPosition(0, 0);
        moveStep = Configuration.MOVE_SPEED;
        offset = 0;
        extraScore = 0;
        topRect.width = topTubeTextRegion.getRegionWidth();
        bottomRect.width = bottomTubeTextRegion.getRegionWidth();
        bottomRect.y = Configuration.groundHeight;
        scoreRect.width = topRect.width;
        scoreRect.y = bottomRect.y;
        scoreRect.height = Configuration.SCREEN_HEIGHT
                - Configuration.groundHeight;

        textureAtlas = assetManager.get("morsecode.atlas",
                TextureAtlas.class);
        imageBackground = textureAtlas.findRegion("boxbackground");
        imageDotAndDashBackground = textureAtlas.findRegion("dashdotbackground");
        imagePlus5 = textureAtlas.findRegion("plus5");
        letterDrawables = new TextureRegion[26];
        numberDrawables = new TextureRegion[10];
        dotDashDrawables = new TextureRegion[2];
        for (char i = 'a'; i <= 'z'; i++) {
            String regionName = "letter" + i;
            letterDrawables[i - 'a'] = textureAtlas.findRegion(regionName);
        }
        for (char i = '0'; i <= '9'; i++) {
            String regionName = "number" + i;
            numberDrawables[i - '0'] = textureAtlas.findRegion(regionName);
        }
        dotDashDrawables[0] = textureAtlas.findRegion("dot");
        dotDashDrawables[1] = textureAtlas.findRegion("dash");
        generateLevelData();




    }

    public char[] getChallengeLetters() {
        return challengeLetters;
    }

    public char randomLetterOrNumber() {
        Array<Character> array=TubePosition.challengeLetters;
        int letter = random.nextInt(20);
        return array.get((letter+currentTubeIndex) % array.size );
    }


    /**
     * Return player's tubeCount.
     *
     * @return player's tubeCount.
     */
    public int getScore() {
        return tubeCount + extraScore + 1;
    }

    /**
     * random generate the tube positions.
     */
    public void generateLevelData() {
        tubePositionArray.clear();
        //if there is user designed tubes, load it.
        if (Configuration.designedTubes != null) {
            TubePosition.resetTubePosition(Configuration.designedTubes);
            for (int i = 0; i < Configuration.designedTubes.size; i++) {
                TubePosition tubePosition = Configuration.designedTubes.get(i);
                tubePositionArray.add(TubePosition.clone(tubePosition));
            }

        } else {
            //otherwise random generate 200 pairs of tubes.
            tubePositionArray.addAll(TubePosition.randomTubes(200));
        }
        tubeCount = -1;
        currentTubeIndex = 0;
        extraScore=0;
        for (int i = 0; i < 5; i++) {
            challengeLetters[i] = randomLetterOrNumber();
        }

    }


    /**
     * Check to see if the bird collide with the tubes.
     *
     * @param bird bird to be checked
     * @return true, there is collision.
     */
    public boolean isCollideWithTube(Bird bird) {
        float x = bird.getCenterX();
        float y = bird.getCenterY();
        boolean isBigger = bird.isBigger();
        for (int i = 0; i < tubePositionArray.size; i++) {
            TubePosition tubePosition = tubePositionArray.get(i);
            if (!tubePosition.deleted) {
                if (tubePosition.powerUp != null) {

                    //check to see if the bird catches any power ups.
                    if (tubePosition.powerUp.getBoundRect().contains(x, y)) {
                        Helper.playSound(powerUpSound);
                        switch (tubePosition.powerUp.getType()) {
                            case Mushroom:
                                bird.growBigger();
                                break;
                            case Tortoise:
                                decreaseSpeed();
                                this.resetSpeed();
                                break;
                            case Lightning:
                                increaseSpeed();
                                this.resetSpeed();
                                break;
                        }
                        tubePosition.powerUp = null;
                    }
                }

                if (tubePosition.posX
                        > -bottomTubeTextRegion.getRegionWidth()
                        && tubePosition.posX
                        < Configuration.SCREEN_WIDTH) {

                    topRect.x = tubePosition.posX;
                    topRect.y = Configuration.SCREEN_HEIGHT
                            - tubePosition.topTubeHeight;
                    topRect.height = tubePosition.topTubeHeight;
                    bottomRect.x = tubePosition.posX;
                    bottomRect.height = tubePosition.bottomTubeHeight;

                    boolean collide = topRect.contains(x, y)
                            || bottomRect.contains(x, y);
                    if (collide) {
                        Helper.playSound(hitSound);
                        //check to see match challenge letters
                        if(Configuration.userSettings.gameEncode){
                            int ci=0;
                            for( ci=0;ci<5;ci++){
                                if(challengeLetters[ci]==tubePosition.letterOfMorseCode){
                                    break;
                                }
                            }
                            if(ci<5){
                                for(int j=ci+1;j<5;j++){
                                    challengeLetters[j-1]=challengeLetters[j];
                                }
                                challengeLetters[4]=randomLetterOrNumber();
                                tubePosition.deleted = true;
                                extraScore += 5;
                                return false;
                            }
                        }else{
                            if(challengeLetters[0]==tubePosition.letterOfMorseCode){
                                challengeLetters[0]=randomLetterOrNumber();
                                tubePosition.deleted = true;
                                extraScore += 5;
                                return false;
                            }
                        }

                        if (isBigger) {
                            tubePosition.deleted = true;
                            extraScore += 5;
                        }
                        return true;
                    }

                    //the bird passes this pair of tube .
                    scoreRect.x = tubePosition.posX;
                    if (scoreRect.contains(x, y)) {
                        if (tubeCount < i) {
                            tubeCount = i;
                            Helper.playSound(pointSound);
                            tubePosition.drawNumber = true;
                            currentTubeIndex = i;
                        }
                    }


                }
            }

        }
        return false;
    }


    /**
     * Increase game speed
     */
    public void increaseSpeed() {
        Configuration.MOVE_SPEED += 1;
        if (Configuration.MOVE_SPEED > 10) {
            Configuration.MOVE_SPEED = 10;
        }

        powerUpPeriod = POWERUP_PERIOD;
    }

    /**
     * Decrease game speed.
     */
    public void decreaseSpeed() {
        Configuration.MOVE_SPEED -= 1;
        if (Configuration.MOVE_SPEED < 1) {
            Configuration.MOVE_SPEED = 1;
        }
        powerUpPeriod = POWERUP_PERIOD;

    }

    /**
     * Reset game speed to normal.
     */
    public void resetSpeed() {
        moveStep = Configuration.MOVE_SPEED;
    }


    /**
     * Stop playground moving.
     *
     * @param stop true to stop.
     */
    public void setStopMoving(boolean stop) {
        stopMoving = stop;
    }

    /**
     * Check to see if reaches end of all tubes.
     *
     * @return true if it's the end.
     */
    public boolean reachEndOfTubes() {
        return (currentTubeIndex >= tubePositionArray.size - 1);
    }


    @Override
    public void act(float delta) {
        if (!stopMoving) {
            for (int i = 0; i < tubePositionArray.size; i++) {
                TubePosition tubePosition = tubePositionArray.get(i);

                tubePosition.posX -= moveStep;
                if (tubePosition.powerUp != null) {
                    tubePosition.powerUp.posX -= moveStep;
                }
            }
        }
        //check speed

        if (Configuration.MOVE_SPEED != Configuration.NORMAL_SPEED) {
            powerUpPeriod -= graphics.getDeltaTime();
        }
        if (powerUpPeriod < 0) {
            Configuration.MOVE_SPEED = Configuration.NORMAL_SPEED;
            resetSpeed();

        }

    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        int backWidth = groundTextRegion.getRegionWidth();
        int size = Configuration.SCREEN_WIDTH / backWidth;
        if (size * backWidth < Configuration.SCREEN_WIDTH) size++;
        //make the ground moving animation.
        if (!stopMoving) {
            offset -= moveStep;
            offset %= backWidth;
        }
        for (int i = 0; i < size + 1; i++) {
            batch.draw(groundTextRegion, offset + i * backWidth, 0);
        }
        for (int i = 0; i < tubePositionArray.size; i++) {
            TubePosition tubePosition = tubePositionArray.get(i);
            if ((tubePosition.posX > -bottomTubeTextRegion.getRegionWidth()
                    && tubePosition.posX < Configuration.SCREEN_WIDTH)) {
                if (!tubePosition.deleted) {
                    batch.draw(bottomTubeTextRegion, tubePosition.posX,
                            Configuration.groundHeight,
                            bottomTubeTextRegion.getRegionWidth(),
                            tubePosition.bottomTubeHeight);
                    batch.draw(topTubeTextRegion, tubePosition.posX,
                            Configuration.SCREEN_HEIGHT
                                    - tubePosition.topTubeHeight);
                    if (tubePosition.drawNumber) {
                        int num = String.valueOf(i + 1).length();
                        numbers.drawNumber(batch, i + 1,
                                tubePosition.posX
                                        + (bottomTubeTextRegion.getRegionWidth() - 28 * num) / 2,

                                (Configuration.SCREEN_HEIGHT - tubePosition.topTubeHeight +
                                        tubePosition.bottomTubeHeight) / 2 + 28);

                    }
                    if (tubePosition.powerUp != null) {
                        tubePosition.powerUp.draw(batch);
                    }

                    if(Configuration.userSettings.gameEncode){
                        drawMorseLetterEncode(batch, tubePosition);
                    }else{
                        drawMorseLetter(batch, tubePosition);
                    }

                    if(Configuration.userSettings.gameHint) {
                        if(Configuration.userSettings.gameEncode){
                            drawMorseLetter(batch, tubePosition);
                        }else{
                            drawMorseLetterEncode(batch, tubePosition);
                        }
                    }
                } else {
                    batch.draw(imagePlus5, tubePosition.posX,
                            Configuration.SCREEN_HEIGHT / 2 -
                                    32);
                }
            }

        }

    }

    private void drawMorseLetterEncode(Batch batch, TubePosition tubePosition) {
        String morseString = MorseHelper.morseCodeData.get(tubePosition.letterOfMorseCode);
        int offsetX = tubePosition.posX - 50;
        int offsetY = Configuration.groundHeight;

        int length = morseString.length();

        for (int i = 0; i < length; i++) {
            char ch = morseString.charAt(i);
            batch.draw(imageDotAndDashBackground, offsetX, offsetY + i * 48);
            if (ch == '.') {
                batch.draw(dotDashDrawables[0],
                        offsetX + 6, 6 + offsetY + (length - i - 1) * 48);
            } else {
                batch.draw(dotDashDrawables[1],
                        offsetX + 6, 6 + offsetY + (length - i - 1) * 48);
            }

        }

    }

    private void drawMorseLetter(Batch batch, TubePosition tubePosition) {
        int offsetX = tubePosition.posX + 2;
        int offsetY = 0;
        if (tubePosition.topTubeHeight > tubePosition.bottomTubeHeight) {
            //draw on top
            offsetY = Configuration.SCREEN_HEIGHT
                    - tubePosition.topTubeHeight + (tubePosition.topTubeHeight - 48) / 2;

        } else {
            offsetY = Configuration.groundHeight + (tubePosition.bottomTubeHeight - 48) / 2;

        }
        batch.draw(imageBackground, offsetX, offsetY);
        if (tubePosition.letterOfMorseCode >= 'a' && tubePosition.letterOfMorseCode <= 'z') {
            batch.draw(letterDrawables[tubePosition.letterOfMorseCode - 'a'],
                    offsetX + 6, offsetY + 6);
        }
        if (tubePosition.letterOfMorseCode >= '0' && tubePosition.letterOfMorseCode <= '9') {
            batch.draw(numberDrawables[tubePosition.letterOfMorseCode - '0'],
                    offsetX + 6, offsetY + 6);

        }
    }
}

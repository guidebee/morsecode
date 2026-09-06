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
import com.guidebee.game.graphics.Animation;
import com.guidebee.game.graphics.Batch;
import com.guidebee.game.graphics.TextureAtlas;
import com.guidebee.game.graphics.TextureRegion;
import com.guidebee.game.scene.Actor;
import com.guidebee.math.Vector3;
import com.guidebee.utils.collections.Array;

import au.com.guidebee.morsetoolkit.activity.flappybird.config.Configuration;

import static com.guidebee.game.GameEngine.assetManager;
import static com.guidebee.game.GameEngine.graphics;
import static com.guidebee.game.GameEngine.input;


//[------------------------------ MAIN CLASS ----------------------------------]

/**
 * The bird actor.
 * @author James Shen <james.shen@guidebee.com>
 */
public class Bird extends Actor {

    /**
     * Bird has 6 different colors.
     */
    //public final static String YELLOW="yellow";
    public final static String BLUE = "blue";
    //public final static String CYAN="cyan";
    // public final static String GREEN="green";
    //public final static String RED="red";
    //public final static String PURPLE="yellow";

    private static final int MOVEMENT = 100;
    /**
     * These variables are used when Bird (when big) hit the tubes and
     * the grace period for become normal size again, to avoid
     * the bird hit multiple times.
     */
    private final static int SHIFTING_PERIOD = 1;
    private final Animation flyAnimation;
    private final Animation bigFlyAnimation;
    private final TextureRegion birdTextRegion;
    private final int SPRITE_WIDTH = 34;
    private final int SPRITE_HEIGHT = 24;
    private final int SPRITE_FRAME_SIZE = 3;
    private final Sound flapSound;
    private final Sound dieSound;
    private Vector3 position;
    private Vector3 velocity;
    private float tick = 0.05f;
    private float elapsedTime = 0;
    private boolean isBigger = false;
    private float shiftingPeriod = SHIFTING_PERIOD;


    private boolean isLive = true;

    private String birdColor = BLUE;

    /**
     * Constructor. default color is blue
     */
    public Bird() {
        this(Configuration.userSettings.birdColor);
    }


    /**
     * Constructor.
     * @param color  bird color
     */
    public Bird(String color) {
        super("Bird");
        birdColor = color;
        TextureAtlas textureAtlas = assetManager.get("flappybird.atlas",
                TextureAtlas.class);
        birdTextRegion = textureAtlas.findRegion(birdColor + "birdanimation");
        flapSound = assetManager.get("sfx_wing.ogg", Sound.class);
        dieSound = assetManager.get("sfx_die.ogg", Sound.class);
        Array<TextureRegion> keyFrames = new Array<TextureRegion>();
        for (int i = 0; i < SPRITE_FRAME_SIZE; i++) {
            TextureRegion textureRegion = new TextureRegion(birdTextRegion,
                    i * SPRITE_WIDTH, 0,
                    SPRITE_WIDTH, SPRITE_HEIGHT);
            keyFrames.add(textureRegion);
        }
        flyAnimation = new Animation(tick, keyFrames);
        setTextureRegion(flyAnimation.getKeyFrame(0));
        setPosition(Configuration.BIRD_START_X,
                Configuration.BIRD_START_Y);
        position = new Vector3(Configuration.BIRD_START_X,
                Configuration.BIRD_START_Y, 0);
        velocity = new Vector3(0, 0, 0);
        TextureRegion bird1TextRegion = textureAtlas.findRegion("bird1");
        TextureRegion bird2TextRegion = textureAtlas.findRegion("bird2");
        TextureRegion bird3TextRegion = textureAtlas.findRegion("bird3");
        TextureRegion bird4TextRegion = textureAtlas.findRegion("bird4");

        Array<TextureRegion> keyFrames1 = new Array<TextureRegion>();
        keyFrames1.add(bird1TextRegion);
        keyFrames1.add(bird2TextRegion);
        keyFrames1.add(bird3TextRegion);
        keyFrames1.add(bird4TextRegion);

        bigFlyAnimation = new Animation(tick, keyFrames1);


    }

    /**
     * Reset bird information before restart game.
     */
    public void reset() {
        setPosition(Configuration.BIRD_START_X,
                Configuration.BIRD_START_Y);
        position.set(Configuration.BIRD_START_X,
                Configuration.BIRD_START_Y, 0);
        velocity.set(0, 0, 0);
        setRotation(0);
        Configuration.MOVE_SPEED = Configuration.NORMAL_SPEED;
        isLive = true;


    }

    public boolean isBigger() {
        return isBigger;
    }

    /**
     * Grow bigger, which can sustain one hit.
     */
    public void growBigger() {
        setSize(64, 54);
        isBigger = true;
        shiftingPeriod = SHIFTING_PERIOD;
        setTextureRegion(bigFlyAnimation.getKeyFrame(0));
    }

    /**
     * Reset the bird to its normal size.
     */
    public void resetSize() {
        setSize(SPRITE_WIDTH, SPRITE_HEIGHT);
        setTextureRegion(flyAnimation.getKeyFrame(0));
        isBigger = false;

    }


    /**
     * Check to see if the bird fly outside of the screen.
     * @return true, if it's outside.
     */
    public boolean isOutside() {
        return (position.y <= Configuration.groundHeight
                || position.y >= Configuration.SCREEN_HEIGHT);
    }

    /**
     * Return if bird is still alive
     * @return
     */
    public boolean isLive() {
        return isLive;
    }

    /**
     * Set bird alive flag.
     * @param live
     */
    public void setLive(boolean live) {
        isLive = live;
    }

    /**
     * Kill the bird.
     */
    public void killBird() {
        if (isBigger) {
            resetSize();

        } else {
            if (shiftingPeriod < 0) {
                isLive = false;
                setRotation(180);
                Helper.playSound(dieSound);
                setTextureRegion(flyAnimation.getKeyFrame(0));
                shiftingPeriod = -1f;

            }

        }
    }


    @Override
    public void draw(Batch batch, float parentAlpha) {

        TextureRegion textureRegion;
        if (isBigger) {
            textureRegion = (bigFlyAnimation.getKeyFrame(elapsedTime, true));
            batch.draw(textureRegion, getX(), getY());

        } else {
            super.draw(batch, parentAlpha);
        }
    }

    @Override
    public void act(float delta) {

        elapsedTime += graphics.getDeltaTime();

        if (!isBigger && isLive) {
            TextureRegion textureRegion = (flyAnimation.getKeyFrame(elapsedTime, true));
            setTextureRegion(textureRegion);
        }

        if (isLive) {

            /**
             * Each tap gives the bird a single upward flap, exactly like
             * the original Flappy Bird -- holding the screen down does
             * not keep it aloft, it must be tapped again for another lift.
             */
            if (input.justTouched()) {
                velocity.y = Configuration.FLAP_VELOCITY;
                setRotation(30);
                Helper.playSound(flapSound);
            }
        }

        /**
         * standard, frame-rate independent free-fall integration:
         * velocity accumulates gravity, position moves by velocity.
         */
        velocity.y -= Configuration.GRAVITY * delta;
        position.add(MOVEMENT * delta, velocity.y * delta, 0);

        if (isLive) {

            /**
             * the nose gradually dips towards a dive the longer it's
             * been since the last flap.
             */
            rotateBy(-120 * delta);

            /**
             * control the bird's turning angle.
             */
            float currentAngle = getRotation();
            if (currentAngle < -90 && currentAngle < 0) {
                setRotation(-90);
            }
            if (currentAngle > 90 && currentAngle > 0) {
                setRotation(90);
            }
            if (!isBigger && shiftingPeriod > 0) {
                shiftingPeriod -= graphics.getDeltaTime();
            }


        }

        /**
         * make sure the bird not to pass through the ground.
         */
        if (position.y < Configuration.groundHeight) {
            position.y = Configuration.groundHeight;
        }
        setY(position.y);

    }


}

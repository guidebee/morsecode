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
package au.com.guidebee.morsetoolkit.activity.flappybird.config;

//--------------------------------- IMPORTS ------------------------------------

import com.guidebee.game.GameEngine;
import com.guidebee.game.Preferences;
import com.guidebee.game.activity.GameActivity;
import com.guidebee.utils.collections.Array;

import au.com.guidebee.morsetoolkit.activity.flappybird.state.TubePosition;

//[------------------------------ MAIN CLASS ----------------------------------]

/**
 * Game configurations and some global variables.
 *
 * @author James Shen <james.shen@guidebee.com>
 */
public class Configuration {
    /**
     * Screen virtual width
     */
    public final static int SCREEN_WIDTH = 800;

    /**
     * Screen virtual height
     */
    public final static int SCREEN_HEIGHT = 450;

    /**
     * bird start pos x
     */
    public final static int BIRD_START_X = 300;

    /**
     * bird start pos y
     */
    public final static int BIRD_START_Y = 300;
    /**
     * min gab between top and bottom tubes
     */
    public final static int MIN_GAP = 60;
    /**
     * Preferences store name.
     */
    public final static String prefName = "flappybird";
    /**
     * User preferences.
     */
    public final static UserSettings userSettings = new UserSettings();
    /**
     * bird move speed
     */
    public static int NORMAL_SPEED = 5;
    /**
     * bird move speed
     */
    public static int MOVE_SPEED = NORMAL_SPEED;
    public static int POWERUP_PERCENTAGE = 30;
    /**
     * the height of ground
     */
    public static int groundHeight;
    /**
     * Music on or not.
     */
    public static boolean musicOn = true;
    /**
     * Sound on or not.
     */
    public static boolean soundOn = true;
    /**
     * Global variable to hold game activity.
     */
    public static GameActivity gameActivity;
    /**
     * Global variable to hold user designed tubes.
     */
    public static Array<TubePosition> designedTubes = null;

    /**
     * Read user configurations.
     */
    public final static void readConfiguration() {
        Preferences preferences = GameEngine.app.getPreferences(prefName);
        userSettings.soundOn = preferences.getBoolean("sound", true);
        soundOn = userSettings.soundOn;
        userSettings.musicOn = preferences.getBoolean("music", true);
        musicOn = userSettings.musicOn;

        userSettings.birdColor = preferences.getString("birdColor", "blue");
        userSettings.initMoveSpeed = preferences.getInteger("initMoveSpeed", 5);
        userSettings.musicVolume = preferences.getFloat("musicVolume", 0.5f);
        userSettings.soundVolume = preferences.getFloat("soundVolume", 0.5f);
        MOVE_SPEED = userSettings.initMoveSpeed;
        NORMAL_SPEED = userSettings.initMoveSpeed;

        userSettings.bestScore = preferences.getInteger("bestScore", 0);

        userSettings.last20scores.clear();
        for (int i = 0; i < 20; i++) {
            int score = preferences.getInteger("last20scores" + i, 0);
            if (score > 0) {
                userSettings.last20scores.add(score);
            }
        }
        userSettings.gameEncode = preferences.getBoolean("encode",true);
        userSettings.gameHint = preferences.getBoolean("hint",false);

    }

    /**
     * Save user Configurations.
     */
    public static void saveConfiguration() {
        Preferences preferences = GameEngine.app.getPreferences(prefName);
        preferences.putBoolean("sound", userSettings.soundOn);
        preferences.putBoolean("encode", userSettings.gameEncode);
        preferences.putBoolean("hint", userSettings.gameHint);
        preferences.putBoolean("music", userSettings.musicOn);
        preferences.putString("birdColor", userSettings.birdColor);
        preferences.putInteger("initMoveSpeed", userSettings.initMoveSpeed);
        preferences.putInteger("bestScore", userSettings.bestScore);

        preferences.putFloat("soundVolume", userSettings.soundVolume);
        preferences.putFloat("musicVolume", userSettings.musicVolume);
        for (int i = 0; i < userSettings.last20scores.size; i++) {
            int score = userSettings.last20scores.get(i);
            if (score > 0) {
                preferences.putInteger("last20scores" + i, score);
            }
        }
        preferences.flush();
    }

}

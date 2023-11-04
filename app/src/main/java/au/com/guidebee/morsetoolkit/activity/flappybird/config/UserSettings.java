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

import com.guidebee.utils.collections.Array;

//[------------------------------ MAIN CLASS ----------------------------------]

/**
 * User settings.
 * @author James Shen <james.shen@guidebee.com>
 */
public class UserSettings {

    /**
     * Last 20 high scores.
     */
    public final Array<Integer> last20scores = new Array<Integer>();
    /**
     * Color of the bird.
     */
    public String birdColor = "yellow";
    /**
     * Initial game speed.
     */
    public int initMoveSpeed;
    /**
     * Sound on or off.
     */
    public boolean soundOn;
    /**
     * If sound on, the sound volume value.
     */
    public float soundVolume;
    /**
     * Music on or off.
     */
    public boolean musicOn;
    /**
     * Music volume if its on.
     */
    public float musicVolume;
    /**
     * Best score.
     */
    public int bestScore;

    public boolean gameEncode;

    public boolean gameHint;

    /**
     * Add one score to the list.
     * @param score score to be added.
     */
    public void addScore(int score) {

        if (score > 0) {
            if (bestScore < score) {
                bestScore = score;
            }
            last20scores.add(score);
            last20scores.sort();

            if (last20scores.size > 20) {
                last20scores.removeIndex(0);
            }
            last20scores.reverse();
            bestScore = last20scores.get(0);
            Configuration.saveConfiguration();
        }

    }

}

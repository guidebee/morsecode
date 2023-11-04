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

import com.guidebee.game.audio.Music;
import com.guidebee.game.audio.Sound;

import au.com.guidebee.morsetoolkit.activity.flappybird.config.Configuration;

///[------------------------------ MAIN CLASS ----------------------------------]

/**
 * Helper class to play sound and music.
 * @author James Shen <james.shen@guidebee.com>
 */
public class Helper {

    /**
     * Play the sound with given volume level.
     * @param sound sound to play.
     */
    public static void playSound(Sound sound) {
        if (Configuration.soundOn) {
            sound.play(Configuration.userSettings.soundVolume);
        } else {
            sound.stop();
        }
    }

    /**
     * Play the music with given volume.
     * @param music music to play.
     */
    public static void playMusic(Music music) {
        if (Configuration.musicOn) {
            music.setVolume(Configuration.userSettings.musicVolume);
            music.play();

        } else {
            music.stop();
        }
    }


}

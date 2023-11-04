/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
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
package au.com.guidebee.morsetoolkit.decoder;

//--------------------------------- IMPORTS ------------------------------------


import java.util.Timer;
import java.util.TimerTask;

//[------------------------------ MAIN CLASS ----------------------------------]

/**
 * Key in morse code pattern match
 *
 * @author James Shen
 */
public class KeyboardMorseCodeDecoder extends MorseCodePatternMatch {
    private final TimerTask timerTask;
    private volatile boolean isOn = false;
    private Timer timer = null;
    private int samplePeriod = 25; //ms

    /**
     * Constructor
     */
    public KeyboardMorseCodeDecoder() {
        this(18, 25);
    }

    /**
     * constructor
     *
     * @param dotLimit     dotLimit
     * @param samplePeriod sample period in ms.
     */
    public KeyboardMorseCodeDecoder(int dotLimit, int samplePeriod) {
        super(dotLimit);
        this.samplePeriod = samplePeriod;
        timerTask = new TimerTask() {
            @Override
            public void run() {
                if (!isOn) {
                    process(false);
                }
            }
        };
    }

    /**
     * Start the timer to decode key input
     */
    public void startTimer() {
        timer = new Timer();
        timer.scheduleAtFixedRate(timerTask, 0, samplePeriod);
    }

    /**
     * cancel the timer.
     */
    public void cancelTimer() {
        try {
            if (timer != null) {
                timer.cancel();
            }
        } catch (Exception e) {
            //ignore errors.
        }
    }

    /**
     * key in dot or Dash, dot=0,dash=1
     *
     * @param input input
     */
    public void processKey(boolean input) {
        int factor = 15;
        if (!input) {
            for (int i = 0; i < factor; i++) {
                process(true);
            }
            for (int i = 0; i < factor; i++) {
                process(false);
            }
        } else {
            for (int i = 0; i < factor * 3; i++) {
                process(true);
            }
            for (int i = 0; i < factor; i++) {
                process(false);
            }
        }

    }

    public void setOnOff(boolean isOnOff) {
        isOn = isOnOff;
    }

    public void setSampleSpeed(int newSpeed) {
        samplePeriod = newSpeed;
    }

}

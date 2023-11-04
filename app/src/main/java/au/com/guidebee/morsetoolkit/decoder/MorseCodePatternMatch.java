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

import au.com.guidebee.morsetoolkit.helper.MorseHelper;

//[------------------------------ MAIN CLASS ----------------------------------]

/**
 * Base morse code pattern match
 *
 * @author James Shen
 */
public class MorseCodePatternMatch {


    private static final String MC_DOT_SYMBOL = ".";
    private static final String MC_DASH_SYMBOL = "-";
    private static final Character INVALID_SYMBOL = '^';
    protected int noCharDetectedCounter = 0;
    private float estimateDotLength = 0;
    private int sampleCounter = 0;
    private int spaceCounter = 0;
    private String morseMsg = "";
    private float dotLimit = 3f;
    private float partLimit = dotLimit;
    private float charLimit = dotLimit * 3;
    private float wordLimit = dotLimit * 7;
    private float dotLength = 0;
    private float dashLength = 0;
    private int wordLength = 0;
    private int lastWordLength = 0;
    private MorseCodeListener morseCodeListener;
    private Status decodeDataState;
    /**
     * Constructor
     *
     * @param dotLimit initial dot length.
     */
    public MorseCodePatternMatch(int dotLimit) {
        this.dotLimit = dotLimit;
        partLimit = dotLimit + dotLimit / 2;
        charLimit = dotLimit * 3 - dotLimit / 2;
        wordLimit = dotLimit * 6 - dotLimit / 2;
        reset();
    }

    private static Character getMorseLetter(String morseMsg) {
        if (MorseHelper.morseCodeReverseData.containsKey(morseMsg)) {
            return MorseHelper.morseCodeReverseData.get(morseMsg);
        }
        return INVALID_SYMBOL;
    }

    /**
     * Add a morse code listner
     *
     * @param listener listener object.
     */
    public void addListener(MorseCodeListener listener) {
        morseCodeListener = listener;
    }

    /**
     * process input. input is a boolean. stand for with tone or without tone.
     *
     * @param isTone is Tone input or not.
     */
    public void process(boolean isTone) {
        wordLength++;
        noCharDetectedCounter++;
        if (isTone) {//tone
            spaceCounter = 0;
            sampleCounter++;
            float dotLowerLimit = 3f;
            if (sampleCounter > dotLowerLimit - 1) {
                if (decodeDataState == Status.None) {
                    decodeDataState = Status.Tone;

                    if (morseCodeListener != null) {
                        morseCodeListener.onCharStart();
                    }
                }
            }
        } else {//none
            switch (decodeDataState) {
                case None: {
                    spaceCounter++;
                }
                break;
                case Tone: {
                    {
                        estimateWPM(sampleCounter);
                        if (sampleCounter > partLimit) {
                            morseMsg += MC_DASH_SYMBOL;
                            dashLength = sampleCounter;
                            sampleCounter = 0;
                            spaceCounter = 0;
                            decodeDataState = Status.None;
                            if (morseCodeListener != null) {
                                morseCodeListener.onCharEnd(MC_DASH_SYMBOL, (int) dashLength);
                            }
                        } else {
                            if (sampleCounter > partLimit / 2) {
                                morseMsg += MC_DOT_SYMBOL;
                                dotLength = sampleCounter;
                                sampleCounter = 0;
                                spaceCounter = 0;
                                decodeDataState = Status.None;
                                if (morseCodeListener != null) {
                                    morseCodeListener.onCharEnd(MC_DOT_SYMBOL, (int) dotLength);
                                }
                            }
                        }

                    }

                }
                break;
            }
        }
        if (spaceCounter > charLimit) {
            //can output
            if (morseMsg.length() > 0) {
                if (morseCodeListener != null) {
                    morseCodeListener.onEmit(getMorseLetter(morseMsg));
                    noCharDetectedCounter = 0;
                }
                reset();
            }
        }

        if (spaceCounter > wordLimit) {
            lastWordLength = wordLength;
            if (morseCodeListener != null) {
                morseCodeListener.onEmit(' ');
            }
            reset();
            wordLength = 0;
        }
        adjustWPM();
    }

    /**
     * change dot length.
     *
     * @param value dot length.
     */
    public void changeDotLimit(float value) {
        dotLimit = value;
        partLimit = dotLimit + dotLimit / 2;
        charLimit = dotLimit * 3 - dotLimit / 2;
        wordLimit = dotLimit * 6 - dotLimit / 2;
    }

    /**
     * Check to see if the input has valid WPM.
     *
     * @return
     */
    public boolean hasValidWPM() {
        return (dotLength * 2 > dashLength) && (dashLength < dotLength * 3.5f);
    }

    /**
     * Get the estimated dot length.
     *
     * @return estimated dot length.
     */
    public int getDotLength() {
        return (int) dashLength;
    }

    /**
     * Get the estimated word length.
     *
     * @return estimated word length.
     */
    public int getWordLength() {
        return lastWordLength;
    }

    private void estimateWPM(int newValue) {
        if (estimateDotLength > 0) {
            {
                float value1 = Math.min(estimateDotLength, newValue);
                float value2 = Math.max(estimateDotLength, newValue);
                if (value2 > value1 * 2.5 && value2 < value1 * 6) {
                    changeDotLimit(Math.min(value2 / 3, value1));
                }
            }

        }
        estimateDotLength = newValue;
    }

    private void reset() {
        sampleCounter = 0;
        spaceCounter = 0;
        decodeDataState = Status.None;
        morseMsg = "";
    }

    private void adjustWPM() {
        if (dashLength * dotLength > 0) {
            changeDotLimit(Math.min(dashLength / 3, dotLength));
        }
    }


    private enum Status {
        None,
        Tone
    }

    /**
     * Morse code decoder listener
     */
    public interface MorseCodeListener {
        void onEmit(Character character);

        void onCharStart();

        void onCharEnd(String dotOrDash, int length);
    }
}

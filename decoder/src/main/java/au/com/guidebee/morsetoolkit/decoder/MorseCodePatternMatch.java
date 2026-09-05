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

import java.util.Arrays;

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
    private final float minDotLimit;
    private final float maxDotLimit;
    private int consecutiveDiscardCount = 0;

    /**
     * How many too-short "glitches" in a row we'll silently discard before
     * concluding they're not glitches at all - just real, fast elements that
     * our current dotLimit is calibrated too slow to recognize. One discard
     * is very plausibly noise; three in a row, with nothing else classifying
     * successfully in between, is a miscalibrated threshold.
     */
    private static final int STUCK_DISCARD_LIMIT = 3;

    /**
     * How many recent confirmed elements of each kind (dot/dash) feed the
     * WPM estimate. A ratio comparison of just the single latest dot and
     * dash - what this used to do - lets one atypical element swing
     * calibration on the spot; a small rolling history and its median
     * means one outlier gets outvoted by the rest instead. Small enough to
     * still track a genuine mid-session WPM change within a few letters.
     */
    private static final int WPM_HISTORY_SIZE = 6;
    private final float[] dotHistory = new float[WPM_HISTORY_SIZE];
    private final float[] dashHistory = new float[WPM_HISTORY_SIZE];
    private int dotHistoryFill = 0;
    private int dashHistoryFill = 0;
    private int dotHistoryNext = 0;
    private int dashHistoryNext = 0;

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
        // A single misleading element-length comparison (e.g. a noise glitch
        // that happens to sit at a confusable ratio to the last real element)
        // can otherwise send changeDotLimit() to a wildly wrong value, and
        // since the thresholds derive from dotLimit, that one bad estimate
        // then makes *everything else* misclassify too - a runaway collapse.
        // Keeping dotLimit within a band around its starting guess bounds the
        // damage a single outlier can do while still tracking real WPM drift.
        this.minDotLimit = dotLimit / 4f;
        this.maxDotLimit = dotLimit * 4f;
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
                        if (sampleCounter > partLimit) {
                            consecutiveDiscardCount = 0;
                            recordDash(sampleCounter);
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
                                consecutiveDiscardCount = 0;
                                recordDot(sampleCounter);
                                morseMsg += MC_DOT_SYMBOL;
                                dotLength = sampleCounter;
                                sampleCounter = 0;
                                spaceCounter = 0;
                                decodeDataState = Status.None;
                                if (morseCodeListener != null) {
                                    morseCodeListener.onCharEnd(MC_DOT_SYMBOL, (int) dotLength);
                                }
                            } else {
                                // Too short to be a dot: normally a glitch/noise blip,
                                // not an element, so discard it instead of leaving
                                // decodeDataState stuck at Tone forever (sampleCounter
                                // would otherwise never reset, silently corrupting every
                                // element measured afterwards). Also skip recordDot()
                                // here - a glitch's length is noise, not a WPM sample,
                                // and feeding it in risks the runaway collapse the
                                // dotLimit clamp (and the history's median, to a lesser
                                // extent) guards against.
                                //
                                // But if *every* recent tone is landing here, it's not
                                // noise - dotLimit is calibrated too slow to recognize
                                // real (fast) elements at all, and since they're always
                                // discarded, nothing would ever correct it. After enough
                                // consecutive discards, trust the pattern and let it in.
                                consecutiveDiscardCount++;
                                if (consecutiveDiscardCount >= STUCK_DISCARD_LIMIT) {
                                    recordDot(sampleCounter);
                                    consecutiveDiscardCount = 0;
                                }
                                sampleCounter = 0;
                                spaceCounter = 0;
                                decodeDataState = Status.None;
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
    }

    /**
     * change dot length.
     *
     * @param value dot length.
     */
    public void changeDotLimit(float value) {
        dotLimit = Math.max(minDotLimit, Math.min(maxDotLimit, value));
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

    private void recordDot(int length) {
        dotHistory[dotHistoryNext] = length;
        dotHistoryNext = (dotHistoryNext + 1) % WPM_HISTORY_SIZE;
        if (dotHistoryFill < WPM_HISTORY_SIZE) {
            dotHistoryFill++;
        }
        recalibrateFromHistory();
    }

    private void recordDash(int length) {
        dashHistory[dashHistoryNext] = length;
        dashHistoryNext = (dashHistoryNext + 1) % WPM_HISTORY_SIZE;
        if (dashHistoryFill < WPM_HISTORY_SIZE) {
            dashHistoryFill++;
        }
        recalibrateFromHistory();
    }

    /**
     * Re-estimates dotLimit from the median of recent dot lengths and the
     * median of recent dash lengths (divided by 3), taking the smaller of
     * the two when both are available - same bias as the old single-sample
     * estimator (which took min(dashLength / 3, dotLength)), kept because it
     * favours recognising fast elements over a slower compromise estimate.
     * Using the median of several recent elements, rather than comparing
     * just the single latest dot/dash pair, means one atypical element gets
     * outvoted by the rest of the recent history instead of unilaterally
     * resetting calibration on its own.
     * <p>
     * Requires at least two total recorded elements before touching
     * dotLimit at all: a lone first sample is exactly the "single element
     * unilaterally sets calibration" failure mode this redesign exists to
     * avoid, just with a history of one instead of a confusable ratio - and
     * fully trusting it can cascade into misclassifying the very next
     * element (see dashThenDotAdaptsEstimatedDotLength).
     */
    private void recalibrateFromHistory() {
        boolean haveDots = dotHistoryFill > 0;
        boolean haveDashes = dashHistoryFill > 0;
        if (dotHistoryFill + dashHistoryFill < 2) {
            return;
        }
        float dotEstimate = haveDots ? median(dotHistory, dotHistoryFill) : 0;
        float dashEstimate = haveDashes ? median(dashHistory, dashHistoryFill) / 3f : 0;
        float candidate;
        if (haveDots && haveDashes) {
            candidate = Math.min(dotEstimate, dashEstimate);
        } else {
            candidate = haveDots ? dotEstimate : dashEstimate;
        }
        changeDotLimit(candidate);
    }

    private static float median(float[] values, int count) {
        float[] copy = Arrays.copyOf(values, count);
        Arrays.sort(copy);
        if (count % 2 == 1) {
            return copy[count / 2];
        }
        return (copy[count / 2 - 1] + copy[count / 2]) / 2f;
    }

    private void reset() {
        sampleCounter = 0;
        spaceCounter = 0;
        decodeDataState = Status.None;
        morseMsg = "";
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

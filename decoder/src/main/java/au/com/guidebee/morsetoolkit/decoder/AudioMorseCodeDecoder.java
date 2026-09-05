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

import java.util.ArrayList;

//[------------------------------ MAIN CLASS ----------------------------------]

/**
 * Audio morse code decoder.
 * <p>
 * Supports two ways of turning mic samples into a tone/silence verdict per
 * tick, chosen via {@link DetectionMode}:
 * <ul>
 *     <li>{@link DetectionMode#BROADBAND} (the default, and the original
 *     behavior this class has always had): a frequency-agnostic "how loud is
 *     this" magnitude, so it works regardless of the CW tone's pitch. The
 *     per-tick magnitude used to be computed by running a 128-point FFT,
 *     rebuilding an identical spectrum from its own unmodified magnitude/
 *     phase (a no-op), and inverse-transforming that back - which, because
 *     the inverse only asked for the first 64 of the reconstructed 128
 *     samples, works out to exactly the paired-sample magnitude computed
 *     directly below, just via two wasted FFT passes instead of none.
 *     Confirmed behaviorally equivalent and replaced for speed; nothing
 *     about detection itself changed.</li>
 *     <li>{@link DetectionMode#NARROWBAND}: a {@link ToneDetector} tuned to
 *     one tracked frequency via a Goertzel filter, better at rejecting
 *     broadband noise but only within its tracked band.</li>
 * </ul>
 *
 * @author James Shen
 */
public class AudioMorseCodeDecoder extends MorseCodePatternMatch {

    private static final int BLOCK_SIZE = 128;
    private static final int WAVEFORM_HIGH = 400;
    private static final int WAVEFORM_LOW = 50;

    /** Which per-tick tone/silence detector this decoder uses. */
    public enum DetectionMode {
        BROADBAND,
        NARROWBAND
    }

    private final Object syncObject = new Object();
    private final DetectionMode mode;
    private final ToneDetector narrowbandDetector;
    private int fs = 44100;
    private int multipleFactor = 2;
    private int numberOfPoints;
    private double threshold = 0.4;
    private double maxMagnitude = 1;
    private int shift = 0;
    private int frameSpeed;
    private DataBufferListener dataBufferListener = null;
    private ArrayList<Integer> savedData = new ArrayList<>();
    private ArrayList<Integer> savedRawData = new ArrayList<>();
    private ArrayList<Integer> tempData = new ArrayList<>();
    private ArrayList<Integer> tempRawData = new ArrayList<>();

    /**
     * Constructor
     *
     * @param dotLimit initial dot length.
     */
    public AudioMorseCodeDecoder(int dotLimit) {
        this(dotLimit, 44100, 0.4f, 800, 50, 1);
    }

    /**
     * Constructor
     *
     * @param dotLimit initial dot length.
     */
    public AudioMorseCodeDecoder(int dotLimit, int numberOfPoints, int frameSpeed) {
        this(dotLimit, 44100, 0.4f, numberOfPoints, frameSpeed, 1);
    }

    /**
     * Constructor
     *
     * @param dotLimit initial dot length.
     */
    public AudioMorseCodeDecoder(int dotLimit, int numberOfPoints, int frameSpeed, int multipleFactor) {
        this(dotLimit, 44100, 0.4f, numberOfPoints, frameSpeed, multipleFactor);
    }

    /**
     * Constructor
     *
     * @param dotLimit initial dot length.
     */
    public AudioMorseCodeDecoder(int dotLimit, int sampleFrequency,
                                 float threshold, int numberOfPoints, int frameSpeed, int multipleFactor) {
        this(dotLimit, sampleFrequency, threshold, numberOfPoints, frameSpeed, multipleFactor, DetectionMode.BROADBAND);
    }

    /**
     * Constructor that also picks the detection strategy.
     *
     * @param dotLimit       initial dot length, in {@value #BLOCK_SIZE}-sample ticks.
     * @param numberOfPoints how many waveform ticks to retain for display.
     * @param frameSpeed     how many ticks between waveform-listener callbacks.
     * @param sampleRate     the mic's actual sample rate, in Hz (only used by {@link DetectionMode#NARROWBAND}).
     * @param mode           which per-tick detector to use.
     */
    public AudioMorseCodeDecoder(int dotLimit, int numberOfPoints, int frameSpeed, int sampleRate, DetectionMode mode) {
        this(dotLimit, sampleRate, 0.4f, numberOfPoints, frameSpeed, 1, mode);
    }

    private AudioMorseCodeDecoder(int dotLimit, int sampleFrequency, float threshold, int numberOfPoints,
                                  int frameSpeed, int multipleFactor, DetectionMode mode) {
        super(dotLimit);
        this.fs = sampleFrequency;
        this.threshold = threshold;
        this.numberOfPoints = numberOfPoints;
        this.frameSpeed = frameSpeed;
        this.multipleFactor = multipleFactor;
        this.mode = mode;
        this.narrowbandDetector = mode == DetectionMode.NARROWBAND ? new ToneDetector(sampleFrequency) : null;
    }

    /**
     * Add a morse code listener
     *
     * @param listener listener object.
     */
    public void addDataBufferListener(DataBufferListener listener) {
        dataBufferListener = listener;
    }

    /**
     * Process audio buffer
     *
     * @param audioData     audio data
     * @param numberOfShort size of the audio buffer.
     */
    public void processAudioBuffer(short[] audioData, int numberOfShort) {
        if (mode == DetectionMode.NARROWBAND) {
            processNarrowband(audioData, numberOfShort);
        } else {
            processBroadband(audioData, numberOfShort);
        }
    }

    private void processBroadband(short[] audioData, int numberOfShort) {
        short[] audioData2;
        int numberOfShort2 = numberOfShort;
        if (multipleFactor > 1) {
            audioData2 = new short[multipleFactor * numberOfShort];
            numberOfShort2 = multipleFactor * numberOfShort;
            for (int i = 0; i < numberOfShort; i++) {
                for (int j = 0; j < multipleFactor; j++) {
                    audioData2[i * multipleFactor + j] = audioData[i];
                }
            }
        } else {
            audioData2 = audioData;
        }

        int sampleNumber = numberOfShort2 / BLOCK_SIZE;
        for (int k = 0; k < sampleNumber; k++) {
            int base = k * BLOCK_SIZE;
            double magnitude = 0;
            for (int j = 0; j < BLOCK_SIZE / 4; j++) {
                double a = audioData2[base + 2 * j] / 1024.0;
                double b = audioData2[base + 2 * j + 1] / 1024.0;
                double pairMagnitude = Math.sqrt(a * a + b * b);
                if (pairMagnitude > magnitude) {
                    magnitude = pairMagnitude;
                }
            }
            if (magnitude > maxMagnitude) {
                maxMagnitude = magnitude;
            }
            boolean isTone = magnitude > Math.max(maxMagnitude * threshold, 1);
            emitTick(isTone, magnitude);
        }
        if (noCharDetectedCounter > multipleFactor * getDotLength() * 50) {
            resetMaxMagnitude();
        }
    }

    private void processNarrowband(short[] audioData, int numberOfShort) {
        int blockCount = numberOfShort / BLOCK_SIZE;
        for (int k = 0; k < blockCount; k++) {
            int offset = k * BLOCK_SIZE;
            boolean isTone = narrowbandDetector.classify(audioData, offset, BLOCK_SIZE);
            double magnitude = narrowbandDetector.getDisplayMagnitude();
            if (magnitude > maxMagnitude) {
                maxMagnitude = magnitude;
            }
            emitTick(isTone, magnitude);
        }
        if (noCharDetectedCounter > getDotLength() * 50) {
            resetMaxMagnitude();
        }
    }

    /** Shared per-tick bookkeeping: feeds the dot/dash state machine and the waveform buffer. */
    private void emitTick(boolean isTone, double magnitude) {
        shift++;
        tempData.add(isTone ? WAVEFORM_HIGH : WAVEFORM_LOW);
        process(isTone);
        tempRawData.add(((int) (magnitude * WAVEFORM_HIGH + 0.5)) + WAVEFORM_LOW);

        if (shift >= frameSpeed) {
            synchronized (syncObject) {
                int removeCount = Math.min(frameSpeed, savedData.size());
                if (savedData.size() > numberOfPoints) {
                    for (int kk = 0; kk < removeCount; kk++) {
                        savedData.remove(0);
                        savedRawData.remove(0);
                    }
                }
                savedData.addAll(tempData);
                savedRawData.addAll(tempRawData);
                tempData.clear();
                tempRawData.clear();
                shift = 0;
                if (dataBufferListener != null) {
                    ArrayList<Integer> rawData = new ArrayList<>(savedRawData);
                    ArrayList<Integer> transformedData = new ArrayList<>(savedData);
                    dataBufferListener.onData(rawData, transformedData);
                }
            }
        }
    }

    public double getMaxMagnitude() {
        return maxMagnitude;
    }

    public void resetMaxMagnitude() {
        maxMagnitude = 1f;
        if (narrowbandDetector != null) {
            narrowbandDetector.reset();
        }
    }

    public void setMultipleFactor(int newFactor) {
        multipleFactor = newFactor;
    }

    public void setFrameSpeed(int newSpeed) {
        frameSpeed = newSpeed;
    }

    public void setThreshold(float newThreshold) {
        threshold = newThreshold;
    }

    /**
     * Morse code decoder listener
     */
    public interface DataBufferListener {
        void onData(ArrayList<Integer> rawData, ArrayList<Integer> transformedData);
    }

}

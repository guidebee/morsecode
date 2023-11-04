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

import au.com.guidebee.morsetoolkit.analysis.FFT;

//[------------------------------ MAIN CLASS ----------------------------------]

/**
 * Audio morse code decoder.
 *
 * @author James Shen
 */
public class AudioMorseCodeDecoder extends MorseCodePatternMatch {

    private final Object syncObject = new Object();
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
        super(dotLimit);
        this.fs = sampleFrequency;
        this.threshold = threshold;
        this.numberOfPoints = numberOfPoints;
        this.frameSpeed = frameSpeed;
        this.multipleFactor = multipleFactor;
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

        double[] re = new double[numberOfShort2];
        double[] im = new double[numberOfShort2];
        double MaxMagnitude = 0;
        double[] magnitudes = new double[numberOfShort2];
        for (int i = 0; i < numberOfShort2; i++) {
            re[i] = ((double) audioData2[i] / 1024.0); // signed   16bit
            im[i] = 0;
        }
        int sampleRate = 128;
        int sampleNumber = numberOfShort2 / sampleRate;
        double[] sampleBuffer = new double[sampleRate];
        for (int k = 0; k < sampleNumber; k++) {
            shift++;
            System.arraycopy(re, k * sampleRate, sampleBuffer, 0, sampleRate);
            float[] new_sig = fft(sampleRate, fs, sampleBuffer);
            for (int i = 0; i < new_sig.length; i += 2) {
                re[i / 2] = new_sig[i];
                im[i / 2] = new_sig[i + 1];
                magnitudes[i / 2] = (float) Math.sqrt(re[i / 2] * re[i / 2]
                        + im[i / 2] * im[i / 2]);
            }
            for (int i = 0; i < (magnitudes.length); i++) {
                if (magnitudes[i] > MaxMagnitude) {
                    MaxMagnitude = magnitudes[i];
                }
                if (MaxMagnitude > maxMagnitude) {
                    maxMagnitude = MaxMagnitude;
                }
            }
            int value;
            int low = 50;
            int high = 400;

            double magnitudeThreshold = 1;
            if (MaxMagnitude > Math.max(maxMagnitude * threshold, magnitudeThreshold)) {
                tempData.add(high);
                value = high;
            } else {
                tempData.add(low);
                value = low;
            }
            process(value == high);

            tempRawData.add(((int) (MaxMagnitude * high + 0.5)) + low);
            //int step = 1;
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
                        ArrayList<Integer> rawData = new ArrayList<>();
                        ArrayList<Integer> transformedData = new ArrayList<>();
                        rawData.addAll(savedRawData);
                        transformedData.addAll(savedData);
                        dataBufferListener.onData(rawData, transformedData);
                    }
                }
            }
            MaxMagnitude = 0;

        }
        if (noCharDetectedCounter > multipleFactor * getDotLength() * 50) {
            resetMaxMagnitude();
        }
    }

    private short[] normalizeBuffer(short[] audioData, int numberOfShort) {
        int numberOfShort2 = numberOfShort;
        if (multipleFactor > 1) {
            numberOfShort2 = multipleFactor * numberOfShort;
        }

        int bufferSize;
        int e = (int) (Math.log(numberOfShort2) / Math.log(2));
        bufferSize = 1 << e;
        short[] buffer = new short[bufferSize];
        for (int i = 0; i < multipleFactor; i++) {
            buffer[i] = audioData[0];
            buffer[bufferSize - multipleFactor - 1 + i] = audioData[numberOfShort - 1];
        }

        int onePassSize = bufferSize / multipleFactor;
        for (int i = 1; i < onePassSize - 1; i++) {
            for (int j = 0; j < multipleFactor; j++) {
                buffer[i * multipleFactor + j]
                        = audioData[(int) ((double) i * (double) numberOfShort / (double) onePassSize)];
            }
        }
        return buffer;
    }

    public double getMaxMagnitude() {
        return maxMagnitude;
    }

    public void resetMaxMagnitude() {
        maxMagnitude = 1f;
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

    private float[] fft(int N, int fs, double[] array) {
        float[] tmpr, tmpi;
        float[] res = new float[N / 2];
        // float[] mod_spec =new float[array.length/2];
        float[] real_mod = new float[N];
        float[] imag_mod = new float[N];
        double[] real = new double[N];
        double[] imag = new double[N];
        double[] mag = new double[N];
        double[] phase = new double[N];
        float[] new_array = new float[N];
        // Zero Pad signal
        for (int i = 0; i < N; i++) {
            if (i < array.length) {
                new_array[i] = (float) array[i];
            } else {
                new_array[i] = 0;
            }
        }

        FFT fft = new FFT(N, fs);
        fft.forward(new_array);
        tmpi = fft.getImaginaryPart();
        tmpr = fft.getRealPart();
        for (int i = 0; i < new_array.length; i++) {
            real[i] = (double) tmpr[i];
            imag[i] = (double) tmpi[i];

            mag[i] = Math.sqrt((real[i] * real[i]) + (imag[i] * imag[i]));
            phase[i] = Math.atan2(imag[i], real[i]);

            /**** Reconstruction ****/
            real_mod[i] = (float) (mag[i] * Math.cos(phase[i]));
            imag_mod[i] = (float) (mag[i] * Math.sin(phase[i]));

        }
        fft.inverse(real_mod, imag_mod, res);
        return res;

    }

    /**
     * Morse code decoder listener
     */
    public interface DataBufferListener {
        void onData(ArrayList<Integer> rawData, ArrayList<Integer> transformedData);
    }

}

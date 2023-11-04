package au.com.guidebee.morsetoolkit.helper;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;


public class MorseEncoder {


    private final double fs = 8000;
    protected AudioTrack audioTrack;
    protected int toneFrequency = 800;
    protected int charDistance = 3;
    //10wpm, 15wpm,20wpm,25wpm,40wpm,60wpm
    protected int[] ditPeriods = new int[]{300, 200, 150, 100, 60, 30, 20};
    protected int wpmIndex = 2;
    private byte[] bufferDit;
    private byte[] bufferDah;
    private byte[] bufferEmpty;
    private PlayStatusListener playStatusListener;

    public MorseEncoder(int wpm) {
        wpmIndex = wpm % ditPeriods.length;
        int ditPeriod = ditPeriods[wpmIndex];
        bufferDit = generateTone(toneFrequency, ditPeriod);
        bufferEmpty = new byte[bufferDit.length];
        for (int i = 0; i < bufferEmpty.length; i++) bufferEmpty[i] = 0;
        bufferDah = generateTone(toneFrequency, ditPeriod * 3);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, (int) fs,
                AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
                bufferDah.length * 2, AudioTrack.MODE_STREAM);
        audioTrack.play();
    }

    public void pause() {
        if (audioTrack != null) {
            audioTrack.pause();
        }
    }

    public void resume() {
        if (audioTrack != null) {
            audioTrack.play();
        }
    }

    public void addPlayListener(PlayStatusListener listener) {
        playStatusListener = listener;
    }

    public synchronized void playMorseCode(String input) {
        try {

            String morseString = generateDitDashString(input);
            if (morseString.length() < 4) {
                morseString += "^^";
            }
            int segmentSize = 0;
            for (int i = 0; i < morseString.length(); i++) {
                char character = morseString.charAt(i);
                switch (character) {
                    case '.':
                    case '^':
                        segmentSize += 1;
                        break;
                    case '-':
                        segmentSize += 3;
                        break;
                    case '<':
                        segmentSize += charDistance;
                        break;
                    case '>':
                        segmentSize += 7;
                        break;
                }
            }

            int blockSize = bufferEmpty.length;

            byte[] buffer = new byte[segmentSize * blockSize];
            segmentSize = 0;

            for (int i = 0; i < morseString.length(); i++) {
                char character = morseString.charAt(i);
                switch (character) {
                    case '.':

                        System.arraycopy(bufferDit, 0, buffer,
                                segmentSize * blockSize, blockSize);
                        segmentSize += 1;
                        break;
                    case '^':

                        System.arraycopy(bufferEmpty, 0, buffer,
                                segmentSize * blockSize, blockSize);
                        segmentSize += 1;
                        break;
                    case '-':

                        System.arraycopy(bufferDah, 0, buffer,
                                (segmentSize) * blockSize, blockSize * 3);
                        segmentSize += 3;
                        break;
                    case '<':
                        for (int j = 0; j < charDistance; j++) {

                            System.arraycopy(bufferEmpty, 0, buffer,
                                    (segmentSize + j) * blockSize, blockSize);
                        }
                        segmentSize += charDistance;
                        break;
                    case '>':
                        for (int j = 0; j < 7; j++) {

                            System.arraycopy(bufferEmpty, 0, buffer,
                                    (segmentSize + j) * blockSize, blockSize);
                        }
                        segmentSize += 7;
                        break;
                }
            }


            audioTrack.setNotificationMarkerPosition(buffer.length / 2);
            audioTrack.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener() {
                @Override
                public void onMarkerReached(AudioTrack track) {
                    if (playStatusListener != null) {
                        playStatusListener.playFinished();
                    }
                }

                @Override
                public void onPeriodicNotification(AudioTrack track) {


                }
            });
            audioTrack.write(buffer, 0, buffer.length);
            audioTrack.play();

        } catch (Exception e) {
            //ignore
            e.printStackTrace();
        }
    }

    private byte[] generateTone(double freqOfTone, double duration) {
        // seconds
        // hz
        double sampleRate = fs;              // a number

        double dnumSamples = duration * sampleRate / 1000.0;
        dnumSamples = Math.ceil(dnumSamples);
        int numSamples = (int) dnumSamples;
        double sample[] = new double[numSamples];
        byte generatedSnd[] = new byte[2 * numSamples];


        for (int i = 0; i < numSamples; ++i) {      // Fill the sample array
            sample[i] = Math.sin(freqOfTone * 2 * Math.PI * i / (sampleRate));
        }

        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalized.
        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        int idx = 0;
        int i;

        int ramp = numSamples / 20;  // Amplitude ramp as a percent of sample count


        for (i = 0; i < ramp; ++i) {  // Ramp amplitude up (to avoid clicks)
            double dVal = sample[i];
            // Ramp up to maximum
            final short val = (short) ((dVal * 32767 * i / ramp));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }


        for (i = i; i < numSamples - ramp; ++i) { // Max amplitude for most of the samples
            double dVal = sample[i];
            // scale to maximum amplitude
            final short val = (short) ((dVal * 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }

        for (i = i; i < numSamples; ++i) {  // Ramp amplitude down
            double dVal = sample[i];
            // Ramp down to zero
            final short val = (short) ((dVal * 32767 * (numSamples - i) / ramp));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }

        return generatedSnd;
    }

    protected String generateDitDashString(String text) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);
            try {
                if (character != ' ') {
                    String morseText = MorseHelper.morseCodeData.get(character);
                    if (morseText != null) {

                        for (int j = 0; j < morseText.length(); j++) {
                            char ditOrDah = morseText.charAt(j);
                            stringBuilder.append(ditOrDah);

                            stringBuilder.append('^');
                        }

                        stringBuilder.append('<');

                    }

                } else {

                    stringBuilder.append('>');

                }
            } catch (Exception e) {
                //ignore
                e.printStackTrace();
            }
        }
        return stringBuilder.toString();
    }

    public void release() {
        if (audioTrack != null) {
            audioTrack.flush();
            audioTrack.release();
            audioTrack = null;
        }
    }


    public interface PlayStatusListener {
        void playFinished();
    }
}

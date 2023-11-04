package au.com.guidebee.morsetoolkit.activity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.ImageButton;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import au.com.guidebee.morsetoolkit.ConfigInfo;
import au.com.guidebee.morsetoolkit.decoder.KeyboardMorseCodeDecoder;
import au.com.guidebee.morsetoolkit.decoder.MorseCodePatternMatch;
import au.com.guidebee.morsetoolkit.helper.MorseEncoder;


public class MorseActivity extends DrawerActivity implements
        MorseCodePatternMatch.MorseCodeListener, MorseEncoder.PlayStatusListener {

    private final static int LETTER_LIMIT = 5;
    protected final KeyboardMorseCodeDecoder keyboardMorseCodeDecoder = new KeyboardMorseCodeDecoder();
    protected final ExecutorService executor = Executors.newFixedThreadPool(5);
    protected int correctAnswerColor = 0xff669900;
    protected int wrongAnswerColor = 0xffcc0000;
    protected MediaPlayer mediaPlayerPoint = null;
    protected Character lastChar = '^';
    protected MorseEncoder morseEncoder = null;
    protected int totalSegments = 1;
    protected volatile int currentSegment = 0;
    protected volatile boolean stopThread = true;
    private MediaPlayer mediaPlayerDah = null;
    private MediaPlayer mediaPlayerDit = null;
    private boolean isDown = false;
    private long lastTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        switch (ConfigInfo.morseInputSpeed) {
            case 4:
                keyboardMorseCodeDecoder.setSampleSpeed(15);
                break;
            case 2:
                keyboardMorseCodeDecoder.setSampleSpeed(25);
                break;
            case 1:
                keyboardMorseCodeDecoder.setSampleSpeed(35);
                break;
        }
        keyboardMorseCodeDecoder.startTimer();
        keyboardMorseCodeDecoder.addListener(this);
        morseEncoder = new MorseEncoder(ConfigInfo.morseReceiveWPM);
        morseEncoder.addPlayListener(this);
        ImageButton imageButtonMorse = (ImageButton) findViewById(R.id.imageButtonMorse);
        if (imageButtonMorse != null) {
            imageButtonMorse.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    isDown = true;
                    lastTime = System.currentTimeMillis();
                    if (ConfigInfo.playAudio) {
                        if (mediaPlayerDit.isPlaying()) {
                            mediaPlayerDit.pause();
                            mediaPlayerDit.seekTo(0);
                        }
                        if (mediaPlayerDah.isPlaying()) {
                            mediaPlayerDah.pause();
                            mediaPlayerDah.seekTo(0);
                        }
                    }

                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    long currentTime = System.currentTimeMillis();
                    long diff = currentTime - lastTime;
                    if (diff < 250) {
                        if (ConfigInfo.playAudio) {
                            mediaPlayerDit.start();
                        }
                        keyboardMorseCodeDecoder.processKey(false);
                    } else if (diff < 1000) {
                        if (ConfigInfo.playAudio) {
                            mediaPlayerDah.start();
                        }
                        keyboardMorseCodeDecoder.processKey(true);
                    }
                    isDown = false;
                }

                keyboardMorseCodeDecoder.setOnOff(isDown);
                return false;
            });
        }

        mediaPlayerPoint = MediaPlayer.create(this, R.raw.point);
        mediaPlayerDah = MediaPlayer.create(this, R.raw.dah);
        mediaPlayerDit = MediaPlayer.create(this, R.raw.dit);
        mediaPlayerDah.setLooping(false);
        mediaPlayerDit.setLooping(false);
        mediaPlayerPoint.setLooping(false);

        float volume_level = (ConfigInfo.audioVolume + 1) / 100.0f;
        mediaPlayerDah.setVolume(volume_level, volume_level);
        mediaPlayerDit.setVolume(volume_level, volume_level);
        mediaPlayerPoint.setVolume(volume_level, volume_level);
    }


    @Override
    public void onEmit(Character character) {
        lastChar = character;
    }

    @Override
    public void onCharStart() {

    }

    @Override
    public void onCharEnd(String dotOrDash, int length) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
        keyboardMorseCodeDecoder.cancelTimer();
        if (mediaPlayerDah != null) {
            mediaPlayerDah.stop();
            mediaPlayerDah.release();
            mediaPlayerDah = null;
        }
        if (mediaPlayerDit != null) {
            mediaPlayerDit.stop();
            mediaPlayerDit.release();
            mediaPlayerDit = null;
        }
        if (mediaPlayerPoint != null) {
            mediaPlayerPoint.stop();
            mediaPlayerPoint.release();
            mediaPlayerPoint = null;
        }
        morseEncoder.release();
    }

    @Override
    public void playFinished() {

    }

    protected void SoundFinished() {

    }

    @Override
    public void onResume() {
        stopThread = true;
        super.onResume();
        morseEncoder.resume();
    }

    @Override
    public void onPause() {
        stopThread = true;
        morseEncoder.pause();
        super.onPause();
    }

    protected class PlayAudioThread implements Runnable {
        private final String word;

        PlayAudioThread(String world) {
            word = world;
            stopThread = false;
        }

        @Override
        public void run() {
            int count = word.length() / LETTER_LIMIT;
            int reminder = word.length() - count * LETTER_LIMIT;
            if (reminder != 0) {
                totalSegments = count;
            } else {
                totalSegments = count - 1;
            }
            currentSegment = 0;
            int index = 0;
            while (index < count && !stopThread) {
                String strSegment = word.substring(index * LETTER_LIMIT,
                        (index + 1) * LETTER_LIMIT);
                index++;
                morseEncoder.playMorseCode(strSegment);
            }

            if (reminder > 0 && !stopThread) {
                currentSegment++;
                String strSegment = word.substring(word.length() - reminder,
                        word.length());
                morseEncoder.playMorseCode(strSegment);

            }
            stopThread = true;
            SoundFinished();
        }
    }
}


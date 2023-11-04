package au.com.guidebee.morsetoolkit.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import au.com.guidebee.morsetoolkit.ConfigInfo;
import au.com.guidebee.morsetoolkit.decoder.AudioMorseCodeDecoder;
import au.com.guidebee.morsetoolkit.decoder.MorseCodePatternMatch;
import au.com.guidebee.morsetoolkit.helper.UIHelper;


public class ListenActivity extends DrawerActivity implements
        MorseCodePatternMatch.MorseCodeListener,
        AudioMorseCodeDecoder.DataBufferListener {

    private final static String STATE_POWER_ON = "State_Power_On";
    private final static String STATE_ANALOG_ON = "State_analog_On";
    private final static String STATE_X_POS = "State_X_POS";
    private final static String STATE_Y_POS = "State_Y_POS";
    private final static String STATE_TEXT_OUTPUT = "State_Text_Output";
    private final static int MY_PERMISSIONS_REQUEST_AUDIO_RECORDER = 1;
    private final static String AUDIO_RECORD_PERMISSION = Manifest.permission.RECORD_AUDIO;
    private final int backgroundColor = Color.rgb(54, 161, 59);
    private final RectF canvasRect = new RectF();
    private final ExecutorService executor = Executors.newFixedThreadPool(5);
    private final int fs = ConfigInfo.sampleRate;
    private final int minBufferSize = AudioRecord.getMinBufferSize(fs,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT);
    private final short[] audioData = new short[minBufferSize];
    private Switch switchPower;
    private Switch switchAnalog;
    private SeekBar seekBarYPos;
    private SeekBar seekBarXPos;
    private Button buttonPlay;
    private ImageView imageViewWave;
    private TextView textViewOutput;
    private ScrollView scrollViewOutput;
    private ArrayList<Integer> savedData = new ArrayList<>();
    private ArrayList<Integer> savedRawData = new ArrayList<>();
    private Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint paintRaw = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Bitmap myBitmap;
    private Canvas canvas;
    private int multipleFactor = 1;
    private AudioMorseCodeDecoder morseCodePatternMatch;
    private int finalHeight, finalWidth;
    private int step = 1;
    private double maxMagnitude = 1;
    private float currentWPM;
    private MediaPlayer mediaPlayer = null;
    private int screenDp = 1;
    private int offsetY = 20;
    private volatile DecodeAudioThread recordThread = null;
    private AudioRecord audioRecord = null;
    private Character lastChar = ' ';

    @Override
    protected void setActivityTitle() {
        actionBar.setTitle(R.string.decoder_title);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(STATE_POWER_ON, switchPower.isChecked());
        savedInstanceState.putBoolean(STATE_ANALOG_ON, switchAnalog.isChecked());
        savedInstanceState.putInt(STATE_X_POS, seekBarXPos.getProgress());
        savedInstanceState.putInt(STATE_Y_POS, seekBarYPos.getProgress());
        savedInstanceState.putString(STATE_TEXT_OUTPUT, textViewOutput.getText().toString());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_listen);
        super.onCreate(savedInstanceState);

        screenDp = (int) UIHelper.getScreenDp(this);
        switchPower = (Switch) findViewById(R.id.switchPower);
        switchAnalog = (Switch) findViewById(R.id.switchAnalog);
        seekBarXPos = (SeekBar) findViewById(R.id.seekBarXPos);
        seekBarYPos = (SeekBar) findViewById(R.id.seekBarYPos);
        Button imageButtonReset = (Button) findViewById(R.id.imageButtonReset);
        Button buttonClear = (Button) findViewById(R.id.buttonClear);
        buttonPlay = (Button) findViewById(R.id.buttonPlay);
        imageViewWave = (ImageView) findViewById(R.id.imageViewWave);
        textViewOutput = (TextView) findViewById(R.id.textViewOutput);
        scrollViewOutput = (ScrollView) findViewById(R.id.scrollViewOutput);
        if (savedInstanceState != null) {
            switchPower.setChecked(savedInstanceState.getBoolean(STATE_POWER_ON));
            switchAnalog.setChecked(savedInstanceState.getBoolean(STATE_ANALOG_ON));
            seekBarXPos.setProgress(savedInstanceState.getInt(STATE_X_POS));
            seekBarXPos.setProgress(savedInstanceState.getInt(STATE_Y_POS));
            textViewOutput.setText(savedInstanceState.getString(STATE_TEXT_OUTPUT));
        }
        ViewTreeObserver vto = imageViewWave.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                imageViewWave.getViewTreeObserver().removeOnPreDrawListener(this);
                finalHeight = imageViewWave.getMeasuredHeight();
                finalWidth = imageViewWave.getMeasuredWidth();
                if (finalHeight == 0) finalHeight = 400;
                if (finalWidth == 0) finalWidth = 800;
                canvasRect.top = 0;
                canvasRect.left = 0;
                canvasRect.right = finalWidth;
                canvasRect.bottom = finalHeight;
                myBitmap = Bitmap.createBitmap(finalWidth, finalHeight,
                        Bitmap.Config.RGB_565);
                canvas = new Canvas(myBitmap);
                morseCodePatternMatch = new AudioMorseCodeDecoder(23, finalWidth,
                        50 * multipleFactor, multipleFactor);
                morseCodePatternMatch.addListener(ListenActivity.this);
                morseCodePatternMatch.addDataBufferListener(ListenActivity.this);
                return true;
            }
        });

        switchPower.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                recordThread = new DecodeAudioThread();
                recordThread.resume();
                executor.execute(recordThread);
            } else {
                clearScreen();
                if (recordThread != null) {
                    recordThread.pause();
                    recordThread = null;
                }
            }
        });

        buttonPlay.setOnClickListener(v -> {
            if (mediaPlayer == null) {
                if (morseCodePatternMatch != null) {
                    morseCodePatternMatch.resetMaxMagnitude();
                    morseCodePatternMatch.changeDotLimit(23);
                }
                buttonPlay.setText(R.string.stop_sample);
                mediaPlayer = MediaPlayer.create(ListenActivity.this, R.raw.morse);
                if (switchPower.isEnabled()) {
                    switchPower.setChecked(true);
                }
                mediaPlayer.setOnCompletionListener(mp -> {
                    buttonPlay.setText(R.string.play_sample);
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                });
                mediaPlayer.start();
            } else {
                buttonPlay.setText(R.string.play_sample);
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        });

        if (imageButtonReset != null) {
            imageButtonReset.setOnClickListener(v -> {
                if (morseCodePatternMatch != null) {
                    morseCodePatternMatch.resetMaxMagnitude();
                    morseCodePatternMatch.changeDotLimit(23);
                }
            });
        }

        if (buttonClear != null) {
            buttonClear.setOnClickListener(v -> textViewOutput.setText(""));
        }
        if (seekBarXPos != null) {
            seekBarXPos.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    step = seekBar.getProgress() + 1;

                }
            });
        }
        paintRaw.setColor(Color.WHITE);
        paint.setColor(Color.YELLOW);
        paintText.setColor(Color.YELLOW);
        backgroundPaint.setColor(backgroundColor);
        paintText.setTextSize(48);
        offsetY = 10 * screenDp;
        try {
            if (!ConfigInfo.hasRequestedAudioPermission) {
                ConfigInfo.hasRequestedAudioPermission = true;
                requestAudioPermission();
            } else {
                if (ContextCompat.checkSelfPermission(this,
                        AUDIO_RECORD_PERMISSION)
                        == PackageManager.PERMISSION_GRANTED) {
                    createAudioRecorder();
                }
            }
        } catch (Exception e) {
            //ignore exception.
            e.printStackTrace();
        }

    }

    private void requestAudioPermission() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                AUDIO_RECORD_PERMISSION)
                != PackageManager.PERMISSION_GRANTED) {

            // No explanation needed, we can request the permission.

            ActivityCompat.requestPermissions(this,
                    new String[]{AUDIO_RECORD_PERMISSION},
                    MY_PERMISSIONS_REQUEST_AUDIO_RECORDER);
        } else {
            switchPower.post(this::createAudioRecorder);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_AUDIO_RECORDER: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    switchPower.post(() -> {
                        switchPower.setEnabled(true);
                        createAudioRecorder();
                    });

                } else {
                    switchPower.post(() -> {
                        UIHelper.showSnackBar(switchPower,
                                "Morse code decoder cannot work without proper permission! ");
                        switchPower.setEnabled(false);
                    });

                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void createAudioRecorder() {
        try {

            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    fs,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBufferSize * 2);
            if (audioRecord != null) {
                if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
                    audioRecord.startRecording();
                    switchPower.setEnabled(true);
                } else {
                    switchPower.setEnabled(false);
                    audioRecord.release();
                    UIHelper.showSnackBar(switchPower,
                            getString(R.string.error_get_audio_recorder));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {

        if (mediaPlayer != null) {
            buttonPlay.setText(R.string.play_sample);
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (recordThread != null) {
            recordThread.pause();
            recordThread = null;
        }
        switchPower.setChecked(false);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (audioRecord != null) {
            audioRecord.release();
        }
        super.onDestroy();
    }

    public void clearScreen() {
        if (myBitmap != null) {
            canvas.drawColor(0xffaaaaaa);
            imageViewWave.setImageBitmap(myBitmap);
        }
    }

    public void drawPoints(int step) {
        if (switchPower.isChecked()) {
            if (myBitmap != null) {
                int realHeight = finalHeight - 10 * screenDp;
                int high = 400;
                canvas.drawColor(0xffaaaaaa);
                canvas.drawRoundRect(canvasRect, 7 * screenDp, 7 * screenDp,
                        backgroundPaint);
                if (switchPower.isChecked()) {
                    float scaleY = (seekBarYPos.getProgress() + 1) / 5.0f;
                    for (int i = 1; i < savedData.size(); i++) {

                        if (switchAnalog.isChecked()) {
                            canvas.drawLine((i - 1) * step,
                                    (float) (realHeight - (savedRawData.get(i - 1)
                                            / (maxMagnitude * high + 0.5)) * realHeight * scaleY)
                                            - offsetY,
                                    i * step,
                                    (float) (realHeight - savedRawData.get(i)
                                            / (maxMagnitude * high + 0.5) * realHeight * scaleY)
                                            - offsetY,
                                    paintRaw);
                        }

                        canvas.drawLine((i - 1) * step,
                                realHeight - savedData.get(i - 1) / high * realHeight * scaleY
                                        - offsetY,
                                i * step,
                                realHeight - savedData.get(i) / high * realHeight * scaleY
                                        - offsetY,
                                paint);
                    }
                    canvas.drawText("WPM:" + currentWPM, 50 * screenDp, 50 * screenDp, paintText);
                }
                imageViewWave.setImageBitmap(myBitmap);
            }
        } else {
            clearScreen();
        }
    }

    @Override
    public void onEmit(final Character character) {
        textViewOutput.post(() -> {
            if (!(character == ' ' && lastChar == ' ') && character != '^') {
                textViewOutput.append(character.toString());
                if (textViewOutput.getText().length() > 6000) {
                    textViewOutput.setText("");
                }
                scrollViewOutput.fullScroll(View.FOCUS_DOWN);
            }
            lastChar = character;
        });

    }

    @Override
    public void onCharStart() {

    }

    @Override
    public void onCharEnd(String dotOrDash, int length) {
        if (dotOrDash.compareToIgnoreCase(".") == 0) {
            currentWPM = ((int) ((fs / (float) length) + 0.5)) / 100;
        }
    }

    @Override
    public void onData(ArrayList<Integer> rawData, ArrayList<Integer> transformedData) {
        savedData = transformedData;
        savedRawData = rawData;
        maxMagnitude = morseCodePatternMatch.getMaxMagnitude();
        imageViewWave.post(() -> drawPoints(step));
    }

    class DecodeAudioThread implements Runnable {
        private volatile boolean stopThread = false;

        @Override
        public void run() {
            startRecord();
        }

        public void resume() {
            stopThread = false;
        }

        public void pause() {
            stopThread = true;
        }

        private void startRecord() {
            try {
                while (!stopThread) {
                    if (audioRecord != null) {
                        int numberOfShort = audioRecord.read(audioData, 0, minBufferSize);
                        if (numberOfShort > 0) {
                            morseCodePatternMatch.processAudioBuffer(audioData, numberOfShort);
                        } else {
                            audioRecord.stop();
                            audioRecord.startRecording();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

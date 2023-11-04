package au.com.guidebee.morsetoolkit.activity;

import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.SeekBar;

import au.com.guidebee.morsetoolkit.ConfigInfo;

public class OptionActivity extends DrawerActivity {

    private RadioButton radioButtonTransmitLetter;
    private CheckBox checkBoxTransmitLetter;
    private CheckBox checkBoxTransmitNumber;
    private CheckBox checkBoxTransmitPunctuation;

    private RadioButton radioButtonTransmitWord;
    private RadioButton radioButtonTransmitFreeText;

    private RadioButton radioButtonReceiveLetter;
    private CheckBox checkBoxReceiveLetter;
    private CheckBox checkBoxReceiveNumber;
    private CheckBox checkBoxReceivePunctuation;

    private RadioButton radioButtonReceiveWord;
    private RadioButton radioButtonReceiveFreeText;

    private RadioButton radioButton88200;
    private RadioButton radioButton44100;
    private RadioButton radioButton22050;
    private RadioButton radioButton11025;


    private RadioButton radioButtonSpeedSlow;
    private RadioButton radioButtonSpeedNormal;
    private RadioButton radioButtonSpeedFast;

    private CheckBox checkBoxLetterOnFront;
    private CheckBox checkBoxPlaySound;
    private SeekBar seekBarVolume;

    private RadioButton radioButtonOrder;
    private RadioButton radioButtonRandom;

    private RadioButton radioButton10;
    private RadioButton radioButton15;
    private RadioButton radioButton20;
    private RadioButton radioButton25;
    private RadioButton radioButton40;

    @Override
    protected void setActivityTitle() {
        actionBar.setTitle(R.string.options_title);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setContentView(R.layout.activity_option);
        super.onCreate(savedInstanceState);
        radioButtonTransmitLetter = (RadioButton) findViewById(R.id.radioButtonTransmitLetter);
        checkBoxTransmitLetter = (CheckBox) findViewById(R.id.checkBoxTransmitLetter);
        checkBoxTransmitNumber = (CheckBox) findViewById(R.id.checkBoxTransmitNumber);
        checkBoxTransmitPunctuation = (CheckBox) findViewById(R.id.checkBoxTransmitPunctuation);
        radioButtonTransmitWord = (RadioButton) findViewById(R.id.radioButtonTransmitWord);
        radioButtonTransmitFreeText = (RadioButton) findViewById(R.id.radioButtonTransmitFreeText);

        radioButtonReceiveLetter = (RadioButton) findViewById(R.id.radioButtonReceiveLetter);
        checkBoxReceiveLetter = (CheckBox) findViewById(R.id.checkBoxReceiveLetter);
        checkBoxReceiveNumber = (CheckBox) findViewById(R.id.checkBoxReceiveNumber);
        checkBoxReceivePunctuation = (CheckBox) findViewById(R.id.checkBoxReceivePunctuation);
        radioButtonReceiveWord = (RadioButton) findViewById(R.id.radioButtonReceiveWord);
        radioButtonReceiveFreeText = (RadioButton) findViewById(R.id.radioButtonReceiveFreeText);

        radioButton10 = (RadioButton) findViewById(R.id.radioButton10);
        radioButton15 = (RadioButton) findViewById(R.id.radioButton15);
        radioButton20 = (RadioButton) findViewById(R.id.radioButton20);
        radioButton25 = (RadioButton) findViewById(R.id.radioButton25);
        radioButton40 = (RadioButton) findViewById(R.id.radioButton40);

        radioButton88200 = (RadioButton) findViewById(R.id.radioButton88200);
        radioButton44100 = (RadioButton) findViewById(R.id.radioButton44100);
        radioButton22050 = (RadioButton) findViewById(R.id.radioButton22050);
        radioButton11025 = (RadioButton) findViewById(R.id.radioButton11025);

        radioButtonSpeedSlow = (RadioButton) findViewById(R.id.radioButtonSpeedSlow);
        radioButtonSpeedNormal = (RadioButton) findViewById(R.id.radioButtonSpeedNormal);
        radioButtonSpeedFast = (RadioButton) findViewById(R.id.radioButtonSpeedFast);

        checkBoxPlaySound = (CheckBox) findViewById(R.id.checkBoxPlaySound);
        checkBoxLetterOnFront = (CheckBox) findViewById(R.id.checkBoxLetterOnFront);

        radioButtonOrder = (RadioButton) findViewById(R.id.radioButtonOrder);
        radioButtonRandom = (RadioButton) findViewById(R.id.radioButtonRandom);

        seekBarVolume = (SeekBar) findViewById(R.id.seekBarVolume);

        readConfiguration();

        radioButtonTransmitLetter.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                radioButtonTransmitWord.setChecked(false);
                radioButtonTransmitFreeText.setChecked(false);
            }
            writeConfiguration();
        });

        radioButtonTransmitWord.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                radioButtonTransmitLetter.setChecked(false);
                radioButtonTransmitFreeText.setChecked(false);
            }
            writeConfiguration();
        });

        radioButtonTransmitFreeText.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                radioButtonTransmitLetter.setChecked(false);
                radioButtonTransmitWord.setChecked(false);
            }
            writeConfiguration();
        });

        radioButtonReceiveLetter.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                radioButtonReceiveWord.setChecked(false);
                radioButtonReceiveFreeText.setChecked(false);
            }
            writeConfiguration();
        });

        radioButtonReceiveWord.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                radioButtonReceiveLetter.setChecked(false);
                radioButtonReceiveFreeText.setChecked(false);
            }
            writeConfiguration();
        });

        radioButtonReceiveFreeText.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                radioButtonReceiveLetter.setChecked(false);
                radioButtonReceiveWord.setChecked(false);
            }
            writeConfiguration();
        });

        radioButton10.setOnCheckedChangeListener((buttonView, isChecked)
                -> writeConfiguration());
        radioButton15.setOnCheckedChangeListener((buttonView, isChecked)
                -> writeConfiguration());
        radioButton20.setOnCheckedChangeListener((buttonView, isChecked)
                -> writeConfiguration());
        radioButton25.setOnCheckedChangeListener((buttonView, isChecked)
                -> writeConfiguration());
        radioButton40.setOnCheckedChangeListener((buttonView, isChecked)
                -> writeConfiguration());

        radioButton88200.setOnCheckedChangeListener((buttonView, isChecked)
                -> writeConfiguration());
        radioButton44100.setOnCheckedChangeListener((buttonView, isChecked)
                -> writeConfiguration());
        radioButton22050.setOnCheckedChangeListener((buttonView, isChecked)
                -> writeConfiguration());
        radioButton11025.setOnCheckedChangeListener((buttonView, isChecked)
                -> writeConfiguration());

        radioButtonSpeedSlow.setOnCheckedChangeListener((buttonView, isChecked)
                -> writeConfiguration());
        radioButtonSpeedNormal.setOnCheckedChangeListener((buttonView, isChecked)
                -> writeConfiguration());
        radioButtonSpeedFast.setOnCheckedChangeListener((buttonView, isChecked)
                -> writeConfiguration());

        checkBoxTransmitLetter.setOnCheckedChangeListener((buttonView, isChecked)
                -> writeConfiguration());
        checkBoxTransmitNumber.setOnCheckedChangeListener((buttonView, isChecked)
                -> writeConfiguration());
        checkBoxTransmitPunctuation.setOnCheckedChangeListener((buttonView, isChecked)
                -> writeConfiguration());
        checkBoxReceiveLetter.setOnCheckedChangeListener((buttonView, isChecked)
                -> writeConfiguration());
        checkBoxReceiveNumber.setOnCheckedChangeListener((buttonView, isChecked)
                -> writeConfiguration());
        checkBoxReceivePunctuation.setOnCheckedChangeListener((buttonView, isChecked)
                -> writeConfiguration());

        checkBoxPlaySound.setOnCheckedChangeListener((buttonView, isChecked)
                -> writeConfiguration());
        checkBoxLetterOnFront.setOnCheckedChangeListener((buttonView, isChecked)
                -> writeConfiguration());

        radioButtonOrder.setOnCheckedChangeListener((buttonView, isChecked)
                -> writeConfiguration());
        radioButtonRandom.setOnCheckedChangeListener((buttonView, isChecked)
                -> writeConfiguration());

        seekBarVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                writeConfiguration();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    private void writeConfiguration() {
        if (radioButtonTransmitLetter.isChecked()) ConfigInfo.transmitType = 1;
        if (radioButtonTransmitWord.isChecked()) ConfigInfo.transmitType = 2;
        if (radioButtonTransmitFreeText.isChecked()) ConfigInfo.transmitType = 4;

        if (radioButtonReceiveLetter.isChecked()) ConfigInfo.receiveType = 1;
        if (radioButtonReceiveWord.isChecked()) ConfigInfo.receiveType = 2;
        if (radioButtonReceiveFreeText.isChecked()) ConfigInfo.receiveType = 4;

        ConfigInfo.transmitLetterType = 0;

        if (checkBoxTransmitLetter.isChecked())
            ConfigInfo.transmitLetterType |= ConfigInfo.TYPE_LETTER_LETTER;
        if (checkBoxTransmitNumber.isChecked())
            ConfigInfo.transmitLetterType |= ConfigInfo.TYPE_LETTER_NUMBER;
        if (checkBoxTransmitPunctuation.isChecked())
            ConfigInfo.transmitLetterType |= ConfigInfo.TYPE_LETTER_PUNCTUATION;

        ConfigInfo.receiveLetterType = 0;
        if (checkBoxReceiveLetter.isChecked())
            ConfigInfo.receiveLetterType |= ConfigInfo.TYPE_LETTER_LETTER;
        if (checkBoxReceiveNumber.isChecked())
            ConfigInfo.receiveLetterType |= ConfigInfo.TYPE_LETTER_NUMBER;
        if (checkBoxReceivePunctuation.isChecked())
            ConfigInfo.receiveLetterType |= ConfigInfo.TYPE_LETTER_PUNCTUATION;

        if (radioButton10.isChecked()) ConfigInfo.morseReceiveWPM = 0;
        if (radioButton15.isChecked()) ConfigInfo.morseReceiveWPM = 1;
        if (radioButton20.isChecked()) ConfigInfo.morseReceiveWPM = 2;
        if (radioButton25.isChecked()) ConfigInfo.morseReceiveWPM = 3;
        if (radioButton40.isChecked()) ConfigInfo.morseReceiveWPM = 4;

        if (radioButton88200.isChecked()) ConfigInfo.sampleRate = 88200;
        if (radioButton44100.isChecked()) ConfigInfo.sampleRate = 44100;
        if (radioButton11025.isChecked()) ConfigInfo.sampleRate = 11025;
        if (radioButton22050.isChecked()) ConfigInfo.sampleRate = 22050;
        if (ConfigInfo.sampleRate > MorseApplication.getValidSampleRates()) {
            ConfigInfo.sampleRate = MorseApplication.getValidSampleRates();
        }

        ConfigInfo.playAudio = checkBoxPlaySound.isChecked();
        ConfigInfo.letterOnFront = checkBoxLetterOnFront.isChecked();
        ConfigInfo.audioVolume = seekBarVolume.getProgress();

        ConfigInfo.flashCardType = radioButtonOrder.isChecked();

        if (radioButtonSpeedFast.isChecked()) ConfigInfo.morseInputSpeed = 4;
        if (radioButtonSpeedSlow.isChecked()) ConfigInfo.morseInputSpeed = 1;
        if (radioButtonSpeedNormal.isChecked()) ConfigInfo.morseInputSpeed = 2;

        ConfigInfo.saveConfiguration(this);
    }

    private void readConfiguration() {
        ConfigInfo.loadConfiguration(this);
        radioButtonTransmitLetter.setChecked(ConfigInfo.transmitType == 1);
        radioButtonTransmitWord.setChecked(ConfigInfo.transmitType == 2);
        radioButtonTransmitFreeText.setChecked(ConfigInfo.transmitType == 4);

        radioButtonReceiveLetter.setChecked(ConfigInfo.receiveType == 1);
        radioButtonReceiveWord.setChecked(ConfigInfo.receiveType == 2);
        radioButtonReceiveFreeText.setChecked(ConfigInfo.receiveType == 4);

        checkBoxTransmitLetter.setChecked((ConfigInfo.transmitLetterType
                & ConfigInfo.TYPE_LETTER_LETTER) > 0);
        checkBoxTransmitNumber.setChecked((ConfigInfo.transmitLetterType
                & ConfigInfo.TYPE_LETTER_NUMBER) > 0);
        checkBoxTransmitPunctuation.setChecked((ConfigInfo.transmitLetterType
                & ConfigInfo.TYPE_LETTER_PUNCTUATION) > 0);

        checkBoxReceiveLetter.setChecked((ConfigInfo.receiveLetterType
                & ConfigInfo.TYPE_LETTER_LETTER) > 0);
        checkBoxReceiveNumber.setChecked((ConfigInfo.receiveLetterType
                & ConfigInfo.TYPE_LETTER_NUMBER) > 0);
        checkBoxReceivePunctuation.setChecked((ConfigInfo.receiveLetterType
                & ConfigInfo.TYPE_LETTER_PUNCTUATION) > 0);

        radioButton10.setChecked(ConfigInfo.morseReceiveWPM == 0);
        radioButton15.setChecked(ConfigInfo.morseReceiveWPM == 1);
        radioButton20.setChecked(ConfigInfo.morseReceiveWPM == 2);
        radioButton25.setChecked(ConfigInfo.morseReceiveWPM == 3);
        radioButton40.setChecked(ConfigInfo.morseReceiveWPM == 4);

        radioButton11025.setChecked(ConfigInfo.sampleRate == 11025);
        radioButton22050.setChecked(ConfigInfo.sampleRate == 22050);
        radioButton44100.setChecked(ConfigInfo.sampleRate == 44100);
        radioButton88200.setChecked(ConfigInfo.sampleRate == 88200);

        checkBoxPlaySound.setChecked(ConfigInfo.playAudio);
        checkBoxLetterOnFront.setChecked(ConfigInfo.letterOnFront);
        seekBarVolume.setProgress(ConfigInfo.audioVolume);

        radioButtonOrder.setChecked(ConfigInfo.flashCardType);
        radioButtonRandom.setChecked(!ConfigInfo.flashCardType);

        radioButtonSpeedFast.setChecked(ConfigInfo.morseInputSpeed == 4);
        radioButtonSpeedSlow.setChecked(ConfigInfo.morseInputSpeed == 1);
        radioButtonSpeedNormal.setChecked(ConfigInfo.morseInputSpeed == 2);

    }
}

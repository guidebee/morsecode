package au.com.guidebee.morsetoolkit.activity;

import android.os.Bundle;
import android.view.View;

import au.com.guidebee.morsetoolkit.ConfigInfo;
import au.com.guidebee.morsetoolkit.helper.MorseHelper;


public class ReceiveLetterActivity extends TransmitLetterActivity {

    @Override
    protected void setActivityTitle() {
        actionBar.setTitle(R.string.receive_letter_title);
    }

    @Override
    protected void onCorrectAnswer() {
        imageButtonPlay.setVisibility(View.INVISIBLE);
        super.onCorrectAnswer();
    }

    @Override
    protected void onErrorAnswer() {
        imageButtonPlay.setVisibility(View.INVISIBLE);
        super.onErrorAnswer();
    }

    @Override
    protected void initTestLetters() {
        allTestLetters.clear();
        allTestLetters.addAll(MorseHelper.initTestLetters(ConfigInfo.receiveLetterType));
    }

    @Override
    protected void randomLetter(Bundle savedInstanceState) {
        super.randomLetter(savedInstanceState);
        textViewLetter.setTextColor(0x00ffffff);
        executor.execute(new PlayAudioThread(String.valueOf(currentLetter).toLowerCase()));
        imageButtonPlay.setVisibility(View.VISIBLE);
    }

}

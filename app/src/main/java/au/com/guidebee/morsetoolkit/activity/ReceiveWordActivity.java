package au.com.guidebee.morsetoolkit.activity;

import android.os.Bundle;
import android.view.View;

public class ReceiveWordActivity extends TransmitWordActivity {

    @Override
    protected void setActivityTitle() {
        actionBar.setTitle(R.string.receive_word_title);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (imageButtonPlay != null) {
            imageButtonPlay.setOnClickListener(v
                    -> executor.execute(new PlayAudioThread(currentWord.toLowerCase())));

        }
    }

    @Override
    protected void randomWord(Bundle savedInstanceState) {
        super.randomWord(savedInstanceState);
        textViewWord.setTextColor(0x00ffffff);
        executor.execute(new PlayAudioThread(currentWord.toLowerCase()));
        imageButtonPlay.setVisibility(View.VISIBLE);
    }
}

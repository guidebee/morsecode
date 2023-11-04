package au.com.guidebee.morsetoolkit.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;


public class ReceiveFreePadActivity extends MorseActivity implements Runnable {

    private EditText textViewOutput;
    private Button buttonPlay;

    @Override
    protected void setActivityTitle() {
        actionBar.setTitle(R.string.receive_free_pad_title);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_receive_freepad);
        super.onCreate(savedInstanceState);
        textViewOutput = (EditText) findViewById(R.id.textViewOutput);
        Button buttonClear = (Button) findViewById(R.id.buttonClear);
        if (buttonClear != null) {
            buttonClear.setOnClickListener(v -> textViewOutput.setText(""));
        }
        buttonPlay = (Button) findViewById(R.id.buttonPlay);

        if (buttonPlay != null) {
            buttonPlay.setOnClickListener(v -> {
                String inputText = textViewOutput.getText().toString().toLowerCase();
                if (inputText.length() > 0) {
                    stopThread = !stopThread;
                    if (stopThread) {
                        setButtonPlay();
                    } else {
                        buttonPlay.setText(R.string.stop);
                        currentSegment = 0;
                        executor.execute(new PlayAudioThread(inputText));
                    }

                }

            });
        }

    }


    @Override
    public void onPause() {
        setButtonPlay();
        super.onPause();
    }

    @Override
    protected void SoundFinished() {
        buttonPlay.post(() -> buttonPlay.setText(R.string.play));
    }

    private void setButtonPlay() {
        buttonPlay.setText(R.string.play);

    }

    @Override
    public void run() {
        Log.i("PlayInRun", currentSegment + "/" + totalSegments);
        if (currentSegment >= totalSegments) {
            buttonPlay.post(() -> buttonPlay.setText(R.string.play));
        }
    }
}

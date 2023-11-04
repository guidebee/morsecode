package au.com.guidebee.morsetoolkit.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;


public class TransmitFreePadActivity extends MorseActivity {

    private final static String STATE_TEXT_OUTPUT = "State_Text_Output";
    private TextView textViewOutput;
    private ScrollView scrollViewOutput;

    @Override
    public void onEmit(Character character) {
        textViewOutput.post(() -> {
            if (!(character == ' ' && lastChar == ' ')) {

                if (character != '^') {
                    textViewOutput.append(character.toString().toUpperCase());
                    if (textViewOutput.getText().length() > 6000) {
                        textViewOutput.setText("");
                    }
                    scrollViewOutput.fullScroll(View.FOCUS_DOWN);
                }
            }
            lastChar = character;
        });
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(STATE_TEXT_OUTPUT, textViewOutput.getText().toString());
        super.onSaveInstanceState(savedInstanceState);
    }


    @Override
    protected void setActivityTitle() {
        actionBar.setTitle(R.string.transmit_free_pad_title);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_transmit_freepad);
        super.onCreate(savedInstanceState);
        textViewOutput = (TextView) findViewById(R.id.textViewOutput);
        scrollViewOutput = (ScrollView) findViewById(R.id.scrollViewOutput);
        Button buttonClear = (Button) findViewById(R.id.buttonClear);
        if (savedInstanceState != null) {
            textViewOutput.setText(savedInstanceState.getString(STATE_TEXT_OUTPUT));
        }
        if (buttonClear != null) {
            buttonClear.setOnClickListener(v -> textViewOutput.setText(""));
        }

    }

}

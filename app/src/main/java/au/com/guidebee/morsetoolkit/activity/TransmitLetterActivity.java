package au.com.guidebee.morsetoolkit.activity;

import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

import au.com.guidebee.morsetoolkit.ConfigInfo;
import au.com.guidebee.morsetoolkit.component.FontTextView;
import au.com.guidebee.morsetoolkit.helper.MorseHelper;
import au.com.guidebee.morsetoolkit.helper.UIHelper;


public class TransmitLetterActivity extends MorseActivity {

    protected final static int TryLimit = 3;
    private final static String STATE_TRY_LEFT = "State_Try_Left";
    private final static String STATE_TOTAL_LETTERS = "State_Total_Letters";
    private final static String STATE_TOTAL_CORRECT = "State_Total_Correct";
    private final static String STATE_CURRENT_LETTER = "State_Current_Letter";
    protected final Random random = new Random();
    protected final ArrayList<Character> allTestLetters = new ArrayList<>();
    protected TextView textViewLetter;
    protected FontTextView textViewMorseString;
    protected TextView textViewTries;
    protected TextView textViewTryInfo;
    protected ImageButton imageButtonPlay;
    protected FrameLayout textViewBackground;
    protected int totalLetters = 0;
    protected int totalCorrect = 0;
    protected int tries = 0;
    protected char currentLetter = 'a';

    @Override
    public void onEmit(Character character) {
        textViewLetter.post(() -> {
            if (!(character == ' ' && lastChar == ' ')) {
                if (character.toString().compareToIgnoreCase(String.valueOf(currentLetter)) == 0) {
                    onCorrectAnswer();
                    textViewLetter.setTextColor(correctAnswerColor);
                    textViewLetter.postDelayed(() -> randomLetter(null), 1500);
                    totalCorrect += 1;

                } else {
                    tries += 1;
                    if (tries >= TryLimit) {
                        onErrorAnswer();
                        textViewLetter.setTextColor(wrongAnswerColor);
                        String morseString = MorseHelper.morseCodeData.get(currentLetter);
                        morseString = morseString.replace('.', 'E').replace('-', 'T');
                        textViewMorseString.setText(morseString);
                        textViewMorseString.setVisibility(View.VISIBLE);
                        textViewLetter.postDelayed(() -> randomLetter(null), 3000);
                    }
                }
            }
            textViewTries.setText(String.valueOf(Math.max(0, TryLimit - tries)));
            textViewTryInfo.setText(totalCorrect + "/" + totalLetters);

        });
        lastChar = character;
    }


    protected void onCorrectAnswer() {
        try {
            if (!mediaPlayerPoint.isPlaying()) {
                mediaPlayerPoint.seekTo(0);
                mediaPlayerPoint.start();
            }
        } catch (Exception e) {
            //ignore error
        }

    }

    protected void onErrorAnswer() {

    }

    @Override
    protected void setActivityTitle() {
        actionBar.setTitle(R.string.transmit_letter_title);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putChar(STATE_CURRENT_LETTER, currentLetter);
        savedInstanceState.putInt(STATE_TOTAL_LETTERS, totalLetters);
        savedInstanceState.putInt(STATE_TOTAL_CORRECT, totalCorrect);
        savedInstanceState.putInt(STATE_TRY_LEFT, tries);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_transmit_letter);
        super.onCreate(savedInstanceState);
        initComponent(savedInstanceState);

    }

    protected void initComponent(Bundle savedInstanceState) {
        textViewLetter = (TextView) findViewById(R.id.textViewLetter);
        textViewMorseString = (FontTextView) findViewById(R.id.textViewMorseString);
        textViewTries = (TextView) findViewById(R.id.textViewTries);
        textViewTryInfo = (TextView) findViewById(R.id.textViewTryInfo);
        imageButtonPlay = (ImageButton) findViewById(R.id.imageButtonPlay);
        textViewBackground = (FrameLayout) findViewById(R.id.textViewBackground);

        ViewTreeObserver vto;
        if (textViewBackground != null) {
            vto = textViewBackground.getViewTreeObserver();
            vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                public boolean onPreDraw() {
                    textViewBackground.getViewTreeObserver().removeOnPreDrawListener(this);
                    int height = textViewBackground.getMeasuredHeight();
                    int width = textViewBackground.getMeasuredWidth();
                    int minSize = Math.min(width, height);
                    float spSize = (float) (minSize / UIHelper.getScreenDp(TransmitLetterActivity.this) * 0.6);
                    textViewLetter.setTextSize(spSize);
                    return true;
                }
            });
        }
        initTestLetters();
        if (imageButtonPlay != null) {
            imageButtonPlay.setOnClickListener(v
                    ->
                    executor.execute(new PlayAudioThread(String.valueOf(currentLetter).toLowerCase())));
            imageButtonPlay.setVisibility(View.INVISIBLE);
        }
        randomLetter(savedInstanceState);
    }

    protected void initTestLetters() {
        allTestLetters.clear();
        allTestLetters.addAll(MorseHelper.initTestLetters(ConfigInfo.transmitLetterType));
    }


    protected void randomLetter(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            int index;
            tries = 0;
            index = random.nextInt(100) % allTestLetters.size();
            currentLetter = allTestLetters.get(index);
            totalLetters += 1;
        } else {

            totalCorrect = savedInstanceState.getInt(STATE_TOTAL_CORRECT);
            totalLetters = savedInstanceState.getInt(STATE_TOTAL_LETTERS);
            currentLetter = (char) savedInstanceState.get(STATE_CURRENT_LETTER);
            tries = savedInstanceState.getInt(STATE_TRY_LEFT);

        }
        textViewMorseString.setVisibility(View.INVISIBLE);
        textViewLetter.setTextColor(primaryLetterColor);
        textViewLetter.setText(String.valueOf(currentLetter).toUpperCase());
    }

}

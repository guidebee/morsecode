package au.com.guidebee.morsetoolkit.activity;

import android.animation.Animator;
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


public class FlashcardActivity extends MorseActivity {

    private final Random random = new Random();
    private final ArrayList<Character> allTestLetters = new ArrayList<>();
    private TextView textViewLetter;
    private FontTextView textViewMorseString;
    private ImageButton imageButtonPlay;
    private TextView textViewBackground;
    private char currentLetter = 'a';
    private int currentLetterIndex;
    private boolean letterOnFront = false;

    @Override
    protected void setActivityTitle() {
        actionBar.setTitle(R.string.flashcard_title);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_flashcard);
        super.onCreate(savedInstanceState);

        textViewLetter = (TextView) findViewById(R.id.textViewLetter);
        textViewMorseString = (FontTextView) findViewById(R.id.textViewMorseString);
        textViewBackground = (TextView) findViewById(R.id.textViewBackground);
        imageButtonPlay = (ImageButton) findViewById(R.id.imageButtonPlay);
        ImageButton imageButtonLeft = (ImageButton) findViewById(R.id.imageButtonLeft);
        ImageButton imageButtonRight = (ImageButton) findViewById(R.id.imageButtonRight);

        letterOnFront = ConfigInfo.letterOnFront;
        initTestLetters();
        if (imageButtonPlay != null) {
            imageButtonPlay.setOnClickListener(v
                    ->
                    executor.execute(new PlayAudioThread(String.valueOf(currentLetter).toLowerCase())));


        }
        textViewLetter.postDelayed(this::randomLetter, 500);
        textViewLetter.setOnClickListener(v -> textViewLetter.animate().rotationY(180).alpha(0.0f)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        textViewLetter.setVisibility(View.GONE);
                        imageButtonPlay.setVisibility(View.VISIBLE);
                        textViewMorseString.setVisibility(View.VISIBLE);
                        textViewMorseString.setAlpha(1.0f);
                        textViewMorseString.setRotationY(0);
                        imageButtonPlay.setRotationY(0);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }));


        textViewMorseString.setOnClickListener(v -> {
            imageButtonPlay.animate().rotationY(180);
            textViewMorseString.animate().rotationY(180).alpha(0.0f)
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            textViewLetter.setVisibility(View.VISIBLE);
                            imageButtonPlay.setVisibility(View.GONE);
                            textViewMorseString.setVisibility(View.GONE);
                            textViewLetter.setAlpha(1.0f);
                            textViewLetter.setRotationY(0);

                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });


        });

        if (imageButtonLeft != null) {
            imageButtonLeft.setOnClickListener(v -> moveLeft());
        }

        if (imageButtonRight != null) {
            imageButtonRight.setOnClickListener(v -> moveRight());
        }

        ViewTreeObserver vto = textViewBackground.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                textViewBackground.getViewTreeObserver().removeOnPreDrawListener(this);
                int height = textViewBackground.getMeasuredHeight();
                int width = textViewBackground.getMeasuredWidth();
                int minSize = Math.min(width, height);
                float spSize = (float) (minSize / UIHelper.getScreenDp(FlashcardActivity.this) * 0.8);
                if (width > height) {
                    int bigEdge = (int) Math.min(width, height * 1.2);
                    FrameLayout.LayoutParams loParams
                            = (FrameLayout.LayoutParams) textViewBackground.getLayoutParams();
                    loParams.height = height;
                    loParams.width = bigEdge;
                    textViewBackground.setLayoutParams(loParams);

                } else {
                    int bigEdge = (int) Math.min(height, width * 1.2);
                    FrameLayout.LayoutParams loParams
                            = (FrameLayout.LayoutParams) textViewBackground.getLayoutParams();
                    loParams.height = bigEdge;
                    loParams.width = width;
                    textViewBackground.setLayoutParams(loParams);
                }
                textViewLetter.setTextSize(spSize);
                return true;
            }
        });
    }

    private void moveRight() {
        if (ConfigInfo.flashCardType) {
            currentLetterIndex = (currentLetterIndex + 1) % allTestLetters.size();
        } else {
            currentLetterIndex = random.nextInt(allTestLetters.size() + 1) % allTestLetters.size();
        }
        changeLetter();
    }

    private void moveLeft() {
        if (ConfigInfo.flashCardType) {
            currentLetterIndex = (currentLetterIndex - 1) % allTestLetters.size();
            if (currentLetterIndex < 0) currentLetterIndex = allTestLetters.size() - 1;
        } else {
            currentLetterIndex = random.nextInt(allTestLetters.size() + 1) % allTestLetters.size();
        }
        changeLetter();
    }

    private void initTestLetters() {
        allTestLetters.clear();
        allTestLetters.addAll(MorseHelper.initTestLetters(ConfigInfo.TYPE_LETTER_LETTER
                | ConfigInfo.TYPE_LETTER_NUMBER
                | ConfigInfo.TYPE_LETTER_PUNCTUATION));
    }


    protected void randomLetter() {
        if (!ConfigInfo.flashCardType) {
            currentLetterIndex = random.nextInt(100) % allTestLetters.size();
        }
        changeLetter();
    }

    private void changeLetter() {

        if (letterOnFront) {
            textViewLetter.setVisibility(View.VISIBLE);
            textViewMorseString.setVisibility(View.GONE);
            imageButtonPlay.setVisibility(View.GONE);
            textViewLetter.setAlpha(1.0f);
            textViewLetter.setRotationY(0);
        } else {
            textViewLetter.setVisibility(View.GONE);
            textViewMorseString.setVisibility(View.VISIBLE);
            imageButtonPlay.setVisibility(View.VISIBLE);
            textViewMorseString.setAlpha(1.0f);
            textViewMorseString.setRotationY(0);
            imageButtonPlay.setRotationY(0);
        }

        currentLetter = allTestLetters.get(currentLetterIndex);
        textViewLetter.setTextColor(primaryLetterColor);
        textViewLetter.setText(String.valueOf(currentLetter).toUpperCase());
        String morseString = MorseHelper.morseCodeData.get(currentLetter);
        morseString = morseString.replace('.', 'E').replace('-', 'T');
        textViewMorseString.setText(morseString);
    }

}

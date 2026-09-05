package au.com.guidebee.morsetoolkit.component;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import au.com.guidebee.morsetoolkit.ConfigInfo;
import au.com.guidebee.morsetoolkit.activity.R;
import au.com.guidebee.morsetoolkit.helper.MorseEncoder;
import au.com.guidebee.morsetoolkit.helper.MorseHelper;


public class HandbookCardAdapter extends RecyclerView.Adapter<HandbookCardAdapter.ViewHolder> {
    private final ArrayList<Character> allLetters;
    private int gridSize = 250;
    private MorseEncoder morseEncoder;

    public HandbookCardAdapter(int newGridSize, MorseEncoder morseEncoder) {
        allLetters = MorseHelper.initTestLetters(ConfigInfo.TYPE_LETTER_LETTER
                | ConfigInfo.TYPE_LETTER_NUMBER
                | ConfigInfo.TYPE_LETTER_PUNCTUATION);
        this.gridSize = newGridSize;
        this.morseEncoder = morseEncoder;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardView v = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_handbook, parent, false);
        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) v.getLayoutParams();
        layoutParams.width = gridSize;
        v.setLayoutParams(layoutParams);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.updateInfo(allLetters.get(position), morseEncoder);
    }

    @Override
    public int getItemCount() {
        return allLetters.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        protected TextView textViewLetter;
        protected FontTextView textViewMorseString;
        protected ImageButton imageButtonPlay;
        private CardView frameLayout;


        public ViewHolder(CardView itemView) {
            super(itemView);
            frameLayout = itemView;
            textViewLetter = (TextView) frameLayout.findViewById(R.id.textViewLetter);
            textViewMorseString = (FontTextView) frameLayout.findViewById(R.id.textViewMorseString);
            imageButtonPlay = (ImageButton) frameLayout.findViewById(R.id.imageButtonPlay);
        }

        public void updateInfo(char letter, MorseEncoder morseEncoder) {
            textViewLetter.setText(String.valueOf(letter).toUpperCase());
            String morseString = MorseHelper.morseCodeData.get(letter);
            morseString = morseString.replace('.', 'E').replace('-', 'T');
            textViewMorseString.setText(morseString);
            if (imageButtonPlay != null) {
                imageButtonPlay.setOnClickListener(v
                        ->
                        morseEncoder.playMorseCode(String.valueOf(letter).toLowerCase()));

            }
        }
    }
}

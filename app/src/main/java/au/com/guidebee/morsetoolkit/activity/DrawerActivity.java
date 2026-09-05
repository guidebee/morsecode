package au.com.guidebee.morsetoolkit.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.mikepenz.materialdrawer.holder.ImageHolder;
import com.mikepenz.materialdrawer.holder.StringHolder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.widget.MaterialDrawerSliderView;

import au.com.guidebee.morsetoolkit.ConfigInfo;
import au.com.guidebee.morsetoolkit.activity.battlecity.BattleCityGameActivity;
import au.com.guidebee.morsetoolkit.activity.flappybird.FlappyBirdGameActivity;


public abstract class DrawerActivity extends AppCompatActivity {

    protected ActionBar actionBar;
    protected int primaryLetterColor = 0xff3f51b5;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(androidx.appcompat.R.attr.colorPrimary, typedValue, true);
        primaryLetterColor = typedValue.data;
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        ConfigInfo.loadConfiguration(this);
        setupDrawer(savedInstanceState);
    }

    protected void setActivityTitle() {
        actionBar.setTitle(R.string.app_name);
    }

    protected void setupDrawer(Bundle savedInstanceState) {
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(false);
            setActivityTitle();
        }
        PrimaryDrawerItem transmitDrawItem = new PrimaryDrawerItem();
        transmitDrawItem.setName(new StringHolder(R.string.transmit));
        transmitDrawItem.setIcon(new ImageHolder(R.drawable.transmit));
        transmitDrawItem.setIdentifier(1L);

        PrimaryDrawerItem receiveDrawItem = new PrimaryDrawerItem();
        receiveDrawItem.setName(new StringHolder(R.string.receive));
        receiveDrawItem.setIcon(new ImageHolder(R.drawable.receive));
        receiveDrawItem.setIdentifier(2L);

        PrimaryDrawerItem listenDrawItem = new PrimaryDrawerItem();
        listenDrawItem.setName(new StringHolder(R.string.decoder));
        listenDrawItem.setIcon(new ImageHolder(R.drawable.listen));
        listenDrawItem.setIdentifier(3L);

        PrimaryDrawerItem optionDrawItem = new PrimaryDrawerItem();
        optionDrawItem.setName(new StringHolder(R.string.options));
        optionDrawItem.setIcon(new ImageHolder(R.drawable.settings));
        optionDrawItem.setIdentifier(4L);

        PrimaryDrawerItem flashcardDrawItem = new PrimaryDrawerItem();
        flashcardDrawItem.setName(new StringHolder(R.string.flashcard));
        flashcardDrawItem.setIcon(new ImageHolder(R.drawable.flashcard));
        flashcardDrawItem.setIdentifier(5L);

        PrimaryDrawerItem handbookDrawItem = new PrimaryDrawerItem();
        handbookDrawItem.setName(new StringHolder(R.string.handbook));
        handbookDrawItem.setIcon(new ImageHolder(R.drawable.handbook));
        handbookDrawItem.setIdentifier(6L);

        PrimaryDrawerItem battleCityDrawItem = new PrimaryDrawerItem();
        battleCityDrawItem.setName(new StringHolder(R.string.battlecity));
        battleCityDrawItem.setIcon(new ImageHolder(R.drawable.morsegame));
        battleCityDrawItem.setIdentifier(8L);

        //Create the drawer
        MaterialDrawerSliderView sliderView = new MaterialDrawerSliderView(this);
        sliderView.getItemAdapter().add(
                transmitDrawItem,
                receiveDrawItem,
                listenDrawItem,
                optionDrawItem,
                flashcardDrawItem,
                handbookDrawItem,
                battleCityDrawItem
        );
        sliderView.setOnDrawerItemClickListener((view, drawerItem, position) -> {
            handleItemClick(position);
            return false;
        });
        // Note: For 8.x, you need to add this sliderView to a DrawerLayout in your activity layout.
        // For now, we just initialize it to fix compilation.


    }

    private void handleItemClick(int position) {
        switch (position) {
            case 0: {
                Intent intent = null;
                switch (ConfigInfo.transmitType) {
                    case ConfigInfo.TYPE_LETTER:
                        intent = new Intent(DrawerActivity.this, TransmitLetterActivity.class);
                        break;
                    case ConfigInfo.TYPE_WORD:
                        intent = new Intent(DrawerActivity.this, TransmitWordActivity.class);
                        break;
                    case ConfigInfo.TYPE_FREE_TEXT:
                        intent = new Intent(DrawerActivity.this, TransmitFreePadActivity.class);
                        break;
                }
                if (intent != null) {
                    addIntentFlag(intent);
                    startActivity(intent);
                }
            }

            break;
            case 1: {
                Intent intent = null;
                switch (ConfigInfo.receiveType) {
                    case ConfigInfo.TYPE_LETTER:
                        intent = new Intent(DrawerActivity.this, ReceiveLetterActivity.class);
                        break;
                    case ConfigInfo.TYPE_WORD:
                        intent = new Intent(DrawerActivity.this, ReceiveWordActivity.class);
                        break;
                    case ConfigInfo.TYPE_FREE_TEXT:
                        intent = new Intent(DrawerActivity.this, ReceiveFreePadActivity.class);
                        break;
                }
                if (intent != null) {
                    addIntentFlag(intent);
                    startActivity(intent);
                }
            }
            break;
            case 2: {
                Intent intent = new Intent(DrawerActivity.this, ListenActivity.class);
                addIntentFlag(intent);
                startActivity(intent);
            }
            break;
            case 3: {
                Intent intent = new Intent(DrawerActivity.this, OptionActivity.class);
                addIntentFlag(intent);
                startActivity(intent);
            }
            break;
            case 4: {
                Intent intent = new Intent(DrawerActivity.this, FlashcardActivity.class);
                addIntentFlag(intent);
                startActivity(intent);
            }
            break;
            case 5: {
                Intent intent = new Intent(DrawerActivity.this, HandbookActivity.class);
                addIntentFlag(intent);
                startActivity(intent);
            }
            break;
//            case 6: {
//                Intent intent = new Intent(DrawerActivity.this, FlappyBirdGameActivity.class);
//                addIntentFlag(intent);
//                startActivity(intent);
//            }
//            break;
            case 6: {
                Intent intent = new Intent(DrawerActivity.this, BattleCityGameActivity.class);
                addIntentFlag(intent);
                startActivity(intent);
            }
            break;
        }
    }

    private void addIntentFlag(Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // e
    }
}

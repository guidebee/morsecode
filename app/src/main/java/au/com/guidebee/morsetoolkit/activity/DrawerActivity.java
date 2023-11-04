package au.com.guidebee.morsetoolkit.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;

import com.google.android.gms.ads.AdView;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;

import au.com.guidebee.morsetoolkit.ConfigInfo;
import au.com.guidebee.morsetoolkit.activity.battlecity.BattleCityGameActivity;
import au.com.guidebee.morsetoolkit.activity.flappybird.FlappyBirdGameActivity;


public abstract class DrawerActivity extends AppCompatActivity {

    protected ActionBar actionBar;
    protected int primaryLetterColor = 0xff3f51b5;
    private Toolbar toolbar;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        primaryLetterColor = typedValue.data;
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        ConfigInfo.loadConfiguration(this);
        setupDrawer(savedInstanceState);
        mAdView = (AdView) findViewById(R.id.ad_view);
        if (mAdView != null) {
            if (ConfigInfo.showAds) {
                if (ConfigInfo.adRequest != null) {
                    mAdView.loadAd(ConfigInfo.adRequest);
                }
            } else {
                mAdView.setVisibility(View.INVISIBLE);
            }
        }
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
        PrimaryDrawerItem transmitDrawItem = new PrimaryDrawerItem()
                .withName(R.string.transmit)
                .withIcon(R.drawable.transmit)
                .withIdentifier(1);

        PrimaryDrawerItem receiveDrawItem = new PrimaryDrawerItem()
                .withName(R.string.receive)
                .withIcon(R.drawable.receive)
                .withIdentifier(2);

        PrimaryDrawerItem listenDrawItem = new PrimaryDrawerItem()
                .withName(R.string.decoder)
                .withIcon(R.drawable.listen)
                .withIdentifier(3);

        PrimaryDrawerItem optionDrawItem = new PrimaryDrawerItem()
                .withName(R.string.options)
                .withIcon(R.drawable.settings)
                .withIdentifier(4);

        PrimaryDrawerItem flashcardDrawItem = new PrimaryDrawerItem()
                .withName(R.string.flashcard)
                .withIcon(R.drawable.flashcard)
                .withIdentifier(5);

        PrimaryDrawerItem handbookDrawItem = new PrimaryDrawerItem()
                .withName(R.string.handbook)
                .withIcon(R.drawable.handbook)
                .withIdentifier(6);

//        PrimaryDrawerItem gameDrawItem = new PrimaryDrawerItem()
//                .withName(R.string.flappybird)
//                .withIcon(R.drawable.morsegame)
//                .withIdentifier(7);

        PrimaryDrawerItem battleCityDrawItem = new PrimaryDrawerItem()
                .withName(R.string.battlecity)
                .withIcon(R.drawable.morsegame)
                .withIdentifier(8);

       /* PrimaryDrawerItem audioDrawItem = new PrimaryDrawerItem()
                .withName(R.string.audio)
                .withIcon(R.drawable.audio)
                .withIdentifier(8);

        PrimaryDrawerItem lightDrawItem = new PrimaryDrawerItem()
                .withName(R.string.light)
                .withIcon(R.drawable.light)
                .withIdentifier(9);*/

        //Create the drawer
        new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .addDrawerItems(
                        transmitDrawItem,
                        receiveDrawItem,
                        listenDrawItem,
                        optionDrawItem,
                        flashcardDrawItem,
                        handbookDrawItem,
//                        gameDrawItem,
                        battleCityDrawItem
                        // audioDrawItem,
                        //  lightDrawItem

                ) // add the items we want to use with our Drawer
                .withOnDrawerItemClickListener((view, position, drawerItem) -> {
                    handleItemClick(position);
                    //we do not consume the event and want the Drawer
                    // to continue with the event chain
                    return false;
                })
                .withSavedInstance(savedInstanceState)
                .build();

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
            intent.addFlags(0x8000); // e
    }

    /**
     * Called when returning to the activity
     */
    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    /**
     * Called before the activity is destroyed
     */
    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }
}

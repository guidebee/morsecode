package au.com.guidebee.morsetoolkit.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

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
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;

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

        PrimaryDrawerItem gameDrawItem = new PrimaryDrawerItem();
        gameDrawItem.setName(new StringHolder(R.string.flappybird));
        gameDrawItem.setIcon(new ImageHolder(R.drawable.morsegame));
        gameDrawItem.setIdentifier(7L);

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
                gameDrawItem,
                battleCityDrawItem
        );
        sliderView.setOnDrawerItemClickListener((view, drawerItem, position) -> {
            if (drawerLayout != null) {
                drawerLayout.closeDrawers();
            }
            handleItemClick(position);
            return false;
        });

        // MaterialDrawer 8.x no longer builds the DrawerLayout for us, so wrap the
        // already-inflated content view in one and slide the item list in from the side.
        ViewGroup contentRoot = findViewById(android.R.id.content);
        View content = contentRoot.getChildAt(0);
        contentRoot.removeView(content);

        drawerLayout = new DrawerLayout(this);
        drawerLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        drawerLayout.addView(content, new DrawerLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        DrawerLayout.LayoutParams sliderParams = new DrawerLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.START);
        drawerLayout.addView(sliderView, sliderParams);
        contentRoot.addView(drawerLayout);

        if (actionBar != null) {
            drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                    R.string.drawer_open, R.string.drawer_close);
            drawerLayout.addDrawerListener(drawerToggle);
            drawerToggle.syncState();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (drawerToggle != null) {
            drawerToggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (drawerToggle != null) {
            drawerToggle.onConfigurationChanged(newConfig);
        }
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
            case 6: {
                Intent intent = new Intent(DrawerActivity.this, FlappyBirdGameActivity.class);
                addIntentFlag(intent);
                startActivity(intent);
            }
            break;
            case 7: {
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

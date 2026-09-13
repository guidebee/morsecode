package com.mapdigit.game.tutorial;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Launcher menu listing every Raindrop tutorial lesson so all four are
 * reachable from a single installed APK. The original tutorial repo only
 * registered one Activity ({@code drop.DropGameActivity}) in its manifest;
 * {@code basics}, {@code coords} and {@code microedition} existed as source
 * but were never wired up as launchable screens.
 *
 * Added as part of the gameengine upgrade's regression-suite setup — see
 * docs/GAMEENGINE_UPGRADE_PLAN.md section 5.5 in the morsecode repo.
 */
public class LessonPickerActivity extends ListActivity {

    private static final String[] LESSON_LABELS = {
            "Basics (Hello World)",
            "Coordinates / Camera",
            "Drop (collision, HUD, atlas, touchpad)",
            "Microedition (LayerManager / Sprite)",
    };

    private static final Class<?>[] LESSON_ACTIVITIES = {
            com.mapdigit.game.tutorial.basics.HelloWorldActivity.class,
            com.mapdigit.game.tutorial.coords.CoordinateGameActivity.class,
            com.mapdigit.game.tutorial.drop.DropGameActivity.class,
            com.mapdigit.game.tutorial.microedition.DropGameActivity.class,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setListAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, LESSON_LABELS));
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        startActivity(new Intent(this, LESSON_ACTIVITIES[position]));
    }
}

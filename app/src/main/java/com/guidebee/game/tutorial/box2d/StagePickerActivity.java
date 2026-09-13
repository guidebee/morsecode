package com.guidebee.game.tutorial.box2d;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Launcher menu listing every Box2D tutorial stage so each one is reachable
 * from a single installed APK, instead of requiring a source edit +
 * rebuild to switch which stage {@link Box2DGameScene} shows (the original
 * tutorial repo hardcoded a single stage per commit).
 *
 * Added as part of the gameengine upgrade's regression-suite setup — see
 * docs/GAMEENGINE_UPGRADE_PLAN.md section 5.4 in the morsecode repo.
 */
public class StagePickerActivity extends ListActivity {

    public static final String EXTRA_STAGE_CLASS = "stage_class";

    private static final String STAGE_PACKAGE = "com.guidebee.game.tutorial.box2d.stage.";

    /** The 10 concrete, instantiable stages. {@code Box2DGameStage} itself is abstract. */
    private static final String[] STAGE_SIMPLE_NAMES = {
            "BasicBox2DStage",
            "BodyTypeStage",
            "ShapeTypeStage",
            "ForceAndImpulseStage",
            "CollisionStage",
            "SensorStage",
            "RayCastStage",
            "JointsOverviewStage",
            "SelfControlStage",
            "BulletStage",
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setListAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, STAGE_SIMPLE_NAMES));
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        String stageClass = STAGE_PACKAGE + STAGE_SIMPLE_NAMES[position];
        Intent intent = new Intent(this, Box2DGameActivity.class);
        intent.putExtra(EXTRA_STAGE_CLASS, stageClass);
        startActivity(intent);
    }
}

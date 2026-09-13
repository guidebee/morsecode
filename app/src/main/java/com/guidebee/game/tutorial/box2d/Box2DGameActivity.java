package com.guidebee.game.tutorial.box2d;

import android.os.Bundle;

import com.guidebee.game.Configuration;
import com.guidebee.game.activity.GameActivity;


public class Box2DGameActivity extends GameActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration config = new Configuration();

        config.useAccelerometer = false;
        config.useCompass = false;

        String stageClass = getIntent().getStringExtra(StagePickerActivity.EXTRA_STAGE_CLASS);
        if (stageClass == null) {
            // Preserve the original tutorial repo's default when launched without
            // an explicit stage (e.g. directly from an IDE run configuration).
            stageClass = "com.guidebee.game.tutorial.box2d.stage.BulletStage";
        }

        initialize(new Box2DGamePlay(stageClass), config);
    }
}
package com.guidebee.game.tutorial.box2d;

import com.guidebee.game.GamePlay;
import com.guidebee.game.graphics.TextureAtlas;

import static com.guidebee.game.GameEngine.assetManager;


public class Box2DGamePlay extends GamePlay {

    private final String stageClass;

    public Box2DGamePlay(String stageClass) {
        this.stageClass = stageClass;
    }

    @Override
    public void create() {
        loadAssets();
        Box2DGameScene screen=new Box2DGameScene(stageClass);
        setScreen(screen);
    }

    @Override
    public void dispose(){
        assetManager.dispose();
    }

    private void loadAssets(){


        assetManager.load("box2d.atlas", TextureAtlas.class);
        assetManager.finishLoading();
    }
}
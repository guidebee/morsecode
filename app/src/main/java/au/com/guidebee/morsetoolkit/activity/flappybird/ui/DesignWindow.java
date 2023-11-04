/*******************************************************************************
 * Copyright 2015 See AUTHORS file.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
//--------------------------------- PACKAGE ------------------------------------
package au.com.guidebee.morsetoolkit.activity.flappybird.ui;

//--------------------------------- IMPORTS ------------------------------------

import android.widget.Toast;

import com.guidebee.game.GameEngine;
import com.guidebee.game.InputMultiplexer;
import com.guidebee.game.InputProcessor;
import com.guidebee.game.graphics.TextureAtlas;
import com.guidebee.game.input.GestureDetector;
import com.guidebee.game.ui.Button;
import com.guidebee.game.ui.ChangeListener;
import com.guidebee.game.ui.Event;
import com.guidebee.game.ui.EventListener;
import com.guidebee.game.ui.Image;
import com.guidebee.game.ui.Label;
import com.guidebee.game.ui.Skin;
import com.guidebee.game.ui.Slider;
import com.guidebee.game.ui.Table;
import com.guidebee.game.ui.UIComponent;
import com.guidebee.game.ui.Window;
import com.guidebee.math.Vector2;

import au.com.guidebee.morsetoolkit.activity.flappybird.FlappyBirdGamePlay;
import au.com.guidebee.morsetoolkit.activity.flappybird.FlappyBirdScene;
import au.com.guidebee.morsetoolkit.activity.flappybird.config.Configuration;
import au.com.guidebee.morsetoolkit.activity.flappybird.state.TubePosition;

import static com.guidebee.game.GameEngine.graphics;
import static com.guidebee.game.GameEngine.input;

//[------------------------------ MAIN CLASS ----------------------------------]

/**
 * Tube design window.
 * @author James Shen <james.shen@guidebee.com>
 */
public class DesignWindow extends BaseWindow
        implements GestureDetector.GestureListener {

    private static final String TEXTURE_SKIN_UI = "skin/default/uiskin.atlas";
    private static final String SKIN_UI = "skin/default/uiskin.json";
    final TubeLayoutImage tubeLayoutImage = new TubeLayoutImage(this);
    private final Skin defaultSkin;
    private final Slider sldTubeNu;
    private final NumberImage numberTubeNumber;
    private final Slider sldTopHeight;
    private final NumberImage numberTopHeight;
    private final Slider sldBottomHeight;
    //private final Slider sldDistance;
    private final int halfScreenHeight;
    private final NumberImage numberBottomHeight;
    //private final NumberImage numberDistance;
    private InputProcessor savedInputProcessor = null;
    private InputMultiplexer inputMultiplexer = new InputMultiplexer();
    private float movePeriod = 5f;
    private boolean moveLeft = true;
    private int currentTubeIndex;


    /**
     * Constructor.
     * @param gamePlay
     */
    public DesignWindow(final FlappyBirdGamePlay gamePlay) {
        super(gamePlay, Configuration.SCREEN_WIDTH,
                Configuration.SCREEN_HEIGHT);

        defaultSkin = new Skin(GameEngine.files.internal(SKIN_UI),
                new TextureAtlas(TEXTURE_SKIN_UI));
        halfScreenHeight = graphics.getHeight() / 2;

        Image image = new Image(uiSkin, "menu_background");
        image.setFillParent(true);
        stack.addComponent(image);

        Table table = new Table();
        table.setFillParent(true);
        stack.addComponent(table);


        table.add(tubeLayoutImage).colspan(6);
        spacing(table, 3);

        ////////////////////
        Image tubeNumImage = new Image(uiSkin, "tubemum");
        table.add(tubeNumImage);

        numberTubeNumber = new NumberImage(100, true);
        table.add(numberTubeNumber);

        Label spaceLabel = new Label("sp", defaultSkin);
        spaceLabel.setVisible(false);
        table.add(spaceLabel);
        sldTubeNu = new Slider(10f, 500.0f, 10f, false, defaultSkin);
        sldTubeNu.setValue(100);
        sldTubeNu.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, UIComponent component) {
                numberTubeNumber.setValue((int) sldTubeNu.getValue());

            }
        });
        table.add(sldTubeNu);
        spaceLabel = new Label("space", defaultSkin);
        spaceLabel.setVisible(false);
        table.add(spaceLabel);

        Button btnRandom = new Button(uiSkin, "random");
        btnRandom.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {

                movePeriod = 5f;
                moveLeft = true;

                int num = (int) sldTubeNu.getValue();
                tubeLayoutImage.generateLevelData(num);
                tubeLayoutImage.move(2500);

                Configuration.designedTubes = tubeLayoutImage.getTubePositionArray();


                return true;
            }
        });

        table.add(btnRandom);
        spacing(table, 3);

        //////////////
        Image topLenImage = new Image(uiSkin, "toplen");
        table.add(topLenImage);

        numberTopHeight = new NumberImage(1, true);

        table.add(numberTopHeight);
        table.add(new Label(" ", defaultSkin));

        sldTopHeight = new Slider(1f, 100.0f, 1f, false, defaultSkin);
        sldTopHeight.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, UIComponent component) {
                numberTopHeight.setValue((int) sldTopHeight.getValue());
                int newTubeIndex = tubeLayoutImage.getMiddleIndex();
                if (newTubeIndex == currentTubeIndex) {
                    updateTubeInfo();
                }

            }
        });
        table.add(sldTopHeight);

        table.add(new Label(" ", defaultSkin));
        Button btnLoad = new Button(uiSkin, "load");
        btnLoad.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                try {
                    tubeLayoutImage.readTubeFile();
                    Configuration.gameActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Configuration.gameActivity.getApplicationContext(),
                                    "Tube File Loaded!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    Configuration.gameActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Configuration.gameActivity.getApplicationContext(),
                                    "Error loading tube file,please save first!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                movePeriod = 5f;
                moveLeft = true;

                return false;
            }
        });
        //table.add(btnLoad);
        spacing(table, 3);

        //////////////
        Image bottomImage = new Image(uiSkin, "bottom");
        table.add(bottomImage);

        numberBottomHeight = new NumberImage(1, true);

        table.add(numberBottomHeight);

        table.add(new Label(" ", defaultSkin));
        sldBottomHeight = new Slider(1f, 100.0f, 1f, false, defaultSkin);
        sldBottomHeight.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, UIComponent component) {
                numberBottomHeight.setValue((int) sldBottomHeight.getValue());
                int newTubeIndex = tubeLayoutImage.getMiddleIndex();
                if (newTubeIndex == currentTubeIndex) {
                    updateTubeInfo();
                }

            }
        });
        table.add(sldBottomHeight);

        table.add(new Label(" ", defaultSkin));
        Button btnRun = new Button(uiSkin, "run");
        btnRun.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                Configuration.designedTubes = tubeLayoutImage.getTubePositionArray();
                gamePlay.setScreen(new FlappyBirdScene(gamePlay));
                return true;
            }
        });
        table.add(btnRun);
        spacing(table, 3);


        //////////////
        /*Image distanceImage =new Image(uiSkin,"distance");
        table.add(distanceImage);

        numberDistance=new NumberImage(1,true);

        table.add(numberDistance);

        sldDistance= new Slider(1f,100.0f,1f,false,defaultSkin);
        sldDistance.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, UIComponent component) {
                numberDistance.setValue((int) sldDistance.getValue());

            }
        });
        table.add(sldDistance);


        table.add(new Label(" ", defaultSkin));
        Button btnSave=new Button(uiSkin,"save");
        table.add(btnSave);
        spacing(table, 2);


        */

        //////////////


        Button btnChange = new Button(uiSkin, "change");

        table.add(btnChange);
        btnChange.setVisible(false);
        Button btnReset = new Button(uiSkin, "reset");

        btnReset.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                Configuration.designedTubes = null;
                Configuration.gameActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Configuration.gameActivity.getApplicationContext(),
                                "Tube layout reset to use standard.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                return false;
            }
        });

        table.add(btnReset);


        Button btnSave = new Button(uiSkin, "save");
        btnSave.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                tubeLayoutImage.saveTubeFile();
                Configuration.gameActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Configuration.gameActivity.getApplicationContext(),
                                "Tube File Saved!",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                return false;
            }
        });
        table.add(new Label(" ", defaultSkin));
        //table.add(btnSave);
        table.add(new Label(" ", defaultSkin));
        table.add(new Label(" ", defaultSkin));
        Button btnBack = new Button(uiSkin, "back");

        btnBack.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                gamePlay.setScreen(new MainWindow(gamePlay));
                return true;
            }
        });
        table.add(btnBack);


    }


    /**
     * Use to adjust table spacing.
     * @param table
     * @param count
     */
    private void spacing(Table table, int count) {
        for (int j = 0; j < count; j++) {
            table.add(new Label("", Window.defaultSkin));
            table.row();
        }
    }


    /**
     * Update tube info.
     */
    public void updateCurrentTubeInfo() {

        currentTubeIndex = tubeLayoutImage.getMiddleIndex();
        TubePosition tubePosition = tubeLayoutImage.getMiddleTube();

        if (tubePosition != null) {
            sldTopHeight.setValue(TubePosition.getBlockSize(
                    tubePosition.topTubeHeight));
            sldBottomHeight.setValue(TubePosition.getBlockSize(
                    tubePosition.bottomTubeHeight));

        }

    }


    /**
     * Current tube info if user change the slider.
     */
    private void updateTubeInfo() {

        if (movePeriod < 0.05f) {//don't update when moving
            int topHeight = (int) (sldTopHeight.getValue());
            int bottomHeight = (int) sldBottomHeight.getValue();
            if (topHeight + bottomHeight > 90) {
                int offset = topHeight + bottomHeight - 90;
                if (topHeight > offset / 2) {
                    topHeight -= offset / 2;

                }
                if (bottomHeight > offset / 2) {
                    bottomHeight -= offset / 2;
                }
                numberBottomHeight.setValue(bottomHeight);
                numberTopHeight.setValue(topHeight);
            }

            tubeLayoutImage.setCurrentTube(topHeight, bottomHeight);
        }
    }


    @Override
    public void render(float delta) {
        super.render(delta);

        if (movePeriod > 0) {
            movePeriod -= delta;

            float direction;
            if (moveLeft) {
                direction = 1.5f;
            } else {
                direction = -1.5f;
            }
            tubeLayoutImage.move((int) (
                    direction * Configuration.MOVE_SPEED * movePeriod));
        }
        if (movePeriod < 0) {
            movePeriod = -1;
        }

    }

    @Override
    public void show() {
        super.show();
        savedInputProcessor = GameEngine.input.getInputProcessor();
        if (savedInputProcessor != null) {
            inputMultiplexer.addProcessor(savedInputProcessor);
        }
        inputMultiplexer.addProcessor(new GestureDetector(this));
        GameEngine.input.setInputProcessor(inputMultiplexer);
        gamePlay.hideBannder();

    }

    @Override
    public void hide() {
        GameEngine.input.setInputProcessor(savedInputProcessor);
        super.hide();
    }


    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        movePeriod = 0;
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        if (input.getY() < halfScreenHeight) {

            moveLeft = velocityX < 0;
            movePeriod = 5f;
        }
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        if (input.getY() < halfScreenHeight) {
            tubeLayoutImage.move(-(int) deltaX);
        }
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2,
                         Vector2 pointer1, Vector2 pointer2) {
        return false;
    }
}

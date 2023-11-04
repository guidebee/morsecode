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

import com.guidebee.game.GameEngine;
import com.guidebee.game.graphics.Color;
import com.guidebee.game.graphics.TextureAtlas;
import com.guidebee.game.ui.Button;
import com.guidebee.game.ui.CheckBox;
import com.guidebee.game.ui.Event;
import com.guidebee.game.ui.EventListener;
import com.guidebee.game.ui.Image;
import com.guidebee.game.ui.Label;
import com.guidebee.game.ui.List;
import com.guidebee.game.ui.Skin;
import com.guidebee.game.ui.Slider;
import com.guidebee.game.ui.Table;
import com.guidebee.utils.collections.Array;

import au.com.guidebee.morsetoolkit.activity.flappybird.FlappyBirdGamePlay;
import au.com.guidebee.morsetoolkit.activity.flappybird.config.Configuration;

//[------------------------------ MAIN CLASS ----------------------------------]

/**
 * User Option windows. this use default skin to draw strings.
 * @author James Shen <james.shen@guidebee.com>
 */
public class OptionWindow extends BaseWindow {

    private static final String TEXTURE_SKIN_UI = "skin/default/uiskin.atlas";
    private static final String SKIN_UI = "skin/default/uiskin.json";
    public final Skin defaultSkin;
    private final Array<String> colors;
    private CheckBox chkSound;
    private Slider sldSound;
    private CheckBox chkMusic;
    private Slider sldMusic;
    private List<String> listColors;
    private Slider sldSpeed;
    private CheckBox chkEncode;
    private CheckBox chkHint;


    /**
     * Constructor.
     * @param gamePlay
     */
    public OptionWindow(final FlappyBirdGamePlay gamePlay) {
        super(gamePlay, Configuration.SCREEN_WIDTH * 4 / 5,
                Configuration.SCREEN_HEIGHT * 4 / 5);
        Configuration.readConfiguration();
        defaultSkin = new Skin(GameEngine.files.internal(SKIN_UI),
                new TextureAtlas(TEXTURE_SKIN_UI));
        colors = new Array<String>();
        colors.add("blue");
        colors.add("cyan");
        colors.add("red");
        colors.add("purple");
        colors.add("yellow");
        Image image = new Image(uiSkin, "menu_background");
        image.setFillParent(true);
        stack.addComponent(image);

        Table table = new Table();
        table.setFillParent(true);
        stack.addComponent(table);


        table.pad(10, 10, 0, 10);

        table.add(new Label("Audio", defaultSkin,
                "default-font", Color.ORANGE)).colspan(2);
        table.row();
        table.columnDefaults(0).padRight(10);
        table.columnDefaults(1).padRight(10);

        chkSound = new CheckBox("Sound", defaultSkin);
        chkSound.setChecked(Configuration.userSettings.soundOn);
        table.add(chkSound);

        sldSound = new Slider(0.0f, 1.0f, 0.1f, false, defaultSkin);
        sldSound.setValue(Configuration.userSettings.soundVolume);
        table.add(sldSound);
        table.row();

        //---------------------------


        chkMusic = new CheckBox("Music", defaultSkin);
        chkMusic.setChecked(Configuration.userSettings.musicOn);
        table.add(chkMusic);

        sldMusic = new Slider(0.0f, 1.0f, 0.1f, false, defaultSkin);
        sldMusic.setValue(Configuration.userSettings.musicVolume);
        table.add(sldMusic);
        table.row();

        table.add(new Label("Bird Color", defaultSkin, "default-font",
                Color.ORANGE));
        listColors = new List<String>(defaultSkin);
        listColors.setItems(colors);
        listColors.setSelectedIndex(getColorIndex(Configuration.userSettings.birdColor));

        table.add(listColors);
        table.row();

        table.add(new Label("Speed", defaultSkin, "default-font",
                Color.ORANGE));
        sldSpeed = new Slider(1.0f, 10.0f, 1f, false, defaultSkin);
        sldSpeed.setValue(Configuration.userSettings.initMoveSpeed);
        table.add(sldSpeed);

        table.row();
        chkEncode = new CheckBox(" Play Encode", defaultSkin);
        chkEncode.setChecked(Configuration.userSettings.gameEncode);
        table.add(chkEncode);

        chkHint = new CheckBox(" Turn on Hint", defaultSkin);
        chkHint.setChecked(Configuration.userSettings.gameHint);
        table.add(chkHint);


        table.row();
        table.add(new Label("", defaultSkin));
        table.row();



        Button saveButton = new Button(uiSkin, "save");

        saveButton.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                Configuration.userSettings.musicOn = chkMusic.isChecked();
                Configuration.userSettings.soundOn = chkSound.isChecked();
                Configuration.soundOn = Configuration.userSettings.soundOn;
                Configuration.musicOn = Configuration.userSettings.musicOn;
                Configuration.userSettings.gameEncode=chkEncode.isChecked();
                Configuration.userSettings.gameHint=chkHint.isChecked();
                Configuration.userSettings.birdColor = listColors.getSelected();
                Configuration.userSettings.initMoveSpeed = (int) sldSpeed.getValue();
                Configuration.MOVE_SPEED = Configuration.userSettings.initMoveSpeed + 1;
                Configuration.NORMAL_SPEED = Configuration.MOVE_SPEED + 1;
                Configuration.userSettings.soundVolume = sldSound.getValue();
                Configuration.userSettings.musicVolume = sldMusic.getValue();
                Configuration.saveConfiguration();
                gamePlay.setScreen(new MainWindow(gamePlay));

                return true;
            }
        });

        table.add(saveButton).left();

        Button backButton = new Button(uiSkin, "back");

        backButton.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                gamePlay.setScreen(new MainWindow(gamePlay));
                return true;
            }
        });

        table.add(backButton).right();
        table.row();


    }

    @Override
    public void show() {
        gamePlay.hideBannder();
        super.show();
    }

    private int getColorIndex(String name) {
        for (int i = 0; i < colors.size; i++) {
            if (colors.get(i) == name) {
                return i;
            }
        }
        return 0;
    }


}

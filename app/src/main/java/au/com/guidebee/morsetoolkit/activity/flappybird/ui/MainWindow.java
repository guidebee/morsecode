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

import com.guidebee.game.ui.Button;
import com.guidebee.game.ui.Event;
import com.guidebee.game.ui.EventListener;
import com.guidebee.game.ui.Image;
import com.guidebee.game.ui.Table;
import com.guidebee.math.Interpolation;

import au.com.guidebee.morsetoolkit.activity.flappybird.FlappyBirdGamePlay;
import au.com.guidebee.morsetoolkit.activity.flappybird.FlappyBirdScene;
import au.com.guidebee.morsetoolkit.activity.flappybird.config.Configuration;

import static com.guidebee.game.ui.actions.Actions.delay;
import static com.guidebee.game.ui.actions.Actions.forever;
import static com.guidebee.game.ui.actions.Actions.moveTo;
import static com.guidebee.game.ui.actions.Actions.rotateBy;
import static com.guidebee.game.ui.actions.Actions.sequence;

//[------------------------------ MAIN CLASS ----------------------------------]

/**
 * Main menu.
 *
 * @author James Shen <james.shen@guidebee.com>
 */
public class MainWindow extends BaseWindow {

    /**
     * Constructor.
     *
     * @param gamePlay
     */
    public MainWindow(final FlappyBirdGamePlay gamePlay) {
        super(gamePlay);

        Configuration.readConfiguration();
        Image image = new Image(uiSkin, "menu_background");
        image.setFillParent(true);
        stack.addComponent(image);

        Table table = new Table();
        table.setFillParent(true);
        stack.addComponent(table);

        Image birdImage = new Image(uiSkin, "guidebeeit");
        table.add(birdImage);
        birdImage.addAction(
                forever(
                        sequence(
                                moveTo(150, 100, 3f, Interpolation.circle),
                                delay(1.0f),
                                moveTo(500, 150, 3f, Interpolation.swingIn),
                                delay(1.0f),
                                rotateBy(360f, 2f),
                                delay(1.0f),
                                moveTo(500, 400, 3f, Interpolation.bounce),
                                rotateBy(360f, 2f),
                                delay(1.0f),
                                moveTo(150, 300, 3f, Interpolation.elastic),
                                rotateBy(360f, 2f),
                                delay(1.0f),
                                moveTo(550, 150, 3f, Interpolation.sine)

                        )));


        Button playButton = new Button(uiSkin, "play");
        table.add(playButton).colspan(6);
        playButton.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                gamePlay.setScreen(new FlappyBirdScene(gamePlay));
                return true;
            }
        });

        table.row();
        Button scoreButton = new Button(uiSkin, "score");
        table.add(scoreButton).colspan(6);

        scoreButton.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                gamePlay.setScreen(new ScoreWindow(gamePlay));

                return true;
            }
        });
        table.row();

        Button shopButton = new Button(uiSkin, "shop");

        table.add(shopButton).colspan(6);

        shopButton.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {

                gamePlay.setScreen(new StoreWindow(gamePlay));
                return true;
            }
        });

        table.row();
        Button backButton = new Button(uiSkin, "back");
        backButton.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                gamePlay.finish();
                return true;
            }
        });
        table.add(backButton).colspan(2);

        Button designButton = new Button(uiSkin, "design");
        table.add(designButton).colspan(2);
        designButton.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                gamePlay.setScreen(new DesignWindow(gamePlay));
                return true;
            }
        });

        Button optionButton = new Button(uiSkin, "option");
        table.add(optionButton).colspan(2);
        optionButton.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                gamePlay.setScreen(new OptionWindow(gamePlay));
                return true;
            }
        });


    }

    @Override
    public void show() {
        gamePlay.showBanner();
        super.show();
    }
}

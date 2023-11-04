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
import com.guidebee.game.ui.Label;
import com.guidebee.game.ui.Table;
import com.guidebee.game.ui.Window;

import au.com.guidebee.morsetoolkit.activity.flappybird.FlappyBirdGamePlay;
import au.com.guidebee.morsetoolkit.activity.flappybird.config.Configuration;

//[------------------------------ MAIN CLASS ----------------------------------]

/**
 * Display score window.
 * @author James Shen <james.shen@guidebee.com>
 */
public class ScoreWindow extends BaseWindow {

    /**
     * Constructor.
     * @param gamePlay
     */
    public ScoreWindow(final FlappyBirdGamePlay gamePlay) {
        super(gamePlay);

        Image image = new Image(uiSkin, "dialog_bg");
        image.setFillParent(true);
        stack.addComponent(image);

        Table table = new Table();
        table.setFillParent(true);
        table.pad(10, 45, 0, 0);
        stack.addComponent(table);

        Image score = new Image(uiSkin, "button_score_up");
        table.add(score).colspan(9);
        table.row();
        for (int i = 0; i < 9; i++) {
            table.columnDefaults(i).width(85);
        }

        Image highscore = new Image(uiSkin, "highscore");
        table.add(highscore).colspan(2).left();

        NumberImage highScoreValue = new NumberImage(
                Configuration.userSettings.bestScore, true);
        table.add(highScoreValue).colspan(4).left();

        Button backButton = new Button(uiSkin, "back");

        backButton.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                gamePlay.setScreen(new MainWindow(gamePlay));
                return true;
            }
        });

        table.add(backButton).colspan(3).right();

        spacing(table, 8);

        int remainCount = 20
                - Configuration.userSettings.last20scores.size;
        for (int i = 0; i < remainCount; i++) {
            Configuration.userSettings.last20scores.add(0);
        }

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 3; j++) {
                NumberImage image1 = new NumberImage(i * 3 + j + 1, false);
                table.add(image1);
                NumberImage image2 = new NumberImage(
                        Configuration.userSettings
                                .last20scores.get(i * 3 + j), true);


                table.add(image2).colspan(2);

            }

            spacing(table, 11);

        }


    }

    @Override
    public void show() {
        gamePlay.hideBannder();
        super.show();
    }

    private void spacing(Table table, int count) {
        for (int j = 0; j < count; j++) {
            table.add(new Label("", Window.defaultSkin));
            table.row();
        }
    }
}

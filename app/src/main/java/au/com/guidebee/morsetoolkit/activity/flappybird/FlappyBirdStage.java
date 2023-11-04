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
package au.com.guidebee.morsetoolkit.activity.flappybird;

//--------------------------------- IMPORTS ------------------------------------

import com.guidebee.game.audio.Music;
import com.guidebee.game.camera.viewports.Viewport;
import com.guidebee.game.scene.Group;
import com.guidebee.game.scene.Stage;

import au.com.guidebee.morsetoolkit.activity.flappybird.actor.BackButton;
import au.com.guidebee.morsetoolkit.activity.flappybird.actor.Background;
import au.com.guidebee.morsetoolkit.activity.flappybird.actor.Bird;
import au.com.guidebee.morsetoolkit.activity.flappybird.actor.GameOver;
import au.com.guidebee.morsetoolkit.activity.flappybird.actor.Helper;
import au.com.guidebee.morsetoolkit.activity.flappybird.actor.Playground;
import au.com.guidebee.morsetoolkit.activity.flappybird.actor.StartButton;
import au.com.guidebee.morsetoolkit.activity.flappybird.actor.YouWin;
import au.com.guidebee.morsetoolkit.activity.flappybird.config.Configuration;
import au.com.guidebee.morsetoolkit.activity.flappybird.hud.ChallengeLetter;
import au.com.guidebee.morsetoolkit.activity.flappybird.hud.Score;
import au.com.guidebee.morsetoolkit.activity.flappybird.ui.MainWindow;

import static com.guidebee.game.GameEngine.assetManager;

//[------------------------------ MAIN CLASS ----------------------------------]

/**
 * Flappy bird game stage.
 *
 * @author James Shen <james.shen@guidebee.com>
 */
public class FlappyBirdStage extends Stage {

    private final Bird bird;
    private final Background background;
    private final Playground playground;

    private final Group actorGroup = new Group();
    private final StartButton startButton;
    private final BackButton backButton;
    private final GameOver gameOver;
    private final YouWin youwin;
    private final Score score;
    private final ChallengeLetter challengeLetter;
    private final Music music;

    private final FlappyBirdGamePlay gamePlay;

    private volatile boolean paused = false;


    public FlappyBirdStage(Viewport viewport,
                           FlappyBirdGamePlay gamePlay) {
        super(viewport);
        addActor(actorGroup);

        this.gamePlay = gamePlay;
        bird = new Bird();
        bird.setPosition(-100, -100);
        addActor(bird);

        playground = new Playground();
        addActor(playground);
        playground.toBack();

        background = new Background();
        addActor(background);
        background.toBack();

        startButton = new StartButton(this);
        actorGroup.addActor(startButton);

        backButton = new BackButton(this);
        actorGroup.addActor(backButton);

        gameOver = new GameOver();
        actorGroup.addActor(gameOver);
        actorGroup.toFront();

        youwin = new YouWin();
        actorGroup.addActor(youwin);
        youwin.toFront();

        score = new Score();
        addHUDComponent(score);

        challengeLetter = new ChallengeLetter();
        addHUDComponent(challengeLetter);

        bird.setLive(false);
        playground.setStopMoving(true);
        background.setStopMoving(true);

        music = assetManager.get("music.mp3", Music.class);
        music.setLooping(true);


        challengeLetter.setMorseCode(playground.getChallengeLetters());
        startGame();


    }


    public void removeStartButton() {
        startButton.setVisible(false);
        gameOver.setVisible(false);
    }

    public void startGame() {
        paused = false;
        bird.reset();
        playground.generateLevelData();
        playground.setStopMoving(false);
        background.setStopMoving(false);
        youwin.setVisible(false);
        backButton.setVisible(false);
        Helper.playMusic(music);
    }

    private void GameOver() {
        bird.killBird();
        if (!bird.isLive()) {
            startButton.setVisible(true);
            gameOver.setVisible(true);
            backButton.setVisible(true);
            playground.setStopMoving(true);
            background.setStopMoving(true);
            music.stop();
            Configuration.userSettings.addScore(playground.getScore());
        }
    }


    private void replay() {
        youwin.setVisible(true);
        bird.growBigger();
        startButton.setVisible(true);
        backButton.setVisible(true);
        playground.setStopMoving(true);
        background.setStopMoving(true);
        paused = true;
        music.stop();
    }

    public void pauseGame() {
        paused = true;
        music.stop();
    }


    public void resumeGame() {
        paused = false;
        Helper.playMusic(music);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!paused) {
            if (bird.isLive()) {
                score.setScore(playground.getScore());
                challengeLetter.setMorseCode(playground.getChallengeLetters());
                if (playground.isCollideWithTube(bird)
                        || bird.isOutside()) {
                    GameOver();
                }
            }

            if (playground.reachEndOfTubes()) {
                replay();
                Configuration.userSettings.addScore(playground.getScore());
            }
        }
    }


    public void returnToMainMenu() {
        pauseGame();
        MainWindow mainWindow = new MainWindow(gamePlay);
        gamePlay.setScreen(mainWindow);
    }

}

package au.com.guidebee.morsetoolkit.activity.mario.screen;

import com.guidebee.game.GameEngine;
import com.guidebee.game.ScreenAdapter;
import com.guidebee.game.camera.viewports.ExtendViewport;
import com.guidebee.game.microedition.LayerManager;
import com.guidebee.game.ui.ClickListener;
import com.guidebee.game.ui.InputEvent;
import com.guidebee.game.ui.Label;
import com.guidebee.game.ui.Skin;
import com.guidebee.game.ui.Table;
import com.guidebee.game.ui.TextButton;

import au.com.guidebee.morsetoolkit.activity.mario.MarioConfiguration;
import au.com.guidebee.morsetoolkit.activity.mario.MarioGamePlay;
import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;

/**
 * World/level select - replaces the original engine's {@code LevelNumber == 10}
 * special-cased start screen (see docs/MARIO_PORT_PLAN.md's Step 8 survey:
 * the original re-purposed its own gameplay loop for this, gating
 * {@code initResources}/{@code update}/{@code render} on that one magic
 * level number and mutating two plain ints - {@code StartWorld}/
 * {@code StartLevel} - as its "menu"). This is a real, separate
 * {@code ScreenAdapter}/{@code LayerManager} instead - v1 is World 1 only
 * (see docs/MARIO_PORT_PLAN.md's locked-in scope), so there's exactly one
 * world's worth of levels to choose from: {@link #LEVEL_NUMBERS} (11-14, i.e.
 * "1-1".."1-4").
 *
 * <p>Built from plain {@code Table}/{@code Label}/{@code TextButton} widgets
 * against the engine's bundled {@code skin/default} (see
 * {@code MarioResourceManager#uiSkin()}) - this app has no existing
 * precedent for an in-engine (as opposed to the host app's own Compose
 * {@code HomeScreen}) menu screen to follow, so this doesn't reuse a
 * pattern from Battle City/Flappy Bird the way {@code MarioGameScreen}'s HUD
 * controls do.
 *
 * <p>Deliberately not {@code setFillParent(true)}: {@code MarioGameScreen}'s
 * own {@code GameController} needed {@code setGameController} instead of
 * plain {@code addHUDComponent} for exactly this reason -
 * {@code getParent().getWidth()} doesn't reliably resolve to anything useful
 * for a widget parented that way in this engine. An explicit
 * {@link #table}{@code .setSize(...)} sidesteps the same pitfall here.
 */
public class MarioMenuScreen extends ScreenAdapter {

    private static final int[] LEVEL_NUMBERS = {11, 12, 13, 14};
    private static final String[] LEVEL_LABELS = {"1-1", "1-2", "1-3", "1-4"};

    private final LayerManager layerManager;
    private final Table table;

    public MarioMenuScreen(MarioGamePlay gamePlay) {
        layerManager = new LayerManager(new ExtendViewport(
                MarioConfiguration.VIEWPORT_WIDTH, MarioConfiguration.VIEWPORT_HEIGHT));

        Skin skin = MarioResourceManager.uiSkin();
        table = new Table();

        table.add(new Label("SUPER MARIO BROS", skin)).padBottom(8f).row();
        table.add(new Label("WORLD 1", skin)).padBottom(24f).row();

        for (int i = 0; i < LEVEL_NUMBERS.length; i++) {
            int levelNumber = LEVEL_NUMBERS[i];
            TextButton button = new TextButton(LEVEL_LABELS[i], skin);
            button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    gamePlay.startLevel(levelNumber);
                }
            });
            table.add(button).width(140f).height(44f).padBottom(10f).row();
        }

        TextButton exitButton = new TextButton("EXIT", skin);
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                gamePlay.finish();
            }
        });
        table.add(exitButton).width(140f).height(44f).padTop(14f);

        // Not setSize/fillParent - see the class doc. pack() sizes the Table
        // from its own content, so render() can center it without depending
        // on getParent().getWidth().
        table.pack();
        layerManager.addHUDComponent(table);
    }

    @Override
    public void show() {
        GameEngine.input.setInputProcessor(layerManager);
    }

    @Override
    public void resize(int width, int height) {
        layerManager.getViewport().update(width, height, true);
    }

    @Override
    public void render(float delta) {
        // Centers the table over whatever the viewport actually extended to
        // (see MarioGameScreen's own ExtendViewport note) - simple enough to
        // just recompute every frame rather than caching/invalidating.
        float viewportWidth = layerManager.getViewport().getWorldWidth();
        float viewportHeight = layerManager.getViewport().getWorldHeight();
        table.setPosition((viewportWidth - table.getWidth()) / 2f, (viewportHeight - table.getHeight()) / 2f);

        layerManager.act(delta);
        GameEngine.graphics.clearScreen(0f, 0f, 0f, 1f);
        layerManager.draw();
    }
}

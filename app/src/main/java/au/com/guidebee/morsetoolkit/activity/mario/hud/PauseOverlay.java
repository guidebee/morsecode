package au.com.guidebee.morsetoolkit.activity.mario.hud;

import com.guidebee.game.microedition.LayerManager;
import com.guidebee.game.ui.ClickListener;
import com.guidebee.game.ui.InputEvent;
import com.guidebee.game.ui.Label;
import com.guidebee.game.ui.Skin;
import com.guidebee.game.ui.Table;
import com.guidebee.game.ui.TextButton;

/**
 * A small centered "PAUSED" panel with RESUME/QUIT buttons, shown while
 * {@code GameStateController} is paused (toggled via {@code MarioGameScreen}'s
 * back button - see its class doc) and hidden otherwise. Like
 * {@link ScoreHud}, its {@link #reposition} must be called every frame to
 * stay screen-anchored through the shared, moving/zooming world camera.
 */
public class PauseOverlay {

    private final Table table;

    public PauseOverlay(LayerManager layerManager, Skin skin, Runnable onResume, Runnable onQuit) {
        table = new Table();

        Label title = new Label("PAUSED", skin);
        TextButton resumeButton = new TextButton("RESUME", skin);
        resumeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onResume.run();
            }
        });
        TextButton quitButton = new TextButton("QUIT TO MENU", skin);
        quitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onQuit.run();
            }
        });

        table.add(title).padBottom(16f).row();
        table.add(resumeButton).width(160f).height(36f).padBottom(8f).row();
        table.add(quitButton).width(160f).height(36f);
        // Not fillParent - see MarioGameScreen's own note on GameController's
        // layout() needing setGameController for getParent().getWidth() to
        // resolve; pack() instead sizes this Table from its own content, so
        // reposition() below can center it without depending on that.
        table.pack();
        table.setVisible(false);

        layerManager.addHUDComponent(table);
    }

    public void show() {
        table.setVisible(true);
    }

    public void hide() {
        table.setVisible(false);
    }

    /** See the class doc; {@code screenLeft/Top/Width/Height} and {@code zoom} match {@link ScoreHud#reposition}'s. */
    public void reposition(float screenLeft, float screenTop, float screenWidth, float screenHeight, float zoom) {
        table.setScale(zoom);
        table.setPosition(
                screenLeft + screenWidth / 2f - table.getWidth() * zoom / 2f,
                screenTop + screenHeight / 2f - table.getHeight() * zoom / 2f);
    }
}

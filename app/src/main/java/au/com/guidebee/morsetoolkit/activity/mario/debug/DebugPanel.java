package au.com.guidebee.morsetoolkit.activity.mario.debug;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import com.guidebee.game.microedition.LayerManager;
import com.guidebee.game.ui.CheckBox;
import com.guidebee.game.ui.ClickListener;
import com.guidebee.game.ui.InputEvent;
import com.guidebee.game.ui.Label;
import com.guidebee.game.ui.Skin;
import com.guidebee.game.ui.Table;
import com.guidebee.game.ui.TextButton;
import com.guidebee.game.ui.TextField;

import au.com.guidebee.morsetoolkit.activity.mario.MarioConfiguration;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.level.LevelDefinition;

/**
 * The in-level warp/cheat panel from docs/MARIO_PORT_PLAN_PHASE2.md §8.3.2-
 * §8.3.6 - only ever constructed behind {@code BuildConfig.DEBUG} (see
 * {@code MarioGameScreen}, which owns showing/hiding it via its own
 * debug-only corner button, and owns the infinite-lives/time-scale state
 * this panel's toggles report back to via the constructor's callbacks,
 * since both gate behavior that lives in {@code MarioGameScreen#render}/
 * {@code #handlePlayerDeath}, not here).
 *
 * <p>Every warp button (one per {@link LevelDefinition.Checkpoint}, plus one
 * per "interesting" {@link LevelDefinition.Tile} - see {@link #INTERESTING_TILE_TYPES})
 * is generated straight from the current level's own already-parsed data, so
 * there's nothing to hand-author or keep in sync as levels change; the
 * manual tile-X/Y field below them covers anywhere else. A warp is just
 * {@code player.setPosition(...)} plus a brief {@link #WARP_GRACE_SECONDS}
 * invincibility window (so teleporting into a wall or next to an enemy isn't
 * an instant, confusing death) - no level reload, so already-defeated
 * enemies/collected coins stay exactly as they were.
 *
 * <p>Like every other HUD element in {@code MarioGameScreen} (see its class
 * doc), this renders through the shared, moving/zooming world camera, so
 * {@link #reposition} must be called every frame to stay screen-anchored;
 * {@link #update} (also called every frame) keeps the live "Mario is
 * currently at tile (x,y)" readout current while the panel is open.
 */
public class DebugPanel {

    /** Ported from §8.3.2's own list - every non-checkpoint tile type worth standing next to for a quick look. */
    private static final Set<String> INTERESTING_TILE_TYPES = new HashSet<>(Arrays.asList(
            "Flag", "Axe", "Boss", "BossHammer", "Bouncer", "FlyingTurtlePatrol", "Monkey"));

    /** Comfortably longer than a warp's own landing settle - see {@code Player#setInvincibleFor}. */
    private static final float WARP_GRACE_SECONDS = 2f;

    private static final float BUTTON_WIDTH = 150f;
    private static final float BUTTON_HEIGHT = 24f;

    private final Table table;
    private final Player player;
    private final Label coordinateLabel;
    private final TextButton timeScaleButton;

    private int timeScale = 1;

    public DebugPanel(LayerManager layerManager, Skin skin, LevelDefinition level, Player player,
                       Consumer<Boolean> onInfiniteLivesToggle, IntConsumer onTimeScaleChange) {
        this.player = player;
        table = new Table();

        table.add(new Label("DEBUG PANEL", skin)).colspan(2).padBottom(4f).row();
        coordinateLabel = new Label("", skin);
        table.add(coordinateLabel).colspan(2).width(BUTTON_WIDTH * 2f).padBottom(6f).row();

        CheckBox godModeBox = new CheckBox("GOD MODE", skin);
        godModeBox.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                player.setDebugInvincible(godModeBox.isChecked());
            }
        });
        table.add(godModeBox).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(4f);

        CheckBox infiniteLivesBox = new CheckBox("INF LIVES", skin);
        infiniteLivesBox.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onInfiniteLivesToggle.accept(infiniteLivesBox.isChecked());
            }
        });
        table.add(infiniteLivesBox).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(4f).row();

        TextButton powerButton = new TextButton("CYCLE POWER", skin);
        powerButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                player.debugCyclePowerState();
            }
        });
        table.add(powerButton).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(6f);

        timeScaleButton = new TextButton("SPEED 1x", skin);
        timeScaleButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // 1x -> 2x -> 4x -> 1x - see MarioGameScreen#render's own
                // "run the frame's update multiple times" note (§8.3.6).
                timeScale = timeScale >= 4 ? 1 : timeScale * 2;
                timeScaleButton.setText("SPEED " + timeScale + "x");
                onTimeScaleChange.accept(timeScale);
            }
        });
        table.add(timeScaleButton).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(6f).row();

        int column = 0;
        for (LevelDefinition.Checkpoint checkpoint : level.checkpoints) {
            table.add(warpButton(skin, checkpoint.kind, (float) checkpoint.x, (float) checkpoint.y))
                    .width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(2f).padRight(2f);
            column++;
            if (column % 2 == 0) {
                table.row();
            }
        }
        for (LevelDefinition.Tile tile : level.tiles) {
            if (!INTERESTING_TILE_TYPES.contains(tile.type)) {
                continue;
            }
            float worldX = tile.x * MarioConfiguration.TILE_SIZE;
            float worldY = tile.y * MarioConfiguration.TILE_SIZE;
            table.add(warpButton(skin, tile.type, worldX, worldY))
                    .width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(2f).padRight(2f);
            column++;
            if (column % 2 == 0) {
                table.row();
            }
        }
        if (column % 2 != 0) {
            table.row();
        }

        TextField tileXField = new TextField("", skin);
        tileXField.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
        TextField tileYField = new TextField("", skin);
        tileYField.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
        table.add(new Label("Tile X/Y", skin)).padTop(4f);
        Table tileFields = new Table();
        tileFields.add(tileXField).width(60f);
        tileFields.add(tileYField).width(60f).padLeft(4f);
        table.add(tileFields).padTop(4f).row();

        TextButton manualWarpButton = new TextButton("WARP TO TILE", skin);
        manualWarpButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                warpToTile(tileXField.getText(), tileYField.getText());
            }
        });
        table.add(manualWarpButton).colspan(2).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padTop(4f).padBottom(6f).row();

        TextButton closeButton = new TextButton("CLOSE", skin);
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                hide();
            }
        });
        table.add(closeButton).colspan(2).width(BUTTON_WIDTH).height(BUTTON_HEIGHT);

        table.pack();
        table.setVisible(false);
        layerManager.addHUDComponent(table);
    }

    private TextButton warpButton(Skin skin, String label, float worldX, float worldY) {
        int tileX = (int) (worldX / MarioConfiguration.TILE_SIZE);
        int tileY = (int) (worldY / MarioConfiguration.TILE_SIZE);
        TextButton button = new TextButton(label + " (" + tileX + "," + tileY + ")", skin);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                warpTo(worldX, worldY);
            }
        });
        return button;
    }

    private void warpToTile(String tileXText, String tileYText) {
        try {
            int tileX = Integer.parseInt(tileXText.trim());
            int tileY = Integer.parseInt(tileYText.trim());
            warpTo(tileX * MarioConfiguration.TILE_SIZE, tileY * MarioConfiguration.TILE_SIZE);
        } catch (NumberFormatException ignored) {
            // Empty/partially-typed field - no-op rather than a crash.
        }
    }

    private void warpTo(float worldX, float worldY) {
        player.setPosition(worldX, worldY);
        player.setInvincibleFor(WARP_GRACE_SECONDS);
    }

    public void toggle() {
        table.setVisible(!table.isVisible());
    }

    public void hide() {
        table.setVisible(false);
    }

    public boolean isVisible() {
        return table.isVisible();
    }

    /** Refreshes the live coordinate readout - a no-op while the panel is closed. */
    public void update() {
        if (!table.isVisible()) {
            return;
        }
        int tileX = (int) (player.getX() / MarioConfiguration.TILE_SIZE);
        int tileY = (int) (player.getY() / MarioConfiguration.TILE_SIZE);
        coordinateLabel.setText("Mario tile: (" + tileX + "," + tileY + ")");
    }

    /** Anchors this panel to the screen's top-left corner - see the class doc. */
    public void reposition(float screenLeft, float screenTop, float zoom) {
        table.setScale(zoom);
        table.setPosition(screenLeft + 4f * zoom, screenTop + 4f * zoom);
    }
}

package au.com.guidebee.morsetoolkit.activity.mario.debug;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
    private static final float BUTTON_HEIGHT = 20f;
    private static final float ROW_PAD = 2f;

    private final Table table;
    private final Player player;
    private final Label coordinateLabel;
    private final TextButton timeScaleButton;
    private final int tileSize;

    private int timeScale = 1;

    public DebugPanel(LayerManager layerManager, Skin skin, LevelDefinition level, Player player,
                       Consumer<Boolean> onInfiniteLivesToggle, IntConsumer onTimeScaleChange, int tileSize) {
        this.player = player;
        this.tileSize = tileSize;
        // Both built up front, before any of the row-building lambdas below
        // that reference them (a blank final field/local can't be read - even
        // from inside a lambda - anywhere in the constructor before it's
        // definitely assigned).
        table = new Table();
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

        // Built as a list of "rows" (each a Runnable that adds one row's own
        // cells, ending in its own .row()) in the INTENDED top-to-bottom
        // visual order, then applied to `table` in REVERSE - see this
        // constructor's own trailing loop. Table's row stacking renders the
        // *first*-added row at the *bottom* under this screen's y-down camera
        // (confirmed on-device: PauseOverlay's own title/RESUME/QUIT rows
        // render bottom-to-top the exact same way, just never flagged before
        // since a 3-line pause menu still reads fine either way - a much
        // longer panel like this one doesn't).
        List<Runnable> rows = new ArrayList<>();

        rows.add(() -> table.add(new Label("DEBUG PANEL", skin)).colspan(2).padBottom(ROW_PAD).row());

        coordinateLabel = new Label("", skin);
        rows.add(() -> table.add(coordinateLabel).colspan(2).width(BUTTON_WIDTH * 2f).padBottom(ROW_PAD).row());

        rows.add(() -> {
            CheckBox godModeBox = new CheckBox("GOD MODE", skin);
            godModeBox.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    player.setDebugInvincible(godModeBox.isChecked());
                }
            });
            table.add(godModeBox).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(ROW_PAD);

            CheckBox infiniteLivesBox = new CheckBox("INF LIVES", skin);
            infiniteLivesBox.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    onInfiniteLivesToggle.accept(infiniteLivesBox.isChecked());
                }
            });
            table.add(infiniteLivesBox).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(ROW_PAD).row();
        });

        rows.add(() -> {
            TextButton powerButton = new TextButton("CYCLE POWER", skin);
            powerButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    player.debugCyclePowerState();
                }
            });
            table.add(powerButton).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(ROW_PAD);

            table.add(timeScaleButton).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(ROW_PAD).row();
        });

        List<TextButton> warpButtons = new ArrayList<>();
        for (LevelDefinition.Checkpoint checkpoint : level.checkpoints) {
            warpButtons.add(warpButton(skin, checkpoint.kind, (float) checkpoint.x, (float) checkpoint.y));
        }
        for (LevelDefinition.Tile tile : level.tiles) {
            if (!INTERESTING_TILE_TYPES.contains(tile.type)) {
                continue;
            }
            warpButtons.add(warpButton(skin, tile.type,
                    tile.x * tileSize, tile.y * tileSize));
        }
        // Two per row, in the SAME left-to-right/top-to-bottom reading order
        // regardless of the reversal above - only whole *rows* flip, not the
        // buttons within one, so pairing them up before reversing (rather
        // than relying on some later row-by-row column-parity counter) keeps
        // e.g. buttons 1-2 as row one, 3-4 as row two, both before reversal
        // and after.
        for (int i = 0; i < warpButtons.size(); i += 2) {
            TextButton first = warpButtons.get(i);
            TextButton second = i + 1 < warpButtons.size() ? warpButtons.get(i + 1) : null;
            rows.add(() -> {
                table.add(first).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(ROW_PAD).padRight(ROW_PAD);
                if (second != null) {
                    table.add(second).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(ROW_PAD);
                }
                table.row();
            });
        }

        TextField tileXField = new TextField("", skin);
        tileXField.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
        TextField tileYField = new TextField("", skin);
        tileYField.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
        rows.add(() -> {
            table.add(new Label("Tile X/Y", skin)).padBottom(ROW_PAD);
            Table tileFields = new Table();
            tileFields.add(tileXField).width(60f);
            tileFields.add(tileYField).width(60f).padLeft(ROW_PAD);
            table.add(tileFields).padBottom(ROW_PAD).row();
        });

        rows.add(() -> {
            TextButton manualWarpButton = new TextButton("WARP TO TILE", skin);
            manualWarpButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    warpToTile(tileXField.getText(), tileYField.getText());
                }
            });
            table.add(manualWarpButton).colspan(2).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(ROW_PAD).row();
        });

        rows.add(() -> {
            TextButton closeButton = new TextButton("CLOSE", skin);
            closeButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    hide();
                }
            });
            table.add(closeButton).colspan(2).width(BUTTON_WIDTH).height(BUTTON_HEIGHT);
        });

        for (int i = rows.size() - 1; i >= 0; i--) {
            rows.get(i).run();
        }

        table.pack();
        table.setVisible(false);
        layerManager.addHUDComponent(table);
    }

    private TextButton warpButton(Skin skin, String label, float worldX, float worldY) {
        int tileX = (int) (worldX / tileSize);
        int tileY = (int) (worldY / tileSize);
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
            warpTo(tileX * tileSize, tileY * tileSize);
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
        int tileX = (int) (player.getX() / tileSize);
        int tileY = (int) (player.getY() / tileSize);
        coordinateLabel.setText("Mario tile: (" + tileX + "," + tileY + ")");
    }

    /** Anchors this panel to the screen's top-left corner - see the class doc. */
    public void reposition(float screenLeft, float screenTop, float zoom) {
        table.setScale(zoom);
        table.setPosition(screenLeft + 4f * zoom, screenTop + 4f * zoom);
    }
}

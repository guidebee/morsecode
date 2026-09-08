package au.com.guidebee.morsetoolkit.platformer.debug;

import java.util.ArrayList;
import java.util.List;

import com.guidebee.game.microedition.LayerManager;
import com.guidebee.game.ui.ClickListener;
import com.guidebee.game.ui.InputEvent;
import com.guidebee.game.ui.Label;
import com.guidebee.game.ui.Skin;
import com.guidebee.game.ui.Table;
import com.guidebee.game.ui.TextButton;
import com.guidebee.game.ui.TextField;

import au.com.guidebee.morsetoolkit.activity.mario.level.LevelDefinition;
import au.com.guidebee.morsetoolkit.platformer.actor.PowerStateActor;

/**
 * The in-level warp/cheat panel - generalized from Mario's own {@code
 * DebugPanel} (see PLATFORMER_ENGINE_ARCHITECTURE.md §3.7). The core idea -
 * "generate warp buttons from whatever's already in this level's own
 * checkpoints/tiles, no per-level authoring" - was already written
 * generically enough to lift almost unchanged; the only per-game input is
 * {@code interestingTileTypes} (which non-checkpoint tile types are worth a
 * warp button) plus, since god-mode/power-cycling/infinite-lives/speed
 * toggles are genuinely Mario-specific *concepts* even though the *pattern*
 * ("a labeled toggle wired to a callback") generalizes, an {@code extraRows}
 * list a game supplies to insert its own such rows between the coordinate
 * readout and the data-driven warp buttons - see {@link RowBuilder}.
 *
 * <p><b>Known scope compromise:</b> like {@code platformer.level.TileHandler}
 * (see that class's own doc), this still imports Mario's own {@link
 * LevelDefinition} directly rather than a fully generalized level schema -
 * out of this implementation plan's declared scope (a real second consumer
 * is Phase H, run on its own schedule).
 *
 * <p>A warp is just {@code player.setPosition(...)} plus a brief grace-period
 * invincibility window (so teleporting into a wall or next to an enemy isn't
 * an instant, confusing death) - no level reload, so already-defeated
 * enemies/collected coins stay exactly as they were. Renders through the
 * shared, moving/zooming world camera, so {@link #reposition} must be called
 * every frame to stay screen-anchored; {@link #update} (also called every
 * frame) keeps the live coordinate readout current while the panel is open.
 */
public class LevelWarpPanel {

    /** One extra row a game wants inserted between the coordinate readout and the data-driven warp buttons - see this class's own doc. */
    @FunctionalInterface
    public interface RowBuilder {
        void build(Table table, Skin skin);
    }

    protected static final float BUTTON_WIDTH = 150f;
    protected static final float BUTTON_HEIGHT = 20f;
    protected static final float ROW_PAD = 2f;

    private final Table table;
    private final PowerStateActor<?> player;
    private final Label coordinateLabel;
    private final int tileSize;
    private final float warpGraceSeconds;

    public LevelWarpPanel(LayerManager layerManager, Skin skin, String title, LevelDefinition level,
                           PowerStateActor<?> player, List<String> interestingTileTypes,
                           int tileSize, float warpGraceSeconds, List<RowBuilder> extraRows) {
        this.player = player;
        this.tileSize = tileSize;
        this.warpGraceSeconds = warpGraceSeconds;

        // Both built up front, before any of the row-building lambdas below
        // that reference them (a blank final field/local can't be read - even
        // from inside a lambda - anywhere in the constructor before it's
        // definitely assigned).
        table = new Table();

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

        rows.add(() -> table.add(new Label(title, skin)).colspan(2).padBottom(ROW_PAD).row());

        coordinateLabel = new Label("", skin);
        rows.add(() -> table.add(coordinateLabel).colspan(2).width(BUTTON_WIDTH * 2f).padBottom(ROW_PAD).row());

        for (RowBuilder extraRow : extraRows) {
            rows.add(() -> extraRow.build(table, skin));
        }

        List<TextButton> warpButtons = new ArrayList<>();
        for (LevelDefinition.Checkpoint checkpoint : level.checkpoints) {
            warpButtons.add(warpButton(skin, checkpoint.kind, (float) checkpoint.x, (float) checkpoint.y));
        }
        for (LevelDefinition.Tile tile : level.tiles) {
            if (!interestingTileTypes.contains(tile.type)) {
                continue;
            }
            warpButtons.add(warpButton(skin, tile.type, tile.x * tileSize, tile.y * tileSize));
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
        player.setInvincibleFor(warpGraceSeconds);
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
        coordinateLabel.setText(coordinateText(tileX, tileY));
    }

    /** Override to customize the live coordinate readout's wording (default: "Player tile: (x,y)"). */
    protected String coordinateText(int tileX, int tileY) {
        return "Player tile: (" + tileX + "," + tileY + ")";
    }

    /** Anchors this panel to the screen's top-left corner - see the class doc. */
    public void reposition(float screenLeft, float screenTop, float zoom) {
        table.setScale(zoom);
        table.setPosition(screenLeft + 4f * zoom, screenTop + 4f * zoom);
    }
}

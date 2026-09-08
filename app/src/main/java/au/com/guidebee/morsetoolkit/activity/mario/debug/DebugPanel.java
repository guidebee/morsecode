package au.com.guidebee.morsetoolkit.activity.mario.debug;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import com.guidebee.game.microedition.LayerManager;
import com.guidebee.game.ui.CheckBox;
import com.guidebee.game.ui.ClickListener;
import com.guidebee.game.ui.InputEvent;
import com.guidebee.game.ui.Skin;
import com.guidebee.game.ui.TextButton;

import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.level.LevelDefinition;
import au.com.guidebee.morsetoolkit.platformer.debug.LevelWarpPanel;

/**
 * The in-level warp/cheat panel from docs/MARIO_PORT_PLAN_PHASE2.md §8.3.2-
 * §8.3.6 - only ever constructed behind {@code BuildConfig.DEBUG} (see
 * {@code MarioGameScreen}, which owns showing/hiding it via its own
 * debug-only corner button, and owns the infinite-lives/time-scale state
 * this panel's toggles report back to via the constructor's callbacks,
 * since both gate behavior that lives in {@code MarioGameScreen#render}/
 * {@code #handlePlayerDeath}, not here).
 *
 * <p>Now a thin construction call into {@link LevelWarpPanel} (see
 * PLATFORMER_ENGINE_ARCHITECTURE.md §3.7) - this class supplies only what's
 * genuinely Mario's own: {@link #INTERESTING_TILE_TYPES} and the GOD MODE/
 * INF LIVES/CYCLE POWER/SPEED toggle row (god mode and power-state cycling
 * are Mario-{@code Player}-specific concepts, even though the *pattern* - a
 * labeled toggle wired to a callback - generalizes; see {@code
 * LevelWarpPanel}'s own doc).
 */
public class DebugPanel extends LevelWarpPanel {

    /** Ported from §8.3.2's own list - every non-checkpoint tile type worth standing next to for a quick look. */
    private static final List<String> INTERESTING_TILE_TYPES = Arrays.asList(
            "Flag", "Axe", "Boss", "BossHammer", "Bouncer", "FlyingTurtlePatrol", "Monkey");

    /** Comfortably longer than a warp's own landing settle - see {@code Player#setInvincibleFor}. */
    private static final float WARP_GRACE_SECONDS = 2f;

    public DebugPanel(LayerManager layerManager, Skin skin, LevelDefinition level, Player player,
                       Consumer<Boolean> onInfiniteLivesToggle, IntConsumer onTimeScaleChange, int tileSize) {
        super(layerManager, skin, "DEBUG PANEL", level, player, INTERESTING_TILE_TYPES, tileSize,
                WARP_GRACE_SECONDS, buildExtraRows(player, onInfiniteLivesToggle, onTimeScaleChange));
    }

    /**
     * Builds the GOD MODE/INF LIVES/CYCLE POWER/SPEED row pair as {@link
     * RowBuilder}s - a static helper (not instance state) since {@link
     * LevelWarpPanel}'s own constructor needs this list *before* {@code
     * super(...)} can finish, at which point no instance field of this class
     * exists yet to reference. {@code timeScale}'s own mutable state lives
     * in a one-element array captured by the row's closure instead of a
     * field, for the same reason.
     */
    private static List<RowBuilder> buildExtraRows(Player player, Consumer<Boolean> onInfiniteLivesToggle,
                                                     IntConsumer onTimeScaleChange) {
        List<RowBuilder> extraRows = new ArrayList<>();

        extraRows.add((table, skin) -> {
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

        extraRows.add((table, skin) -> {
            TextButton powerButton = new TextButton("CYCLE POWER", skin);
            powerButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    player.debugCyclePowerState();
                }
            });
            table.add(powerButton).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(ROW_PAD);

            int[] timeScale = {1};
            TextButton timeScaleButton = new TextButton("SPEED 1x", skin);
            timeScaleButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    // 1x -> 2x -> 4x -> 1x - see MarioGameScreen#render's own
                    // "run the frame's update multiple times" note (§8.3.6).
                    timeScale[0] = timeScale[0] >= 4 ? 1 : timeScale[0] * 2;
                    timeScaleButton.setText("SPEED " + timeScale[0] + "x");
                    onTimeScaleChange.accept(timeScale[0]);
                }
            });
            table.add(timeScaleButton).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(ROW_PAD).row();
        });

        return extraRows;
    }

    @Override
    protected String coordinateText(int tileX, int tileY) {
        return "Mario tile: (" + tileX + "," + tileY + ")";
    }
}

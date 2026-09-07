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

import au.com.guidebee.morsetoolkit.activity.BuildConfig;
import au.com.guidebee.morsetoolkit.activity.mario.MarioConfiguration;
import au.com.guidebee.morsetoolkit.activity.mario.MarioGamePlay;
import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.level.LevelNumbering;
import au.com.guidebee.morsetoolkit.activity.mario.state.MarioSaveState;

/**
 * World-select -> per-world level-select, replacing the original engine's
 * {@code LevelNumber == 10} special-cased start screen (see
 * docs/MARIO_PORT_PLAN.md's Step 8 survey: the original re-purposed its own
 * gameplay loop for this, gating {@code initResources}/{@code update}/
 * {@code render} on that one magic level number and mutating two plain ints -
 * {@code StartWorld}/{@code StartLevel} - as its "menu"). This is a real,
 * separate {@code ScreenAdapter}/{@code LayerManager} instead, folded into
 * one class per docs/MARIO_PORT_PLAN_PHASE2.md Step P2.2.2's own note that a
 * separate {@code MarioWorldSelectScreen} isn't needed - {@link #selectedWorld}
 * just toggles which {@link Table} of buttons {@link #buildTable} produces,
 * with a "BACK" button returning {@code null} to it.
 *
 * <p>A level button is disabled (see {@link #isUnlocked}) until the previous
 * level in its world - or, for a world's first level, the previous world's
 * last level - is cleared (see {@link MarioSaveState}); World 1's own first
 * level is always unlocked, there being no earlier level to require. In a
 * debug build ({@link BuildConfig#DEBUG}), every level is selectable instead
 * - see {@link #buildLevelList}'s own note - for
 * docs/MARIO_PORT_PLAN_PHASE2.md §8.3.1's warp tooling; {@link MarioSaveState}
 * itself is never written any differently either way.
 * Deliberately no lock on *entering* a world's level list itself (browsing
 * ahead is harmless) - only individual level buttons gate on progress.
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

    /** @see LevelNumbering#WORLD_LEVELS */
    private static final int[][] WORLD_LEVELS = LevelNumbering.WORLD_LEVELS;

    private final LayerManager layerManager;
    private final MarioGamePlay gamePlay;

    private Table table;
    /** 0-based index into {@link #WORLD_LEVELS}, or -1 for the world-grid screen. */
    private int selectedWorld = -1;

    public MarioMenuScreen(MarioGamePlay gamePlay) {
        this.gamePlay = gamePlay;
        layerManager = new LayerManager(new ExtendViewport(
                MarioConfiguration.VIEWPORT_WIDTH, MarioConfiguration.VIEWPORT_HEIGHT));
        rebuildTable();
    }

    /** Rebuilds {@link #table} from scratch for whichever screen {@link #selectedWorld} currently selects - simpler than mutating a live Table's children in place for a menu this small. */
    private void rebuildTable() {
        if (table != null) {
            table.remove();
        }
        table = selectedWorld < 0 ? buildWorldGrid() : buildLevelList(selectedWorld);
        table.pack();
        layerManager.addHUDComponent(table);
    }

    private Table buildWorldGrid() {
        Skin skin = MarioResourceManager.uiSkin();
        Table table = new Table();
        table.add(new Label("SUPER MARIO BROS", skin)).padBottom(8f).colspan(2).row();
        table.add(new Label("SELECT A WORLD", skin)).padBottom(24f).colspan(2).row();

        for (int world = 0; world < WORLD_LEVELS.length; world++) {
            int worldNumber = world + 1;
            TextButton button = new TextButton("WORLD " + worldNumber, skin);
            button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    selectedWorld = worldNumber - 1;
                    rebuildTable();
                }
            });
            table.add(button).width(140f).height(44f).padBottom(10f);
            if (world % 2 == 1) {
                table.row();
            }
        }

        TextButton exitButton = new TextButton("EXIT", skin);
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                gamePlay.finish();
            }
        });
        table.row();
        table.add(exitButton).width(140f).height(44f).padTop(14f).colspan(2);
        return table;
    }

    private Table buildLevelList(int world) {
        Skin skin = MarioResourceManager.uiSkin();
        int[] levels = WORLD_LEVELS[world];
        Table table = new Table();
        table.add(new Label("WORLD " + (world + 1), skin)).padBottom(24f).row();

        for (int levelNumber : levels) {
            TextButton button = new TextButton(LevelNumbering.label(levelNumber), skin);
            boolean reallyUnlocked = isUnlocked(world, levelNumber);
            // Debug builds only (see docs/MARIO_PORT_PLAN_PHASE2.md §8.3.1):
            // every level is selectable regardless of MarioSaveState's real
            // clear tracking, which is left untouched either way (see that
            // section's own note on why this never writes to it) - a distinct
            // orange tint (not the normal locked grey, not the normal
            // unlocked look) marks a level that's only reachable this way, so
            // a debug build never looks indistinguishable from a real
            // unlocked/locked state.
            boolean unlocked = reallyUnlocked || BuildConfig.DEBUG;
            button.setDisabled(!unlocked);
            if (!reallyUnlocked) {
                if (BuildConfig.DEBUG) {
                    button.setColor(1f, 0.6f, 0.2f, 1f);
                } else {
                    button.setColor(0.5f, 0.5f, 0.5f, 1f);
                }
            }
            button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    gamePlay.startLevel(levelNumber);
                }
            });
            table.add(button).width(140f).height(44f).padBottom(10f).row();
        }

        TextButton backButton = new TextButton("BACK", skin);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                selectedWorld = -1;
                rebuildTable();
            }
        });
        table.add(backButton).width(140f).height(44f).padTop(14f);
        return table;
    }

    /** World 1's own first level needs no prior clear; every other level needs the one immediately before it (in its own world, or the previous world's last level) cleared. */
    private static boolean isUnlocked(int world, int levelNumber) {
        int[] levels = WORLD_LEVELS[world];
        if (levelNumber == levels[0]) {
            if (world == 0) {
                return true;
            }
            int[] previousWorldLevels = WORLD_LEVELS[world - 1];
            return MarioSaveState.isCleared(previousWorldLevels[previousWorldLevels.length - 1]);
        }
        for (int i = 1; i < levels.length; i++) {
            if (levels[i] == levelNumber) {
                return MarioSaveState.isCleared(levels[i - 1]);
            }
        }
        return false;
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

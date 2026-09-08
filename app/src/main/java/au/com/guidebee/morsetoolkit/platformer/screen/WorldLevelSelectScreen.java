package au.com.guidebee.morsetoolkit.platformer.screen;

import java.util.function.Consumer;
import java.util.function.IntFunction;

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

/**
 * World-select -> per-world level-select, generalized from Mario's own
 * {@code MarioMenuScreen} - see PLATFORMER_ENGINE_ARCHITECTURE.md §3.6. A
 * level button is disabled until {@link LevelUnlockPolicy#isUnlocked} says
 * otherwise - the exact "previous level/world must be cleared" rule is the
 * game's own policy, read generically here. {@code selectedWorld} just
 * toggles which {@link Table} of buttons {@link #buildTable} produces, with
 * a "BACK" button returning to the world grid.
 *
 * <p>{@code debugBypassUnlock} replaces Mario's own {@code BuildConfig.DEBUG}
 * check - every level becomes selectable (tinted orange, not the normal
 * locked grey) regardless of the real unlock policy, without this toolkit
 * class needing to know what a "debug build" is for this game.
 *
 * <p>Built from plain {@code Table}/{@code Label}/{@code TextButton} widgets
 * against the caller's own {@code Skin}. Deliberately not
 * {@code setFillParent(true)} - {@code getParent().getWidth()} doesn't
 * reliably resolve to anything useful for a widget parented that way in this
 * engine; an explicit {@link #table}{@code .setSize(...)} (via {@code pack()})
 * sidesteps the same pitfall here.
 */
public class WorldLevelSelectScreen extends ScreenAdapter {

    private final int[][] worldLevels;
    private final LevelUnlockPolicy unlockPolicy;
    private final IntFunction<String> levelLabeler;
    private final String title;
    private final Skin skin;
    private final Consumer<Integer> onLevelSelected;
    private final Runnable onExit;
    private final boolean debugBypassUnlock;
    private final LayerManager layerManager;

    private Table table;
    /** 0-based index into {@link #worldLevels}, or -1 for the world-grid screen. */
    private int selectedWorld = -1;

    public WorldLevelSelectScreen(int[][] worldLevels, LevelUnlockPolicy unlockPolicy,
                                   IntFunction<String> levelLabeler, String title,
                                   int viewportWidth, int viewportHeight, Skin skin,
                                   Consumer<Integer> onLevelSelected, Runnable onExit,
                                   boolean debugBypassUnlock) {
        this.worldLevels = worldLevels;
        this.unlockPolicy = unlockPolicy;
        this.levelLabeler = levelLabeler;
        this.title = title;
        this.skin = skin;
        this.onLevelSelected = onLevelSelected;
        this.onExit = onExit;
        this.debugBypassUnlock = debugBypassUnlock;
        layerManager = new LayerManager(new ExtendViewport(viewportWidth, viewportHeight));
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
        Table table = new Table();
        table.add(new Label(title, skin)).padBottom(8f).colspan(2).row();
        table.add(new Label("SELECT A WORLD", skin)).padBottom(24f).colspan(2).row();

        for (int world = 0; world < worldLevels.length; world++) {
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
                onExit.run();
            }
        });
        table.row();
        table.add(exitButton).width(140f).height(44f).padTop(14f).colspan(2);
        return table;
    }

    private Table buildLevelList(int world) {
        int[] levels = worldLevels[world];
        Table table = new Table();
        table.add(new Label("WORLD " + (world + 1), skin)).padBottom(24f).row();

        for (int levelNumber : levels) {
            TextButton button = new TextButton(levelLabeler.apply(levelNumber), skin);
            boolean reallyUnlocked = unlockPolicy.isUnlocked(world, levelNumber);
            // debugBypassUnlock: every level is selectable regardless of the
            // real unlock policy, which is never consulted any differently
            // either way - a distinct orange tint (not the normal locked
            // grey, not the normal unlocked look) marks a level that's only
            // reachable this way, so this never looks indistinguishable from
            // a real unlocked/locked state.
            boolean unlocked = reallyUnlocked || debugBypassUnlock;
            button.setDisabled(!unlocked);
            if (!reallyUnlocked) {
                if (debugBypassUnlock) {
                    button.setColor(1f, 0.6f, 0.2f, 1f);
                } else {
                    button.setColor(0.5f, 0.5f, 0.5f, 1f);
                }
            }
            button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    onLevelSelected.accept(levelNumber);
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
        // - simple enough to just recompute every frame rather than
        // caching/invalidating.
        float viewportWidth = layerManager.getViewport().getWorldWidth();
        float viewportHeight = layerManager.getViewport().getWorldHeight();
        table.setPosition((viewportWidth - table.getWidth()) / 2f, (viewportHeight - table.getHeight()) / 2f);

        layerManager.act(delta);
        GameEngine.graphics.clearScreen(0f, 0f, 0f, 1f);
        layerManager.draw();
    }
}

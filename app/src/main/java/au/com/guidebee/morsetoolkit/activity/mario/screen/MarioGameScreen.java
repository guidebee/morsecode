package au.com.guidebee.morsetoolkit.activity.mario.screen;

import com.guidebee.game.GameEngine;
import com.guidebee.game.InputProcessor;
import com.guidebee.game.ScreenAdapter;
import com.guidebee.game.camera.viewports.FitViewport;
import com.guidebee.game.microedition.LayerManager;

import au.com.guidebee.morsetoolkit.activity.mario.MarioConfiguration;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.input.MarioInputController;
import au.com.guidebee.morsetoolkit.activity.mario.level.LevelCatalog;
import au.com.guidebee.morsetoolkit.activity.mario.level.LevelDefinition;
import au.com.guidebee.morsetoolkit.activity.mario.level.LevelLoader;
import au.com.guidebee.morsetoolkit.activity.mario.world.CameraController;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioWorld;

/**
 * Step 4 vertical slice B: small Mario runs/jumps/collides with a level's
 * static geometry, camera following him. See docs/MARIO_PORT_PLAN.md Step 4.4.
 * Still no interactive bricks/items/enemies - those are Steps 5-6.
 */
public class MarioGameScreen extends ScreenAdapter {

    private final LayerManager layerManager;
    private final MarioWorld world;
    private final Player player;
    private final CameraController camera;

    private final float clearR;
    private final float clearG;
    private final float clearB;

    private InputProcessor savedInputProcessor;

    public MarioGameScreen(int levelNumber) {
        LevelDefinition level = LevelCatalog.load(levelNumber);
        world = LevelLoader.createWorld(level);

        layerManager = new LayerManager(new FitViewport(
                MarioConfiguration.VIEWPORT_WIDTH, MarioConfiguration.VIEWPORT_HEIGHT));
        layerManager.append(world);

        // The original engine's per-level "pos" field (BasicLevel.pos) is never
        // actually read anywhere in Mario.java - the shipped game only reaches a
        // level via a checkpoint's NextLocation, and the initial boot checkpoint
        // points at the title/world-map level (10), not straight into a level.
        // Since we skip straight to a level (no menu yet - see MarioGamePlay),
        // there's no equivalent checkpoint chain to read from, so we use "pos"
        // instead: it's clearly the intended per-level spawn point (verified by
        // hand for Level 11: pos=(10,12) puts Mario's feet exactly on the
        // y=13 stone row), just dead data in the original.
        player = new Player(level.posX * MarioConfiguration.TILE_SIZE,
                level.posY * MarioConfiguration.TILE_SIZE, world, new MarioInputController());
        layerManager.append(player);

        camera = new CameraController(MarioConfiguration.VIEWPORT_WIDTH, MarioConfiguration.VIEWPORT_HEIGHT,
                world.getWidthPx(), world.getHeightPx());
        camera.centerOn(player.getX(), player.getY());

        float[] clear = clearColor(level.backgroundColor);
        clearR = clear[0];
        clearG = clear[1];
        clearB = clear[2];
    }

    private static float[] clearColor(String backgroundColor) {
        if ("Black".equals(backgroundColor)) {
            return new float[]{0f, 0f, 0f};
        }
        // Mario's classic sky-blue, matching the original's "Blue" background.
        return new float[]{92f / 255f, 148f / 255f, 252f / 255f};
    }

    @Override
    public void show() {
        savedInputProcessor = GameEngine.input.getInputProcessor();
        GameEngine.input.setInputProcessor(layerManager);
    }

    @Override
    public void hide() {
        GameEngine.input.setInputProcessor(savedInputProcessor);
    }

    @Override
    public void resize(int width, int height) {
        layerManager.getViewport().update(width, height, false);
    }

    @Override
    public void render(float delta) {
        layerManager.act(delta);
        camera.centerOn(player.getX() + player.getWidth() / 2f, player.getY() + player.getHeight() / 2f);

        GameEngine.graphics.clearScreen(clearR, clearG, clearB, 1f);
        layerManager.draw(camera.getX(), camera.getY());
    }
}

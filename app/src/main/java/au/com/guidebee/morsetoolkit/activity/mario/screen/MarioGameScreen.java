package au.com.guidebee.morsetoolkit.activity.mario.screen;

import com.guidebee.game.GameEngine;
import com.guidebee.game.InputProcessor;
import com.guidebee.game.ScreenAdapter;
import com.guidebee.game.camera.viewports.FitViewport;
import com.guidebee.game.microedition.LayerManager;

import au.com.guidebee.morsetoolkit.activity.mario.MarioConfiguration;
import au.com.guidebee.morsetoolkit.activity.mario.level.LevelCatalog;
import au.com.guidebee.morsetoolkit.activity.mario.level.LevelDefinition;
import au.com.guidebee.morsetoolkit.activity.mario.level.LevelLoader;
import au.com.guidebee.morsetoolkit.activity.mario.world.CameraController;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioWorld;

/**
 * Step 3 vertical slice A: renders one level's static geometry (brick/stone/
 * chocolate) via a {@link MarioWorld}, with no player and no enemies yet -
 * proves atlas packing + {@code TiledLayer} end-to-end. See
 * docs/MARIO_PORT_PLAN.md Step 3.3.
 */
public class MarioGameScreen extends ScreenAdapter {

    /**
     * Debug-only: auto-pans the camera across the level so its full extent
     * can be eyeballed without a player yet. Step 4 replaces this with
     * {@code camera.centerOn(player.getX(), player.getY())} every frame.
     */
    private static final float DEBUG_SCROLL_SPEED_PX_PER_SEC = 96f;

    private final LayerManager layerManager;
    private final MarioWorld world;
    private final CameraController camera;

    private final float clearR;
    private final float clearG;
    private final float clearB;

    private float debugScrollX;
    private InputProcessor savedInputProcessor;

    public MarioGameScreen(int levelNumber) {
        LevelDefinition level = LevelCatalog.load(levelNumber);
        world = LevelLoader.createWorld(level);

        layerManager = new LayerManager(new FitViewport(
                MarioConfiguration.VIEWPORT_WIDTH, MarioConfiguration.VIEWPORT_HEIGHT));
        layerManager.append(world);

        camera = new CameraController(MarioConfiguration.VIEWPORT_WIDTH, MarioConfiguration.VIEWPORT_HEIGHT,
                world.getWidthPx(), world.getHeightPx());
        camera.centerOn(MarioConfiguration.VIEWPORT_WIDTH / 2f, world.getHeightPx() / 2f);

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

        float maxScrollX = Math.max(0, world.getWidthPx() - MarioConfiguration.VIEWPORT_WIDTH);
        debugScrollX += DEBUG_SCROLL_SPEED_PX_PER_SEC * delta;
        if (debugScrollX > maxScrollX) {
            debugScrollX = 0;
        }
        camera.centerOn(debugScrollX + MarioConfiguration.VIEWPORT_WIDTH / 2f, world.getHeightPx() / 2f);

        GameEngine.graphics.clearScreen(clearR, clearG, clearB, 1f);
        layerManager.draw(camera.getX(), camera.getY());
    }
}

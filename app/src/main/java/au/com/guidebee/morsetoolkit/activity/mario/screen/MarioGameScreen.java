package au.com.guidebee.morsetoolkit.activity.mario.screen;

import com.guidebee.game.GameEngine;
import com.guidebee.game.InputProcessor;
import com.guidebee.game.ScreenAdapter;
import com.guidebee.game.audio.Music;
import com.guidebee.game.camera.OrthographicCamera;
import com.guidebee.game.camera.viewports.FitViewport;
import com.guidebee.game.microedition.LayerManager;

import au.com.guidebee.morsetoolkit.activity.mario.MarioConfiguration;
import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.collision.PlayerCollisionResolver;
import au.com.guidebee.morsetoolkit.activity.mario.input.MarioInputController;
import au.com.guidebee.morsetoolkit.activity.mario.level.LevelCatalog;
import au.com.guidebee.morsetoolkit.activity.mario.level.LevelDefinition;
import au.com.guidebee.morsetoolkit.activity.mario.level.LevelLoader;
import au.com.guidebee.morsetoolkit.activity.mario.world.CameraController;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioContext;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioWorld;

/**
 * Step 5 vertical slice C: a level's interactive bricks and items work on
 * top of Step 4's movement/collision - breaking bricks, collecting
 * mushrooms/flowers/stars/1UPs, growing/shrinking. Still no enemies (Step 6).
 * See docs/MARIO_PORT_PLAN.md Step 5.4.
 */
public class MarioGameScreen extends ScreenAdapter {

    private final LayerManager layerManager;
    private final OrthographicCamera gdxCamera;
    private final MarioWorld world;
    private final Player player;
    private final CameraController camera;
    private final String levelAttribute;

    private final float clearR;
    private final float clearG;
    private final float clearB;

    private Music currentMusic;
    private boolean starMusicActive;
    private InputProcessor savedInputProcessor;

    public MarioGameScreen(int levelNumber) {
        LevelDefinition level = LevelCatalog.load(levelNumber);
        levelAttribute = level.attribute;
        world = LevelLoader.createWorld(level);

        layerManager = new LayerManager(new FitViewport(
                MarioConfiguration.VIEWPORT_WIDTH, MarioConfiguration.VIEWPORT_HEIGHT));
        layerManager.append(world);

        // The engine's camera defaults to the standard libGDX Y-up convention
        // (increasing Y = up-screen), but every other piece of this port -
        // TiledLayer/Sprite's row/y math, LevelDefinition's tile y-coordinates,
        // Player's gravity (increasing Y = falling) - assumes Y-down, matching
        // the original engine's AWT/Java2D coordinate system the level data
        // came from. Reconfiguring the camera to Y-down here, once, keeps that
        // one assumption consistent everywhere instead of inverting Y in every
        // piece of gameplay code that touches a Y coordinate.
        gdxCamera = (OrthographicCamera) layerManager.getViewport().getCamera();
        gdxCamera.setToOrtho(true, MarioConfiguration.VIEWPORT_WIDTH, MarioConfiguration.VIEWPORT_HEIGHT);

        // Bricks/items spawned below register themselves into MarioContext.world()
        // and layerManager - see LevelLoader.spawnBricks and MarioContext's class doc.
        MarioContext.init(layerManager, world);
        LevelLoader.spawnBricks(level);

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

    private void startMusic(String attribute) {
        if (currentMusic != null) {
            currentMusic.stop();
        }
        currentMusic = MarioResourceManager.music(attribute);
        currentMusic.setLooping(true);
        currentMusic.play();
    }

    @Override
    public void show() {
        savedInputProcessor = GameEngine.input.getInputProcessor();
        GameEngine.input.setInputProcessor(layerManager);
        startMusic(levelAttribute);
    }

    @Override
    public void hide() {
        GameEngine.input.setInputProcessor(savedInputProcessor);
        if (currentMusic != null) {
            currentMusic.stop();
        }
    }

    @Override
    public void resize(int width, int height) {
        layerManager.getViewport().update(width, height, false);
    }

    @Override
    public void render(float delta) {
        layerManager.act(delta);
        PlayerCollisionResolver.resolvePickups(player, world);

        boolean hasStar = player.hasStar();
        if (hasStar != starMusicActive) {
            starMusicActive = hasStar;
            startMusic(hasStar ? "Star" : levelAttribute);
        }

        camera.centerOn(player.getX() + player.getWidth() / 2f, player.getY() + player.getHeight() / 2f);

        // Set the camera's absolute world position directly rather than going
        // through LayerManager.draw(int,int)'s relative "translate from
        // wherever the camera currently sits, then restore" trick - that trick
        // is meant for a small constant centering nudge (as Battle City uses
        // it), not for an arbitrarily large, continuously moving scroll
        // target, and using it that way was the bug behind a blank screen:
        // the camera ended up looking at a world position with nothing in it.
        gdxCamera.position.set(camera.getX() + MarioConfiguration.VIEWPORT_WIDTH / 2f,
                camera.getY() + MarioConfiguration.VIEWPORT_HEIGHT / 2f, 0);

        GameEngine.graphics.clearScreen(clearR, clearG, clearB, 1f);
        layerManager.draw();
    }
}

package au.com.guidebee.morsetoolkit.activity.mario.screen;

import com.guidebee.game.GameEngine;
import com.guidebee.game.InputProcessor;
import com.guidebee.game.ScreenAdapter;
import com.guidebee.game.audio.Music;
import com.guidebee.game.camera.OrthographicCamera;
import com.guidebee.game.camera.viewports.ExtendViewport;
import com.guidebee.game.camera.viewports.Viewport;
import com.guidebee.game.graphics.Pixmap;
import com.guidebee.game.graphics.Texture;
import com.guidebee.game.graphics.TextureRegion;
import com.guidebee.game.microedition.LayerManager;
import com.guidebee.game.ui.ClickListener;
import com.guidebee.game.ui.GameController;
import com.guidebee.game.ui.ImageButton;
import com.guidebee.game.ui.InputEvent;
import com.guidebee.game.ui.drawable.TextureRegionDrawable;

import au.com.guidebee.morsetoolkit.activity.mario.MarioConfiguration;
import au.com.guidebee.morsetoolkit.activity.mario.MarioGamePlay;
import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.collision.CheckpointResolver;
import au.com.guidebee.morsetoolkit.activity.mario.collision.EnemyCollisionResolver;
import au.com.guidebee.morsetoolkit.activity.mario.collision.LiftCollisionResolver;
import au.com.guidebee.morsetoolkit.activity.mario.collision.PlayerCollisionResolver;
import au.com.guidebee.morsetoolkit.activity.mario.collision.ProjectileCollisionResolver;
import au.com.guidebee.morsetoolkit.activity.mario.input.MarioInputController;
import au.com.guidebee.morsetoolkit.activity.mario.input.PlayerCommand;
import au.com.guidebee.morsetoolkit.activity.mario.level.LevelCatalog;
import au.com.guidebee.morsetoolkit.activity.mario.level.LevelDefinition;
import au.com.guidebee.morsetoolkit.activity.mario.level.LevelLoader;
import au.com.guidebee.morsetoolkit.activity.mario.world.CameraController;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioContext;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioWorld;

/**
 * Steps 5-7 vertical slice: a level's interactive bricks, items,
 * ground-walking enemies (stomp/shell/kick, Fire Mario's fireballs), moving
 * platforms, and level-end flag/pipe checkpoints all work on top of Step 4's
 * movement/collision. See docs/MARIO_PORT_PLAN.md Step 5.4/6.1-6.3/7.1. Not
 * yet covered: flying/patrol enemy variants, Boss, same-level teleports
 * (unused by any World-1 level), bomb/flying-fish {@code SpawnController}
 * (Step 7.2 - also unused by any World-1 level, see {@code LevelLoader}),
 * HUD/menu/game state (Step 8).
 *
 * <h2>Level completion</h2>
 * {@link #levelState} is a tiny state machine driving what happens once the
 * player touches a checkpoint (see {@code CheckpointResolver}): control is
 * handed to a {@link PlayerCommand} this screen drives directly instead of
 * the real input controller (walking Mario into the level-end flag, or
 * holding him still while "entering" a pipe) for a short, kind-specific
 * delay, after which {@link MarioGamePlay#goToLevel} swaps in the next
 * level's screen at the checkpoint's saved spawn tile. Ported from the
 * original's {@code Player_CheckPoint.collided}/{@code Player_Flag.collided}
 * or {@code IncreaseLevel} - see {@link #beginTransition}.
 *
 * <p>On-screen controls are the same {@code GameController} (touchpad +
 * button A/B) widget Battle City uses for its own controls, loading the same
 * "style 08" joystick art (see {@code MarioResourceManager}'s
 * {@code CONTROLLER_TEXTURES} and {@code BattleCityGameScene}'s constructor)
 * from this project's {@code assets/controller/} tree - button A is jump,
 * button B is fire, and the touchpad's knob drives left/right/down (see
 * {@code MarioInputController}). A separate small back button, drawn
 * procedurally via {@code Pixmap} exactly like
 * {@code BattleCityGameScene.createBackIcon}, exits to the main menu.
 *
 * <h2>Why the HUD buttons are repositioned every frame</h2>
 * {@code Stage} draws its main layer and its HUD layer (added via
 * {@code addHUDComponent}) through the exact same shared {@code Camera}
 * object (both {@code InternalStage}s are constructed with the same
 * {@code Viewport} instance) - there's no separate "HUD camera" in this
 * engine. Battle City never notices because its camera never moves; Mario's
 * does (following the player), which drags HUD buttons positioned in fixed
 * world coordinates along with the scrolling world instead of leaving them
 * screen-anchored. Since {@link #render} already computes {@code camera}'s
 * current scroll offset, {@link #repositionHud} cancels it out by nudging
 * each button's world position by that same amount every frame, keeping
 * them visually (and, since hit-testing goes through the same camera, also
 * interactively) fixed on screen.
 */
public class MarioGameScreen extends ScreenAdapter {

    private static final int BACK_BUTTON_SIZE = 16;
    private static final int MARGIN = 8;

    // The same "style 08" joystick art Battle City loads for its own
    // GameController - see MarioResourceManager.CONTROLLER_TEXTURES. Button A
    // reuses Battle City's own fire-button icon ("Shoot" - BattleCityGameScene
    // wires BUTTON_A to the tank's fire key), now Mario's fire button too;
    // button B reuses Battle City's unused button ("Virgin" - built by
    // BattleCityGameScene's GameController but never acted on), repurposed
    // here as Mario's jump button.
    private static final String CONTROLLER_BACKGROUND = "controller/Backgrounds/Back_08.png";
    private static final String CONTROLLER_KNOB = "controller/Joystick/Joystick_08.png";
    private static final String CONTROLLER_BUTTON_A_NORMAL = "controller/Buttons/Button_08_Normal_Shoot.png";
    private static final String CONTROLLER_BUTTON_A_PRESSED = "controller/Buttons/Button_08_Pressed_Shoot.png";
    private static final String CONTROLLER_BUTTON_B_NORMAL = "controller/Buttons/Button_08_Normal_Virgin.png";
    private static final String CONTROLLER_BUTTON_B_PRESSED = "controller/Buttons/Button_08_Pressed_Virgin.png";

    /** How long the "walk into the flag/castle" sequence plays before the next level loads - {@code "CheckPoints"} kind only. */
    private static final float FLAG_WALK_SECONDS = 1.2f;
    /** How long a pipe-entry checkpoint holds Mario still before the next level loads. */
    private static final float PIPE_ENTRY_SECONDS = 0.6f;
    /** Comfortably longer than either delay above, so a stray hit can't interrupt the sequence - see {@code Player#setInvincibleFor}. */
    private static final float TRANSITION_INVINCIBILITY_SECONDS = 5f;

    private enum LevelState {PLAYING, ENTERING, ADVANCING}

    private final MarioGamePlay gamePlay;
    private final LevelDefinition level;
    private final LayerManager layerManager;
    private final OrthographicCamera gdxCamera;
    /** The camera window's actual size, in world pixels - see the constructor's {@code ExtendViewport} note. */
    private final int viewportWidth;
    private final int viewportHeight;
    private final MarioWorld world;
    private final Player player;
    private final CameraController camera;
    private final String levelAttribute;

    private final GameController gameController;
    private final ImageButton backButton;

    private final float clearR;
    private final float clearG;
    private final float clearB;

    private Music currentMusic;
    private boolean starMusicActive;
    private InputProcessor savedInputProcessor;

    private LevelState levelState = LevelState.PLAYING;
    private LevelDefinition.Checkpoint pendingCheckpoint;
    private float transitionTimer;

    public MarioGameScreen(int levelNumber, MarioGamePlay gamePlay) {
        this(levelNumber, gamePlay, -1, -1);
    }

    /**
     * @param spawnTileX overrides {@code level}'s own default spawn tile
     *                   (-1 to use it) - how a checkpoint transition places
     *                   Mario at its saved {@code locX}/{@code locY} instead.
     */
    public MarioGameScreen(int levelNumber, MarioGamePlay gamePlay, int spawnTileX, int spawnTileY) {
        this.gamePlay = gamePlay;
        level = LevelCatalog.load(levelNumber);
        levelAttribute = level.attribute;
        world = LevelLoader.createWorld(level);

        // FitViewport (Step 3's original choice) letterboxes/pillarboxes
        // whenever the device screen's aspect ratio isn't exactly
        // MarioConfiguration.VIEWPORT_WIDTH:HEIGHT's 4:3 - every landscape
        // phone (much wider than 4:3) got permanent empty bars down both
        // sides. ExtendViewport instead keeps the configured size as a
        // MINIMUM and stretches the world in the shorter dimension (here,
        // width - phone height is the constraining side) until it fills the
        // screen, showing more of the level side-to-side instead of leaving
        // it blank - no letterboxing, no distortion (see its own class doc).
        layerManager = new LayerManager(new ExtendViewport(
                MarioConfiguration.VIEWPORT_WIDTH, MarioConfiguration.VIEWPORT_HEIGHT));
        layerManager.append(world);

        // Stage's constructor already resolved the extended size above
        // against the real screen (see UIWindow's constructor, which calls
        // Viewport.update() immediately) - read it back instead of assuming
        // it's still exactly MarioConfiguration.VIEWPORT_WIDTH/HEIGHT, and use
        // it everywhere below a screen size is needed (CameraController's
        // clamp bounds, the HUD's screen-edge anchors) so they match
        // whatever this device actually extended to.
        Viewport viewport = layerManager.getViewport();
        viewportWidth = Math.round(viewport.getWorldWidth());
        viewportHeight = Math.round(viewport.getWorldHeight());

        // The engine's camera defaults to the standard libGDX Y-up convention
        // (increasing Y = up-screen), but every other piece of this port -
        // TiledLayer/Sprite's row/y math, LevelDefinition's tile y-coordinates,
        // Player's gravity (increasing Y = falling) - assumes Y-down, matching
        // the original engine's AWT/Java2D coordinate system the level data
        // came from. Reconfiguring the camera to Y-down here, once, keeps that
        // one assumption consistent everywhere instead of inverting Y in every
        // piece of gameplay code that touches a Y coordinate.
        gdxCamera = (OrthographicCamera) viewport.getCamera();
        gdxCamera.setToOrtho(true, viewportWidth, viewportHeight);

        // Bricks/items spawned below register themselves into MarioContext.world()
        // and layerManager - see LevelLoader.spawnBricks and MarioContext's class doc.
        // Scenery goes first so bricks/enemies/the player draw in front of it.
        MarioContext.init(layerManager, world);
        LevelLoader.spawnScenery(level);
        LevelLoader.spawnBricks(level);
        LevelLoader.spawnEnemies(level);
        LevelLoader.spawnLifts(level);

        gameController = createGameController();
        backButton = createBackButton(gamePlay);
        MarioInputController input = new MarioInputController(gameController);

        // The original engine's per-level "pos" field (BasicLevel.pos) is never
        // actually read anywhere in Mario.java - the shipped game only reaches a
        // level via a checkpoint's NextLocation (now honored here whenever
        // spawnTileX/Y are given - see MarioGamePlay.goToLevel), and the initial
        // boot checkpoint points at the title/world-map level (10), not straight
        // into a level. When there's no checkpoint to arrive from (this screen's
        // very first level), "pos" is the fallback: it's clearly the intended
        // per-level spawn point (verified by hand for Level 11: pos=(10,12) puts
        // Mario's feet exactly on the y=13 stone row), just dead data in the
        // original.
        int startTileX = spawnTileX >= 0 ? spawnTileX : level.posX;
        int startTileY = spawnTileY >= 0 ? spawnTileY : level.posY;
        player = new Player(startTileX * MarioConfiguration.TILE_SIZE,
                startTileY * MarioConfiguration.TILE_SIZE, world, input);
        layerManager.append(player);

        camera = new CameraController(viewportWidth, viewportHeight,
                world.getWidthPx(), world.getHeightPx());
        camera.centerOn(player.getX(), player.getY());
        repositionHud();

        float[] clear = clearColor(level.backgroundColor);
        clearR = clear[0];
        clearG = clear[1];
        clearB = clear[2];
    }

    /**
     * The on-screen joystick + A/B buttons - see the class doc. Button A
     * (the "Shoot" icon) is fire, button B (the "Virgin" icon) is jump;
     * {@code MarioInputController} polls both plus the touchpad knob.
     */
    private GameController createGameController() {
        Texture background = MarioResourceManager.controllerTexture(CONTROLLER_BACKGROUND);
        GameController controller = new GameController(
                new TextureRegionDrawable(new TextureRegion(background)),
                controllerDrawable(CONTROLLER_KNOB),
                controllerDrawable(CONTROLLER_BUTTON_A_NORMAL),
                controllerDrawable(CONTROLLER_BUTTON_A_PRESSED),
                controllerDrawable(CONTROLLER_BUTTON_B_NORMAL),
                controllerDrawable(CONTROLLER_BUTTON_B_PRESSED));
        // GameController never sizes itself from its own touchpad (see
        // repositionHud) - set explicitly here so repositionHud can anchor it
        // to the screen's bottom edge.
        controller.setSize(background.getWidth(), background.getHeight());
        controller.setAlpha(MarioResourceManager.controllerAlpha());
        // Battle City's own GameController.layout() positions button A/B from
        // getParent().getWidth() - that only resolves to the real screen
        // width when the parent is the engine's dedicated, always-full-screen
        // tableGameControl (set up by Stage's constructor - see
        // setGameController's doc), which is exactly what setGameController
        // (rather than addHUDComponent, used for every other HUD widget here)
        // wires it into. Without this, that width read back as 0, pushing
        // both buttons off-screen.
        layerManager.setGameController(controller);
        return controller;
    }

    private static TextureRegionDrawable controllerDrawable(String assetPath) {
        return new TextureRegionDrawable(new TextureRegion(MarioResourceManager.controllerTexture(assetPath)));
    }

    /** Exits to the main menu - otherwise this screen has no other way back. */
    private ImageButton createBackButton(MarioGamePlay gamePlay) {
        ImageButton back = new ImageButton(createBackIcon(BACK_BUTTON_SIZE, false), createBackIcon(BACK_BUTTON_SIZE, true));
        back.setSize(BACK_BUTTON_SIZE, BACK_BUTTON_SIZE);
        back.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                gamePlay.finish();
            }
        });
        layerManager.addHUDComponent(back);
        return back;
    }

    /** Re-anchors the HUD to a fixed screen position - see the class doc. */
    private void repositionHud() {
        float scrollX = camera.getX();
        float scrollY = camera.getY();
        // GameController positions its own knob/buttons relative to its
        // parent's width (see com.guidebee.game.ui.GameController.layout()),
        // so it must stay flush against the screen's left edge for those
        // offsets to land at the intended screen edges; scrollY increases
        // downward (see the class doc's Y-down note), so the bottom edge is
        // scrollY + viewportHeight, and subtracting the controller's own
        // height anchors it there instead of the screen's top-left corner.
        gameController.setPosition(scrollX,
                scrollY + viewportHeight - gameController.getHeight() - MARGIN);
        backButton.setPosition(scrollX + viewportWidth / 2f - BACK_BUTTON_SIZE / 2f,
                scrollY + viewportHeight - BACK_BUTTON_SIZE - MARGIN / 2f);
    }

    private static TextureRegionDrawable createBackIcon(int size, boolean pressed) {
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setColor(0f, 0f, 0f, pressed ? 0.85f : 0.55f);
        pixmap.fillCircle(size / 2, size / 2, size / 2 - 1);
        pixmap.setColor(1f, 1f, 1f, 1f);
        pixmap.fillTriangle(size * 2 / 3, size / 4, size * 2 / 3, size * 3 / 4, size / 3, size / 2);
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegionDrawable(new TextureRegion(texture));
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
        // See MarioConfiguration.MAX_DELTA_SECONDS - avoids tunneling through
        // thin colliders after a hitch (e.g. this screen's own construction
        // eating real time before the first frame renders).
        delta = Math.min(delta, MarioConfiguration.MAX_DELTA_SECONDS);

        layerManager.act(delta);
        PlayerCollisionResolver.resolvePickups(player, world);
        EnemyCollisionResolver.resolve(player, world);
        ProjectileCollisionResolver.resolve(world);
        LiftCollisionResolver.resolve(player, world);
        updateLevelCompletion(delta);

        boolean hasStar = player.hasStar();
        if (hasStar != starMusicActive) {
            starMusicActive = hasStar;
            startMusic(hasStar ? "Star" : levelAttribute);
        }

        camera.centerOn(player.getX() + player.getWidth() / 2f, player.getY() + player.getHeight() / 2f);
        repositionHud();

        // Set the camera's absolute world position directly rather than going
        // through LayerManager.draw(int,int)'s relative "translate from
        // wherever the camera currently sits, then restore" trick - that trick
        // is meant for a small constant centering nudge (as Battle City uses
        // it), not for an arbitrarily large, continuously moving scroll
        // target, and using it that way was the bug behind a blank screen:
        // the camera ended up looking at a world position with nothing in it.
        gdxCamera.position.set(camera.getX() + viewportWidth / 2f,
                camera.getY() + viewportHeight / 2f, 0);

        GameEngine.graphics.clearScreen(clearR, clearG, clearB, 1f);
        layerManager.draw();
    }

    /** Drives {@link #levelState} - see the class doc's "Level completion" section. */
    private void updateLevelCompletion(float delta) {
        switch (levelState) {
            case PLAYING:
                LevelDefinition.Checkpoint hit = CheckpointResolver.findTouched(level.checkpoints, player);
                if (hit != null) {
                    beginTransition(hit);
                }
                break;
            case ENTERING:
                transitionTimer -= delta;
                if (transitionTimer <= 0) {
                    advanceToNextLevel();
                }
                break;
            case ADVANCING:
                // gamePlay.goToLevel already swapped this screen out; nothing left to do.
                break;
        }
    }

    /**
     * Ported from the original's {@code Player_CheckPoint.collided}/
     * {@code Player_Flag.collided}: locks out normal input, shields Mario
     * from any stray hit for the duration, and - for the level-end flag
     * specifically - walks him forward into the castle instead of just
     * standing still, matching the original's more elaborate
     * {@code MarioSlidingDown} sequence in spirit if not in frame-by-frame
     * detail (see docs/MARIO_PORT_PLAN.md Step 7.1).
     */
    private void beginTransition(LevelDefinition.Checkpoint checkpoint) {
        pendingCheckpoint = checkpoint;
        levelState = LevelState.ENTERING;
        player.setInvincibleFor(TRANSITION_INVINCIBILITY_SECONDS);

        PlayerCommand forced = new PlayerCommand();
        boolean isFlag = "CheckPoints".equals(checkpoint.kind);
        forced.right = isFlag;
        player.setForcedCommand(forced);
        transitionTimer = isFlag ? FLAG_WALK_SECONDS : PIPE_ENTRY_SECONDS;

        if (currentMusic != null) {
            currentMusic.stop();
        }
        boolean isPipe = checkpoint.kind.startsWith("InsidePump");
        MarioResourceManager.sound(isPipe ? "smb_pipe" : "smb_stage_clear").play();
    }

    private void advanceToNextLevel() {
        boolean advanced = gamePlay.goToLevel(pendingCheckpoint.nextLevel, pendingCheckpoint.locX, pendingCheckpoint.locY);
        if (advanced) {
            levelState = LevelState.ADVANCING;
        } else {
            // Target level isn't shipped in v1 (see MarioGamePlay.goToLevel) -
            // stay on this level instead of getting stuck mid-transition.
            levelState = LevelState.PLAYING;
            player.clearForcedCommand();
        }
    }
}

package au.com.guidebee.morsetoolkit.activity.mario.world;

import java.util.Random;

import au.com.guidebee.morsetoolkit.activity.mario.MarioConfiguration;
import au.com.guidebee.morsetoolkit.activity.mario.actors.enemies.Rocket;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.level.LevelDefinition;

/**
 * Ticking background spawners tied to a level's own data flags, ported from
 * {@code Mario.java}'s main loop - just the "Bombs" half for now (see below);
 * "FlyingFishes" is Sea-attribute-only and deferred to
 * docs/MARIO_PORT_PLAN_PHASE2.md Step P2.6 alongside the rest of Sea/water
 * content, matching this port's existing Sea-theme deferral (see
 * {@code tools/mario-atlas-packer}'s own class doc).
 *
 * <p>"Bombs": while {@code level.bombs} is set and the player hasn't yet
 * passed the level's own {@code bombsTurnOff} tile column, periodically
 * spawns a {@link Rocket} just past the camera's right edge at a random
 * height, flying left toward the player - reuses {@code Rocket} itself
 * (the original does too: {@code EnemyGroup.add(new Rocket(...))}, the same
 * class {@code RocketLauncher} fires, just spawned directly rather than
 * from a placed turret). The original spawns at
 * {@code (player.getX() - player.getScreenX()) + 680} (680 being a fixed
 * offset tuned for its own 640px-wide desktop window, i.e. just past its
 * right edge) - this port uses the *current* camera's own right edge
 * instead, so it still spawns just off-screen regardless of viewport width.
 */
public class SpawnController {

    private static final float PHYSICS_FPS = 60f;
    /** Roughly the original's own `680 - 640` window-width buffer. */
    private static final float SPAWN_MARGIN_PX = 40f;

    private static final Random RANDOM = new Random();

    private final boolean bombsEnabled;
    private final int bombsTurnOffTile;
    private final boolean blackAndWhite;
    private float bombsDelay;

    public SpawnController(LevelDefinition level) {
        this.bombsEnabled = level.bombs;
        this.bombsTurnOffTile = level.bombsTurnOff;
        // Ported from Mario.java's own ambient-bomb block: on a CloudsNight
        // level (World 6's Level_63, the only one that both sets Bombs=true
        // and CloudsNight - confirmed by reading its own source) the rocket
        // itself swaps to the black-and-white asset too, matching Rocket's
        // own "bw_rocket_launcher" doc.
        this.blackAndWhite = "CloudsNight".equals(level.backgroundImage);
    }

    public void update(float delta, CameraController camera, Player player) {
        if (!bombsEnabled || player.getX() / MarioConfiguration.TILE_SIZE >= bombsTurnOffTile) {
            return;
        }
        float frames = delta * PHYSICS_FPS;
        bombsDelay -= frames;
        if (bombsDelay < 0) {
            bombsDelay = (1 + RANDOM.nextInt(5)) * 100f;
            float x = camera.getX() + camera.getEffectiveWidth() + SPAWN_MARGIN_PX;
            float y = (1 + RANDOM.nextInt(10)) * MarioConfiguration.TILE_SIZE;
            Rocket rocket = new Rocket(x, y, false, blackAndWhite);
            MarioContext.world().addEnemy(rocket);
            MarioContext.spawn(rocket);
        }
    }
}

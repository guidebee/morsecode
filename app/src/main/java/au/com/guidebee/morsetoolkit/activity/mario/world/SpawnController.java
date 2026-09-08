package au.com.guidebee.morsetoolkit.activity.mario.world;

import java.util.Random;

import au.com.guidebee.morsetoolkit.activity.mario.actors.enemies.Enemy;
import au.com.guidebee.morsetoolkit.activity.mario.actors.enemies.FishyGround;
import au.com.guidebee.morsetoolkit.activity.mario.actors.enemies.Rocket;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.level.LevelDefinition;
import au.com.guidebee.morsetoolkit.platformer.core.CameraFollow;

/**
 * Ticking background spawners tied to a level's own data flags, ported from
 * {@code Mario.java}'s main loop: "Bombs" (see below) and "FlyingFishes"
 * (World 2/7's Level_23/73, World 8's Level_843 - none of them Sea
 * attribute, confirmed by reading their own converted JSON; the jumping-fish
 * hazard just happens to reuse Sea-theme fish art, see {@link FishyGround}'s
 * own doc for why that art is packed COMMON instead of SEA-only).
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
 *
 * <p>"FlyingFishes": while {@code level.flyingFishes} is set and the player
 * is between world x=640 and the level's own {@code flyingFishesLength},
 * periodically spawns a {@link FishyGround} at the player's own x (offset
 * sideways, opposite its launch direction, so it doesn't appear directly
 * underfoot - matches the original's own {@code moveX(random(...))} call),
 * capped at 10 concurrent fish - ported verbatim from Mario.java's own
 * {@code FlyingFishesGroup.getSize() < 10} / {@code FlyingFishesdelay}
 * block, ticked independently of Bombs' own delay timer.
 */
public class SpawnController {

    private static final float PHYSICS_FPS = 60f;
    /** Roughly the original's own `680 - 640` window-width buffer. */
    private static final float SPAWN_MARGIN_PX = 40f;

    private static final int FLYING_FISH_MAX_ACTIVE = 10;
    private static final float FLYING_FISH_SPAWN_Y = 512f;

    private static final Random RANDOM = new Random();

    private final boolean bombsEnabled;
    private final int bombsTurnOffTile;
    private final boolean blackAndWhite;
    private final int tileSize;
    private float bombsDelay;

    private final boolean flyingFishesEnabled;
    private final int flyingFishesLength;
    /** Ported from the original's own `player.getX() > 20*32`. */
    private final float flyingFishMinX;
    /** Ported from the original's own `moveX(random(4*32, 8*32))` (in whichever direction sends it away from the launch heading). */
    private final float flyingFishOffsetMinPx;
    private final float flyingFishOffsetMaxPx;
    private float flyingFishesDelay = -1;

    public SpawnController(LevelDefinition level, int tileSize) {
        this.bombsEnabled = level.bombs;
        this.bombsTurnOffTile = level.bombsTurnOff;
        // Ported from Mario.java's own ambient-bomb block: on a CloudsNight
        // level (World 6's Level_63, the only one that both sets Bombs=true
        // and CloudsNight - confirmed by reading its own source) the rocket
        // itself swaps to the black-and-white asset too, matching Rocket's
        // own "bw_rocket_launcher" doc.
        this.blackAndWhite = "CloudsNight".equals(level.backgroundImage);
        this.tileSize = tileSize;
        this.flyingFishesEnabled = level.flyingFishes;
        this.flyingFishesLength = level.flyingFishesLength;
        this.flyingFishMinX = 20 * tileSize;
        this.flyingFishOffsetMinPx = 4 * tileSize;
        this.flyingFishOffsetMaxPx = 8 * tileSize;
    }

    public void update(float delta, CameraFollow camera, Player player) {
        float frames = delta * PHYSICS_FPS;
        updateBombs(frames, camera, player);
        updateFlyingFishes(frames, player);
    }

    private void updateBombs(float frames, CameraFollow camera, Player player) {
        if (!bombsEnabled || player.getX() / tileSize >= bombsTurnOffTile) {
            return;
        }
        bombsDelay -= frames;
        if (bombsDelay < 0) {
            bombsDelay = (1 + RANDOM.nextInt(5)) * 100f;
            float x = camera.getX() + camera.getEffectiveWidth() + SPAWN_MARGIN_PX;
            float y = (1 + RANDOM.nextInt(10)) * tileSize;
            Rocket rocket = new Rocket(x, y, false, blackAndWhite, tileSize);
            MarioContext.world().addEnemy(rocket);
            MarioContext.spawn(rocket);
        }
    }

    private void updateFlyingFishes(float frames, Player player) {
        if (!flyingFishesEnabled) {
            return;
        }
        float px = player.getX();
        if (px >= flyingFishesLength || px <= flyingFishMinX) {
            return;
        }
        long active = 0;
        for (Enemy enemy : MarioContext.world().getEnemies()) {
            if (enemy.isActive() && enemy instanceof FishyGround) {
                active++;
            }
        }
        if (active >= FLYING_FISH_MAX_ACTIVE) {
            return;
        }
        flyingFishesDelay -= frames;
        if (flyingFishesDelay < 0) {
            flyingFishesDelay = RANDOM.nextInt(100) + 120;
            boolean movingRight = RANDOM.nextBoolean();
            float offset = flyingFishOffsetMinPx
                    + RANDOM.nextFloat() * (flyingFishOffsetMaxPx - flyingFishOffsetMinPx);
            float x = movingRight ? px - offset : px + offset;
            float speedMagnitude = (1 + RANDOM.nextInt(3)) / 2f;
            FishyGround fish = new FishyGround(x, FLYING_FISH_SPAWN_Y, movingRight, speedMagnitude, tileSize);
            MarioContext.world().addEnemy(fish);
            MarioContext.spawn(fish);
        }
    }
}

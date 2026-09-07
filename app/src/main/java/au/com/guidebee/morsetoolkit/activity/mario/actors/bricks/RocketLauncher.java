package au.com.guidebee.morsetoolkit.activity.mario.actors.bricks;

import com.guidebee.game.graphics.TextureRegion;

import java.util.Random;

import au.com.guidebee.morsetoolkit.activity.mario.actors.enemies.Rocket;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioContext;

/**
 * A rocket-firing turret's head, ported from {@code Bricks/RocketLauncher.java}
 * - solid, like {@link Iron} ({@code HitFromDown} is a no-op), and fires a
 * {@link Rocket} toward the player on a random timer, but only while the
 * player is *not* within a 100px "safe zone" either side of it (confirmed
 * by reading the source: the original's own delay countdown still ticks
 * down to zero while the player is close, then just silently skips firing
 * and waits for the *next* tick to check again, rather than pausing the
 * countdown - ported faithfully, not "improved" into pausing it).
 *
 * <p>{@code region} is frame index 0 of the shared "rocket_launcher" strip
 * (see {@code Mario.java}'s own case 19, "y=0 is Shooter") - already sliced
 * out by {@code LevelLoader}, not a multi-frame region itself.
 */
public class RocketLauncher extends InteractiveBrick {

    private static final float PHYSICS_FPS = 60f;
    private static final float SAFE_ZONE_PX = 100f;

    private static final Random RANDOM = new Random();

    private float shootTimer;

    public RocketLauncher(float x, float y, TextureRegion region) {
        super(region, x, y);
        shootTimer = randomTicks(1, 3, 50);
    }

    private static float randomTicks(int min, int maxInclusive, int multiplier) {
        return (min + RANDOM.nextInt(maxInclusive - min + 1)) * multiplier;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!isActive()) {
            return;
        }
        float frames = delta * PHYSICS_FPS;
        shootTimer -= frames;
        if (shootTimer > 0) {
            return;
        }
        Player player = MarioContext.player();
        if (player.getX() > getX() - SAFE_ZONE_PX && player.getX() < getX() + SAFE_ZONE_PX) {
            return;
        }
        boolean towardLeft = player.getX() < getX();
        Rocket rocket = new Rocket(getX(), getY(), !towardLeft);
        MarioContext.world().addEnemy(rocket);
        MarioContext.spawn(rocket);
        shootTimer = randomTicks(2, 5, 200);
    }
}

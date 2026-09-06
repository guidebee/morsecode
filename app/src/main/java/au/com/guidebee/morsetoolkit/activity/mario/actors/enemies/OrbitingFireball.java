package au.com.guidebee.morsetoolkit.activity.mario.actors.enemies;

import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.world.OscillatorClock;

/**
 * One fireball of a {@code FireBar}/{@code BigFireBar} ring, ported from
 * {@code Objects/EnemyFireBall.java}. A "FireBar" tile spawns 6 of these
 * (12 for "BigFireBar") around one pivot, each at a different
 * {@code distanceFromCenter} (0, 16, 32, 48... px), all reading the same
 * shared angle from {@link OscillatorClock} - see that class's doc for why
 * the angle lives there instead of on this class.
 *
 * <p>Indestructible and un-stompable by design (matches the original's empty
 * {@code KilledByFireBall()} and its {@code MarioJumpedOnEnemy}/
 * {@code CollidedWithMarioXXX} bodies, which only ever hurt Mario, never
 * react to being touched themselves) - only {@link #onStomped}/
 * {@link #onTouchedSide} are overridden, both matching the original's
 * identical "if star: nothing; else hurt" body across all four of its touch
 * callbacks.
 *
 * <p>Positioned by direct {@code setPosition} every frame (no gravity, no
 * tile collision) - it orbits a fixed point in space, disconnected from the
 * tile grid, exactly like the original's own raw {@code setX/setY}.
 */
public class OrbitingFireball extends Enemy {

    private static final int FRAME_SIZE = 16;
    private static final float ANIMATION_INTERVAL = 0.1f;

    private final float centerX;
    private final float centerY;
    private final float radius;
    private final boolean clockwise;
    private float animTimer;

    public OrbitingFireball(float centerX, float centerY, float radius, boolean clockwise) {
        super(MarioResourceManager.region("fire_ball"), FRAME_SIZE, FRAME_SIZE, centerX, centerY, false);
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = radius;
        this.clockwise = clockwise;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!isActive()) {
            return;
        }
        float angle = clockwise ? OscillatorClock.getDistance() : OscillatorClock.getInvertDistance();
        setX(centerX + (float) Math.sin(angle) * radius);
        setY(centerY + (float) Math.cos(angle) * radius);

        animTimer += delta;
        if (animTimer >= ANIMATION_INTERVAL) {
            animTimer = 0;
            nextFrame();
        }
    }

    @Override
    public void onStomped(Player player) {
        onTouchedSide(player);
    }

    @Override
    public void onTouchedSide(Player player) {
        if (!player.hasStar()) {
            player.shrink();
        }
    }

    @Override
    public void onDefeatedByProjectile() {
        // Indestructible - matches the original's empty KilledByFireBall().
    }
}

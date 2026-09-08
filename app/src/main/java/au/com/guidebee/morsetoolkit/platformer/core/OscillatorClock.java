package au.com.guidebee.morsetoolkit.platformer.core;

/**
 * Three shared, ever-advancing angles, ported from {@code Mario.java}'s own
 * {@code Distance}/{@code InvertDistance}/{@code SlowDistance} fields - ticked
 * once per frame in the original's main loop and read by every
 * {@code EnemyFireBall} (a "CW" {@code FireBar} reads {@link #getDistance()},
 * an "ACW" one reads {@link #getInvertDistance()}) and every
 * {@code FlyingTurtlePatrol} (reads {@link #getSlowDistance()}) so a level's
 * fire-bar rings/flying turtles all stay in lockstep with each other, the same
 * way the original's single shared field did.
 *
 * <p>Static (one clock, not one per instance) for the same reason
 * {@code MarioContext} is: exactly one level is active at a time.
 * {@link #reset()} must be called once per level load (see
 * {@code MarioGameScreen}'s constructor) so a replayed level doesn't inherit
 * the previous level's angle; {@link #advance} must be called exactly once
 * per frame (see {@code MarioGameScreen#render}), not once per reader, or
 * every fire-bar/flying-turtle in the level would speed up by however many of
 * them exist.
 */
public final class OscillatorClock {

    private static final float DISTANCE_STEP = 0.02f;
    private static final float INVERT_DISTANCE_STEP = 0.02f;
    private static final float SLOW_DISTANCE_STEP = 0.015f;
    private static final float WRAP_MAGNITUDE = 63f;
    private static final float PHYSICS_FPS = 60f;

    private static float distance = (float) Math.PI;
    private static float invertDistance;
    private static float slowDistance = (float) Math.PI;

    private OscillatorClock() {
    }

    public static void reset() {
        distance = (float) Math.PI;
        invertDistance = 0f;
        slowDistance = (float) Math.PI;
    }

    public static void advance(float delta) {
        float frames = delta * PHYSICS_FPS;
        distance += DISTANCE_STEP * frames;
        invertDistance -= INVERT_DISTANCE_STEP * frames;
        slowDistance += SLOW_DISTANCE_STEP * frames;
        if (distance >= WRAP_MAGNITUDE) {
            distance = 0f;
        }
        if (invertDistance < -WRAP_MAGNITUDE) {
            invertDistance = 0f;
        }
        if (slowDistance > WRAP_MAGNITUDE) {
            slowDistance = 0f;
        }
    }

    public static float getDistance() {
        return distance;
    }

    public static float getInvertDistance() {
        return invertDistance;
    }

    public static float getSlowDistance() {
        return slowDistance;
    }
}

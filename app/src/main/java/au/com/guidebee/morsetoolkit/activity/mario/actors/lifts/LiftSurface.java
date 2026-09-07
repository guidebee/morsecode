package au.com.guidebee.morsetoolkit.activity.mario.actors.lifts;

/**
 * A "solid from above only" platform surface {@code LiftCollisionResolver}
 * can catch a falling player on - extracted so both {@link Lift} and
 * {@link BalanceLiftPlatform} (which needs to keep its own rendering/physics
 * class separate from {@code Lift}'s single-platform oscillation formulas)
 * can be tracked in {@code MarioWorld}'s one lift list and resolved the same
 * way, without one having to pretend to be the other.
 */
public interface LiftSurface {

    /** This frame's horizontal movement, for carrying a rider along. */
    float getDeltaX();

    float getTopY();

    /**
     * Whether a falling rectangle (given by its own position/size) is
     * landing on this platform's top surface right now - horizontal overlap,
     * plus its bottom edge sitting at (or just above) the platform's top
     * within a small tolerance. Mirrors the original's per-frame "snap onto
     * whatever the lift's top is right now" behavior rather than a swept
     * collision test.
     */
    boolean isLandingSpot(float x, float y, int width, int height);

    /**
     * Called once per frame a rider is actually caught standing on this
     * surface - a no-op for a plain {@link Lift} (its own motion never
     * depends on whether anyone's riding it), but {@link BalanceLiftPlatform}
     * overrides this to drive its seesaw physics (see that class's doc).
     */
    default void onRidden() {
    }
}

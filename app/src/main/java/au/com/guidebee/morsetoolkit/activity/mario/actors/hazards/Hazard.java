package au.com.guidebee.morsetoolkit.activity.mario.actors.hazards;

/**
 * A contact-damage obstacle that isn't an {@code Enemy} - it can't be
 * stomped, kicked, or killed by a fireball, and doesn't award a stomp bounce;
 * touching it (from any side, unless the player has a star) just hurts.
 * Ported from the original's {@code HammerGroup} vs {@code PlayerGroup}
 * collision pair ({@code BossFire}/{@code Hammer}), which never checked for a
 * stomp the way {@code Player_EnemyGroup} did.
 */
public interface Hazard {

    boolean isActive();

    boolean overlaps(float x, float y, int width, int height);

    /** @return this hazard's current top-edge y and height - used by {@code HazardCollisionResolver} for the duck-clearance check. */
    float getY();

    float getHeight();
}

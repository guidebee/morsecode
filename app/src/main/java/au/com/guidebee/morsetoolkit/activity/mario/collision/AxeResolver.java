package au.com.guidebee.morsetoolkit.activity.mario.collision;

import au.com.guidebee.morsetoolkit.activity.mario.actors.bricks.Axe;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioWorld;

/**
 * An axe's invisible wall, checked once per frame after {@code Player}'s own
 * movement. Ported verbatim from {@code Bricks/Axe.java}'s {@code update()}:
 * {@code if (player.getX() > this.getX()) player.setX(this.getX())} - height
 * is never checked, matching the original (see {@code Axe}'s class doc).
 *
 * <p>Skipped once triggered ({@link Axe#isTriggered}) - ported from {@code
 * Player_Brick.collided}'s own axe case, which calls {@code b.setActive(false)}
 * right after triggering it; a GTGE sprite with {@code active=false} never
 * runs its own {@code update()} again, so the original's wall-clamp stops
 * right along with everything else the axe was doing. Without this check,
 * {@code MarioGameScreen#triggerAxe}'s forced walk-right command would drive
 * Mario into this same wall every single frame, permanently clamping him
 * back to the axe's own X and never letting him reach the level-end
 * checkpoint just past it - confirmed on-device (Mario visibly "stuck"
 * walking in place at the axe after triggering it, both from a debug warp
 * and from a normal playthrough).
 */
public final class AxeResolver {

    private AxeResolver() {
    }

    public static void resolve(Player player, MarioWorld world) {
        for (Axe axe : world.getAxes()) {
            if (!axe.isTriggered() && player.getX() > axe.getX()) {
                player.setX(axe.getX());
            }
        }
    }

    /** The first not-yet-triggered axe the player is touching this frame, or null - see {@code Axe#trigger}'s doc and {@code MarioGameScreen}'s own axe-handling for what happens next. */
    public static Axe findTriggered(Player player, MarioWorld world) {
        for (Axe axe : world.getAxes()) {
            if (!axe.isTriggered() && axe.overlaps(player)) {
                return axe;
            }
        }
        return null;
    }
}

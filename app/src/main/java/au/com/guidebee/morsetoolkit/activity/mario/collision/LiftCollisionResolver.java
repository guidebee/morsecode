package au.com.guidebee.morsetoolkit.activity.mario.collision;

import au.com.guidebee.morsetoolkit.activity.mario.actors.lifts.LiftSurface;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioWorld;

/**
 * Player-vs-lift collision, checked once per frame after {@code Player}'s
 * own tile-collision movement (which doesn't know about lifts at all - they
 * aren't part of {@code MarioWorld}'s tile grid). Ported from the original's
 * {@code Player_Lift.collided} - see {@code Lift}'s class doc for why a lift
 * only ever catches a falling player from above.
 */
public final class LiftCollisionResolver {

    private LiftCollisionResolver() {
    }

    public static void resolve(Player player, MarioWorld world) {
        if (!player.isFalling()) {
            return;
        }
        int width = (int) player.getWidth();
        int height = (int) player.getHeight();
        for (LiftSurface lift : world.getLifts()) {
            if (lift.isLandingSpot(player.getX(), player.getY(), width, height)) {
                player.landOnLift(lift.getTopY(), lift.getDeltaX());
                lift.onRidden();
                return;
            }
        }
    }
}

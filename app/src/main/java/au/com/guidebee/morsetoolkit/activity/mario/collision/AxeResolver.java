package au.com.guidebee.morsetoolkit.activity.mario.collision;

import au.com.guidebee.morsetoolkit.activity.mario.actors.bricks.Axe;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioWorld;

/**
 * An axe's invisible wall, checked once per frame after {@code Player}'s own
 * movement. Ported verbatim from {@code Bricks/Axe.java}'s {@code update()}:
 * {@code if (player.getX() > this.getX()) player.setX(this.getX())} - height
 * is never checked, matching the original (see {@code Axe}'s class doc).
 */
public final class AxeResolver {

    private AxeResolver() {
    }

    public static void resolve(Player player, MarioWorld world) {
        for (Axe axe : world.getAxes()) {
            if (player.getX() > axe.getX()) {
                player.setX(axe.getX());
            }
        }
    }
}

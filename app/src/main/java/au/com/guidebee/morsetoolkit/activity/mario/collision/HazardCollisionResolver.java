package au.com.guidebee.morsetoolkit.activity.mario.collision;

import java.util.Iterator;

import au.com.guidebee.morsetoolkit.activity.mario.actors.hazards.Hazard;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioWorld;

/**
 * Player-vs-hazard collision, checked once per frame after movement. Ported
 * from the original's {@code HammerGroup} vs {@code PlayerGroup} pair
 * (thrown fire/hammers): unlike {@code EnemyCollisionResolver}, there's no
 * stomp distinction and no bounce - any touch just hurts, unless the player
 * has a star, matching how a star protects against every other hazard in
 * this port.
 */
public final class HazardCollisionResolver {

    private HazardCollisionResolver() {
    }

    public static void resolve(Player player, MarioWorld world) {
        // Ported from Hammer_Player's own `p.getY() + 48` threshold - see
        // EnemyCollisionResolver's identical check and Player#isDucking's doc.
        float duckClearanceY = player.isDucking()
                ? player.getY() + Player.DUCK_OVERHEAD_CLEARANCE_PX : Float.NEGATIVE_INFINITY;

        Iterator<Hazard> it = world.getHazards().iterator();
        while (it.hasNext()) {
            Hazard hazard = it.next();
            if (!hazard.isActive()) {
                it.remove();
                continue;
            }
            if (hazard.getY() + hazard.getHeight() < duckClearanceY) {
                continue;
            }
            if (!player.hasStar() && hazard.overlaps(player.getX(), player.getY(),
                    (int) player.getWidth(), (int) player.getHeight())) {
                player.shrink();
            }
        }
    }
}

package au.com.guidebee.morsetoolkit.activity.mario.collision;

import java.util.Iterator;

import au.com.guidebee.morsetoolkit.activity.mario.actors.items.Collectible;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioWorld;

/**
 * Player-vs-item pickup resolution, checked once per frame after movement.
 * Player-vs-brick collision (solid blocking, hit-from-below) is handled
 * inline in {@code Player}'s own movement resolution instead - it's part of
 * the same tile-collision algorithm, not a separate pass, so pulling it out
 * here would mean duplicating that math rather than reusing it.
 */
public final class PlayerCollisionResolver {

    private PlayerCollisionResolver() {
    }

    public static void resolvePickups(Player player, MarioWorld world) {
        Iterator<Collectible> it = world.getCollectibles().iterator();
        while (it.hasNext()) {
            Collectible item = it.next();
            if (!item.isActive()) {
                it.remove();
                continue;
            }
            if (item.overlaps(player.getX(), player.getY(),
                    (int) player.getWidth(), (int) player.getHeight())) {
                item.onCollected(player);
                it.remove();
            }
        }
    }
}

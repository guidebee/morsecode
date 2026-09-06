package au.com.guidebee.morsetoolkit.activity.mario.actors.items;

import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;

/**
 * Anything a touch from the player (from any side - matching the original's
 * identical behavior across {@code CollidedWithMarioFromTOLeft/TORight/
 * EnemyJumperOnMario}) picks up: Mushroom, Flower, Star, Life.
 */
public interface Collectible {

    boolean isActive();

    boolean overlaps(int x, int y, int width, int height);

    void onCollected(Player player);
}

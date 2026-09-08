package au.com.guidebee.morsetoolkit.platformer.core;

import com.guidebee.game.scene.Actor;

/**
 * Shared tile-snap collision resolution for simple mobile actors (Mushroom,
 * Life, Star - anything that walks/falls and bounces off walls without
 * needing {@code Player}'s extra brick-hit-from-below wiring). Factored out
 * once three different item classes needed the exact same clamp-to-tile-edge
 * math {@code Player} already has in Step 4/5 - see docs/MARIO_PORT_PLAN.md
 * Step 5.2.
 */
public final class TileMovement {

    private TileMovement() {
    }

    /** Moves an actor horizontally, clamping at a wall. @return true if it hit a wall. */
    public static boolean moveX(Actor actor, float dx, TileCollisionSource world) {
        int width = (int) actor.getWidth();
        int height = (int) actor.getHeight();
        int tileSize = world.tileSize();
        float newX = actor.getX() + dx;
        boolean blocked = false;

        if (dx > 0 && world.containsImpassableArea(newX, actor.getY(), width, height)) {
            newX = (float) (((int) (newX + width) / tileSize) * tileSize - width);
            blocked = true;
        } else if (dx < 0 && world.containsImpassableArea(newX, actor.getY(), width, height)) {
            newX = (float) (((int) newX / tileSize + 1) * tileSize);
            blocked = true;
        }
        actor.setX(Math.max(0, newX));
        return blocked;
    }

    /** Moves an actor vertically, clamping at floor/ceiling. @return true if it landed or hit a ceiling. */
    public static boolean moveY(Actor actor, float dy, TileCollisionSource world) {
        int width = (int) actor.getWidth();
        int height = (int) actor.getHeight();
        int tileSize = world.tileSize();
        float newY = actor.getY() + dy;
        boolean blocked = false;

        if (dy > 0 && world.containsImpassableArea(actor.getX(), newY, width, height)) {
            newY = (float) (((int) (newY + height) / tileSize) * tileSize - height);
            blocked = true;
        } else if (dy < 0 && world.containsImpassableArea(actor.getX(), newY, width, height)) {
            newY = (float) (((int) newY / tileSize + 1) * tileSize);
            blocked = true;
        }
        actor.setY(newY);
        return blocked;
    }
}

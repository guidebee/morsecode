package au.com.guidebee.morsetoolkit.activity.mario.collision;

import java.util.ArrayList;
import java.util.List;

import au.com.guidebee.morsetoolkit.activity.mario.actors.enemies.Enemy;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioContext;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioWorld;

/**
 * Player-vs-enemy collision, checked once per frame after movement. Ported
 * from {@code Collusion/Player_EnemyGroup.java}'s angle-based side
 * classifier as *design*, not code: the original computed an
 * {@code atan2} angle between sprite corners and bucketed it into up/down/
 * left/right; this uses the simpler, equally standard "which axis has less
 * overlap, and is the player's center above the enemy's" AABB test instead -
 * same intent (stomp = landed on top; anything else = touched from the
 * side), less machinery.
 */
public final class EnemyCollisionResolver {

    /** Classic NES value for a stomp - awarded here (not per-enemy-type) since every ground enemy stomped so far is worth the same. */
    private static final int STOMP_SCORE = 100;

    private EnemyCollisionResolver() {
    }

    public static void resolve(Player player, MarioWorld world) {
        List<Enemy> enemies = world.getEnemies();
        enemies.removeIf(enemy -> !enemy.isActive());

        float px = player.getX();
        float py = player.getY();
        int pw = (int) player.getWidth();
        int ph = (int) player.getHeight();

        // Snapshot before iterating: a callback below (EnemyTurtle.onStomped
        // spawning a TurtleShell) appends to the live list, which would
        // otherwise throw ConcurrentModificationException mid-iteration.
        for (Enemy enemy : new ArrayList<>(enemies)) {
            if (!enemy.isActive() || !enemy.overlaps(px, py, pw, ph)) {
                continue;
            }

            float overlapX = Math.min(px + pw, enemy.getX() + enemy.getWidth()) - Math.max(px, enemy.getX());
            float overlapY = Math.min(py + ph, enemy.getY() + enemy.getHeight()) - Math.max(py, enemy.getY());
            boolean playerAbove = py + ph / 2f <= enemy.getY() + enemy.getHeight() / 2f;

            if (overlapY <= overlapX && playerAbove) {
                enemy.onStomped(player);
                player.bounceOffEnemy();
                MarioContext.gameState().addScore(STOMP_SCORE);
            } else {
                enemy.onTouchedSide(player);
            }
        }
    }
}

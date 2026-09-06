package au.com.guidebee.morsetoolkit.activity.mario.collision;

import java.util.ArrayList;
import java.util.Iterator;

import au.com.guidebee.morsetoolkit.activity.mario.actors.enemies.Enemy;
import au.com.guidebee.morsetoolkit.activity.mario.actors.projectiles.FireBall;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioWorld;

/**
 * Fireball-vs-enemy collision, checked once per frame. Fireball-vs-terrain
 * (wall/ground bounce) is handled inline in {@code FireBall}'s own movement,
 * the same scoping choice as {@code PlayerCollisionResolver}: movement
 * collision stays with the mover, this resolver only covers the "hits
 * something else" case.
 */
public final class ProjectileCollisionResolver {

    private ProjectileCollisionResolver() {
    }

    public static void resolve(MarioWorld world) {
        Iterator<FireBall> it = world.getFireBalls().iterator();
        while (it.hasNext()) {
            FireBall fireBall = it.next();
            if (!fireBall.isActive()) {
                it.remove();
                continue;
            }
            float fx = fireBall.getX();
            float fy = fireBall.getY();
            int fw = (int) fireBall.getWidth();
            int fh = (int) fireBall.getHeight();

            // Snapshot: an onDefeatedByProjectile() override that spawns a new
            // enemy (none do today, but EnemyCollisionResolver got bitten by
            // this exact shape of bug once already) must not corrupt this
            // iteration with a ConcurrentModificationException.
            for (Enemy enemy : new ArrayList<>(world.getEnemies())) {
                if (enemy.isActive() && enemy.overlaps(fx, fy, fw, fh)) {
                    enemy.onDefeatedByProjectile();
                    fireBall.explode();
                    break;
                }
            }
        }
    }
}

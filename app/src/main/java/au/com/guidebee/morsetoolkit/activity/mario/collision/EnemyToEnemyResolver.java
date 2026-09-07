package au.com.guidebee.morsetoolkit.activity.mario.collision;

import java.util.List;

import au.com.guidebee.morsetoolkit.activity.mario.actors.enemies.Enemy;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioWorld;

/**
 * Enemy-vs-enemy contact, checked once per frame - ported from {@code
 * Collusion/EnemyToEnemy.java}'s own left/right cases: two enemies that touch
 * each other turn around, same as bouncing off a wall (see {@code
 * Enemy#bouncesOffEnemies}'s own doc for which types react at all; items -
 * mushroom/flower/life/star/coin - are excluded structurally in this port,
 * since they're a different actor list entirely, matching the original's own
 * explicit item-type exclusion). A kicked/moving shell killing whatever it
 * touches (the original's own {@code CollidedWithMovingShell} case) is
 * already handled elsewhere, by {@code TurtleShell}/{@code HelmetShell}'s own
 * {@code killOverlappingEnemies} - not duplicated here.
 *
 * <p>Only reverses a pair while they're still moving toward each other (see
 * {@link #approaching}) - the original's collision callback fires once per
 * contact event; this port's per-frame overlap re-check would otherwise flip
 * the same pair back and forth every frame for as long as they stayed
 * overlapping.
 */
public final class EnemyToEnemyResolver {

    private EnemyToEnemyResolver() {
    }

    public static void resolve(MarioWorld world) {
        List<Enemy> enemies = world.getEnemies();
        for (int i = 0; i < enemies.size(); i++) {
            Enemy a = enemies.get(i);
            if (!a.isActive()) {
                continue;
            }
            for (int j = i + 1; j < enemies.size(); j++) {
                Enemy b = enemies.get(j);
                if (!b.isActive() || !a.overlaps(b.getX(), b.getY(), (int) b.getWidth(), (int) b.getHeight())) {
                    continue;
                }
                if (a.bouncesOffEnemies() && approaching(a, b)) {
                    a.reverseDirection();
                }
                if (b.bouncesOffEnemies() && approaching(b, a)) {
                    b.reverseDirection();
                }
            }
        }
    }

    private static boolean approaching(Enemy mover, Enemy other) {
        return mover.isMovingRight() ? other.getX() >= mover.getX() : other.getX() <= mover.getX();
    }
}

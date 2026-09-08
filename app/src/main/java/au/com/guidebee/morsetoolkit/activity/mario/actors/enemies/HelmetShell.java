package au.com.guidebee.morsetoolkit.activity.mario.actors.enemies;

import com.guidebee.game.graphics.TextureRegion;

import java.util.ArrayList;

import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.fx.FallingDeadSprite;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioContext;
import au.com.guidebee.morsetoolkit.platformer.core.TileMovement;

/**
 * A Helmet's shell, ported from {@code Objects/HelmetShell.java} +
 * {@code Objects/MovingHelmetShell.java} - unified into one class with a
 * {@code moving} flag, exactly the same shape as {@link TurtleShell} (which
 * unifies the turtle's own stationary/moving shell pair the same way): a
 * stomp or side-touch while stationary kicks it into motion; a stomp while
 * moving stops it back to stationary; a side-touch while moving hurts the
 * player (or kills the shell, with a star). Unlike {@code TurtleShell},
 * {@code color} is an explicit 3-way choice ("normal"/"dark"/"white") the
 * original passes down from whichever level's {@code Helmet} spawned it,
 * not a level-attribute lookup.
 *
 * <p>The "helmet"/"helmet_shell" regions are 32x32 per frame - confirmed
 * against the source PNGs directly (unlike the turtle family, not taller
 * than one tile).
 */
public class HelmetShell extends Enemy {

    private static final float GRAVITY = 5f;
    private static final float MOVING_SPEED = 5f;

    private final String color;
    private boolean moving;

    public HelmetShell(float x, float y, String color, boolean movingRight) {
        super(regionFor(color), 32, 32, x, y, movingRight);
        this.color = color;
    }

    private static TextureRegion regionFor(String color) {
        if ("dark".equals(color)) {
            return MarioResourceManager.region("helmet_shell_dark");
        }
        if ("white".equals(color)) {
            return MarioResourceManager.region("helmet_shell_white");
        }
        return MarioResourceManager.region("helmet_shell");
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!isActive()) {
            return;
        }
        if (moving) {
            walkAndFall(delta, GRAVITY, MOVING_SPEED);
            killOverlappingEnemies();
        } else {
            TileMovement.moveY(this, GRAVITY * delta * PHYSICS_FPS, MarioContext.world());
        }
    }

    private void killOverlappingEnemies() {
        // Snapshot: see EnemyCollisionResolver's class doc.
        for (Enemy other : new ArrayList<>(MarioContext.world().getEnemies())) {
            if (other != this && other.isActive()
                    && other.overlaps(getX(), getY(), (int) getWidth(), (int) getHeight())) {
                other.onDefeatedByProjectile();
            }
        }
    }

    @Override
    public void onStomped(Player player) {
        if (moving) {
            moving = false;
        } else {
            kick(player);
        }
    }

    @Override
    public void onTouchedSide(Player player) {
        if (!moving) {
            kick(player);
        } else if (player.hasStar()) {
            deactivate();
        } else {
            player.shrink();
        }
    }

    private void kick(Player player) {
        moving = true;
        movingRight = player.getX() < getX();
    }

    /**
     * Ported from {@code Objects/HelmetShell.java}'s own {@code
     * CollidedWithMovingShell} (a kicked shell hitting this one) - falls with
     * a "smb_kick" and the drift-away animation, matching {@link
     * TurtleShell}'s own equivalent. Note a real behavioral gap versus the
     * original left as-is here: the original's own {@code KilledByFireBall()}
     * is a no-op (a Helmet-family shell is immune to fireballs, matching
     * {@link Helmet}'s own immunity), but this port's single {@code
     * onDefeatedByProjectile} hook can't yet tell "hit by a fireball" apart
     * from "hit by a moving shell" the way the original's two separate
     * methods could - fixing that needs threading a trigger-kind distinction
     * through {@code ProjectileCollisionResolver}, out of scope for this pass
     * (see docs/MARIO_PORT_PLAN_PHASE2.md S7).
     */
    @Override
    public void onDefeatedByProjectile() {
        MarioResourceManager.sound("smb_kick").play();
        FallingDeadSprite.spawn(getX(), getY(), regionFor(color).split(32, 32)[0][0]);
        deactivate();
    }
}

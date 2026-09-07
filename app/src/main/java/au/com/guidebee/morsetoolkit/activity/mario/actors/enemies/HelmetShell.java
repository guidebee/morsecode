package au.com.guidebee.morsetoolkit.activity.mario.actors.enemies;

import com.guidebee.game.graphics.TextureRegion;

import java.util.ArrayList;

import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioContext;
import au.com.guidebee.morsetoolkit.activity.mario.world.TileMovement;

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
}

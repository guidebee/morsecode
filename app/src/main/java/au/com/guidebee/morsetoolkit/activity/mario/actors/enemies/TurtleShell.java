package au.com.guidebee.morsetoolkit.activity.mario.actors.enemies;

import com.guidebee.game.graphics.TextureRegion;

import java.util.ArrayList;

import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.fx.FallingDeadSprite;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioContext;
import au.com.guidebee.morsetoolkit.platformer.core.TileMovement;

/**
 * A turtle shell, ported from {@code Objects/TurtelShell.java} and
 * {@code Objects/MovingTurtelShell.java} - unified into one class with a
 * {@code moving} flag instead of the original's two separate classes (which
 * only existed because the original engine had no way to swap a live
 * object's behavior, so it destroyed and recreated the sprite instead).
 *
 * <p>Stationary: sits in place under gravity only, no horizontal drift.
 * Touched (stomp or side) while stationary: kicked - starts sliding away
 * from the player. Touched while moving: a stomp stops it back to
 * stationary; a side-touch hurts the player (unless they have a star, in
 * which case the shell dies) - a moving shell is dangerous, matching the
 * original. While moving, it also kills any other enemy it touches.
 */
public class TurtleShell extends Enemy {

    private static final float GRAVITY = 6f;
    private static final float MOVING_SPEED = 5f;

    private final String attribute;
    private final int tileSize;
    private boolean moving;

    public TurtleShell(float x, float y, String attribute, boolean movingRight, int tileSize) {
        super(regionFor(attribute), tileSize, tileSize, x, y, movingRight);
        this.attribute = attribute;
        this.tileSize = tileSize;
    }

    private static TextureRegion regionFor(String attribute) {
        return MarioResourceManager.region("UnderGround".equals(attribute) ? "turtle_shell_dark" : "turtle_shell");
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
        // Snapshot: see EnemyCollisionResolver's class doc - a callback that
        // appends to the live enemies list mid-iteration throws
        // ConcurrentModificationException, so a defensive copy is cheap insurance
        // even though no current onDefeatedByProjectile() override does that.
        for (Enemy other : new ArrayList<>(MarioContext.world().getEnemies())) {
            if (other != this && other.isActive()
                    && other.overlaps(getX(), getY(), (int) getWidth(), (int) getHeight())) {
                other.onDefeatedByProjectile();
            }
        }
    }

    @Override
    public boolean onStomped(Player player) {
        if (moving) {
            moving = false;
        } else {
            kick(player);
        }
        return true;
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

    /** Ported from {@code Objects/TurtelShell.java}'s own {@code KilledByFireBall} - see {@code FallingDeadSprite}'s class doc. */
    @Override
    public void onDefeatedByProjectile() {
        MarioResourceManager.sound("smb_kick").play();
        FallingDeadSprite.spawn(getX(), getY(), regionFor(attribute).split(tileSize, tileSize)[0][0]);
        deactivate();
    }
}

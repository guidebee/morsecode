package au.com.guidebee.morsetoolkit.activity.mario.actors.projectiles;

import com.guidebee.game.microedition.Sprite;

import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.fx.Explosion;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioContext;
import au.com.guidebee.morsetoolkit.platformer.core.TileMovement;

/**
 * Fire Mario's fireball, ported from {@code Objects/FireBall.java}: launches
 * horizontally already at terminal fall speed (the original starts
 * {@code Gravity} at its own cap, so it only ever ramps back up to that
 * *after* a bounce, never before), bounces off the ground, and explodes
 * against a wall or after falling well past the level's bottom.
 *
 * <p>The "fire_ball" region is 16x16 per frame (64x16 total) - smaller than
 * most sprites here, confirmed against the source PNG rather than assumed
 * (several other enemy/projectile sprites turned out non-32x32 too - see
 * {@code EnemyTurtle}'s class doc).
 */
public class FireBall extends Sprite {

    private static final int FRAME_SIZE = 16;
    private static final float PHYSICS_FPS = 60f;
    private static final float GRAVITY_STEP = 1f;
    private static final float GRAVITY_CAP = 8f;
    private static final float BOUNCE_IMPULSE = -8f;
    private static final float SPEED = 8f;
    private static final float ANIMATION_INTERVAL = 0.1f;
    private static final float FALL_OUT_MARGIN_PX = 200f;

    private final boolean movingRight;
    private boolean active = true;
    private float gravity = GRAVITY_CAP;
    private float animTimer;

    public FireBall(float x, float y, boolean movingRight) {
        super(MarioResourceManager.region("fire_ball"), FRAME_SIZE, FRAME_SIZE);
        setPosition(x, y);
        this.movingRight = movingRight;
    }

    public boolean isActive() {
        return active;
    }

    public boolean overlaps(float x, float y, int width, int height) {
        return active
                && x < getX() + getWidth() && x + width > getX()
                && y < getY() + getHeight() && y + height > getY();
    }

    /** Silent removal - the level's own fall-out cleanup (ported from the original's own {@code getY() > 700} check), and the base every other explode variant below calls into. */
    public void explode() {
        active = false;
        remove();
    }

    /** Ported from {@code FireBallToEnemys.collided} - every enemy hit spawns an {@link Explosion}, but (unlike a wall hit) plays no sound of its own. */
    public void explodeAgainstEnemy() {
        Explosion.spawn(getX(), getY());
        explode();
    }

    /** Ported from {@code FireBallToBricks.collided}'s own left/right cases - a wall hit plays "smb_bump" in addition to the {@link Explosion}, unlike an enemy hit. */
    private void explodeAgainstWall() {
        MarioResourceManager.sound("smb_bump").play();
        Explosion.spawn(getX(), getY());
        explode();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!active) {
            return;
        }
        float frames = delta * PHYSICS_FPS;

        boolean wasFalling = gravity >= 0;
        if (gravity < GRAVITY_CAP) {
            gravity = Math.min(GRAVITY_CAP, gravity + GRAVITY_STEP * frames);
        }
        if (TileMovement.moveY(this, gravity * frames, MarioContext.world()) && wasFalling) {
            gravity = BOUNCE_IMPULSE;
        }
        if (TileMovement.moveX(this, (movingRight ? SPEED : -SPEED) * frames, MarioContext.world())) {
            explodeAgainstWall();
            return;
        }
        if (getY() > MarioContext.world().getHeightPx() + FALL_OUT_MARGIN_PX) {
            explode();
            return;
        }

        animTimer += delta;
        if (animTimer >= ANIMATION_INTERVAL) {
            animTimer = 0;
            nextFrame();
        }
    }
}

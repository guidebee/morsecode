package au.com.guidebee.morsetoolkit.activity.mario.actors.items;

import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioContext;
import au.com.guidebee.morsetoolkit.platformer.core.TileMovement;

/**
 * The invincibility star, ported from {@code Objects/Star.java}: bounces
 * like a ball (re-launches upward every time it lands) while drifting
 * sideways, turning around at walls. The original's bounce was triggered
 * externally by its collision manager landing it on a brick; here it
 * bounces itself whenever {@link TileMovement#moveY} reports a downward
 * landing.
 */
public class Star extends CollectibleItem {

    private static final float PHYSICS_FPS = 60f;
    private static final float WALK_SPEED = 1.5f;
    private static final float GRAVITY_STEP = 0.25f;
    private static final float GRAVITY_CAP = 5f;
    private static final float BOUNCE_IMPULSE = -6f;
    private static final float ANIMATION_INTERVAL = 0.1f;

    private boolean movingRight = true;
    private float gravity = -5f;
    private float animTimer;

    public Star(float x, float y) {
        super(MarioResourceManager.region("star"), 32, 32, x, y);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!isActive()) {
            return;
        }
        float frames = delta * PHYSICS_FPS;

        if (gravity < GRAVITY_CAP) {
            gravity = Math.min(GRAVITY_CAP, gravity + GRAVITY_STEP * frames);
        }
        boolean wasFalling = gravity >= 0;
        if (TileMovement.moveY(this, gravity * frames, MarioContext.world()) && wasFalling) {
            gravity = BOUNCE_IMPULSE;
        }
        if (TileMovement.moveX(this, (movingRight ? WALK_SPEED : -WALK_SPEED) * frames, MarioContext.world())) {
            movingRight = !movingRight;
        }

        animTimer += delta;
        if (animTimer >= ANIMATION_INTERVAL) {
            animTimer = 0;
            nextFrame();
        }
    }

    @Override
    public void onCollected(Player player) {
        player.collectStar();
        MarioResourceManager.sound("smb_powerup").play();
        collect();
    }
}

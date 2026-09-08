package au.com.guidebee.morsetoolkit.activity.mario.actors.items;

import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioContext;
import au.com.guidebee.morsetoolkit.platformer.core.TileMovement;

/**
 * The growth-mushroom power-up, ported from {@code Objects/Mashroom.java}:
 * walks at a constant pace, falls under gravity, and turns around at walls.
 * Touching it from any side grows the player (matches the original's
 * identical behavior across all its Mario-collision callbacks).
 */
public class Mushroom extends CollectibleItem {

    private static final float PHYSICS_FPS = 60f;
    private static final float WALK_SPEED = 2f;
    private static final float GRAVITY_STEP = 0.5f;
    private static final float GRAVITY_CAP = 5f;
    private static final float ANIMATION_INTERVAL = 0.3f;

    private boolean movingRight = true;
    private float gravity;
    private float animTimer;

    public Mushroom(float x, float y) {
        super(MarioResourceManager.region("mashrooms"), 32, 32, x, y);
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
        if (TileMovement.moveY(this, gravity * frames, MarioContext.world())) {
            gravity = 0;
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
        player.grow();
        collect();
    }
}

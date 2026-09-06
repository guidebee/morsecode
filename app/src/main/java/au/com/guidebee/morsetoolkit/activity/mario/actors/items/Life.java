package au.com.guidebee.morsetoolkit.activity.mario.actors.items;

import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioContext;
import au.com.guidebee.morsetoolkit.activity.mario.world.TileMovement;

/**
 * The 1UP (extra life) power-up, ported from {@code Objects/Life.java}:
 * walks and falls like {@link Mushroom}. The original never actually stops
 * this falling (its own {@code update()} calls {@code moveY(Gravity)}
 * unconditionally with no floor check - only the original's now-unported
 * external collision manager kept it from sinking through the ground), so
 * this port resolves ground/wall collision itself via {@link TileMovement}
 * instead of replicating that fall-through.
 *
 * <p>TODO Step 8: credit an extra life via {@code GameStateController} once
 * it exists, instead of only playing the pickup sound.
 */
public class Life extends CollectibleItem {

    private static final float PHYSICS_FPS = 60f;
    private static final float WALK_SPEED = 2f;
    private static final float GRAVITY = 8f;
    private static final float ANIMATION_INTERVAL = 0.3f;

    private boolean movingRight = true;
    private float animTimer;

    public Life(float x, float y) {
        super(MarioResourceManager.region("one_up"), 32, 32, x, y);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!isActive()) {
            return;
        }
        float frames = delta * PHYSICS_FPS;

        TileMovement.moveY(this, GRAVITY * frames, MarioContext.world());
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
        MarioResourceManager.sound("smb_1-up").play();
        collect();
    }
}

package au.com.guidebee.morsetoolkit.activity.mario.actors.enemies;

import com.guidebee.game.graphics.TextureRegion;

import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;

/**
 * The "jumping fish" ambient hazard for non-Sea levels with the
 * {@code FlyingFishes} flag set (World 2/7's Level_23/73, World 8's
 * Level_843 - none of them Sea attribute, see {@code SpawnController}'s own
 * doc), ported from {@code Objects/FishyGround.java}'s ambient (no
 * explicit-image) constructor: launches from just off the bottom of the
 * level in an upward arc (gravity ramps from a strong upward kick back
 * toward positive, same shape as a thrown projectile), always swims
 * (visually) in one fixed horizontal direction for its whole brief
 * lifetime, and despawns once it falls back down past the bottom.
 *
 * <p>Always uses the "FishRed" art regardless of {@code type} (confirmed by
 * reading the original constructor - both direction branches load "FishRed",
 * never "FishGrey"), flipped horizontally for a rightward launch - same
 * convention as {@link Rocket}'s own {@code regionFor}.
 *
 * <p>The original's despawn check also ORs in two screen-relative
 * off-either-side conditions; skipped here since the vertical arc (spawned
 * near the bottom, launched up, falls back past it) always completes well
 * before this enemy's slow (0.5-1.5px/tick) horizontal drift could carry it
 * off-screen - see {@code SpawnController}'s own spawn-offset doc for the
 * "lands near the player, not on top of them" placement this class doesn't
 * itself compute.
 */
public class FishyGround extends Enemy {

    private static final int FRAME_SIZE = 32;
    private static final float ANIM_INTERVAL = 300f / 60f;
    private static final float GRAVITY_START = -7.65f;
    private static final float GRAVITY_STEP = 0.06f;
    private static final float GRAVITY_CAP = 8f;
    /** Ported from the original's own {@code this.getY() > 576}. */
    private static final float DESPAWN_Y = 576f;

    private final float speedX;
    private float gravity = GRAVITY_START;
    private float animTimer;
    private boolean showingFirstFrame = true;

    public FishyGround(float x, float y, boolean movingRight, float speedMagnitude) {
        super(regionFor(movingRight), FRAME_SIZE, FRAME_SIZE, x, y, movingRight);
        this.speedX = movingRight ? speedMagnitude : -speedMagnitude;
        setFrame(0);
    }

    private static TextureRegion regionFor(boolean movingRight) {
        TextureRegion region = MarioResourceManager.region("fish_red");
        if (movingRight) {
            TextureRegion flipped = new TextureRegion(region);
            flipped.flip(true, false);
            return flipped;
        }
        return region;
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
        setY(getY() + gravity * frames);
        setX(getX() + speedX * frames);

        if (getY() > DESPAWN_Y) {
            deactivate();
            return;
        }

        animTimer += delta;
        if (animTimer >= ANIM_INTERVAL) {
            animTimer = 0;
            showingFirstFrame = !showingFirstFrame;
            setFrame(showingFirstFrame ? 0 : 1);
        }
    }

    // onStomped/onTouchedSide both match Enemy's own defaults exactly (a
    // plain stomp kills it, matching MarioJumpedOnEnemy(); a side touch
    // hurts Mario unless starred, matching CollidedWithMarioFromTOLeft/
    // ToRight) - see this class's own doc for how that differs from
    // FishyWater/OctoPussy's own always-hurt override.

    @Override
    public void onDefeatedByProjectile() {
        MarioResourceManager.sound("smb_kick").play();
        deactivate();
    }
}

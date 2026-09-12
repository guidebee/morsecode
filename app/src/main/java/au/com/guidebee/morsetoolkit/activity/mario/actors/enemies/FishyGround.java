package au.com.guidebee.morsetoolkit.activity.mario.actors.enemies;

import au.com.guidebee.morsetoolkit.activity.mario.MarioConfiguration;
import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.fx.FallingDeadSprite;

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
 * never "FishGrey"), mirrored for a rightward launch - see the constructor's
 * own doc for why that's a {@link #setTransform} rather than a pre-flipped
 * region, same fix {@link Rocket} needed.
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

    private static final float ANIM_INTERVAL = 300f / 60f;
    private static final float GRAVITY_START = -7.65f;
    private static final float GRAVITY_STEP = 0.06f;
    private static final float GRAVITY_CAP = 8f;
    /** Ported from the original's own {@code this.getY() > 576}. */
    private static final float DESPAWN_Y = 576f;

    private final float speedX;
    private final int tileSize;
    private float gravity = GRAVITY_START;
    private float animTimer;
    private boolean showingFirstFrame = true;

    public FishyGround(float x, float y, boolean movingRight, float speedMagnitude, int tileSize) {
        super(MarioResourceManager.region("fish_red"), tileSize, tileSize, x, y, movingRight);
        this.speedX = movingRight ? speedMagnitude : -speedMagnitude;
        this.tileSize = tileSize;
        // Flipping the region before handing it to Sprite's constructor (as
        // this used to do, mirroring Rocket's own former mistake - see that
        // class's constructor doc) never actually took effect: Sprite's
        // constructor immediately re-slices the region via
        // TextureRegion.split(), which always rebuilds sub-regions from raw
        // pixel coordinates and discards any flip already applied to the
        // region being split. Mirroring via the Sprite's own transform
        // instead applies at draw time, after slicing, so it actually works.
        if (movingRight) {
            setTransform(TRANS_MIRROR);
        }
        setFrame(0);
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
        FallingDeadSprite.spawn(getX(), getY(),
                MarioResourceManager.region("fish_red")
                        .split(tileSize * MarioConfiguration.ART_SCALE, tileSize * MarioConfiguration.ART_SCALE)[0][0]);
        deactivate();
    }
}

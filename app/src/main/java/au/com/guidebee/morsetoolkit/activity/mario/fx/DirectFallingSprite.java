package au.com.guidebee.morsetoolkit.activity.mario.fx;

import com.guidebee.game.graphics.TextureRegion;
import com.guidebee.game.microedition.Sprite;

import au.com.guidebee.morsetoolkit.activity.mario.world.MarioContext;

/**
 * A stomped enemy's corpse falling in place, ported from {@code
 * Animations/DirectFalling.java} - used by several original enemies'
 * {@code MarioJumpedOnEnemy()} (and, for {@code Objects/Boss.java}, every one
 * of its death reactions - star stomp/touch, moving shell, out of fireball
 * hits) where a stomp/kill doesn't remove the enemy silently but sends its
 * (vertically-flipped) sprite falling before it's gone. Deliberately a
 * separate class from {@link FallingDeadSprite} (which ports the original's
 * *other* death animation, {@code Animations/FallingDeadSprites.java}, used
 * for fireball/shell kills, not stomps): the two aren't the same animation
 * with different parameters, they have genuinely different original tuning -
 * gravity ramps up from a standing start here (the original's {@code
 * Gravity = 0} field default) rather than {@link FallingDeadSprite}'s initial
 * upward kick, and only {@link #spawnDrifting} drifts at all.
 *
 * <p>Flips vertically via {@link #setTransform}, not by flipping the region
 * before handing it to {@code Sprite}'s constructor the way an earlier
 * version of {@link FallingDeadSprite}/{@code Rocket} did - see {@code
 * Rocket}'s own constructor doc for why that doesn't work ({@code
 * TextureRegion.split()}, which {@code Sprite}'s constructor calls
 * internally, discards a flip applied before it).
 *
 * <p>The original's {@code DirectFalling} only ever flips its image
 * *vertically* - any horizontal facing is already baked into whichever frame
 * the dying enemy's own {@code getImage()} happened to be showing (a
 * left/right frame pair, e.g. {@code Boss}'s own {@code setAnimationFrame(0,
 * 1)} vs {@code (4, 5)}), never a property of {@code DirectFalling} itself.
 * {@link #spawn(float, float, TextureRegion)} matches that: pass whichever
 * representative region already has the right pose baked in (same "doesn't
 * attempt to reproduce the exact live frame" simplification {@link
 * FallingDeadSprite} itself already documents) and only a plain vertical
 * flip is applied. {@link Rocket} is the one exception in this port with no
 * baked left/right frame at all - its own facing is a {@code
 * setTransform(TRANS_MIRROR)} applied at *render* time (see its own
 * constructor doc), so {@link #spawn(float, float, TextureRegion, boolean)}
 * exists specifically for it, to fold that runtime mirror into the vertical
 * flip: flipping both axes together is equivalent to a plain 180-degree
 * rotation, so a rightward-facing corpse uses {@code TRANS_ROT180} instead of
 * stacking {@code TRANS_MIRROR} and {@code TRANS_MIRROR_ROT180}, which {@code
 * Sprite}'s single-{@code transform}-field design can't represent directly.
 */
public class DirectFallingSprite extends Sprite {

    /** Ported from the original's own literal {@code moveX(1)}/{@code moveX(-1)} - only {@link #spawnDrifting} uses this. */
    private static final float DRIFT_SPEED = 1f;
    private static final float GRAVITY_STEP = 0.25f;
    private static final float GRAVITY_CAP = 3f;
    private static final float PHYSICS_FPS = 60f;
    private static final float FALL_OUT_MARGIN_PX = 200f;

    private final float driftSpeed;
    private float gravity;

    private DirectFallingSprite(float x, float y, TextureRegion region, int transform, float driftSpeed) {
        super(region);
        setPosition(x, y);
        setTransform(transform);
        this.driftSpeed = driftSpeed;
    }

    /** Straight down, no drift - for an enemy whose region already has the correct left/right facing baked in (see the class doc). */
    public static void spawn(float x, float y, TextureRegion region) {
        MarioContext.spawn(new DirectFallingSprite(x, y, region, TRANS_MIRROR_ROT180, 0f));
    }

    /** Straight down, no drift, but also folds in a runtime-mirror-only sprite's current facing - see the class doc; {@link Rocket} is this port's only caller. */
    public static void spawn(float x, float y, TextureRegion region, boolean movingRight) {
        MarioContext.spawn(new DirectFallingSprite(x, y, region,
                movingRight ? TRANS_ROT180 : TRANS_MIRROR_ROT180, 0f));
    }

    /**
     * Drifts horizontally while falling - ported from {@code DirectFalling}'s
     * 4-arg constructor, whose {@code ToRight} flag (here, {@code
     * driftRight}) only ever controls this drift, never the image's own flip
     * (still just a plain vertical flip - see the class doc). {@code
     * Objects/Monkey.java}'s own {@code MarioJumpedOnEnemy()} is this port's
     * only caller, passing its own {@code MariotoRight()}.
     */
    public static void spawnDrifting(float x, float y, TextureRegion region, boolean driftRight) {
        MarioContext.spawn(new DirectFallingSprite(x, y, region, TRANS_MIRROR_ROT180,
                driftRight ? DRIFT_SPEED : -DRIFT_SPEED));
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        float frames = delta * PHYSICS_FPS;
        setX(getX() + driftSpeed * frames);
        if (gravity < GRAVITY_CAP) {
            gravity = Math.min(GRAVITY_CAP, gravity + GRAVITY_STEP * frames);
        }
        setY(getY() + gravity * frames);
        if (getY() > MarioContext.world().getHeightPx() + FALL_OUT_MARGIN_PX) {
            remove();
        }
    }
}

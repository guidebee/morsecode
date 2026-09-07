package au.com.guidebee.morsetoolkit.activity.mario.fx;

import com.guidebee.game.graphics.TextureRegion;
import com.guidebee.game.microedition.Sprite;

import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioContext;

/**
 * An enemy's upside-down corpse drifting off screen after a fireball/shell
 * kill, ported from {@code Animations/FallingDeadSprites.java}: flips the
 * enemy's own current-pose region vertically, drifts it horizontally at a
 * constant speed while gravity pulls it down (an initial upward flip of
 * {@code -10}, ramping to a {@code +10} cap - the original's own numbers,
 * same shape as {@code Player}'s own death-fall), then removes itself once
 * clear of the level.
 *
 * <p>Doesn't attempt to reproduce whichever exact animation frame the enemy
 * happened to be showing at the moment of death (the original grabs whatever
 * {@code getImage()} returned, effectively arbitrary too since none of its
 * callers pick a specific frame either) - callers pass one representative
 * region (typically frame 0 of their own idle strip).
 */
public class FallingDeadSprite extends Sprite {

    private static final float DRIFT_SPEED = 2f;
    private static final float GRAVITY_START = -10f;
    private static final float GRAVITY_STEP = 0.5f;
    private static final float GRAVITY_CAP = 10f;
    private static final float PHYSICS_FPS = 60f;
    private static final float FALL_OUT_MARGIN_PX = 200f;

    private final float driftSpeed;
    private float gravity = GRAVITY_START;

    private FallingDeadSprite(float x, float y, TextureRegion region, boolean driftRight) {
        super(flipVertical(region));
        setPosition(x, y);
        driftSpeed = driftRight ? DRIFT_SPEED : -DRIFT_SPEED;
    }

    private static TextureRegion flipVertical(TextureRegion region) {
        TextureRegion flipped = new TextureRegion(region);
        flipped.flip(false, true);
        return flipped;
    }

    /**
     * Ported from the original's own (confusingly-named) {@code ToRight}
     * flag: every real call site but {@link #spawnDriftingRight} passes
     * {@code MariotoRight()} in, which the original's {@code update()} then
     * inverts - net effect, the corpse drifts *away* from wherever Mario is
     * standing, not toward him. Derived here instead of passed in, so every
     * caller is just "here's my region, here's where I died."
     */
    public static void spawn(float x, float y, TextureRegion region) {
        Player player = MarioContext.player();
        boolean marioToRight = player.getX() >= x;
        MarioContext.spawn(new FallingDeadSprite(x, y, region, !marioToRight));
    }

    /** Ported from {@code Objects/Rocket.java}'s own literal {@code false} argument - always drifts right regardless of Mario's position, unlike every other caller. */
    public static void spawnDriftingRight(float x, float y, TextureRegion region) {
        MarioContext.spawn(new FallingDeadSprite(x, y, region, true));
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        float frames = delta * PHYSICS_FPS;
        setX(getX() + driftSpeed * frames);
        if (gravity < GRAVITY_CAP) {
            gravity += GRAVITY_STEP * frames;
        }
        setY(getY() + gravity * frames);
        if (getY() > MarioContext.world().getHeightPx() + FALL_OUT_MARGIN_PX) {
            remove();
        }
    }
}

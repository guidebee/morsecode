package au.com.guidebee.morsetoolkit.activity.mario.fx;

import com.guidebee.game.graphics.TextureRegion;
import com.guidebee.game.microedition.Sprite;

import au.com.guidebee.morsetoolkit.activity.mario.MarioConfiguration;
import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.PlayerPowerState;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioContext;

/**
 * The sliding double shown entering a pipe, ported from {@code Animations
 * /MarioGoingInPump.java}/{@code MarioGoingDownAnimation.java}:
 * {@code MarioGameScreen#beginTransition} hides the real {@link Player} for
 * a pipe checkpoint and spawns this stand-in in its place, for the same
 * duration the transition already holds for before the level actually
 * switches (a deliberate simplification of the original's own two different
 * literal tick counts - 70 for horizontal, 100 for vertical - to this port's
 * own single, already-established {@code PIPE_ENTRY_SECONDS}), then removes
 * itself.
 *
 * <p>A horizontal entry ({@link #horizontal}) animates the walk-right cycle
 * while sliding right, matching the original's own animated double
 * ({@code setAnimationFrame(4, 6)} - frames 4-6 are exactly Player's own
 * walk-right cycle, see that class's own frame-layout doc); a vertical entry
 * shows a single static standing pose while sliding down, also matching the
 * original (its own equivalent is a plain, unanimated image snapshot - which
 * pose it froze on wasn't meaningful enough to reproduce exactly, the same
 * simplification {@link MarioGhost} already makes for its own snapshot).
 */
public class PipeEntryAnimation extends Sprite {

    private static final float SLIDE_SPEED_PX_PER_SEC = 60f;
    private static final float ANIM_FRAME_INTERVAL = 0.2f;
    /** Walk-right cycle - see {@link Player}'s own frame-layout doc. */
    private static final int[] WALK_FRAMES = {4, 5, 6};
    /** Standing-right pose - see {@link MarioGhost}'s own matching frame index. */
    private static final int STANDING_FRAME = 0;

    private final boolean horizontal;
    private float lifetime;
    private float animTimer;
    private int walkFrameIndex;

    /** {@code frameWidth}/{@code frameHeight} are world-space, per {@link Player}'s {@code PlayerPowerState}. */
    private PipeEntryAnimation(TextureRegion region, int frameWidth, int frameHeight,
                                float x, float y, boolean horizontal, float duration) {
        super(region, frameWidth * MarioConfiguration.ART_SCALE, frameHeight * MarioConfiguration.ART_SCALE);
        setSize(frameWidth, frameHeight);
        setPosition(x, y);
        this.horizontal = horizontal;
        this.lifetime = duration;
        setFrame(horizontal ? WALK_FRAMES[0] : STANDING_FRAME);
    }

    public static void spawn(Player player, boolean horizontal, float duration) {
        PlayerPowerState state = player.getPowerState();
        TextureRegion region = MarioResourceManager.region(state.regionName);
        MarioContext.spawn(new PipeEntryAnimation(region, state.width, state.height,
                player.getX(), player.getY(), horizontal, duration));
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (horizontal) {
            setX(getX() + SLIDE_SPEED_PX_PER_SEC * delta);
            animTimer += delta;
            if (animTimer >= ANIM_FRAME_INTERVAL) {
                animTimer = 0;
                walkFrameIndex = (walkFrameIndex + 1) % WALK_FRAMES.length;
                setFrame(WALK_FRAMES[walkFrameIndex]);
            }
        } else {
            setY(getY() + SLIDE_SPEED_PX_PER_SEC * delta);
        }

        lifetime -= delta;
        if (lifetime <= 0) {
            remove();
        }
    }
}

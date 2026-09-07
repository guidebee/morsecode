package au.com.guidebee.morsetoolkit.activity.mario.actors.bricks;

import com.guidebee.game.microedition.Sprite;

import au.com.guidebee.morsetoolkit.activity.mario.MarioConfiguration;
import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;

/**
 * A permanent, non-interactive block, ported from {@code Bricks/Iron.java}.
 * Two roles in the original: the "used up" replacement left behind after a
 * Bank/QuestionMark/BrickWithStar/InvisibleBrck is exhausted, and (in later
 * World-1 levels, e.g. Level 14) a directly-placed static obstacle.
 *
 * <p>The "iron" region is the original's 4-frame Sea/Ground/UnderGround/
 * Castle strip; frame 0 (Sea) is unused in World 1. The original actually
 * picks between only 2 of these 4 frames for the "used up" case (a minor
 * inconsistency - see the class's inline note in the original source), which
 * this port doesn't replicate: it always uses the correct theme frame.
 *
 * <p>Every Iron hops up then settles right after spawning - ported from the
 * original's own {@code Gravity}-ramp {@code update()} (starts at -5,
 * increments +1/tick, moves each tick until it reaches +4) - using the same
 * simplified time-based parabola {@link Brick}'s own bump animation already
 * uses for the same reason (see that class's own doc): reads as the same
 * kind of hop either way, without replicating the original's tick-by-tick
 * gravity dance verbatim. Reachable every single time any brick in the game
 * gets exhausted, not just the level-placed Iron blocks - confirmed missing
 * entirely by a later audit pass; an earlier version of this class was
 * purely static.
 */
public class Iron extends InteractiveBrick {

    private static final int FRAME_GROUND = 1;
    private static final int FRAME_UNDERGROUND = 2;
    private static final int FRAME_CASTLE = 3;

    private static final float PHYSICS_FPS = 60f;
    /** ~9 original ticks - matches {@link Brick}'s own bump-parabola duration. */
    private static final float BUMP_DURATION_TICKS = 9f;
    private static final float BUMP_PEAK_OFFSET_PX = 10f;

    private final float restY;
    private float bumpTicks = 0;

    public Iron(float x, float y, String attribute) {
        super(MarioResourceManager.region("iron"),
                MarioConfiguration.TILE_SIZE, MarioConfiguration.TILE_SIZE, x, y);
        setFrame(frameFor(attribute));
        this.restY = y;
    }

    private static int frameFor(String attribute) {
        if ("UnderGround".equals(attribute)) {
            return FRAME_UNDERGROUND;
        }
        if ("Castle".equals(attribute)) {
            return FRAME_CASTLE;
        }
        return FRAME_GROUND;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (bumpTicks < 0) {
            return;
        }
        bumpTicks += delta * PHYSICS_FPS;
        if (bumpTicks >= BUMP_DURATION_TICKS) {
            bumpTicks = -1;
            setY(restY);
        } else {
            float t = bumpTicks / BUMP_DURATION_TICKS;
            setY(restY - BUMP_PEAK_OFFSET_PX * 4f * t * (1f - t));
        }
    }
}

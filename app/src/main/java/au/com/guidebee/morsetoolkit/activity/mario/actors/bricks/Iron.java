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
 */
public class Iron extends InteractiveBrick {

    private static final int FRAME_GROUND = 1;
    private static final int FRAME_UNDERGROUND = 2;
    private static final int FRAME_CASTLE = 3;

    public Iron(float x, float y, String attribute) {
        super(MarioResourceManager.region("iron"),
                MarioConfiguration.TILE_SIZE, MarioConfiguration.TILE_SIZE, x, y);
        setFrame(frameFor(attribute));
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
}

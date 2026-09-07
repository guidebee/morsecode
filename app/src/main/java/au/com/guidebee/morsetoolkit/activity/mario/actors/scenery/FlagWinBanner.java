package au.com.guidebee.morsetoolkit.activity.mario.actors.scenery;

import com.guidebee.game.graphics.Batch;
import com.guidebee.game.graphics.TextureRegion;
import com.guidebee.game.microedition.Layer;

import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;

/**
 * The small banner that rises beside the castle once Mario reaches the
 * level's real end-of-level checkpoint - ported from {@code Animations
 * .FlagWin.java}: spawned at {@code (x-24, 9*32)} and rises 1px/tick until
 * {@code y <= 7*32}. Every level places its castle at the same relative spot
 * (confirmed by reading every converted level JSON's "CheckPoints" checkpoint
 * y, always 384), so those two literals are kept as offsets from the
 * triggering checkpoint's own y instead of copying the original's absolute
 * ones, in case a level ever differs.
 */
public class FlagWinBanner extends Layer {

    private static final float START_OFFSET_Y = -96f;
    private static final float RISE_DISTANCE = 64f;
    private static final float RISE_SPEED = 1f;
    private static final float PHYSICS_FPS = 60f;

    private final TextureRegion region = MarioResourceManager.region("flag_win");
    private final float stopY;

    public FlagWinBanner(float checkpointX, float checkpointY) {
        super(checkpointX - 24f, checkpointY + START_OFFSET_Y, 32, 32, true);
        stopY = checkpointY + START_OFFSET_Y - RISE_DISTANCE;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (getY() <= stopY) {
            return;
        }
        setY(Math.max(stopY, getY() - RISE_SPEED * delta * PHYSICS_FPS));
    }

    @Override
    public void paint(Batch g) {
        g.draw(region, getX(), getY(), getWidth(), getHeight());
    }
}

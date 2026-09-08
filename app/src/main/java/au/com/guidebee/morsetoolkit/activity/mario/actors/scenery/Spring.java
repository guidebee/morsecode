package au.com.guidebee.morsetoolkit.activity.mario.actors.scenery;

import com.guidebee.game.microedition.Sprite;

import au.com.guidebee.morsetoolkit.activity.mario.MarioConfiguration;
import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;

/**
 * The decorative spring coil rendered one tile above a {@code
 * actors.bricks.Bouncer}, ported from {@code Bricks/Spring.java}: sits at
 * rest (frame 0) until {@link #play} triggers its squish-and-recover strip
 * once, then automatically stops back at rest - matching the original's own
 * {@code PlayAnimation()}/{@code AnimatedSprite.updateAnimation()} default
 * (no explicit {@code setLoopAnim(true)} call in the source, so a
 * non-looping playthrough is the real, verified behavior, not an assumption).
 *
 * <p>{@code Bouncer} calls {@link #play} from {@code Player
 * #moveYWithCollision}'s own launch branch - the actual moment a player
 * bounces - rather than reproducing the original's own separate,
 * geometry-dependent Spring-tile collision (the original's {@code
 * Player_Brick.collided} dispatches on whichever brick Mario's own hitbox
 * happens to overlap; for Big/Fire Mario that can include the Spring tile
 * sitting just above the Bouncer he lands on, for Small Mario it's border-
 * line - tying this to the Bouncer's own always-reliable launch event avoids
 * depending on that incidental overlap). {@code 60ms} per frame - the
 * original's own default {@code AnimatedSprite} timer, never overridden for
 * this class in the source.
 */
public class Spring extends Sprite {

    private static final float FRAME_INTERVAL = 60f / 1000f;
    private static final int[] SQUISH_FRAMES = {0, 1, 2, 2, 1, 0};

    private int sequenceIndex = -1;
    private float frameTimer;

    public Spring(float x, float y, int tileSize) {
        super(MarioResourceManager.region("spring"),
                tileSize * MarioConfiguration.ART_SCALE, tileSize * 2 * MarioConfiguration.ART_SCALE);
        setSize(tileSize, tileSize * 2);
        setPosition(x, y);
    }

    /** Plays the squish-and-recover strip once, matching {@code PlayAnimation()} - a no-op if already mid-play. */
    public void play() {
        if (sequenceIndex >= 0) {
            return;
        }
        sequenceIndex = 0;
        frameTimer = 0;
        setFrame(SQUISH_FRAMES[0]);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (sequenceIndex < 0) {
            return;
        }
        frameTimer += delta;
        if (frameTimer < FRAME_INTERVAL) {
            return;
        }
        frameTimer = 0;
        sequenceIndex++;
        if (sequenceIndex >= SQUISH_FRAMES.length) {
            sequenceIndex = -1;
            setFrame(0);
        } else {
            setFrame(SQUISH_FRAMES[sequenceIndex]);
        }
    }
}

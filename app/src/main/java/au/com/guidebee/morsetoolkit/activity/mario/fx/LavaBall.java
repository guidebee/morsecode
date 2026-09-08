package au.com.guidebee.morsetoolkit.activity.mario.fx;

import com.guidebee.game.microedition.Sprite;

import java.util.Random;

import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;

/**
 * A ball of lava that erupts, arcs, and falls back at a fixed point, then
 * pauses before erupting again - ported from {@code Objects/LavaBall.java}.
 * Purely decorative: every {@code BasicEnemy} touch callback in the original
 * (stomp, side-touch, fireball) is an empty override, confirmed by reading
 * the source rather than assumed - unlike every other hazard in this port,
 * touching this never hurts Mario.
 *
 * <p>Ignores the level data's own y entirely - the original's constructor
 * calls {@code super(x, 14*32)}, never reading the {@code y} parameter it's
 * given (a hardcoded rest/erupt row, not level data); this port matches that
 * rather than "fixing" it into reading y, since the original level author
 * placed the level-data marker at a specific tile only to record where the
 * ball spawns *horizontally* - AddLavaBall(x)'s own hardcoded y=6 is never
 * meant as a real height. Also purely vertical - no horizontal movement, no
 * level (tile) collision at all, matching the original's raw {@code moveY}
 * with no wall/floor check.
 *
 * <p>The "lava_ball" region is 32x32 per frame, 2 frames (64x32 total) -
 * confirmed against the source PNG directly.
 */
public class LavaBall extends Sprite {

    private static final float PHYSICS_FPS = 60f;
    private static final float GRAVITY_STEP = 0.4f;
    private static final float GRAVITY_CAP = 10f;
    private static final float SINK_Y = 500f;

    private static final Random RANDOM = new Random();

    private float gravity = -10f;
    private float eruptTimer;

    public LavaBall(float x, int tileSize) {
        super(MarioResourceManager.region("lava_ball"), tileSize, tileSize);
        setPosition(x, 14 * tileSize);
        setFrame(0);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        float frames = delta * PHYSICS_FPS;

        if (gravity < GRAVITY_CAP) {
            gravity += GRAVITY_STEP * frames;
        }
        if (getY() > SINK_Y) {
            eruptTimer -= frames;
            if (eruptTimer < 0) {
                eruptTimer = (2 + RANDOM.nextInt(6)) * 20;
                setY(SINK_Y);
                gravity = -14 - RANDOM.nextInt(4);
            }
        } else {
            setY(getY() + gravity * frames);
        }
        setFrame(gravity > 0 ? 0 : 1);
    }
}

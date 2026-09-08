package au.com.guidebee.morsetoolkit.activity.mario.fx;

import com.guidebee.game.microedition.Sprite;

import au.com.guidebee.morsetoolkit.activity.mario.MarioConfiguration;
import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioContext;

/**
 * The ambient swim bubble, ported from {@code Animations/Bubble.java}:
 * spawned at Mario's current position while he's underwater (see {@code
 * Player}'s own bubble timer), rises straight up, and vanishes once it
 * clears the water surface line - the same absolute 64px line {@code Player}
 * itself surfaces at (see that class's own {@code WATER_SURFACE_Y}), matched
 * here rather than re-derived. Purely decorative - no gameplay effect,
 * matching the original.
 */
public class Bubble extends Sprite {

    private static final int FRAME_WIDTH = 8;
    private static final int FRAME_HEIGHT = 14;
    private static final float PHYSICS_FPS = 60f;
    private static final float FRAME_INTERVAL = 100f / 1000f;
    private static final int[] FRAME_SEQUENCE = {0, 1, 2, 3, 2, 1, 0};
    /** Ported from the original's own literal {@code moveY(-1)}. */
    private static final float RISE_SPEED = 1f;
    /** Ported from the original's own literal {@code getY() < 64}. */
    private static final float SURFACE_Y = 64f;

    private float frameTimer;

    private Bubble(float x, float y) {
        super(MarioResourceManager.region("bubble"),
                FRAME_WIDTH * MarioConfiguration.ART_SCALE, FRAME_HEIGHT * MarioConfiguration.ART_SCALE);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setFrameSequence(FRAME_SEQUENCE);
        setPosition(x, y);
    }

    public static void spawn(float x, float y) {
        MarioContext.spawn(new Bubble(x, y));
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        setY(getY() - RISE_SPEED * delta * PHYSICS_FPS);
        if (getY() < SURFACE_Y) {
            remove();
            return;
        }

        frameTimer += delta;
        if (frameTimer >= FRAME_INTERVAL) {
            frameTimer = 0;
            nextFrame();
        }
    }
}

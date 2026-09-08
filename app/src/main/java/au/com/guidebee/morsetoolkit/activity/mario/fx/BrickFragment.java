package au.com.guidebee.morsetoolkit.activity.mario.fx;

import com.guidebee.game.microedition.Sprite;

import au.com.guidebee.morsetoolkit.activity.mario.MarioConfiguration;
import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioContext;

/**
 * One of the four physics-driven debris pieces spawned when Big/Fire Mario
 * breaks a brick, ported from {@code Animations/BrickPeaces.java} (spawned via
 * {@code Mario.addSomeBrickFragmends}). All four instances play the same
 * 2-frame flipbook (a little spin, not four distinct fragment looks) sliced
 * from the theme's row of the "brick_peaces" strip (row 1 = Ground/Sea, row 2
 * = UnderGround, row 3 = Castle - row 0 is unused, matching the original's
 * {@code BrickPeaces[2]/[3]}, {@code [4]/[5]}, {@code [6]/[7]} indices into
 * its row-major-sliced 2x4 sheet), but launch with different initial
 * horizontal speed/gravity - see {@link #spawnBreak}.
 */
public class BrickFragment extends Sprite {

    private static final int FRAME_SIZE = 16;
    private static final float PHYSICS_FPS = 60f;
    private static final float GRAVITY_STEP = 1f;
    private static final float GRAVITY_CAP = 10f;
    private static final float ANIMATION_INTERVAL = 0.2f;
    private static final float FALL_OUT_MARGIN_PX = 200f;

    private final float xSpeed;
    private float gravity;
    private float animTimer;

    private BrickFragment(float x, float y, float xSpeed, float gravity, int[] frameSequence) {
        super(MarioResourceManager.region("brick_peaces"),
                FRAME_SIZE * MarioConfiguration.ART_SCALE, FRAME_SIZE * MarioConfiguration.ART_SCALE);
        setSize(FRAME_SIZE, FRAME_SIZE);
        setFrameSequence(frameSequence);
        // Centers the 16x16 fragment within the 32x32 brick tile - matches
        // the original's `setLocation(x + 8, y + 8)`.
        setPosition(x + 8, y + 8);
        this.xSpeed = xSpeed;
        this.gravity = gravity;
    }

    /** Ported from {@code Mario.addSomeBrickFragmends} - the exact 4 velocity/gravity pairs and the break sound. */
    public static void spawnBreak(float brickX, float brickY, String attribute) {
        int[] frames = themeFrames(attribute);
        MarioResourceManager.sound("smb_breakblock").play();
        MarioContext.spawn(new BrickFragment(brickX, brickY, -4f, -16f, frames));
        MarioContext.spawn(new BrickFragment(brickX, brickY, 4f, -16f, frames));
        MarioContext.spawn(new BrickFragment(brickX, brickY, -3f, -12f, frames));
        MarioContext.spawn(new BrickFragment(brickX, brickY, 3f, -12f, frames));
    }

    private static int[] themeFrames(String attribute) {
        if ("UnderGround".equals(attribute)) {
            return new int[]{4, 5};
        }
        if ("Castle".equals(attribute)) {
            return new int[]{6, 7};
        }
        return new int[]{2, 3};
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        float frames = delta * PHYSICS_FPS;

        if (gravity < GRAVITY_CAP) {
            gravity = Math.min(GRAVITY_CAP, gravity + GRAVITY_STEP * frames);
        }
        setY(getY() + gravity * frames);
        setX(getX() + xSpeed * frames);

        if (getY() > MarioContext.world().getHeightPx() + FALL_OUT_MARGIN_PX) {
            remove();
            return;
        }

        animTimer += delta;
        if (animTimer >= ANIMATION_INTERVAL) {
            animTimer = 0;
            nextFrame();
        }
    }
}

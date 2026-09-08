package au.com.guidebee.morsetoolkit.activity.mario.actors.projectiles;

import com.guidebee.game.microedition.Sprite;

import java.util.Random;

import au.com.guidebee.morsetoolkit.activity.mario.MarioConfiguration;
import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.actors.hazards.Hazard;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioContext;

/**
 * A hammer thrown by a "hammer bro"-mode {@code Boss} or by {@code Monkey},
 * ported from {@code Objects/Hammer.java}'s two constructors: {@link
 * #Hammer(float, float, float, float)} is {@code Boss}'s own continuous
 * throw (externally-computed speed/gravity, always leftward in the
 * original); {@link #Hammer(float, float, boolean)} is {@code Monkey}'s own
 * one-shot throw (random speed/gravity picked fresh per throw, direction
 * toward wherever the player currently is). Arcs under gravity and constant
 * horizontal drift either way; never bounces or collides with terrain -
 * only ever disappears by falling past the level's bottom.
 *
 * <p>The original's {@code Xspeedinvert}/{@code PositiveX} fields are dead
 * code on {@code Boss}'s own throw path (that constructor always leaves
 * {@code PositiveX} false, so the {@code moveX(Xspeedinvert)} branch never
 * runs) - only {@code Xspeed} matters there, so this class skips both;
 * {@code Monkey}'s throw path is what actually exercises the
 * "toward the player" direction choice. Likewise the original's small
 * per-throw jitter on {@code Boss}'s path (`Utility.getRandom(-10, 10) / 10`,
 * integer division of an int in [-10,10] by 10) rounds to zero the
 * overwhelming majority of the time and is skipped rather than replicated
 * as a near-no-op.
 *
 * <p>The "bw_hammer" region is 28x28 per frame, 4 frames (112x28 total) -
 * confirmed against {@code WholeGame.java}'s
 * {@code getImages("CloudsNight/Hammer.png", 4, 1)} slicing call. Despite the
 * "BW" (black-and-white/{@code CloudsNight}) source path, the original always
 * loads this one image regardless of level theme (see
 * {@code tools/mario-atlas-packer}'s own note on this) - preserved as-is.
 */
public class Hammer extends Sprite implements Hazard {

    private static final int FRAME_WIDTH = 28;
    private static final int FRAME_HEIGHT = 28;
    private static final float PHYSICS_FPS = 60f;
    private static final float GRAVITY_STEP = 0.2f;
    private static final float GRAVITY_CAP = 4f;
    private static final float ANIMATION_INTERVAL = 0.1f;
    private static final float FALL_OUT_MARGIN_PX = 200f;

    private static final Random RANDOM = new Random();

    private final float xSpeed;
    private float gravity;
    private boolean active = true;
    private float animTimer;

    /**
     * @param xSpeed  horizontal speed, world px per original-60fps-tick (negative = leftward) - ported from {@code Boss}'s {@code Hammerxspeed}.
     * @param gravity initial vertical speed - ported from {@code Boss}'s {@code HammerGravity}.
     */
    public Hammer(float x, float y, float xSpeed, float gravity) {
        super(MarioResourceManager.region("bw_hammer"),
                FRAME_WIDTH * MarioConfiguration.ART_SCALE, FRAME_HEIGHT * MarioConfiguration.ART_SCALE);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setPosition(x, y);
        this.xSpeed = xSpeed;
        this.gravity = gravity;
    }

    /**
     * Ported from {@code Monkey}'s own throw ({@code Objects/Hammer.java}'s
     * first constructor): fresh random speed/gravity every throw, direction
     * toward wherever the player is right now.
     *
     * @param towardLeft true if the player is currently to the left (ported from {@code Monkey.HammerThrow}'s own `player.getX() < this.getX()` check).
     */
    public Hammer(float x, float y, boolean towardLeft) {
        this(x, y, towardLeft ? -(2 + RANDOM.nextInt(2)) : (2 + RANDOM.nextInt(2)), -4 - RANDOM.nextInt(4));
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!active) {
            return;
        }
        float frames = delta * PHYSICS_FPS;
        if (gravity < GRAVITY_CAP) {
            gravity = Math.min(GRAVITY_CAP, gravity + GRAVITY_STEP * frames);
        }
        setY(getY() + gravity * frames);
        setX(getX() + xSpeed * frames);

        if (getY() > MarioContext.world().getHeightPx() + FALL_OUT_MARGIN_PX) {
            active = false;
            remove();
            return;
        }
        animTimer += delta;
        if (animTimer >= ANIMATION_INTERVAL) {
            animTimer = 0;
            nextFrame();
        }
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public boolean overlaps(float x, float y, int width, int height) {
        return active
                && x < getX() + getWidth() && x + width > getX()
                && y < getY() + getHeight() && y + height > getY();
    }
}

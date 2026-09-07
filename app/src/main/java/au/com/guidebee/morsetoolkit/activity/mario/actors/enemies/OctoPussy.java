package au.com.guidebee.morsetoolkit.activity.mario.actors.enemies;

import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioContext;

/**
 * A Bloober/octopus analog for Sea levels, ported from {@code Objects/OctoPussy.java}:
 * a bob-and-dart chaser, not a straight swimmer like {@link FishyWater}. Its
 * cycle: rest at a target point for a while (drifting slowly downward and
 * blinking between two frames while waiting), then dart toward a new target
 * point offset from its current position (away from Mario horizontally, and
 * upward only if Mario is currently above it - otherwise it just waits a
 * very long time instead, per the original's own {@code Wait=4000} branch),
 * repeating once reached; if it ever drifts down past
 * {@link #RISE_TRIGGER_Y}, it immediately re-targets upward regardless of
 * the wait timer. Never safely stompable - see {@link FishyWater}'s own doc
 * for why.
 *
 * <p>The original's dart movement uses GTGE's own
 * {@code Sprite.moveTo(long, x, y, speed)}, whose exact interpolation isn't
 * available to inspect (its library source isn't part of this workspace) -
 * {@link #stepToward} instead moves in a straight line toward the target at
 * a constant px/frame speed (re-derived from the same {@code 0.1} literal
 * the original passed), which reproduces the original's *behavior* (rest,
 * dart, repeat) even if the exact per-frame stepping isn't byte-identical.
 */
public class OctoPussy extends Enemy {

    private static final int FRAME_WIDTH = 32;
    private static final int FRAME_HEIGHT = 48;
    /** Ported from the original's own {@code this.getY() > 9*32}. */
    private static final float RISE_TRIGGER_Y = 9 * 32f;
    private static final float DART_OFFSET_PX = 64f;
    private static final float RISE_OFFSET_PX = 64f;
    private static final float WAIT_RISE_OFFSET_PX = 32f;
    private static final float MOVE_SPEED = 0.1f;
    private static final float REACHED_EPSILON = 0.5f;
    /** Ticks to rest once a target is reached - matches {@code Wait = 40}. */
    private static final int WAIT_TICKS = 40;
    /** Effectively "never" (until the rise-trigger check above fires) - matches {@code Wait = 4000}. */
    private static final int WAIT_TICKS_STUCK = 4000;
    private static final float DRIFT_SPEED = 1f;

    private float targetX;
    private float targetY;
    private float waitTicks;
    private float downFrame;

    public OctoPussy(float x, float y) {
        super(MarioResourceManager.region("octopussy"), FRAME_WIDTH, FRAME_HEIGHT, x, y, false);
        targetX = x - DART_OFFSET_PX;
        targetY = y;
        setFrame(0);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!isActive()) {
            return;
        }
        float frames = delta * PHYSICS_FPS;
        Player player = MarioContext.player();

        if (getY() > RISE_TRIGGER_Y) {
            waitTicks = 0;
            targetX = getX() + (player.getX() < getX() ? -RISE_OFFSET_PX : RISE_OFFSET_PX);
            targetY = getY() - RISE_OFFSET_PX;
            downFrame = 0;
        }
        boolean reachedTarget = Math.abs(getX() - targetX) < REACHED_EPSILON
                && Math.abs(getY() - targetY) < REACHED_EPSILON;
        if (reachedTarget) {
            waitTicks = WAIT_TICKS;
            targetX = getX() + (player.getX() < getX() ? -RISE_OFFSET_PX : RISE_OFFSET_PX);
            if (player.getY() < getY()) {
                targetY = getY() - WAIT_RISE_OFFSET_PX;
            } else {
                waitTicks = WAIT_TICKS_STUCK;
            }
            downFrame = 0;
        }

        if (waitTicks <= 0) {
            stepToward(targetX, targetY, MOVE_SPEED * frames);
        } else {
            waitTicks -= frames;
            setY(getY() + DRIFT_SPEED * frames);
            downFrame += frames;
            setFrame(downFrame > 5 && downFrame < 20 ? 1 : 0);
        }
    }

    private void stepToward(float tx, float ty, float step) {
        float dx = tx - getX();
        float dy = ty - getY();
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist <= step || dist == 0f) {
            setX(tx);
            setY(ty);
        } else {
            setX(getX() + dx / dist * step);
            setY(getY() + dy / dist * step);
        }
    }

    @Override
    public void onStomped(Player player) {
        onTouchedSide(player);
    }

    @Override
    public void onDefeatedByProjectile() {
        MarioResourceManager.sound("smb_kick").play();
        deactivate();
    }
}

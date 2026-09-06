package au.com.guidebee.morsetoolkit.activity.mario.actors.player;

import com.guidebee.game.microedition.Sprite;

import au.com.guidebee.morsetoolkit.activity.mario.MarioConfiguration;
import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.input.MarioInputController;
import au.com.guidebee.morsetoolkit.activity.mario.input.PlayerCommand;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioWorld;

/**
 * Small-Mario physics/state machine, ported from the original engine's
 * {@code Objects/Player.java} as *design* rather than transcribed line for
 * line (per docs/MARIO_PORT_PLAN.md SS1 - every API call changes even though
 * the algorithm carries over). Growth/shrink (big/fire), ducking and
 * swimming are out of scope for Step 4 (see docs/MARIO_PORT_PLAN.md Step
 * 4.2) - this is exactly enough to run, jump and collide as small Mario.
 *
 * <h2>Why "frames" instead of raw delta seconds</h2>
 * The original's constants (accel +-2, friction -+1, gravity step +0.42,
 * jump impulse -11, position += speed/20) were all tuned as "per update()
 * call", implicitly assuming a fixed ~60fps loop - GTGE's update(long) never
 * scaled anything by elapsed time either. Rather than re-derive each constant
 * into a px/sec figure (and risk subtly changing the feel), every original
 * constant is kept verbatim and scaled by {@code frames = delta * PHYSICS_FPS}
 * ("how many original 60fps ticks did this real frame cover") - at exactly
 * 60fps this reduces to the original formulas exactly, and it degrades
 * gracefully at other frame rates instead of the original's frame-rate-
 * dependent behavior.
 */
public class Player extends Sprite {

    private static final float PHYSICS_FPS = 60f;

    private static final float ACCEL = 2f;
    private static final float FRICTION = 1f;
    private static final float MAX_SPEED = 60f;

    private static final float GRAVITY_STEP = 0.42f;
    private static final float GRAVITY_CAP = 10f;
    private static final float JUMP_BASE = -11f;
    private static final float JUMP_SPEED_BONUS_DIVISOR = 60f;

    private static final float WALK_CYCLE_THRESHOLD = 160f;

    private final MarioWorld world;
    private final MarioInputController input;

    /** Original's abstract "speed" unit - not px/sec, see the class doc. */
    private float speed;
    /** Original's "Gravity" unit - vertical speed in px per 60fps tick. */
    private float gravity;
    private boolean onGround;
    private boolean facingRight = true;

    private float walkCycleAccumulator;
    private int walkCyclePos;

    public Player(float x, float y, MarioWorld world, MarioInputController input) {
        super(MarioResourceManager.region(PlayerPowerState.SMALL.regionName),
                PlayerPowerState.SMALL.width, PlayerPowerState.SMALL.height);
        setPosition(x, y);
        this.world = world;
        this.input = input;
        setFrame(0);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        PlayerCommand command = input.poll();
        float frames = delta * PHYSICS_FPS;

        applyHorizontalInput(command, frames);
        applyJump(command);
        applyGravity(frames);

        moveXWithCollision(speed / 20f * frames);
        moveYWithCollision(gravity * frames);

        updateAnimation(command, frames);
    }

    private void applyHorizontalInput(PlayerCommand command, float frames) {
        if (command.left) {
            facingRight = false;
            speed = Math.max(speed - ACCEL * frames, -MAX_SPEED);
        } else if (command.right) {
            facingRight = true;
            speed = Math.min(speed + ACCEL * frames, MAX_SPEED);
        } else if (speed > 0) {
            speed = Math.max(0, speed - FRICTION * frames);
        } else if (speed < 0) {
            speed = Math.min(0, speed + FRICTION * frames);
        }
    }

    private void applyJump(PlayerCommand command) {
        if (!command.jumpPressed || !onGround) {
            return;
        }
        if (speed > 0) {
            gravity = JUMP_BASE - speed / JUMP_SPEED_BONUS_DIVISOR;
        } else if (speed < 0) {
            gravity = JUMP_BASE + speed / JUMP_SPEED_BONUS_DIVISOR;
        } else {
            gravity = JUMP_BASE;
        }
        onGround = false;
        MarioResourceManager.sound("smb_jump-small").play();
    }

    private void applyGravity(float frames) {
        if (gravity < GRAVITY_CAP) {
            gravity = Math.min(GRAVITY_CAP, gravity + GRAVITY_STEP * frames);
        }
    }

    private void moveXWithCollision(float dx) {
        int width = (int) getWidth();
        int height = (int) getHeight();
        float newX = Math.max(0, getX() + dx);
        int tileSize = MarioConfiguration.TILE_SIZE;

        if (dx > 0 && world.containsImpassableArea((int) newX, (int) getY(), width, height)) {
            newX = (float) (((int) (newX + width) / tileSize) * tileSize - width);
            speed = 0;
        } else if (dx < 0 && world.containsImpassableArea((int) newX, (int) getY(), width, height)) {
            newX = (float) (((int) newX / tileSize + 1) * tileSize);
            speed = 0;
        }
        setX(newX);
    }

    private void moveYWithCollision(float dy) {
        int width = (int) getWidth();
        int height = (int) getHeight();
        float newY = getY() + dy;
        int tileSize = MarioConfiguration.TILE_SIZE;

        if (dy > 0) {
            if (world.containsImpassableArea((int) getX(), (int) newY, width, height)) {
                newY = (float) (((int) (newY + height) / tileSize) * tileSize - height);
                gravity = 0;
                onGround = true;
            } else {
                onGround = false;
            }
        } else if (dy < 0 && world.containsImpassableArea((int) getX(), (int) newY, width, height)) {
            newY = (float) (((int) newY / tileSize + 1) * tileSize);
            gravity = 0;
        }
        setY(newY);
    }

    /**
     * Frame layout in the "player" region (4 cols x 7 rows, matching the
     * original's frame indices): 0/1 = idle right/left, 2/3 = airborne
     * right/left, 4-6 = walk-right cycle, 7 = skid-right, 8-10 = walk-left
     * cycle, 11 = skid-left. This mirrors the original's frame-selection
     * *behavior* (idle/airborne/skid/cadence-proportional-to-speed walk
     * cycle) rather than transcribing its exact accumulator bookkeeping.
     */
    private void updateAnimation(PlayerCommand command, float frames) {
        if (!onGround) {
            setFrame(facingRight ? 2 : 3);
            return;
        }
        if (speed == 0) {
            walkCycleAccumulator = 0;
            setFrame(facingRight ? 0 : 1);
            return;
        }
        boolean skidding = (command.right && speed < 0) || (command.left && speed > 0);
        if (skidding) {
            setFrame(facingRight ? 7 : 11);
            return;
        }
        walkCycleAccumulator += Math.abs(speed) * frames;
        if (walkCycleAccumulator > WALK_CYCLE_THRESHOLD) {
            walkCycleAccumulator = 0;
            walkCyclePos = (walkCyclePos + 1) % 3;
        }
        setFrame((facingRight ? 4 : 8) + walkCyclePos);
    }

    public boolean isOnGround() {
        return onGround;
    }
}

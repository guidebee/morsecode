package au.com.guidebee.morsetoolkit.activity.mario.actors.player;

import com.guidebee.game.graphics.Batch;
import com.guidebee.game.graphics.TextureRegion;
import com.guidebee.game.microedition.Layer;

import au.com.guidebee.morsetoolkit.activity.mario.MarioConfiguration;
import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.actors.bricks.InteractiveBrick;
import au.com.guidebee.morsetoolkit.activity.mario.actors.projectiles.FireBall;
import au.com.guidebee.morsetoolkit.activity.mario.input.MarioInputController;
import au.com.guidebee.morsetoolkit.activity.mario.input.PlayerCommand;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioContext;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioWorld;

/**
 * Mario's physics/state machine, ported from the original engine's
 * {@code Objects/Player.java} as *design* rather than transcribed line for
 * line (per docs/MARIO_PORT_PLAN.md SS1 - every API call changes even though
 * the algorithm carries over).
 *
 * <h2>Why this draws via Batch directly instead of composing a Sprite</h2>
 * Growing/shrinking swaps Mario's entire image strip and frame size (32x32
 * "player" vs 32x64 "big_player"/"fire_player"). The original's GTGE Sprite
 * supported that via {@code setImages(BufferedImage[])} at runtime; this
 * engine's {@code microedition.Sprite} binds its region/frame-grid at
 * construction with no equivalent setter, so {@code Player} extends
 * {@code Layer} directly (same base {@code Sprite}/{@code TiledLayer}
 * extend) rather than one fixed-size {@code Sprite}. Rendering uses
 * {@code TextureRegion.split(...)} (the same slicing {@code Sprite} does
 * internally) plus a direct {@code Batch.draw(...)} call in {@link #paint}
 * - not a second, separately-constructed {@code Sprite} instance that's
 * never added to the {@code LayerManager} itself, since a Sprite's
 * rendering path is wired through being a stage member, and one that isn't
 * one is untested territory this class doesn't need to depend on.
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
public class Player extends Layer {

    private static final float PHYSICS_FPS = 60f;

    private static final float ACCEL = 2f;
    private static final float FRICTION = 1f;
    private static final float MAX_SPEED = 60f;

    private static final float GRAVITY_STEP = 0.42f;
    private static final float GRAVITY_CAP = 10f;
    private static final float JUMP_BASE = -11f;
    private static final float JUMP_SPEED_BONUS_DIVISOR = 60f;

    private static final float WALK_CYCLE_THRESHOLD = 160f;

    /** ~400 original 60fps ticks - matches Player.java's post-shrink `invincible = 400`. */
    private static final float INVINCIBLE_SECONDS = 400f / 60f;
    /** ~1000 original 60fps ticks - matches Player.java's `STAR()`'s `delay = 1000`. */
    private static final float STAR_SECONDS = 1000f / 60f;
    /** How far past the level's bottom edge counts as "fallen into a pit" - matches {@code FireBall}'s own fall-out margin. */
    private static final float FALL_OUT_MARGIN_PX = 200f;

    private final MarioWorld world;
    private final MarioInputController input;
    private final float spawnX;
    private final float spawnY;

    private TextureRegion[][] frameRegions;
    private int frameCols;
    private int frame;
    private PlayerPowerState powerState = PlayerPowerState.SMALL;

    /** Original's abstract "speed" unit - not px/sec, see the class doc. */
    private float speed;
    /** Original's "Gravity" unit - vertical speed in px per 60fps tick. */
    private float gravity;
    private boolean onGround;
    private boolean facingRight = true;

    private float walkCycleAccumulator;
    private int walkCyclePos;

    private float invincibleTimer;
    private float starTimer;

    /** Set while a level-complete/pipe-entry sequence drives Mario instead of the player - see {@code MarioGameScreen}. */
    private PlayerCommand forcedCommand;
    private PlayerCommand lastCommand;

    /** Set by {@link #die()}, cleared by {@link #consumeDeath()} - see that method's doc. */
    private boolean justDied;

    public Player(float x, float y, MarioWorld world, MarioInputController input) {
        super(x, y, PlayerPowerState.SMALL.width, PlayerPowerState.SMALL.height, true);
        this.world = world;
        this.input = input;
        this.spawnX = x;
        this.spawnY = y;
        initFrames(powerState);
    }

    private void initFrames(PlayerPowerState state) {
        TextureRegion region = MarioResourceManager.region(state.regionName);
        frameRegions = region.split(state.width, state.height);
        frameCols = region.getRegionWidth() / state.width;
        frame = 0;
    }

    @Override
    public void paint(Batch g) {
        TextureRegion region = frameRegions[frame / frameCols][frame % frameCols];
        g.draw(region, getX(), getY(), getWidth(), getHeight());
    }

    private void setFrame(int frame) {
        this.frame = frame;
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (invincibleTimer > 0) {
            invincibleTimer -= delta;
        }
        if (starTimer > 0) {
            starTimer -= delta;
        }

        PlayerCommand command = forcedCommand != null ? forcedCommand : input.poll();
        lastCommand = command;
        float frames = delta * PHYSICS_FPS;

        applyHorizontalInput(command, frames);
        applyJump(command);
        applyGravity(frames);

        moveXWithCollision(speed / 20f * frames);
        moveYWithCollision(gravity * frames);

        // A pit fall - ported from Mario.java's own `player.getY() > 500`
        // check, same idea as FireBall's own fall-out margin. Unconditional
        // (bypasses isInvincible(), unlike shrink()) since a star or hit-
        // invincibility never saved you from a pit in the original either.
        if (getY() > world.getHeightPx() + FALL_OUT_MARGIN_PX) {
            die();
        }

        applyFire(command);
        updateAnimation(command, frames);
    }

    /** Ported from {@code Player.Fire()} - Fire Mario only, capped at 2 concurrent fireballs. */
    private void applyFire(PlayerCommand command) {
        if (!command.firePressed || powerState != PlayerPowerState.FIRE) {
            return;
        }
        long activeFireBalls = 0;
        for (FireBall fireBall : world.getFireBalls()) {
            if (fireBall.isActive()) {
                activeFireBalls++;
            }
        }
        if (activeFireBalls >= 2) {
            return;
        }
        FireBall fireBall = new FireBall(getX(), getY(), facingRight);
        world.addFireBall(fireBall);
        MarioContext.spawn(fireBall);
        MarioResourceManager.sound("smb_fireball").play();
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
        MarioResourceManager.sound(powerState == PlayerPowerState.SMALL ? "smb_jump-small" : "smb_jump-super").play();
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

        if (dx > 0 && world.containsImpassableArea(newX, getY(), width, height)) {
            newX = (float) (((int) (newX + width) / tileSize) * tileSize - width);
            speed = 0;
        } else if (dx < 0 && world.containsImpassableArea(newX, getY(), width, height)) {
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
            if (world.containsImpassableArea(getX(), newY, width, height)) {
                newY = (float) (((int) (newY + height) / tileSize) * tileSize - height);
                gravity = 0;
                onGround = true;
            } else {
                onGround = false;
            }
        } else if (dy < 0 && world.containsImpassableArea(getX(), newY, width, height)) {
            InteractiveBrick hit = world.findActiveBrickAt(getX(), newY, width, height);
            if (hit != null) {
                hit.hitFromBelow(this);
            }
            if (hit == null || hit.isActive()) {
                newY = (float) (((int) newY / tileSize + 1) * tileSize);
                gravity = 0;
            }
        }
        setY(newY);
    }

    /**
     * Frame layout in every player region (4 cols x 7 rows, matching the
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

    /**
     * Small -> Big -> Fire, ported from {@code Player.Grow()}. Already-Fire
     * is a no-op (matches the original). The original's multi-frame morph
     * animation (which also briefly pauses enemies) is skipped - a cosmetic
     * simplification, not a mechanic.
     */
    public void grow() {
        if (powerState == PlayerPowerState.SMALL) {
            changePowerState(PlayerPowerState.BIG);
        } else if (powerState == PlayerPowerState.BIG) {
            changePowerState(PlayerPowerState.FIRE);
        }
        MarioResourceManager.sound("smb_powerup").play();
    }

    /**
     * Fire -> Big -> Small -> (death), ported from {@code Player.Decerease()}.
     * No-ops while invincible, matching the original.
     */
    public void shrink() {
        if (isInvincible()) {
            return;
        }
        if (powerState == PlayerPowerState.FIRE) {
            changePowerState(PlayerPowerState.BIG);
            invincibleTimer = INVINCIBLE_SECONDS;
            MarioResourceManager.sound("smb_pipe").play();
        } else if (powerState == PlayerPowerState.BIG) {
            changePowerState(PlayerPowerState.SMALL);
            invincibleTimer = INVINCIBLE_SECONDS;
            MarioResourceManager.sound("smb_pipe").play();
        } else {
            die();
        }
    }

    /**
     * Small Mario's death - either a hit while already Small ({@link #shrink()}),
     * or falling into a pit (see {@link #act}'s fall-out check, which unlike
     * {@code shrink()} can fire at any power state, so this resets back to
     * Small itself rather than assuming it already is). Respawns immediately
     * at the level's spawn point, matching the original's instant
     * {@code Restart()} - no death animation/delay.
     *
     * <p>Player has no notion of lives or game over; {@link #consumeDeath()}
     * is how {@code MarioGameScreen} finds out a life should be charged -
     * see {@code GameStateController}.
     */
    private void die() {
        MarioResourceManager.sound("smb_mariodie").play();
        changePowerState(PlayerPowerState.SMALL);
        speed = 0;
        gravity = 0;
        setPosition(spawnX, spawnY);
        invincibleTimer = INVINCIBLE_SECONDS;
        justDied = true;
    }

    /**
     * @return true the first time this is called after a death, false
     * otherwise - a consume-once flag so {@code MarioGameScreen} charges
     * exactly one life per death regardless of how many frames pass before
     * it's polled.
     */
    public boolean consumeDeath() {
        if (justDied) {
            justDied = false;
            return true;
        }
        return false;
    }

    private void changePowerState(PlayerPowerState newState) {
        float oldHeight = getHeight();
        powerState = newState;
        initFrames(newState);
        setSize(newState.width, newState.height);
        // Keep Mario's feet planted: growing/shrinking extends/retracts upward.
        setY(getY() - (newState.height - oldHeight));
    }

    /**
     * A small upward hop after stomping an enemy, ported from the
     * {@code game.player.Jump(-8)} call every stomp reaction makes in the
     * original (EnemyMashroom/EnemyTurtle/TurtleShell alike).
     */
    public void bounceOffEnemy() {
        gravity = -8f;
        onGround = false;
    }

    /** Ported from {@code Player.STAR()} - temporary invincibility (no speed/visual flourish yet). */
    public void collectStar() {
        starTimer = STAR_SECONDS;
    }

    public boolean hasStar() {
        return starTimer > 0;
    }

    public boolean isInvincible() {
        return invincibleTimer > 0 || hasStar();
    }

    public PlayerPowerState getPowerState() {
        return powerState;
    }

    public boolean isOnGround() {
        return onGround;
    }

    /** Moving downward (or momentarily weightless at a jump's apex) - used by {@code LiftCollisionResolver}. */
    public boolean isFalling() {
        return gravity >= 0;
    }

    /**
     * Snaps onto a lift's top surface, ported from {@code Player_Lift.collided}'s
     * {@code isFalling()} branch. Unlike tile ground contact (re-derived from
     * {@code MarioWorld}'s grid every frame in {@link #moveYWithCollision}), a
     * lift moves, so {@code LiftCollisionResolver} re-supplies its current top
     * and this frame's horizontal drift every frame instead.
     *
     * @param topY the lift's current top edge, in world pixels
     * @param dx    the lift's horizontal movement this frame, to carry a rider along
     */
    public void landOnLift(float topY, float dx) {
        setY(topY - getHeight());
        setX(getX() + dx);
        gravity = 0;
        onGround = true;
    }

    /**
     * Overrides {@code input.poll()} for one or more frames - used to drive
     * Mario through a scripted sequence (walking into a level-complete flag,
     * standing still while entering a pipe) without the player's own input.
     * Pass {@code null} to release control back to {@code input}.
     */
    public void setForcedCommand(PlayerCommand command) {
        forcedCommand = command;
    }

    public void clearForcedCommand() {
        forcedCommand = null;
    }

    /** Extends (never shortens) the invincibility window - used to shield Mario during a level-complete sequence. */
    public void setInvincibleFor(float seconds) {
        invincibleTimer = Math.max(invincibleTimer, seconds);
    }

    /** This frame's input intent, for checkpoints that gate on a held direction (e.g. a pipe entrance). */
    public boolean wantsRight() {
        return lastCommand != null && lastCommand.right;
    }

    public boolean wantsDown() {
        return lastCommand != null && lastCommand.down;
    }
}

package au.com.guidebee.morsetoolkit.activity.mario.actors.player;

import com.guidebee.game.graphics.Batch;
import com.guidebee.game.graphics.TextureRegion;
import com.guidebee.game.microedition.Layer;

import au.com.guidebee.morsetoolkit.activity.mario.MarioConfiguration;
import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.actors.bricks.Bouncer;
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
    /** Ported from {@code Player.GoToRight/Left}'s {@code turbo} branch - same acceleration, higher cap. */
    private static final float MAX_SPEED_TURBO = 100f;

    private static final float GRAVITY_STEP = 0.42f;
    private static final float GRAVITY_CAP = 10f;
    private static final float JUMP_BASE = -11f;
    private static final float JUMP_SPEED_BONUS_DIVISOR = 60f;
    /** Ported from {@code Player_Brick.collided}'s own {@code p.Jump(-22)} for a Bouncer - see {@code Bouncer}'s class doc. */
    private static final float BOUNCER_LAUNCH_GRAVITY = -22f;

    private static final float WALK_CYCLE_THRESHOLD = 160f;

    /** ~400 original 60fps ticks - matches Player.java's post-shrink `invincible = 400`. */
    private static final float INVINCIBLE_SECONDS = 400f / 60f;
    /** ~1000 original 60fps ticks - matches Player.java's `STAR()`'s `delay = 1000`. */
    private static final float STAR_SECONDS = 1000f / 60f;
    /** How far past the level's bottom edge counts as "fallen into a pit" - matches {@code FireBall}'s own fall-out margin. */
    private static final float FALL_OUT_MARGIN_PX = 200f;

    /** Every growth/shrink strip's per-frame delay - matches e.g. {@code SmallToBigMarioAnim}'s {@code setAnimationTimer(new Timer(120))}. */
    private static final float TRANSITION_FRAME_SECONDS = 120f / 1000f;
    /** All 4 growth/shrink strips share this cell size (the "Big"/"Fire" box - even the small-mario-posed early frames of a growth strip use it), confirmed against the source PNGs' pixel dimensions. */
    private static final int TRANSITION_FRAME_WIDTH = 32;
    private static final int TRANSITION_FRAME_HEIGHT = 64;

    /**
     * ~100 original 60fps ticks of standing still before falling starts, then
     * launching upward before gravity pulls it back down - ported from
     * {@code Animations/FallingDeadMario.java}'s {@code delay}/{@code Gravity}
     * fields (this is the enemy-hit-while-Small death; a pit-fall stays the
     * original's instant, silent {@code Restart()} - see {@link #act}).
     */
    private static final float DEATH_INITIAL_DELAY_SECONDS = 100f / 60f;
    private static final float DEATH_GRAVITY_START = -10f;
    private static final float DEATH_GRAVITY_STEP = 0.5f;
    private static final float DEATH_GRAVITY_CAP = 10f;

    /**
     * Ported from {@code Player.update}'s {@code Timer save = new Timer(1000)}
     * checkpoint-save logic: while grounded, every second, if Mario has moved
     * more than this far from the last saved point, it becomes the new
     * respawn point - so a mid-level death doesn't always send him all the
     * way back to the level's start.
     */
    private static final float CHECKPOINT_SAVE_INTERVAL_SECONDS = 1f;
    private static final float CHECKPOINT_MIN_DISTANCE_PX = 1000f;

    private final MarioWorld world;
    private final MarioInputController input;

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

    /**
     * True whenever the down input is held, ported from {@code Player.KeyPressedDown}.
     * Locks out new horizontal acceleration regardless of power state (see
     * {@link #applyHorizontalInput}) - existing momentum still decays via
     * friction, matching the original's {@code breaks} logic living outside
     * the {@code GoToLeft/Right} methods this gates.
     */
    private boolean keyPressedDown;
    /**
     * {@link #keyPressedDown}, but only for Big/Fire Mario - ported from the
     * original's collision pairs ({@code Player_Brick}/{@code Player_EnemyGroup}/
     * {@code Hammer_Player}), which all additionally check {@code getID() != 1}
     * (small Mario is already short enough that there's nothing to duck under).
     * While true: shows the crouch pose (see {@link #updateAnimation}), and
     * lets Mario duck under an overhead {@code InteractiveBrick} (see
     * {@link #moveXWithCollision}/{@link #moveYWithCollision}), enemy, or
     * hazard (see {@code EnemyCollisionResolver}/{@code HazardCollisionResolver})
     * whose bottom edge sits above his own {@link #DUCK_HEAD_ROOM_PX}/
     * {@link #DUCK_OVERHEAD_CLEARANCE_PX} line - the original's crouch is a
     * selective *collision* shrink, not an actual hitbox resize (confirmed:
     * nothing in the original ever changes Mario's width/height while
     * crouching, only which collisions register). This gate is
     * collision-only - the *visual* crouch pose is Small Mario's too, see
     * {@link #updateAnimation}.
     */
    private boolean ducking;
    /** Ported from {@code Player_Brick}'s own {@code p.getY() + 32} threshold. */
    private static final float DUCK_HEAD_ROOM_PX = 32f;
    /** Ported from {@code Player_EnemyGroup}/{@code Hammer_Player}'s own {@code p.getY() + 48} threshold. */
    public static final float DUCK_OVERHEAD_CLEARANCE_PX = 48f;

    /** Ported from {@code Player}'s {@code invincible} field - post-hit/post-death, and the only one of the three that blinks (see {@link #paint}). */
    private float invincibleTimer;
    private float starTimer;
    /**
     * A shield with no original-engine equivalent - {@code MarioGameScreen}'s
     * level-complete/pipe-entry sequences use it (see
     * {@link #setInvincibleFor}) to keep a stray touch from interrupting a
     * scripted transition, which the original instead achieves by actually
     * deactivating enemies. Kept separate from {@link #invincibleTimer} so it
     * doesn't trigger that field's blink rendering during a transition -
     * only {@link #isInvincible} treats them the same.
     */
    private float shieldTimer;
    /** Toggled once per frame while {@code invincibleTimer > 0} - ported from {@code Player.render}'s {@code blink} field. */
    private boolean blinkVisible = true;

    /** Set while a level-complete/pipe-entry sequence drives Mario instead of the player - see {@code MarioGameScreen}. */
    private PlayerCommand forcedCommand;
    private PlayerCommand lastCommand;

    /** Set by {@link #die()}/{@link #beginDeathAnimation()}, cleared by {@link #consumeDeath()} - see that method's doc. */
    private boolean justDied;

    /** Non-null while a {@link #grow()}/{@link #shrink()} morph flipbook is playing - see {@link #beginTransition}. */
    private TextureRegion[] transitionFrames;
    private int transitionFrameIndex;
    private float transitionFrameTimer;
    private PlayerPowerState transitionTarget;

    /** True while the enemy-hit-while-Small death (launch up, then fall) is playing - see {@link #beginDeathAnimation}. */
    private boolean dyingAnimated;
    private float deathDelayTimer;
    private float deathGravity;

    /** The current respawn point - starts at the level's spawn tile, advances via {@link #updateCheckpoint} while grounded. */
    private float checkpointX;
    private float checkpointY;
    private float checkpointSaveTimer;

    /** Lazily-split palette-swap frame grids for {@link #hasStar}'s color-cycle render - see {@link #paint}. */
    private TextureRegion[][] smallBlackFrames;
    private TextureRegion[][] smallGreenFrames;
    private TextureRegion[][] smallRedFrames;
    private TextureRegion[][] bigBlackFrames;
    private TextureRegion[][] bigGreenFrames;
    private TextureRegion[][] bigRedFrames;
    /** 1..4, cycling Black/normal/Green/Red - ported from {@code Player.render}'s {@code currentFrame}. */
    private int starColorIndex = 2;
    private float starColorTimer;

    public Player(float x, float y, MarioWorld world, MarioInputController input) {
        super(x, y, PlayerPowerState.SMALL.width, PlayerPowerState.SMALL.height, true);
        this.world = world;
        this.input = input;
        this.checkpointX = x;
        this.checkpointY = y;
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
        if (dyingAnimated) {
            g.draw(MarioResourceManager.region("small_dead_mario"), getX(), getY(),
                    PlayerPowerState.SMALL.width, PlayerPowerState.SMALL.height);
            return;
        }
        if (transitionFrames != null) {
            g.draw(transitionFrames[transitionFrameIndex], getX(), getY(),
                    TRANSITION_FRAME_WIDTH, TRANSITION_FRAME_HEIGHT);
            return;
        }
        // Ported from Player.render(): a Star's color-cycle draw always wins
        // over the plain post-hit invincibility blink below (the original
        // draws both, in this order, onto the same spot every frame - the
        // opaque Star sprite just happens to cover the base one).
        if (hasStar()) {
            g.draw(starColorRegion(), getX(), getY(), getWidth(), getHeight());
            return;
        }
        // Ported from Player.render()'s `if (invincible > 0) { if (blink) ... }` -
        // a plain on/off flicker, not a color-cycle.
        if (invincibleTimer > 0 && !blinkVisible) {
            return;
        }
        TextureRegion region = frameRegions[frame / frameCols][frame % frameCols];
        g.draw(region, getX(), getY(), getWidth(), getHeight());
    }

    /** The current star-color-cycle frame, from whichever palette-swap grid matches {@link #powerState}. */
    private TextureRegion starColorRegion() {
        TextureRegion[][] frames;
        switch (starColorIndex) {
            case 1:
                frames = powerState == PlayerPowerState.SMALL ? smallBlackFrames() : bigBlackFrames();
                break;
            case 3:
                frames = powerState == PlayerPowerState.SMALL ? smallGreenFrames() : bigGreenFrames();
                break;
            case 4:
                frames = powerState == PlayerPowerState.SMALL ? smallRedFrames() : bigRedFrames();
                break;
            default:
                return frameRegions[frame / frameCols][frame % frameCols];
        }
        return frames[frame / frameCols][frame % frameCols];
    }

    private TextureRegion[][] smallBlackFrames() {
        if (smallBlackFrames == null) {
            smallBlackFrames = MarioResourceManager.region("small_black_mario")
                    .split(PlayerPowerState.SMALL.width, PlayerPowerState.SMALL.height);
        }
        return smallBlackFrames;
    }

    private TextureRegion[][] smallGreenFrames() {
        if (smallGreenFrames == null) {
            smallGreenFrames = MarioResourceManager.region("small_green_mario")
                    .split(PlayerPowerState.SMALL.width, PlayerPowerState.SMALL.height);
        }
        return smallGreenFrames;
    }

    private TextureRegion[][] smallRedFrames() {
        if (smallRedFrames == null) {
            smallRedFrames = MarioResourceManager.region("small_red_mario")
                    .split(PlayerPowerState.SMALL.width, PlayerPowerState.SMALL.height);
        }
        return smallRedFrames;
    }

    /** Shared by Big and Fire (the original's own equivalent branch inexplicably always used Big-sized art here too, and no Fire-specific black/green/red art exists to port instead - see docs/MARIO_PORT_PLAN.md's asset survey). */
    private TextureRegion[][] bigBlackFrames() {
        if (bigBlackFrames == null) {
            bigBlackFrames = MarioResourceManager.region("big_black_mario")
                    .split(PlayerPowerState.BIG.width, PlayerPowerState.BIG.height);
        }
        return bigBlackFrames;
    }

    private TextureRegion[][] bigGreenFrames() {
        if (bigGreenFrames == null) {
            bigGreenFrames = MarioResourceManager.region("big_green_mario")
                    .split(PlayerPowerState.BIG.width, PlayerPowerState.BIG.height);
        }
        return bigGreenFrames;
    }

    private TextureRegion[][] bigRedFrames() {
        if (bigRedFrames == null) {
            bigRedFrames = MarioResourceManager.region("big_red_mario")
                    .split(PlayerPowerState.BIG.width, PlayerPowerState.BIG.height);
        }
        return bigRedFrames;
    }

    private void setFrame(int frame) {
        this.frame = frame;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        float frames = delta * PHYSICS_FPS;

        if (invincibleTimer > 0) {
            invincibleTimer -= delta;
            blinkVisible = !blinkVisible;
        } else {
            blinkVisible = true;
        }
        if (shieldTimer > 0) {
            shieldTimer -= delta;
        }
        if (starTimer > 0) {
            starTimer -= delta;
            updateStarColorCycle(delta);
        }

        if (dyingAnimated) {
            updateDeathAnimation(delta, frames);
            return;
        }
        if (transitionFrames != null) {
            updateTransition(delta);
            return;
        }

        PlayerCommand command = forcedCommand != null ? forcedCommand : input.poll();
        lastCommand = command;
        keyPressedDown = command.down;
        ducking = keyPressedDown && powerState != PlayerPowerState.SMALL;

        applyHorizontalInput(command, frames);
        applyJump(command);
        applyGravity(frames);

        moveXWithCollision(speed / 20f * frames);
        moveYWithCollision(gravity * frames);
        updateCheckpoint(delta);

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

    /**
     * Ported from {@code Player.update}'s {@code Timer save}-gated block:
     * while grounded, every second, promote the current position to the
     * respawn point if it's moved far enough from the last one - see
     * {@link #CHECKPOINT_SAVE_INTERVAL_SECONDS}'s doc.
     */
    private void updateCheckpoint(float delta) {
        if (!onGround) {
            return;
        }
        checkpointSaveTimer += delta;
        if (checkpointSaveTimer < CHECKPOINT_SAVE_INTERVAL_SECONDS) {
            return;
        }
        checkpointSaveTimer = 0;
        float dx = getX() - checkpointX;
        float dy = getY() - checkpointY;
        if (Math.sqrt(dx * dx + dy * dy) > CHECKPOINT_MIN_DISTANCE_PX) {
            checkpointX = getX();
            checkpointY = getY();
        }
    }

    /**
     * Ported from {@code Player.render}'s {@code Star} color-cycle block:
     * cycles {@link #starColorIndex} through Black/normal/Green/Red, at a
     * pace that quickens then eases as {@link #starTimer} counts down (the
     * original re-derives the same thresholds off its own countdown field,
     * {@code delay}; translated here into wall-clock seconds since one
     * original tick was implicitly 1/60s).
     */
    private void updateStarColorCycle(float delta) {
        float interval;
        if (starTimer > 510f / 60f) {
            interval = 0f;
        } else if (starTimer > 200f / 60f) {
            interval = 3f / 60f;
        } else if (starTimer > 100f / 60f) {
            interval = 6f / 60f;
        } else {
            interval = 11f / 60f;
        }
        starColorTimer += delta;
        if (starColorTimer >= interval) {
            starColorTimer = 0;
            starColorIndex = starColorIndex % 4 + 1;
        }
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
        float maxSpeed = command.runHeld ? MAX_SPEED_TURBO : MAX_SPEED;
        // Ported from GoToLeft/GoToRight's own `if (ControlleByKeyboard & !KeyPressedDown)`
        // guard - holding down locks out new acceleration (existing speed
        // still decays via the friction branch below) regardless of power
        // state, see keyPressedDown's doc.
        if (keyPressedDown) {
            if (speed > 0) {
                speed = Math.max(0, speed - FRICTION * frames);
            } else if (speed < 0) {
                speed = Math.min(0, speed + FRICTION * frames);
            }
            return;
        }
        if (command.left) {
            facingRight = false;
            speed = Math.max(speed - ACCEL * frames, -maxSpeed);
        } else if (command.right) {
            facingRight = true;
            speed = Math.min(speed + ACCEL * frames, maxSpeed);
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
        float duckAboveY = ducking ? getY() + DUCK_HEAD_ROOM_PX : Float.NEGATIVE_INFINITY;

        if (dx > 0 && world.containsImpassableArea(newX, getY(), width, height, duckAboveY)) {
            newX = (float) (((int) (newX + width) / tileSize) * tileSize - width);
            speed = 0;
        } else if (dx < 0 && world.containsImpassableArea(newX, getY(), width, height, duckAboveY)) {
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
        float duckAboveY = ducking ? getY() + DUCK_HEAD_ROOM_PX : Float.NEGATIVE_INFINITY;

        if (dy > 0) {
            if (world.containsImpassableArea(getX(), newY, width, height, duckAboveY)) {
                newY = (float) (((int) (newY + height) / tileSize) * tileSize - height);
                InteractiveBrick landedOn = world.findActiveBrickAt(getX(), newY, width, height);
                if (landedOn instanceof Bouncer) {
                    // Ported from Player_Brick.collided's own `if (b.getID() == 13)
                    // p.Jump(-22)` - a Bouncer never lets Mario actually stand on
                    // it, always relaunching him instead (roughly double a normal
                    // jump's impulse) - see Bouncer's own class doc.
                    gravity = BOUNCER_LAUNCH_GRAVITY;
                    onGround = false;
                } else {
                    gravity = 0;
                    onGround = true;
                }
            } else {
                onGround = false;
            }
        } else if (dy < 0 && world.containsImpassableArea(getX(), newY, width, height, duckAboveY)) {
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
        // Ported from Player.update()'s own KeyPressedDown block - takes
        // priority over every other pose, matching the original (its
        // equivalent check runs last each tick, so it always wins), and is
        // NOT gated on power state there (unlike this class's own `ducking`,
        // which only affects collision - see that field's doc): Small
        // Mario's "player" strip has the same 4 cols x 7 rows layout as
        // Big/Fire (confirmed against WholeGame.java's own
        // `getImages("player.png", 4, 7)` load call), so frames 24/25 are a
        // real crouch pose there too, just with no collision effect.
        if (keyPressedDown) {
            setFrame(facingRight ? 24 : 25);
            return;
        }
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
     * Small -> Big -> Fire, ported from {@code Player.Grow()}: plays the
     * matching morph flipbook (see {@link #beginTransition}) before the power
     * state actually changes. Already-Fire is a no-op (matches the original,
     * including that it still plays the powerup jingle).
     */
    public void grow() {
        if (powerState == PlayerPowerState.SMALL) {
            beginTransition(hasStar() ? "small_to_big_star_mario" : "small_to_big_mario",
                    PlayerPowerState.BIG, true);
        } else if (powerState == PlayerPowerState.BIG) {
            beginTransition("big_to_fire_mario", PlayerPowerState.FIRE, false);
        }
        MarioResourceManager.sound("smb_powerup").play();
    }

    /**
     * Fire -> Small, Big -> Small, or Small -> (death), ported from
     * {@code Player.Decerease()}. No-ops while invincible, matching the
     * original. Note Fire drops straight to Small, not to Big first - the
     * original's own {@code ID==3} branch calls {@code setID(1)} (Small)
     * despite a stale "big mario" comment beside it.
     */
    public void shrink() {
        if (isInvincible()) {
            return;
        }
        if (powerState == PlayerPowerState.FIRE) {
            beginTransition("fire_to_small_mario", PlayerPowerState.SMALL, false);
            invincibleTimer = INVINCIBLE_SECONDS;
            MarioResourceManager.sound("smb_pipe").play();
        } else if (powerState == PlayerPowerState.BIG) {
            beginTransition("big_to_small_mario", PlayerPowerState.SMALL, false);
            invincibleTimer = INVINCIBLE_SECONDS;
            MarioResourceManager.sound("smb_pipe").play();
        } else {
            beginDeathAnimation();
        }
    }

    /**
     * Starts a {@link #grow()}/{@link #shrink()} morph flipbook - ported from
     * {@code Player.Grow()}/{@code Decerease()}'s {@code AnimationGroup.add(new
     * ...MarioAnim(...))} calls. Unlike the original (a separate overlay
     * sprite drawn on top of the frozen real player, with the whole world
     * paused via {@code pauseEnemys()} until it finishes), this drives
     * {@link #paint}'s render directly and only freezes the player itself
     * (via {@link #act}'s early return while {@link #transitionFrames} is
     * set) - simpler, and enemies are rendered harmless anyway since
     * {@link #shrink()} already grants {@link #INVINCIBLE_SECONDS} up front
     * for the shrink case (the grow case was never in danger either way,
     * since nothing in this port damages Mario while growing).
     *
     * @param preShiftUp32 true only for Small -> Big: the strip's cells are
     *                     all Big-sized (32x64, confirmed against the source
     *                     PNGs) even for its small-mario-posed early frames,
     *                     so the box needs the extra headroom immediately,
     *                     not just once {@link #changePowerState} applies at
     *                     the end - matches the original's own immediate
     *                     {@code this.setY(this.getY() - 32)} in this one case.
     */
    private void beginTransition(String regionName, PlayerPowerState target, boolean preShiftUp32) {
        TextureRegion[] rowFrames = MarioResourceManager.region(regionName)
                .split(TRANSITION_FRAME_WIDTH, TRANSITION_FRAME_HEIGHT)[0];
        transitionFrames = new TextureRegion[rowFrames.length];
        for (int i = 0; i < rowFrames.length; i++) {
            TextureRegion copy = new TextureRegion(rowFrames[i]);
            if (!facingRight) {
                copy.flip(true, false);
            }
            transitionFrames[i] = copy;
        }
        transitionFrameIndex = 0;
        transitionFrameTimer = 0;
        transitionTarget = target;
        if (preShiftUp32) {
            setY(getY() - (PlayerPowerState.BIG.height - PlayerPowerState.SMALL.height));
        }
        speed = 0;
        gravity = 0;
    }

    private void updateTransition(float delta) {
        transitionFrameTimer += delta;
        if (transitionFrameTimer < TRANSITION_FRAME_SECONDS) {
            return;
        }
        transitionFrameTimer = 0;
        transitionFrameIndex++;
        if (transitionFrameIndex >= transitionFrames.length) {
            PlayerPowerState target = transitionTarget;
            transitionFrames = null;
            transitionTarget = null;
            changePowerState(target);
        }
    }

    /**
     * The enemy-hit-while-Small death: launches Mario up then lets gravity
     * pull him back down off-screen before respawning - ported from
     * {@code Player.Decerease()}'s {@code ID==1} branch and
     * {@code Animations/FallingDeadMario.java}. A pit-fall (see {@link #act}'s
     * fall-out check) stays the original's instant, silent {@code Restart()}
     * instead - that distinction (animated "you died" beat vs. silent
     * respawn) is the original's own, not new here.
     */
    private void beginDeathAnimation() {
        MarioResourceManager.sound("smb_mariodie").play();
        dyingAnimated = true;
        deathDelayTimer = DEATH_INITIAL_DELAY_SECONDS;
        deathGravity = DEATH_GRAVITY_START;
        speed = 0;
        justDied = true;
    }

    private void updateDeathAnimation(float delta, float frames) {
        if (deathDelayTimer > 0) {
            deathDelayTimer -= delta;
            return;
        }
        if (deathGravity < DEATH_GRAVITY_CAP) {
            deathGravity = Math.min(DEATH_GRAVITY_CAP, deathGravity + DEATH_GRAVITY_STEP * frames);
        }
        setY(getY() + deathGravity * frames);
        // Ported from FallingDeadMario's own `this.getY() > 700` - re-derived
        // against this level's own height (like the pit-fall check above)
        // rather than the original's fixed constant, which only worked
        // because every original level happened to be shorter than that.
        if (getY() > world.getHeightPx() + FALL_OUT_MARGIN_PX) {
            dyingAnimated = false;
            speed = 0;
            gravity = 0;
            setPosition(checkpointX, checkpointY);
            invincibleTimer = INVINCIBLE_SECONDS;
        }
    }

    /**
     * A pit-fall - ported from {@code Mario.java}'s own
     * {@code player.getY() > 500} check in its main loop, same idea as
     * {@code FireBall}'s own fall-out margin. Unconditional (bypasses
     * {@link #isInvincible()}, unlike {@link #shrink()}) since a star or
     * hit-invincibility never saved you from a pit in the original either.
     * Respawns immediately at the last checkpoint (see {@link #updateCheckpoint}),
     * matching the original's instant {@code Restart()} - no death
     * animation/delay, unlike {@link #beginDeathAnimation()}.
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
        setPosition(checkpointX, checkpointY);
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
        return invincibleTimer > 0 || hasStar() || shieldTimer > 0;
    }

    public PlayerPowerState getPowerState() {
        return powerState;
    }

    public boolean isOnGround() {
        return onGround;
    }

    /** True for Big/Fire Mario while holding down - see {@link #ducking}'s doc. */
    public boolean isDucking() {
        return ducking;
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

    /** Extends (never shortens) the shield window - used to protect Mario during a level-complete sequence. See {@link #shieldTimer}. */
    public void setInvincibleFor(float seconds) {
        shieldTimer = Math.max(shieldTimer, seconds);
    }

    /** This frame's input intent, for checkpoints that gate on a held direction (e.g. a pipe entrance). */
    public boolean wantsRight() {
        return lastCommand != null && lastCommand.right;
    }

    public boolean wantsDown() {
        return lastCommand != null && lastCommand.down;
    }

    /** For a {@code ClowdGoUP_CheckPoint}'s entry gate - see {@code CheckpointResolver}. */
    public boolean wantsUp() {
        return lastCommand != null && lastCommand.up;
    }
}

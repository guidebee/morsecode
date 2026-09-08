package au.com.guidebee.morsetoolkit.activity.mario.actors.player;

import com.guidebee.game.graphics.Batch;
import com.guidebee.game.graphics.TextureRegion;

import au.com.guidebee.morsetoolkit.activity.mario.MarioConfiguration;
import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.actors.bricks.Bouncer;
import au.com.guidebee.morsetoolkit.activity.mario.actors.bricks.InteractiveBrick;
import au.com.guidebee.morsetoolkit.activity.mario.actors.projectiles.FireBall;
import au.com.guidebee.morsetoolkit.activity.mario.fx.Bubble;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioContext;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioWorld;
import au.com.guidebee.morsetoolkit.platformer.actor.PowerStateActor;
import au.com.guidebee.morsetoolkit.platformer.input.PlatformerCommand;
import au.com.guidebee.morsetoolkit.platformer.input.TouchOrKeyboardInput;

import java.util.Random;

/**
 * Mario's physics/state machine, ported from the original engine's
 * {@code Objects/Player.java} as *design* rather than transcribed line for
 * line (per docs/MARIO_PORT_PLAN.md SS1 - every API call changes even though
 * the algorithm carries over).
 *
 * <p>Extends {@link PowerStateActor} (see PLATFORMER_ENGINE_ARCHITECTURE.md
 * §3.2), which owns the invincibility/star/shield timers, the growth/shrink
 * morph-transition flipbook, and checkpoint/respawn tracking - everything
 * below is the Mario-specific half: movement physics, animation, rendering,
 * and this game's own extra state machines ({@link #dyingAnimated}'s death
 * sequence, {@link #forcedCommand}'s scripted-input override) that aren't
 * generic enough to live in the shared base.
 *
 * <h2>Why this draws via Batch directly instead of composing a Sprite</h2>
 * Growing/shrinking swaps Mario's entire image strip and frame size (32x32
 * "player" vs 32x64 "big_player"/"fire_player"). The original's GTGE Sprite
 * supported that via {@code setImages(BufferedImage[])} at runtime; this
 * engine's {@code microedition.Sprite} binds its region/frame-grid at
 * construction with no equivalent setter, so {@code Player} extends
 * {@code PowerStateActor} (itself a {@code Layer}, same base {@code Sprite}/
 * {@code TiledLayer} extend) rather than one fixed-size {@code Sprite}.
 * Rendering uses {@code TextureRegion.split(...)} (the same slicing
 * {@code Sprite} does internally) plus a direct {@code Batch.draw(...)} call
 * in {@link #paintPowerState} - not a second, separately-constructed
 * {@code Sprite} instance that's never added to the {@code LayerManager}
 * itself, since a Sprite's rendering path is wired through being a stage
 * member, and one that isn't one is untested territory this class doesn't
 * need to depend on.
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
public class Player extends PowerStateActor<PlayerPowerState> {

    private static final float PHYSICS_FPS = 60f;
    private static final Random RANDOM = new Random();

    private static final float ACCEL = 2f;
    private static final float FRICTION = 1f;
    private static final float MAX_SPEED = 60f;
    /** Ported from {@code Player.GoToRight/Left}'s {@code turbo} branch - same acceleration, higher cap. */
    private static final float MAX_SPEED_TURBO = 100f;
    /**
     * Ported from {@code Player.AutomaticGoRight()}'s own literal cap/step -
     * the scripted forward-walk {@code MarioGameScreen} drives Mario through
     * via {@link #forcedCommand} (the axe/bridge finale, the flagpole's own
     * walk-to-checkpoint) is deliberately slower/gentler than any
     * player-held movement, not just a reuse of {@link #MAX_SPEED}/{@link
     * #ACCEL} - confirmed by reading the source rather than assumed (an
     * earlier version of this class made exactly that assumption).
     */
    private static final float AUTO_WALK_MAX_SPEED = 40f;
    private static final float AUTO_WALK_ACCEL = 1f;

    private static final float GRAVITY_STEP = 0.42f;
    private static final float GRAVITY_CAP = 10f;
    private static final float JUMP_BASE = -11f;
    private static final float JUMP_SPEED_BONUS_DIVISOR = 60f;
    /** Ported from {@code Player_Brick.collided}'s own {@code p.Jump(-22)} for a Bouncer - see {@code Bouncer}'s class doc. */
    private static final float BOUNCER_LAUNCH_GRAVITY = -22f;

    /**
     * Water/swim constants, ported from {@code Player.update}/{@code Jump}/
     * {@code Gravity}'s own {@code if (Water)} branches - see {@link #water}'s
     * doc and {@link #applyWaterSurfaceConstraints}.
     */
    private static final float WATER_GRAVITY_STEP = 0.1f;
    private static final float WATER_GRAVITY_CAP = 2f;
    /** A tap-to-paddle impulse, not a held jump - ported from {@code Jump()}'s own {@code Gravity = -3.5} (fires every {@code jumpPressed} edge, no {@link #onGround} gate, unlike a ground jump). */
    private static final float WATER_JUMP_GRAVITY = -3.5f;
    /** Ported from {@code Player.update}'s own {@code if (OnGround) { speed>30 ? 30 : ... }} - walking the sea floor caps out well below free-swimming's {@link #MAX_SPEED}. */
    private static final float WATER_GROUNDED_SPEED_CAP = 30f;
    /** Ported from {@code Player.update}'s own literal {@code getY()<64 -> setY(64)} - every Sea level's water surface sits at this same absolute world Y (confirmed: original hardcodes it, not derived from level data). */
    private static final float WATER_SURFACE_Y = 64f;
    private static final float WATER_FORCE_SINK_Y = 32f;
    private static final float WATER_FORCE_SINK_GRAVITY = 2f;
    /** Ported from {@code Player.update}'s own {@code swimmDelay} (4 original ticks between each stroke-frame toggle). */
    private static final float SWIM_ANIM_INTERVAL_TICKS = 4f;
    /** Ported from {@code AddBubbles}'s own {@code Utility.getRandom(1,2)*60} tick range. */
    private static final float BUBBLE_DELAY_MIN_SECONDS = 1f;
    private static final float BUBBLE_DELAY_MAX_SECONDS = 2f;

    private static final float WALK_CYCLE_THRESHOLD = 160f;

    /** ~400 original 60fps ticks - matches Player.java's post-shrink `invincible = 400`. */
    private static final float INVINCIBLE_SECONDS = 400f / 60f;
    /** ~1000 original 60fps ticks - matches Player.java's `STAR()`'s `delay = 1000`. */
    private static final float STAR_SECONDS = 1000f / 60f;
    /** How far past the level's bottom edge counts as "fallen into a pit" - matches {@code FireBall}'s own fall-out margin. */
    private static final float FALL_OUT_MARGIN_PX = 200f;

    /** All 4 growth/shrink strips share this cell size (the "Big"/"Fire" box - even the small-mario-posed early frames of a growth strip use it), confirmed against the source PNGs' pixel dimensions. */
    private final int transitionFrameWidth;
    private final int transitionFrameHeight;

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
     * way back to the level's start. Threaded into {@link PowerStateActor
     * #updateCheckpoint} as parameters rather than baked into that shared
     * method, since these are this game's own tuning, not a toolkit default.
     */
    private static final float CHECKPOINT_SAVE_INTERVAL_SECONDS = 1f;
    private static final float CHECKPOINT_MIN_DISTANCE_PX = 1000f;

    private final MarioWorld world;
    private final TouchOrKeyboardInput input;

    private TextureRegion[][] frameRegions;
    private int frameCols;
    private int frame;

    /** Original's abstract "speed" unit - not px/sec, see the class doc. */
    private float speed;
    /** Original's "Gravity" unit - vertical speed in px per 60fps tick. */
    private float gravity;
    private boolean onGround;
    /** Set by {@link #landOnLift}, cleared by {@code LiftCollisionResolver} the moment it no longer finds a landing spot - see {@link #updateCheckpoint}'s own gate on it. */
    private boolean onLift;
    private boolean facingRight = true;
    /** Set once at level load from {@code "Sea".equals(level.attribute)} - see {@link #setWater}. */
    private boolean water;
    private float swimAnimTimer;
    /** Counts down to the next ambient {@link Bubble} spawn while {@link #water} - see {@link #updateBubbles}. */
    private float bubbleTimer;

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
     * whose bottom edge sits above his own {@link #duckHeadRoomPx}/
     * {@link #duckOverheadClearancePx} line - the original's crouch is a
     * selective *collision* shrink, not an actual hitbox resize (confirmed:
     * nothing in the original ever changes Mario's width/height while
     * crouching, only which collisions register). This gate is
     * collision-only - the *visual* crouch pose is Small Mario's too, see
     * {@link #updateAnimation}.
     */
    private boolean ducking;
    /** Ported from {@code Player_Brick}'s own {@code p.getY() + 32} threshold. */
    private final float duckHeadRoomPx;
    /** Ported from {@code Player_EnemyGroup}/{@code Hammer_Player}'s own {@code p.getY() + 48} threshold. */
    private final float duckOverheadClearancePx;

    /** Set while a level-complete/pipe-entry sequence drives Mario instead of the player - see {@code MarioGameScreen}. Kept here (not in {@link PowerStateActor}) because nothing in the shared base needs to interpret a command. */
    private PlatformerCommand forcedCommand;
    private PlatformerCommand lastCommand;

    /** Set by {@link #die()}/{@link #beginDeathAnimation()}, cleared by {@link #consumeDeath()} - see that method's doc. */
    private boolean justDied;

    /** True while the enemy-hit-while-Small death (launch up, then fall) is playing - see {@link #beginDeathAnimation}. Not part of {@link PowerStateActor}'s own transition flipbook - a genuinely different, Mario-specific state machine (see this class's own doc). */
    private boolean dyingAnimated;
    private float deathDelayTimer;
    private float deathGravity;

    /** Lazily-split palette-swap frame grids for {@link #hasStar}'s color-cycle render - see {@link #paintPowerState}. */
    private TextureRegion[][] smallBlackFrames;
    private TextureRegion[][] smallGreenFrames;
    private TextureRegion[][] smallRedFrames;
    private TextureRegion[][] bigBlackFrames;
    private TextureRegion[][] bigGreenFrames;
    private TextureRegion[][] bigRedFrames;
    /** 1..4, cycling Black/normal/Green/Red - ported from {@code Player.render}'s {@code currentFrame}. */
    private int starColorIndex = 2;
    private float starColorTimer;

    public Player(float x, float y, MarioWorld world, TouchOrKeyboardInput input) {
        super(x, y, PlayerPowerState.SMALL.width, PlayerPowerState.SMALL.height, true, PlayerPowerState.SMALL);
        this.world = world;
        this.input = input;
        int tileSize = world.tileSize();
        this.transitionFrameWidth = tileSize;
        this.transitionFrameHeight = tileSize * 2;
        this.duckHeadRoomPx = tileSize;
        this.duckOverheadClearancePx = (tileSize * 3) / 2f;
        initFrames(powerState);
    }

    public float getDuckOverheadClearancePx() {
        return duckOverheadClearancePx;
    }

    private void initFrames(PlayerPowerState state) {
        TextureRegion region = MarioResourceManager.region(state.regionName);
        int pixelWidth = state.width * MarioConfiguration.ART_SCALE;
        int pixelHeight = state.height * MarioConfiguration.ART_SCALE;
        frameRegions = region.split(pixelWidth, pixelHeight);
        frameCols = region.getRegionWidth() / pixelWidth;
        frame = 0;
    }

    @Override
    protected void paintPowerState(Batch g) {
        if (dyingAnimated) {
            g.draw(MarioResourceManager.region("small_dead_mario"), getX(), getY(),
                    PlayerPowerState.SMALL.width, PlayerPowerState.SMALL.height);
            return;
        }
        if (isTransitioning()) {
            g.draw(currentTransitionFrame(), getX(), getY(), getTransitionWidth(), getTransitionHeight());
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
                    .split(PlayerPowerState.SMALL.width * MarioConfiguration.ART_SCALE,
                            PlayerPowerState.SMALL.height * MarioConfiguration.ART_SCALE);
        }
        return smallBlackFrames;
    }

    private TextureRegion[][] smallGreenFrames() {
        if (smallGreenFrames == null) {
            smallGreenFrames = MarioResourceManager.region("small_green_mario")
                    .split(PlayerPowerState.SMALL.width * MarioConfiguration.ART_SCALE,
                            PlayerPowerState.SMALL.height * MarioConfiguration.ART_SCALE);
        }
        return smallGreenFrames;
    }

    private TextureRegion[][] smallRedFrames() {
        if (smallRedFrames == null) {
            smallRedFrames = MarioResourceManager.region("small_red_mario")
                    .split(PlayerPowerState.SMALL.width * MarioConfiguration.ART_SCALE,
                            PlayerPowerState.SMALL.height * MarioConfiguration.ART_SCALE);
        }
        return smallRedFrames;
    }

    /** Shared by Big and Fire (the original's own equivalent branch inexplicably always used Big-sized art here too, and no Fire-specific black/green/red art exists to port instead - see docs/MARIO_PORT_PLAN.md's asset survey). */
    private TextureRegion[][] bigBlackFrames() {
        if (bigBlackFrames == null) {
            bigBlackFrames = MarioResourceManager.region("big_black_mario")
                    .split(PlayerPowerState.BIG.width * MarioConfiguration.ART_SCALE,
                            PlayerPowerState.BIG.height * MarioConfiguration.ART_SCALE);
        }
        return bigBlackFrames;
    }

    private TextureRegion[][] bigGreenFrames() {
        if (bigGreenFrames == null) {
            bigGreenFrames = MarioResourceManager.region("big_green_mario")
                    .split(PlayerPowerState.BIG.width * MarioConfiguration.ART_SCALE,
                            PlayerPowerState.BIG.height * MarioConfiguration.ART_SCALE);
        }
        return bigGreenFrames;
    }

    private TextureRegion[][] bigRedFrames() {
        if (bigRedFrames == null) {
            bigRedFrames = MarioResourceManager.region("big_red_mario")
                    .split(PlayerPowerState.BIG.width * MarioConfiguration.ART_SCALE,
                            PlayerPowerState.BIG.height * MarioConfiguration.ART_SCALE);
        }
        return bigRedFrames;
    }

    private void setFrame(int frame) {
        this.frame = frame;
    }

    /**
     * Everything {@code PowerStateActor#act} doesn't already handle itself
     * (the invincibility/star/shield timer bookkeeping) - ported from this
     * class's own former {@code act(float)} override verbatim, just renamed
     * to satisfy {@link PowerStateActor#applyMovement}. The star-color-cycle
     * update below runs first, at exactly the same point in the frame the
     * old {@code act()} ran it (as part of that same timer bookkeeping,
     * before this method's predecessor even started) - see this method's own
     * git history for the exact original ordering this preserves.
     */
    @Override
    protected void applyMovement(float delta) {
        float frames = delta * PHYSICS_FPS;

        if (hasStar()) {
            updateStarColorCycle(delta);
        }

        if (dyingAnimated) {
            updateDeathAnimation(delta, frames);
            return;
        }
        if (isTransitioning()) {
            updateTransition(delta);
            return;
        }

        PlatformerCommand command = forcedCommand != null ? forcedCommand : input.poll();
        lastCommand = command;
        keyPressedDown = command.down;
        ducking = keyPressedDown && powerState != PlayerPowerState.SMALL;

        // Ported from the original's own per-frame ordering: GoToLeft/Right
        // and Jump() are called from Mario.java's outer input-polling loop,
        // *before* Player.update() (this method) runs each frame - so the
        // Water block's grounded-speed clamp and surface-breach overrides
        // apply *after* this frame's horizontal/jump input, not before (see
        // applyWaterSurfaceConstraints's own doc).
        applyHorizontalInput(command, frames);
        applyJump(command);
        applyWaterSurfaceConstraints();
        applyGravity(frames);
        updateBubbles(delta);

        moveXWithCollision(speed / 20f * frames);
        moveYWithCollision(gravity * frames);
        updateCheckpoint(delta, onGround && !onLift, CHECKPOINT_SAVE_INTERVAL_SECONDS, CHECKPOINT_MIN_DISTANCE_PX);

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
     * Ported from {@code Player.update}'s own {@code if (Water) {...}} block
     * (run after this frame's horizontal/jump input but before gravity's own
     * ramp - see {@link #applyMovement}'s own ordering comment): while resting on the
     * sea floor, swim speed is capped much lower than free-swimming (this
     * frame's acceleration already applied by {@link #applyHorizontalInput},
     * then clamped back down here); breaching too far above the surface
     * overrides even a same-frame jump/paddle back into a forced sink, and
     * he can never rise past the surface line itself. A no-op outside Sea
     * levels.
     */
    private void applyWaterSurfaceConstraints() {
        if (!water) {
            return;
        }
        if (onGround) {
            speed = Math.max(-WATER_GROUNDED_SPEED_CAP, Math.min(WATER_GROUNDED_SPEED_CAP, speed));
        }
        if (getY() < WATER_FORCE_SINK_Y) {
            gravity = WATER_FORCE_SINK_GRAVITY;
        }
        if (getY() < WATER_SURFACE_Y) {
            setY(WATER_SURFACE_Y);
        }
    }

    /**
     * Ported from {@code Player.update}'s own {@code AddBubbles()}: while
     * underwater (regardless of {@link #onGround}, matching the original),
     * spawns a {@link Bubble} at Mario's current position every 1-2 seconds.
     * Purely decorative.
     */
    private void updateBubbles(float delta) {
        if (!water) {
            return;
        }
        bubbleTimer -= delta;
        if (bubbleTimer <= 0) {
            Bubble.spawn(getX(), getY());
            bubbleTimer = BUBBLE_DELAY_MIN_SECONDS
                    + RANDOM.nextInt((int) (BUBBLE_DELAY_MAX_SECONDS - BUBBLE_DELAY_MIN_SECONDS) + 1);
        }
    }

    /**
     * Ported from {@code Player.render}'s {@code Star} color-cycle block:
     * cycles {@link #starColorIndex} through Black/normal/Green/Red, at a
     * pace that quickens then eases as {@code starTimer} counts down (the
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
    private void applyFire(PlatformerCommand command) {
        if (!command.actionPressed || powerState != PlayerPowerState.FIRE) {
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

    private void applyHorizontalInput(PlatformerCommand command, float frames) {
        // A scripted auto-walk (forcedCommand != null) uses AutomaticGoRight()'s
        // own slower cap/step instead of GoToRight/Left's - see AUTO_WALK_MAX_SPEED's doc.
        boolean autoWalk = forcedCommand != null;
        // Ported from Player.Speed()'s own `if (Water) turbo = false` - no
        // turbo while swimming, regardless of the run input.
        float maxSpeed = autoWalk ? AUTO_WALK_MAX_SPEED
                : command.runHeld && !water ? MAX_SPEED_TURBO : MAX_SPEED;
        float accel = autoWalk ? AUTO_WALK_ACCEL : ACCEL;
        // Ported from Player.update()'s own three-way friction branch:
        // `if (!Water) {...} else if (Water & !OnGround) {} else if
        // (OnGround) {...}` - free-swimming (water and airborne) applies NO
        // friction at all, so speed persists until directly countered by the
        // opposite input; every other case (dry ground, or standing on the
        // sea floor) decays toward zero same as before.
        boolean noFriction = water && !onGround;
        // Ported from GoToLeft/GoToRight's own `if (ControlleByKeyboard & !KeyPressedDown)`
        // guard - holding down locks out new acceleration (existing speed
        // still decays via the friction branch below, water permitting)
        // regardless of power state, see keyPressedDown's doc.
        if (keyPressedDown) {
            if (!noFriction) {
                decaySpeedTowardZero(frames);
            }
            return;
        }
        if (command.left) {
            facingRight = false;
            speed = speed < -maxSpeed
                    // Turbo just released mid-run: ease back down to the
                    // slower cap via ordinary friction instead of snapping
                    // straight to it - releasing Fire to reach for Jump with
                    // one finger shouldn't cost all of Mario's speed the
                    // instant it lifts.
                    ? Math.min(-maxSpeed, speed + FRICTION * frames)
                    : Math.max(speed - accel * frames, -maxSpeed);
        } else if (command.right) {
            facingRight = true;
            speed = speed > maxSpeed
                    ? Math.max(maxSpeed, speed - FRICTION * frames)
                    : Math.min(speed + accel * frames, maxSpeed);
        } else if (!noFriction) {
            decaySpeedTowardZero(frames);
        }
    }

    private void decaySpeedTowardZero(float frames) {
        if (speed > 0) {
            speed = Math.max(0, speed - FRICTION * frames);
        } else if (speed < 0) {
            speed = Math.min(0, speed + FRICTION * frames);
        }
    }

    private void applyJump(PlatformerCommand command) {
        if (!command.jumpPressed) {
            return;
        }
        // Ported from Jump()'s own `if (Water) { Gravity = -3.5; }` branch: a
        // fixed-strength paddle stroke on every press (no OnGround gate, no
        // speed-scaled bonus, no jump sound - the original plays none here).
        if (water) {
            gravity = WATER_JUMP_GRAVITY;
            return;
        }
        if (!onGround) {
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
        float cap = water ? WATER_GRAVITY_CAP : GRAVITY_CAP;
        float step = water ? WATER_GRAVITY_STEP : GRAVITY_STEP;
        if (gravity < cap) {
            gravity = Math.min(cap, gravity + step * frames);
        }
    }

    private void moveXWithCollision(float dx) {
        int width = (int) getWidth();
        int height = (int) getHeight();
        float newX = Math.max(0, getX() + dx);
        int tileSize = world.tileSize();
        float duckAboveY = ducking ? getY() + duckHeadRoomPx : Float.NEGATIVE_INFINITY;

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
        int tileSize = world.tileSize();
        float duckAboveY = ducking ? getY() + duckHeadRoomPx : Float.NEGATIVE_INFINITY;

        if (dy > 0) {
            if (world.containsImpassableArea(getX(), newY, width, height, duckAboveY)) {
                // Looked up at this pre-snap newY, not the post-snap value
                // below - once snapped, the player's feet sit exactly flush
                // with the brick's top edge, and InteractiveBrick#overlaps's
                // strict "y + height > getY()" no longer holds at that exact
                // boundary, so the Bouncer would never be found (silently
                // falling back to a normal stand instead of relaunching).
                InteractiveBrick landedOn = world.findActiveBrickAt(getX(), newY, width, height);
                newY = (float) (((int) (newY + height) / tileSize) * tileSize - height);
                if (landedOn instanceof Bouncer) {
                    // Ported from Player_Brick.collided's own `if (b.getID() == 13)
                    // p.Jump(-22)` - a Bouncer never lets Mario actually stand on
                    // it, always relaunching him instead (roughly double a normal
                    // jump's impulse) - see Bouncer's own class doc. Player.Jump(int)
                    // itself (the explicit-gravity overload both this and
                    // bounceOffEnemy() port from) always plays smb_stomp.
                    gravity = BOUNCER_LAUNCH_GRAVITY;
                    onGround = false;
                    MarioResourceManager.sound("smb_stomp").play();
                    ((Bouncer) landedOn).triggerSpring();
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
    private void updateAnimation(PlatformerCommand command, float frames) {
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
        if (water && !onGround) {
            updateSwimAnimation(frames);
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
     * Ported from {@code Player.Swim()}/{@code WaterJump()}: an alternating
     * two-frame stroke, gated to fire only every {@link #SWIM_ANIM_INTERVAL_TICKS}
     * ticks (not every frame) same as the original's own {@code swimmDelay}
     * countdown - which pose depends on whether Mario is currently sinking
     * ({@link #isFalling}, frames 16-19) or rising (frames 20-23), each split
     * further by facing direction.
     */
    private void updateSwimAnimation(float frames) {
        swimAnimTimer += frames;
        if (swimAnimTimer < SWIM_ANIM_INTERVAL_TICKS) {
            return;
        }
        swimAnimTimer = 0;
        if (isFalling()) {
            if (facingRight) {
                setFrame(frame == 16 ? 17 : 16);
            } else {
                setFrame(frame == 18 ? 19 : 18);
            }
        } else {
            if (facingRight) {
                setFrame(frame == 20 ? 21 : 20);
            } else {
                setFrame(frame == 22 ? 23 : 22);
            }
        }
    }

    /**
     * Small -> Big -> Fire, ported from {@code Player.Grow()}: plays the
     * matching morph flipbook (see {@link #startTransition}) before the power
     * state actually changes. Already-Fire is a no-op (matches the original,
     * including that it still plays the powerup jingle).
     */
    public void grow() {
        // Guards against re-entering mid-morph - see startTransition's own
        // doc for why this matters here specifically: EnemyCollisionResolver
        // runs independently of Player#act's own early-return freeze, so an
        // enemy touching Mario mid-grow could otherwise call shrink() (via
        // Enemy#onTouchedSide) and overwrite this transition with a new one
        // built against the *old*, not-yet-applied powerState - for a
        // Small->Big grow specifically, that reads powerState as still
        // SMALL and kills Mario instead of hurting him. Same guard
        // debugCyclePowerState() already uses.
        if (isTransitioning() || dyingAnimated) {
            return;
        }
        if (powerState == PlayerPowerState.SMALL) {
            startTransition(hasStar() ? "small_to_big_star_mario" : "small_to_big_mario",
                    PlayerPowerState.BIG, true);
        } else if (powerState == PlayerPowerState.BIG) {
            startTransition("big_to_fire_mario", PlayerPowerState.FIRE, false);
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
        // See grow()'s own matching guard doc - same re-entrancy risk applies
        // in this direction too (a second hit landing mid-shrink, before
        // shrink()'s own INVINCIBLE_SECONDS grant from a *previous* call even
        // takes effect the very first frame it's set).
        if (isTransitioning()) {
            return;
        }
        if (powerState == PlayerPowerState.FIRE) {
            startTransition("fire_to_small_mario", PlayerPowerState.SMALL, false);
            invincibleTimer = INVINCIBLE_SECONDS;
            MarioResourceManager.sound("smb_pipe").play();
        } else if (powerState == PlayerPowerState.BIG) {
            startTransition("big_to_small_mario", PlayerPowerState.SMALL, false);
            invincibleTimer = INVINCIBLE_SECONDS;
            MarioResourceManager.sound("smb_pipe").play();
        } else {
            beginDeathAnimation();
        }
    }

    /**
     * Builds this growth/shrink strip's flipbook frames and hands them to
     * {@link PowerStateActor#beginTransition} - ported from {@code
     * Player.Grow()}/{@code Decerease()}'s {@code AnimationGroup.add(new
     * ...MarioAnim(...))} calls. Unlike the original (a separate overlay
     * sprite drawn on top of the frozen real player, with the whole world
     * paused via {@code pauseEnemys()} until it finishes), this drives
     * {@link #paintPowerState}'s render directly and only freezes the player
     * itself (via {@link #applyMovement}'s early return while {@link
     * PowerStateActor#isTransitioning} is true) - simpler, but doesn't freeze
     * the *world*: {@code EnemyCollisionResolver} still runs every frame
     * during a transition, so {@link #grow()}/{@link #shrink()} both guard
     * against re-entering mid-morph instead (see their own doc) rather than
     * relying on nothing being able to touch Mario while frozen the way the
     * original's own {@code pauseEnemys()} guaranteed.
     *
     * @param preShiftUp32 true only for Small -> Big: the strip's cells are
     *                     all Big-sized (32x64, confirmed against the source
     *                     PNGs) even for its small-mario-posed early frames,
     *                     so the box needs the extra headroom immediately,
     *                     not just once {@link #changePowerState} applies at
     *                     the end - matches the original's own immediate
     *                     {@code this.setY(this.getY() - 32)} in this one case.
     */
    private void startTransition(String regionName, PlayerPowerState target, boolean preShiftUp32) {
        TextureRegion[] rowFrames = MarioResourceManager.region(regionName)
                .split(transitionFrameWidth * MarioConfiguration.ART_SCALE,
                        transitionFrameHeight * MarioConfiguration.ART_SCALE)[0];
        TextureRegion[] frames = new TextureRegion[rowFrames.length];
        for (int i = 0; i < rowFrames.length; i++) {
            TextureRegion copy = new TextureRegion(rowFrames[i]);
            if (!facingRight) {
                copy.flip(true, false);
            }
            frames[i] = copy;
        }
        float yShift = preShiftUp32 ? (PlayerPowerState.BIG.height - PlayerPowerState.SMALL.height) : 0f;
        beginTransition(frames, target, transitionFrameWidth, transitionFrameHeight, yShift);
        speed = 0;
        gravity = 0;
    }

    /** Applies the new power state once a {@link #startTransition} flipbook finishes - see {@link PowerStateActor#onTransitionComplete}. */
    @Override
    protected void onTransitionComplete(PlayerPowerState target) {
        changePowerState(target);
    }

    /**
     * The enemy-hit-while-Small death: launches Mario up then lets gravity
     * pull him back down off-screen before respawning - ported from
     * {@code Player.Decerease()}'s {@code ID==1} branch and
     * {@code Animations/FallingDeadMario.java}. A pit-fall (see {@link
     * #applyMovement}'s fall-out check) stays the original's instant, silent
     * {@code Restart()} instead - that distinction (animated "you died" beat
     * vs. silent respawn) is the original's own, not new here.
     *
     * <p>Guarded against re-entry: {@code EnemyCollisionResolver} re-checks
     * every enemy Mario still overlaps every frame, and this death has no
     * immediate invincibility window of its own (unlike {@link #shrink()}'s
     * other two branches, which set one right away) - in the original, a
     * dying Mario stops being "Mario" to collision entirely (its own
     * {@code Decerease()} swaps in a dead-state ID immediately); here, {@link
     * #dyingAnimated} already being true is that same signal, so without this
     * guard the same still-overlapping enemy re-triggered this method - and
     * replayed "smb_mariodie" - every single frame until Mario finally fell
     * clear of it.
     */
    private void beginDeathAnimation() {
        if (dyingAnimated) {
            return;
        }
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
     * Respawns immediately at the last checkpoint (see {@link PowerStateActor
     * #updateCheckpoint}), matching the original's instant {@code Restart()}
     * - no death animation/delay, unlike {@link #beginDeathAnimation()}.
     *
     * <p>Player has no notion of lives or game over; {@link #consumeDeath()}
     * is how {@code MarioGameScreen} finds out a life should be charged -
     * see {@code GameStateController}.
     */
    private void die() {
        // No smb_mariodie here - see this method's own doc ("instant, silent
        // Restart()"); the original's Restart() plays no sound at all,
        // confirmed by reading the source. A stray call here (since fixed)
        // used to contradict that doc comment.
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
     *
     * <p>{@code Player.Jump(int)} - the explicit-gravity overload both this
     * and the Bouncer's own relaunch (see {@code moveYWithCollision}) port
     * from - always plays {@code smb_stomp}, confirmed by reading the source
     * rather than assumed: a gap wider than docs/MARIO_PORT_PLAN_PHASE2.md
     * Step P2.11.3's own "the Bouncer's launch" wording suggested, since
     * every ordinary enemy stomp shares that exact same original method.
     */
    public void bounceOffEnemy() {
        gravity = -8f;
        onGround = false;
        MarioResourceManager.sound("smb_stomp").play();
    }

    /** Ported from {@code Player.STAR()} - temporary invincibility (no speed/visual flourish yet). */
    public void collectStar() {
        starTimer = STAR_SECONDS;
    }

    /**
     * Debug-only Small -> Big -> Fire -> Small cycle, reusing {@link #grow()}'s
     * own morph-flipbook machinery for the first two steps and
     * {@link #startTransition} directly (bypassing {@link #shrink()}'s
     * "Small means die" branch, which a debug cycle should never trigger) for
     * the last. See docs/MARIO_PORT_PLAN_PHASE2.md §8.3.5. No-ops while a
     * transition/death animation is already playing, same guard {@link #grow()}/
     * {@link #shrink()} implicitly get from their own callers never firing
     * mid-animation.
     */
    public void debugCyclePowerState() {
        if (isTransitioning() || dyingAnimated) {
            return;
        }
        if (powerState == PlayerPowerState.FIRE) {
            startTransition("fire_to_small_mario", PlayerPowerState.SMALL, false);
        } else {
            grow();
        }
    }

    public PlayerPowerState getPowerState() {
        return powerState;
    }

    /** Used by {@code fx.MarioGhost}'s static snapshot at the axe-triggered boss finale - see that class's doc. */
    public boolean isFacingRight() {
        return facingRight;
    }

    public boolean isOnGround() {
        return onGround;
    }

    /** Set once at level load from {@code "Sea".equals(level.attribute)} - see {@link #water}'s doc. Never toggled mid-level, matching the original. */
    public void setWater(boolean water) {
        this.water = water;
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
        onLift = true;
    }

    /** Cleared by {@code LiftCollisionResolver} the moment it no longer finds a landing spot for the player - see {@link #onLift}'s own doc. */
    public void setOnLift(boolean onLift) {
        this.onLift = onLift;
    }

    /**
     * Overrides {@code input.poll()} for one or more frames - used to drive
     * Mario through a scripted sequence (walking into a level-complete flag,
     * standing still while entering a pipe) without the player's own input.
     * Pass {@code null} to release control back to {@code input}.
     */
    public void setForcedCommand(PlatformerCommand command) {
        forcedCommand = command;
    }

    public void clearForcedCommand() {
        forcedCommand = null;
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

package au.com.guidebee.morsetoolkit.platformer.actor;

import com.guidebee.game.graphics.Batch;
import com.guidebee.game.graphics.TextureRegion;
import com.guidebee.game.microedition.Layer;

/**
 * The reusable half of a platformer's player character - see
 * PLATFORMER_ENGINE_ARCHITECTURE.md §3.2. Owns the state-machine scaffolding
 * every power-up-driven player needs (invincibility/star/shield timers, a
 * morph-transition flipbook, checkpoint/respawn tracking) and leaves the two
 * genuinely game-specific halves - how this frame actually moves (including
 * polling input), and how the current power state actually renders - to the
 * subclass via {@link #applyMovement}/{@link #paintPowerState}.
 *
 * <p>Deliberately does *not* try to generalize a "forced command" scripted-
 * override concept the way an earlier draft of this class considered:
 * {@link #applyMovement} takes only {@code delta}, not a command type, so
 * this class never needs to know what a "command" is at all - a game's own
 * input-override mechanism (Mario's own {@code Player#setForcedCommand}) can
 * live entirely in the subclass instead, with zero loss of reuse here.
 *
 * @param <S> the concrete power-state enum (e.g. Mario's own {@code PlayerPowerState}).
 */
public abstract class PowerStateActor<S extends Enum<S>> extends Layer {

    private static final float TRANSITION_FRAME_SECONDS = 120f / 1000f;

    protected S powerState;

    protected float invincibleTimer;
    protected float starTimer;
    protected float shieldTimer;
    protected boolean blinkVisible = true;
    private boolean debugInvincible;

    private TextureRegion[] transitionFrames;
    private int transitionFrameIndex;
    private float transitionFrameTimer;
    private S transitionTarget;
    private float transitionWidth;
    private float transitionHeight;

    /** The current respawn point - starts wherever the subclass places this actor, advances via {@link #updateCheckpoint} while grounded. */
    protected float checkpointX;
    protected float checkpointY;
    private float checkpointSaveTimer;

    protected PowerStateActor(float x, float y, float width, float height, boolean visible, S initialPowerState) {
        super(x, y, width, height, visible);
        this.powerState = initialPowerState;
        this.checkpointX = x;
        this.checkpointY = y;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
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
        }
        applyMovement(delta);
    }

    @Override
    public void paint(Batch batch) {
        paintPowerState(batch);
    }

    /** Everything this frame does beyond the shared timer bookkeeping above - subclass owns polling input, physics, animation, and any extra state machines (e.g. a death sequence) of its own. */
    protected abstract void applyMovement(float delta);

    /** How to render the current power state's frame - subclass owns its own atlas regions; see {@link #isTransitioning()}/{@link #currentTransitionFrame()} for drawing mid-morph. */
    protected abstract void paintPowerState(Batch batch);

    // ---- Invincibility / star / shield ----

    public boolean hasStar() {
        return starTimer > 0;
    }

    public boolean isInvincible() {
        return invincibleTimer > 0 || hasStar() || shieldTimer > 0 || debugInvincible;
    }

    /** Extends (never shortens) the shield window - used to protect the player during a scripted sequence. */
    public void setInvincibleFor(float seconds) {
        shieldTimer = Math.max(shieldTimer, seconds);
    }

    /** Debug-only god mode. */
    public void setDebugInvincible(boolean invincible) {
        debugInvincible = invincible;
    }

    public boolean isDebugInvincible() {
        return debugInvincible;
    }

    // ---- Morph-transition flipbook ----

    protected boolean isTransitioning() {
        return transitionFrames != null;
    }

    protected TextureRegion currentTransitionFrame() {
        return transitionFrames[transitionFrameIndex];
    }

    protected float getTransitionWidth() {
        return transitionWidth;
    }

    protected float getTransitionHeight() {
        return transitionHeight;
    }

    /**
     * Starts a growth/shrink-style morph flipbook - the subclass has already
     * built {@code frames} (sliced from its own atlas, flipped for facing,
     * etc.) and knows what size to draw them at; {@code yShift} lets a
     * grow-in-place transition keep the actor's feet planted (shifts up by
     * this many pixels) the moment the flipbook starts, not just once it
     * finishes - pass {@code 0} for no shift.
     */
    protected void beginTransition(TextureRegion[] frames, S target, float width, float height, float yShift) {
        this.transitionFrames = frames;
        this.transitionFrameIndex = 0;
        this.transitionFrameTimer = 0;
        this.transitionTarget = target;
        this.transitionWidth = width;
        this.transitionHeight = height;
        if (yShift != 0f) {
            setY(getY() - yShift);
        }
    }

    /** Steps the flipbook; calls {@link #onTransitionComplete} once it's played through. */
    protected void updateTransition(float delta) {
        transitionFrameTimer += delta;
        if (transitionFrameTimer < TRANSITION_FRAME_SECONDS) {
            return;
        }
        transitionFrameTimer = 0;
        transitionFrameIndex++;
        if (transitionFrameIndex >= transitionFrames.length) {
            S target = transitionTarget;
            transitionFrames = null;
            transitionTarget = null;
            onTransitionComplete(target);
        }
    }

    /** Called once a {@link #beginTransition} flipbook finishes playing - apply {@code target} as the new power state. */
    protected abstract void onTransitionComplete(S target);

    // ---- Checkpoint / respawn ----

    /**
     * Ticks the "promote respawn point after standing still for a while"
     * timer - see {@code Player#updateCheckpoint}'s own doc for the exact
     * semantics this ports verbatim (only the tuning values are threaded as
     * parameters here instead of baked-in constants, since they're this
     * game's own tuning, not a toolkit-wide default).
     *
     * @param eligible whether this frame counts toward a checkpoint
     *                 promotion at all (e.g. grounded and not standing on a
     *                 moving lift)
     */
    protected void updateCheckpoint(float delta, boolean eligible, float saveIntervalSeconds, float minDistancePx) {
        if (!eligible) {
            return;
        }
        checkpointSaveTimer += delta;
        if (checkpointSaveTimer < saveIntervalSeconds) {
            return;
        }
        checkpointSaveTimer = 0;
        float dx = getX() - checkpointX;
        float dy = getY() - checkpointY;
        if (Math.sqrt(dx * dx + dy * dy) > minDistancePx) {
            checkpointX = getX();
            checkpointY = getY();
        }
    }
}

package au.com.guidebee.morsetoolkit.activity.mario.actors.enemies;

import com.guidebee.game.graphics.TextureRegion;
import com.guidebee.game.microedition.Sprite;

import au.com.guidebee.morsetoolkit.activity.mario.MarioConfiguration;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioContext;
import au.com.guidebee.morsetoolkit.platformer.core.TileMovement;

/**
 * Common base for enemies (EnemyMashroom/EnemyTurtle/TurtleShell in the
 * original). See docs/MARIO_PORT_PLAN.md Step 6.1.
 *
 * <p>Stomp-vs-hurt is decided by {@code EnemyCollisionResolver}, not here -
 * this class only reacts via {@link #onStomped}/{@link #onTouchedSide}.
 *
 * <p>{@link #walkAndFall} (constant-speed walk + gravity + wall-bounce) is a
 * helper subclasses call from their own {@code act()}, not something this
 * class does automatically - {@code TurtleShell} needs to sit motionless
 * while "standing" and only walk once kicked, so a fixed base-class
 * {@code act()} would fight that rather than help it.
 */
public abstract class Enemy extends Sprite {

    protected static final float PHYSICS_FPS = 60f;

    private boolean active = true;
    protected boolean movingRight;

    /**
     * {@code frameWidth}/{@code frameHeight} are world-space (unaffected by
     * {@link MarioConfiguration#ART_SCALE}) - {@code region} is sliced at
     * {@code frameWidth * ART_SCALE} per {@link com.guidebee.game.microedition.Sprite}'s
     * own two-arg constructor (which uses that same value for both the pixel
     * split and the actor's world bounds), so the bounds are corrected back
     * to world space immediately after via {@link #setSize}.
     */
    protected Enemy(TextureRegion region, int frameWidth, int frameHeight, float x, float y, boolean movingRight) {
        super(region, frameWidth * MarioConfiguration.ART_SCALE, frameHeight * MarioConfiguration.ART_SCALE);
        setSize(frameWidth, frameHeight);
        setPosition(x, y);
        this.movingRight = movingRight;
    }

    public boolean isActive() {
        return active;
    }

    /** Public so {@code MarioGameScreen} can silently clear the field when the boss fight ends (see {@code Axe}'s own doc) - every other caller is a subclass reacting to its own hit/stomp/fireball logic. */
    public void deactivate() {
        active = false;
        remove();
    }

    public boolean overlaps(float x, float y, int width, int height) {
        return active
                && x < getX() + getWidth() && x + width > getX()
                && y < getY() + getHeight() && y + height > getY();
    }

    /**
     * Whether touching another enemy turns this one around, ported from
     * {@code Collusion/EnemyToEnemy.java}'s own per-type switch - true for
     * the plain ground-walkers it calls {@code OtherEnemyTouchedFromLeft/
     * Right()} on (EnemyMashroom/EnemyTurtle/FlyingTurtle/Helmet/Spikey);
     * every other type (Monkey, Rocket, the fish/water enemies, Boss, ...)
     * is a no-op there, matching this defaulting to false. See {@code
     * EnemyToEnemyResolver} for the actual per-frame check.
     */
    public boolean bouncesOffEnemies() {
        return false;
    }

    /** Flips {@link #movingRight} - see {@link #bouncesOffEnemies}. */
    public void reverseDirection() {
        movingRight = !movingRight;
    }

    public boolean isMovingRight() {
        return movingRight;
    }

    /** Walks at a constant pace, falls under gravity, turns around at walls. */
    protected void walkAndFall(float delta, float gravity, float walkSpeed) {
        float frames = delta * PHYSICS_FPS;
        TileMovement.moveY(this, gravity * frames, MarioContext.world());
        if (TileMovement.moveX(this, (movingRight ? walkSpeed : -walkSpeed) * frames, MarioContext.world())) {
            movingRight = !movingRight;
        }
    }

    /**
     * Player landed on top. Default: die (matches EnemyMashroom; EnemyTurtle
     * overrides).
     *
     * @return true if this was a genuine, safe stomp - {@code
     * EnemyCollisionResolver} only bounces Mario and awards the stomp score
     * when this returns true. An override that can't be safely stomped
     * (Spikey, PiranhaPlant, OrbitingFireball, ...) delegates straight to
     * {@link #onTouchedSide} and returns false: without that, the resolver
     * used to bounce/score Mario off these exactly as if they'd been beaten,
     * *in addition to* whatever onTouchedSide just did to hurt him - and,
     * since bounceOffEnemy() plays "smb_stomp" with no re-entry guard of its
     * own (unlike shrink()'s own dyingAnimated/invincibility checks), every
     * frame Mario kept overlapping a stationary hazard like a FireBar ring or
     * a Piranha Plant re-fired that sound on top of his one real death cry,
     * which is what actually read as the death sound "playing multiple
     * times".
     */
    public boolean onStomped(Player player) {
        deactivate();
        return true;
    }

    /** Player touched from the side (or from below). Default: hurt the player unless they have a star. */
    public void onTouchedSide(Player player) {
        if (player.hasStar()) {
            deactivate();
        } else {
            player.shrink();
        }
    }

    /** A fireball or a moving shell hit this enemy - always dies, star or not. */
    public void onDefeatedByProjectile() {
        deactivate();
    }
}

package au.com.guidebee.morsetoolkit.activity.mario.actors.enemies;

import java.util.ArrayList;
import java.util.Random;

import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.actors.projectiles.BossFire;
import au.com.guidebee.morsetoolkit.activity.mario.actors.projectiles.Hammer;
import au.com.guidebee.morsetoolkit.activity.mario.fx.DirectFallingSprite;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioContext;
import au.com.guidebee.morsetoolkit.platformer.core.TileMovement;

/**
 * The end-of-castle boss, ported from {@code Objects/Boss.java}. Patrols
 * +-3 tiles around its spawn, jumping occasionally and (fire mode) breathing
 * {@link BossFire} at Mario, until Mario's x passes the boss's own x, at
 * which point it walks steadily toward him instead (see {@link #act}) -
 * bounded overall by {@code maxXPx} (the level's own bridge-end wall). Killed
 * instantly by a star touch/stomp, or by 6 Fire Mario fireball hits
 * (see {@link #onDefeatedByProjectile}) - a plain stomp without a star just
 * hurts Mario instead, matching the classic games (jumping on Bowser's head
 * doesn't kill him here either).
 *
 * <p>{@link #die} spawns a {@link DirectFallingSprite} corpse before
 * deactivating - matches every one of the original's own death reactions
 * (star stomp/touch, a moving shell, running out of fireball hits), which
 * all route through the same {@code DirectFalling(this.getImage(),
 * this.getX(), this.getY())} call.
 *
 * <p>The "boss" region is 64x64 per frame, 3 cols x 2 rows - confirmed
 * against {@code WholeGame.java}'s {@code getImages("Boss.png", 3, 2)}.
 */
public class Boss extends Enemy {

    private static final float FRAME_INTERVAL = 0.3f;

    private static final float GRAVITY_STEP = 0.5f;
    private static final float GRAVITY_CAP = 4f;
    private static final float JUMP_GRAVITY = -12f;
    private static final float PATROL_SPEED = 1f;
    private static final float CHASE_SPEED = 1f;
    private static final int START_LIFE = 5;

    private static final Random RANDOM = new Random();

    private final float leftBoundX;
    private final float rightBoundX;
    private final float maxXPx;
    private final boolean hammerMode;
    private final int tileSize;

    private int life = START_LIFE;
    private float gravity;
    private float jumpTimer = randomTicks(3, 6, 20);
    private float fireThrowTimer = randomTicks(1, 10, 10);
    private float fireDelayTimer = 20;
    private boolean throwingFire;
    private float hammerThrowTimer = randomTicks(1, 10, 10);
    private float hammerXSpeed;
    private float hammerGravity;

    private float frameTimer;
    private boolean showingFirstFrame = true;

    /** @param maxXPx the level's own bound past which the boss can't walk further right (the original's {@code MaxX*32}, i.e. {@code patrolLength*TILE_SIZE}). */
    public Boss(float x, float y, float maxXPx, boolean hammerMode, int tileSize) {
        super(MarioResourceManager.region("boss"), tileSize * 2, tileSize * 2, x, y, true);
        leftBoundX = x - 3 * tileSize;
        rightBoundX = x + 3 * tileSize;
        this.maxXPx = maxXPx;
        this.hammerMode = hammerMode;
        this.tileSize = tileSize;
    }

    private static float randomTicks(int min, int maxInclusive, int multiplier) {
        return (min + RANDOM.nextInt(maxInclusive - min + 1)) * multiplier;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!isActive()) {
            return;
        }
        float frames = delta * PHYSICS_FPS;

        if (hammerMode) {
            updateHammerThrow(delta, frames);
        }

        if (gravity < GRAVITY_CAP) {
            gravity += GRAVITY_STEP * frames;
        }
        TileMovement.moveY(this, gravity * frames, MarioContext.world());

        Player player = MarioContext.player();
        boolean chasing = player.getX() > getX();
        if (chasing) {
            setX(getX() + CHASE_SPEED * frames);
        } else {
            jumpTimer -= frames;
            if (jumpTimer < 0) {
                if (player.getX() < getX()) {
                    gravity = JUMP_GRAVITY;
                }
                jumpTimer = randomTicks(3, 6, 40);
            }
            if (getX() < leftBoundX) {
                movingRight = true;
            } else if (getX() > rightBoundX) {
                movingRight = false;
            }
            setX(getX() + (movingRight ? PATROL_SPEED : -PATROL_SPEED) * frames);

            updateFireBreath(delta, frames, player);
        }

        if (getX() + getWidth() > maxXPx) {
            setX(maxXPx - getWidth());
        }

        updateLookAtMarioFrame(delta, player);
    }

    /** Ported from {@code HammerFire()} - only while patrolling (never while chasing), matching the original's branch structure. */
    private void updateFireBreath(float delta, float frames, Player player) {
        fireThrowTimer -= frames;
        if (fireThrowTimer >= 0 || player.getX() >= getX()) {
            return;
        }
        throwingFire = true;
        fireDelayTimer -= frames;
        if (fireDelayTimer < 0) {
            BossFire fire = new BossFire(getX(), ((int) getY() / tileSize) * tileSize, tileSize);
            MarioContext.world().addHazard(fire);
            MarioContext.spawn(fire);
            MarioResourceManager.sound("smb_bowserfire").play();
            fireThrowTimer = randomTicks(5, 10, 20);
            fireDelayTimer = 40;
            throwingFire = false;
        }
    }

    /** Ported from {@code HammerThrow()}/{@code ContinuousHammerThrow()} - runs regardless of patrol/chase, matching the original. */
    private void updateHammerThrow(float delta, float frames) {
        hammerThrowTimer -= frames;
        if (hammerThrowTimer < 0) {
            hammerThrowTimer = randomTicks(4, 10, 30);
            hammerGravity = -7 - RANDOM.nextInt(4);
            hammerXSpeed = -2 - RANDOM.nextInt(3);
        }
        if (hammerThrowTimer < 10 && MarioContext.player().getX() < getX()) {
            Hammer hammer = new Hammer(getX(), getY(), hammerXSpeed, hammerGravity);
            MarioContext.world().addHazard(hammer);
            MarioContext.spawn(hammer);
        }
    }

    /**
     * Ported from {@code LookAtMario()}/{@code HammerFire()}'s own
     * {@code setAnimationFrame} calls - frame indices are linear (0-5) across
     * the 3x2 "boss" strip, matching the original's own numbering exactly:
     * 0/1 = look-left idle cycle, 4/5 = look-right idle cycle, 2 = the
     * fixed "spitting fire" pose (no second frame, matching the original's
     * {@code setAnimationFrame(2, 2)}).
     */
    private void updateLookAtMarioFrame(float delta, Player player) {
        frameTimer += delta;
        if (frameTimer >= FRAME_INTERVAL) {
            frameTimer = 0;
            showingFirstFrame = !showingFirstFrame;
        }
        if (throwingFire) {
            setFrame(2);
            return;
        }
        boolean lookLeft = player.getX() < getX();
        setFrame((lookLeft ? 0 : 4) + (showingFirstFrame ? 0 : 1));
    }

    /** Never a real stomp - see the class doc ("jumping on Bowser's head doesn't kill him here either"); always routes through {@link #onTouchedSide}'s identical star/hurt logic. */
    @Override
    public boolean onStomped(Player player) {
        onTouchedSide(player);
        return false;
    }

    @Override
    public void onTouchedSide(Player player) {
        if (player.hasStar()) {
            die(true);
        } else {
            player.shrink();
        }
    }

    @Override
    public void onDefeatedByProjectile() {
        life--;
        if (life < 0) {
            die(false);
        }
    }

    /** @param kicked whether this was a star touch (plays the extra "kick" sound, matching the original) - a fireball kill doesn't. */
    private void die(boolean kicked) {
        MarioResourceManager.sound("smb_bowserfalls").play();
        if (kicked) {
            MarioResourceManager.sound("smb_kick").play();
        }
        DirectFallingSprite.spawn(getX(), getY(),
                MarioResourceManager.region("boss").split(tileSize * 2, tileSize * 2)[0][0]);
        deactivate();
        for (Enemy enemy : new ArrayList<>(MarioContext.world().getEnemies())) {
            if (enemy != this && enemy.isActive()) {
                enemy.onDefeatedByProjectile();
            }
        }
    }
}

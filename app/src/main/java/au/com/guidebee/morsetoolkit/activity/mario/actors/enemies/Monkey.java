package au.com.guidebee.morsetoolkit.activity.mario.actors.enemies;

import java.util.Random;

import au.com.guidebee.morsetoolkit.activity.mario.MarioConfiguration;
import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.actors.projectiles.Hammer;
import au.com.guidebee.morsetoolkit.activity.mario.fx.DirectFallingSprite;
import au.com.guidebee.morsetoolkit.activity.mario.fx.FallingDeadSprite;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioContext;
import au.com.guidebee.morsetoolkit.platformer.core.TileMovement;

/**
 * A hammer-throwing enemy, ported from {@code Objects/Monkey.java}: patrols
 * a tight +-1 tile range around its spawn, jumping occasionally, and throws
 * a {@link Hammer} at Mario on a random timer (see the 3-arg
 * {@link Hammer#Hammer(float, float, boolean)} constructor, built for
 * exactly this). A stomp sends it drifting/falling away (see {@link
 * #onStomped}, ported from {@code MarioJumpedOnEnemy()}, not the {@link
 * Enemy} default); a side-touch hurts Mario unless starred (the default, not
 * overridden).
 *
 * <p>Skips the original's {@code ComeDown}/{@code setYloc} interaction - a
 * mechanism for some *external* system to override this enemy's y position,
 * which nothing in this port's architecture ever calls (unlike the
 * original, where some other collision code apparently could) - dead code
 * in this port's context, not a simplification of anything observable.
 *
 * <p>The "monkey" region is 32x48 per frame (taller than a tile, like
 * {@code EnemyTurtle}), 6 frames (96x96 total, 3 cols x 2 rows) - confirmed
 * against the source PNG directly.
 */
public class Monkey extends Enemy {

    private static final float PATROL_SPEED = 0.5f;
    private static final float GRAVITY_STEP = 0.3f;
    private static final float GRAVITY_CAP = 5f;
    private static final float JUMP_GRAVITY = -9f;
    private static final float FRAME_INTERVAL = 0.3f;

    private static final Random RANDOM = new Random();

    private final float leftBoundX;
    private final float rightBoundX;
    private final int tileSize;

    private float gravity;
    private float jumpTimer = randomTicks(3, 6, 20);
    private float hammerTimer = randomTicks(1, 10, 10);
    private float frameTimer;
    private boolean showingFirstFrame = true;

    public Monkey(float x, float y, int tileSize) {
        super(MarioResourceManager.region("monkey"), tileSize, (tileSize * 3) / 2, x, y, true);
        leftBoundX = x - tileSize;
        rightBoundX = x + tileSize;
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
        Player player = MarioContext.player();

        updateHammerThrow(frames, player);

        jumpTimer -= frames;
        if (jumpTimer < 0) {
            gravity = JUMP_GRAVITY;
            jumpTimer = randomTicks(3, 6, 40);
        }
        if (gravity < GRAVITY_CAP) {
            gravity = Math.min(GRAVITY_CAP, gravity + GRAVITY_STEP * frames);
        }
        TileMovement.moveY(this, gravity * frames, MarioContext.world());

        if (getX() < leftBoundX) {
            movingRight = true;
        } else if (getX() > rightBoundX) {
            movingRight = false;
        }
        setX(getX() + (movingRight ? PATROL_SPEED : -PATROL_SPEED) * frames);

        updateLookAtMarioFrame(delta, player);
    }

    /** Ported from {@code HammerThrow()} - a fresh random throw, direction toward wherever the player currently is. */
    private void updateHammerThrow(float frames, Player player) {
        hammerTimer -= frames;
        if (hammerTimer < 0) {
            hammerTimer = randomTicks(1, 10, 20);
            boolean towardLeft = player.getX() < getX();
            Hammer hammer = new Hammer(getX(), getY(), towardLeft);
            MarioContext.world().addHazard(hammer);
            MarioContext.spawn(hammer);
        }
    }

    /** Frame indices 0/1 = look-left idle cycle, 4/5 = look-right idle cycle - same 3x2 strip layout as {@code Boss}. */
    private void updateLookAtMarioFrame(float delta, Player player) {
        frameTimer += delta;
        if (frameTimer >= FRAME_INTERVAL) {
            frameTimer = 0;
            showingFirstFrame = !showingFirstFrame;
        }
        boolean lookLeft = player.getX() < getX();
        setFrame((lookLeft ? 0 : 4) + (showingFirstFrame ? 0 : 1));
    }

    /**
     * Ported from {@code MarioJumpedOnEnemy()}: unlike the {@link Enemy}
     * default (a plain {@link #deactivate}), a stomped monkey drifts away
     * while falling - the original passes no sound of its own here (the
     * generic stomp bounce/sound already covers it, same as every ordinary
     * enemy - see {@code Player#bounceOffEnemy}'s own doc).
     */
    @Override
    public boolean onStomped(Player player) {
        boolean driftRight = player.getX() >= getX();
        DirectFallingSprite.spawnDrifting(getX(), getY(),
                MarioResourceManager.region("monkey").split(tileSize, (tileSize * 3) / 2)[0][0], driftRight);
        deactivate();
        return true;
    }

    @Override
    public void onDefeatedByProjectile() {
        MarioResourceManager.sound("smb_kick").play();
        FallingDeadSprite.spawn(getX(), getY(),
                MarioResourceManager.region("monkey")
                        .split(tileSize * MarioConfiguration.ART_SCALE, ((tileSize * 3) / 2) * MarioConfiguration.ART_SCALE)[0][0]);
        deactivate();
    }
}

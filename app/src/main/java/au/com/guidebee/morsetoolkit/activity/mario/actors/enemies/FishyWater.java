package au.com.guidebee.morsetoolkit.activity.mario.actors.enemies;

import au.com.guidebee.morsetoolkit.activity.mario.MarioConfiguration;
import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.fx.FallingDeadSprite;

/**
 * A Cheep-Cheep analog for Sea levels, ported from {@code Objects/FishyWater.java}:
 * always swims left at a fixed speed - never turns around, never falls under
 * gravity, no tile collision at all (open-water movement, unlike
 * {@link Enemy#walkAndFall}'s ground-walker helper) - optionally bobbing
 * vertically between {@code spawnY-32} and {@code spawnY+32} (the "UpDown"
 * variants). Like every other water enemy here (see {@link OctoPussy}), it
 * can never be safely stomped - touching it from any side, top included,
 * hurts Mario unless he has a Star, confirmed by reading
 * {@code MarioJumpedOnEnemy()}'s own always-hurt branch (there's no
 * "stomp kills it" path at all, unlike a ground enemy).
 *
 * <p>{@code type} matches the original's own 1-4 argument (1=grey straight,
 * 2=grey up-down, 3=red straight, 4=red up-down) - red is faster and
 * animates quicker than grey, per the original's own per-type speed/timer
 * literals.
 */
public class FishyWater extends Enemy {

    private static final float SPEED_GREY = 0.5f;
    private static final float SPEED_RED = 1f;
    private static final float ANIM_INTERVAL_GREY = 200f / 60f;
    private static final float ANIM_INTERVAL_RED = 100f / 60f;
    private static final float BOB_SPEED = 0.5f;

    private final boolean red;
    private final float speedX;
    private final boolean bobbing;
    private final float upY;
    private final float downY;
    private final float animInterval;
    private final int tileSize;

    private boolean goingUp = true;
    private float animTimer;
    private boolean showingFirstFrame = true;

    public FishyWater(float x, float y, int type, int tileSize) {
        super(MarioResourceManager.region(type == 3 || type == 4 ? "fish_red" : "fish_grey"),
                tileSize, tileSize, x, y, false);
        this.red = type == 3 || type == 4;
        this.bobbing = type == 2 || type == 4;
        this.speedX = red ? -SPEED_RED : -SPEED_GREY;
        this.animInterval = red ? ANIM_INTERVAL_RED : ANIM_INTERVAL_GREY;
        this.upY = y - tileSize;
        this.downY = y + tileSize;
        this.tileSize = tileSize;
        setFrame(0);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!isActive()) {
            return;
        }
        float frames = delta * PHYSICS_FPS;
        setX(getX() + speedX * frames);

        if (bobbing) {
            if (getY() < upY) {
                goingUp = false;
            }
            if (getY() > downY) {
                goingUp = true;
            }
            setY(getY() + (goingUp ? -BOB_SPEED : BOB_SPEED) * frames);
        }

        animTimer += delta;
        if (animTimer >= animInterval) {
            animTimer = 0;
            showingFirstFrame = !showingFirstFrame;
            setFrame(showingFirstFrame ? 0 : 1);
        }
    }

    @Override
    public void onStomped(Player player) {
        onTouchedSide(player);
    }

    @Override
    public void onDefeatedByProjectile() {
        MarioResourceManager.sound("smb_kick").play();
        FallingDeadSprite.spawn(getX(), getY(),
                MarioResourceManager.region(red ? "fish_red" : "fish_grey")
                        .split(tileSize * MarioConfiguration.ART_SCALE, tileSize * MarioConfiguration.ART_SCALE)[0][0]);
        deactivate();
    }
}

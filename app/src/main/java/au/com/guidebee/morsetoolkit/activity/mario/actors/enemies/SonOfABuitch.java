package au.com.guidebee.morsetoolkit.activity.mario.actors.enemies;

import java.util.Random;

import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioContext;

/**
 * A Lakitu-analog, ported from {@code Objects/SonOfABuitch.java} (yes, that's
 * the literal original class name). Floats at a fixed height ({@code y}
 * hardcoded to 80 in the original constructor - the level data's own
 * {@code y} is never read, same "ignores y" quirk as {@code LavaBall}),
 * swaying left and right around the player's own x (never gravity, never
 * tile collision - confirmed by its own empty
 * {@code CollidedWithJumping_Brick}, commented "this does not collide with
 * anything"), and periodically drops a {@link SpikeyEgg}.
 *
 * <p>The original's {@code PositiveX} field (passed as each egg's initial
 * facing) is set at construction and never actually reassigned anywhere in
 * the class despite existing seemingly for that purpose - ported faithfully
 * as a fixed {@code true}, not "fixed" into direction logic that was never
 * really there.
 *
 * <p>The "son_of_a_buitch" region is 32x48 per frame (taller than a tile,
 * like {@code EnemyTurtle}), 2 frames (64x48 total) - confirmed against the
 * source PNG directly.
 */
public class SonOfABuitch extends Enemy {

    private static final float FIXED_Y = 80f;
    private static final float SWAY_LIMIT = 100f;
    private static final float SWAY_SPEED = 1f;
    /** World-px per sway unit - matches the original's own `Movement * 2`. */
    private static final float SWAY_SCALE = 2f;
    private static final float REARING_BACK_TICKS = 10f;

    private static final Random RANDOM = new Random();

    private final int tileSize;
    private float sway = 100f;
    private boolean swayingLeft = true;
    private float throwTimer = 100f;

    public SonOfABuitch(float x, int tileSize) {
        super(MarioResourceManager.region("son_of_a_buitch"), tileSize, (tileSize * 3) / 2, x, FIXED_Y, true);
        this.tileSize = tileSize;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!isActive()) {
            return;
        }
        float frames = delta * PHYSICS_FPS;
        Player player = MarioContext.player();

        throwTimer -= frames;
        if (throwTimer < 0) {
            throwSpikeyEgg();
            setFrame(0);
        } else if (throwTimer > 1 && throwTimer < REARING_BACK_TICKS) {
            setFrame(1);
        }

        sway += (swayingLeft ? -SWAY_SPEED : SWAY_SPEED) * frames;
        if (sway < -SWAY_LIMIT) {
            swayingLeft = false;
        } else if (sway > SWAY_LIMIT) {
            swayingLeft = true;
        }
        setX(player.getX() + sway * SWAY_SCALE);
    }

    private void throwSpikeyEgg() {
        throwTimer = (1 + RANDOM.nextInt(5)) * 100f;
        SpikeyEgg egg = new SpikeyEgg(getX(), getY(), true, tileSize);
        MarioContext.world().addEnemy(egg);
        MarioContext.spawn(egg);
    }

    @Override
    public void onDefeatedByProjectile() {
        MarioResourceManager.sound("smb_kick").play();
        deactivate();
    }
}

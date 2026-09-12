package au.com.guidebee.morsetoolkit.activity.mario.actors.enemies;

import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.fx.FallingDeadSprite;

/**
 * A Spiny-analog - ported from {@code Objects/Spikey.java}: walks and falls
 * like {@link EnemyTurtle}, but (confirmed by reading the source, not
 * assumed) can never be safely stomped - every touch, from any side
 * including straight down, plays out identically ("if star: kill; else:
 * hurt Mario"), matching the classic games' own "can't jump on a Spiny"
 * rule. {@link #onStomped} overrides the {@link Enemy} default (which would
 * otherwise let a plain stomp kill it) to match; {@link #onTouchedSide}
 * already matches the default exactly, so isn't overridden.
 *
 * <p>Only ever spawns dynamically, when a {@link SpikeyEgg} lands (see that
 * class) - no level places one directly.
 *
 * <p>The "spikey" region is 32x32 per frame, 4 frames (128x32 total) -
 * confirmed against the source PNG directly.
 */
public class Spikey extends Enemy {

    private static final float GRAVITY = 5f;
    private static final float WALK_SPEED = 1f;
    private static final float ANIMATION_INTERVAL = 0.3f;

    private final int tileSize;
    private float animTimer;
    private boolean showingFirstFrame = true;

    public Spikey(float x, float y, boolean movingRight, int tileSize) {
        super(MarioResourceManager.region("spikey"), tileSize, tileSize, x, y, movingRight);
        this.tileSize = tileSize;
        setFrame(movingRight ? 2 : 0);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!isActive()) {
            return;
        }
        walkAndFall(delta, GRAVITY, WALK_SPEED);

        animTimer += delta;
        if (animTimer >= ANIMATION_INTERVAL) {
            animTimer = 0;
            showingFirstFrame = !showingFirstFrame;
        }
        setFrame((movingRight ? 2 : 0) + (showingFirstFrame ? 0 : 1));
    }

    @Override
    public boolean onStomped(Player player) {
        onTouchedSide(player);
        return false;
    }

    /** Ported from {@code Collusion/EnemyToEnemy.java}'s own {@code case 117}. */
    @Override
    public boolean bouncesOffEnemies() {
        return true;
    }

    @Override
    public void onDefeatedByProjectile() {
        MarioResourceManager.sound("smb_kick").play();
        FallingDeadSprite.spawn(getX(), getY(), MarioResourceManager.region("spikey").split(tileSize, tileSize)[0][0]);
        deactivate();
    }
}

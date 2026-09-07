package au.com.guidebee.morsetoolkit.activity.mario.actors.enemies;

import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.fx.FallingDeadSprite;

/**
 * The walking mushroom enemy (note: hostile, not the growth power-up -
 * that's {@code actors.items.Mushroom}), ported from
 * {@code Objects/EnemyMashroom.java}. Walks at a constant pace, falls under
 * gravity, turns around at walls; dies on stomp, hurts the player on side
 * touch (both the base {@link Enemy} defaults, so no overrides needed here).
 *
 * <p>The "enemy" region is a 2 cols x 4 rows strip: rows are
 * Sea/Ground/UnderGround/Castle (2 frames each) - World 1 never uses "Sea".
 */
public class EnemyMashroom extends Enemy {

    private static final float GRAVITY = 5f;
    private static final float WALK_SPEED = 1f;
    private static final float ANIMATION_INTERVAL = 0.3f;

    private final int frameA;
    private final int frameB;
    private float animTimer;
    private boolean showingA = true;

    public EnemyMashroom(float x, float y, String attribute) {
        super(MarioResourceManager.region("enemy"), 32, 32, x, y, false);
        int base = themedFrameBase(attribute);
        frameA = base;
        frameB = base + 1;
        setFrame(frameA);
    }

    private static int themedFrameBase(String attribute) {
        if ("UnderGround".equals(attribute)) {
            return 4;
        }
        if ("Castle".equals(attribute)) {
            return 6;
        }
        return 2;
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
            showingA = !showingA;
            setFrame(showingA ? frameA : frameB);
        }
    }

    /** Ported from {@code Collusion/EnemyToEnemy.java}'s own {@code case 100} - a ground-walker turns around when it touches another one. */
    @Override
    public boolean bouncesOffEnemies() {
        return true;
    }

    /** Ported from {@code Objects/EnemyMashroom.java}'s own {@code KilledByFireBall} - flips and drops the current pose off screen, matching every other enemy's fireball death (see {@code FallingDeadSprite}'s class doc). */
    @Override
    public void onDefeatedByProjectile() {
        MarioResourceManager.sound("smb_kick").play();
        FallingDeadSprite.spawn(getX(), getY(),
                MarioResourceManager.region("enemy").split(32, 32)[frameA / 2][frameA % 2]);
        deactivate();
    }
}

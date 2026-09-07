package au.com.guidebee.morsetoolkit.activity.mario.actors.enemies;

import com.guidebee.game.graphics.TextureRegion;

import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioContext;

/**
 * A Buzzy-Beetle-analog walking enemy, ported from {@code Objects/Helmet.java}:
 * walks and falls like {@link EnemyTurtle}, but at a constant (non-ramping)
 * gravity, and - confirmed by reading the source, not assumed - is entirely
 * immune to Fire Mario's fireballs (the original's own {@code KilledByFireBall()}
 * is an empty override, unlike every ground-walker this port has ported so
 * far), matching the classic games' own "buzzy beetles can't be fireballed"
 * rule. {@code color} ("normal"/"dark"/"white") picks which of three palettes
 * to use, passed down explicitly by whichever level spawned it (not derived
 * from the level's own attribute).
 *
 * <p>Skips the original's separate "a rising brick bumps this enemy into a
 * shell too" reaction ({@code CollidedWithJumping_Brick}) - no enemy type in
 * this port has that interaction yet (bricks don't currently know what's
 * standing on them), so adding it just for Helmet would be new, untested
 * surface area rather than reused pattern.
 *
 * <p>The "helmet" region is 32x32 per frame, 4 frames (128x32 total) -
 * confirmed against the source PNG directly.
 */
public class Helmet extends Enemy {

    private static final int FRAME_SIZE = 32;
    private static final float GRAVITY = 5f;
    private static final float WALK_SPEED = 1f;
    private static final float ANIMATION_INTERVAL = 0.3f;

    private final String color;
    private float animTimer;
    private boolean showingFirstFrame = true;

    public Helmet(float x, float y, String color) {
        super(regionFor(color), FRAME_SIZE, FRAME_SIZE, x, y, false);
        this.color = color;
    }

    private static TextureRegion regionFor(String color) {
        if ("dark".equals(color)) {
            return MarioResourceManager.region("helmet_dark");
        }
        if ("white".equals(color)) {
            return MarioResourceManager.region("helmet_white");
        }
        return MarioResourceManager.region("helmet");
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

    /** Ported from {@code Collusion/EnemyToEnemy.java}'s own {@code case 105}. */
    @Override
    public boolean bouncesOffEnemies() {
        return true;
    }

    @Override
    public void onStomped(Player player) {
        HelmetShell shell = new HelmetShell(getX(), getY(), color, movingRight);
        MarioContext.world().addEnemy(shell);
        MarioContext.spawn(shell);
        deactivate();
    }

    @Override
    public void onDefeatedByProjectile() {
        // Immune to fireballs - matches the original's empty KilledByFireBall().
    }
}

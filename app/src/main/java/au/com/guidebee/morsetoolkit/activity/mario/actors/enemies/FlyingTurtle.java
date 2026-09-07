package au.com.guidebee.morsetoolkit.activity.mario.actors.enemies;

import com.guidebee.game.graphics.TextureRegion;

import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioContext;
import au.com.guidebee.morsetoolkit.activity.mario.world.TileMovement;

/**
 * A free-roaming flying turtle (distinct from {@link FlyingTurtlePatrol},
 * which just bobs vertically in place), ported from
 * {@code Objects/FlyingTurtle.java}: wall-bounces horizontally like
 * {@link EnemyTurtle}, while a gentle gravity keeps nudging it down.
 *
 * <p>The original stays aloft via an externally-called {@code setYloc}/
 * {@code bounce()} hook (this port's architecture has no equivalent caller -
 * see {@code Monkey}'s own class doc on the same point) that resets its
 * gravity to -8 whenever some other system corrects its position - here,
 * that's reproduced as "whenever {@link TileMovement#moveY} reports this
 * hit something (floor or ceiling), bounce back up", which is exactly what
 * the hook's own effect was for and keeps it floating indefinitely the same
 * way, without needing the hook itself.
 *
 * <p>Stomping it drops it into a regular {@link EnemyTurtle} in place -
 * "dark" color forces {@code EnemyTurtle}'s own UnderGround-shell lookup
 * regardless of the current level's actual attribute, matching the
 * original's own {@code new EnemyTurtle(x, y, game, true, this.Color)} call.
 *
 * <p>The "flying_turtle"/"flying_turtle_dark" regions are 32x48 per frame,
 * 4 frames (128x48 total) - matches the original's own
 * {@code getImages("FlyingTurtle.png", 4, 1)} (4 columns, 1 row), confirmed
 * against the source PNG directly.
 */
public class FlyingTurtle extends Enemy {

    private static final int FRAME_WIDTH = 32;
    private static final int FRAME_HEIGHT = 48;
    private static final float GRAVITY_STEP = 0.25f;
    private static final float GRAVITY_CAP = 5f;
    private static final float BOUNCE_GRAVITY = -8f;
    private static final float WALK_SPEED = 1f;
    private static final float ANIMATION_INTERVAL = 0.3f;

    private final String color;
    private float gravity = -6f;
    private float animTimer;
    private boolean showingFirstFrame = true;

    public FlyingTurtle(float x, float y, String color) {
        super(regionFor(color), FRAME_WIDTH, FRAME_HEIGHT, x, y, false);
        this.color = color;
    }

    private static TextureRegion regionFor(String color) {
        return MarioResourceManager.region("normal".equals(color) ? "flying_turtle" : "flying_turtle_dark");
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!isActive()) {
            return;
        }
        float frames = delta * PHYSICS_FPS;
        if (gravity < GRAVITY_CAP) {
            gravity += GRAVITY_STEP * frames;
        }
        if (TileMovement.moveY(this, gravity * frames, MarioContext.world())) {
            gravity = BOUNCE_GRAVITY;
        }
        if (TileMovement.moveX(this, (movingRight ? WALK_SPEED : -WALK_SPEED) * frames, MarioContext.world())) {
            movingRight = !movingRight;
        }

        animTimer += delta;
        if (animTimer >= ANIMATION_INTERVAL) {
            animTimer = 0;
            showingFirstFrame = !showingFirstFrame;
        }
        setFrame((movingRight ? 2 : 0) + (showingFirstFrame ? 0 : 1));
    }

    @Override
    public void onStomped(Player player) {
        EnemyTurtle turtle = new EnemyTurtle(getX(), getY(), "normal".equals(color) ? "Ground" : "UnderGround");
        MarioContext.world().addEnemy(turtle);
        MarioContext.spawn(turtle);
        deactivate();
    }

    @Override
    public void onTouchedSide(Player player) {
        if (player.hasStar()) {
            onDefeatedByProjectile();
        } else {
            player.shrink();
        }
    }

    @Override
    public void onDefeatedByProjectile() {
        MarioResourceManager.sound("smb_kick").play();
        deactivate();
    }
}

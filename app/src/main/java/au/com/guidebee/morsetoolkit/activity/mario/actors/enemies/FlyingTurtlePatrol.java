package au.com.guidebee.morsetoolkit.activity.mario.actors.enemies;

import com.guidebee.game.graphics.TextureRegion;

import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.fx.FallingDeadSprite;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioContext;
import au.com.guidebee.morsetoolkit.platformer.core.OscillatorClock;

/**
 * A turtle that bobs up and down in place, ported from
 * {@code Objects/FlyingTurtlePatrol.java}: never moves horizontally, and
 * oscillates vertically around a center point {@code patrolLengthTiles*32}px
 * below its spawn, using the shared {@link OscillatorClock#getSlowDistance()}
 * angle (same clock {@code FireBar}'s rings read, so every flying turtle and
 * fire-bar ring in a level stays in sync, matching the original's single
 * shared {@code SlowDistance} field).
 *
 * <p>Skips the original's odd extra {@code moveY(game.SlowDistance)} call
 * right after directly setting Y from the same angle - adding a raw radian
 * value (usually well under 1) as a pixel offset immediately after precisely
 * placing the sprite reads as leftover/dead code from the original, not
 * intended behavior, so this port keeps only the actual cosine placement.
 *
 * <p>Always spawns a plain (non-themed) {@link EnemyTurtle} when stomped,
 * matching the original's {@code Green} field, which is never set true
 * anywhere in the source - same "always the plain palette" behavior as
 * {@link EnemyTurtlePatrol}.
 */
public class FlyingTurtlePatrol extends Enemy {

    private static final int FRAME_WIDTH = 32;
    private static final int FRAME_HEIGHT = 48;
    private static final float AMPLITUDE_PX = 4 * 32f;
    private static final float ANIMATION_INTERVAL = 0.3f;

    private final float centerX;
    private final float centerY;
    private float animTimer;
    private boolean showingFirstFrame = true;

    public FlyingTurtlePatrol(float x, float y, int patrolLengthTiles) {
        super(MarioResourceManager.region("flying_turtle_patrol"), FRAME_WIDTH, FRAME_HEIGHT, x, y, false);
        centerX = x;
        centerY = y + 32 * patrolLengthTiles;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!isActive()) {
            return;
        }
        setX(centerX);
        setY(centerY + (float) Math.cos(OscillatorClock.getSlowDistance()) * AMPLITUDE_PX);

        animTimer += delta;
        if (animTimer >= ANIMATION_INTERVAL) {
            animTimer = 0;
            showingFirstFrame = !showingFirstFrame;
        }
        setFrame(showingFirstFrame ? 0 : 1);
    }

    @Override
    public void onStomped(Player player) {
        EnemyTurtle turtle = new EnemyTurtle(getX(), getY(), "Ground");
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

    /** Ported from {@code Objects/FlyingTurtlePatrol.java}'s own {@code KilledByFireBall} - always the red shell, horizontally flipped, matching the original's own fixed {@code "TurtelShellRed"} (unlike {@link FlyingTurtle}, this type has no color variants). */
    @Override
    public void onDefeatedByProjectile() {
        MarioResourceManager.sound("smb_kick").play();
        TextureRegion shell = new TextureRegion(MarioResourceManager.region("turtle_shell_red"));
        shell.flip(true, false);
        FallingDeadSprite.spawn(getX(), getY(), shell);
        deactivate();
    }
}

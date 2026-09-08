package au.com.guidebee.morsetoolkit.activity.mario.actors.enemies;

import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.fx.FallingDeadSprite;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioContext;
import au.com.guidebee.morsetoolkit.platformer.core.TileMovement;

/**
 * A turtle bounded to a patrol strip, ported from
 * {@code Objects/EnemyTurtlePatrol.java}: walks between its spawn x and
 * {@code spawnX + 32*patrolLengthTiles}, turning around at either bound
 * (not wall-bounce like {@link Enemy#walkAndFall} - a fixed x range instead),
 * falling under a constant (non-ramping) gravity.
 *
 * <p>Always the plain green palette regardless of level theme, even when
 * stomped - ported faithfully from the original, which always passes the
 * literal string {@code "normal"} to the shell it spawns rather than the
 * level's actual attribute (World 1's own {@code Level_12} places one in an
 * {@code UnderGround} level, so this is a real, if odd, original behavior,
 * not a porting slip - see {@link #onStomped}). Single palette full stop:
 * unlike {@link EnemyTurtle}, no themed image variant exists for this type.
 */
public class EnemyTurtlePatrol extends Enemy {

    private static final float GRAVITY = 3f;
    private static final float WALK_SPEED = 1f;
    private static final float ANIMATION_INTERVAL = 0.3f;

    private final float leftBoundX;
    private final float rightBoundX;
    private final int tileSize;
    private float animTimer;
    private boolean showingFirstFrame = true;

    public EnemyTurtlePatrol(float x, float y, int patrolLengthTiles, int tileSize) {
        super(MarioResourceManager.region("enemy_turtle_patrol"), tileSize, (tileSize * 3) / 2, x, y, false);
        leftBoundX = x;
        rightBoundX = x + tileSize * patrolLengthTiles;
        this.tileSize = tileSize;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!isActive()) {
            return;
        }
        float frames = delta * 60f;
        TileMovement.moveY(this, GRAVITY * frames, MarioContext.world());

        if (getX() < leftBoundX) {
            movingRight = true;
        } else if (getX() > rightBoundX) {
            movingRight = false;
        }
        setX(getX() + (movingRight ? WALK_SPEED : -WALK_SPEED) * frames);

        animTimer += delta;
        if (animTimer >= ANIMATION_INTERVAL) {
            animTimer = 0;
            showingFirstFrame = !showingFirstFrame;
        }
        setFrame((movingRight ? 2 : 0) + (showingFirstFrame ? 0 : 1));
    }

    @Override
    public void onStomped(Player player) {
        TurtleShell shell = new TurtleShell(getX(), getY() + tileSize / 2f, "Ground", movingRight, tileSize);
        MarioContext.world().addEnemy(shell);
        MarioContext.spawn(shell);
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
        FallingDeadSprite.spawn(getX(), getY(),
                MarioResourceManager.region("enemy_turtle_patrol").split(tileSize, (tileSize * 3) / 2)[0][0]);
        deactivate();
    }
}

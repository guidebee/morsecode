package au.com.guidebee.morsetoolkit.activity.mario.actors.enemies;

import com.guidebee.game.graphics.TextureRegion;

import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.fx.FallingDeadSprite;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioContext;

/**
 * The walking (non-patrol) turtle enemy, ported from
 * {@code Objects/EnemyTurtle.java}. Walks at a constant pace, falls under
 * gravity, turns around at walls. Side-touch hurts the player like any other
 * enemy (the base {@link Enemy} default - the original's
 * {@code CollidedWithMarioFromTOLeft/TORight} just call {@code Decerease()});
 * only a *stomp* turns it into a stationary {@link TurtleShell} in place -
 * it's the shell's own later side-touch that "kicks" it into motion, not
 * this class.
 *
 * <p>The "turtle"/"turtle_dark" regions are 128x48 (4 frames of 32x48, taller
 * than a tile) - not 32x32 like most other sprites, confirmed by checking
 * the source PNG directly rather than assuming.
 *
 * <p>Green ("turtle" region) vs dark ("turtle_dark") is chosen by attribute,
 * matching {@code Mario.java}'s only two cases it actually spawns
 * ({@code UnderGround} -> dark, everything else -> green) - Sea's separate
 * dark variant and the flying/patrol turtle types aren't in World 1's data.
 */
public class EnemyTurtle extends Enemy {

    private static final float GRAVITY = 6f;
    private static final float WALK_SPEED = 1f;
    private static final float ANIMATION_INTERVAL = 0.3f;

    private final String attribute;
    private final int tileSize;
    private float animTimer;
    private boolean showingFirstFrame = true;

    public EnemyTurtle(float x, float y, String attribute, int tileSize) {
        super(regionFor(attribute), tileSize, (tileSize * 3) / 2, x, y, false);
        this.attribute = attribute;
        this.tileSize = tileSize;
        setFrame(movingRight ? 2 : 0);
    }

    private static TextureRegion regionFor(String attribute) {
        return MarioResourceManager.region("UnderGround".equals(attribute) ? "turtle_dark" : "turtle");
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
    public void onStomped(Player player) {
        TurtleShell shell = new TurtleShell(getX(), getY(), attribute, movingRight, tileSize);
        MarioContext.world().addEnemy(shell);
        MarioContext.spawn(shell);
        deactivate();
    }

    /** Ported from {@code Collusion/EnemyToEnemy.java}'s own {@code case 102}. */
    @Override
    public boolean bouncesOffEnemies() {
        return true;
    }

    /** Ported from {@code Objects/EnemyTurtle.java}'s own {@code KilledByFireBall} - see {@code FallingDeadSprite}'s class doc. */
    @Override
    public void onDefeatedByProjectile() {
        MarioResourceManager.sound("smb_kick").play();
        FallingDeadSprite.spawn(getX(), getY(), regionFor(attribute).split(tileSize, (tileSize * 3) / 2)[0][0]);
        deactivate();
    }
}

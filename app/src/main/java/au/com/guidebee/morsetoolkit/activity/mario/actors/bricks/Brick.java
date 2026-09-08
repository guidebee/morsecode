package au.com.guidebee.morsetoolkit.activity.mario.actors.bricks;

import com.guidebee.game.graphics.TextureRegion;

import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.PlayerPowerState;
import au.com.guidebee.morsetoolkit.activity.mario.fx.BrickFragment;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioContext;

/**
 * A plain breakable brick, ported from {@code Bricks/Brick.java}. Big/fire
 * Mario breaks it into 4 physics-driven fragments (see {@link BrickFragment});
 * small Mario just bonks it - solid, and (see {@link #act}) the tile hops up
 * a few pixels and settles back, with a "smb_bump" sound, ported from the
 * original's own {@code HitFromDown()} (its {@code Gravity}/{@code Jump}
 * fields drive the exact same kind of bounce via its own {@code update()} -
 * this port uses a plain time-based parabola instead of replicating that
 * tick-by-tick gravity/bitwise-NOT dance, which reads as an original-engine
 * oddity rather than an intentional shape worth preserving verbatim).
 */
public class Brick extends InteractiveBrick {

    private static final float PHYSICS_FPS = 60f;
    /** ~9 original ticks - matches {@code Bricks/Brick.java}'s own {@code Gravity} ramp from -5 back up past +4. */
    private static final float BUMP_DURATION_TICKS = 9f;
    private static final float BUMP_PEAK_OFFSET_PX = 10f;

    private final String attribute;
    private final float restY;
    /** &gt;= 0 while the bump animation plays, in original-tick units; negative means "not bumping". */
    private float bumpTicks = -1;

    public Brick(float x, float y, String attribute) {
        this(x, y, MarioResourceManager.themedRegion("brick", attribute), attribute);
    }

    /**
     * The original's {@code Brick} constructor actually took an explicit
     * image, not an attribute (World 1's plain bricks just always passed a
     * themed one) - Level 14's bridge reuses the exact same class with the
     * "bridge_blocks" skin instead (see {@code Mario.java}'s case 36, "//
     * BridgeBloks"). {@code fragmentAttribute} still themes the break
     * fragments (see {@link BrickFragment#spawnBreak}) since the bridge
     * breaks into the castle's own brick debris, not bridge-colored debris.
     */
    public Brick(float x, float y, TextureRegion region, String fragmentAttribute) {
        super(region, x, y);
        this.attribute = fragmentAttribute;
        this.restY = y;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (bumpTicks < 0) {
            return;
        }
        bumpTicks += delta * PHYSICS_FPS;
        if (bumpTicks >= BUMP_DURATION_TICKS) {
            bumpTicks = -1;
            setY(restY);
        } else {
            float t = bumpTicks / BUMP_DURATION_TICKS;
            setY(restY - BUMP_PEAK_OFFSET_PX * 4f * t * (1f - t));
        }
    }

    @Override
    public void hitFromBelow(Player player) {
        if (player.getPowerState() != PlayerPowerState.SMALL) {
            BrickFragment.spawnBreak(getX(), getY(), attribute);
            // Ported from the original's own HitFromDown(): a brief invisible
            // solid stand-in left behind for ~10 ticks so anything standing
            // exactly on top the instant this brick breaks doesn't fall
            // through a frame early - see TemporaryInvisibleBrick's own doc.
            TemporaryInvisibleBrick.spawnAt(getX(), getY(), MarioContext.world().tileSize());
            deactivate();
        } else {
            MarioResourceManager.sound("smb_bump").play();
            bumpTicks = 0;
        }
    }
}

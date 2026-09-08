package au.com.guidebee.morsetoolkit.activity.mario.actors.bricks;

import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.fx.CoinPopEffect;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioContext;

/**
 * A multi-hit coin brick, ported from {@code Bricks/Bank.java}: dispenses a
 * coin (with a bump hop, like {@link Brick}'s own bonk reaction) on every hit
 * from below for ~100 original ticks (~1.67s) starting from the *first* hit,
 * then turns into a permanent {@link Iron} block on the next hit past that
 * window - a real-time countdown, not a 100-hit budget. Confirmed by reading
 * the source: {@code ActiveCoins} decrements once per game tick in {@code
 * update()}, gated on {@code active} (set true by the first hit), not once
 * per hit - an earlier version of this class ported it as a 100-hit counter
 * instead, effectively unlimited in practice since no player hits a block
 * 100 times, caught by a later audit pass re-reading the source.
 */
public class Bank extends InteractiveBrick {

    private static final float PHYSICS_FPS = 60f;
    /** Ported from the original's own literal {@code ActiveCoins = 100}. */
    private static final float ACTIVE_TICKS = 100f;
    /** ~9 original ticks - matches {@link Brick}'s own bump-parabola duration (this class's own hit reaction is the same {@code Bounce()} call in the original). */
    private static final float BUMP_DURATION_TICKS = 9f;
    private static final float BUMP_PEAK_OFFSET_PX = 10f;

    private final String attribute;
    private final float restY;
    private final int tileSize;
    private boolean active;
    private float ticksLeft = ACTIVE_TICKS;
    /** &gt;= 0 while the bump animation plays, in original-tick units; negative means "not bumping". */
    private float bumpTicks = -1;

    public Bank(float x, float y, String attribute, int tileSize) {
        super(MarioResourceManager.themedRegion("brick", attribute), x, y);
        this.attribute = attribute;
        this.restY = y;
        this.tileSize = tileSize;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (active) {
            ticksLeft -= delta * PHYSICS_FPS;
        }
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
        if (active && ticksLeft < 0) {
            Iron iron = new Iron(getX(), getY(), attribute, tileSize);
            MarioContext.world().addBrick(iron);
            MarioContext.spawn(iron);
            deactivate();
            return;
        }
        active = true;
        MarioContext.spawn(new CoinPopEffect(getX(), getY(), tileSize));
        bumpTicks = 0;
    }
}

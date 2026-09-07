package au.com.guidebee.morsetoolkit.activity.mario.actors.enemies;

import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioContext;

/**
 * A Piranha Plant, ported from {@code Objects/plant.java}: bobs up out of its
 * pipe and back down along a fixed 96px (3-tile) travel range, pausing
 * retracted at the bottom of that range while Mario stands within 100px of it
 * - ported verbatim including its one quirk: {@code CanStopMovingUp} is only
 * ever checked while still in the bottom 32px of the range, so once it
 * commits to rising (because Mario was far enough away at that moment) it
 * keeps rising to the very top even if Mario then walks back into range
 * mid-ascent, only reconsidering once it's fully retracted again.
 *
 * <p>Hurts Mario on any touch - landing on top included, {@link #onStomped}
 * delegates to {@link #onTouchedSide} exactly like {@link Spikey}'s own
 * "can't be safely stomped" override - unless Mario has a star, in which case
 * the touch kills it instead; both cases are {@link Enemy}'s own defaults, so
 * neither is overridden here. Dies silently to a fireball or a shell (moving
 * or not, both already routed through {@link Enemy#onDefeatedByProjectile}
 * the same as every other enemy - see {@code TurtleShell}'s own
 * {@code killOverlappingEnemies}/{@code ProjectileCollisionResolver}) - just
 * an {@code smb_kick} and gone, no falling-dead sprite, unlike every
 * ground-walker P2.9.2 gave one to: confirmed by reading the source,
 * {@code KilledByFireBall()} has no such animation either, a deliberate
 * original difference here, not an oversight.
 *
 * <p>Spawned by {@code LevelLoader#spawnEnemies} from the top cell of every
 * "pump"/"PumpWarp" tile, except levels named {@code "OrangePump"} (World
 * 4-2's Clowd bonus area) - matching the original's own exclusion in
 * {@code Mario.java}'s case-12 block.
 */
public class PiranhaPlant extends Enemy {

    private static final int FRAME_WIDTH = 32;
    private static final int FRAME_HEIGHT = 48;
    private static final float ANIMATION_INTERVAL = 0.3f;

    /** Ported from {@code plant}'s own {@code Upheight = y - 96}. */
    private static final float TRAVEL_PX = 96f;
    /** Ported from the original's own literal {@code moveY(+-0.5)}. */
    private static final float MOVE_SPEED = 0.5f;
    /** Ported from {@code MarioIsNear}'s own {@code +-100} window. */
    private static final float NEAR_RANGE_PX = 100f;
    /** Ported from {@code CanStopMovingUp}'s own {@code DownHeight - 32} threshold. */
    private static final float RETRACTED_ZONE_PX = 32f;

    private final float upY;
    private final float downY;
    private boolean movingUp = true;
    private float animTimer;
    private boolean showingFirstFrame = true;

    public PiranhaPlant(float x, float y, String regionName) {
        super(MarioResourceManager.region(regionName), FRAME_WIDTH, FRAME_HEIGHT, x, y, true);
        downY = y;
        upY = y - TRAVEL_PX;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!isActive()) {
            return;
        }
        float frames = delta * PHYSICS_FPS;

        if (getY() < upY) {
            movingUp = false;
        }
        if (getY() > downY) {
            movingUp = true;
        }

        if (movingUp) {
            boolean marioNear = Math.abs(MarioContext.player().getX() - getX()) < NEAR_RANGE_PX;
            boolean retracted = getY() > downY - RETRACTED_ZONE_PX;
            if (!(marioNear && retracted)) {
                setY(getY() - MOVE_SPEED * frames);
            }
        } else {
            setY(getY() + MOVE_SPEED * frames);
        }

        animTimer += delta;
        if (animTimer >= ANIMATION_INTERVAL) {
            animTimer = 0;
            showingFirstFrame = !showingFirstFrame;
            setFrame(showingFirstFrame ? 0 : 1);
        }
    }

    /** Never a real stomp - see the class doc. */
    @Override
    public void onStomped(Player player) {
        onTouchedSide(player);
    }

    @Override
    public void onDefeatedByProjectile() {
        MarioResourceManager.sound("smb_kick").play();
        deactivate();
    }
}

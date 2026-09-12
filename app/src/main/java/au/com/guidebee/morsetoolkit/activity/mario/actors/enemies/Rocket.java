package au.com.guidebee.morsetoolkit.activity.mario.actors.enemies;

import com.guidebee.game.graphics.TextureRegion;

import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.fx.DirectFallingSprite;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioContext;

/**
 * A rocket fired by a {@code RocketLauncher}, ported from
 * {@code Objects/Rocket.java}: flies straight and level at a constant speed
 * toward whichever direction it was launched, no gravity, no tile collision
 * (matches the original - it only ever disappears by flying far enough off
 * either side of the level). A stomp sends it falling straight down in place
 * (see {@link #onStomped}, ported from {@code MarioJumpedOnEnemy()}, not the
 * {@link Enemy} default); a side-touch hurts Mario unless starred (the
 * default, not overridden); immune to fireballs (the original's own {@code
 * KilledByFireBall()} is empty), so {@link #onDefeatedByProjectile} is
 * overridden to a no-op.
 *
 * <p>Reuses frame index 3 of the "rocket_launcher" (or, on a CloudsNight
 * level - only ever reachable via {@code SpawnController}'s ambient
 * "Bombs" spawner, never a placed {@code RocketLauncher} turret, see
 * {@code Mario.java}'s own case 19 vs. its ambient-bomb block) "bw_rocket_launcher")
 * region for its own sprite - not a separate asset - matching the original's
 * own {@code bsLoader.getStoredImages("RocketLauncher"/"BWRocketLauncher")[3]}.
 * Mirrored for a rightward launch, matching the original's own {@code
 * ImageUtil.flipHorizontal} - see the constructor's own doc for why that's a
 * {@link #setTransform} rather than a pre-flipped region.
 */
public class Rocket extends Enemy {

    private static final float SPEED = 3f;
    private static final float FALL_OUT_MARGIN_TILES = 20f;

    private final int tileSize;
    private final boolean blackAndWhite;

    public Rocket(float x, float y, boolean movingRight, int tileSize) {
        this(x, y, movingRight, false, tileSize);
    }

    public Rocket(float x, float y, boolean movingRight, boolean blackAndWhite, int tileSize) {
        super(regionFor(blackAndWhite, tileSize), tileSize, tileSize, x, y, movingRight);
        this.tileSize = tileSize;
        this.blackAndWhite = blackAndWhite;
        // The "rocket_launcher" strip's frame 3 faces left as drawn. Flipping
        // the region itself before handing it to Sprite's constructor (as
        // this used to do) doesn't work: Sprite's constructor immediately
        // re-slices it via TextureRegion.split(), which - per that method's
        // own doc - always rebuilds sub-regions from raw pixel coordinates
        // and silently discards any flip already applied to the region being
        // split. That left every rightward-fired rocket rendering with the
        // original left-facing art no matter what. Flipping via the Sprite's
        // own mirror transform instead applies at draw time, after slicing,
        // so it actually takes effect.
        if (movingRight) {
            setTransform(TRANS_MIRROR);
        }
    }

    private static TextureRegion regionFor(boolean blackAndWhite, int tileSize) {
        return MarioResourceManager.region(blackAndWhite ? "bw_rocket_launcher" : "rocket_launcher")
                .split(tileSize, tileSize)[3][0];
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!isActive()) {
            return;
        }
        float frames = delta * PHYSICS_FPS;
        setX(getX() + (movingRight ? SPEED : -SPEED) * frames);

        float marginPx = FALL_OUT_MARGIN_TILES * tileSize;
        if (getX() < -marginPx || getX() > MarioContext.world().getWidthPx() + marginPx) {
            deactivate();
        }
    }

    /**
     * Ported from {@code MarioJumpedOnEnemy()}: unlike the {@link Enemy}
     * default (a plain {@link #deactivate}), a stomped rocket doesn't just
     * vanish - it spawns a {@link DirectFallingSprite} at its current
     * position/facing (the original's own {@code new DirectFalling(
     * this.getImage(), this.getX(), this.getY())}, image already carrying
     * whichever way it happened to be flying) and falls straight down out of
     * view before disappearing.
     */
    @Override
    public boolean onStomped(Player player) {
        MarioResourceManager.sound("smb_kick").play();
        DirectFallingSprite.spawn(getX(), getY(), regionFor(blackAndWhite, tileSize), movingRight);
        deactivate();
        return true;
    }

    /**
     * Immune to fireballs, matching the original's empty {@code
     * KilledByFireBall()}. Note a real behavioral gap left as-is here: the
     * original's *separate* {@code CollidedWithMovingShell()} (a kicked shell
     * hitting this rocket) does kill it, with a "smb_kick" and a
     * rightward-drifting {@code FallingDeadSprite} - this port's single
     * {@code onDefeatedByProjectile} hook can't yet tell "hit by a fireball"
     * apart from "hit by a moving shell" the way the original's two methods
     * could, so it's kept a no-op for both rather than breaking the
     * (more central) fireball immunity to add the (rarer) shell-kill case -
     * same tradeoff noted on {@code HelmetShell}'s own equivalent gap, see
     * docs/MARIO_PORT_PLAN_PHASE2.md S7.
     */
    @Override
    public void onDefeatedByProjectile() {
    }
}

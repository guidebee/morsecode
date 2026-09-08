package au.com.guidebee.morsetoolkit.activity.mario.actors.enemies;

import au.com.guidebee.morsetoolkit.activity.mario.MarioConfiguration;
import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.fx.FallingDeadSprite;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioContext;
import au.com.guidebee.morsetoolkit.platformer.core.TileMovement;

/**
 * A falling egg that hatches into a {@link Spikey} on landing - ported from
 * {@code Objects/SpikeyEgg.java}. Thrown by {@link SonOfABuitch}; starts
 * moving slightly *up* ({@code Gravity} starts at -6) before gravity ramps
 * it back down into an arc, matching the original exactly.
 *
 * <p>The original's "landed" signal is {@code setYloc(double)}, an
 * externally-called correction hook this port's architecture has no
 * equivalent for (see {@code Monkey}'s own class doc on the same point) -
 * here, landing is instead detected directly from
 * {@link TileMovement#moveY}'s own "blocked while falling" return, which is
 * exactly the same event.
 *
 * <p>The "spikey_egg" region is 32x32 per frame, 2 frames (64x32 total) -
 * confirmed against the source PNG directly.
 */
public class SpikeyEgg extends Enemy {

    private static final float GRAVITY_STEP = 0.25f;
    private static final float GRAVITY_CAP = 5f;
    private static final float ANIMATION_INTERVAL = 0.2f;

    private final int tileSize;
    private float gravity = -6f;
    private float animTimer;
    private boolean showingFirstFrame = true;

    public SpikeyEgg(float x, float y, boolean movingRight, int tileSize) {
        super(MarioResourceManager.region("spikey_egg"), tileSize, tileSize, x, y, movingRight);
        this.tileSize = tileSize;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!isActive()) {
            return;
        }
        float frames = delta * PHYSICS_FPS;
        if (gravity < GRAVITY_CAP) {
            gravity = Math.min(GRAVITY_CAP, gravity + GRAVITY_STEP * frames);
        }
        boolean landed = TileMovement.moveY(this, gravity * frames, MarioContext.world()) && gravity >= 0;
        if (landed) {
            hatch();
            return;
        }

        animTimer += delta;
        if (animTimer >= ANIMATION_INTERVAL) {
            animTimer = 0;
            showingFirstFrame = !showingFirstFrame;
        }
        setFrame(showingFirstFrame ? 0 : 1);
    }

    private void hatch() {
        Spikey spikey = new Spikey(getX(), getY(), movingRight, tileSize);
        MarioContext.world().addEnemy(spikey);
        MarioContext.spawn(spikey);
        deactivate();
    }

    @Override
    public void onStomped(Player player) {
        onTouchedSide(player);
    }

    @Override
    public void onDefeatedByProjectile() {
        MarioResourceManager.sound("smb_kick").play();
        FallingDeadSprite.spawn(getX(), getY(), MarioResourceManager.region("spikey_egg")
                .split(tileSize * MarioConfiguration.ART_SCALE, tileSize * MarioConfiguration.ART_SCALE)[0][0]);
        deactivate();
    }
}

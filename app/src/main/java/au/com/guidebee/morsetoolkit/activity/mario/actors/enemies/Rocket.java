package au.com.guidebee.morsetoolkit.activity.mario.actors.enemies;

import com.guidebee.game.graphics.TextureRegion;

import au.com.guidebee.morsetoolkit.activity.mario.MarioConfiguration;
import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioContext;

/**
 * A rocket fired by a {@code RocketLauncher}, ported from
 * {@code Objects/Rocket.java}: flies straight and level at a constant speed
 * toward whichever direction it was launched, no gravity, no tile collision
 * (matches the original - it only ever disappears by flying far enough off
 * either side of the level). A stomp always destroys it outright (matches
 * {@link Enemy}'s own default {@link #onStomped}, not overridden); a
 * side-touch hurts Mario unless starred (also the default, not overridden);
 * immune to fireballs (the original's own {@code KilledByFireBall()} is
 * empty), so {@link #onDefeatedByProjectile} is overridden to a no-op.
 *
 * <p>Reuses frame index 3 of the "rocket_launcher" region for its own
 * sprite - not a separate asset - matching the original's own
 * {@code bsLoader.getStoredImages("RocketLauncher")[3]}. Flipped
 * horizontally for a rightward launch, matching the original's own
 * {@code ImageUtil.flipHorizontal}.
 */
public class Rocket extends Enemy {

    private static final int FRAME_SIZE = 32;
    private static final float SPEED = 3f;
    private static final float FALL_OUT_MARGIN_TILES = 20f;

    public Rocket(float x, float y, boolean movingRight) {
        super(regionFor(movingRight), FRAME_SIZE, FRAME_SIZE, x, y, movingRight);
    }

    private static TextureRegion regionFor(boolean movingRight) {
        TextureRegion frame = MarioResourceManager.region("rocket_launcher")
                .split(FRAME_SIZE, FRAME_SIZE)[3][0];
        if (movingRight) {
            TextureRegion flipped = new TextureRegion(frame);
            flipped.flip(true, false);
            return flipped;
        }
        return frame;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!isActive()) {
            return;
        }
        float frames = delta * PHYSICS_FPS;
        setX(getX() + (movingRight ? SPEED : -SPEED) * frames);

        float marginPx = FALL_OUT_MARGIN_TILES * MarioConfiguration.TILE_SIZE;
        if (getX() < -marginPx || getX() > MarioContext.world().getWidthPx() + marginPx) {
            deactivate();
        }
    }

    @Override
    public void onStomped(Player player) {
        MarioResourceManager.sound("smb_kick").play();
        deactivate();
    }

    @Override
    public void onDefeatedByProjectile() {
        // Immune to fireballs - matches the original's empty KilledByFireBall().
    }
}

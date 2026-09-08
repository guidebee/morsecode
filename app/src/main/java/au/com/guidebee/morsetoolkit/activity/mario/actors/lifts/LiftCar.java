package au.com.guidebee.morsetoolkit.activity.mario.actors.lifts;

import com.guidebee.game.graphics.Batch;
import com.guidebee.game.graphics.TextureRegion;
import com.guidebee.game.microedition.Layer;

import au.com.guidebee.morsetoolkit.activity.mario.MarioConfiguration;
import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioContext;

/**
 * A one-shot horizontal conveyor, ported from {@code Lifts/LiftCar.java}:
 * sits still until a rider is caught standing on it, then slides right
 * forever at a constant speed (never resets or stops) - the same "trigger
 * once, then commit" shape as {@link LiftFall}, just sideways instead of
 * down. Used by the 4 pure-climb "Clowd" beanstalk levels (92/93/95/96) to
 * carry Mario up their vertical shafts as he lands on each successive car.
 *
 * <p>Ported from the original's own 4-arg constructor
 * ({@code LiftCar(x, y, game, Points)}, the only one any level actually
 * calls per {@code BasicLevel.AddLiftCar}): {@code setLocation(x+24, y)} -
 * the +24 shift is preserved verbatim rather than "centered" differently,
 * and {@code Points} (always 6 - the same width as {@link
 * BalanceLiftPlatform}'s own platform) is this port's own {@code
 * widthTiles} parameter. Despawn margin is re-derived against the level's
 * own width rather than the original's fixed {@code x > 32*100} (a
 * WorldLength-agnostic constant that only worked because every level using
 * it happened to be shorter than that) - same reasoning as
 * {@link LiftFall}'s own fall-out margin.
 */
public class LiftCar extends Layer implements LiftSurface {

    private static final float SPEED = 2f;
    private static final float PHYSICS_FPS = 60f;
    private static final float DESPAWN_MARGIN_PX = 200f;

    private final TextureRegion region;
    private final float landingTolerance;
    private boolean triggered;
    private float deltaX;

    public LiftCar(float x, float y, int widthTiles, int tileSize) {
        super(x + 24, y, Math.max(1, widthTiles) * tileSize, tileSize / 2f, true);
        region = MarioResourceManager.region("lift");
        landingTolerance = tileSize / 2f;
    }

    @Override
    public void onRidden() {
        triggered = true;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!triggered) {
            deltaX = 0;
            return;
        }
        deltaX = SPEED * delta * PHYSICS_FPS;
        setX(getX() + deltaX);
        if (getX() > MarioContext.world().getWidthPx() + DESPAWN_MARGIN_PX) {
            remove();
        }
    }

    @Override
    public void paint(Batch g) {
        // See Lift#paint's matching note on why this divides by ART_SCALE.
        int nativeWidth = region.getRegionWidth() / MarioConfiguration.ART_SCALE;
        float height = getHeight();
        for (float drawn = 0; drawn < getWidth(); drawn += nativeWidth) {
            g.draw(region, getX() + drawn, getY(), nativeWidth, height);
        }
    }

    @Override
    public float getDeltaX() {
        return deltaX;
    }

    @Override
    public float getTopY() {
        return getY();
    }

    @Override
    public boolean isLandingSpot(float x, float y, int width, int height) {
        boolean overlapsHorizontally = x < getX() + getWidth() && x + width > getX();
        if (!overlapsHorizontally) {
            return false;
        }
        float top = getTopY();
        float bottom = y + height;
        return bottom >= top - landingTolerance && bottom <= top + landingTolerance;
    }
}

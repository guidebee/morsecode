package au.com.guidebee.morsetoolkit.activity.mario.actors.lifts;

import com.guidebee.game.graphics.Batch;
import com.guidebee.game.graphics.TextureRegion;
import com.guidebee.game.microedition.Layer;

import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioContext;

/**
 * A collapsing platform, ported from {@code Lifts/LiftFall.java}: sits still
 * until a rider is caught standing on it, then falls away at a constant
 * speed forever (never resets), removed once it's fallen well past the
 * level's bottom.
 */
public class LiftFall extends Layer implements LiftSurface {

    private static final float FALL_SPEED = 3f;
    private static final float PHYSICS_FPS = 60f;
    private static final float FALL_OUT_MARGIN_PX = 200f;

    private final TextureRegion region;
    private final float landingTolerance;
    private boolean falling;

    public LiftFall(float x, float y, int widthTiles, int tileSize) {
        super(x, y, Math.max(1, widthTiles) * tileSize, tileSize / 2f, true);
        region = MarioResourceManager.region("lift");
        landingTolerance = tileSize / 2f;
    }

    @Override
    public void onRidden() {
        falling = true;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!falling) {
            return;
        }
        setY(getY() + FALL_SPEED * delta * PHYSICS_FPS);
        if (getY() > MarioContext.world().getHeightPx() + FALL_OUT_MARGIN_PX) {
            remove();
        }
    }

    @Override
    public void paint(Batch g) {
        int nativeWidth = region.getRegionWidth();
        float height = getHeight();
        for (float drawn = 0; drawn < getWidth(); drawn += nativeWidth) {
            g.draw(region, getX() + drawn, getY(), nativeWidth, height);
        }
    }

    @Override
    public float getDeltaX() {
        return 0f;
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

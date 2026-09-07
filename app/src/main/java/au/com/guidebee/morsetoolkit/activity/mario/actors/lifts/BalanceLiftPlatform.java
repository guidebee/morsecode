package au.com.guidebee.morsetoolkit.activity.mario.actors.lifts;

import com.guidebee.game.graphics.Batch;
import com.guidebee.game.graphics.TextureRegion;
import com.guidebee.game.microedition.Layer;

import au.com.guidebee.morsetoolkit.activity.mario.MarioConfiguration;
import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;

/**
 * One platform of a seesaw pair, ported from {@code Lifts/BalenceLiftParent.java}
 * + {@code Lifts/BalenceLiftChild.java}: standing on either side sinks it and
 * raises the other, proportionally, decaying back toward level once nobody's
 * riding either side.
 *
 * <p>Redesigned from the original's own mechanism rather than transcribed
 * line for line: the original gives *each* platform its own independent
 * {@code SpeedY} field, and has *each* one push the other by its own speed
 * every frame (so both sides nudge each other simultaneously, mostly
 * redundantly since only one side's speed is ever actually nonzero at a
 * time) - here, only the {@link #link linked} *primary* platform's
 * {@link #act} runs the shared physics (one {@link #speedY}, one signed
 * {@link #direction} recording which side is currently the "heavy" one) and
 * applies the result to both platforms; the secondary's own {@code act()} is
 * a no-op, since its position is fully driven by the primary within the same
 * frame. Same observable seesaw behavior, without the redundant dual state.
 *
 * <p>Skips the original's chain/hook/black-box background decoration
 * ({@code AddChain}/{@code AddBlackBoxes}/{@code AddHooksAndChain}) - purely
 * cosmetic scaffolding masking the platform's support visually, not
 * gameplay - and its {@code DirectFalling} debris animation when the pair
 * breaks (goes out of bounds): breaking just deactivates both platforms here,
 * matching this port's usual skip of minor original animation flourishes.
 */
public class BalanceLiftPlatform extends Layer implements LiftSurface {

    private static final float PHYSICS_FPS = 60f;
    private static final float MAX_Y = 3 * MarioConfiguration.TILE_SIZE;
    private static final float MIN_Y = 11 * MarioConfiguration.TILE_SIZE;
    private static final float ACCEL = 0.2f;
    private static final float MAX_SPEED = 15f;
    private static final float SPEED_DIVISOR = 12f;
    /** The original tiles its platform image 6x wide ({@code ImageUtil.TileImage(getStoredImage("Lift"), 6)}), not 1 tile. */
    private static final int WIDTH_TILES = 6;

    private final TextureRegion region;

    private BalanceLiftPlatform partner;
    private boolean primary;
    private boolean riddenThisFrame;
    private float speedY;
    /** +1 while this platform is the "heavy" (sinking-when-ridden) side, -1 while the partner is - only meaningful on the primary. */
    private int direction = 1;
    private boolean broken;

    public BalanceLiftPlatform(float x, float y) {
        super(x, y, WIDTH_TILES * MarioConfiguration.TILE_SIZE, MarioConfiguration.TILE_SIZE / 2f, true);
        region = MarioResourceManager.region("lift");
    }

    /** Links two platforms into a seesaw pair - call once, right after constructing both. */
    public static void link(BalanceLiftPlatform a, BalanceLiftPlatform b) {
        a.partner = b;
        b.partner = a;
        a.primary = true;
    }

    @Override
    public void onRidden() {
        riddenThisFrame = true;
    }

    private boolean consumeRidden() {
        boolean ridden = riddenThisFrame;
        riddenThisFrame = false;
        return ridden;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!primary || broken) {
            return;
        }
        float frames = delta * PHYSICS_FPS;

        boolean thisRidden = consumeRidden();
        boolean partnerRidden = partner.consumeRidden();
        if (thisRidden) {
            direction = 1;
        } else if (partnerRidden) {
            direction = -1;
        }

        if (thisRidden || partnerRidden) {
            speedY = Math.min(MAX_SPEED, speedY + ACCEL * frames);
        } else if (speedY > 0) {
            speedY = Math.max(0, speedY - ACCEL * frames);
        }

        float move = direction * speedY / SPEED_DIVISOR * frames;
        setY(getY() + move);
        partner.setY(partner.getY() - move);

        if (getY() < MAX_Y || getY() > MIN_Y || partner.getY() < MAX_Y || partner.getY() > MIN_Y) {
            broken = true;
            remove();
            partner.remove();
        }
    }

    /** Tiles the native (16x16) "lift" image across the platform's width, like {@link Lift#paint} - stretching a small source tile would blur it. */
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
        return bottom >= top - MarioConfiguration.TILE_SIZE / 2f && bottom <= top + MarioConfiguration.TILE_SIZE / 2f;
    }
}

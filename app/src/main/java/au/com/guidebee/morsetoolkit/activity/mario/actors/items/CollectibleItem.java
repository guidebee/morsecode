package au.com.guidebee.morsetoolkit.activity.mario.actors.items;

import com.guidebee.game.graphics.TextureRegion;
import com.guidebee.game.microedition.Sprite;

import au.com.guidebee.morsetoolkit.activity.mario.MarioConfiguration;

/** Shared AABB/active-flag bookkeeping for {@link Collectible}s. */
public abstract class CollectibleItem extends Sprite implements Collectible {

    private boolean active = true;

    /**
     * {@code frameWidth}/{@code frameHeight} are world-space - see
     * {@code Enemy}'s own constructor doc for why the {@link #setSize} call
     * right after {@code super(...)} is needed once
     * {@link MarioConfiguration#ART_SCALE} isn't 1.
     */
    protected CollectibleItem(TextureRegion region, int frameWidth, int frameHeight, float x, float y) {
        super(region, frameWidth * MarioConfiguration.ART_SCALE, frameHeight * MarioConfiguration.ART_SCALE);
        setSize(frameWidth, frameHeight);
        setPosition(x, y);
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public boolean overlaps(float x, float y, int width, int height) {
        return active
                && x < getX() + getWidth() && x + width > getX()
                && y < getY() + getHeight() && y + height > getY();
    }

    protected void collect() {
        active = false;
        remove();
    }
}

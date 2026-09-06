package au.com.guidebee.morsetoolkit.activity.mario.actors.items;

import com.guidebee.game.graphics.TextureRegion;
import com.guidebee.game.microedition.Sprite;

/** Shared AABB/active-flag bookkeeping for {@link Collectible}s. */
public abstract class CollectibleItem extends Sprite implements Collectible {

    private boolean active = true;

    protected CollectibleItem(TextureRegion region, int frameWidth, int frameHeight, float x, float y) {
        super(region, frameWidth, frameHeight);
        setPosition(x, y);
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public boolean overlaps(int x, int y, int width, int height) {
        return active
                && x < getX() + getWidth() && x + width > getX()
                && y < getY() + getHeight() && y + height > getY();
    }

    protected void collect() {
        active = false;
        remove();
    }
}

package au.com.guidebee.morsetoolkit.activity.mario.actors.bricks;

import com.guidebee.game.graphics.TextureRegion;
import com.guidebee.game.microedition.Sprite;

import au.com.guidebee.morsetoolkit.activity.mario.MarioConfiguration;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.platformer.core.SolidTile;

/**
 * Common base for the original engine's "BasicBrick"-implementing classes
 * (Brick, Bank, QuestionMark, BrickWithStar, InvisibleBrck, Iron, Pump).
 *
 * <p>Unlike Step 3's static terrain (brick/stone/chocolate baked into
 * {@code MarioWorld}'s {@code TiledLayer}), these are real {@code Sprite}
 * actors - see {@code MarioConfiguration}'s note on why: they need to react
 * to being hit (break, dispense an item, disappear) or, for {@code Pump},
 * because their art is wider than one tile and TiledLayer can't represent
 * that. {@code MarioWorld} registers them under {@link SolidTile} (see that
 * interface's own doc) so the generic {@code TileWorld}'s
 * {@code containsImpassableArea} folds them in without needing to know
 * they're bricks specifically.
 */
public abstract class InteractiveBrick extends Sprite implements SolidTile {

    private boolean active = true;

    /**
     * {@code region} is a single already-tile-sized atlas region (not a
     * multi-frame strip) - its own pixel dimensions are {@code ART_SCALE}
     * times the world size once new higher-res art lands, so the bounds
     * {@link Sprite}'s one-arg constructor derives from it need correcting
     * back down to world space.
     */
    protected InteractiveBrick(TextureRegion region, float x, float y) {
        super(region);
        setSize(getWidth() / MarioConfiguration.ART_SCALE, getHeight() / MarioConfiguration.ART_SCALE);
        setPosition(x, y);
    }

    /**
     * For a brick backed by a multi-frame strip region (e.g. {@link Iron}'s
     * themed variants). {@code frameWidth}/{@code frameHeight} are
     * world-space - see {@code Enemy}'s own constructor doc for why the
     * {@link #setSize} call right after {@code super(...)} is needed.
     */
    protected InteractiveBrick(TextureRegion region, int frameWidth, int frameHeight, float x, float y) {
        super(region, frameWidth * MarioConfiguration.ART_SCALE, frameHeight * MarioConfiguration.ART_SCALE);
        setSize(frameWidth, frameHeight);
        setPosition(x, y);
    }

    public boolean isActive() {
        return active;
    }

    protected void deactivate() {
        active = false;
        setVisible(false);
    }

    public boolean overlaps(float x, float y, int width, int height) {
        return active
                && x < getX() + getWidth() && x + width > getX()
                && y < getY() + getHeight() && y + height > getY();
    }

    /**
     * Called when the player's head hits this brick's underside while
     * jumping. Default: nothing (matches Stone/Pump/Iron's no-op
     * {@code HitFromDown()} in the original engine).
     */
    public void hitFromBelow(Player player) {
    }
}

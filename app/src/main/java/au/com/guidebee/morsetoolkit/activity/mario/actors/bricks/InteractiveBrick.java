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

    /**
     * Whether this brick blocks landing on top of it or walking into its
     * side - true for every brick except {@link InvisibleBrck} before it's
     * been triggered, which overrides this to false. Ported from {@code
     * Collusion/Player_Brick.collided}'s own {@code if (b.getID() != 17)}
     * guard wrapped around every directional collision branch *except* the
     * hit-from-below one - an invisible brick reacts to nothing but a jump
     * into its underside until that turns it into a real {@link Iron} block.
     * {@link SolidTile#isActive} alone can't express this: it's still {@code
     * true} the whole time (so the hit-from-below check, which shares the
     * same generic {@code containsImpassableArea}/{@code findActiveBrickAt}
     * machinery, can still find it), so the landing/horizontal call sites in
     * {@code Player}'s own movement code check this separately instead.
     */
    public boolean blocksLanding() {
        return true;
    }
}

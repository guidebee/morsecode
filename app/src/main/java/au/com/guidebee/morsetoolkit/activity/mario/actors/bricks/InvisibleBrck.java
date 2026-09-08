package au.com.guidebee.morsetoolkit.activity.mario.actors.bricks;

import com.guidebee.game.graphics.TextureRegion;
import com.guidebee.game.graphics.Texture;
import com.guidebee.game.graphics.Pixmap;

import au.com.guidebee.morsetoolkit.activity.mario.MarioConfiguration;
import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.actors.items.Life;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.fx.CoinPopEffect;
import au.com.guidebee.morsetoolkit.activity.mario.fx.ItemReveal;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioContext;

/**
 * A brick with no visible image until hit, ported from
 * {@code Bricks/InvisibleBrck.java}. World 1 only ever uses its "1UP"
 * variant (Level 11's {@code InvisibleBrckWith1Up}); the "CoinInside"
 * variant ({@code InvisibleBrckWithCoin}, used by Level 14) is supported too
 * since it costs nothing extra here. Turns into an {@link Iron} block after use.
 */
public class InvisibleBrck extends InteractiveBrick {

    private static final float RISE_SPEED_PX_PER_SEC = 60f;

    private final String attribute;
    private final String insideItem;
    private final int tileSize;

    private static TextureRegion blankRegion;

    public InvisibleBrck(float x, float y, String attribute, String insideItem, int tileSize) {
        super(blankRegion(tileSize), x, y);
        this.attribute = attribute;
        this.insideItem = insideItem;
        this.tileSize = tileSize;
        setVisible(false);
    }

    /**
     * Lazily-cached shared blank texture - one GPU texture for every invisible
     * brick, not one each. Generated at {@code tileSize * ART_SCALE} (not
     * bare {@code tileSize}) purely so it follows the same pixel-size
     * convention every real atlas region does - {@link InteractiveBrick}'s
     * single-region constructor divides by {@code ART_SCALE} unconditionally
     * to get back to world space, and a flat color is no less correct at
     * whatever resolution it's generated at.
     */
    private static TextureRegion blankRegion(int tileSize) {
        if (blankRegion == null) {
            int pixelSize = tileSize * MarioConfiguration.ART_SCALE;
            Pixmap pixmap = new Pixmap(pixelSize, pixelSize, Pixmap.Format.RGBA8888);
            blankRegion = new TextureRegion(new Texture(pixmap));
        }
        return blankRegion;
    }

    @Override
    public void hitFromBelow(Player player) {
        Iron iron = new Iron(getX(), getY(), attribute, tileSize);
        deactivate();

        if ("1UP".equals(insideItem)) {
            TextureRegion preview = MarioResourceManager.region("one_up")
                    .split(tileSize * MarioConfiguration.ART_SCALE, tileSize * MarioConfiguration.ART_SCALE)[0][0];
            ItemReveal reveal = new ItemReveal(preview, getX(), getY(), RISE_SPEED_PX_PER_SEC, (x, y) -> {
                Life life = new Life(x, y, tileSize);
                MarioContext.world().addCollectible(life);
                MarioContext.spawn(life);
            }, tileSize);
            // Ported from the original's own draw order (VolitileGroup -
            // where LifeAnim lives - added before BrickGroup) - see
            // QuestionMark's own matching note.
            MarioContext.spawn(reveal);
            MarioContext.world().addBrick(iron);
            MarioContext.spawn(iron);
        } else {
            // CoinAnim lives in the original's own AnimationGroup, added
            // after BrickGroup - draws in front of the block, so iron spawns
            // first here instead - see QuestionMark's own note.
            MarioContext.world().addBrick(iron);
            MarioContext.spawn(iron);
            MarioContext.spawn(new CoinPopEffect(getX(), getY(), tileSize));
        }
    }
}

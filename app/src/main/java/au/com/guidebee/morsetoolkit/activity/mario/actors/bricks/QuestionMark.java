package au.com.guidebee.morsetoolkit.activity.mario.actors.bricks;

import com.guidebee.game.graphics.TextureRegion;

import au.com.guidebee.morsetoolkit.activity.mario.MarioConfiguration;
import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.actors.items.Flower;
import au.com.guidebee.morsetoolkit.activity.mario.actors.items.Mushroom;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.PlayerPowerState;
import au.com.guidebee.morsetoolkit.activity.mario.fx.CoinPopEffect;
import au.com.guidebee.morsetoolkit.activity.mario.fx.ItemReveal;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioContext;

/**
 * The "?" block, ported from {@code Bricks/QuestionMark.java}: bobs through
 * a 3-frame idle loop, and on a hit from below reveals either a coin
 * ("CoinInside" - the original's plain QuestionMark) or a growth item
 * ("Mashroom" - the original's QuestionMarkWithMushroom, which reveals a
 * Mushroom for small Mario or a Flower for already-big Mario), then
 * permanently turns into an {@link Iron} block.
 *
 * <p>Swaps to the grey ("question_mark_grey") region on UnderGround/Castle
 * levels, matching the original's own {@code game.GetAttribute()} check -
 * every other attribute (Ground/Sea) keeps the normal yellow region.
 */
public class QuestionMark extends InteractiveBrick {

    private static final float ANIMATION_INTERVAL = 0.2f;
    private static final int[] IDLE_FRAMES = {0, 0, 1, 2, 1, 0};
    private static final float RISE_SPEED_PX_PER_SEC = 30f;

    private final String attribute;
    private final String insideItem;
    private float animTimer;

    public QuestionMark(float x, float y, String attribute, String insideItem) {
        super(MarioResourceManager.region(regionFor(attribute)),
                MarioConfiguration.TILE_SIZE, MarioConfiguration.TILE_SIZE, x, y);
        setFrameSequence(IDLE_FRAMES);
        this.attribute = attribute;
        this.insideItem = insideItem;
    }

    private static String regionFor(String attribute) {
        return "UnderGround".equals(attribute) || "Castle".equals(attribute)
                ? "question_mark_grey" : "question_mark";
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!isActive()) {
            return;
        }
        animTimer += delta;
        if (animTimer >= ANIMATION_INTERVAL) {
            animTimer = 0;
            nextFrame();
        }
    }

    @Override
    public void hitFromBelow(Player player) {
        Iron iron = new Iron(getX(), getY(), attribute);
        deactivate();

        if ("Mashroom".equals(insideItem)) {
            boolean big = player.getPowerState() != PlayerPowerState.SMALL;
            // Matches the original: the rising icon is the static singular
            // "Mashroom" image (not a frame off the walking "Mashrooms"
            // strip) for the growth case, or the flower strip's first frame
            // for the fire case (the original animates Flower's reveal too;
            // skipping that animation while rising is a cosmetic simplification).
            TextureRegion preview = big
                    ? MarioResourceManager.region("flower").split(MarioConfiguration.TILE_SIZE, MarioConfiguration.TILE_SIZE)[0][0]
                    : MarioResourceManager.region("mashroom");
            ItemReveal reveal = new ItemReveal(preview, getX(), getY(), RISE_SPEED_PX_PER_SEC, (x, y) -> {
                if (big) {
                    Flower flower = new Flower(x, y);
                    MarioContext.world().addCollectible(flower);
                    MarioContext.spawn(flower);
                } else {
                    Mushroom mushroom = new Mushroom(x, y);
                    MarioContext.world().addCollectible(mushroom);
                    MarioContext.spawn(mushroom);
                }
            });
            // Ported from the original's own draw order (VolitileGroup -
            // where MashroomAnim/FlowerAnim live - added to the playfield
            // *before* BrickGroup): the rising reveal renders behind the
            // block it's replacing while it's still emerging, not in front
            // of it - spawn it first.
            MarioContext.spawn(reveal);
            MarioContext.world().addBrick(iron);
            MarioContext.spawn(iron);
        } else {
            // CoinAnim lives in the original's own AnimationGroup, added
            // *after* BrickGroup - draws in front of the block, unlike the
            // growth-item reveal above, so iron spawns first here instead.
            MarioContext.world().addBrick(iron);
            MarioContext.spawn(iron);
            MarioContext.spawn(new CoinPopEffect(getX(), getY()));
        }
    }
}

package au.com.guidebee.morsetoolkit.activity.mario.actors.bricks;

import com.guidebee.game.graphics.TextureRegion;

import au.com.guidebee.morsetoolkit.activity.mario.MarioConfiguration;
import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.actors.items.Life;
import au.com.guidebee.morsetoolkit.activity.mario.actors.items.Mushroom;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.fx.CoinPopEffect;
import au.com.guidebee.morsetoolkit.activity.mario.fx.ItemReveal;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioContext;

/**
 * A one-hit reveal brick styled as a plain themed brick rather than a "?"
 * mark, ported from {@code Bricks/BankWithItem.java} (used by the original's
 * {@code BrickWithMushroom}/{@code BrickWith1UP}/{@code BrickWithCoin} tile
 * types - same reveal logic {@link QuestionMark} already has, just a
 * different static visual and no idle bob animation). Always reveals a
 * {@link Mushroom} for "Mashroom" (unlike {@code QuestionMark}, the original
 * never grows this into a Flower for already-big Mario - confirmed by
 * reading {@code Bricks/BankWithItem.java} directly, only one branch exists
 * there, not {@code QuestionMark}'s own big/small split).
 */
public class BankWithItem extends InteractiveBrick {

    private static final float RISE_SPEED_PX_PER_SEC = 30f;

    private final String attribute;
    private final String insideItem;

    public BankWithItem(float x, float y, String attribute, String insideItem) {
        super(MarioResourceManager.themedRegion("brick", attribute), x, y);
        this.attribute = attribute;
        this.insideItem = insideItem;
    }

    @Override
    public void hitFromBelow(Player player) {
        Iron iron = new Iron(getX(), getY(), attribute);
        MarioContext.world().addBrick(iron);
        MarioContext.spawn(iron);
        deactivate();

        switch (insideItem) {
            case "Mashroom": {
                TextureRegion preview = MarioResourceManager.region("mashroom");
                ItemReveal reveal = new ItemReveal(preview, getX(), getY(), RISE_SPEED_PX_PER_SEC, (x, y) -> {
                    Mushroom mushroom = new Mushroom(x, y);
                    MarioContext.world().addCollectible(mushroom);
                    MarioContext.spawn(mushroom);
                });
                MarioContext.spawn(reveal);
                break;
            }
            case "1UP": {
                TextureRegion preview = MarioResourceManager.region("one_up")
                        .split(MarioConfiguration.TILE_SIZE, MarioConfiguration.TILE_SIZE)[0][0];
                ItemReveal reveal = new ItemReveal(preview, getX(), getY(), RISE_SPEED_PX_PER_SEC, (x, y) -> {
                    Life life = new Life(x, y);
                    MarioContext.world().addCollectible(life);
                    MarioContext.spawn(life);
                });
                MarioContext.spawn(reveal);
                break;
            }
            default:
                MarioContext.spawn(new CoinPopEffect(getX(), getY()));
                break;
        }
    }
}

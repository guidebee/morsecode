package au.com.guidebee.morsetoolkit.activity.mario.actors.bricks;

import com.guidebee.game.graphics.TextureRegion;

import au.com.guidebee.morsetoolkit.activity.mario.MarioConfiguration;
import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.actors.items.Star;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.fx.ItemReveal;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioContext;

/**
 * A single-use brick that reveals an invincibility {@link Star}, ported from
 * {@code Bricks/BrickWithStar.java}. Turns into an {@link Iron} block after use.
 */
public class BrickWithStar extends InteractiveBrick {

    private static final float RISE_SPEED_PX_PER_SEC = 30f;

    private final String attribute;

    public BrickWithStar(float x, float y, String attribute) {
        super(MarioResourceManager.themedRegion("brick", attribute), x, y);
        this.attribute = attribute;
    }

    @Override
    public void hitFromBelow(Player player) {
        Iron iron = new Iron(getX(), getY(), attribute);
        MarioContext.world().addBrick(iron);
        MarioContext.spawn(iron);
        deactivate();

        TextureRegion preview = MarioResourceManager.region("star")
                .split(MarioConfiguration.TILE_SIZE, MarioConfiguration.TILE_SIZE)[0][0];
        ItemReveal reveal = new ItemReveal(preview, getX(), getY(), RISE_SPEED_PX_PER_SEC, (x, y) -> {
            Star star = new Star(x, y);
            MarioContext.world().addCollectible(star);
            MarioContext.spawn(star);
        });
        MarioContext.spawn(reveal);
    }
}

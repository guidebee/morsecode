package au.com.guidebee.morsetoolkit.activity.mario.actors.bricks;

import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.fx.CoinPopEffect;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioContext;

/**
 * A multi-hit coin brick, ported from {@code Bricks/Bank.java}: dispenses a
 * coin on every hit from below until its 100-coin supply runs out, then
 * turns into a permanent {@link Iron} block.
 */
public class Bank extends InteractiveBrick {

    private final String attribute;
    private int coinsRemaining = 100;

    public Bank(float x, float y, String attribute) {
        super(MarioResourceManager.themedRegion("brick", attribute), x, y);
        this.attribute = attribute;
    }

    @Override
    public void hitFromBelow(Player player) {
        if (coinsRemaining <= 0) {
            Iron iron = new Iron(getX(), getY(), attribute);
            MarioContext.world().addBrick(iron);
            MarioContext.spawn(iron);
            deactivate();
            return;
        }
        coinsRemaining--;
        CoinPopEffect pop = new CoinPopEffect(getX(), getY());
        MarioContext.spawn(pop);
    }
}

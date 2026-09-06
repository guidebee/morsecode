package au.com.guidebee.morsetoolkit.activity.mario.actors.bricks;

import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.PlayerPowerState;
import au.com.guidebee.morsetoolkit.activity.mario.fx.BrickFragment;

/**
 * A plain breakable brick, ported from {@code Bricks/Brick.java}. Big/fire
 * Mario breaks it into 4 physics-driven fragments (see {@link BrickFragment});
 * small Mario just bonks it - solid, nothing else happens. The original's
 * small-bonk "jitter" (the whole tile briefly jolting up/down) is a minor
 * cosmetic flourish and is skipped here.
 */
public class Brick extends InteractiveBrick {

    private final String attribute;

    public Brick(float x, float y, String attribute) {
        super(MarioResourceManager.themedRegion("brick", attribute), x, y);
        this.attribute = attribute;
    }

    @Override
    public void hitFromBelow(Player player) {
        if (player.getPowerState() != PlayerPowerState.SMALL) {
            BrickFragment.spawnBreak(getX(), getY(), attribute);
            deactivate();
        }
    }
}

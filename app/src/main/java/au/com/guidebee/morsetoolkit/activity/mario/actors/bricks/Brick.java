package au.com.guidebee.morsetoolkit.activity.mario.actors.bricks;

import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.PlayerPowerState;

/**
 * A plain breakable brick, ported from {@code Bricks/Brick.java}. Big/fire
 * Mario breaks it (removed permanently, no replacement); small Mario just
 * bonks it - solid, nothing else happens. The original's break-fragment
 * particles and small-bonk "jitter" animation are cosmetic and skipped here.
 */
public class Brick extends InteractiveBrick {

    public Brick(float x, float y, String attribute) {
        super(MarioResourceManager.themedRegion("brick", attribute), x, y);
    }

    @Override
    public void hitFromBelow(Player player) {
        if (player.getPowerState() != PlayerPowerState.SMALL) {
            deactivate();
        }
    }
}

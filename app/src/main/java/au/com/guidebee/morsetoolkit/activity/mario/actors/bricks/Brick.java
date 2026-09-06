package au.com.guidebee.morsetoolkit.activity.mario.actors.bricks;

import com.guidebee.game.graphics.TextureRegion;

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
        this(x, y, MarioResourceManager.themedRegion("brick", attribute), attribute);
    }

    /**
     * The original's {@code Brick} constructor actually took an explicit
     * image, not an attribute (World 1's plain bricks just always passed a
     * themed one) - Level 14's bridge reuses the exact same class with the
     * "bridge_blocks" skin instead (see {@code Mario.java}'s case 36, "//
     * BridgeBloks"). {@code fragmentAttribute} still themes the break
     * fragments (see {@link BrickFragment#spawnBreak}) since the bridge
     * breaks into the castle's own brick debris, not bridge-colored debris.
     */
    public Brick(float x, float y, TextureRegion region, String fragmentAttribute) {
        super(region, x, y);
        this.attribute = fragmentAttribute;
    }

    @Override
    public void hitFromBelow(Player player) {
        if (player.getPowerState() != PlayerPowerState.SMALL) {
            BrickFragment.spawnBreak(getX(), getY(), attribute);
            deactivate();
        }
    }
}

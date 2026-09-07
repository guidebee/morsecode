package au.com.guidebee.morsetoolkit.activity.mario.actors.bricks;

import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;

/**
 * A plain static bridge platform, ported from {@code Bricks/WoodenBridge.java}
 * - solid and indestructible, like {@link Iron} ({@code HitFromDown} is a
 * no-op in the original).
 */
public class WoodenBridge extends InteractiveBrick {

    public WoodenBridge(float x, float y) {
        super(MarioResourceManager.region("wooden_bridge"), x, y);
    }
}

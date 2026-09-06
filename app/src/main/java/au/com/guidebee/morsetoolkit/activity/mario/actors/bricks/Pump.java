package au.com.guidebee.morsetoolkit.activity.mario.actors.bricks;

import com.guidebee.game.graphics.TextureRegion;

import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;

/**
 * A pipe segment, ported from {@code Bricks/pump.java} - purely solid
 * decoration, no interaction. Its art (64px, 2 tiles wide) is why pipes
 * couldn't be static {@code TiledLayer} cells - see
 * {@code MarioConfiguration}'s note. World 1 never places a pipe on the
 * "Sea" attribute, so only the Ground/UnderGround (shared "pump"/"pump_top")
 * and Castle variants are needed, matching the original's
 * {@code Mario.java} switch.
 */
public class Pump extends InteractiveBrick {

    public Pump(float x, float y, String attribute, boolean top) {
        super(regionFor(attribute, top), x, y);
    }

    private static TextureRegion regionFor(String attribute, boolean top) {
        boolean castle = "Castle".equals(attribute);
        if (top) {
            return MarioResourceManager.region(castle ? "pump_top_castle" : "pump_top");
        }
        return MarioResourceManager.region(castle ? "pump_castle" : "pump");
    }
}

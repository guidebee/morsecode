package au.com.guidebee.morsetoolkit.activity.mario.actors.bricks;

import com.guidebee.game.graphics.TextureRegion;

import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;

/**
 * A pipe segment, ported from {@code Bricks/pump.java} - purely solid
 * decoration, no interaction. Its art (64px, 2 tiles wide) is why pipes
 * couldn't be static {@code TiledLayer} cells - see
 * {@code MarioConfiguration}'s note. Ground/UnderGround share plain "pump"/
 * "pump_top"; Castle and Sea (World 8's Level_844, its only Sea-attribute
 * pipe - confirmed by reading that level's own converted JSON) each get
 * their own recolor, matching the original's {@code Mario.java} switch.
 */
public class Pump extends InteractiveBrick {

    public Pump(float x, float y, String attribute, boolean top) {
        super(regionFor(attribute, top), x, y);
    }

    /** Ported from {@code Mario.java}'s case 44/45 ("HoriImage"/"PumpImage") - a pipe-styled solid decoration with an explicit image, not attribute-derived. */
    public Pump(float x, float y, TextureRegion region) {
        super(region, x, y);
    }

    private static TextureRegion regionFor(String attribute, boolean top) {
        if ("Castle".equals(attribute)) {
            return MarioResourceManager.region(top ? "pump_top_castle" : "pump_castle");
        }
        if ("Sea".equals(attribute)) {
            return MarioResourceManager.region(top ? "pump_top_sea" : "pump_sea");
        }
        return MarioResourceManager.region(top ? "pump_top" : "pump");
    }
}

package au.com.guidebee.morsetoolkit.activity.mario.fx;

import com.guidebee.game.graphics.Batch;
import com.guidebee.game.graphics.TextureRegion;
import com.guidebee.game.microedition.Layer;

import au.com.guidebee.morsetoolkit.activity.mario.MarioConfiguration;

/**
 * A level's own scrolling backdrop, ported from {@code Mario.java}'s "Tiled
 * background" block: repeats a level's {@code backgroundImage} (Mountain/
 * Clouds/CloudsNight/Fence/Fence2) 10 times side by side starting at world
 * x=0, y=32 - matches the original's literal {@code new Sprite(image,
 * 1536*i, 32)} loop; each source PNG is confirmed 1536x448 by hand, which is
 * what makes 10 repeats tile with no gap or overlap regardless of a level's
 * own length (the original always builds all 10 too, even for levels far
 * shorter than 15360px - harmless, so kept as-is rather than trimmed to the
 * level's actual length).
 *
 * <p>Purely decorative and non-collided; {@code MarioGameScreen} appends
 * this to the layerManager *before* the level's own {@code MarioWorld} so it
 * always paints behind every terrain tile/actor, the same way {@code
 * Scenery} is spawned before bricks/enemies for the same reason.
 *
 * <p>The Sea attribute's own backdrop ("sea_background", ported from the
 * same "Tiled background" block's own {@code Sea} branch) uses the second
 * constructor instead: tiled every 32px, not 1536px, starting at y=0 - see
 * that constructor's own doc.
 */
public class BackgroundBand extends Layer {

    private static final int DEFAULT_TILE_COUNT = 10;
    private static final float DEFAULT_Y = 32f;

    private final TextureRegion region;
    private final float y;
    private final int tileCount;

    public BackgroundBand(TextureRegion region) {
        this(region, DEFAULT_Y, DEFAULT_TILE_COUNT);
    }

    /**
     * Ported from Mario.java's own Sea-backdrop loop:
     * {@code new Sprite(getImage("Sea.png"), 32*i, 0)} repeated once per
     * 32px column across the level's own length (not a fixed 10 repeats of
     * a 1536px-wide image, unlike every other {@code backgroundImage} band -
     * see {@code MarioGameScreen}'s own call site for how {@code tileCount}
     * is derived from the level's length instead of hardcoded).
     */
    public BackgroundBand(TextureRegion region, float y, int tileCount) {
        super(0, y, (region.getRegionWidth() / MarioConfiguration.ART_SCALE) * tileCount,
                region.getRegionHeight() / MarioConfiguration.ART_SCALE, true);
        this.region = region;
        this.y = y;
        this.tileCount = tileCount;
    }

    @Override
    public void paint(Batch g) {
        int nativeWidth = region.getRegionWidth() / MarioConfiguration.ART_SCALE;
        float height = region.getRegionHeight() / MarioConfiguration.ART_SCALE;
        for (int i = 0; i < tileCount; i++) {
            g.draw(region, i * nativeWidth, y, nativeWidth, height);
        }
    }
}

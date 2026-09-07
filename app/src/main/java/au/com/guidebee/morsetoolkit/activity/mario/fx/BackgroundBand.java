package au.com.guidebee.morsetoolkit.activity.mario.fx;

import com.guidebee.game.graphics.Batch;
import com.guidebee.game.graphics.TextureRegion;
import com.guidebee.game.microedition.Layer;

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
 */
public class BackgroundBand extends Layer {

    private static final int TILE_COUNT = 10;
    private static final float Y = 32f;

    private final TextureRegion region;

    public BackgroundBand(TextureRegion region) {
        super(0, Y, region.getRegionWidth() * TILE_COUNT, region.getRegionHeight(), true);
        this.region = region;
    }

    @Override
    public void paint(Batch g) {
        int nativeWidth = region.getRegionWidth();
        float height = region.getRegionHeight();
        for (int i = 0; i < TILE_COUNT; i++) {
            g.draw(region, i * nativeWidth, Y, nativeWidth, height);
        }
    }
}

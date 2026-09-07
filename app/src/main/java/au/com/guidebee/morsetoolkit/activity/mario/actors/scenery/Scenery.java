package au.com.guidebee.morsetoolkit.activity.mario.actors.scenery;

import com.guidebee.game.graphics.Batch;
import com.guidebee.game.graphics.TextureRegion;
import com.guidebee.game.microedition.Layer;

/**
 * A purely decorative, non-solid image drawn at its native size and a fixed
 * world position - the flagpole and the end-of-level castles. These never
 * participate in collision themselves; the gameplay-relevant "level
 * complete" trigger is {@code LevelDefinition}'s separate
 * {@code checkpoints} data (see {@code CheckpointResolver}), placed
 * independently in the original level data, not these tiles' own bounding
 * boxes - see docs/MARIO_PORT_PLAN.md Step 7.1.
 */
public class Scenery extends Layer {

    private final TextureRegion region;

    public Scenery(float x, float y, TextureRegion region) {
        this(x, y, region, region.getRegionWidth(), region.getRegionHeight());
    }

    /**
     * Ported from {@code Mario.java}'s case 61 ("WhiteLine") - the one
     * decoration that's stretched to an explicit size rather than drawn at
     * its source pixel dimensions ({@code ImageUtil.resize(..., 32, 13*32)}).
     */
    public Scenery(float x, float y, TextureRegion region, float width, float height) {
        super(x, y, width, height, true);
        this.region = region;
    }

    @Override
    public void paint(Batch g) {
        g.draw(region, getX(), getY(), getWidth(), getHeight());
    }
}

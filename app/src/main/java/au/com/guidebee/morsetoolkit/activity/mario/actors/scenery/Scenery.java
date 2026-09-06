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
        super(x, y, region.getRegionWidth(), region.getRegionHeight(), true);
        this.region = region;
    }

    @Override
    public void paint(Batch g) {
        g.draw(region, getX(), getY(), getWidth(), getHeight());
    }
}

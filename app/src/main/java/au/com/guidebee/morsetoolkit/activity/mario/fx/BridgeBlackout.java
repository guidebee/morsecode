package au.com.guidebee.morsetoolkit.activity.mario.fx;

import com.guidebee.game.graphics.Batch;
import com.guidebee.game.graphics.Pixmap;
import com.guidebee.game.graphics.Texture;
import com.guidebee.game.graphics.TextureRegion;
import com.guidebee.game.microedition.Layer;

import au.com.guidebee.morsetoolkit.activity.mario.MarioConfiguration;

/**
 * One tile of the boss-bridge collapsing into blackness, ported from
 * {@code Animations/Black.java}: invisible until its own delay elapses, then
 * a solid black square for good - {@code BossFallingAnim.spawnCollapse} lays
 * 13 of these across the bridge (the axe's own 13-tile-wide deck, matching
 * {@code Bricks/BridgeBloks}) with a staggered delay per tile so the collapse
 * reads as a wave starting at the axe and sweeping back toward Mario, not an
 * instant flip. A plain runtime-generated black texture, not a packed asset -
 * there's nothing to source, it's one flat color.
 */
public class BridgeBlackout extends Layer {

    private static final float PHYSICS_FPS = 60f;

    private static TextureRegion blackRegion;

    private float delayTicks;

    public BridgeBlackout(float x, float y, float delayTicks) {
        super(x, y, MarioConfiguration.TILE_SIZE, MarioConfiguration.TILE_SIZE, true);
        this.delayTicks = delayTicks;
    }

    private static TextureRegion blackRegion() {
        if (blackRegion == null) {
            int tileSize = MarioConfiguration.TILE_SIZE;
            Pixmap pixmap = new Pixmap(tileSize, tileSize, Pixmap.Format.RGBA8888);
            pixmap.setColor(0f, 0f, 0f, 1f);
            pixmap.fill();
            Texture texture = new Texture(pixmap);
            pixmap.dispose();
            blackRegion = new TextureRegion(texture);
        }
        return blackRegion;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (delayTicks > 0) {
            delayTicks -= delta * PHYSICS_FPS;
        }
    }

    @Override
    public void paint(Batch g) {
        if (delayTicks <= 0) {
            g.draw(blackRegion(), getX(), getY(), getWidth(), getHeight());
        }
    }
}

package au.com.guidebee.morsetoolkit.activity.mario.actors.bricks;

import com.guidebee.game.graphics.Pixmap;
import com.guidebee.game.graphics.Texture;
import com.guidebee.game.graphics.TextureRegion;

import au.com.guidebee.morsetoolkit.activity.mario.MarioConfiguration;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioContext;

/**
 * A brief, invisible solid placeholder left where a {@link Brick} just broke,
 * ported from {@code Bricks/TemporaryAndInvisibleBrick.java}: keeps that
 * spot solid for ~10 original ticks after the real brick deactivates, so
 * anything standing exactly on top of it the instant it breaks (an enemy,
 * mainly) doesn't fall through a frame early. Its own image is a plain
 * transparent placeholder in the original too ({@code
 * ImageUtil.createImage(32, 32, 3)}) - generated at runtime here as well,
 * matching {@code fx.BridgeBlackout}'s own "nothing to source, generate it"
 * precedent, rather than a packed asset for something never actually seen.
 */
public class TemporaryInvisibleBrick extends InteractiveBrick {

    private static final float PHYSICS_FPS = 60f;
    /** Ported from the original's own literal {@code delay = 10}. */
    private static final float LIFETIME_TICKS = 10f;

    private static TextureRegion transparentRegion;

    private float ticksLeft = LIFETIME_TICKS;

    private TemporaryInvisibleBrick(float x, float y) {
        super(transparentRegion(), x, y);
        setVisible(false);
    }

    public static void spawnAt(float x, float y) {
        TemporaryInvisibleBrick brick = new TemporaryInvisibleBrick(x, y);
        MarioContext.world().addBrick(brick);
        MarioContext.spawn(brick);
    }

    private static TextureRegion transparentRegion() {
        if (transparentRegion == null) {
            int tileSize = MarioConfiguration.TILE_SIZE;
            Pixmap pixmap = new Pixmap(tileSize, tileSize, Pixmap.Format.RGBA8888);
            pixmap.setColor(0f, 0f, 0f, 0f);
            pixmap.fill();
            Texture texture = new Texture(pixmap);
            pixmap.dispose();
            transparentRegion = new TextureRegion(texture);
        }
        return transparentRegion;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        ticksLeft -= delta * PHYSICS_FPS;
        if (ticksLeft <= 0) {
            deactivate();
        }
    }
}

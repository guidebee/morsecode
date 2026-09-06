package au.com.guidebee.morsetoolkit.activity.mario.fx;

import com.guidebee.game.graphics.TextureRegion;
import com.guidebee.game.microedition.Sprite;

import au.com.guidebee.morsetoolkit.activity.mario.MarioConfiguration;
import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;

/**
 * The "pop out of a block and rise one tile" effect shared by every original
 * reveal animation (Mushroom/Flower/Star/Life - see {@code Animations
 * .MashroomAnim/FlowerAnim/StarAnim/LifeAnim}, which are otherwise identical
 * modulo their rise speed and what they spawn at the top). Waits briefly,
 * rises to one tile above the brick, then hands off to {@code onRisen} and
 * removes itself.
 *
 * <p>Simplification: the original's Flower variant animates (bobs through
 * frames) while rising; this always shows a static frame during the rise,
 * which is a minor cosmetic difference only.
 */
public class ItemReveal extends Sprite {

    /** Spawns the real collectible once the reveal finishes rising. */
    public interface Spawner {
        void spawn(float x, float y);
    }

    private static final float DELAY_SECONDS = 32f / 60f;

    private final float targetY;
    private final float riseSpeedPxPerSec;
    private final Spawner spawner;

    private float delayTimer;
    private boolean rising;

    public ItemReveal(TextureRegion region, float x, float y, float riseSpeedPxPerSec, Spawner spawner) {
        super(region);
        setPosition(x, y);
        this.targetY = y - MarioConfiguration.TILE_SIZE;
        this.riseSpeedPxPerSec = riseSpeedPxPerSec;
        this.spawner = spawner;
        setVisible(false);
        MarioResourceManager.sound("smb_powerup_appears").play();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!rising) {
            delayTimer += delta;
            if (delayTimer >= DELAY_SECONDS) {
                rising = true;
                setVisible(true);
            }
            return;
        }

        float newY = getY() - riseSpeedPxPerSec * delta;
        if (newY <= targetY) {
            spawner.spawn(getX(), targetY);
            remove();
        } else {
            setY(newY);
        }
    }
}

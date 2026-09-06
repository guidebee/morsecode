package au.com.guidebee.morsetoolkit.activity.mario.actors.projectiles;

import com.guidebee.game.microedition.Sprite;

import java.util.Random;

import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.actors.hazards.Hazard;

/**
 * A drifting flame, ported from {@code Objects/BossFire.java}. The same
 * class serves two roles in the original, both reproduced here unchanged:
 * a handful placed directly in Level 14's data (a static hazard drifting
 * across the boss room), and the projectile {@code Boss} itself throws at
 * Mario at random intervals ({@code HammerFire()} in non-hammer mode) - both
 * just drift left at a constant speed while bobbing toward a random target
 * height, so one constructor covers both call sites.
 *
 * <p>The "boss_fire" region is 48x16 per frame, 2 frames (96x16 total) -
 * confirmed against {@code WholeGame.java}'s own
 * {@code getImages("BossFire.png", 2, 1)} slicing call, not assumed.
 *
 * <p>Deactivation: the original checks {@code getScreenX() < 0} (off the
 * *camera's* left edge); this port's actors have no camera reference (see
 * {@code MarioContext}'s class doc), so this checks world-space
 * {@code x + width < 0} instead - deactivates somewhat later than the
 * original (once it's drifted past the level's own left edge, not just off
 * whatever's currently on screen), a harmless difference since a fire this
 * far behind the camera is never seen either way.
 */
public class BossFire extends Sprite implements Hazard {

    private static final int FRAME_WIDTH = 48;
    private static final int FRAME_HEIGHT = 16;
    private static final float PHYSICS_FPS = 60f;
    private static final float DRIFT_SPEED = 2f;
    private static final float VERTICAL_SPEED = 2f;
    private static final float ANIMATION_INTERVAL = 0.1f;

    private static final Random RANDOM = new Random();

    private final float targetY;
    private boolean active = true;
    private float animTimer;

    public BossFire(float x, float y) {
        super(MarioResourceManager.region("boss_fire"), FRAME_WIDTH, FRAME_HEIGHT);
        setPosition(x, y);
        // Ported from `Utility.getRandom(6, 9) * 32` - a random height among
        // the boss room's own floor levels.
        targetY = (6 + RANDOM.nextInt(4)) * 32;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!active) {
            return;
        }
        float frames = delta * PHYSICS_FPS;
        if (getY() > targetY) {
            setY(getY() - VERTICAL_SPEED * frames);
        } else if (getY() < targetY) {
            setY(getY() + VERTICAL_SPEED * frames);
        }
        setX(getX() - DRIFT_SPEED * frames);

        if (getX() + getWidth() < 0) {
            active = false;
            remove();
            return;
        }
        animTimer += delta;
        if (animTimer >= ANIMATION_INTERVAL) {
            animTimer = 0;
            nextFrame();
        }
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public boolean overlaps(float x, float y, int width, int height) {
        return active
                && x < getX() + getWidth() && x + width > getX()
                && y < getY() + getHeight() && y + height > getY();
    }
}

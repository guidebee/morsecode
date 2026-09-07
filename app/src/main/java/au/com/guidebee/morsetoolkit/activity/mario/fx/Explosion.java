package au.com.guidebee.morsetoolkit.activity.mario.fx;

import com.guidebee.game.microedition.Sprite;

import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioContext;

/**
 * The puff a fireball leaves behind when it hits a wall or an enemy, ported
 * from {@code Animations/Explosion.java} (a plain {@code VolatileSprite} -
 * plays its 3-frame strip once, then gone). Reuses the same "explosion"
 * region {@code Fireworks} already uses, just without that class's own
 * repeating-burst/random-reposition behavior - this one plays exactly once.
 */
public class Explosion extends Sprite {

    private static final int FRAME_SIZE = 32;
    private static final float FRAME_INTERVAL = 0.1f;

    private float frameTimer;

    private Explosion(float x, float y) {
        super(MarioResourceManager.region("explosion"), FRAME_SIZE, FRAME_SIZE);
        setPosition(x, y);
    }

    public static void spawn(float x, float y) {
        MarioContext.spawn(new Explosion(x, y));
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        frameTimer += delta;
        if (frameTimer < FRAME_INTERVAL) {
            return;
        }
        frameTimer = 0;
        if (getFrame() == getFrameSequenceLength() - 1) {
            remove();
            return;
        }
        nextFrame();
    }
}

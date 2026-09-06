package au.com.guidebee.morsetoolkit.activity.mario.fx;

import com.guidebee.game.microedition.Sprite;

import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;

/**
 * The "coin get" popup - ported from {@code Animations/CoinAnim.java}: pops
 * up ballistically, falls back under gravity, then self-destructs. Unlike
 * Mushroom/Flower/Star/Life, a "CoinInside" reveal doesn't spawn a
 * persistent, player-touchable collectible - the coin is credited
 * immediately (matching the original's {@code game.parent.CoinInc()} call
 * in its constructor) and this is purely the visual.
 *
 * <p>TODO Step 8: route the coin credit through {@code GameStateController}
 * once it exists, instead of only playing the sound/animation.
 */
public class CoinPopEffect extends Sprite {

    private static final float PHYSICS_FPS = 60f;
    private static final float GRAVITY_STEP = 1f;
    private static final float GRAVITY_CAP = 10f;
    private static final float ANIMATION_INTERVAL = 0.1f;

    private float gravity = -15f;
    private float animTimer;

    public CoinPopEffect(float x, float y) {
        super(MarioResourceManager.region("coin_anim"), 32, 32);
        setPosition(x, y);
        MarioResourceManager.sound("smb_coin").play();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        float frames = delta * PHYSICS_FPS;

        if (gravity < GRAVITY_CAP) {
            gravity = Math.min(GRAVITY_CAP, gravity + GRAVITY_STEP * frames);
            setY(getY() + gravity * frames);
        } else {
            remove();
            return;
        }

        animTimer += delta;
        if (animTimer >= ANIMATION_INTERVAL) {
            animTimer = 0;
            nextFrame();
        }
    }
}

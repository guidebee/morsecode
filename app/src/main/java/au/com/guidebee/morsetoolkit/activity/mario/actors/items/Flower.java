package au.com.guidebee.morsetoolkit.activity.mario.actors.items;

import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;

/**
 * The fire-flower power-up, ported from {@code Objects/Flower.java}:
 * stationary, animated in place.
 *
 * <p>NOTE: the original's collision callbacks are all empty - touching a
 * Flower does nothing in the shipped game, which reads as an unfinished
 * feature rather than an intentional design (Mushroom/Star/Life all *do*
 * apply their effect on touch, and a Flower only ever spawns when Mario is
 * already big, i.e. exactly when a fire upgrade makes sense). Fixed here
 * rather than faithfully replicated, per docs/MARIO_PORT_PLAN.md Step 5.2's
 * instruction to port "the grow/shrink animation sequences".
 */
public class Flower extends CollectibleItem {

    private static final float ANIMATION_INTERVAL = 0.1f;

    private float animTimer;

    public Flower(float x, float y, int tileSize) {
        super(MarioResourceManager.region("flower"), tileSize, tileSize, x, y);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        animTimer += delta;
        if (animTimer >= ANIMATION_INTERVAL) {
            animTimer = 0;
            nextFrame();
        }
    }

    @Override
    public void onCollected(Player player) {
        player.grow();
        collect();
    }
}

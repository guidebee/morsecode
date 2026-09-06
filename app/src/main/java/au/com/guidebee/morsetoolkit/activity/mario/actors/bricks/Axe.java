package au.com.guidebee.morsetoolkit.activity.mario.actors.bricks;

import com.guidebee.game.microedition.Sprite;

import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;

/**
 * The axe at the end of Level 14's bridge, ported from {@code Bricks/Axe.java}.
 * Purely an animated visual here - its actual gameplay effect (an invisible
 * wall stopping Mario at its x, forever, regardless of height) is handled by
 * {@code collision.AxeResolver} each frame instead of this class's own
 * {@code act()}, since (unlike every other actor in this port) it needs to
 * compare against the player's position, which actors don't otherwise have a
 * reference to - see {@code MarioContext}'s class doc for why that lookup is
 * deliberately kept out of individual actors.
 *
 * <p>Ported faithfully as a permanent wall - the original never deactivates
 * or moves this once placed (no "chop the rope, bridge collapses" code exists
 * anywhere in the source, confirmed - see docs/MARIO_PORT_PLAN_PHASE2.md
 * S1.4). Whether Level 14's own layout leaves its post-boss checkpoint
 * actually reachable past this wall is exactly what this step's vertical
 * slice (P2.0.4) needs to verify by playing it, not something to assume
 * either way up front.
 */
public class Axe extends Sprite {

    private static final int FRAME_WIDTH = 32;
    private static final int FRAME_HEIGHT = 32;
    private static final float FRAME_INTERVAL = 0.2f;

    private float animTimer;

    public Axe(float x, float y) {
        super(MarioResourceManager.region("axe"), FRAME_WIDTH, FRAME_HEIGHT);
        setFrameSequence(new int[]{0, 1, 2, 3, 2, 1});
        setPosition(x, y);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        animTimer += delta;
        if (animTimer >= FRAME_INTERVAL) {
            animTimer = 0;
            nextFrame();
        }
    }
}

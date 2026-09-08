package au.com.guidebee.morsetoolkit.activity.mario.actors.bricks;

import com.guidebee.game.microedition.Sprite;

import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;

/**
 * The axe at the end of a castle level's bridge, ported from
 * {@code Bricks/Axe.java}. Its gameplay effect has two independent parts:
 * an invisible wall stopping Mario at its x, forever, regardless of height
 * (unconditional, handled every frame by {@code collision.AxeResolver} since
 * it needs the player's position, which actors don't otherwise have a
 * reference to - see {@code MarioContext}'s class doc), and a one-shot
 * "chop the rope" touch (see {@link #overlaps}/{@link #trigger}) that *does*
 * exist in the original - {@code Collusion/Player_Brick.java}'s own
 * {@code getID()==15} branch, reachable in every boss level, not dead code.
 * (An earlier revision of this class's own doc, and of
 * docs/MARIO_PORT_PLAN_PHASE2.md's S1.4, claimed no such code exists - that
 * was wrong; corrected once this was actually implemented, see that
 * document's S7 for how the gap was found.) {@code MarioGameScreen} is what
 * reacts to {@link #trigger} - see its own axe-handling doc for the full
 * bridge-collapse/boss-fall sequence this kicks off.
 */
public class Axe extends Sprite {

    private static final float FRAME_INTERVAL = 0.2f;

    private float animTimer;
    private boolean triggered;

    public Axe(float x, float y, int tileSize) {
        super(MarioResourceManager.region("axe"), tileSize, tileSize);
        setFrameSequence(new int[]{0, 1, 2, 3, 2, 1});
        setPosition(x, y);
    }

    /** Ported from {@code Player_Brick.collided}'s own angle-bucketed contact test, simplified to a plain AABB overlap - matches this port's existing convention for every other one-shot touch trigger (e.g. {@code FlagPole#overlaps}). */
    public boolean overlaps(Player player) {
        return player.getX() < getX() + getWidth() && player.getX() + player.getWidth() > getX()
                && player.getY() < getY() + getHeight() && player.getY() + player.getHeight() > getY();
    }

    public boolean isTriggered() {
        return triggered;
    }

    /** Ported from {@code b.setActive(false)} in the original's own axe branch - the axe itself vanishes once chopped. */
    public void trigger() {
        triggered = true;
        setVisible(false);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (triggered) {
            return;
        }
        animTimer += delta;
        if (animTimer >= FRAME_INTERVAL) {
            animTimer = 0;
            nextFrame();
        }
    }
}

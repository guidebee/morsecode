package au.com.guidebee.morsetoolkit.activity.mario.actors.bricks;

import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.actors.scenery.Spring;

/**
 * A launch pad, ported from {@code Bricks/Bouncer.java} - a plain solid
 * block on its own (like {@link Iron}, {@code HitFromDown} is a no-op); the
 * actual launch happens in {@code Player}'s own landing check (see
 * {@code Player#moveYWithCollision}), ported from {@code Collusion
 * .Player_Brick.collided}'s {@code if (b.getID() == 13) p.Jump(-22)} -
 * landing on one always launches Mario straight back up at roughly double a
 * normal jump's impulse, never lets him actually stand on it.
 *
 * <p>The decorative {@link Spring} sprite the original always places one
 * tile above a Bouncer squishes on every launch - {@code
 * LevelLoader#spawnBricks}'s "Bouncer" case links the two via {@link
 * #setSpring}, and {@code Player#moveYWithCollision}'s own launch branch
 * calls {@link #triggerSpring} the same frame it applies the launch impulse.
 */
public class Bouncer extends InteractiveBrick {

    private Spring spring;

    public Bouncer(float x, float y) {
        this(x, y, false);
    }

    /**
     * @param blackAndWhite CloudsNight (World 6's Level_63, the only level
     *                      that sets this) swaps in "bw_bouncer" - unlike its
     *                      decorative Spring, which stays the normal asset
     *                      even there (confirmed by reading {@code
     *                      Mario.java}'s own case 31: only the {@code
     *                      Bouncer} constructor call switches to
     *                      "BWBouncer", the {@code Spring} one right above it
     *                      doesn't).
     */
    public Bouncer(float x, float y, boolean blackAndWhite) {
        super(MarioResourceManager.region(blackAndWhite ? "bw_bouncer" : "bouncer"), x, y);
    }

    /** Links this Bouncer to its own decorative Spring, set right after both are spawned - see {@code LevelLoader}. */
    public void setSpring(Spring spring) {
        this.spring = spring;
    }

    /** Called by {@code Player#moveYWithCollision} the instant this Bouncer launches the player. */
    public void triggerSpring() {
        if (spring != null) {
            spring.play();
        }
    }
}

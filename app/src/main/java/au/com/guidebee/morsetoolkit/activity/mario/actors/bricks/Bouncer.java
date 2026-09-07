package au.com.guidebee.morsetoolkit.activity.mario.actors.bricks;

import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;

/**
 * A launch pad, ported from {@code Bricks/Bouncer.java} - a plain solid
 * block on its own (like {@link Iron}, {@code HitFromDown} is a no-op); the
 * actual launch happens in {@code Player}'s own landing check (see
 * {@code Player#moveYWithCollision}), ported from {@code Collusion
 * .Player_Brick.collided}'s {@code if (b.getID() == 13) p.Jump(-22)} -
 * landing on one always launches Mario straight back up at roughly double a
 * normal jump's impulse, never lets him actually stand on it.
 *
 * <p>The decorative {@code Spring} sprite the original always places one
 * tile above a Bouncer is rendered as plain {@code Scenery} in this port
 * (see {@code LevelLoader#spawnBricks}'s "Bouncer" case) - its own
 * {@code PlayAnimation()} squish is a cosmetic-only flourish with no
 * gameplay effect (confirmed by reading {@code Player_Brick.collided}: the
 * branch that would trigger it never repositions or launches the player
 * either), skipped the same way this port already skips other minor
 * original animation nuances.
 */
public class Bouncer extends InteractiveBrick {

    public Bouncer(float x, float y) {
        super(MarioResourceManager.region("bouncer"), x, y);
    }
}

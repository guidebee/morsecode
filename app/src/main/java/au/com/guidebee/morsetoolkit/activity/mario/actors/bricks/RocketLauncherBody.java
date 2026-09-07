package au.com.guidebee.morsetoolkit.activity.mario.actors.bricks;

import com.guidebee.game.graphics.TextureRegion;

/**
 * One stacked body segment below a {@link RocketLauncher}'s own turret
 * head, ported from {@code Bricks/RocketLauncherBody.java} - a plain solid
 * block, like {@link Iron} ({@code HitFromDown} is a no-op). {@code region}
 * is already one sliced-out frame of the shared "rocket_launcher" strip
 * (see {@link RocketLauncher}'s own class doc), not a multi-frame region
 * itself.
 */
public class RocketLauncherBody extends InteractiveBrick {

    public RocketLauncherBody(float x, float y, TextureRegion region) {
        super(region, x, y);
    }
}

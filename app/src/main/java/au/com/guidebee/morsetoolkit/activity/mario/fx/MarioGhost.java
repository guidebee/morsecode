package au.com.guidebee.morsetoolkit.activity.mario.fx;

import com.guidebee.game.graphics.Batch;
import com.guidebee.game.graphics.TextureRegion;
import com.guidebee.game.microedition.Layer;

import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.PlayerPowerState;

/**
 * A frozen stand-in for Mario, ported from {@code Mario.java}'s own
 * {@code DemoMario} (a plain {@code new Sprite(player.getImage(), ...)}
 * snapshot): once the real {@link Player} is put under forced auto-walk for
 * the boss-bridge finale (see {@code MarioGameScreen}'s axe-touch handling),
 * he immediately starts walking off toward the level's exit, which would
 * otherwise leave the dramatic bridge-collapse/boss-fall play out in empty
 * space. This stands in his exact spot for the sequence's duration instead,
 * removed by {@link BossFallingAnim} once it finishes.
 *
 * <p>Doesn't attempt to reproduce whatever mid-animation frame the real
 * Player happened to be showing at the moment of the touch (the original's
 * own snapshot is just as static, so this only needs to look plausible, not
 * pixel-identical) - a plain standing pose, facing the same direction, at the
 * player's current power state.
 */
public class MarioGhost extends Layer {

    /** Ported from {@code Player.updateAnimation}'s own idle frame indices (0 = standing right, 1 = standing left). */
    private static final int STANDING_RIGHT_FRAME = 0;
    private static final int STANDING_LEFT_FRAME = 1;

    private final TextureRegion region;

    public MarioGhost(Player player) {
        super(player.getX(), player.getY(), player.getWidth(), player.getHeight(), true);
        PlayerPowerState state = player.getPowerState();
        int frame = player.isFacingRight() ? STANDING_RIGHT_FRAME : STANDING_LEFT_FRAME;
        TextureRegion[][] frames = MarioResourceManager.region(state.regionName).split(state.width, state.height);
        int cols = frames[0].length;
        region = frames[frame / cols][frame % cols];
    }

    @Override
    public void paint(Batch g) {
        g.draw(region, getX(), getY(), getWidth(), getHeight());
    }
}

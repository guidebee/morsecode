package au.com.guidebee.morsetoolkit.activity.mario.collision;

import java.util.List;

import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.level.LevelDefinition;

/**
 * Finds the checkpoint (if any) the player has just triggered - a level-end
 * flag, a pipe entrance into a bonus area, or Level 14's "WhyYouDOThis"
 * fake-out. Ported from the original's {@code Player_CheckPoint.collided}
 * switch on {@code checkpoint.getID()}: each kind's original per-ID gameplay
 * gate (KeyPressRight+onGround for a horizontal pipe, horizontal
 * proximity+KeyPressedDown for a vertical one, plain contact for everything
 * else) is preserved. What's deferred: the original's fireworks/HUD
 * flourishes and the fake "another castle" message - see
 * docs/MARIO_PORT_PLAN.md Step 7.1 and {@code MarioGameScreen}'s
 * level-completion state machine, which is what actually reacts to a hit
 * this method reports.
 */
public final class CheckpointResolver {

    /** The original stores each checkpoint's exact trigger position, not a tile-sized footprint - this is the generous hitbox around it. */
    private static final int TRIGGER_WIDTH = 32;
    private static final int TRIGGER_HEIGHT = 64;
    private static final float PUMP_HORIZONTAL_PROXIMITY = 10f;

    private CheckpointResolver() {
    }

    public static LevelDefinition.Checkpoint findTouched(List<LevelDefinition.Checkpoint> checkpoints, Player player) {
        float px = player.getX();
        float py = player.getY();
        int pw = (int) player.getWidth();
        int ph = (int) player.getHeight();

        for (LevelDefinition.Checkpoint checkpoint : checkpoints) {
            float cx = (float) checkpoint.x;
            float cy = (float) checkpoint.y;
            boolean overlaps = px < cx + TRIGGER_WIDTH && px + pw > cx
                    && py < cy + TRIGGER_HEIGHT && py + ph > cy;
            if (!overlaps) {
                continue;
            }

            switch (checkpoint.kind) {
                case "InsidePumpHorzontally":
                    if (player.wantsRight() && player.isOnGround()) {
                        return checkpoint;
                    }
                    break;
                case "InsidePumpvertically":
                    if (Math.abs(px - cx) < PUMP_HORIZONTAL_PROXIMITY && player.wantsDown()) {
                        return checkpoint;
                    }
                    break;
                default:
                    // "CheckPoints" (basic level end), "WhyYouDOThis", and any
                    // other kind trigger on plain contact.
                    return checkpoint;
            }
        }
        return null;
    }
}

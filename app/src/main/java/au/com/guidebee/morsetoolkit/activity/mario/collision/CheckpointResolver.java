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
    /**
     * Ported from {@code Mario.java}'s own {@code LoadCheckPoints}: a
     * {@code Clowd_CheckPoint}'s collision image is a deliberately huge,
     * invisible {@code ImageUtil.createImage(640, 64, 3)} - it marks a whole
     * landing platform at the top of a beanstalk climb, not a precise point,
     * unlike every other checkpoint kind's plain 32-wide default.
     */
    private static final int CLOWD_TRIGGER_WIDTH = 640;
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
            int triggerWidth = "Clowd_CheckPoint".equals(checkpoint.kind) ? CLOWD_TRIGGER_WIDTH : TRIGGER_WIDTH;
            boolean overlaps = px < cx + triggerWidth && px + pw > cx
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
                case "ClowdGoUP_CheckPoint":
                    // Ported from Player_CheckPoint.collided's own ID-28
                    // case: `if (p.KeyPressedUP)` - the beanstalk-climb entry
                    // only fires while Up is held, matching the original.
                    if (player.wantsUp()) {
                        return checkpoint;
                    }
                    break;
                default:
                    // "CheckPoints" (basic level end), "WhyYouDOThis",
                    // "Clowd_CheckPoint", and any other kind trigger on plain
                    // contact.
                    return checkpoint;
            }
        }
        return null;
    }
}

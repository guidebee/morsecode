package au.com.guidebee.morsetoolkit.activity.mario.input;

/**
 * One frame's worth of player intent, polled by {@link MarioInputController}
 * and consumed by {@code Player.act(float)}. See docs/MARIO_PORT_PLAN.md Step 4.1.
 */
public final class PlayerCommand {

    public boolean left;
    public boolean right;
    /** Held, not edge-triggered - gates a vertical pipe's entry (see {@code CheckpointResolver}). */
    public boolean down;
    /**
     * Held, not edge-triggered - ported from {@code Player.KeyPressedUP},
     * itself a plain {@code isKeyDown(VK_UP)} read wholly separate from the
     * edge-triggered jump input below (confirmed by reading {@code
     * Mario.java}'s own input-polling block). Gates a {@code
     * ClowdGoUP_CheckPoint}'s beanstalk-climb entry (see {@code
     * CheckpointResolver}) - nothing else reads it.
     */
    public boolean up;

    /** Edge-triggered: true only on the frame the jump input started. */
    public boolean jumpPressed;

    /** Edge-triggered: true only on the frame the fire input started. Only Fire Mario acts on it. */
    public boolean firePressed;

    /**
     * Held, not edge-triggered - ported from {@code Player.Speed(boolean)}.
     * The original reads this off the *same* physical key as fire
     * ({@code VK_Z}: tapped fires, held runs) rather than a separate button;
     * this mirrors that by reusing the fire input (button A / keyboard X)
     * continuously instead of adding new UI.
     */
    public boolean runHeld;
}

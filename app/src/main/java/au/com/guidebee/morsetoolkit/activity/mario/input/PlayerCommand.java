package au.com.guidebee.morsetoolkit.activity.mario.input;

/**
 * One frame's worth of player intent, polled by {@link MarioInputController}
 * and consumed by {@code Player.act(float)}. See docs/MARIO_PORT_PLAN.md Step 4.1.
 */
public final class PlayerCommand {

    public boolean left;
    public boolean right;

    /** Edge-triggered: true only on the frame the jump input started. */
    public boolean jumpPressed;

    /** Edge-triggered: true only on the frame the fire input started. Only Fire Mario acts on it. */
    public boolean firePressed;
}

package au.com.guidebee.morsetoolkit.platformer.input;

/**
 * One frame's worth of platformer-player intent.
 *
 * <p>Directional and run fields describe held input. {@link #jumpPressed}
 * and {@link #actionPressed} describe an input edge, so actions only occur
 * once when their button is pressed.
 */
public final class PlatformerCommand {

    /** Held, not edge-triggered. */
    public boolean left;
    /** Held, not edge-triggered. */
    public boolean right;
    /** Held, not edge-triggered - a game may use this for e.g. gating a crouch or a vertical entry trigger. */
    public boolean down;
    /** Held, not edge-triggered - kept separate from {@link #jumpPressed}'s own edge-triggered read of a jump key, for a game that reads a held "up" intent for something other than jumping. */
    public boolean up;

    /** Edge-triggered: true only on the frame the jump input started. */
    public boolean jumpPressed;

    /** Edge-triggered: true only on the frame the primary action input started (e.g. firing, interacting). */
    public boolean actionPressed;

    /**
     * Held, not edge-triggered. Typically read off the same physical
     * input as {@link #actionPressed} (tap to act, hold to run) rather than
     * a separate control - see {@code TouchOrKeyboardInput#poll}'s own
     * reuse of the action button/key for this.
     */
    public boolean runHeld;
}

package au.com.guidebee.morsetoolkit.platformer.input;

import com.guidebee.game.GameEngine;
import com.guidebee.game.Input;
import com.guidebee.game.ui.GameController;

/**
 * Polls keyboard and {@link GameController} touch state into one reusable
 * {@link PlatformerCommand} each frame.
 *
 * <p>Arrow keys or WASD move; Space, Up, or Z jumps; X activates the primary
 * action and holds run. Game-controller button A activates the primary action
 * and holds run, button B jumps, and its knob moves in the four directions.
 *
 * <p>A platformer's own movement is typically continuous and held, not a
 * series of discrete per-move events, so the knob/buttons are polled
 * directly here every frame ({@code getKnobPercentX/Y()}, {@code
 * isButtonXPressed()}) rather than going through a listener.
 */
public class TouchOrKeyboardInput {

    /**
     * How far off-center the knob must be dragged, as a fraction of its full
     * travel, to register as a direction.
     */
    private static final float KNOB_DEADZONE = 0.3f;

    private final PlatformerCommand command = new PlatformerCommand();
    private final GameController gameController;

    private boolean jumpButtonWasPressed;
    private boolean actionButtonWasPressed;

    public TouchOrKeyboardInput(GameController gameController) {
        this.gameController = gameController;
    }

    public PlatformerCommand poll() {
        boolean keyboardLeft = GameEngine.input.isKeyPressed(Input.Keys.LEFT)
                || GameEngine.input.isKeyPressed(Input.Keys.A);
        boolean keyboardRight = GameEngine.input.isKeyPressed(Input.Keys.RIGHT)
                || GameEngine.input.isKeyPressed(Input.Keys.D);
        boolean keyboardJump = GameEngine.input.isKeyJustPressed(Input.Keys.SPACE)
                || GameEngine.input.isKeyJustPressed(Input.Keys.UP)
                || GameEngine.input.isKeyJustPressed(Input.Keys.Z);
        boolean keyboardAction = GameEngine.input.isKeyJustPressed(Input.Keys.X);
        boolean keyboardRunHeld = GameEngine.input.isKeyPressed(Input.Keys.X);
        boolean keyboardDown = GameEngine.input.isKeyPressed(Input.Keys.DOWN)
                || GameEngine.input.isKeyPressed(Input.Keys.S);
        boolean keyboardUp = GameEngine.input.isKeyPressed(Input.Keys.UP)
                || GameEngine.input.isKeyPressed(Input.Keys.W);

        // The controller's buttons only expose "currently held"
        // (isButtonXPressed()), so jump/action are edge-detected here, the
        // same way isKeyJustPressed works for keyboard.
        boolean actionButtonPressed = gameController.isButtonAPressed();
        boolean actionButtonJustPressed = actionButtonPressed && !actionButtonWasPressed;
        actionButtonWasPressed = actionButtonPressed;

        boolean jumpButtonPressed = gameController.isButtonBPressed();
        boolean jumpButtonJustPressed = jumpButtonPressed && !jumpButtonWasPressed;
        jumpButtonWasPressed = jumpButtonPressed;

        float knobX = gameController.getKnobPercentX();
        float knobY = gameController.getKnobPercentY();

        command.left = keyboardLeft || knobX < -KNOB_DEADZONE;
        command.right = keyboardRight || knobX > KNOB_DEADZONE;
        // Touchpad's own local Y axis (like every other Actor's) increases
        // upward within whatever coordinate system its parent hands it. This
        // sign choice (down = positive knobY) matches a screen built with a
        // Y-down world/HUD camera, where increasing Y is physically lower on
        // screen - dragging the knob toward the physically lower half of the
        // touchpad then increases, not decreases, getKnobPercentY(), the
        // reverse of Touchpad's own javadoc (written for an ordinary Y-up
        // camera). Not a bug: verified by hand on-device against a Y-down
        // screen. A game whose own camera is the ordinary Y-up convention
        // instead should swap these two lines' comparisons.
        command.down = keyboardDown || knobY > KNOB_DEADZONE;
        command.up = keyboardUp || knobY < -KNOB_DEADZONE;
        command.jumpPressed = keyboardJump || jumpButtonJustPressed;
        command.actionPressed = keyboardAction || actionButtonJustPressed;
        command.runHeld = keyboardRunHeld || actionButtonPressed;
        return command;
    }
}

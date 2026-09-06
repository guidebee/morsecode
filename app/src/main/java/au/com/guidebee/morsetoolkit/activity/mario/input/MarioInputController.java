package au.com.guidebee.morsetoolkit.activity.mario.input;

import com.guidebee.game.GameEngine;
import com.guidebee.game.Input;
import com.guidebee.game.ui.GameController;

/**
 * Polls keyboard/touch state into a {@link PlayerCommand} each frame.
 * Keyboard (arrow keys or WASD to move, Space/Up/Z to jump, X or Down/S to
 * fire/duck) always works; touch is the same on-screen {@link GameController}
 * (touchpad + two buttons) Battle City uses for its own controls (see
 * {@code BattleCityGameScene}'s constructor and
 * {@code MarioGameScreen.createGameController}) - button A is fire (Battle
 * City's own fire button), button B is jump (Battle City's own unused
 * button, repurposed here).
 *
 * <p>Unlike Battle City (whose tanks react to discrete per-move key events
 * via {@code GameControllerListener.KnobMoved}), Mario's movement is
 * continuous and held, so the knob/buttons are polled directly here every
 * frame ({@code getKnobPercentX/Y()}, {@code isButtonXPressed()}) instead of
 * going through a listener.
 */
public class MarioInputController {

    /**
     * How far off-center the knob must be dragged, as a fraction of its full
     * travel, to register as a direction - filters out thumb jitter near the
     * resting position turning into unintended input.
     */
    private static final float KNOB_DEADZONE = 0.3f;

    private final PlayerCommand command = new PlayerCommand();
    private final GameController gameController;

    private boolean jumpButtonWasPressed;
    private boolean fireButtonWasPressed;

    public MarioInputController(GameController gameController) {
        this.gameController = gameController;
    }

    public PlayerCommand poll() {
        boolean keyboardLeft = GameEngine.input.isKeyPressed(Input.Keys.LEFT)
                || GameEngine.input.isKeyPressed(Input.Keys.A);
        boolean keyboardRight = GameEngine.input.isKeyPressed(Input.Keys.RIGHT)
                || GameEngine.input.isKeyPressed(Input.Keys.D);
        boolean keyboardJump = GameEngine.input.isKeyJustPressed(Input.Keys.SPACE)
                || GameEngine.input.isKeyJustPressed(Input.Keys.UP)
                || GameEngine.input.isKeyJustPressed(Input.Keys.Z);
        boolean keyboardFire = GameEngine.input.isKeyJustPressed(Input.Keys.X);
        boolean keyboardDown = GameEngine.input.isKeyPressed(Input.Keys.DOWN)
                || GameEngine.input.isKeyPressed(Input.Keys.S);

        // The controller's buttons only expose "currently held"
        // (isButtonXPressed()), so jump/fire are edge-detected here, the same
        // way isKeyJustPressed works for keyboard.
        boolean fireButtonPressed = gameController.isButtonAPressed();
        boolean fireButtonJustPressed = fireButtonPressed && !fireButtonWasPressed;
        fireButtonWasPressed = fireButtonPressed;

        boolean jumpButtonPressed = gameController.isButtonBPressed();
        boolean jumpButtonJustPressed = jumpButtonPressed && !jumpButtonWasPressed;
        jumpButtonWasPressed = jumpButtonPressed;

        float knobX = gameController.getKnobPercentX();
        float knobY = gameController.getKnobPercentY();

        command.left = keyboardLeft || knobX < -KNOB_DEADZONE;
        command.right = keyboardRight || knobX > KNOB_DEADZONE;
        // Touchpad's own local Y axis (like every other Actor's) increases
        // upward within whatever coordinate system its parent hands it - but
        // that parent is this screen's Y-DOWN world/HUD camera (see
        // MarioGameScreen's class doc), where increasing Y means physically
        // lower on screen. So dragging the knob toward the physically lower
        // half of the touchpad increases, not decreases, getKnobPercentY() -
        // the reverse of Touchpad's own javadoc (written for an ordinary
        // Y-up camera, as Battle City uses). Verified by hand on-device.
        command.down = keyboardDown || knobY > KNOB_DEADZONE;
        command.jumpPressed = keyboardJump || jumpButtonJustPressed;
        command.firePressed = keyboardFire || fireButtonJustPressed;
        return command;
    }
}

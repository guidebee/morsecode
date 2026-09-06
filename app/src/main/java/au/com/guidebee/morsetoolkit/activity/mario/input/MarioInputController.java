package au.com.guidebee.morsetoolkit.activity.mario.input;

import com.guidebee.game.GameEngine;
import com.guidebee.game.Input;
import com.guidebee.game.ui.ImageButton;

/**
 * Polls keyboard/touch state into a {@link PlayerCommand} each frame.
 * Keyboard (arrow keys or WASD to move, Space/Up/Z to jump, X or Down/S to
 * fire/duck) always works; touch is a real on-screen control panel -
 * left/right/down/jump/fire {@link ImageButton}s built and positioned by
 * {@code MarioGameScreen}, the same procedurally-drawn-icon +
 * {@code ImageButton} + {@code addHUDComponent} pattern Battle City uses for
 * its own on-screen controls (see {@code BattleCityGameScene.createBackIcon}).
 */
public class MarioInputController {

    private final PlayerCommand command = new PlayerCommand();
    private final ImageButton leftButton;
    private final ImageButton rightButton;
    private final ImageButton downButton;
    private final ImageButton jumpButton;
    private final ImageButton fireButton;

    private boolean jumpButtonWasPressed;
    private boolean fireButtonWasPressed;

    public MarioInputController(ImageButton leftButton, ImageButton rightButton, ImageButton downButton,
                                 ImageButton jumpButton, ImageButton fireButton) {
        this.leftButton = leftButton;
        this.rightButton = rightButton;
        this.downButton = downButton;
        this.jumpButton = jumpButton;
        this.fireButton = fireButton;
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

        // Buttons only expose "currently held" (isPressed()), so jump/fire are
        // edge-detected here, the same way isKeyJustPressed works for keyboard.
        boolean jumpButtonPressed = jumpButton.isPressed();
        boolean jumpButtonJustPressed = jumpButtonPressed && !jumpButtonWasPressed;
        jumpButtonWasPressed = jumpButtonPressed;

        boolean fireButtonPressed = fireButton.isPressed();
        boolean fireButtonJustPressed = fireButtonPressed && !fireButtonWasPressed;
        fireButtonWasPressed = fireButtonPressed;

        command.left = keyboardLeft || leftButton.isPressed();
        command.right = keyboardRight || rightButton.isPressed();
        command.down = keyboardDown || downButton.isPressed();
        command.jumpPressed = keyboardJump || jumpButtonJustPressed;
        command.firePressed = keyboardFire || fireButtonJustPressed;
        return command;
    }
}

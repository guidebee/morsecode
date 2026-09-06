package au.com.guidebee.morsetoolkit.activity.mario.input;

import com.guidebee.game.GameEngine;
import com.guidebee.game.Input;

/**
 * Polls keyboard/touch state into a {@link PlayerCommand} each frame. Step
 * 4.1's "start with direct polling" per docs/MARIO_PORT_PLAN.md: keyboard
 * (arrow keys or WASD to move, Space/Up/Z to jump) is the fully-supported
 * scheme for this step's vertical slice. Touch gets a provisional three-zone
 * mapping (left third = left, right third = right, middle third = jump) -
 * good enough to smoke-test on a touch device, but a real virtual
 * {@code GameController} with on-screen buttons is still a later follow-up
 * (the plan explicitly defers it: "add ... later if on-screen buttons wanted").
 */
public class MarioInputController {

    private final PlayerCommand command = new PlayerCommand();

    public PlayerCommand poll() {
        boolean keyboardLeft = GameEngine.input.isKeyPressed(Input.Keys.LEFT)
                || GameEngine.input.isKeyPressed(Input.Keys.A);
        boolean keyboardRight = GameEngine.input.isKeyPressed(Input.Keys.RIGHT)
                || GameEngine.input.isKeyPressed(Input.Keys.D);
        boolean keyboardJump = GameEngine.input.isKeyJustPressed(Input.Keys.SPACE)
                || GameEngine.input.isKeyJustPressed(Input.Keys.UP)
                || GameEngine.input.isKeyJustPressed(Input.Keys.Z);

        boolean touchLeft = false;
        boolean touchRight = false;
        boolean touchJump = false;
        int screenWidth = GameEngine.graphics.getWidth();
        if (GameEngine.input.isTouched()) {
            int x = GameEngine.input.getX();
            if (x < screenWidth / 3) {
                touchLeft = true;
            } else if (x > screenWidth * 2 / 3) {
                touchRight = true;
            }
        }
        if (GameEngine.input.justTouched()) {
            int x = GameEngine.input.getX();
            if (x >= screenWidth / 3 && x <= screenWidth * 2 / 3) {
                touchJump = true;
            }
        }

        command.left = keyboardLeft || touchLeft;
        command.right = keyboardRight || touchRight;
        command.jumpPressed = keyboardJump || touchJump;
        return command;
    }
}

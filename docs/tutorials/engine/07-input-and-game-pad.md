# 7. Input and the On-Screen Game Pad

GGE exposes touch/key input two ways — raw polling for simple cases, and a ready-made
virtual joystick widget for anything that needs directional movement — and the two
bundled games each pick the one that fits.

## Direct polling: `GameEngine.input`

`GameEngine.input` (an `Input` instance) can simply be asked what's happening right now.
Flappy Bird's entire control scheme is one polled boolean, checked every frame from
`Bird.act()`:

```java
// Bird.java
if (input.isTouched()) {
    rotateBy(30 * delta);
    velocity.y = 250;              // "flap"
    // ...
} else {
    rotateBy(-20 * delta);
}
```

There's no gesture recognition, no down/up event bookkeeping — a single-button "is the
screen currently being touched" query is all a flap-to-fly game needs. `StartButton`
(the Play button on Flappy Bird's game-over screen) uses the same polling style for a
hit-test against its own bounds, rather than a click listener:

```java
// StartButton.java
if (input.isTouched()) {
    Vector3 touchPos = new Vector3(input.getX(), input.getY(), 0);
    getStage().getCamera().unproject(touchPos);   // screen pixels -> stage coordinates
    if (getBoundingAABB().contains(touchPos.x, touchPos.y)) {
        flappyBirdStage.removeStartButton();
        flappyBirdStage.startGame();
    }
}
```

`unproject` matters here: `input.getX()/getY()` are raw screen pixels, but the stage's
own coordinate system depends on its `Viewport` (see [14](14-camera-and-viewports.md)) —
`unproject` runs that transform in reverse so you can hit-test in the same coordinates
the actor's bounds are defined in.

## A virtual joystick: `GameController` / `Touchpad`

Battle City needs four-directional movement plus a fire button, so it renders an
on-screen `GameController` — a `WidgetGroup` combining a `Touchpad` (the draggable
joystick knob) with up to two buttons — skinned entirely from atlas regions:

```java
// BattleCityGameScene.java
GameController gameController = new GameController(
        (TextureRegionDrawable) touchpadSkin.getDrawable("touchBackground"),
        (TextureRegionDrawable) touchpadSkin.getDrawable("touchKnob"),
        (TextureRegionDrawable) touchpadSkin.getDrawable("shoot"),
        (TextureRegionDrawable) touchpadSkin.getDrawable("shoot_pressed"),
        (TextureRegionDrawable) touchpadSkin.getDrawable("virgin"),
        (TextureRegionDrawable) touchpadSkin.getDrawable("virgin_pressed"));
gameController.addGameControllerListener(this);
layerManager.setGameController(gameController);
```

`GameControllerListener` reports the knob's direction as one of eight compass values
(`GameControllerListener.Direction`: `NORTH`, `NORTHEAST`, `EAST`, ... `NONE`) and button
presses as `GameButton.BUTTON_A`/`BUTTON_B`. `BattleCityGameScene` implements the
listener and translates both into the same `KeyEvent` D-pad codes a physical gamepad or
keyboard would send:

```java
@Override
public void KnobMoved(Touchpad touchpad, Direction direction) {
    int gameAction = 0;
    switch (direction) {
        case EAST:  gameAction = KeyEvent.KEYCODE_DPAD_RIGHT; break;
        case NORTH: gameAction = KeyEvent.KEYCODE_DPAD_UP; break;
        case WEST:  gameAction = KeyEvent.KEYCODE_DPAD_LEFT; break;
        case SOUTH: gameAction = KeyEvent.KEYCODE_DPAD_DOWN; break;
    }
    playerTank.keyPressed(gameAction);
}

@Override
public void ButtonPressed(GameButton button) {
    if (button == GameButton.BUTTON_A) {
        playerTank.keyPressed(KeyEvent.KEYCODE_DPAD_CENTER);   // fire
    }
}
```

Routing both the virtual joystick *and* any real hardware D-pad/keyboard through the
same `KeyEvent` codes means `PlayerTank.keyPressed(int)` doesn't need to know or care
which input source triggered it — one code path handles both.

## Wiring the game pad into the scene graph

`layerManager.setGameController(gameController)` and (for `Screen`-level key/touch
routing more generally) `GameEngine.input.setInputProcessor(...)` are how these widgets
actually receive events. `BattleCityGameScene` swaps the active `InputProcessor` in and
out on `show()`/`hide()` so the game only grabs input while it's the visible screen:

```java
@Override
public void show() {
    savedInputProcessor = GameEngine.input.getInputProcessor();
    GameEngine.input.setInputProcessor(layerManager);
}

@Override
public void hide() {
    GameEngine.input.setInputProcessor(savedInputProcessor);
}
```

## Choosing between the two

Poll `GameEngine.input` directly for a single implicit action (tap-anywhere-to-flap,
tap-a-button hit-test). Reach for `GameController`/`Touchpad` once you need continuous,
directional movement with a recognizable on-screen control — it also gives you the
`KeyEvent`-code translation step above for free, so the same game logic works from
touch, a keyboard, or a physical gamepad without three separate code paths.

## Where to look

- `com.guidebee.game.Input`, `com.guidebee.game.ui.{GameController,Touchpad,GameControllerListener}` (`gameengine/src/main/java/com/guidebee/game/`).
- `Bird.java`, `StartButton.java` — direct polling.
- `BattleCityGameScene.java` — `GameController` setup and `GameControllerListener`.

---

[← Back to tutorial index](../README.md) · Previous: [6. TextureAtlas](06-texture-atlases.md) · Next: [8. Sound and Music](08-sound-and-music.md)

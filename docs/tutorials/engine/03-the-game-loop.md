# 3. The Game Loop

GGE's frame loop follows the standard `ApplicationListener` contract (`create`, `resize`,
`render`, `pause`, `resume`, `dispose`), the same shape Android's own `Activity`
lifecycle uses. `GamePlay` implements that interface once and delegates every call to
whichever `Screen` is current, so as a game author you mostly just implement `Screen`.

## Two shapes of `render(delta)`

Both games render every frame the same way in outline — clear the screen, advance game
state, draw — but split "advance" and "draw" differently depending on which scene-graph
API they use (see [12](12-microedition-game-api.md) for why).

**Flappy Bird** (`Stage`-based) keeps `render()` minimal — the `Stage` walks its own
actor tree for both steps:

```java
// FlappyBirdScene.java
@Override
public void render(float delta) {
    graphics.clearScreen(0, 0, 0.2f, 1);
    sceneStage.act();     // every Actor's act(delta) runs here
    sceneStage.draw();    // every Actor's draw(batch, alpha) runs here
}
```

**Battle City** (`LayerManager`-based) separates "advance the scene graph" from "run the
game's own state machine" as two explicit steps, because a lot of its game logic (enemy
spawn timing, win/lose, power-ups) isn't per-actor behavior — it's decided centrally:

```java
// BattleCityGameScene.java
@Override
public void render(float delta) {
    layerManager.act();          // every Sprite's act(delta) runs here
    applyGameLogic();            // centralized spawn/score/win-loss logic
    GameEngine.graphics.clearScreen(0.25f, 0.25f, 0.25f, 1);
    layerManager.draw(battleFieldX, battleFieldY);
}
```

Either approach is valid — pick per-actor `act()` overrides when behavior is naturally
local to one game object (Flappy Bird's `Bird` falls and flaps on its own), and a
centralized method when behavior depends on cross-cutting state (Battle City's enemy
spawn count depends on how many tanks are alive *and* the current level).

## `delta`: frame-rate-independent movement

Every `render`/`act` receives `delta` — the elapsed time in seconds since the last
frame — so movement is expressed as *rate* × `delta`, not a fixed per-frame step. `Bird.act()`
is a compact example:

```java
@Override
public void act(float delta) {
    velocity.add(0, GRAVITY, 0);
    velocity.scl(delta);
    position.add(MOVEMENT * delta, velocity.y, 0);
    // ...
    setY(position.y);
}
```

`GRAVITY` and `MOVEMENT` are constants expressed in units/second; multiplying by `delta`
is what makes the bird fall at the same real-world speed whether the device is rendering
at 30fps or 120fps.

## Pause and resume

`Screen.pause()`/`resume()` are called from the Android activity lifecycle (backgrounding
the app, an incoming call, etc.) and are where you should stop anything that shouldn't
keep running invisibly — music being the obvious one:

```java
// FlappyBirdScene.java
@Override
public void pause() {
    sceneStage.pauseGame();     // -> FlappyBirdStage.pauseGame() stops the music, sets a paused flag
}

@Override
public void resume() {
    sceneStage.resumeGame();
}
```

`FlappyBirdStage.act(delta)` checks that `paused` flag itself before running any game
logic, rather than the engine trying to freeze the whole `Stage` for you:

```java
@Override
public void act(float delta) {
    super.act(delta);
    if (!paused) {
        // collision checks, scoring, etc.
    }
}
```

## Where to look

- `com.guidebee.game.ApplicationListener` / `Screen` / `ScreenAdapter` — the interfaces
  (`gameengine/src/main/java/com/guidebee/game/`).
- `FlappyBirdScene.java`, `FlappyBirdStage.java` — the `Stage`-driven loop.
- `BattleCityGameScene.java` — the `LayerManager` + centralized-logic loop.

---

[← Back to tutorial index](../README.md) · Previous: [2. Package Tour](02-package-tour.md) · Next: [4. Graphics and the Batch](04-graphics-and-batch.md)

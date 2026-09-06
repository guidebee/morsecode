# 14. Camera and Viewports

Every `Stage` (and `LayerManager`, which is one) renders a fixed-size **virtual world**
through a `Camera`, and a `Viewport` decides how that virtual world maps onto whatever
size the physical device screen actually is. This indirection is why neither game's
gameplay code has to worry about the huge range of real Android screen sizes and aspect
ratios — actors are positioned in fixed virtual-world units, and the viewport handles
scaling to pixels.

## Two scaling strategies, one per game

Both games pick a virtual world size once (`Configuration.SCREEN_WIDTH/HEIGHT` = 800×450
for Flappy Bird; `gameWorldWidth`/`gameWoldHeight` = 420×240 for Battle City) and hand it
to a `Viewport` constructor — but each chooses a different scaling strategy for what
happens when that aspect ratio doesn't match the device's.

**Flappy Bird: `StretchViewport`** — stretches the world to fill the entire screen,
*not* preserving aspect ratio:

```java
// FlappyBirdScene.java
sceneStage = new FlappyBirdStage(
        new StretchViewport(Configuration.SCREEN_WIDTH, Configuration.SCREEN_HEIGHT),
        gamePlay);
```

From the engine source: *"A ScalingViewport that uses `Scaling.stretch` so it does not
keep the aspect ratio, the world is scaled to take the whole screen."* Flappy Bird's art
tolerates mild horizontal/vertical distortion, and filling the whole screen edge-to-edge
(no black bars) matters more for a game this visually simple.

**Battle City: `FitViewport`** — scales the world up to fit, preserving aspect ratio,
adding letterbox bars for any leftover space:

```java
// BattleCityGameScene.java
layerManager = new BattleCityLayerManager(new FitViewport(gameWorldWidth, gameWoldHeight));
```

From the engine source: *"...keeps the aspect ratio by scaling the world up to fit the
screen, adding black bars (letterboxing) for the remaining space."* Battle City's
tile-grid battlefield would look visibly warped if stretched non-uniformly — square
tiles need to stay square — so `FitViewport`'s letterboxing is the right trade-off:
some unused screen space, but no distortion.

## Resizing

Both scenes forward Android's `resize(width, height)` callback straight to their
viewport, which is what makes rotation and multi-window resizing work correctly without
either game handling it manually:

```java
// FlappyBirdScene.java
@Override
public void resize(int width, int height) {
    sceneStage.getViewport().update(width, height, false);
}
```

## Screen coordinates vs. world coordinates

Because the camera/viewport sits between the device screen and your actors' coordinate
space, raw touch coordinates (`GameEngine.input.getX()/getY()`, in screen pixels) are
*not* directly comparable to an actor's position (in virtual-world units) unless you
convert. `StartButton`'s hit-test (from [7. Input](07-input-and-game-pad.md)) shows the
conversion:

```java
Vector3 touchPos = new Vector3(input.getX(), input.getY(), 0);
getStage().getCamera().unproject(touchPos);   // now touchPos is in world coordinates
if (getBoundingAABB().contains(touchPos.x, touchPos.y)) { ... }
```

Skipping `unproject` here is the classic bug this API guards against — on a device
whose screen doesn't exactly match your virtual world size (which, given the two
strategies above, is nearly always), a raw pixel coordinate would only line up with
world-space actor bounds by coincidence.

## Choosing a viewport strategy

| Viewport | Aspect ratio | Screen usage | Good for |
|---|---|---|---|
| `StretchViewport` | Not preserved (may distort) | Fills the entire screen | Simple/forgiving art where full-bleed matters more than pixel-perfect shapes (Flappy Bird) |
| `FitViewport` | Preserved | Letterboxed (may show bars) | Grid/tile worlds or art where distortion would be visually wrong (Battle City) |

The engine also ships `ScreenViewport`, `ExtendViewport` and others under
`com.guidebee.game.camera.viewports` for cases neither bundled game needed (e.g.
`ExtendViewport`, which grows the world instead of scaling it) — the same
`ScalingViewport` base class both `StretchViewport` and `FitViewport` extend, so any of
them can be swapped in wherever a `Viewport` is expected.

## Where to look

- `com.guidebee.game.camera.viewports.{Viewport,ScalingViewport,StretchViewport,FitViewport}` (`gameengine/src/main/java/com/guidebee/game/camera/viewports/`).
- `FlappyBirdScene.java` — `StretchViewport` + `resize`.
- `BattleCityGameScene.java` — `FitViewport`.
- `StartButton.java` — `camera.unproject` for touch hit-testing.

---

[← Back to tutorial index](../README.md) · Previous: [13. UI Components and HUD](13-ui-components-and-hud.md) · Next: [15. Actions and Tweening](15-actions-and-tweening.md)

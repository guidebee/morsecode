# 15. Actions and Tweening

Both `Bird`'s flapping (from [3. The Game Loop](03-the-game-loop.md)) and everything in
`FlappyBirdStage` update their own position every frame by hand inside `act(delta)`.
That's the right call for physics-driven movement, but for anything that's really just
"animate this property from A to B over N seconds," hand-rolling per-frame interpolation
is more code than the problem needs. `com.guidebee.game.ui.actions.Actions` is GGE's
tween engine for exactly that case — declare the animation once, and the engine advances
it every frame for you.

## The building blocks

`Actions` is a static factory class — every method returns an `Action` object you attach
to an `Actor` (or UI widget, both extend the same base) with `addAction(...)`. Real
methods pulled from the engine source (`com.guidebee.game.ui.actions.Actions`):

- **Property actions** — `moveTo(x, y[, duration[, interpolation]])`, `rotateBy(amount[, duration])`,
  `scaleTo(x, y[, duration])`, `alpha(a[, duration])`, `fadeIn(duration)`, `color(color[, duration])`.
  Omitting `duration` makes the change instant.
- **Combinators** — `sequence(a, b, ...)` runs actions one after another; `parallel(a, b, ...)`
  runs them at the same time; `delay(duration)` waits; `forever(action)` repeats an
  action indefinitely; `repeat(count, action)` repeats it a fixed number of times;
  `after(action)` waits for an actor's other in-flight actions to finish first.

Because every actor's animation is declared as data (a tree of `Action` objects) rather
than procedural code, arbitrarily complex timelines compose out of a handful of calls
with no per-frame bookkeeping in your own classes at all.

## The one place this repo uses it: Flappy Bird's main menu

`MainWindow`'s bouncing logo is the sole `Actions` call site in this codebase, and it's
enough to show most of the combinators above at once:

```java
// MainWindow.java
import static com.guidebee.game.ui.actions.Actions.delay;
import static com.guidebee.game.ui.actions.Actions.forever;
import static com.guidebee.game.ui.actions.Actions.moveTo;
import static com.guidebee.game.ui.actions.Actions.rotateBy;
import static com.guidebee.game.ui.actions.Actions.sequence;

Image birdImage = new Image(uiSkin, "guidebeeit");
table.add(birdImage);
birdImage.addAction(
        forever(
                sequence(
                        moveTo(150, 100, 3f, Interpolation.circle),
                        delay(1.0f),
                        moveTo(500, 150, 3f, Interpolation.swingIn),
                        delay(1.0f),
                        rotateBy(360f, 2f),
                        delay(1.0f),
                        moveTo(500, 400, 3f, Interpolation.bounce),
                        rotateBy(360f, 2f),
                        delay(1.0f),
                        moveTo(150, 300, 3f, Interpolation.elastic),
                        rotateBy(360f, 2f),
                        delay(1.0f),
                        moveTo(550, 150, 3f, Interpolation.sine)
                )));
```

Reading this as a static import makes the call read close to plain English: *forever, do
this sequence* — move here over 3 seconds using a circular ease, wait a second, move
there with a swing-in ease, wait, spin 360°, wait, move again with a bounce ease, spin
again... The whole multi-second, multi-leg animation is one expression, with the engine
handling every intermediate frame.

## `Interpolation`: how a value gets from A to B

Every action that takes a `duration` optionally also takes an `Interpolation`
(`com.guidebee.math.Interpolation`) controlling the easing curve — linear by default if
omitted. The snippet above alone uses five different curves (`circle`, `swingIn`,
`bounce`, `elastic`, `sine`) — each gives the same "move from point A to point B" a
different *feel* (overshoot-and-settle for `bounce`/`elastic`, a smooth ease for
`circle`/`sine`). Swapping the `Interpolation` argument is the cheapest way to change how
an animation feels without touching its timing or waypoints.

## Where this fits next to hand-rolled `act()` logic

`Actions` and per-actor `act(delta)` logic aren't mutually exclusive — `Bird` could, in
principle, use `Actions` for a one-off cosmetic flourish (a death-spin, a menu
transition) while still hand-rolling its core gravity/flap physics in `act()`, exactly
as `MainWindow`'s logo uses `Actions` for pure decoration while the actual gameplay
screen it leads into (`FlappyBirdStage`) does its physics by hand. Reach for `Actions`
whenever an animation is a fixed, declarative timeline; keep hand-rolled `act()` code for
anything that has to react to physics, input, or other runtime state each frame.

## Where to look

- `com.guidebee.game.ui.actions.Actions` (`gameengine/src/main/java/com/guidebee/game/ui/actions/`) — the full method list.
- `com.guidebee.math.Interpolation` — the available easing curves.
- `flappybird/ui/MainWindow.java` — the only call site in this repo.

---

[← Back to tutorial index](../README.md) · Previous: [14. Camera and Viewports](14-camera-and-viewports.md) · Next: [16. Physics with Box2D](16-physics-with-box2d.md)

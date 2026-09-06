# 12. The Microedition Game API

`com.guidebee.game.microedition` mirrors the old Java ME `javax.microedition.lcdui.game`
package almost method-for-method (`LayerManager`, `Sprite`, `TiledLayer`) — GGE's answer
to "I have a MIDP game and don't want to redesign it from scratch." Battle City is built
entirely on this API, in contrast to Flappy Bird's `Stage`/`Actor` approach (see
[GAME_ENGINE.md](../../GAME_ENGINE.md#two-ways-to-build-a-game) for the side-by-side
framing). This page goes one level deeper into the three classes themselves.

## `LayerManager`: an ordered stack of layers

A `LayerManager` (itself a `Stage` subclass) holds a z-ordered list of `Layer`s —
`append`/`getLayerAt`/`getSize`/`remove` are the whole interface for managing that list:

```java
public void append(Layer layer)     // adds to the front (closest to the viewer)
public Layer getLayerAt(int i)
public int getSize()
public void remove(Layer layer)
```

Battle City appends its `BattleField` (a `TiledLayer`, see [9](09-tiled-layers-and-scenery.md))
and every `Sprite` (tanks, bullets, explosions, the game-over banner) to one
`BattleCityLayerManager`, and overrides `drawExtra(Batch)` to layer the score bar on top
of everything else after the normal draw pass — see
[4. Graphics and the Batch](04-graphics-and-batch.md#drawing-composite-graphics-directly-no-actor-needed).

## `Sprite`: frames, sequences, and pooling

A `Sprite` wraps a strip image cut into equal-sized frames — pass the frame size to the
constructor and indexing is automatic:

```java
public Sprite(TextureRegion img, int frameWidth, int frameHeight)
```

Two ways to pick which frame shows:

- **`setFrame(int)`** — jump straight to an absolute frame index. `Bullet` computes its
  frame directly from its direction (`setFrame(direction)`); `EnemyTank` from its type
  and turret rotation (`setFrame(type * 4 + 2)`).
- **`setFrameSequence(int[])`** — declare a named sub-sequence of frames once, then just
  `setFrame(0)`/advance through it, for a repeating animation. `Explosion` sets its
  frame sequence once per explosion "strength" (small vs. big):

```java
// Explosion.java
private void setStrength(int strength) {
    setFrameSequence(FRAME_SEQ[strength]);
}

public static Explosion explode(int x, int y, int strength) {
    for (int i = 0; i < POOL_SIZE; ++i) {
        Explosion explosion = EXPLOSIONS_POOL[i];
        if (!explosion.isVisible()) {
            explosion.setCenterPosition(x, y);
            explosion.setFrame(0);
            explosion.setStrength(strength);
            explosion.setVisible(true);
            return explosion;
        }
    }
    // ...
}
```

Notice `explode(...)` never calls `new Explosion(...)` — it pulls a hidden instance out
of a fixed-size `EXPLOSIONS_POOL` array and reactivates it. This object-pooling pattern
shows up throughout Battle City (`Bullet`, `Explosion`, `Powerup`, tanks) precisely
*because* `Sprite`s are cheap, reusable visual objects rather than heavyweight actors —
toggling `setVisible`/`setFrame`/`setCenterPosition` on an existing instance avoids
allocating and garbage-collecting a fresh object every time a bullet fires or a tank
dies, which matters far more on this API's original target (MIDP feature phones) than it
does today, but the pattern is still free performance.

`Sprite` also gets collision detection for free, as covered in
[11. Collision Detection](11-collision-detection.md#built-in-sprite-collision-battle-citys-bullets) —
`collidesWith(Sprite)`, `collidesWith(TiledLayer)`.

## `TiledLayer`: the grid

Covered in full in [9. Tiled Layers and Scenery](09-tiled-layers-and-scenery.md) —
`BattleField extends TiledLayer` directly, using `getCell`/`setCell` for both rendering
and collision.

## Why this API exists alongside `Stage`/`Actor`

`LayerManager` literally *is* a `Stage` subclass, and `Sprite` an `Actor` subclass (via
`Layer`) — this isn't a second, separate scene graph bolted on, it's the same
foundation with a MIDP-flavored API layered over it. That means you can mix styles
within one `LayerManager` if you need to (nothing stops you from appending a plain
`Actor` alongside `Sprite`s), though neither bundled game does — Battle City stays
consistently in the `Sprite`/`Layer` vocabulary throughout, which is exactly what makes
porting an existing MIDP game's structure over as directly as possible.

## Where to look

- `com.guidebee.game.microedition.{LayerManager,Sprite,TiledLayer,Layer}` (`gameengine/src/main/java/com/guidebee/game/microedition/`).
- `BattleCityGameScene.java` — the `LayerManager` setup.
- `Explosion.java`, `Bullet.java`, `tank/EnemyTank.java` — `Sprite` frame control and pooling.

---

[← Back to tutorial index](../README.md) · Previous: [11. Collision Detection](11-collision-detection.md) · Next: [13. UI Components and HUD](13-ui-components-and-hud.md)

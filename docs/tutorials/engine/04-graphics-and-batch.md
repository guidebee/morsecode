# 4. Graphics and the Batch

Underneath both `Stage` and `LayerManager`, all drawing eventually goes through a
`Batch` (`com.guidebee.game.graphics.Batch`) — a sprite batcher that accumulates
textured quads and flushes them to OpenGL ES in as few draw calls as possible. You
rarely construct a `Batch` yourself (the `Stage`/`LayerManager` owns one), but you'll
draw through it directly whenever an actor or HUD component needs custom rendering
beyond "draw my one texture region".

## Clearing the screen

`GameEngine.graphics.clearScreen(r, g, b, a)` is the first call in every `render()`:

```java
// FlappyBirdScene.java — a dark blue clear color
graphics.clearScreen(0, 0, 0.2f, 1);

// BattleCityGameScene.java — a neutral grey clear color
GameEngine.graphics.clearScreen(0.25f, 0.25f, 0.25f, 1);
```

## Drawing through an `Actor.draw()` override

Most actors never override `draw()` at all — `Actor`'s default implementation just
draws whatever `TextureRegion` was set via `setTextureRegion(...)`. `Bird` overrides it
only because it needs to pick between two different animations depending on state:

```java
// Bird.java
@Override
public void draw(Batch batch, float parentAlpha) {
    if (isBigger) {
        TextureRegion textureRegion = bigFlyAnimation.getKeyFrame(elapsedTime, true);
        batch.draw(textureRegion, getX(), getY());
    } else {
        super.draw(batch, parentAlpha);   // falls back to the default: draw the current region at (x, y)
    }
}
```

`batch.draw(region, x, y)` is the fundamental call: given a `TextureRegion` (a
rectangular slice of a loaded `Texture` — see [5](05-textures-and-regions.md)) and a
position, it queues a textured quad.

## Drawing composite graphics directly (no Actor needed)

Battle City's score bar isn't an actor at all — it's drawn straight onto the
`LayerManager`'s batch from a `drawExtra(Batch)` override, using `batch.draw(...)` in a
loop to build up a number display out of individual digit glyphs cut from one texture:

```java
// BattleCityGameScene.java
private void drawNumber(Batch g, int number, int x, int y) {
    TextureRegion imageNumber = imgNumberBlack;
    String strNumber = String.valueOf(number);
    int numberWidth = imageNumber.getRegionHeight();
    for (int i = 0; i < strNumber.length(); i++) {
        char ch = strNumber.charAt(i);
        int index = (ch - '0') % 10;
        TextureRegion oneNumber = new TextureRegion(imageNumber,
                index * numberWidth, 0, numberWidth, numberWidth);
        g.draw(oneNumber, x + i * numberWidth, y);
    }
}

class BattleCityLayerManager extends LayerManager {
    @Override
    public void drawExtra(Batch batch) {
        drawScoreBar(batch);   // calls drawNumber(...) for lives/level, plus icon draws
    }
}
```

This is the same technique Flappy Bird's `Score` HUD component uses for its own
digit display — see [5](05-textures-and-regions.md) for how `new TextureRegion(bigRegion, x, y, w, h)`
cuts out a sub-rectangle like `imgNumberBlack` above.

## When to reach for `Batch` directly

Use a plain `Actor`/`Sprite` (set a texture region, let the base class draw it) for
anything that's just "one image at a position." Reach for a raw `batch.draw(...)` call —
either from a `draw()` override or a `drawExtra()` hook — when you're compositing several
regions together procedurally, like a digit-by-digit number readout or (as in
[11](11-collision-detection.md) and the [main engine walkthrough](../../GAME_ENGINE.md#where-the-morse-code-actually-lives))
Battle City's brick walls, which are built at the tile level rather than as individual
actors.

## Where to look

- `com.guidebee.game.graphics.Batch` (`gameengine/src/main/java/com/guidebee/game/graphics/`).
- `Bird.java` — a `draw()` override choosing between two textures.
- `BattleCityGameScene.java` — `drawNumber`/`drawScoreBar`/`drawExtra`.

---

[← Back to tutorial index](../README.md) · Previous: [3. The Game Loop](03-the-game-loop.md) · Next: [5. Textures and TextureRegions](05-textures-and-regions.md)

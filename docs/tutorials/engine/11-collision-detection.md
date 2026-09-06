# 11. Collision Detection

The engine ships a general `Stage`-oriented collision helper package
(`com.guidebee.game.scene.collision`), but neither bundled game actually calls into it —
each reaches for whichever *simpler, cheaper* check fits its own world representation
instead. Between the two games you get three genuinely different techniques, which is
more instructive than any one of them alone.

## Rectangle-vs-point: Flappy Bird's pipes

Flappy Bird's world isn't a grid and doesn't need per-pixel accuracy — it just needs to
know whether the bird's center point has entered a pipe's rectangle. `Playground.isCollideWithTube`
builds a `Rectangle` for the current pipe pair and tests containment against the bird's
center:

```java
// Playground.java
public boolean isCollideWithTube(Bird bird) {
    float x = bird.getCenterX();
    float y = bird.getCenterY();
    for (int i = 0; i < tubePositionArray.size; i++) {
        TubePosition tubePosition = tubePositionArray.get(i);
        // ...
        topRect.x = tubePosition.posX;
        topRect.y = Configuration.SCREEN_HEIGHT - tubePosition.topTubeHeight;
        topRect.height = tubePosition.topTubeHeight;
        bottomRect.x = tubePosition.posX;
        bottomRect.height = tubePosition.bottomTubeHeight;

        boolean collide = topRect.contains(x, y) || bottomRect.contains(x, y);
        if (collide) {
            // ...
        }
    }
}
```

This is the same `com.guidebee.math.geometry.Rectangle` used for the `StartButton`
hit-test back in [7. Input and the On-Screen Game Pad](07-input-and-game-pad.md) —
`Rectangle.contains(x, y)` is a cheap, general-purpose tool worth reaching for any time
"is this point inside this box" is really the whole question.

### The collision that doesn't end the game

What happens on `collide` isn't always "you lose" — this is where Flappy Bird's Morse
teaching layer actually lives. Each pipe carries a `letterOfMorseCode`; if the bird hits
a pipe whose letter matches the player's current challenge letter, that's treated as a
*correct answer*, not a crash:

```java
if (challengeLetters[0] == tubePosition.letterOfMorseCode) {
    challengeLetters[0] = randomLetterOrNumber();
    tubePosition.deleted = true;
    extraScore += 5;
    return false;      // no collision — the pipe was "answered", not hit
}
```

Only a mismatched letter (or the bird already being small) turns the same rectangle
overlap into game-over. The [`ChallengeLetter` HUD](13-ui-components-and-hud.md#a-morse-specific-hud-component)
is what shows the player which letter they're aiming for.

## Built-in sprite collision: Battle City's bullets

The MIDP-style `Sprite` base class (see [12](12-microedition-game-api.md)) ships its own
collision methods for free — `collidesWith(Sprite)`, `collidesWith(TiledLayer)`, and
`collidesWith(TextureRegion, x, y)` — so anything extending `Sprite` gets hit-testing
against other sprites or against a tile grid without writing any geometry code at all.
`Bullet` uses the sprite-vs-sprite form to check whether it just hit a tank:

```java
// Bullet.java
if (friendly) {
    for (int i = 1; i < Tank.POOL_SIZE; i++) {
        EnemyTank enemy = (EnemyTank) Tank.getTank(i);
        if (enemy != null && enemy.isVisible() && collidesWith(enemy)) {
            enemy.explode();
            explode();
            return;
        }
    }
} else if (collidesWith(playerTank)) {
    playerTank.explode();
    explode();
}
```

Both sprites being visible is checked automatically inside `collidesWith` — an exploded
(hidden) tank stops colliding with anything with no extra code at the call site.

## Tile-grid collision: Battle City's walls

Moving actors (tanks, bullets) don't collide with wall *sprites* at all — walls are just
cells in `BattleField`'s `TiledLayer` grid (see [9](09-tiled-layers-and-scenery.md)), so
"did I hit a wall" is answered by converting a movement rectangle into tile coordinates
and checking the handful of cells it overlaps:

```java
// BattleField.java
public boolean containsImpassableArea(int x, int y, int width, int height) {
    // ... convert x/y/width/height to a small range of rows/columns ...
    for (int row = rowMin; row <= rowMax; ++row) {
        for (int column = columnMin; column <= columnMax; ++column) {
            int cell = getCell(column, row);
            if (cell < 0 || cell == BRICK_WALL || cell == CONCRETE_WALL) return true;
        }
    }
    return false;
}
```

This is dramatically cheaper than a rectangle-vs-rectangle test per wall segment once a
level has hundreds of wall tiles, which is exactly why tile-grid worlds use grid lookups
for static-geometry collision instead of per-object bounding boxes.

## Picking a technique

| Technique | Good for | Used by |
|---|---|---|
| `Rectangle.contains`/manual bounds | A handful of simple, moving hit-zones | Flappy Bird's pipes, `StartButton` |
| `Sprite.collidesWith(Sprite)` | Actor-vs-actor hits, free with the MIDP `Sprite` API | Battle City's bullets vs. tanks |
| Tile-grid lookup (`TiledLayer.getCell`) | Static, dense level geometry (walls, terrain) | Battle City's tanks/bullets vs. walls |

## Where to look

- `com.guidebee.math.geometry.Rectangle` (`gameengine/src/main/java/com/guidebee/math/geometry/`).
- `com.guidebee.game.microedition.Sprite#collidesWith` (`gameengine/src/main/java/com/guidebee/game/microedition/`).
- `Playground.isCollideWithTube`, `Bullet.java`, `BattleField.containsImpassableArea`.

---

[← Back to tutorial index](../README.md) · Previous: [10. Unmanaged Assets](10-unmanaged-assets.md) · Next: [12. The Microedition Game API](12-microedition-game-api.md)

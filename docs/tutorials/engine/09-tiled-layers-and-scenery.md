# 9. Tiled Layers and Scrolling Scenery

GGE offers two quite different ways to build a game world bigger than one screen, and —
usefully for this tutorial — each bundled game picked a different one.

> **Note:** the engine also ships a Tiled-editor-format map loader
> (`com.guidebee.game.maps.tiled.TiledMap`, with orthogonal and isometric renderers,
> wired up via `Stage.setScenery(...)`) for levels authored in the
> [Tiled](https://www.mapeditor.org/) map editor. Neither bundled game uses it — Battle
> City's grid is built procedurally at runtime (below), so there's no exported `.tmx`
> map in this repo to walk through. If you're authoring hand-drawn levels rather than
> generating them, that's the API to reach for instead of `TiledLayer`.

## Grid-based: `TiledLayer` (Battle City)

`com.guidebee.game.microedition.TiledLayer` is a grid of cells, each holding a small
integer tile index into a shared tile-image strip — the same model MIDP's
`javax.microedition.lcdui.game.TiledLayer` used. Its own javadoc puts the goal well:

> A TiledLayer is a visual element composed of a grid of cells that can be filled with a
> set of tile images. This class allows large virtual layers to be created without the
> need for an extremely large Image.

`BattleField` *is* one — it extends `TiledLayer` directly rather than composing one:

```java
// BattleField.java
public class BattleField extends TiledLayer {

    private static final int BRICK_WALL = 2;
    private static final int CONCRETE_WALL = 6;
    // ...

    public BattleField(int xTiles, int yTiles) {
        super(xTiles * 2, yTiles * 2, ResourceManager.getInstance().getTileImage(),
                ResourceManager.TILE_WIDTH / 2, ResourceManager.TILE_WIDTH / 2);
        createAnimatedTile(waterFrames[0][0]);
        createAnimatedTile(waterFrames[1][0]);
        // ...
    }
}
```

The `super(cols, rows, tileSetImage, tileWidth, tileHeight)` call sets up the whole grid
from one small strip image — the tile at grid index `n` is just the `n`th
`tileWidth`×`tileHeight` slice of that strip. `createAnimatedTile(...)` registers extra
tile indices that cycle between frames automatically (used here for animated water).

Reading and writing the grid is just `getCell`/`setCell` by column/row, which is what
makes tile-based collision cheap — `containsImpassableArea` answers "does this pixel
rectangle overlap a wall?" by converting pixels to a small range of tile coordinates and
checking each cell, rather than testing against individual wall actors:

```java
// BattleField.java
public boolean containsImpassableArea(int x, int y, int width, int height) {
    int TILE_WIDTH = ResourceManager.TILE_WIDTH / 2;
    int rowMin = y / TILE_WIDTH, rowMax = (y + height - 1) / TILE_WIDTH;
    int columnMin = x / TILE_WIDTH, columnMax = (x + width - 1) / TILE_WIDTH;
    for (int row = rowMin; row <= rowMax; ++row) {
        for (int column = columnMin; column <= columnMax; ++column) {
            int cell = getCell(column, row);
            if (cell < 0 || cell == BRICK_WALL || cell == CONCRETE_WALL) return true;
        }
    }
    return false;
}
```

Recall from the [main engine walkthrough](../../GAME_ENGINE.md#where-the-morse-code-actually-lives)
that `BattleField.readBattlefieldFromLedLetter()` fills this same grid, cell by cell,
from a dot-matrix rendering of two random letters *and* their Morse pattern — the
teaching content and the level geometry are the same data structure.

## Hand-rolled parallax scrolling (Flappy Bird)

Flappy Bird's scrolling background and ground don't use `TiledLayer` at all — they're
plain `Actor`s that redraw the same one or two `TextureRegion`s repeatedly, offset by a
counter that increments every frame, wrapping the offset back to zero once it exceeds
one tile width:

```java
// Background.java
if (!stopMoving) {
    offset += moveStep;
    offset %= backWidth;
}
for (int i = 0; i < widthSize + 1; i++) {
    batch.draw(backgroundTextureRegion, -offset + i * backWidth, Configuration.groundHeight);
}
```

`Playground` (the pipes) uses the same "just move the X position and check the wrap
condition" approach for each obstacle, just without a fixed tile grid — it tracks each
pipe's world-space `posX` directly and decrements it every frame:

```java
// Playground.java
tubePosition.posX -= moveStep;
```

## Picking one

Reach for `TiledLayer` when your world is naturally a grid that needs cheap
tile-granularity collision and lookups (a maze, a top-down arena with walls) — Battle
City is the clear case. Hand-roll offset-and-wrap drawing, as Flappy Bird does, when
your "world" is really just one or two looping strips scrolling behind (or past)
gameplay actors and doesn't need per-cell queries.

## Where to look

- `com.guidebee.game.microedition.TiledLayer` (`gameengine/src/main/java/com/guidebee/game/microedition/`).
- `com.guidebee.game.maps.tiled.TiledMap` — the unused Tiled-editor-format alternative.
- `BattleField.java` — the `TiledLayer` subclass and tile-based collision.
- `Background.java`, `Playground.java` — hand-rolled scrolling.

---

[← Back to tutorial index](../README.md) · Previous: [8. Sound and Music](08-sound-and-music.md) · Next: [10. Unmanaged Assets](10-unmanaged-assets.md)

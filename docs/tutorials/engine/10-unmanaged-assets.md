# 10. Unmanaged Assets

Everything in [6. TextureAtlas](06-texture-atlases.md) and [8. Sound and Music](08-sound-and-music.md)
goes through `GameEngine.assetManager` — the *managed* asset pipeline, which knows how
to decode each file type, caches by path, and loads asynchronously. Not every file a
game needs fits that pipeline, though. `GameEngine.files` (a `Files` instance) gives you
a plain `InputStream` onto any bundled asset, with none of the decoding or caching —
useful for text/JSON configuration, or binary formats the asset manager has no loader
for.

## Loading a UI skin's JSON directly

`Skin` (used throughout [13. UI Components and HUD](13-ui-components-and-hud.md)) is
built from a JSON file plus a `TextureAtlas`, and the JSON side is read as a raw,
unmanaged file rather than through `assetManager`:

```java
// BaseWindow.java
import static com.guidebee.game.GameEngine.files;

uiSkin = new Skin(files.internal("birdskin.json"),
        new TextureAtlas("birdmenu.atlas"));
```

`files.internal(path)` resolves a path relative to the app's bundled assets (the Android
equivalent of a classpath resource) and returns a `FileHandle` you can `.read()` as a
stream, or hand straight to APIs — like `Skin`'s constructor here — that accept a
`FileHandle`.

## Reading a raw binary resource

Battle City's `BattleField` has a second, more literal example: `readBattlefieldFromHZK(int)`
is a legacy level-loading path (superseded by the letter/Morse-pattern generation
described in the [main engine walkthrough](../../GAME_ENGINE.md#where-the-morse-code-actually-lives),
and no longer called from `newGame()`) that reads a Chinese bitmap font file (`hzk12`)
directly as raw bytes to extract glyph bitmaps:

```java
// BattleField.java
public void readBattlefieldFromHZK(int gameLevel) {
    InputStream is = GameEngine.files.internal("hzk12").read();
    int[] buffer = new int[24];
    is.skip(gameLevel * 24);
    for (int i = 0; i < 24; i++) buffer[i] = is.read();
    // ... decode buffer's bits into a 12x12 glyph bitmap
}
```

There's no `Class` type here at all — `hzk12` isn't an image, sound, or atlas the engine
has a loader for, just an opaque binary blob this code knows how to interpret itself.
This is the pattern to reach for whenever an asset's format is specific to your game
rather than something the engine's asset loaders already understand.

## SVG: available, unused here

The engine also bundles an SVG rendering path (`com.guidebee.game.engine.platform.svg`)
for resolution-independent vector art. Neither bundled game ships any `.svg` assets —
both use raster PNG atlases exclusively (see [6](06-texture-atlases.md)) — so there's no
in-repo call site to walk through here. If a game needs art that scales cleanly across
very different screen densities without shipping multiple raster resolutions, that
package is where to start; expect to load it as an unmanaged asset via `GameEngine.files`
the same way `birdskin.json` is loaded above, since it isn't one of `ResourceManager`'s
built-in managed asset types.

## Where to look

- `com.guidebee.game.Files` (`gameengine/src/main/java/com/guidebee/game/Files.java`).
- `BaseWindow.java` — `files.internal("birdskin.json")` for a `Skin`.
- `BattleField.readBattlefieldFromHZK` — raw binary asset reading.

---

[← Back to tutorial index](../README.md) · Previous: [9. Tiled Layers and Scenery](09-tiled-layers-and-scenery.md) · Next: [11. Collision Detection](11-collision-detection.md)

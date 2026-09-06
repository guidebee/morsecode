# 6. TextureAtlas

A `TextureAtlas` is a packed sprite sheet plus a text index describing where each named
region sits within it — one (or a few) large `Texture`s standing in for dozens of
individually-named images, avoiding per-sprite texture binds. This repo ships three:
`flappybird.atlas`, `battlecity.atlas`, and `morsecode.atlas` (all under
`app/src/main/assets/`), each paired with a `.png` of the same name.

## Anatomy of a `.atlas` file

`flappybird.atlas` is plain text — a header naming the backing `.png`, then one entry
per packed region:

```
flappybird.png
format: RGBA8888
filter: Nearest,Nearest
repeat: none
0
  rotate: false
  xy: 260, 516
  size: 28, 28
  orig: 28, 28
  offset: 0, 0
  index: -1
birdanimation
  rotate: false
  xy: 466, 170
  ...
```

Each block's first line is the region's **name** — the string you pass to
`TextureAtlas.findRegion(...)`. `filter: Nearest,Nearest` is why this repo's pixel-art
sprites stay crisp instead of blurring when scaled — nearest-neighbor filtering, not
the smoother (but blurrier for pixel art) linear filtering.

## Loading an atlas

Atlases are loaded like any other asset, through `GameEngine.assetManager` (a
`ResourceManager`), asynchronously, then fetched by name once loading completes.
`FlappyBirdGamePlay.loadAssets()` loads three atlases alongside the game's audio in one
batch:

```java
// FlappyBirdGamePlay.java
private void loadAssets() {
    assetManager.load("birdmenu.atlas", TextureAtlas.class);
    assetManager.load("flappybird.atlas", TextureAtlas.class);
    assetManager.load("morsecode.atlas", TextureAtlas.class);
    assetManager.load("music.mp3", Music.class);
    assetManager.load("sfx_wing.ogg", Sound.class);
    // ...
    assetManager.finishLoading();   // block until every load(...) call above has completed

    TextureAtlas textureAtlas = assetManager.get("flappybird.atlas", TextureAtlas.class);
    TextureRegion groundTextRegion = textureAtlas.findRegion("ground");
    Configuration.groundHeight = groundTextRegion.getRegionHeight();
}
```

`load(path, Class)` queues an asset; `finishLoading()` blocks until every queued asset
is ready (fine for a menu's worth of assets loaded once at startup — for a bigger game
you'd poll `assetManager.update()` and show a loading bar instead). Once loaded, any
other class calls `assetManager.get(path, Class)` to fetch the same shared instance —
notice `Bird`, `Background` and `Score` (from earlier pages) all independently call
`assetManager.get("flappybird.atlas", TextureAtlas.class)` rather than being handed a
reference; the `ResourceManager` is a process-wide cache keyed by path.

## `morsecode.atlas`: an atlas dedicated to teaching content

`morsecode.atlas` is worth calling out specifically — it's not game art at all, but a
Morse-specific atlas: dot/dash glyphs and a-z/0-9 letter tiles, packed once and reused
by `ChallengeLetter`'s HUD (renders the current target letter's Morse pattern) — see
[13. UI Components and HUD](13-ui-components-and-hud.md#a-morse-specific-hud-component).
Splitting it out from `flappybird.atlas` keeps the Morse-teaching visuals reusable
independently of any one game's art style.

## `findRegion` vs. cutting sub-regions

`findRegion("name")` looks up a whole packed region by name. If you need a *piece* of a
packed region (an animation frame, a digit out of a number strip), you cut it with
`new TextureRegion(atlasRegion, x, y, w, h)` as covered in
[5. Textures and TextureRegions](05-textures-and-regions.md) — packing and sub-cutting
are complementary, not alternatives.

## Where to look

- `com.guidebee.game.graphics.TextureAtlas` (`gameengine/src/main/java/com/guidebee/game/graphics/`).
- `app/src/main/assets/{flappybird,battlecity,morsecode}.atlas` — the actual packed data.
- `FlappyBirdGamePlay.loadAssets()` — the loading pattern.

---

[← Back to tutorial index](../README.md) · Previous: [5. Textures and TextureRegions](05-textures-and-regions.md) · Next: [7. Input and the On-Screen Game Pad](07-input-and-game-pad.md)

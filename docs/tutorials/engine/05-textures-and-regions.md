# 5. Textures and TextureRegions

## Texture

A `Texture` (`com.guidebee.game.graphics.Texture`) is one decoded image uploaded whole
into GPU memory. Loading and holding onto a `Texture` is comparatively expensive — it's
a full GPU upload and, on the OpenGL ES versions this engine targets, ideally has
power-of-two dimensions. Neither game in this repo constructs a `Texture` directly by
hand very often; almost everything goes through `TextureAtlas` instead (next page),
which manages one or more shared `Texture`s for you.

## TextureRegion

A `TextureRegion` is a rectangular sub-slice of a `Texture` — coordinates plus width and
height, no pixel data of its own. This is the key performance idea behind sprite
sheets: load *one* big texture, then cut out as many regions from it as you need,
without any additional GPU uploads or texture-binding switches per sprite.

Both games use this in two ways: atlas-provided regions (looked up by name — the
subject of [6](06-texture-atlases.md)), and **regions cut from other regions**, for
sprite-sheet animation frames or digit fonts.

### Cutting animation frames out of one region

`Bird` looks up one wide `TextureRegion` containing three flap-animation frames side by
side, then cuts each frame out as its own sub-region:

```java
// Bird.java
TextureAtlas textureAtlas = assetManager.get("flappybird.atlas", TextureAtlas.class);
birdTextRegion = textureAtlas.findRegion(birdColor + "birdanimation");   // one wide strip

Array<TextureRegion> keyFrames = new Array<TextureRegion>();
for (int i = 0; i < SPRITE_FRAME_SIZE; i++) {
    TextureRegion textureRegion = new TextureRegion(birdTextRegion,
            i * SPRITE_WIDTH, 0, SPRITE_WIDTH, SPRITE_HEIGHT);
    keyFrames.add(textureRegion);
}
flyAnimation = new Animation(tick, keyFrames);
```

`new TextureRegion(parentRegion, x, y, width, height)` — the constructor used here — cuts
a rectangle *relative to another region*, not the raw texture, so cutting frames out of
an atlas-provided region works exactly like cutting them out of a whole texture.

`com.guidebee.game.graphics.Animation` then does the frame-timing work:
`flyAnimation.getKeyFrame(elapsedTime, true)` (the `true` means loop) returns whichever
frame should be showing at a given elapsed time, given the tick duration passed to the
constructor.

### Cutting a digit font out of a numbers strip

Flappy Bird's `Score` HUD component and Battle City's score bar (from
[4](04-graphics-and-batch.md)) both use the same trick for rendering numbers without a
real font: an atlas region containing digits 0–9 side by side gets cut into ten
same-width sub-regions once, up front:

```java
// Score.java
TextureRegion numbers = textureAtlas.findRegion("numbers");
numberDrawables = new TextureRegionDrawable[11];
for (int i = 0; i < 10; i++) {
    numberDrawables[i] = new TextureRegionDrawable(
            new TextureRegion(numbers, i * 14, 0, 14, 14));   // each digit is 14x14
}
```

Setting `thousands`/`hundreds`/`tens`/`units` `Image`s to whichever `numberDrawables[i]`
matches the current score digit is the entire "font" — no glyph rendering, just region
selection.

## Where to look

- `com.guidebee.game.graphics.Texture` / `TextureRegion` / `Animation` (`gameengine/src/main/java/com/guidebee/game/graphics/`).
- `Bird.java` — cutting animation frames from a strip.
- `Score.java`, `BattleCityGameScene.drawNumber` — cutting a digit font from a strip.

---

[← Back to tutorial index](../README.md) · Previous: [4. Graphics and the Batch](04-graphics-and-batch.md) · Next: [6. TextureAtlas](06-texture-atlases.md)

# 2. Package Tour

Before diving topic by topic, it helps to know where things live. This page is a map of
`com.guidebee.*` (the engine, in `gameengine/src/main/java/`) cross-referenced against
where Flappy Bird and Battle City actually use each package, so later tutorials can
point at a package name and you'll already know roughly what's in it.

| Package | What's in it | Used by our games as |
|---|---|---|
| `com.guidebee.game` | The static `GameEngine` context (`graphics`, `input`, `audio`, `assetManager`, ...), `GamePlay`, `Screen`, `Application` | The plumbing every screen and actor touches — see [1](01-project-setup-and-lifecycle.md) |
| `com.guidebee.game.activity` | `GameActivity` and friends — the Android entry points | `FlappyBirdGameActivity`, `BattleCityGameActivity` |
| `com.guidebee.game.scene` | `Stage`, `Actor`, `Group` — the higher-level, libGDX-style scene graph | Flappy Bird's entire gameplay screen (`FlappyBirdStage`, `Bird`, `Background`, `Playground`, ...) |
| `com.guidebee.game.microedition` | `LayerManager`, `Sprite`, `TiledLayer` — the lower-level, MIDP `javax.microedition.lcdui.game`-style API | Battle City's entire gameplay screen (`BattleCityLayerManager`, `Tank`, `Bullet`, `BattleField`) |
| `com.guidebee.game.ui` | `Table`, `Button`, `Image`, `Skin`, `Window`, `Touchpad`, `GameController`, plus `ui.actions` (the tween engine) | Both games' menu screens (`MainWindow`, `BaseWindow`) and Battle City's on-screen joystick |
| `com.guidebee.game.graphics` | `Texture`, `TextureRegion`, `TextureAtlas`, `Animation`, `Batch` | Every sprite in both games |
| `com.guidebee.game.audio` | `Sound`, `Music` | `Helper.playSound`/`playMusic` in Flappy Bird, `ResourceManager.playSound` in Battle City |
| `com.guidebee.game.camera` / `camera.viewports` | `Camera`, `StretchViewport`, `FitViewport`, `ScreenViewport`, ... | `StretchViewport` (Flappy Bird), `FitViewport` (Battle City) — see [14](14-camera-and-viewports.md) |
| `com.guidebee.game.physics` | The Box2D Java/JNI bindings — `World`, `Body`, `Fixture`, joints | Not used by either bundled game — see [16](16-physics-with-box2d.md) |
| `com.guidebee.game.entity` | The Entity System Framework (entities/directors/signals) | Not used by either bundled game; both use `Actor`/`Sprite` subclassing instead |
| `com.guidebee.game.maps.tiled` | `TiledMap` support for maps authored in the Tiled editor, with orthogonal/isometric renderers | Not used — Battle City builds its grid procedurally with the simpler `microedition.TiledLayer` instead (see [9](09-tiled-layers-and-scenery.md)) |
| `com.guidebee.game.engine.platform.svg` | SVG vector rendering | Not used — both games ship raster (PNG) atlases |
| `com.guidebee.game.scene.collision` | AABB/shape collision helpers for the `Stage` API | Not used directly — both games implement their own simpler rectangle/tile checks (see [11](11-collision-detection.md)) |
| `com.guidebee.math` | `Vector2`/`Vector3`, `Matrix4`, `Interpolation`, geometry (`Rectangle`, ...) | Everywhere — e.g. `Bird`'s `Vector3 velocity`, `Interpolation.circle` in menu `Actions` |

A quick way to read this table: **the left half of `com.guidebee.game` (scene, graphics,
audio, ui) is exercised heavily by both games; the right half (physics, entity, tiled
maps, SVG) ships in the engine but isn't something either bundled game happens to need.**
That's not a gap in the games — Flappy Bird and Battle City are simple enough that they
don't need full rigid-body physics or an entity-component framework — but it does mean
a few later tutorials in this series ([9](09-tiled-layers-and-scenery.md) and
[16](16-physics-with-box2d.md)) can't walk through *our* code for those specific
sub-features and say so up front.

---

[← Back to tutorial index](../README.md) · Previous: [1. Project Setup](01-project-setup-and-lifecycle.md) · Next: [3. The Game Loop](03-the-game-loop.md)

# Guidebee Game Engine Tutorials

A from-scratch tutorial series for the Guidebee Game Engine (GGE) module bundled in
this repo (`gameengine/`). Unlike the upstream engine's own tutorials — which taught
each topic through standalone demo games (RainDrop, a UI demo, an Actions demo, a Box2D
demo) hosted on the now end-of-life `GuidebeeGameEngine` org's wikis, with diagrams on a
blog that's since gone offline — **every page here is grounded entirely in the two games
that actually ship in this repo: Flappy Bird and Battle City.** Every code sample is
quoted from a real file in `app/` or `gameengine/`, and every image is either a live
screenshot of the bundled apps or omitted rather than left pointing at a dead link.

If you want the short version first, read
[`docs/GAME_ENGINE.md`](../GAME_ENGINE.md) — a single-page tour covering the same two
games. Come here for a deeper, topic-by-topic walk through the engine's API surface.

## Engine tutorials

1. [Project Setup and the Application Lifecycle](engine/01-project-setup-and-lifecycle.md) — `GameActivity` → `GamePlay` → `Screen`
2. [Package Tour](engine/02-package-tour.md) — a map of `com.guidebee.*`, and what each game actually uses
3. [The Game Loop](engine/03-the-game-loop.md) — `render`/`act`/`draw`, `delta`, pause/resume
4. [Graphics and the Batch](engine/04-graphics-and-batch.md) — `Batch`, `clearScreen`, drawing composite graphics
5. [Textures and TextureRegions](engine/05-textures-and-regions.md) — cutting sprite-sheet frames and digit fonts
6. [TextureAtlas](engine/06-texture-atlases.md) — packed sprite sheets, `assetManager`, the Morse-specific atlas
7. [Input and the On-Screen Game Pad](engine/07-input-and-game-pad.md) — touch polling vs. `GameController`/`Touchpad`
8. [Sound and Music](engine/08-sound-and-music.md) — `Sound`/`Music`, settings-aware playback wrappers
9. [Tiled Layers and Scrolling Scenery](engine/09-tiled-layers-and-scenery.md) — `TiledLayer` vs. hand-rolled parallax
10. [Unmanaged Assets](engine/10-unmanaged-assets.md) — `GameEngine.files`, raw resource loading, SVG (unused)
11. [Collision Detection](engine/11-collision-detection.md) — three techniques, side by side
12. [The Microedition Game API](engine/12-microedition-game-api.md) — `LayerManager`/`Sprite` in depth
13. [UI Components and HUD](engine/13-ui-components-and-hud.md) — `Window`/`Skin`/`Table`, HUD widgets
14. [Camera and Viewports](engine/14-camera-and-viewports.md) — `StretchViewport` vs. `FitViewport`
15. [Actions and Tweening](engine/15-actions-and-tweening.md) — the declarative animation API
16. [Physics with Box2D](engine/16-physics-with-box2d.md) — reference only; not used by either bundled game

## A note on coverage

Two topics in this series (9 and 16) don't have a live example to walk through, because
neither bundled game happens to need that part of the engine — Battle City doesn't use
the Tiled-editor `TiledMap` format, and neither game uses Box2D physics. Both pages say
so explicitly and fall back to documenting the real engine API instead of pretending an
example exists. Everything else is a working, running feature of Flappy Bird or Battle
City today.

## Relation to the rest of the docs

- [`docs/GAME_ENGINE.md`](../GAME_ENGINE.md) — the single-page overview; start there.
- [`../../README.md`](../../README.md) — the main project README (features, build instructions).

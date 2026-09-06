# Guidebee Game Engine tutorials (archived)

This is a full mirror of the tutorial series that used to live on the wikis of the
[GuidebeeGameEngine](https://github.com/GuidebeeGameEngine) GitHub org — the
`GuidebeeGameEngine/Raindrop`, `GuidebeeGameEngine/Box2D`, `GuidebeeGameEngine/UIComponents`
and `GuidebeeGameEngine/ActionDemo` demo repos. That org's projects are end-of-life and
its wikis may disappear entirely, so the full text and code samples are preserved here,
alongside the [engine walkthrough](../GAME_ENGINE.md) grounded in this repo's own two
games (Flappy Bird / Battle City).

**A few things no longer work, unavoidably, because the original hosting is gone:**

- Diagrams hosted on the old `guidebee.com.au` WordPress blog (linked as
  `i0.wp.com`/`i1.wp.com`/`i2.wp.com` image URLs) are dead — the blog itself is offline.
  The surrounding text still describes what each diagram showed.
- Links to `guidebeegameengine.com/javadoc/...` (the hosted Javadoc) are dead for the
  same reason.
- The `compile 'com.guidebee:game-engine:...'` Gradle dependency shown in several pages
  pointed at Bintray/JCenter, which [shut down in 2021](https://jfrog.com/blog/into-the-sunset-bintray-jcenter-gocenter-and-chartcenter/) —
  it can no longer be resolved. This doesn't affect this repo, which builds the engine
  from source in `gameengine/` instead of pulling a published artifact.
- Embedded YouTube demo videos and links into other `GuidebeeGameEngine/*` source repos
  are left as-is; most still resolve since they're hosted on YouTube/GitHub rather than
  the defunct blog.

Everything else — the prose and every Java code sample — is unchanged from the original
wiki source, only reformatted into plain Markdown files and re-linked to point at each
other locally instead of back to GitHub wiki URLs.

## Engine tutorials

Originally the `GuidebeeGameEngine/Raindrop` wiki, built around a simple libGDX-style
"catch the raindrops" game:

1. [Android Gradle Project](engine/Android-Gradle-Project.md)
2. [Basics](engine/Basics.md)
3. [Packages](engine/Packages.md)
4. [Game Logic](engine/Game-Logic.md)
5. [Basic Graphics](engine/Basic-Graphics.md)
6. [Texture & TextureRegion](engine/Texture-&-TextureRegion.md)
7. [TextureAtlas](engine/TextureAtlas.md)
8. [Handling Input and On Screen Game Pad](engine/Handling-Input-and-On-Screen-Game-Pad.md)
9. [Sound and Music](engine/Sound-and-Music.md)
10. [Scenery and TiledMap](engine/Scenery-and-TiledMap.md)
11. [SVG Image and Unmanaged Assets](engine/SVG-Image-and-Unmanaged-Assets.md)
12. [Collision Detection](engine/Collision-Detection.md)
13. [Microedition Game API](engine/Microedition-Game-API.md)
14. [Bring Physics to your Game World](engine/Bring-Physics-to-your-Game-World.md)
15. [UI Components and HUD Components](engine/UI-Components-and-HUD-Components.md)
16. [Camera and Viewport basics](engine/Camera-and-Viewport-basics.md)
17. [User Interface and UI Components](ui-components.md) — originally the
    `GuidebeeGameEngine/UIComponents` wiki (`Window`, `Skin`, `Table`/`Button` layout).
18. [Animate With Actions](actions.md) — originally the `GuidebeeGameEngine/ActionDemo`
    wiki (the `Actions`/tween API).

## Box2D tutorials

Originally the `GuidebeeGameEngine/Box2D` wiki:

1. [Introduction](box2d/Introduction.md)
2. [Basic Concepts](box2d/Basic-Concepts.md)
3. [Control Player](box2d/Control-Player.md)
4. [Body Types](box2d/Body-Types.md)
5. [Shape Types](box2d/Shape-Types.md)
6. [Apply Forces and Impulses to Bodies](box2d/Apply-Forces-and-Impulses-to-Bodies.md)
7. [Collision Detection and Collision Filter](box2d/Collision-Detection-and-Collision-Filter.md)
8. [Sensors](box2d/Sensors.md)
9. [Ray Casts](box2d/Ray-Casts.md)
10. [Joints](box2d/Joints.md)
11. [More on Bodies](box2d/More-on-Bodies.md)
12. [The world](box2d/The-world.md)

## Relation to this repo

These tutorials teach the engine in the abstract, using their own standalone demo games
(RainDrop, a Box2D demo, a UI demo, an Actions demo) — none of it is specific to Morse
Code Toolkit. If you want engine concepts explained against code that actually ships in
*this* repo, start with [`docs/GAME_ENGINE.md`](../GAME_ENGINE.md) instead, which walks
through the same `Stage`/`Actor`, `LayerManager`/`Sprite`, `Actions` and input APIs using
Flappy Bird and Battle City as the examples. Come here for the topics that walkthrough
doesn't cover in depth — Box2D physics, TiledMap/scenery, SVG assets, cameras/viewports.

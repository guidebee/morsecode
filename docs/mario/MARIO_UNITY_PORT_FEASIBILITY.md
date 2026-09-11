# Mario / Ampere's Run: Unity Port Feasibility and Platformer Roadmap

**Research date:** 2026-09-11

**Code baseline:** `0d43eac` in this repository

**Status:** feasibility report and proposed implementation strategy; no Unity port has been implemented.

**Scope:** the existing Android Mario game, its extracted Java platformer layer, shipped content, Android host integration, and reuse for subsequent games.

## Executive recommendation

**Yes: this game can be ported to Unity, and Unity is a strong technical fit for a growing family of 2D platformers.** Sprite sheets are fully supported. The difficult work is preserving this game's movement and interaction semantics, then integrating the Unity runtime into the existing Morse Code Toolkit application.

Treat the project as a **C# gameplay migration with reusable content and algorithms**, rather than an import of the existing Java game. Unity replaces Guidebee's rendering, scene/UI, audio, input, and application lifecycle APIs; the game-specific rules still need implementation.

Recommended approach:

1. Start with a **10–15 engineer-day vertical slice**, including an early Android-host experiment.
2. Retain the current custom tile/AABB simulation as C# logic, with a deliberate 60 Hz simulation policy. Use Unity `SpriteRenderer`, Tilemap, UI, audio, and asset tooling around it.
3. Import the existing **55 JSON levels** through a compatibility importer. Preserve their unusual field meanings and level-transition graph.
4. Build a small, composable platformer package as the port progresses. Validate reuse by building a **mechanically different second sample**, not merely a palette swap.
5. Adopt Unity for the production migration once the slice demonstrates acceptable gameplay fidelity, Android integration, launch time, and memory use.

### How complicated is it?

| Deliverable | Assessment | Planning range |
|---|---|---|
| Display and animate existing sprite sheets | Low complexity | A small part of the slice; allow 1–3 days for representative imports and orientation checks |
| Playable representative Unity slice | Medium complexity | **10–15 engineer-days** |
| Full 55-level standalone Unity port, reusable package, second sample | Medium–high complexity | **58–100 engineer-days**, including contingency |
| Full port embedded in the existing Android app, reusable package, second sample | High overall integration complexity | **68–119 engineer-days**, including contingency |

For one experienced full-time developer, the embedded range is approximately **14–24 working weeks**, or **3.5–6 months**, before allowing for holidays, interruptions, or external content-production delays. The detailed work breakdown and assumptions are in [§11](#11-effort-estimate-and-staffing). These are engineering estimates, not measured implementation times.

**Decision in one sentence:** Unity is worth investigating for the planned multi-game future, but the first investment should prove the custom controller and the Android embedding cost before committing to the full port.

## Contents

- [1. Evidence and current-state findings](#1-evidence-and-current-state-findings)
- [2. Unity capabilities and recommended baseline](#2-unity-capabilities-and-recommended-baseline)
- [3. Migration strategy comparison](#3-migration-strategy-comparison)
- [4. Sprite sheets, atlases, and higher-resolution art](#4-sprite-sheets-atlases-and-higher-resolution-art)
- [5. Level data and authoring pipeline](#5-level-data-and-authoring-pipeline)
- [6. Physics, timing, collision, and gameplay fidelity](#6-physics-timing-collision-and-gameplay-fidelity)
- [7. Reusable architecture for subsequent games](#7-reusable-architecture-for-subsequent-games)
- [8. Android application integration](#8-android-application-integration)
- [9. Performance, memory, and delivery](#9-performance-memory-and-delivery)
- [10. Implementation phases and acceptance gates](#10-implementation-phases-and-acceptance-gates)
- [11. Effort estimate and staffing](#11-effort-estimate-and-staffing)
- [12. Regression and validation strategy](#12-regression-and-validation-strategy)
- [13. Risks and decisions to settle](#13-risks-and-decisions-to-settle)
- [14. Relationship to the reskin and existing plans](#14-relationship-to-the-reskin-and-existing-plans)
- [15. Final assessment](#15-final-assessment)
- [16. Research references](#16-research-references)

## 1. Evidence and current-state findings

### 1.1 What was examined

The review covered the Mario source tree, the extracted `platformer/` source tree, all ten existing Markdown documents in `docs/mario/` at varying depth, the atlas packer, the sprite manifest, representative level JSON, and the Android build configuration. Detailed source reads focused on the player, frame loop, level loading/registry, collision resolvers, representative enemy/platform/spawner behavior, resources, input, and persistence.

Official Unity manuals, package documentation, release-support information, and pricing pages were retrieved during the research. Numbered references such as [U1] are defined in [§16](#16-research-references).

**Evidence boundary:** this is a source-and-documentation assessment. No Unity Editor project, device benchmark, new gameplay recording, or compiled Unity build was produced. Historical reports of successful device testing come from the repository's implementation record, not a new test performed for this report.

### 1.2 Measured inventory

| Area | Current inventory | Porting significance |
|---|---|---|
| `activity/mario/` | **97 Java files; 10,311 physical lines** | Substantial but bounded gameplay implementation |
| `platformer/` | **22 Java files; 1,564 physical lines** | Some reusable boundaries already exist |
| Combined runtime source reviewed | **119 files; 11,875 physical lines** | Counts include blank lines and extensive comments; they are not executable-code estimates |
| Largest orchestration class | `MarioGameScreen.java`: **1,075 lines** | Setup, UI, audio, simulation, scripted completion, and transitions should be split in Unity |
| Player | `Player.java`: **1,060 lines**, plus `PowerStateActor` | The highest-risk gameplay migration |
| Levels | **55 JSON files** | Importable data, not 55 scenes that must be rebuilt manually |
| Level distribution | 36 main levels, 14 bonus areas, 5 beanstalk-family levels, as catalogued in the level atlas | Regression must include transitions and special rooms, not just main levels |
| Placed-content dispatch | **56 explicit registrations** across six registries, plus two static-terrain types | A finite conversion checklist; dynamically spawned actors add behaviors beyond these keys |
| Sprite resources | **122 source `AssetSpec` entries + 7 generated terrain composites = 129 logical regions** | These are sheets/regions, not 129 individual animation frames or unique characters |
| Packed graphics | Five atlas descriptors, six 2048×2048 PNG pages: two common pages and one per Ground/UnderGround/Castle/Sea theme | Unity can repack the underlying sprites |
| Audio | **23 SFX + 5 music tracks** in `MarioResourceManager` | Straightforward asset import, with event timing and music switching to preserve |

Source-file/line counts were collected with `git grep -c '^'` over the two tracked Java trees. Registry and packer counts were checked against explicit registrations/declarations. Level count was checked from the files containing `levelNumber` under the shipped level directory.

### 1.3 The platformer extraction already exists

The current structure is:

```text
Android application
  └─ Guidebee Game Engine
       └─ Java platformer layer
            └─ Mario-specific player, actors, rules, and content
```

Examples actually present:

- `TileCollisionSource`, `TileMetrics`, `TileWorld`, `TileMovement`.
- `CameraFollow`, `OscillatorClock`, `GameContext`.
- `PowerStateActor`, `CollisionPipeline`, `FrameResolver`.
- `PlatformerCommand`, `TouchOrKeyboardInput`.
- `ScoreLivesState`, `LevelProgressState`, HUD/menu/debug infrastructure.

This is useful design work to carry forward. It is **not yet an engine-independent library**:

- `TileWorld` extends Guidebee `TiledLayer`.
- `PowerStateActor` extends Guidebee `Layer` and uses texture/rendering types.
- Input, HUD, menus, and preferences depend on Guidebee APIs.
- `TileHandler`, `TileTypeRegistry`, and `LevelWarpPanel` still import Mario's `LevelDefinition`.
- `MarioContext` exposes a static, process-wide facade around the current game session.
- `PlayerPowerState` and some trigger/offset geometry still contain fixed dimensions, despite the broader tile-size extraction.

**Consequence:** port the architectural ideas and useful algorithms; replace the rendering inheritance and finish the content/core separation in C#.

### 1.4 Important discrepancies between the documents and current code

The documents are valuable, but several describe earlier stages. Use the source at the recorded baseline as the implementation authority.

| Documentation claim or implication | Current-source finding | Migration implication |
|---|---|---|
| Architecture document starts with “proposal, nothing implemented” | `PLATFORMER_ENGINE_IMPLEMENTATION.md` records Phases A–G complete; the classes and imports exist | Do not estimate a fresh Java extraction as a prerequisite |
| Mechanics diagram shows `Player extends Layer`, `MarioWorld extends TiledLayer`, old input/HUD classes | They now use `PowerStateActor`, `TileWorld`, and shared platformer input/HUD | Read through the new bases when translating behavior |
| Mechanics frame-loop example places axe/checkpoint detection after drawing | `render()` calls `updateLevelCompletion()` inside the update loop, before `draw()` | Preserve the current ordering, not the old pseudocode |
| Old plan headers say the port/Phase 2 is unimplemented | Current registry, actors, and shipped JSON implement the full content set | Historical task lists are not a current gap list |
| Reskin checklist describes all three player states as 64×64 frames | Current small is 32×32; big/fire are 32×64 | At 2× source density, faithful frame canvases are **64×64 and 64×128**, respectively |

The current configuration has **no `ART_SCALE` constant**. The reskin's 2× density is a recorded decision, not a completed runtime feature. Unity's PPU and explicit logical hitboxes can implement that separation directly.

Primary local references: [mechanics](MARIO_GAME_MECHANICS.md), [level atlas](MARIO_LEVEL_ATLAS.md), [implementation record](PLATFORMER_ENGINE_IMPLEMENTATION.md), [reskin execution](MARIO_RESKIN_EXECUTION.md).

## 2. Unity capabilities and recommended baseline

### 2.1 Does Unity support this kind of 2D sprite-sheet game?

**Yes, directly.** The normal Unity workflow supports PNG sheets imported as multiple sprites, grid slicing by cell size or count, sprite rendering, frame-based animation, atlas packing, and tile-based level editing. There is no need to convert these characters into 3D models or skeletal rigs. [U2–U5]

| Existing requirement | Unity equivalent | Assessment |
|---|---|---|
| Rectangular sprite sheets | Texture Type `Sprite (2D and UI)`, Sprite Mode `Multiple`, Sprite Editor grid slicing | Native tooling |
| Frame-index animation | `SpriteRenderer.sprite` driven by a flipbook/state controller; Animation Clips/Animator where useful | Straightforward; retain exact frame order explicitly |
| Ground and walls | Grid + Tilemap + TilemapRenderer | Good fit for the two static terrain types |
| Interactive blocks and pipes | Prefabs with presentation and custom gameplay components | Better fit than forcing every object into a Tilemap cell |
| Atlas packing and themes | Sprite Atlas assets and explicit theme loading | Replaces the bespoke runtime atlas lookup |
| Orthographic scrolling | Orthographic Camera; custom follow adapter or Cinemachine | Preserve existing camera behavior first |
| Pixel-art display | Point filtering; Pixel Perfect Camera if the chosen zoom policy allows it | Useful, but not automatic compatibility with arbitrary pinch zoom |
| Keyboard/touch/gamepad | Input System actions, OnScreenStick, OnScreenButton | Touch visuals and gesture arbitration still need setup |
| Menu/HUD/pause | Screen-space Canvas and uGUI, text components | Removes current world-camera HUD compensation |
| SFX/music | AudioClip, AudioSource, AudioMixer | Standard implementation |
| Reusable configuration | ScriptableObject assets | Good for immutable authored definitions, not saved runtime state |
| Content loading | Serialized catalogs for the slice; Addressables for controlled production loading | Reference ownership must be designed |
| Android hosting | Unity as a Library (`unityLibrary`) | Supported, with meaningful constraints |

Unity provides the tools, **not this game's rules**. It will not infer shell behavior, six-hit boss defeat, crouching exceptions, pipe-entry conditions, or checkpoint semantics from a sprite sheet.

### 2.2 Editor/version choice

**Proposed production baseline: the current patched Unity 6.3 LTS, provided the Android integration spike passes.** Unity lists support through December 2027. Unity 6.0 LTS is listed through October 2026, making it a poor new long-lived baseline at this research date. [U1]

This is a project-specific recommendation to reduce moving parts during a fidelity port. Unity itself recommends supported Update releases for new and mid-cycle productions; they are production-ready, not betas. The live release page retrieved during this review identified **6000.6.0f1, released 2026-08-31**. Consider the current supported Update release if required for the host's Android/toolchain requirements. [U1, U20]

Pin the exact Editor patch and package lockfile after the spike. Do not assume every Unity 6 patch uses the same Android build dependencies; the official compatibility table changes even within 6.3. [U12]

Suggested initial stack:

- Universal 2D/URP project with **unlit sprites** for baseline visual fidelity.
- 2D Sprite and 2D Tilemap Editor tooling.
- Input System, uGUI, Unity Test Framework.
- Sprite Atlases, then Addressables when production content lifetimes are established.
- Cinemachine as an optional camera implementation after the reference camera is matched.

The standalone `com.unity.2d.pixel-perfect` package documentation explicitly distinguishes its non-SRP component from URP's integrated implementation. With URP, use the URP-compatible Pixel Perfect Camera rather than adding a conflicting standalone component. [U5]

## 3. Migration strategy comparison

| Strategy | Initial effort | Fidelity risk | Reuse potential | Recommendation |
|---|---|---|---|---|
| Translate custom simulation to C#, use Unity presentation and tooling | Medium | Lowest practical risk | High for tile-based arcade platformers | **Preferred** |
| Rebuild around Dynamic Rigidbody2D, physical contacts, and Unity gravity | Medium–high | High: movement, contacts, and platform behavior change | High for games intentionally built around physical interactions | Use only if redesigned feel is acceptable |
| Replace gameplay with an existing platformer template/controller | Low for a demo; uncertain for full parity | High until proven against actual mechanics | Depends on extension model and dependencies | Evaluate only against the hard mechanics slice |
| Run the Java game inside Unity via Android/Java interop | High integration cost | Does not solve Guidebee rendering/lifecycle coupling | Poor; Android-specific | Unsuitable as the gameplay architecture |
| Continue with the current Java toolkit | Lowest immediate cost | Lowest migration risk | Good for more Android/GGE games | Rational if app footprint and Android-only delivery dominate |

### Why the preferred approach still meaningfully uses Unity

Unity would own rendering, imported sprites, Tilemap editing, prefab assembly, UI, input devices, audio, profiling, asset delivery, and platform builds. Keeping custom movement does not diminish those advantages. Precise arcade platformers commonly need explicit movement and collision rules regardless of engine.

A narrow C# simulation model avoids reproducing all of Guidebee. For example, replace `Sprite extends Layer` with a small actor-state object plus an `ActorView` that displays its current pose. Preserve the custom solidity query; let TilemapRenderer display the same terrain data.

### What is reusable and what is rewritten?

| Area | Reuse |
|---|---|
| Level layouts and transition data | Existing JSON can be the source of truth; parser/importer is new |
| PNG and WAV content | Technically reusable after orientation/import processing; the existing reskin plan governs final content |
| Gameplay algorithms | Port to C# with semantic tests and adaptation |
| Registry keys and actor specifications | Reuse as legacy content identifiers |
| Guidebee graphics/UI/input/audio code | Replace with Unity APIs and authored assets |
| Android Java/Kotlin shell | Retain for the embedded product; connect through a small bridge |
| Java platformer extraction | Reuse boundaries and lessons; C# implementation is new |
| Existing documentation/minimaps | Reuse as test coverage and content reference, checking stale details against source |

Java source and Java bytecode do not become Unity C# gameplay assemblies. Android plug-ins are suitable for host services and existing Android-specific functionality, not a cross-platform shortcut around the gameplay migration.

## 4. Sprite sheets, atlases, and higher-resolution art

### 4.1 The existing assets are well suited to conversion

`PackMarioAtlas.java` already supplies most of the useful import metadata:

- Source path and logical region name.
- Theme membership.
- Columns and rows of frames.
- Seven generated terrain-composite definitions.

The documentation also contains [a 129-entry sprite manifest](assets/mario-sprites/manifest.json) with region dimensions, frame dimensions, and extracted image paths. This is an excellent cross-check, but the generated documentation assets should be verified against the current packed source before becoming production inputs.

Use the packer's declarations to create an import manifest with:

```text
logicalId, theme, sourceFile, columns, rows,
frameWidthPx, frameHeightPx, sourceOrientation,
pixelsPerUnit, pivot, logicalBounds, animationDefinitions
```

The current manifest does not fully specify animation timing or semantics. For those, inspect actor frame selection: some sheets contain themes, directions, poses, or unused cells rather than one continuous animation.

### 4.2 Conversion steps

1. Obtain natural-orientation PNGs from the art source or reconstruct them from the shipped atlases.
2. If using original source files, reproduce the exact magenta-to-alpha conversion for legacy assets that need it. New artwork should use real alpha.
3. If extracting shipped atlas regions, **undo the packer's vertical flip per frame cell**. It flips each cell in place, without reversing the order of rows. Flipping the entire sheet would reorder multi-row animations incorrectly.
4. Import as Multiple sprites with exact grid dimensions and stable names such as `player_000` through `player_027`.
5. Preserve empty cells when they occupy meaningful frame indices. Avoid transparency-based automatic slicing for the compatibility content. [U2]
6. Generate explicitly ordered animation definitions; do not rely on enumeration order or lexical sorting of unpadded numbers.
7. Configure pivots, PPU, Point filtering, and baseline uncompressed import settings.
8. Repack individual imported sprites using Unity Sprite Atlas assets. The libGDX/GGE `.atlas` descriptor is a different format and requires conversion; Unity does not directly treat it as a Unity Sprite Atlas.

For an atlas region at top-origin `(rx, ry)` and a frame at `(column, row)`, the Unity bottom-origin rectangle calculation is:

```text
frameLeft = rx + column * frameWidth
frameBottom = atlasHeight - (ry + (row + 1) * frameHeight)
```

This rectangle conversion locates the frame. It **does not undo its pixel-content flip**; that is a separate extraction operation. Current packer output has `rotate: false` and zero trim offsets, which simplifies conversion. Reject or explicitly handle different metadata rather than assuming that remains true forever.

### 4.3 Separate source pixels from gameplay size

Use **one Unity scene unit per logical tile** for presentation. Retain the legacy 32-pixel simulation grid behind the adapter during the port.

| Asset | Current frame | Legacy logical bounds | Unity bounds | PPU now | 2× art frame | PPU at 2× |
|---|---|---|---|---|---|---|
| Small player | 32×32 | 32×32 | 1×1 | 32 | 64×64 | 64 |
| Big/fire player | 32×64 | 32×64 | 1×2 | 32 | 64×128 | 64 |
| Typical tall enemy | 32×48 | 32×48 | 1×1.5 | 32 | 64×96 | 64 |
| Boss | 64×64 | 64×64 | 2×2 | 32 | 128×128 | 64 |
| Terrain tile | 32×32 | 32×32 | 1×1 | 32 | 64×64 | 64 |

At 2× density, increase PPU together with source frame dimensions. **Do not double the logical collider or the movement constants.** Keep physics bounds authored separately from sprite transparency, trim bounds, and visual padding.

For a centered sprite representing a legacy top-left AABB `(x, y, w, h)`:

```text
unityX = (x + w / 2) / 32
unityY = -(y + h / 2) / 32
```

For a terrain cell with column `c` and downward row `r`, use Unity Tilemap cell **`(c, -r - 1)`**, with the standard centered tile anchor. This maps its center to `(c + 0.5, -r - 0.5)`.

The literal `32` belongs in the **legacy adapter configuration**. A reusable adapter reads logical units per tile from configuration. If using a foot pivot instead of a centered pivot, derive a corresponding foot-position mapping and apply it consistently, including growth/shrink transitions.

### 4.4 Animation strategy

For the parity port, a small `SpriteFlipbookPlayer` and named animation definitions are easier to audit than a large Animator graph:

- Player pose selection is state-driven and its walk cadence depends on speed.
- Both facing directions already have authored frames; a blanket `flipX` can select the wrong art.
- Growth/shrink strips change presentation and eventually logical size; gameplay owns the transition completion.
- Star palettes and invincibility blinking are presentation effects driven by simulation state.
- `Boss` uses selected indices from a 3×2 sheet, not a six-frame loop.

Animator is appropriate where authored state transitions improve workflow. Future games can use skeletal 2D Animation if desired; the core should only expose pose/state data. Avoid making an animation event the sole authority for damage, pickups, or progression.

### 4.5 Sorting, filtering, and large backgrounds

Assign explicit sorting layers/orders for background, terrain, scenery, interactive blocks, actors, foreground effects, and UI. Add local exceptions for:

- Plants behind their own pipes.
- Growth-item reveals behind their replacement block.
- Coin pops in front of the block.
- Pipe-entry ghost/masking effects.

The current six-pass spawn order affects both display and update ordering. A single generic spawn pass with arbitrary Unity object order is not automatically equivalent.

For initial imports, use Full Rect geometry, Point filtering, no mipmaps, no lossy compression, padding, and no atlas rotation. Then profile/adjust per asset. These are baseline choices for this art, not mandatory settings for all future games. Atlas packing can reduce texture switches, but one atlas does **not guarantee one draw call** across different materials, sorting constraints, and render passes. [U3]

Pixel-perfect presentation and smooth arbitrary zoom are competing requirements. The existing game supports continuous zoom from 0.6× to 1.6×. Decide whether to preserve that behavior or adopt stepped zoom; do not let a Pixel Perfect Camera silently redefine it. If Cinemachine Confiner 2D is used, lens changes require the documented cache invalidation. [U5, U7]

Match the existing `ExtendViewport` framing as well: its minimum window is 20×15 tiles, with more world exposed on wide screens. For aspect ratio `a = screenWidth / screenHeight`, an equivalent initial orthographic size is `0.5 * max(15, 20 / a) * zoom`. Clamp the camera using that effective window, including levels smaller than the window. Keep touch controls and HUD on a screen-space Canvas with safe-area layout; their size should not change with world zoom.

One concrete high-resolution issue: several backgrounds are **1536 pixels wide** in the manifest. At 2× they become 3072 pixels wide and no longer fit the existing Java packer's 2048-page limit as whole regions. In Unity, use appropriately sized separate background textures or split them into authored repeatable sections. Do not silently downscale them during import and call the result a 2× art upgrade.

## 5. Level data and authoring pipeline

### 5.1 Keep the current JSON as the legacy source of truth

The highest-value reuse is already available: the 55 levels are data. No second execution of the original desktop Java level constructors is necessary to port the shipped content.

Recommended pipeline:

```text
Existing level JSON + legacy content registry
  → LegacyLevelDto parser
  → validator and compatibility normalizer
  → LevelAsset / immutable runtime definition
  → terrain grid + Tilemap display + actor prefab/model factories
```

Use a **Bootstrap scene**, a menu, and a reusable gameplay shell. Generated level content can be instantiated beneath a level root. Creating 55 hand-maintained Unity scenes would duplicate the existing data and increase drift risk.

For the first implementation, import at Editor/build time. This catches content errors before device launch and allows prefab/sprite references to be serialized normally. Runtime JSON parsing can be added if editable/downloadable levels become a real requirement.

### 5.2 Preserve the actual serialized schema

The Java class contains flattened `posX`/`posY`, but the JSON contains **`"pos": {"x": ..., "y": ...}`**. The importer must target the JSON shape rather than mechanically copying Java member names.

Unity `JsonUtility` supports a structured DTO with serializable fields, nested serializable classes, and lists, but ignores unknown fields and is not a full schema validator. Use field-based DTOs rather than assuming C# record properties or Java-style immutable fields will deserialize. It does not support dictionaries in the documented 6.3 JSON path. Use explicit validation, or a suitable general-purpose JSON library when richer parsing is needed. Test the selected serializer in an IL2CPP build as well as the Editor. [U14]

### 5.3 Unit and semantic traps

| Field/behavior | Current meaning | Required handling |
|---|---|---|
| `tiles[].x/y`, `lengthX/Y` | Grid coordinates and placement extents | Convert once; expand by type-specific rules |
| `pos.x/y` | Fallback spawn in tiles | Some direct-menu arrivals must use predecessor checkpoint locations instead |
| `checkpoints[].x/y` | Exact **legacy pixel** positions, stored as doubles | Do not multiply by tile size |
| `checkpoints[].locX/locY` | Destination spawn in **tiles** | Different units from the trigger location |
| `teleports[].inX/inY/outX/outY` | Legacy pixel coordinates | Current trigger is offset by +32 X; only player X is replaced |
| `bombsTurnOff` | Tile-column threshold | Compare against player X divided by tile size |
| `flyingFishesLength` | Pixel-distance threshold | Do not interpret like `bombsTurnOff` |
| `patrolLength` | Type-dependent: patrol distance, boss wall position, or lift width input | Translate through each handler, not one global formula |
| `bridgeLength` | Balance-lift partner displacement in tiles | Preserve linked-object construction |
| `levelLength` | Legacy field used by specific systems | World/camera dimensions actually come from placement extents |
| `attribute`, `backgroundImage`, `levelName` | Multiple independent selectors | Theme, water behavior, night visuals, and pipe exceptions are not one enum |
| `time` | Serialized string metadata | Do not add countdown/death behavior merely because the field exists |

Examples that should be importer fixtures:

- `level_11.json` has `levelLength = 6768`, while the level atlas/current extent calculation gives a **311-tile / 9952-pixel** world width. Replacing bounds with `levelLength` would change the world.
- A pipe placement may have `lengthX = 1` but render/collide as a two-tile-wide sprite. Its extent fields do not universally describe final visual/collision dimensions.
- World 1 bonus IDs **97/98** and World 8 IDs **841–845** are real identifiers. Do not derive next level using `id + 1` or parse an ID as two decimal digits.
- Five beanstalk-family levels are catalogued, but not all use the `Clowd` attribute. `Clowd` music falls back to Ground; CloudsNight is a visual selection independent of the actual Ground attribute.
- Level **844** has a special Sea terrain composite.
- Any successful cross-level checkpoint transition currently marks the source level cleared, including a bonus-room entry. Changing that is a progression redesign.
- Each new `MarioGameScreen` constructs a fresh Small player; `goToLevel` carries the score/lives state but does not transfer the previous player's power state. Preserving power across entrances would be a deliberate gameplay change.

### 5.4 Registry conversion and validation

Create a legacy mapping table for all **56 registered placement keys + `stone` and `chocolate`**. Each key should record its factory, parameters, spawn multiplicity, visual layer, and logical collision participation.

One placement does not necessarily produce one actor:

- `FireBar`/`BigFireBar`: six/twelve orbiting fireballs.
- `pump`: pipe segments and sometimes a plant.
- `tree`: solid canopy cells and decorative trunk cells.
- `BalenceLift`: two linked platforms.
- `Flag`: decorative pieces plus an interactive pole.

Conversely, many important actors are created dynamically: shells, brick items, projectiles, eggs, and effects. A placement-registry checklist alone does not cover the full runtime roster.

The current loader silently ignores unknown placement types. The new importer should produce a complete report of unknowns; preserve intentionally ignored legacy types through an explicit allow-list and fail on unexplained gameplay omissions. This provides compatibility without making future authoring errors invisible.

### 5.5 Future-game level authoring

Provide Unity Tile Palettes for terrain and prefab placement tools/inspectors for actors, patrol paths, moving platforms, and entrances. Designers should be able to play from a selected entrance and inspect logical hitboxes.

Keep the legacy schema behind its importer. For new content, use versioned definitions with stable `gameId`, `levelId`, actor-definition IDs, explicit bounds/entrances, and typed settings. Avoid turning the overloaded `extraInfo`/`patrolLength` convention into the permanent public toolkit API.

Maintain **one authoritative representation per level**: legacy JSON for imported levels; Unity-authored assets for newly designed levels, with export if needed. Generated Tilemaps/prefabs should not become a second independent version of the same legacy level.

## 6. Physics, timing, collision, and gameplay fidelity

### 6.1 The game uses custom collision, not Guidebee Box2D

`Player` implements its own axis-separated movement. `TileWorld.containsImpassableArea()` queries a grid plus active `SolidTile` objects. `TileMovement` does similar movement for other actors. Actor interactions are resolved by ordered AABB checks.

This means Unity's Rigidbody2D physics is **available but not required**. Replacing the current controller with default gravity and dynamic collision response would change the central behavior being ported.

Recommended first version:

- Simulation owns positions, logical bounds, velocities, actor states, and interactions.
- Unity Transforms/SpriteRenderers display those positions.
- Static terrain is represented once in simulation data and mirrored into Tilemap visuals.
- Interactive blocks use the same solidity contract as the legacy code.
- One coordinator advances the simulation in a defined order.

If a later motor uses Unity collision queries, use explicit casts/overlap queries and tested contact filters. A Kinematic Rigidbody2D does not automatically provide the legacy wall/floor clamp. Full Kinematic Contacts changes contact generation; it does not make the solver move a kinematic player out of obstacles. [U8]

### 6.2 The numerical units need care

The player currently computes:

```text
frames = deltaSeconds * 60
horizontal displacement = speed / 20 * frames
vertical displacement = gravity * frames
```

`speed = 60` is **not** 60 pixels/second or 60 Unity units/second. At the normal cap:

```text
60 / 20 * 60 = 180 legacy pixels/second = 5.625 tiles/second
```

Useful reference conversions for a one-tile Unity unit:

| Quantity | Legacy value | Equivalent nominal scene-unit value |
|---|---|---|
| Normal horizontal cap | `speed = 60` | 5.625 units/s |
| Turbo horizontal cap | `speed = 100` | 9.375 units/s |
| Horizontal acceleration | `2` speed-units per 60 Hz tick | 11.25 units/s² |
| Gravity increment | `0.42` pixels/tick per tick | 47.25 units/s² downward |
| Base jump impulse | `-11` pixels/tick | 20.625 units/s upward, before the same-tick gravity increment |
| Fall cap | `10` pixels/tick | 18.75 units/s downward |

These conversions explain scale; they are **not enough to guarantee an identical jump** under a different solver/integration order. Preserve acceleration-before-movement, clamping, and the speed bonus to jumps. Initially retaining the original numerical units inside C# greatly reduces translation mistakes.

### 6.3 Choose an explicit time policy

The current game is **variable-step**, despite its 60 Hz constants:

- Incoming render delta is clamped to `1/30` second.
- Actors scale individual operations by `delta * 60`.
- A large hitch discards excess elapsed simulation time.
- Debug fast-forward repeats the update rather than using one oversized delta.

Scaling constants by delta does not make all discrete integration, animation toggles, random spawning, and edge-triggered input frame-rate invariant.

**Proposed Unity policy:** a central **60 Hz fixed simulation**, with bounded catch-up, buffered input edges, and optional visual interpolation. Unity's default fixed interval is not the desired compatibility assumption; set it explicitly if using `FixedUpdate`, or own an accumulator. [U9]

Expose `Step(delta)` in the simulation so recorded legacy delta sequences can be replayed in tests. Match the reference at 60 Hz first. A fixed-step build can differ from the current game at 30 Hz or after a stall; record that as an intentional timing improvement and validate it, rather than claiming bit-identical behavior at every frame rate.

Operational requirements:

- Sample/receive input independently of render frequency; queue presses and consume each once on a simulation tick.
- A held fire/run button has two meanings: an action press and a sustained run modifier.
- Bound catch-up work to avoid a long update spiral on mobile.
- Reset pending elapsed time on pause/resume and host transitions.
- Keep UI animation time separate from paused gameplay time.
- Give simulation random sources explicit seeds or inject recorded random values. Java `Random`, C# `System.Random`, and Unity random APIs are not interchangeable streams.

### 6.4 Preserve interaction order

The current screen's update sequence is:

```text
Layer/actor updates (in the existing actor order)
Advance shared oscillator clock
Resolve pickups
Resolve player/enemy interactions
Resolve enemy/enemy interactions
Resolve projectiles
Resolve moving-platform contacts
Resolve hazards
Resolve axe constraint
Resolve same-level teleports
Update ambient spawners
Update level-completion/death/transition state
Update Star music selection
Update HUD/camera and draw
```

Notable dependencies:

- Player movement performs tile collision internally, before the actor-pair resolvers.
- Orbiting actors read the shared oscillator before the current screen advances it.
- Ambient rocket spawns read the camera before its final current-frame follow update.
- Stomping a turtle can register a shell during collision processing.
- The level may change during the update, requiring remaining work against the old level to stop.

Unity does not guarantee the required relative order merely because separate scripts implement `Update()`. Use explicit simulation phases and stable actor IDs/order. Define when spawned actors enter each phase; match the source's snapshot/live-list semantics before introducing a universal “spawn next tick” rule.

### 6.5 Mechanics that deserve focused parity work

| Mechanic | Why a generic Unity controller can differ |
|---|---|
| Crouch | It selectively ignores overhead interactive blocks/enemies/hazards; it does not simply resize the player or ignore all static terrain |
| Growth/shrink | Morph freezes the player while the world continues; final power state and bounds change later; Fire shrinks straight to Small |
| Stomp detection | Based on overlap axes and relative centers; current resolver does not require an explicit downward-velocity check |
| Shell transformations | Successor spawn, kick direction, stop-on-stomp, and immunity behavior interact |
| Platforms | Landing is a top-surface/tolerance rule; horizontal carry is applied explicitly |
| Balance lifts | One primary drives both platforms; ridden flags feed subsequent updates; a joint-based seesaw would be different |
| Swimming | Whole-level mode with different gravity/friction, repeated paddle presses, no turbo, and fixed surface constraints |
| Bouncers | Strong upward launch, tied visual spring animation, and ceiling interactions |
| Boss | Six projectile hits, star behavior, randomized attacks, patrol/chase, and an independently triggered finale |
| Warps | Same-level teleports change only X; cross-level pipe entries have direction/ground/proximity gates |
| Death/respawn | Animated enemy death differs from immediate pit death; respawn checkpoint advances under specific grounded conditions |
| Scripted endings | Flag slide → automatic walk → celebration; axe/bridge sequence and final message have distinct state paths |

Current final-position tile tests can still tunnel in some high-displacement cases; the delta clamp is mitigation, not a proof. Sweep/substep improvements are possible, but introduce them with explicit behavior tests, especially strong bouncer launches and thin geometry.

## 7. Reusable architecture for subsequent games

### 7.1 Design for a family of games

The shared layer should make it inexpensive to build another platformer with a different player, progression system, enemy roster, and art. “New game” should not mean copying `MarioGameScreen` and editing dozens of Mario conditionals.

Recommended dependency direction:

```text
Morse Android host / standalone bootstrap
                 │
                 ▼
       Unity presentation and host adapters
                 │
                 ▼
       Platformer runtime contracts and simulation
                 ▲
                 │
     ┌───────────┴─────────────┐
     │                         │
Mario compatibility /       Second game
Ampere content module      content + abilities
     │                         │
     └──── optional training/challenge module
```

The actual assembly dependency rule is simple: **game modules reference the core; the core never references a game module**. Presentation adapters implement core interfaces. A composition root supplies the concrete factories/services for the selected game.

### 7.2 Suggested project/package layout

```text
unity/PlatformerGames/
  Assets/
    Bootstrap/
    Games/
      MarioCompatibility/
        Runtime/
        Editor/LegacyImport/
        Art/ Audio/ Definitions/ Levels/
      Ampere/
      SecondGameSample/
    HostBridge/
  Packages/
    com.guidebee.platformer/
      package.json
      Runtime/
        Core/             # logical state, commands, clocks, contracts
        Collision/        # grid queries, motor, ordered interactions
        Gameplay/         # reusable optional capabilities
        Presentation/     # Unity views and data adapters
      Editor/             # palettes, inspectors, validators
      Tests/
      Samples~/
      Documentation~/
  ProjectSettings/
```

This is a proposed layout, not a directory created by this report. During the slice, assemblies can live under `Assets` for convenience; package them before the second consumer is accepted. Use `.asmdef` boundaries to enforce dependencies and an Editor-only assembly for importer/tooling. Unity's package documentation makes clear that `Editor/` naming alone is not sufficient to configure assembly platform restrictions inside a package. [U15]

Prefer one Unity project/runtime containing multiple content modules for games launched inside this app. If a future game becomes a separate product, move the shared package to a versioned local/Git/package source rather than copying it.

### 7.3 Core contracts and composition

| Contract/component | Responsibility |
|---|---|
| `GameDefinition` | Game ID, level catalog, content registries, player prefab/config, rules and presentation bindings |
| `LevelDefinition` | Engine-neutral authored geometry, entrances, spawns, and typed metadata |
| `LevelSession` | Owns one live level, actor registry, simulation clock, RNG, and services |
| `ICommandSource` | Produces held directions and queued actions from touch, keyboard, replay, AI, or scripted control |
| `ITileCollisionWorld` | Logical tile and interactive-solid queries |
| `ICharacterMotor` | Game-selected movement behavior; legacy motor is one implementation |
| `IInteractionResolver` | Ordered interaction processing |
| `ISpawnFactory` | Turns a typed definition into model/view instances with explicit ownership |
| `DamageReceiver`, `Collectible`, `MovingSurface` | Small optional capabilities |
| `ISaveStore`, `IAudioEvents`, `IGameHost` | Persistence, presentation requests, and native-host boundary |

ScriptableObjects are appropriate for movement profiles, actor definitions, animation sets, theme palettes, sound cues, and progression rules. Live state belongs to runtime objects; otherwise two enemies or two game sessions can accidentally share health, timers, or collected-item state through one asset. ScriptableObjects are also not a deployed application's save-file mechanism. [U13]

Prefer composition over reproducing a large generic `PowerStateActor<T>` inheritance tree. For example:

```text
Player model = motor + ability set + damage/power-state policy
Player view  = sprite/animation set + visual offsets + effect responses
```

Mario can use grow/fire/star capabilities. Another game can use dash, double-jump, health, and keys without inheriting Mario power-state assumptions. Share useful motor code and allow replacement; per-game tuning should not mutate global constants.

### 7.4 Candidate reusable mechanics

Extract only components actually exercised during the port/second sample:

- Grid collision and explicit movement phases.
- Walking/patrol movement, projectile launching, timed effects.
- Collectibles and item dispensers.
- Moving-platform surfaces, path motion, and rider notification.
- Damage immunity policies and temporary invulnerability.
- Checkpoint/entrance infrastructure and progression policies.
- Camera bounds, command override, pause, save storage, content catalogs.
- Debug warp, collision overlay, single-step, replay, and asset validation tools.

Keep Mario-specific rules in its module: Small/Big/Fire, shell behavior, exact jump feel, Star treatment, flag/axe endings, numeric IDs, and legacy field interpretation.

### 7.5 The second game is an architecture test

Build a small sample with:

- One original level and a different art density.
- A fixed-size player with health and dash, rather than growth/fire states.
- A key/door objective instead of a flag/axe ending.
- One differently configured enemy and moving platform.
- Independent progression/save namespace.

Acceptance: changing to this game requires selecting a different `GameDefinition`, adding its content/abilities, and composing existing services. No `if (gameId == ...)` branches are needed inside the core.

It is reasonable for the exercise to expose a missing extension point. Fix that based on the real second use case, rerun Mario compatibility tests, then stabilize the package. Reserve this work in the migration budget because reuse is a stated goal.

### 7.6 Morse training as an optional module

The reskin documents already plan letter-tagged collectibles and a challenge HUD. Provide this through a training module rather than putting Morse concepts into every platformer actor:

```text
Collectible interaction
  → collection event with optional challenge payload
  → training rules evaluate the answer
  → score/progress event + challenge HUD update
```

An illustrative service boundary:

```text
IChallengeService: next prompt, evaluate selection, report outcome
ITrainingHost: load user settings, persist training results
```

Reuse the app's established Morse mappings and settings semantics through exported data or a native service adapter. Java helper classes and the GGE Morse atlas still need C#/asset adaptation. For future standalone builds, provide a C# implementation behind the same interface.

If live microphone decoding or precise Morse tone scheduling is later required, scope that separately from collectible matching. It introduces device/audio timing and lifecycle work not exercised by a letter-tagged coin.

### 7.7 How far does reuse extend?

The proposed package is a good base for arcade platformers, educational platformers, action platformers, and some runners. Input, saves, audio, catalogs, and challenges can also support unrelated games. Their movement, combat, world structure, and goals may still require separate modules.

A platformer package does not automatically make a tank game, puzzle game, or multiplayer game cheap. In particular, networking/rollback is a separate requirement; a fixed timestep alone does not provide cross-platform determinism. DOTS/ECS is not justified by the current roster without profiling evidence.

## 8. Android application integration

### 8.1 Product topology

The existing product opens Mario as a full-screen landscape Activity. That makes Unity as a Library a plausible fit. [U10, U11]

For the integrated product:

```text
Existing Android :app / Compose home
  → dedicated Unity host Activity
      → one unityLibrary runtime
          → selected game module and level
  ← exit / completion / training-result messages
```

Build a standalone Unity player for fast iteration, and run an embedded host proof during the initial slice. A successful standalone APK does not establish that the existing Android app can consume the export unchanged.

### 8.2 Current host/toolchain versus Unity

| Item | Repository baseline | Retrieved Unity 6.3 documentation | Implication |
|---|---|---|---|
| Minimum Android API | `minSdk = 21` | Android **7.1 / API 25+** | Embedded Unity 6.3 normally raises the delivered app's minimum; a hidden menu entry does not solve manifest/runtime compatibility |
| Target/compile API | Both **37** | Android compatibility page explicitly mentions targeting 35/36; dependency page permits recent SDK tools | API 37 support in the chosen patch/export must be proven; the docs do not establish it here |
| Android Gradle Plugin | **9.4.0** | **9.0.0** for 6000.3.17f1+ in the current table | Generated build logic needs an actual compatibility test |
| Gradle wrapper | **9.7.1** | **9.3.1** for 6000.3.25f1+; 9.1.0 for 17–24 | Do not replace the app's wrapper blindly |
| Java language level | **17** | OpenJDK **17** toolchain documented | Language-level alignment helps, but does not prove the host/export toolchain as a whole |
| Native toolchain | Existing `:gameengine` native dependencies | Unity documents **NDK r27c** | Audit native libraries and packaging together |

These are version-specific observations, not a claim that all Unity versions require API 25 or AGP 9.0. Unity 6.0 documentation retrieved separately listed API 23; relying on an old generic “Unity 6 minimum” would be inaccurate for this proposed baseline. [U12, U16, U17]

If retaining API 21–24 users is essential, make that a product decision before migration. A separate Unity application preserves the original app's existing compatibility boundary, but is a different distribution/user-experience choice. A production migration to an older unsupported editor is not a good default workaround.

### 8.3 Supported integration and lifecycle constraints

Unity exports a `unityLibrary` module containing runtime/player data and a separate `launcher`. The existing app replaces the launcher and includes the generated library/dependencies. The selected patch's current API distinguishes Activity/Service and GameActivity host implementations; follow its generated code and current docs rather than copying an old `UnityPlayer` constructor tutorial. [U11]

Documented constraints include:

- Standard Android/iOS Unity-as-a-Library integration supports **full-screen rendering**. A small Unity view inside a Compose card is not the baseline supported design.
- There can be **only one Unity runtime instance**. Multiple future games should be modules/scenes/content within that runtime.
- Unloading retains runtime memory for reopening. Unity's overview describes a range of approximately **80–180 MB**, dependent on the device/resolution; the 6.3 page renders the suffix as “Mb,” while the 6.0 page explicitly uses “MB.” Treat this as vendor guidance, not a measured budget for this app. [U10, U21]
- On Android, quitting Unity ends the process running it. A normal “return to Morse home” must use the supported unload/host navigation path, not casually call `Application.Quit`. [U11]
- The Android documentation says the Unity runtime library cannot be integrated as a dynamic module with **Play Feature Delivery**. Downloadable game content is a different concept from deferring the runtime itself. [U11]

### 8.4 Host bridge

Keep the bridge small and coarse-grained:

| Direction | Example message |
|---|---|
| Android → Unity | Launch `{gameId, levelId, profile/settings, progress snapshot}` |
| Unity → Android | Ready / failed to initialize |
| Unity → Android | Level cleared / score result / challenge outcome |
| Unity → Android | Request exit |
| Android → Unity | Updated settings or lifecycle-driven pause |

Use a ready handshake before sending initial data. Queue startup messages until the Unity receiver exists, dispatch Unity-object work on its main thread, and give each session an ID so late messages from an unloaded level cannot update a new session.

Avoid per-frame JNI traffic. Gameplay should run locally in C#; native calls should carry settings, results, and host-service requests.

### 8.5 Save migration

Current persistent progress is a boolean per cleared level:

```text
Android preferences namespace: mario_save_state
Keys: cleared_<levelNumber>
```

Current score/coins/lives survive screen-to-screen transitions within a run, but are reset when the player starts a new run from the menu. Do not accidentally persist those as if they were existing saved progress.

Unity `PlayerPrefs` is not automatically the same named Android preferences file. For the embedded version, either:

1. Keep Android as the owner of progress and use an `ISaveStore` adapter; or
2. Import the existing cleared-level flags once into a versioned, namespaced Unity save, with an idempotent migration marker.

For a separate application, Android's app sandbox prevents simply reading the original app's private preferences. Progress transfer needs an explicit handoff/export if it is required.

### 8.6 Lifecycle validation

Test launch → play → return → relaunch repeatedly, Home/background/resume, screen lock, process recreation, interruptions, orientation restoration, Android Back behavior, and audio focus. The current source already documents stale resource problems after Activity recreation; Unity changes the resource model but does not eliminate host lifecycle responsibilities.

Also verify that the existing Guidebee games still work before and after a Unity session. Their native libraries/runtime remain part of the app while those games still depend on them; replacing Mario alone does not remove the `:gameengine` module's footprint.

## 9. Performance, memory, and delivery

### 9.1 Expected workload

This is a plausible mobile Unity workload: tile terrain, a bounded actor roster, sprite effects, and lightweight AI. There is no source evidence requiring a high-end rendering architecture.

However, “Unity supports sprites” is not evidence that the combined app meets a low-end memory or startup budget. Measure the actual build on intended devices.

### 9.2 Texture budget: useful concrete arithmetic

The current packer outputs uncompressed RGBA8888 pages:

```text
2048 × 2048 × 4 bytes = 16 MiB per texture page
```

- All six current pages: **96 MiB** of raw pixel storage.
- Two common pages + one theme: **48 MiB** of raw pixel storage.
- Common-only `Clowd` path: **32 MiB**.

These are texture arithmetic, **not measured total process memory**, and exclude audio, CPU copies, render targets, Java/native/managed heaps, and engine overhead. Doubling artwork dimensions multiplies source pixel area by four, but actual packed page count depends on packing, split backgrounds, and unused space.

Unity may repack more efficiently and use platform texture compression. Conversely, runtime overhead, duplicated source/atlas dependencies, Read/Write-enabled textures, or unsuitable compression fallback can increase memory. Confirm with the Unity Memory Profiler and Android process-memory tools. [U3, U16]

### 9.3 Asset lifetime

Start with a simple catalog for the slice. For the full multi-game product, use a game/session asset scope:

- Bootstrap UI and shared services have app-runtime ownership.
- Game-common art/audio loads when that game begins.
- Theme/level assets load with the level and release when no longer needed.
- Shared assets are retained across same-theme transitions to avoid unload/reload churn.
- Release instances and dependencies through the same ownership path that loaded them.

Addressables uses reference counts; releasing a handle does not guarantee that an individual asset's memory disappears immediately, because AssetBundle lifetime matters. Avoid a permanent global `GameDefinition` graph with hard references to every game's heavy content. [U18]

### 9.4 CPU and rendering recommendations

- Use TilemapRenderer for static cells rather than a GameObject and update callback per terrain tile.
- Pool high-churn projectiles/effects once profiling identifies useful pools.
- Reuse collision buffers instead of translating every Java snapshot into allocating LINQ/`ToList()` loops each tick.
- If actor counts warrant it, add spatial buckets/broadphase queries while preserving stable interaction ordering.
- Keep unlit rendering as the initial baseline; add lights/post-processing after device measurements.
- Profile offscreen simulation before changing it. Current actors can advance while offscreen; camera-based activation can alter enemy and platform positions when the player arrives.
- Separate simulation interpolation from physics bounds and camera snapping.
- Measure both development and release/IL2CPP builds; Editor performance is not the shipping result.

### 9.5 Build and distribution

Use reproducible Editor export/build scripts, pin dependencies, commit Unity `.meta` files and project settings, and exclude generated `Library`, `Temp`, and build outputs. Keep export customization in templates/postprocessors so regenerating `unityLibrary` does not erase manual fixes.

For Android release integration, verify AAB packaging, the selected ABIs, native-library merge behavior, symbol handling, stripping, and 16 KB page-size compatibility across **all** native dependencies. Unity documents support, but that does not certify the app's existing native libraries. [U16]

Do not promise a fixed APK/AAB size increase. Record compressed download size, installed size, first-launch latency, warm-launch latency, peak gameplay memory, and memory after returning to the Android home screen.

### 9.6 License and recurring costs

Official pricing retrieved for this report states:

- Unity Personal is available subject to the **US$200,000 trailing-12-month revenue/funding eligibility threshold** and applicable terms.
- Listed 2026 Pro pricing is **US$2,310 per seat/year prepaid**, or **US$210 per seat/month** under the listed monthly payment option; local taxes/currency and contract terms can differ.
- The Runtime Fee was canceled and is not a per-install cost to include in this estimate. [U19]

Budget separately for developer time, art/audio work, test devices, any paid packages, and CI services. The existing Nintendo-derived placeholder assets and planned Ampere identity remain content concerns under the repository's reskin plan; changing engines does not perform that work. If publishing the reusable toolkit itself, keep game-specific art outside the package unless its redistribution terms permit inclusion.

## 10. Implementation phases and acceptance gates

### Phase 0 — Reference capture and host feasibility

Deliverables:

- Pin the source baseline and record representative gameplay.
- Capture player position/velocity/power-state traces with command and delta inputs.
- Prototype a blank Unity player inside this Android app using the chosen Editor patch.
- Exercise launch, return, relaunch, progress handoff, and audio/lifecycle transitions.
- Measure baseline footprint and decide the minimum-device policy.

**Exit:** a reproducible embedded build and a documented compatibility matrix, or a decision to use standalone delivery. Resolve the API/toolchain boundary before actor-port work expands.

### Phase 1 — Asset/import/motor vertical slice

Deliverables:

- Representative natural-orientation sprites, all three player forms, a morph, tall enemy, pipe, and terrain.
- Level 11 geometry loaded from the original JSON.
- Custom C# motor, input actions/touch UI, basic camera, one enemy, one interactive block, and a pickup.
- A small mechanics room exercising growth beneath a ceiling, a moving platform, and a transition trigger.

**Exit:** convincing baseline movement on desktop and a real Android device; correct sprite orientation, PPU, bounds, and touch edges. The initial **10–15 day slice** combines focused parts of Phases 0 and 1; it is not the whole port.

### Phase 2 — Content systems by mechanic family

Port in risk-revealing groups:

1. Bricks/items and transformations; normal enemies and shell interactions.
2. Moving/balance/falling platforms and bouncers.
3. Fire bars, boss/projectiles, axe/bridge and flag sequences.
4. Swimming, water enemies, and ambient rocket/fish spawners.
5. Teleport mazes, bonus areas, beanstalk entries, and final progression.

**Exit:** all registered placement types and dynamically spawned actor families have verified behavior. Unlock the relevant existing levels as mechanics land rather than porting “one world at a time” and repeatedly revisiting shared systems.

### Phase 3 — Full content, UI, persistence, and Android hardening

Deliverables:

- All 55 levels import, spawn, and transition correctly.
- Menu, pause, game over, scoring/lives, music switching, saved progress, and debug tools.
- Repeated-load/resource-lifetime fixes and release builds on target devices.
- Host integration hardened from the initial experiment.

**Exit:** full campaign/content traversal and agreed device budgets met, including post-unload behavior.

### Phase 4 — Second game and package stabilization

Deliverables:

- Mechanically different sample from §7.5.
- Core/content assembly boundaries, useful editor tools, and extension documentation.
- Regression coverage showing sample-driven changes did not break Mario compatibility.

**Exit:** the second game demonstrates extension through composition/configuration, with no Mario dependency in the shared core.

### Phase 5 — Ampere content release pass

Apply the approved identity, art/audio replacements, correct 2× import settings, and final content validation. Introduce the Morse collectible/challenge feature as a separately accepted gameplay increment.

**Exit:** the team's reskin/content requirements and release criteria are met. This report does not assume those assets have already been produced.

## 11. Effort estimate and staffing

### 11.1 Work breakdown

Assumptions: an experienced Unity/C# developer, access to an Android developer when needed, existing shipped JSON/art available for internal parity work, single-player offline scope, and no major redesign of the existing campaign.

| Work package | Engineer-days | Included work |
|---|---:|---|
| Reference capture, project setup, initial host spike | 5–8 | Baseline traces, build/export skeleton, basic lifecycle proof |
| Sprite and level import pipeline | 5–8 | Atlas extraction/orientation, slicing, manifests, DTO/import validation |
| Player motor, state, collision parity | 8–12 | Run/jump/water/crouch/grow/shrink/death and input semantics |
| Actors, platforms, hazards, effects, endings | 10–16 | Full runtime roster and interaction rules |
| Menus, camera, HUD, audio, progression, saves | 6–10 | Game session flow and persistence semantics |
| Full-level regression and optimization | 7–12 | All-level coverage, frame-rate/device work, regression fixes |
| Shared package and second-game sample | 5–8 | Dependency cleanup, real second consumer, extension documentation |
| Production Android integration/hardening | 8–14 | Host/toolchain fixes beyond initial spike, native packaging and lifecycle testing |
| **Base embedded total** | **54–88** | Before contingency |

Allow **25–35% contingency**, primarily for fidelity edge cases and Android integration: rounded planning total **68–119 engineer-days**.

For standalone delivery, remove the production Android-host-hardening package: base **46–74**, or approximately **58–100 days** with the same contingency. Basic standalone Android build/device work remains included.

The vertical-slice estimate is **part of**, not additional to, the totals. If the slice exposes a hard host/toolchain incompatibility, re-estimate before treating the range as a delivery commitment.

### 11.2 Calendar and team effects

- **One experienced developer:** about 14–24 working weeks for the embedded scope.
- **Two complementary developers** (Unity gameplay/tooling and Android/integration), plus active playtesting: approximately **9–16 calendar weeks** is plausible if work can proceed concurrently. This is not a halving of effort; player/motor decisions and final regression remain shared dependencies.
- **Team new to Unity:** allow roughly **30–60% additional effort** for learning, tooling iteration, and integration mistakes, then revise using the slice's measured velocity.

Labor-budget formula: `engineer-days × blended daily rate`, plus content production, subscriptions/services, and hardware. No staffing rate or currency has been assumed.

### 11.3 Exclusions and useful allowances

The base range excludes production of the complete replacement art/audio set, substantial campaign redesign, live audio decoding, online features, console certification, and a full iOS/desktop product launch. Technically building another platform is easier with Unity, but that platform's input, lifecycle, QA, distribution, and native integrations still need work.

Indicative additional engineering allowances after the foundation exists:

- Letter-tagged collectibles, challenge HUD, and training-result integration: **5–10 days**, depending on settings/progress integration and UX.
- Plugging a supplied, already-compatible reskin into the finished import pipeline: **3–7 days**, excluding creating/adapting missing art and full art-direction iteration.
- A genuinely new platformer: estimate its new abilities, enemies, level content, and QA separately. Shared infrastructure is reusable; the product is not “free.”

These allowances are lower-confidence than the migration inventory and should be refined after the slice.

## 12. Regression and validation strategy

### 12.1 Automated checks with useful failure signals

No Mario/platformer-specific test files were found in the inspected test locations/name search. The implementation record reports earlier Java compile checks and a user-confirmed device pass. A migration needs new tests that verify behavior rather than simply restating implementation.

**Importer/EditMode checks:**

- All 55 files parse with required fields and expected IDs.
- Every placement key is implemented or explicitly classified as ignored legacy data.
- Every cross-level destination exists; loops are allowed and should not be rejected as errors.
- Per-level static cell counts and expanded actor spawn summaries match source-derived fixtures.
- Default/menu/transition entrances are valid under the actual arrival policy.
- Sprite keys, dimensions, ordered frame sets, sound keys, and theme references resolve.
- Reimporting content preserves stable sprite/asset references.

**Simulation tests:**

- Replayed commands reproduce a baseline jump/run trace within a documented numerical tolerance at 60 Hz.
- Ceiling/floor/wall contacts, crouch exceptions, bouncer launches, and moving-platform carry.
- Small/Big/Fire transitions, invulnerability windows, two-fireball cap, shell succession, and six-hit boss behavior.
- Same-level X-only warp and cross-level entrance semantics.
- One action edge is consumed once across multiple catch-up ticks.
- Pause/resume cannot inject a large movement step or stale action.

Use tight tolerances for pure geometry/state checks. Establish acceptable trajectory/visual tolerances from the reference capture rather than inventing a claim of frame-perfect equivalence.

### 12.2 Representative level matrix

Use the full [level atlas](MARIO_LEVEL_ATLAS.md) as the inventory. Prioritize:

| Levels | Focus |
|---|---|
| 11 | Basic movement, pipes/plants, bricks, power-ups, flag ending |
| 12 and 97/98 | Underground art, menu arrival, bonus exits and cross-world shortcuts |
| 13 | Patrol enemies, canopy geometry, moving platforms |
| 14 | Boss, fire bars, axe/bridge, castle message |
| 22, 72, 844 | Water motion/enemies and special Sea terrain |
| 33 | Linked/balance and falling-platform behavior |
| 44, 74 | Same-level warp mazes; all branches/loops |
| 51/52 | Turrets, projectiles, ambient-spawn coverage as present in data |
| 63, 64 | Night visuals, bouncers, hammer-boss behavior |
| 92–96 | Beanstalk-family entrances/returns and atypical camera geometry |
| 841–845 | Multi-room finale, loop structure, final completion path |

After focused mechanics verification, smoke-test every remaining level and traverse the full transition graph, including bonus returns and alternate exits. A successful run through only the main linear route does not cover the 55-level product.

### 12.3 Device matrix and measurements

Use real Android devices for acceptance; Unity's current Android documentation does not support emulators as the definitive runtime test environment. Device Simulator is useful for layout, not GPU/memory validation. [U16]

Cover:

- Agreed low-end device, representative midrange phone, high-refresh phone, and tablet/wide aspect ratio.
- 30/60/90/120 Hz presentation where devices support it, with the same intended simulation speed.
- Simultaneous movement/run/jump touches, pointer cancellation, pinch conflicts, and controller reconnection if gamepad support is enabled.
- Cold/warm launch, 20 repeated level/game switches, and repeated host return/relaunch cycles.
- Peak memory, retained memory after unload, frame-time percentiles, allocation spikes, thermal behavior, and battery impact over a sustained session.

Proposed performance target: stable 60 fps gameplay on the agreed target device with bounded frame-time spikes. Set concrete launch-time, download-size, and memory budgets with the product owner after the initial blank-player and slice measurements; this report has no measurements from which to derive honest absolute limits.

## 13. Risks and decisions to settle

| Risk | Likelihood / impact | Mitigation or decision |
|---|---|---|
| Gameplay looks right but feels different | High / high | Reference traces, custom motor, fixed ordering, early ceiling/platform/water tests |
| Android minimum/toolchain mismatch | High / high | Blank embedded export in Phase 0; decide API 25+ policy and prove API 37 build path |
| Runtime memory/startup unacceptable for a utility app | Medium / high | Measure cold/warm/unload states before the full port |
| Incorrect atlas flips, frame order, or pivots | Medium / medium | Automated import manifest and visual contact sheets |
| Level-unit or entrance errors | High / high if untested | Explicit compatibility DTO, per-field units, graph and spawn fixtures |
| Premature framework generalized around Mario | Medium / high for future games | Second sample with health/dash/key-door mechanics |
| Static state leaks between levels/games | Medium / high | Session-owned services, explicit disposal/reset, repeated switch tests |
| New art density changes hitboxes | Medium / high | PPU and visual/logical bounds separation, dimension validation |
| Generated Unity export drifts through manual edits | Medium / medium | Reproducible exporter and checked-in integration templates |
| Existing documents used as literal current behavior | High / medium | Baseline source links and discrepancy list in §1.4 |

Settle these before full implementation:

1. **Delivery:** retain an in-app full-screen mini-game, or create a standalone game product?
2. **Devices:** is raising the minimum Android requirement acceptable for the combined app?
3. **Fidelity:** preserve the current 60 Hz feel with improved scheduling, or require legacy variable-step quirks too?
4. **Camera:** continuous pinch zoom or stepped pixel-perfect zoom?
5. **Scope:** full 55-level parity before new mechanics, or a deliberately smaller first release?
6. **Second game:** what concrete mechanic distinguishes it from Ampere/Mario?
7. **Training:** simple collectible matching first, or live audio interaction?

Recommended defaults for planning are: embedded full-screen delivery subject to measured feasibility, full existing content parity, custom 60 Hz motor, continuous zoom during parity work, and a one-level health/dash/key-door sample to validate reuse.

## 14. Relationship to the reskin and existing plans

The existing Java platformer extraction has already paid for useful understanding and boundaries. It should inform the Unity port rather than be repeated as a separate prerequisite.

If Unity is adopted after the slice:

1. Establish the Unity compatibility baseline using the current internal reference content.
2. Implement art-density independence through PPU and explicit simulation bounds.
3. Apply the approved **Ampere's Run** identity and correct 2× sprite specifications.
4. Add the planned Morse training increment through the optional challenge module.
5. Validate the second game and stabilize the package.

Art sourcing/preparation can proceed once frame/pivot/bounds requirements are settled, while gameplay is being migrated. Keep the old version as the playable behavior reference throughout the migration.

The Unity route can avoid doing the unfinished Java `ART_SCALE` runtime rewrite solely as preparation for a runtime that will be replaced. If the Java game must ship or remain actively supported first, its reskin work still has independent value. This report proposes an alternative execution path; it does not retroactively change the recorded decisions in the existing plans.

## 15. Final assessment

**Technical feasibility: high.** The game's assets, tile world, and platformer mechanics fit Unity's 2D tooling well.

**Migration complexity: medium–high for gameplay, high when Android embedding and a reusable product foundation are included.** The main work is C# behavior implementation and verification, not drawing sprites.

**Long-term value: strongest when several games and designer-authored content are real goals.** Unity supplies mature authoring, profiling, asset management, and platform deployment that the team otherwise maintains itself. The reusable package should focus on platformer contracts and optional capabilities, while each game owns its feel and rules.

**Recommended next action:** implement the 10–15 engineer-day slice with a custom motor, imported Level 11, a hard-mechanics test room, and an early embedded Android build. Use its fidelity and device measurements to confirm the full **68–119 engineer-day** embedded migration budget.

## 16. Research references

### Local implementation evidence

Paths below are relative links from this report. Named methods are more durable than line numbers as the code evolves.

| Evidence | Source |
|---|---|
| Frame/update ordering, camera, UI, level-completion state | [`MarioGameScreen`](../../app/src/main/java/au/com/guidebee/morsetoolkit/activity/mario/screen/MarioGameScreen.java): constructor, `render`, `updateLevelCompletion`, `advanceToNextLevel` |
| Player units, movement, crouch, power/animation states | [`Player`](../../app/src/main/java/au/com/guidebee/morsetoolkit/activity/mario/actors/player/Player.java): `applyMovement`, `applyHorizontalInput`, `moveXWithCollision`, `moveYWithCollision` |
| Static + interactive solidity | [`TileWorld`](../../app/src/main/java/au/com/guidebee/morsetoolkit/platformer/core/TileWorld.java), [`TileMovement`](../../app/src/main/java/au/com/guidebee/morsetoolkit/platformer/core/TileMovement.java) |
| Actual serialized parser | [`LevelDefinition`](../../app/src/main/java/au/com/guidebee/morsetoolkit/activity/mario/level/LevelDefinition.java): `parse` |
| Extents, terrain, compound spawns | [`LevelLoader`](../../app/src/main/java/au/com/guidebee/morsetoolkit/activity/mario/level/LevelLoader.java), [`MarioTileRegistry`](../../app/src/main/java/au/com/guidebee/morsetoolkit/activity/mario/level/MarioTileRegistry.java) |
| Menu-arrival policy and run-state lifetime | [`MarioGamePlay`](../../app/src/main/java/au/com/guidebee/morsetoolkit/activity/mario/MarioGamePlay.java): `startLevel`, `findArrivalTile`, `goToLevel` |
| Resource inventory and theme lifetime | [`MarioResourceManager`](../../app/src/main/java/au/com/guidebee/morsetoolkit/activity/mario/MarioResourceManager.java) |
| Persistent cleared-level flags | [`MarioSaveState`](../../app/src/main/java/au/com/guidebee/morsetoolkit/activity/mario/state/MarioSaveState.java), [`LevelProgressState`](../../app/src/main/java/au/com/guidebee/morsetoolkit/platformer/state/LevelProgressState.java) |
| Input mapping/edges | [`TouchOrKeyboardInput`](../../app/src/main/java/au/com/guidebee/morsetoolkit/platformer/input/TouchOrKeyboardInput.java) |
| Teleport/checkpoint differences | [`TeleportResolver`](../../app/src/main/java/au/com/guidebee/morsetoolkit/activity/mario/collision/TeleportResolver.java), [`CheckpointResolver`](../../app/src/main/java/au/com/guidebee/morsetoolkit/activity/mario/collision/CheckpointResolver.java) |
| Atlas grid metadata, masking, orientation and page size | [`PackMarioAtlas`](../../tools/mario-atlas-packer/src/PackMarioAtlas.java): `ASSETS`, `TERRAIN_TILES`, `drawFlippedPerCell`, `applyMagentaMask` |
| Host target/minimum SDK | [`app/build.gradle`](../../app/build.gradle) |
| Host AGP and Gradle | [`build.gradle`](../../build.gradle), [`gradle-wrapper.properties`](../../gradle/wrapper/gradle-wrapper.properties) |

### Official Unity sources

All sources below were retrieved on **2026-09-11**. Versioned documentation is intentional. Package documentation versions establish the described capabilities; exact compatible package versions should be resolved and locked with the chosen Editor patch.

- **[U1]** [Unity 6 release support](https://unity.com/releases/unity-6/support) — LTS support dates and Unity's Update-versus-LTS guidance.
- **[U2]** [Unity 6.3 Sprite Editor reference](https://docs.unity3d.com/6000.3/Documentation/Manual/sprite/sprite-editor/sprite-editor-window-reference.html) — Multiple sprites, cell-size/count slicing, pivots, empty cells, and rectangle coordinates.
- **[U3]** [Unity 6.3 Sprite Atlas reference](https://docs.unity3d.com/6000.3/Documentation/Manual/sprite/atlas/sprite-atlas-reference.html) — packing, rotation, padding, texture filtering, Read/Write, and platform overrides.
- **[U4]** [Unity 6.3 Tilemaps](https://docs.unity3d.com/6000.3/Documentation/Manual/tilemaps/tilemaps-landing.html) — tile editing, palettes, custom tiles/brushes, and collision support.
- **[U5]** [2D Pixel Perfect 5.1 documentation](https://docs.unity3d.com/Packages/com.unity.2d.pixel-perfect@5.1/manual/index.html) — camera behavior, sprite setup, and the explicit distinction between standalone and URP-integrated implementations.
- **[U6]** [Input System 1.17: On-screen controls](https://docs.unity3d.com/Packages/com.unity.inputsystem@1.17/manual/OnScreen.html) — sticks, buttons, virtual devices, and isolated input behavior.
- **[U7]** [Cinemachine 3.1: Confiner 2D](https://docs.unity3d.com/Packages/com.unity.cinemachine@3.1/manual/CinemachineConfiner2D.html) — camera bounds, lens cache invalidation, and oversize windows.
- **[U8]** [Unity 6.3 Kinematic Rigidbody2D reference](https://docs.unity3d.com/6000.3/Documentation/Manual/2d-physics/rigidbody/body-types/kinematic/kinematic-body-type-reference.html) — body behavior and Full Kinematic Contacts limitations.
- **[U9]** [Unity 6.3: Handling variation in time](https://docs.unity3d.com/6000.3/Documentation/Manual/time-handling-variations.html) — delta limits, fixed-loop catch-up, pause, and unscaled time.
- **[U10]** [Unity 6.3: Unity as a Library](https://docs.unity3d.com/6000.3/Documentation/Manual/UnityasaLibrary.html) — supported platforms, full-screen/single-runtime limitations, and retained-memory guidance.
- **[U11]** [Unity 6.3: Integrating Unity into Android applications](https://docs.unity3d.com/6000.3/Documentation/Manual/UnityasaLibrary-Android.html) — `unityLibrary`, current Java host API, load/unload/quit, and Play Feature Delivery limitation.
- **[U12]** [Unity 6.3: Unity and Gradle compatibility](https://docs.unity3d.com/6000.3/Documentation/Manual/android-gradle-version-compatibility.html) — patch-specific Gradle and AGP versions.
- **[U13]** [Unity 6.3: ScriptableObject](https://docs.unity3d.com/6000.3/Documentation/Manual/class-ScriptableObject.html) — shared authored data and runtime persistence limitations.
- **[U14]** [Unity 6.3: JSON serialization](https://docs.unity3d.com/6000.3/Documentation/Manual/json-serialization.html) — DTO fields, unknown fields, supported types, and serializer limitations.
- **[U15]** [Unity 6.3: UPM package layout](https://docs.unity3d.com/6000.3/Documentation/Manual/cus-layout.html) — runtime/editor/test assemblies and package folder conventions.
- **[U16]** [Unity 6.3: Android requirements and compatibility](https://docs.unity3d.com/6000.3/Documentation/Manual/android-requirements-and-compatibility.html) — API 25 minimum, graphics support, emulator limitations, compression, and 16 KB pages.
- **[U17]** [Unity 6.3: Supported Android dependency versions](https://docs.unity3d.com/6000.3/Documentation/Manual/android-supported-dependency-versions.html) — SDK tools, NDK r27c, and JDK 17.
- **[U18]** [Addressables 2.7: Managing asset memory](https://docs.unity3d.com/Packages/com.unity.addressables@2.7/manual/memory-assets.html) — reference counts, bundle lifetime, release behavior, and asset churn.
- **[U19]** [Unity Personal eligibility](https://unity.com/products/unity-personal) and [Unity pricing updates](https://unity.com/products/pricing-updates) — financial thresholds, 2026 seat prices, and Runtime Fee cancellation.
- **[U20]** [Unity latest release notes](https://unity.com/releases/editor/whats-new) — the live page identified 6000.6.0f1 during this review; this URL changes with releases.
- **[U21]** [Unity 6.0 Unity-as-a-Library overview](https://docs.unity3d.com/6000.0/Documentation/Manual/UnityasaLibrary.html) and [system requirements](https://docs.unity3d.com/6000.0/Documentation/Manual/system-requirements.html) — cross-check of retained-memory units and the older API 23 baseline, not requirements substituted for Unity 6.3.

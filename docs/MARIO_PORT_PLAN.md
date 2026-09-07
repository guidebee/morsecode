# Mario Port Plan: GTGE → Guidebee Game Engine

This is the working plan for porting `C:\workspace\Mario` (a GTGE/Java2D desktop Super
Mario Bros clone, ~27,300 lines across ~150 gameplay classes plus 58 level/bonus-area
files) into this repo as a new GGE mini-game, alongside Flappy Bird and Battle City, with
Morse-code training mechanics added. It assumes familiarity with
[GAME_ENGINE.md](GAME_ENGINE.md) and the [engine tutorials](tutorials/README.md) —
this document is Mario-specific; it doesn't re-explain GGE itself.

**Status of this document:** planning only. Nothing described here has been implemented
yet. Treat each numbered step below as a checkpoint — build and smoke-test at each
vertical slice rather than writing all modules before running anything.

**Looking for the finished port instead of the plan?** See
[MARIO_GAME_MECHANICS.md](MARIO_GAME_MECHANICS.md) — the as-built reference for physics,
actors, collision, the level-data pipeline, and the sprite-sheet/atlas system, plus
step-by-step recipes for adding new levels/enemies/bricks — and
[MARIO_LEVEL_ATLAS.md](MARIO_LEVEL_ATLAS.md) (every level, with minimaps) and
[MARIO_PLAYER_GUIDE.md](MARIO_PLAYER_GUIDE.md) (the player-facing manual). This document
(and [Phase 2](MARIO_PORT_PLAN_PHASE2.md)) is the *history* of how it got built.

**Decisions locked in:**
- **v1 scope = World 1 only** (`Level_11`–`Level_14` + their two bonus areas).
- **Morse-code training (§9) is explicitly deferred** until World 1 is playable
  end-to-end (Steps 0–8 complete). v1 is a straight Mario port with no Morse mechanic —
  Step 9 and the level-scope decision in §9 are out of scope until then.

## 1. Why this isn't a mechanical port

GTGE's `Sprite`/`SpriteGroup`/`PlayField`/`CollisionManager` and GGE's
`microedition.Sprite`/`LayerManager` are conceptually the same idea (GGE's
`microedition` package exists specifically to make old MIDP/GTGE-style ports
straightforward — see [12. The Microedition Game API](tutorials/engine/12-microedition-game-api.md))
but are different, incompatible classes: `BufferedImage` vs `TextureRegion`,
`Graphics2D.drawImage` vs `Batch.draw`, AWT `KeyEvent` vs touch/`GameController`. Every
gameplay class needs rewriting against the new API. What *does* carry over directly:

- **Art assets** — Mario's PNGs are already sprite-sheet strips
  (`getImages("player.png", 4, 7)`), which is exactly the shape GGE's
  `Sprite(TextureRegion, frameWidth, frameHeight)` wants.
- **Level data** — the 58 `Level_XX`/`BonusAreaXX` classes and their `Construct[]`
  data are plain POJOs (see §3) that can be converted mechanically, not hand-ported.
- **Game logic as algorithm** — `Player`'s run/jump/grow/shrink/fire state machine and
  enemy AI are portable as *design*, even though every surrounding API call changes.

## 2. Target architecture

Same three-level chain as Flappy Bird / Battle City
(`Activity → GamePlay → Screen`, see [GAME_ENGINE.md](GAME_ENGINE.md#the-lifecycle-activity--gameplay--screen)),
using the **microedition** (`LayerManager`/`Sprite`/`TiledLayer`) API rather than
`Stage`/`Actor`, because Mario's world — like Battle City's — is a dense 32px tile grid
with dozens of concurrent actors, which is exactly the case that API is optimized for.

```
MarioGameActivity          (Android entry point)
  └─ MarioGamePlay          (loads atlases/audio once, owns cross-screen state)
       ├─ MarioMenuScreen    (world/level select — replaces LevelNumber==10 special case)
       └─ MarioGameScreen    (extends ScreenAdapter — the core gameplay loop)
            └─ MarioWorld    (extends LayerManager — replaces PlayField)
                 ├─ TiledLayer   (static grid: bricks, stone, pipes, ground)
                 └─ Sprite lists (enemies, items, projectiles, particles — replaces the
                                  15 SpriteGroups)
```

This directly targets the single biggest problem in the current code: **`Mario.java` is
2,348 lines doing eleven unrelated jobs.** §4 breaks each job out into its own class.

## 3. Level data: convert, don't hand-port

The 58 `Levels.*` classes (`Level_11`, `BonusArea11A`, ...) are builders that populate a
`Construct[]` via chained `AddBrick(x,y)` / `AddQuestionMark(x,y)` / ... calls
(`Levels/BasicLevel.java`). Critically, the data types involved have **no GTGE/AWT
rendering dependency**:

- `Gears/Construct.java` — pure `Serializable` POJO (`Item_Type`, `x`, `y`,
  `Length_X/Y`, `ExtraInfo`, `BridgeLength`). No imports beyond `java.io.Serializable`.
- `Teleport/Teleport.java` — pure POJO, no imports at all.
- `CheckPoint/CheckPoints.java` — extends `com.golden.gamedev.object.Sprite`, but only
  uses it for `x`/`y`/`Point` storage with a `null` image; this runs fine headless (no
  display/AWT-toolkit requirement — GTGE's `Sprite`/`Background` classes don't touch a
  screen unless you call `render()`).

**Plan:** write a small offline `LevelConverter` (a throwaway `main()`, run once per
level, *not* shipped in the APK) that instantiates each `Level_XX`/`BonusAreaXX` class
directly from `C:\workspace\Mario`'s compiled classes (or source, added as a
build-time-only dependency) and serializes `constructors[]`/`checkPoints[]`/`teleport[]`
plus the level's public fields (`attribute`, `BackGroundImage`, `BackGroundColor`,
`type`, `Bombs`, `FlyingFishes`, ...) to one JSON file per level under
`app/src/main/assets/mario/levels/`.

This turns "hand-port 58 Java classes" into "write one converter + one loader" — the
highest-leverage step in the whole plan. Do this **before** porting individual brick/enemy
classes, since the `LevelDefinition` schema determines what fields those classes need to
read.

### LevelDefinition schema (draft)

```json
{
  "levelNumber": 11,
  "attribute": "Ground",
  "backgroundImage": "Clouds",
  "backgroundColor": "Blue",
  "type": "GreenAndTrees",
  "levelLength": 6720,
  "bombs": false,
  "flyingFishes": false,
  "tiles": [
    { "type": "Brick", "x": 10, "y": 8, "lengthX": 3, "lengthY": 1, "extraInfo": null }
  ],
  "checkpoints": [ { "kind": "basic", "x": 3, "y": 11, "nextLevel": 12, "loc": [3, 11] } ],
  "teleports": [ { "inX": 27, "inY": 11, "outX": 27, "outY": 6 } ],
  "morseChallenges": []
}
```

`morseChallenges` is an empty placeholder added now so the schema doesn't need a
breaking change later — see §9.

## 4. Breaking up `Mario.java` (and `Player.java`)

`Mario.java` currently does all of the following in one class. Each row is a new class.

| Current responsibility in `Mario.java` | New class | Notes |
|---|---|---|
| Resource loading orchestration | `MarioResourceManager` | Wraps `assetManager`; atlas region + sound lookups, replaces `bsLoader` |
| SpriteGroup creation & wiring (15 groups) | `MarioWorld` | One `TiledLayer` + typed `Sprite` lists, not 15 named groups |
| `LoadLevel` giant switch (30+ tile-type cases) | `LevelLoader` | Consumes `LevelDefinition` (§3), populates `MarioWorld` |
| CheckPoint loading/handling | `level/CheckpointLoader` + `state/CheckpointState` | |
| Teleport loading/handling | `level/TeleportLoader` + `actors/teleport/*` | |
| Input polling (keyboard + JInput gamepad) | `input/MarioInputController` | Emits a `PlayerCommand` intent, decoupling `Player` from the input source (touch vs. pad vs. keyboard-for-dev) |
| 17 explicit `CollisionManager` pairs | `collision/PlayerCollisionResolver`, `collision/EnemyCollisionResolver`, `collision/ProjectileCollisionResolver` | Grouped by *what's colliding*, not one class per pair |
| Bomb/rocket spawn timer, flying-fish spawn timer | `world/SpawnController` | Ticking components, not inline counters in `update()` |
| Camera scroll/center/edge-clamp (`StopScroll`, `SmoothScroll`, `getScreenX` bounds) | `world/CameraController` | Wraps GGE `Camera`/`Viewport` instead of manual `g.scale()` |
| Pause toggle, start-screen state, level-complete countdown | `state/GameStateController` | Explicit state machine (`START`, `PLAYING`, `PAUSED`, `LEVEL_COMPLETE`, `GAME_OVER`) instead of scattered booleans |
| Score/HUD/pause-text rendering, debug Info/Info2 overlays | `hud/ScoreHud`, `hud/PauseOverlay`, `hud/DebugOverlay` | `Table`-based HUD components, same pattern as Flappy Bird's `Score`/`ChallengeLetter` |
| Brick-break particle spawning (`addSomeBrickFragmends`) | `fx/BrickBreakEffect` | Pure visual, no collision |
| World/level progression (`IncreaseWorld`/`IncreaseLevel`) | `MarioMenuScreen` | Belongs in the menu screen, not the gameplay screen |

`MarioGameScreen.render(delta)` ends up close to `BattleCityGameScene`'s shape:

```java
@Override
public void render(float delta) {
    input.poll();
    if (state.isPlaying()) {
        spawnController.update(delta);
        world.act(delta);
        collision.resolveAll(world);
        camera.follow(world.player);
    }
    world.draw(batch);
    hud.draw(batch);
}
```

`Player.java` (812 lines) similarly splits, but stays one *actor* — it's cohesive
behavior, not mixed responsibilities like `Mario.java`:

- `actors/player/Player.java` — the `Sprite` subclass + physics/movement, ported
  roughly 1:1 in logic from the original.
- `actors/player/PlayerPowerState.java` — enum (`SMALL`, `BIG`, `FIRE`, `STAR`)
  replacing the original's ad hoc ints/strings for power-up state.
- `actors/player/PlayerAnimations.java` — frame-set lookup per power state
  (`SmallToBigMarioAnim`, `BigToFireMarioAnim`, ...), separated so `Player` itself isn't
  also an animation-table.

## 5. Full package layout

```
app/src/main/java/au/com/guidebee/morsetoolkit/activity/mario/
  MarioGameActivity.java
  MarioGamePlay.java
  config/MarioConfiguration.java        # tile size, gravity, speeds, resource keys
  resource/MarioResourceManager.java
  screen/MarioMenuScreen.java
  screen/MarioGameScreen.java
  input/MarioInputController.java
  input/PlayerCommand.java
  world/MarioWorld.java
  world/CameraController.java
  world/SpawnController.java
  collision/PlayerCollisionResolver.java
  collision/EnemyCollisionResolver.java
  collision/ProjectileCollisionResolver.java
  level/LevelDefinition.java
  level/LevelCatalog.java
  level/LevelLoader.java
  level/CheckpointLoader.java
  level/TeleportLoader.java
  state/GameStateController.java
  state/CheckpointState.java
  state/MarioSaveState.java
  actors/player/{Player,PlayerPowerState,PlayerAnimations}.java
  actors/bricks/{Brick,Stone,QuestionMark,Bank,BankWithItem,Pump,Tree,...}.java
  actors/enemies/{EnemyTurtle,EnemyMashroom,FlyingTurtle,Monkey,Boss,...}.java
  actors/items/{Coin,Mushroom,Flower,Star,Life,Helmet,...}.java
  actors/projectiles/{FireBall,EnemyFireBall,Hammer,LavaBall,Rocket}.java
  actors/lifts/{...}.java
  actors/teleport/{...}.java
  actors/checkpoint/{...}.java
  fx/{BrickBreakEffect,Explosion,CoinAnim,FireWorks,...}.java
  hud/{ScoreHud,PauseOverlay,DebugOverlay}.java
  morse/{MorseChallengeProvider,MorseBrick}.java
  hud/MorseChallengeHud.java

tools/mario-level-converter/                 # offline only, not shipped
  LevelConverter.java                        # reflects over C:\workspace\Mario\Levels\*

app/src/main/assets/mario/
  levels/level_11.json, level_12.json, ...
  atlases/mario-ground.atlas, mario-castle.atlas, mario-sea.atlas, ...
  audio/...
```

## 6. Step-by-step sequence

Each step ends with something runnable — don't move on until the prior slice builds and
plays. Recommended **v1 scope: World 1 only** (`Level_11`–`Level_14` + their two bonus
areas) to prove the whole pipeline before spending effort porting all 58 levels — see
§8 for scaling out afterward.

**Step 0 — Preparation**
- 0.1 Create the `activity/mario/` package skeleton (empty classes/interfaces per §5).
- 0.2 Register `MarioGameActivity` in the manifest (mirroring
  `FlappyBirdGameActivity`/`BattleCityGameActivity`) so the shell launches. *(Level
  scope confirmed: World 1 only for v1.)*
- 0.3 *(Deferred — see §9. No Morse mechanic in v1; the `morseChallenges` field in the
  schema below stays empty and unused until after Step 8.)*

**Step 1 — Level data pipeline**
- 1.1 Write `LevelConverter`, run it against `Level_11`–`Level_14` (+ bonus areas),
  inspect the JSON by hand for correctness against the original level layout.
- 1.2 Define `LevelDefinition` (Kotlin/Java data class mirroring the JSON) and
  `LevelCatalog` (loads JSON from assets by level number).
- 1.3 Write `LevelLoader` skeleton (no actor spawning yet — just grid population).

**Step 2 — Asset pipeline**
- 2.1 Pack World-1 PNGs into one or two `TextureAtlas` pages (ground theme first).
- 2.2 Re-encode World-1 sound effects/music if format conversion is needed (verify
  what `Sound`/`Music` accept on Android before assuming WAV needs conversion).
- 2.3 `MarioResourceManager` loads the above via `assetManager`.

**Step 3 — Static world skeleton**
- 3.1 `MarioWorld` with a `TiledLayer` sized to World 1's grid; `LevelLoader` fills
  cells for brick/stone/pipe tile types only.
- 3.2 `CameraController` + `FitViewport`.
- 3.3 **Vertical slice A:** `MarioGameScreen` renders Level 11's static geometry, no
  player, no enemies. This validates atlas packing + `TiledLayer` end-to-end.

**Step 4 — Player & input**
- 4.1 `MarioInputController` + `PlayerCommand` (start with direct touch/keyboard
  polling like Flappy Bird's `input.isTouched()`; add the virtual `GameController`
  later if on-screen buttons are wanted).
- 4.2 Port `Player`'s physics/state machine (small/big/fire, run, jump, duck, swim).
- 4.3 Tile collision against `MarioWorld`'s `TiledLayer` (a `containsImpassableArea`
  equivalent, same technique as `BattleField`).
- 4.4 **Vertical slice B:** player runs/jumps/collides with Level 11's static bricks.

**Step 5 — Interactive bricks & items**
- 5.1 Port `Brick`, `Stone`, `QuestionMark`, `Bank`, `BankWithItem`, `Pump`, `Tree`.
- 5.2 Port `Coin`, `Mushroom`, `Flower`, `Star`, `Life` and the grow/shrink animation
  sequences.
- 5.3 `PlayerCollisionResolver` (player vs. bricks/items).
- 5.4 **Vertical slice C:** Level 11 fully playable minus enemies.

**Step 6 — Enemies & projectiles**
- 6.1 `EnemyTurtle`, `EnemyMashroom` first (ground-walkers), then flying/patrol
  variants, then `Boss`.
- 6.2 `EnemyCollisionResolver` (stomp-kill, shell-kick, enemy-vs-enemy).
- 6.3 `FireBall`/`Hammer`/`EnemyFireBall`/`LavaBall` + `ProjectileCollisionResolver`.
  Pool projectiles the way Battle City pools `Bullet`/`Explosion` — GGE's
  microedition `Sprite` is built for exactly this (see
  [12. The Microedition Game API](tutorials/engine/12-microedition-game-api.md#sprite-frames-sequences-and-pooling)).

**Step 7 — World mechanics**
- 7.1 Lifts (moving platforms), teleports (pipes/warps), checkpoints, flag/level-complete.
- 7.2 `SpawnController` (rocket-launcher bombs, flying fish) for the levels that use them.

**Step 8 — HUD, menu, game state**
- 8.1 `ScoreHud`, lives counter, `PauseOverlay`.
- 8.2 `MarioMenuScreen` (world/level select, replacing the `LevelNumber == 10`
  special-cased start screen).
- 8.3 `GameStateController` wired through the full loop.

**Step 9 — Morse-code training** *(deferred — starts only after World 1 is playable
via Steps 0–8; not part of v1)*
- 9.1 Implement the chosen mechanic (§9) via `MorseChallengeProvider` + `MorseBrick`
  and/or `MorseChallengeHud`.
- 9.2 Extend `LevelLoader`/`LevelDefinition` so future levels can tag challenges
  declaratively rather than in code.

**Step 10 — App integration polish**
- 10.1 Manifest/launcher-icon polish, confirm `MarioGameActivity` fits the app's
  existing navigation alongside Flappy Bird/Battle City.

**Step 11 — Scale out remaining content**
- 11.1 Re-run `LevelConverter` across the remaining 7 worlds + bonus areas.
- 11.2 Pack remaining theme atlases (Sea/UnderGround/Castle/Night) and audio.
- 11.3 QA each world as it's added — this is where most of the 27K lines of "small
  class" work lives (most of §4/§5/§6's actor types repeat with only skin/behavior
  variation, so steps 5–6 patterns should mostly be copy-adapt from here on).

**Step 12 — Polish & QA**
- 12.1 Device testing across screen sizes (viewport scaling), touch feel.
- 12.2 Performance pass (draw calls per atlas, sprite pooling coverage).
- 12.3 Side-by-side parity pass against the original desktop build for movement feel
  and hitbox accuracy.

## 7. Collision model translation notes

The original's 17 `CollisionManager` instances each pair exactly two `SpriteGroup`s and
run one rectangle-intersection pass per pair per frame
(`Enemy_Brick`, `Player_Brick`, `Player_EnemyGroup`, `FireBall_Enemys`, ...). None of
this logic is engine-specific — it's "if A's rect overlaps B's rect, call A's/B's
reaction method" — so the *rules* (stomp kills a turtle, touching an active fireball
breaks a brick, touching a mushroom grows the player) port directly. What changes is
*how* the check runs:

- **Player/enemy/item vs. static grid** (bricks, stone, pipes) → `TiledLayer.getCell`
  range check, exactly like `BattleField.containsImpassableArea` — no per-brick
  rectangle test needed once bricks are grid cells rather than individual sprites.
  This is a **behavior change from the original**, which treats every brick as its own
  `Sprite` in `BrickGroup` — worth deciding whether *destructible* bricks (breakable
  question marks, breakable bricks) stay as individual `Sprite`s (since they need
  independent animation/removal) while *static* bricks (stone, pipes, undestroyable
  walls) become grid cells. Recommended split: **destructible/interactive tiles stay
  `Sprite`s; purely static geometry becomes `TiledLayer` cells.**
- **Actor vs. actor** (player vs. enemy, fireball vs. enemy) → `Sprite.collidesWith(Sprite)`,
  same as `Bullet` vs. `Tank`.
- **The 17-manager fan-out** collapses to 3 resolver classes (§4) each looping over
  the relevant actor lists once per frame — same total number of checks, far less
  boilerplate than 17 separate `CollisionManager` objects wired up by hand in
  `initResources()`.

## 8. Level-scope rollout order

Recommended order for §6 Step 11, easiest-to-hardest by mechanic novelty:

1. World 1 (Ground) — v1, per §6.
2. World 4 / World 6 (UnderGround/Castle reuse the same brick/enemy set with a skin
   swap — low new-mechanic risk, validates the theme-atlas approach from §2).
3. World 2/3/5/7 (introduce lifts, teleports, patrol enemies, bosses incrementally).
4. World 8 (final castle, `Boss`/`BossFire`) last.
5. Bonus areas and the black-and-white "CloudsNight" variant reuse existing actors
   with alternate art — mechanically simplest, but do them last since they depend on
   every actor type already being ported.

## 9. Morse-code training hook — deferred to post-v1

Not part of v1. Revisit this section once World 1 (Steps 0–8) is playable end-to-end.
Both existing games establish a pattern worth reusing rather than inventing a third:

- **Flappy Bird**: `Playground.isCollideWithTube` checks the pipe's assigned letter
  against a `ChallengeLetter` HUD target on collision — a wrong hit is a crash, a right
  hit is a scored answer ([11. Collision Detection](tutorials/engine/11-collision-detection.md#the-collision-that-doesnt-end-the-game)).
- **Battle City**: `BattleField.readBattlefieldFromLedLetter()` *generates* the level's
  wall layout from a Morse pattern — the geometry *is* the lesson
  ([9. Tiled Layers](tutorials/engine/09-tiled-layers-and-scenery.md)).

Mario's block-heavy, grid-based levels support either style. Options to choose between
before finalizing the `LevelDefinition` schema in Step 1:

- **A. Challenge blocks** — question-mark/coin blocks require answering a Morse
  challenge (shown via `MorseChallengeHud`) before yielding their reward; wrong answer
  = no reward (or a `QuestionMarkGrey`-style "used up" state), closest to Flappy Bird's
  model. Low risk, additive — doesn't change level geometry.
- **B. Morse-pattern brick runs** — specific brick-run lengths along a level segment
  encode a letter's dot/dash pattern (as Battle City's walls do), and the player must
  identify the letter to unlock a checkpoint/pipe. Higher authoring cost per level but
  ties more directly into level *design*.
- **C. Both** — challenge blocks for in-the-moment practice, occasional brick-run
  puzzles at checkpoints for a bigger set-piece moment.

**Recommendation: start with A** (lowest implementation risk, reuses the
`ChallengeLetter` HUD pattern almost directly, and doesn't block the level-converter
work in Step 1 since it's just an optional per-`Construct` field). Revisit B once the
core loop (Steps 0–8) is proven.

## 10. Open risks to flag before starting

- **Audio format** — verify what `Sound`/`Music` actually accept before assuming WAV
  needs re-encoding (Android's decoders often accept WAV directly; don't do
  speculative conversion work).
- **`LevelConverter` headless execution** — `CheckPoints` extends GTGE's `Sprite`,
  which lazily touches `Background.getDefaultBackground()`; confirm this doesn't
  require an AWT display context when run in a plain `java` process (expected to be
  fine — no `render()` call, no `Toolkit` access — but verify with a smoke test before
  relying on it for all 58 levels).
- **Atlas packing strategy** — per-theme atlases (Ground/Sea/UnderGround/Castle) vs.
  one monolithic atlas; affects memory and draw-call count. Recommend per-theme,
  matching how the original organizes its own image variants.

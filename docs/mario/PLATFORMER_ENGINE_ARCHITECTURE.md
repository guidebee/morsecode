# From Mario Port to Platformer Toolkit: An Extraction Architecture

**Status: proposal, nothing implemented.** Same discipline as every other planning
document in this doc set — read [MARIO_GAME_MECHANICS.md](MARIO_GAME_MECHANICS.md) first
for how the current code actually works; this document proposes restructuring it, not a
description of what exists today.

## 0. The ask, restated precisely

Today, `activity/mario/` is one game's worth of code sitting directly on top of the
Guidebee Game Engine (GGE) — the same relationship Flappy Bird and Battle City have to
GGE. If we wanted to build a *second, different* platform game (not a Mario reskin —
a new game with its own levels, enemies, and identity, but the same genre of gameplay:
run/jump, tile collision, power-ups, a level-select menu, checkpoints), today's honest
answer is "copy `activity/mario/`'s shape and rewrite most of it," because almost nothing
below the game-specific art and level data is actually exposed as a reusable layer.

This document proposes a middle layer — call it the **platformer toolkit** — sitting
between GGE and any specific platform game, the same way GGE's own `microedition` package
(`LayerManager`/`Sprite`/`TiledLayer`, see
[12. The Microedition Game API](../tutorials/engine/12-microedition-game-api.md)) sits
between raw OpenGL and any MIDP-style game built on it:

```
Guidebee Game Engine (gameengine/)         — generic 2D framework: Stage/Actor,
                                              LayerManager/Sprite/TiledLayer, Box2D,
                                              atlases, audio, input. Untouched by this
                                              proposal.
        │
        ▼
Platformer toolkit (NEW — au.com.guidebee.morsetoolkit.platformer/)
                                            — generic run/jump/tile-collision game
                                              scaffolding: world model, collision
                                              pipeline, data-driven level loading via a
                                              tile-type registry, power-state actor base,
                                              save-state/menu/HUD/debug-panel patterns.
                                              What this document designs.
        │
        ├──────────────┬──────────────────────────
        ▼              ▼
activity/mario/    activity/<new-game>/          — content layers: art, level data,
(unchanged          (hypothetical — §8 sketches    game-specific actors, identity. Each
 gameplay,           what building one looks       is what "reskinning the engine" (not
 different code      like on top of the toolkit)   just the art) produces.
 organization)
```

The bar for "does this belong in the toolkit": **would a second, unrelated platform game
plausibly want this exact code, unchanged, with only its game-specific inputs swapped
out?** Section 2 below answers that question for every current class in
`activity/mario/`, with evidence from the actual source — this isn't a guess, most of the
answer was already determined while reading the code for the other documents in this set.

## 1. Why now, and why this is lower-risk than it sounds

Two things make this a good time to attempt this, and one thing bounds how far to take it:

- **Phase 2 is functionally done** (all 8 worlds playable, per
  [MARIO_PORT_PLAN_PHASE2.md](MARIO_PORT_PLAN_PHASE2.md)'s own status) and the reskin
  hasn't started. Restructuring *before* the reskin means verifying the refactor against
  familiar placeholder art (easy to spot a regression against something you already know
  looks/plays right) rather than against brand-new art at the same time — the exact
  reasoning [MARIO_RESKIN_PLAN.md §5](MARIO_RESKIN_PLAN.md) already used to justify
  "finish Phase 2 before reskinning," extended one layer further: finish *this*
  restructuring before the reskin too, for the same reason. See §7 for the explicit
  sequencing recommendation.
- **This codebase already over-corrected toward small, focused classes once** — the
  original `Mario.java`'s 2,348-line God-class was the very first problem
  [MARIO_PORT_PLAN.md §4](MARIO_PORT_PLAN.md) solved, by splitting it into ~15 single-
  responsibility classes. Extracting a toolkit layer is the same move one level up: most
  of the classes this document proposes moving already *are* single-responsibility and
  loosely coupled to Mario specifically — the work is mostly relocation plus parameterizing
  the handful of Mario-specific constants each one carries, not a rewrite.
- **The bound: don't extract from a sample size of one.** Every abstraction below is a
  *proposal* informed by what Mario's code looks like today, not a guarantee that it's the
  right shape for a second game nobody has designed yet. §6 makes this risk explicit and
  §7's migration plan ends with building a small second consumer specifically to test the
  boundary before treating it as settled — the standard "wait for the second use case"
  discipline for avoiding a premature, wrong abstraction.

## 2. Current-state audit: what's already generic vs. what's Mario-specific

Every class under `activity/mario/`, sorted by how much rework moving it to the toolkit
would need. This table is the evidence base for §3's package design.

### 2.1 Already generic — move near-verbatim, parameterize only

| Class | Why it's already generic |
|---|---|
| `world/TileMovement` | `moveX`/`moveY`'s wall/floor/ceiling clamp math reads only `Actor` (a GGE type) and `MarioWorld.containsImpassableArea` — no Mario-specific field or constant beyond the `MarioWorld` type name itself. |
| `world/OscillatorClock` | Three ever-advancing angles for synchronizing bobbing/rotating actors across a level — zero Mario-specific content, purely a shared-timer utility any game with orbiting/bobbing enemies would want. |
| `world/CameraController` | Clamped camera-window-follows-a-point math, parameterized entirely by constructor arguments (viewport size, level size) — no Mario type appears in it at all. |
| `world/MarioWorld.containsImpassableArea` | The tile-grid-plus-active-interactive-actor solidity query (see
[MARIO_GAME_MECHANICS.md §6](MARIO_GAME_MECHANICS.md#the-containsimpassablearea-algorithm)) is pure `TiledLayer` + actor-list algorithm; only its enclosing class's other members are Mario-specific. |
| Collision-resolver *pattern* (`collision/*Resolver`) | "One static method per interaction pair, called in a fixed order once per frame" is pure convention with zero Mario content in the pattern itself — see §3.4. |
| `Enemy`'s contract (`onStomped`/`onTouchedSide`/`onDefeatedByProjectile`/`bouncesOffEnemies`) | A three-verb reaction contract for "something touched this hazardous actor" — genuinely game-agnostic; only the *default implementations'* behavior (shrink/deactivate) encodes Mario rules, and even those are reasonable platformer-genre defaults, not Mario-specific ones. |
| `actors/items/Collectible`, `actors/hazards/Hazard`, `actors/lifts/LiftSurface` | All three are already minimal, self-contained interfaces with no Mario-specific members — see [MARIO_GAME_MECHANICS.md §9](MARIO_GAME_MECHANICS.md#9-actor-catalog). |

### 2.2 Generic shape, Mario-specific data — extract as a parameterized base + config

| Class | Generic shape | Mario-specific part to parameterize out |
|---|---|---|
| `state/GameStateController` | "score/coins/lives counter with a coin→life threshold and a pause flag" | `STARTING_LIVES=3`, `COINS_PER_LIFE=100`, `SCORE_PER_COIN=200` — three named constants, otherwise the whole class is generic |
| `state/MarioSaveState` | "a set of cleared level numbers backed by `Preferences`" | The hardcoded `PREF_NAME = "mario_save_state"` string only |
| `world/MarioContext` | "a static holder for the current level's `LayerManager`/world/game-state/player, so dynamically-spawned actors can reach them without constructor-threading" | The concrete `MarioWorld`/`Player` types — generalizes to a small type-parameterized holder |
| `level/LevelDefinition` | A placement record (`type`, `x`, `y`, `lengthX`, `lengthY`, a string field, two numeric fields) plus checkpoints/teleports/level-scoped flags | Nothing, structurally — see §3.3's note on why this schema is *already* game-agnostic in shape, even though its field names read Mario-flavored |
| `level/LevelCatalog` | "load-and-cache a JSON file by an integer id from a fixed assets path pattern" | The hardcoded `"mario/levels/level_" + n + ".json"` path template only |
| `screen/MarioMenuScreen` | World-grid → per-world level-list, with a lock policy based on "the previous level was cleared" | The world/level number table (`LevelNumbering.WORLD_LEVELS`), the title strings, and the lock-policy specifics |
| `hud/ScoreHud` | A score/lives/world-label/message HUD bar, screen-anchored against a zooming camera | The specific fields shown (score/coins/lives) and their exact text formatting |
| `hud/PauseOverlay` | A pause/resume overlay with buttons | Button labels/skin only |
| `debug/DebugPanel` | A data-driven warp panel generated from a `LevelDefinition`'s own checkpoints + an "interesting tile types" allow-list | The allow-list content and the specific debug toggles it exposes (god mode, power-state cycling are Mario-specific *concepts*, though the *pattern* "expose a few game-specific debug toggles" generalizes) |
| `MarioGamePlay` | "own cross-screen state (save/score), reverse-lookup a level's arrival tile from its predecessors' checkpoints, swap screens" | The concrete level list/table and screen types |
| `input/PlayerCommand` + `MarioInputController` | A held-direction + edge-triggered-action command struct, polled from keyboard-or-touch each frame | Field *names* read Mario-specific (`firePressed`) but the shape (2 held directions, 1-2 edge-triggered buttons, 1 held modifier) is standard platformer input |

### 2.3 Genuinely Mario-specific — stays in the content layer, but conforms to a new extension point

| Class | Why it stays put |
|---|---|
| `actors/player/Player` | Its **physics constants** (jump arc, run speed, water mode) are Mario's own tuned feel — per §6, deliberately *not* something to genericize. Its **scaffolding** (power-state enum, invincibility/star/shield timers, checkpoint/respawn, morph-transition flipbook machinery, forced-command scripting) is a reusable *pattern* worth extracting as a base class — see §3.2's `PowerStateActor`. |
| `level/LevelLoader`'s tile-type switch statements | This is the one piece of Mario's code that most obviously wants to become *data* the toolkit consumes rather than code the toolkit contains — see §3.3's `TileTypeRegistry`, the highest-leverage single change this document proposes. |
| Every concrete actor in `actors/{enemies,bricks,items,hazards,lifts,projectiles,scenery}` | These are the game's actual content — a new game writes its own, just like it draws its own sprites. |
| `world/SpawnController` | Its *pattern* ("read a level-scoped boolean+threshold flag, tick a delay timer, spawn near the camera edge") is worth documenting as a convention (§3.3), but its content (bombs, flying fish) is pure Mario data — not worth a shared base class for two ambient spawners. |
| `MarioConfiguration`, `MarioResourceManager`, `MarioGameActivity`, `MarioGameScreen` | The screen/activity/resource-manager *pattern* already matches Flappy Bird/Battle City's own convention (see [GAME_ENGINE.md](../GAME_ENGINE.md#the-lifecycle-activity--gameplay--screen)) — genuinely per-game glue code, not toolkit material. `MarioGameScreen` in particular (1060 lines) is the screen-level orchestrator; it *calls into* the toolkit's collision pipeline/level loader rather than being replaced by one. |

## 3. Proposed package design

A new top-level package, sibling to the existing per-game activity packages (not a new
Gradle module — see §6.3 for why):

```
app/src/main/java/au/com/guidebee/morsetoolkit/platformer/
  core/
    TileMetrics.java            // NEW - tile size as a value, not a global (§3.8)
    TileWorld.java              // generalized MarioWorld: TiledLayer + a registry of
                                 // typed actor lists (§3.1) + containsImpassableArea
    TileMovement.java           // moved verbatim from mario.world
    OscillatorClock.java        // moved verbatim from mario.world
    CameraFollow.java           // moved from mario.world.CameraController (renamed to
                                 // read as a behavior, not a Mario-specific noun)
    GameContext.java            // generalized MarioContext, see §3.5
  actor/
    Enemy.java                  // moved, contract unchanged (§2.1)
    Collectible.java, Hazard.java, LiftSurface.java, InteractiveTile.java
                                 // moved/renamed (InteractiveBrick -> InteractiveTile,
                                 // since "brick" is a Mario noun the interface itself
                                 // never actually depended on)
    PowerStateActor.java         // NEW - extracted scaffolding from Player, see §3.2
  collision/
    FrameResolver.java           // NEW - the one-method-per-pair contract, formalized
    CollisionPipeline.java       // NEW - runs an ordered list of FrameResolvers once
                                  // per frame, see §3.4
  level/
    LevelDefinition.java         // moved, schema unchanged (§3.3)
    LevelCatalog.java            // generalized path template
    TileTypeRegistry.java        // NEW - the highest-leverage abstraction, see §3.3
    LevelLoader.java             // generalized: iterates tiles, dispatches through the
                                  // registry instead of a hardcoded switch
  state/
    LevelProgressState.java      // generalized MarioSaveState (namespaced Preferences)
    ScoreLivesState.java         // generalized GameStateController (configurable
                                  // starting lives / coin-to-life threshold / score value)
  hud/
    StatusBar.java               // generalized ScoreHud
    PauseOverlay.java            // generalized, moved
  screen/
    WorldLevelSelectScreen.java  // generalized MarioMenuScreen, see §3.6
  debug/
    LevelWarpPanel.java          // generalized DebugPanel, see §3.7
  input/
    PlatformerCommand.java       // generalized PlayerCommand
    TouchOrKeyboardInput.java    // generalized MarioInputController
```

Each section below sketches the highest-value abstractions concretely enough to
implement from — not full source, but real method signatures and the reasoning behind
each design choice.

### 3.1 `TileWorld`: from 7 hardcoded lists to a typed registry

Today, `MarioWorld` hardcodes exactly the 7 actor-list types Mario happens to need
(`bricks`, `collectibles`, `enemies`, `fireBalls`, `lifts`, `hazards`, `axes` — see
[MARIO_GAME_MECHANICS.md §5](MARIO_GAME_MECHANICS.md#5-the-world-model-marioworld)). A
new game might need a different set (say, no "axes" concept at all, but a "checkpoints
flags" list Mario doesn't have). Generalize to a small type-keyed registry:

```java
public class TileWorld extends TiledLayer {
    private final Map<Class<?>, List<Object>> actorLists = new HashMap<>();

    public <T> void register(Class<T> type, T actor) {
        listFor(type).add(actor);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> listFor(Class<T> type) {
        return (List<T>) actorLists.computeIfAbsent(type, k -> new ArrayList<>());
    }

    // containsImpassableArea(...) moves here verbatim, generalized to query
    // whichever registered list implements a small `Solid`-like marker interface
    // (or, simplest: keep it Sprite/InteractiveTile-specific, since that's the
    // only list every platformer needs solidity queries against in practice).
}
```

`MarioWorld` becomes a thin, still-real subclass or a plain instance of `TileWorld` that
just calls `listFor(Enemy.class)`, `listFor(InteractiveTile.class)`, etc. — Mario's own
code barely changes call-site shape (`world.getEnemies()` → `world.listFor(Enemy.class)`,
or keep the named convenience methods as one-line wrappers for readability, whichever the
team prefers when implementing).

### 3.2 `PowerStateActor`: the reusable half of `Player`

`Player`'s 1150 lines split cleanly along the boundary [MARIO_GAME_MECHANICS.md §4](MARIO_GAME_MECHANICS.md#4-player-mechanics)
already documents: **movement physics** (Mario's own tuned jump/run/water constants —
stays in `activity/mario`) versus **state-machine scaffolding** (timers, transitions,
checkpoints — genuinely reusable). The scaffolding half becomes an abstract base:

```java
public abstract class PowerStateActor<S extends Enum<S>> extends Layer {
    protected S powerState;
    private float invincibleTimer, shieldTimer;
    private TextureRegion[] transitionFrames;   // morph-flipbook machinery
    private float checkpointX, checkpointY;     // respawn point
    // ... consumeDeath()/beginDeathAnimation()/setInvincibleFor()/isInvincible(),
    //     updateCheckpoint()'s "promote respawn point after N seconds grounded" logic,
    //     setForcedCommand()/clearForcedCommand() scripted-sequence override —
    //     all moved verbatim, since none of it reads a Mario-specific constant.

    /** The one thing a subclass must supply: how to actually move this frame. */
    protected abstract void applyMovement(PlatformerCommand command, float frames);

    /** How to render the current power state's frame - subclass owns its own atlas regions. */
    protected abstract void paintPowerState(Batch batch);
}
```

Mario's `Player` becomes `class MarioPlayer extends PowerStateActor<PlayerPowerState>`,
implementing `applyMovement` with exactly today's `applyHorizontalInput`/`applyJump`/
`applyGravity`/`moveXWithCollision`/`moveYWithCollision` body, unchanged. A future game's
player character gets the same invincibility/checkpoint/transition scaffolding for free
and writes its own movement feel from scratch — which is exactly right, since movement
feel is the one thing that should never be shared wholesale between two different games
(see §6.2).

### 3.3 `TileTypeRegistry`: the single highest-leverage change

This is the change worth prioritizing first (§7, Phase B) because it's where the current
code is most obviously "data pretending to be code." Today,
`LevelLoader.spawnBricks`/`spawnEnemies`/`spawnHazards`/`spawnItems`/`spawnLifts`/
`spawnScenery` are six hardcoded `switch` statements over `tile.type` strings (the full
registry is documented in [MARIO_GAME_MECHANICS.md §8](MARIO_GAME_MECHANICS.md#8-tile-type-dispatch-registry)).
A second game would need to copy-paste and rewrite all six switches for its own tile
vocabulary. Instead:

```java
public interface TileHandler {
    /** Spawns whatever this tile type produces into the world/context. Most
     *  handlers are a one-line lambda, matching today's LevelLoader cases exactly. */
    void spawn(LevelDefinition.Tile tile, LevelDefinition level, GameContext<?, ?, ?> ctx);
}

public final class TileTypeRegistry {
    private final Map<String, TileHandler> handlers = new HashMap<>();

    public TileTypeRegistry register(String type, TileHandler handler) {
        handlers.put(type, handler);
        return this;  // chainable, so a game's registry setup reads as one fluent block
    }

    void spawnAll(LevelDefinition level, GameContext<?, ?, ?> ctx) {
        for (LevelDefinition.Tile tile : level.tiles) {
            TileHandler handler = handlers.get(tile.type);
            if (handler != null) {
                handler.spawn(tile, level, ctx);
            }
        }
    }
}
```

`LevelLoader` (toolkit-side) shrinks to: build the `TiledLayer`-backed static geometry
(still needs a small `staticTerrainTypes` set/predicate a game supplies, since "which
strings mean baked tile-grid cells" is inherently game data), then call
`registry.spawnAll(level, ctx)` once. Mario's own `LevelLoader` today becomes
`MarioTileRegistry` — a single static method building one `TileTypeRegistry` with ~50
`.register("Brick", (tile, level, ctx) -> ...)` calls, i.e. **the exact same logic in
every case, just inverted from "switch statement the loader owns" to "table the game
populates."** A new game writes its own equivalent table for its own vocabulary and the
generic `LevelLoader`/`LevelDefinition`/`LevelCatalog`/JSON pipeline (level converter
tooling aside — that was always a one-time offline step, see
[MARIO_PORT_PLAN.md §3](MARIO_PORT_PLAN.md)) needs zero changes to support it.

**Why `LevelDefinition`'s schema already generalizes without renaming anything:** its
`Tile` record (`type: String, x/y/lengthX/lengthY: int, extraInfo: String, bridgeLength/
patrolLength: int`) reads Mario-flavored in its field *names*, but structurally it's just
"a typed placement with a position, a size, and three free-form parameter slots (one
string, two ints)" — every one of Mario's ~50 tile types already reinterprets those three
slots completely differently per type (`extraInfo` means spin direction for `FireBar` but
nothing at all for `Brick`; `patrolLength` means a patrol range in tiles for
`EnemyTurtlePatrol` but a bridge-end wall bound for `Boss`). A new game can do exactly the
same thing with its own types without touching the schema. Leave the field names as-is
(renaming to generic `param1`/`param2` would just make Mario's own registry *less*
readable for no real gain) and document the convention instead — this doc doing exactly
that is the documentation.

### 3.4 `CollisionPipeline`: formalizing the existing convention

[MARIO_GAME_MECHANICS.md §3](MARIO_GAME_MECHANICS.md#3-the-frame-loop) already documents
today's convention precisely: "one static method per interaction pair, called in a fixed
order once per frame." Formalize it as a real, tiny interface plus a runner instead of
`MarioGameScreen.render`'s current 8 hardcoded call lines:

```java
public interface FrameResolver {
    void resolve(TileWorld world);
}

public final class CollisionPipeline {
    private final List<FrameResolver> resolvers;

    public CollisionPipeline(FrameResolver... resolvers) {
        this.resolvers = List.of(resolvers);
    }

    public void resolveAll(TileWorld world) {
        for (FrameResolver r : resolvers) {
            r.resolve(world);
        }
    }
}
```

Mario's screen builds its pipeline once (`new CollisionPipeline(this::resolvePickups,
EnemyCollisionResolver::resolve, EnemyToEnemyResolver::resolve, ...)`, method references
satisfy the functional interface with zero wrapper-class churn) and calls
`pipeline.resolveAll(world)` once per frame instead of 8 named calls. This buys nothing
functionally for Mario alone — the value is purely that a new game's screen becomes "build
your own resolver list, same runner," rather than needing its own copy of the
render-loop's resolver-ordering logic.

### 3.5 `GameContext<TPlayer, TWorld, TGameState>`

`MarioContext` generalizes almost by deletion — replace the three concrete types with
type parameters:

```java
public class GameContext<TPlayer, TWorld extends TileWorld, TGameState> {
    private LayerManager layerManager;
    private TWorld world;
    private TGameState gameState;
    private TPlayer player;
    // init(...)/world()/player()/setPlayer()/gameState()/spawn(Layer) unchanged
}
```

Mario declares `class MarioContext extends GameContext<MarioPlayer, MarioWorld,
GameStateController> { private static final MarioContext INSTANCE = new MarioContext(); ... }`
(or keeps the existing static-holder shape parameterized the same way) — call sites
(`MarioContext.world()`, `MarioContext.spawn(...)`) don't change at all.

### 3.6 `WorldLevelSelectScreen`

Generalizes `MarioMenuScreen`'s world-grid → level-list flow behind a small data +
policy pair the game supplies:

```java
public interface LevelUnlockPolicy {
    boolean isUnlocked(int worldIndex, int levelNumber);
}

public class WorldLevelSelectScreen extends ScreenAdapter {
    public WorldLevelSelectScreen(int[][] worldLevels, LevelUnlockPolicy unlockPolicy,
                                   Function<Integer, String> levelLabeler,
                                   String title, Skin skin,
                                   Consumer<Integer> onLevelSelected, Runnable onExit) { ... }
}
```

Mario's own menu becomes a thin construction call passing `LevelNumbering.WORLD_LEVELS`,
a policy backed by `MarioSaveState`, `LevelNumbering::label`, `"SUPER MARIO BROS"` (or
whatever the reskin renames it to), and `gamePlay::startLevel`/`gamePlay::finish`. The
debug-build "every level unlocked, tinted orange" behavior
([MARIO_GAME_MECHANICS.md §12](MARIO_GAME_MECHANICS.md#12-debugqa-tooling)) moves into the
generic screen too, gated on a `boolean debugBypassUnlock` constructor flag rather than a
Mario-specific `BuildConfig` reference baked into toolkit code.

### 3.7 `LevelWarpPanel`

`DebugPanel`'s core idea — "generate warp buttons from whatever's already in this level's
own `LevelDefinition.checkpoints`/`tiles`, no per-level authoring" — is already written
generically enough to lift almost unchanged; the only per-game input is the "which tile
*types* are worth a warp button" allow-list, which becomes a constructor parameter
(`List<String> interestingTileTypes`) instead of Mario's own hardcoded list. A new game's
debug tooling is then "supply your own short list of type strings," not a rewrite.

### 3.8 `TileMetrics`: making tile size a first-class, non-global value

A concrete requirement worth designing in explicitly, not leaving implicit: **the
toolkit itself must not hardcode a tile size anywhere**, so a future game can pick 16px,
48px, or 64px tiles as freely as it picks its own art. This is a real gap in today's code,
audited directly rather than assumed — worth stating precisely before proposing the fix.

**What's already fine:** GGE's own `TiledLayer` constructor takes `tileWidth`/`tileHeight`
as plain arguments (`gameengine/.../microedition/TiledLayer.java`) — it doesn't assume 32,
or even assume square tiles. The engine layer was never the obstacle.

**What isn't fine today, audited by grep + reading, not guessed:**

1. `MarioConfiguration.TILE_SIZE` is a `public static final int = 32`, imported directly
   by **26 files / 71 call sites** across `activity/mario` for tile-index ↔ world-pixel
   conversion (`LevelLoader`, `MarioWorld`, `TileMovement`, HUD/debug code, ...). Every one
   of these is mechanically easy to retarget — they already go through one named constant.
2. **21 more classes** (`Boss`, `EnemyTurtle`, `Helmet`, `Coin`, `Spring`, `Player`, `Axe`,
   `Fireworks`, ... — the full list is longer than this) each declare their *own* local
   `FRAME_WIDTH`/`FRAME_HEIGHT`/`FRAME_SIZE = 32` constant, numerically equal to
   `TILE_SIZE` today purely by coincidence, not by reference. This matters more than it
   sounds: GGE's `Sprite(TextureRegion, frameWidth, frameHeight)` constructor
   (`gameengine/.../microedition/Sprite.java`) feeds that same number into
   `super(0, 0, frameWidth, frameHeight, true)` — i.e. it sets **both** the atlas-slice
   size **and** the actor's actual collision hitbox in one call. These 21 numbers are
   quietly load-bearing for physics, not just art, and nothing in the compiler enforces
   that they stay equal to `TILE_SIZE`. This is a real, larger count than
   [MARIO_RESKIN_PLAN.md §4.1](MARIO_RESKIN_PLAN.md#41-resolution-architecture-this-is-not-a-pure-content-swap)
   originally estimated ("three or four call sites") for the *same underlying problem* —
   that estimate covered only `PlayerPowerState`/`Scenery`/`Lift`; this audit found the
   pattern repeats across most of the enemy/item/fx roster. Correcting that estimate here
   rather than leaving it stale in the reskin plan.
3. **A handful of spots do raw tile-relative arithmetic with no constant reference at
   all** — `Boss.java`'s `((int) getY() / 32) * 32` (snapping a fireball spawn to a tile
   row), `BossFire.java`'s `(6 + RANDOM.nextInt(4)) * 32` (randomizing among floor
   levels), `EnemyTurtlePatrol`/`FlyingTurtlePatrol`'s `x + 32 * patrolLengthTiles` /
   `y + 32 * patrolLengthTiles` (patrol-range math), `FlagWinBanner`'s
   `START_OFFSET_Y = -96f` / `RISE_DISTANCE = 64f`, and `Player`'s own
   `DUCK_HEAD_ROOM_PX = 32f` / `DUCK_OVERHEAD_CLEARANCE_PX = 48f`. These would silently
   misbehave (not crash) if `TILE_SIZE` ever changed without also finding and updating
   each of them by hand.

**The design fix — thread tile size through the object graph instead of importing a
global:**

```java
/** Immutable, per-game world-grid metrics. Square tiles only, per this doc's own scope,
  * but modeled as a real value rather than a bare int so a future non-square need
  * (unlikely, but cheap to leave room for) isn't a breaking change later. */
public final class TileMetrics {
    public final int tileSize;
    public TileMetrics(int tileSize) { this.tileSize = tileSize; }
}
```

- `TileWorld` (§3.1) takes a `TileMetrics` in its constructor alongside `cols`/`rows`/the
  tiles region, and exposes `tileSize()` — the one, single source of truth for "how big is
  a tile in this world," readable off the object every other class already receives a
  reference to.
- `TileMovement.moveX/moveY` and `containsImpassableArea` already take a `TileWorld`
  parameter — they read `world.tileSize()` instead of a static import. **Zero new
  parameters needed**, since the world reference was always there; this alone fixes
  bucket 1 (the 26-file/71-call-site category) for free.
- `TileTypeRegistry` handlers (§3.3) receive a `GameContext` — add `int tileSize()` there
  too, so every actor constructor a handler calls can take tile size as an explicit
  constructor argument instead of reading a global. This is where buckets 2 and 3 actually
  get fixed: it's mechanical (thread one more `int` through constructors that don't have
  it yet) but it *is* real, non-zero work — budget it as part of Phase B (§7), since it
  touches the exact same call sites that phase already rewrites.
- The atlas-packer convention (`tools/mario-atlas-packer`, per
  [MARIO_GAME_MECHANICS.md §13](MARIO_GAME_MECHANICS.md#13-sprite-sheets-and-the-atlas-system))
  keeps its own `cols × rows` frame-grid metadata independent of pixel size — no change
  needed there; a new game's packer tool just targets its own chosen `TileMetrics.tileSize
  × artScale` when slicing.

**What this design deliberately does *not* attempt — and why:** it does not try to make
an *already-tuned* game's tile size changeable "for free," and it does not make physics
constants automatically scale with tile size. Two reasons, both important:

- **Feel is not derivable from grid size alone.** `Player`'s `MAX_SPEED=60`,
  `GRAVITY_STEP=0.42`, `JUMP_BASE=-11` are absolute pixels-per-tick numbers, not
  expressed as multiples of `TILE_SIZE` — deliberately, since they were carried over
  unchanged from the original desktop game's own tuned feel
  ([MARIO_GAME_MECHANICS.md §4.1](MARIO_GAME_MECHANICS.md#41-the-frames-unit)). If a
  *toolkit* auto-derived "jump height = 1.4× tile height" and re-scaled every constant
  whenever tile size changed, two different tile sizes for the *same* absolute constants
  would still produce different-*feeling* jump arcs relative to gaps/enemy heights sized
  in tiles — there's no formula that makes that automatic and correct, only per-game
  playtesting can. Trying to auto-solve this would be exactly the kind of premature,
  wrong abstraction §6.1 already warns about.
- **This is the same lesson
  [MARIO_RESKIN_PLAN.md §4.1](MARIO_RESKIN_PLAN.md#41-resolution-architecture-this-is-not-a-pure-content-swap)
  already learned the hard way** about not conflating two things that happen to be equal
  today only by coincidence — there, world-space size vs. texture-pixel size; here,
  grid granularity vs. tuned movement feel.

**So, concretely, what tile-size independence actually buys:**

- **For a *new* game built on the toolkit:** picking `tileSize=48` (say) instead of `32`
  is a true one-line `TileMetrics` change, with zero hunting for scattered literals —
  because the game's actors, registry handlers, and physics constants are all being
  written fresh *against* that chosen size from the start, the same way Mario's own `32`
  and its constants were chosen together once and never revisited.
- **For changing an *existing*, already-tuned game's tile size later (e.g. Mario
  itself):** this design makes it *find-able* (one `TileMetrics` value plus every
  constructor call site that now explicitly takes `tileSize`, rather than 35-40 scattered
  literals) but does not make it *free* — re-tuning `MAX_SPEED`/`GRAVITY_STEP`/`JUMP_BASE`
  and every tile-relative offset against the new grid, then a full
  [MARIO_LEVEL_ATLAS.md](MARIO_LEVEL_ATLAS.md)-driven regression pass, is still required
  and still a feel decision a person makes, not a mechanical migration. Document that
  distinction plainly to whoever picks this up — "tile-size-independent engine" should not
  be read as "changing tile size has no consequences."

## 4. The content-layer contract

Once §3 exists, building a new platform game on the toolkit means supplying exactly these
things — this list doubles as the acceptance test for whether the abstraction in §3 is
complete enough (if building the example in §8 needs something not on this list, the
toolkit design is missing a piece):

1. **A `TileTypeRegistry` population** — the new game's equivalent of `MarioTileRegistry`,
   one `.register(...)` call per tile type it needs.
2. **Actor classes** — a `PowerStateActor` subclass for the player (or a plain custom
   actor, if the new game doesn't want power-ups at all — nothing forces that pattern on
   it), `Enemy`/`InteractiveTile`/`Collectible`/`Hazard`/`LiftSurface` subclasses for its
   own content.
3. **Level JSON data** matching `LevelDefinition`'s schema, under its own asset path
   (`LevelCatalog`'s path template is a constructor parameter, not hardcoded to
   `mario/levels/`).
4. **An atlas + packer tool**, following the `Theme`/`AssetSpec`/`TerrainTile` *convention*
   `tools/mario-atlas-packer` established (see
   [MARIO_GAME_MECHANICS.md §13](MARIO_GAME_MECHANICS.md#13-sprite-sheets-and-the-atlas-system))
   — this stays a per-game offline dev tool, not shared code, since each game's actual
   asset list is entirely its own; only the *pattern* (per-theme atlases, magenta masking,
   per-cell flip for the engine's y-down convention) is worth copying deliberately rather
   than reinventing.
5. **A `TileMetrics` (§3.8) plus a config object** analogous to `MarioConfiguration`
   (viewport, physics tuning) — the toolkit can supply sane defaults, but every real game
   will pick its own tile size and every physics constant, since feel is inherently
   game-specific (§6.2).
6. **Screens/Activity/GamePlay/ResourceManager** — thin, per-game glue following the
   existing `GameActivity`/`GamePlay`/`ScreenAdapter` convention every game in this app
   already uses (see [GAME_ENGINE.md](../GAME_ENGINE.md#the-lifecycle-activity--gameplay--screen))
   — this was never going to be shared code across games even before this proposal, and
   stays that way.
7. **A save-state namespace and world/level table** feeding `WorldLevelSelectScreen`.
8. **HUD/menu strings and skin** — content, not code.

## 5. What definitely does *not* change

- **`gameengine/`** — zero changes. The toolkit is a consumer of GGE's existing public
  API (`microedition`, `scene`, `ui`, `graphics`, `audio`), exactly as `activity/mario`
  is today.
- **Mario's gameplay feel, level data, or art.** Done correctly, this restructuring is
  invisible to a player — same physics constants, same 55 levels, same enemy behaviors,
  just reorganized code underneath. The regression test *is* "does Mario still play
  identically," per §7's own gate on every phase.
- **Flappy Bird / Battle City.** Neither needs to adopt the toolkit; both stay exactly as
  they are. (Whether either *could* benefit from parts of it — Flappy Bird's score
  persistence looks a lot like `LevelProgressState`'s shape, for instance — is a genuinely
  interesting follow-up question, but explicitly out of scope here: retrofitting a working
  game to a new internal API it doesn't need is a cost with no player-facing benefit,
  the same reasoning [MARIO_RESKIN_PLAN.md §1.4](MARIO_RESKIN_PLAN.md) already applied to
  *not* renaming Mario's own internal class names for the reskin.)

## 6. Risks and explicit trade-offs

### 6.1 Premature abstraction — the biggest risk

Every interface in §3 is inferred from **one** real example. The classic failure mode
here is designing an API around what one consumer happens to need, then discovering a
second real consumer needs something the API can't express — at which point the "reusable
toolkit" either grows awkward escape hatches or gets partially bypassed, and the
abstraction effort was wasted or worse (harder to work with than no abstraction at all).
§7's migration plan deliberately ends with building a small second consumer *before*
calling any of this "done" — that's the actual test, not code review of the toolkit in
isolation.

### 6.2 Never genericize movement feel itself

`Player`'s physics constants (`MAX_SPEED=60`, `GRAVITY_STEP=0.42`, `JUMP_BASE=-11`, ...)
are Mario's own tuned feel, carried over deliberately unchanged from the original desktop
game (see [MARIO_GAME_MECHANICS.md §4.1](MARIO_GAME_MECHANICS.md#41-the-frames-unit)).
`PowerStateActor` (§3.2) extracts the *scaffolding* around movement (timers, transitions,
checkpoints) but leaves `applyMovement` abstract for exactly this reason — a future game's
jump arc and run speed should never come from a shared base class's defaults. This mirrors
[MARIO_RESKIN_PLAN.md §4.1](MARIO_RESKIN_PLAN.md#41-resolution-architecture-this-is-not-a-pure-content-swap)'s
own hard-won lesson about not conflating two things that happen to be equal today only by
coincidence — there, world-space size vs. texture pixel size; here, state-machine
scaffolding vs. tuned physics constants.

### 6.3 Where the toolkit should live: a package, not a Gradle module — for now

`gameengine/` is a real Gradle module because it compiles native code (Box2D via
`ndkBuild`) and is genuinely meant to be reusable outside this specific app (it has its
own upstream repo, per [GAME_ENGINE.md](../GAME_ENGINE.md)). The platformer toolkit
proposed here is pure Java, has exactly one real consumer today, and gains nothing from
module boundaries that a plain package doesn't already give it (Gradle modules add build
complexity — separate `build.gradle`, dependency wiring, potential versioning — for zero
benefit until something outside this app actually needs to depend on it independently).
**Recommendation: a plain package under `app/src/main/java/.../platformer/`, promoted to
its own module only if and when a second real app or a public-release plan for the toolkit
itself materializes.** This is the same YAGNI judgment this codebase's own documentation
already applies elsewhere (e.g. [MARIO_PORT_PLAN_PHASE2.md §6](MARIO_PORT_PLAN_PHASE2.md)'s
save-state note: "resist adding more than [what's] actually needs[ed]").

### 6.4 Sequencing against the reskin — decided

**Decided (2026-09-08): the full platformer re-architecture (§7's Phases A–G — the
toolkit extraction plus migrating Mario itself onto it, tile-size independence per §3.8
included) lands first, in full; the reskin
([MARIO_RESKIN_EXECUTION.md](MARIO_RESKIN_EXECUTION.md)) starts only once Mario is running
on the new toolkit end to end.** `TILE_SIZE` itself stays `32` throughout — this
restructuring is an internal code-organization change, not a resolution change; `ART_SCALE`
(per [MARIO_RESKIN_PLAN.md §4.1](MARIO_RESKIN_PLAN.md#41-resolution-architecture-this-is-not-a-pure-content-swap))
remains the actual, separate mechanism the reskin uses for higher-resolution art, and lands
during the reskin itself, not as part of this restructuring.

This is a firmer commitment than this document's earlier draft of this section proposed
(which tried to carve out only the minimum slice needed for tile-size independence and
let the rest of the toolkit extraction happen independently of the reskin's timing) — the
team's call, recorded here as the decision that actually governs execution order. The
reasoning it still shares with that earlier draft, and with
[MARIO_RESKIN_PLAN.md §5](MARIO_RESKIN_PLAN.md)'s own precedent for "finish Phase 2 fully
before reskinning": verifying a *code* restructuring is behavior-neutral is far easier
against art and levels already known intimately than against brand-new art at the same
time, and every call site the reskin's own `ART_SCALE` work would otherwise touch gets
touched once (during this restructuring, where tile size stops being a scattered literal)
rather than twice.

**Phase H (§7) — building a small second consumer to validate the abstraction — is *not*
part of this commitment.** It has no interaction with Mario or the reskin either way
(§6.1's premature-abstraction risk is about trusting the *design*, not about blocking
Mario's own migration), so it stays exactly what §7 already scopes it as: something to do
once a second platform game is actually planned, on no schedule this document sets.

**Concretely: run §7's Phases A–G to completion against the *current* placeholder art,
confirm the full [MARIO_LEVEL_ATLAS.md](MARIO_LEVEL_ATLAS.md) regression checklist passes
with zero gameplay/visual change at every phase boundary, then start
[MARIO_RESKIN_EXECUTION.md](MARIO_RESKIN_EXECUTION.md).**

### 6.5 Cost this doesn't pay for

This restructuring buys **zero** player-visible benefit for Mario on its own — it's pure
internal engineering investment whose payoff only materializes when a second game gets
built. If there's no concrete plan or strong intent to build a second platform game in
this app, the honest recommendation is: **don't do this yet.** Keep
[MARIO_GAME_MECHANICS.md](MARIO_GAME_MECHANICS.md)'s extension recipes (§14 of that
document) as the path for extending *Mario itself* — they're sufficient for that, and
this document's abstractions only start paying rent once a second consumer exists to
justify their maintenance cost (an API with one caller is documentation with extra steps).

## 7. Migration plan

Phased, each phase gated on a full regression pass (the same discipline
[MARIO_PORT_PLAN_PHASE2.md §5](MARIO_PORT_PLAN_PHASE2.md) already used: "test the first
thing that introduces a mechanic thoroughly, treat repeats as lighter smoke tests" — here,
every phase's "mechanic" is *itself still being Mario, unchanged*, so the regression bar
is simply "nothing about how Mario plays or looks changed"). Don't start a phase until the
previous one's regression pass is clean.

**For the literal, file-by-file version of every phase below — exact classes, exact call
sites (grep-verified, not estimated), exact new signatures — see
[PLATFORMER_ENGINE_IMPLEMENTATION.md](PLATFORMER_ENGINE_IMPLEMENTATION.md).** That
document also corrects one thing the summary below simplifies: `TileMovement` can't
actually move to `platformer.core` "verbatim" as Phase A states, since its signature
names `MarioWorld` directly — a small `TileCollisionSource` interface (detailed there)
closes that gap without changing this phase's zero-logic-change bar.

**Phase A — Move the zero-coupling utilities.** `TileMovement`, `OscillatorClock`,
`CameraFollow` (renamed from `CameraController`) move to `platformer.core` verbatim; every
call site in `activity/mario` updates its import only. Zero logic changes anywhere.
*Exit: full regression pass, identical to before the move.*

**Phase B1 — `TileMetrics` threading (tile-size independence).** This is the concrete
"make it tile-size independent" work, and it's narrower than standing up the full registry
in B2 below, which is why it's split out and sequenced first: introduce `TileMetrics`
(§3.8), have `TileWorld` (still `MarioWorld` at this point — no registry rewrite needed
yet) hold and expose `tileSize()`, point `TileMovement`/`containsImpassableArea` at it
instead of the static import, and — the real work — thread an explicit `tileSize`
parameter through every one of §3.8's ~40 audited call sites (the 26-file/71-reference
`TILE_SIZE`-import category is a mechanical find-and-replace; the 21 `FRAME_WIDTH=32`-style
classes and the handful of raw-arithmetic spots each need their constructor to actually
take the value instead of hardcoding it). *Exit: full regression pass, zero
gameplay/visual change — same bar as Phase A.* Doing this before B2 means B2's own
constructor rewrites (below) take `tileSize` as a parameter from the start, rather than
needing a second pass over the same call sites later.

**Phase B2 — `TileTypeRegistry`.** Introduce the registry + generic `LevelLoader` in
`platformer.level`; write `MarioTileRegistry` reproducing every existing `LevelLoader`
switch-statement case as a `.register(...)` call, one-for-one (each now taking `tileSize`
from B1's `GameContext`/`TileWorld`, already threaded — no second pass over those call
sites). Delete the old switch statements only once the new registry-driven path is
confirmed to spawn byte-for-byte the same actors on a full 8-world playthrough.

**Phase C — `GameContext`.** Generalize `MarioContext` per §3.5. Purely mechanical
(type-parameter introduction); low risk.

**Phase D — `CollisionPipeline`.** Formalize the resolver-ordering convention per §3.4.
Purely mechanical; `MarioGameScreen.render`'s 8 call lines become one `pipeline.resolveAll(world)`
call plus a one-time pipeline construction.

**Phase E — `PowerStateActor`.** Extract `Player`'s scaffolding (timers, checkpoints,
morph-transition machinery) into the base class per §3.2, leaving `applyMovement`
abstract. Note this no longer needs to bundle the `ART_SCALE` change the way an earlier
draft of this plan suggested — per §6.4's decision, the entire toolkit migration
(including this phase) now runs to completion *before* the reskin starts, so `ART_SCALE`
(which is a reskin-time concern, not a toolkit one) simply hasn't landed yet when this
phase happens; `MARIO_RESKIN_EXECUTION.md`'s Step R.1 lands it afterward, against
whatever `Player`/`PowerStateActor` looks like once this phase is done.

**Phase F — HUD/menu/save-state/debug panel.** `WorldLevelSelectScreen`, `StatusBar`,
generalized `PauseOverlay`, `LevelWarpPanel`, `LevelProgressState`. Mostly mechanical
parameter-extraction per §2.2's table; moderate risk only in `WorldLevelSelectScreen`
given its UI-layout fragility already noted in `MarioMenuScreen`'s own class doc
(non-`setFillParent` positioning quirks) — test on-device, not just in code review.

**Phase G — Input.** `PlatformerCommand`/`TouchOrKeyboardInput`. Low risk, purely a rename
+ field-list generalization.

**Phase H — Build a second, minimal consumer.** Not a real shipped product — a throwaway
vertical slice (even a single tiny level, one enemy type, no reskin-quality art needed)
built entirely on the toolkit from §3–§4's contract, with zero toolkit code changes
allowed unless the exercise genuinely finds a gap. This is the actual test of whether §3's
design is right; treat anything it reveals as a real finding to fix before calling the
toolkit stable, not as scope creep to defer. *Exit: the second consumer runs, and Mario's
own full regression pass is still clean after every toolkit change this phase required.*

## 8. Illustrative sketch: what a second game looks like on top of this

To make §4's contract concrete rather than abstract, a short sketch of a hypothetical
second platform game ("Signal Runner" — a throwaway placeholder name, not a real product
plan) built on the toolkit, touching every item in that list:

```java
// 1. Tile registry — this game's own vocabulary, nothing shared with Mario's types.
TileTypeRegistry registry = new TileTypeRegistry()
    .register("Girder", (tile, level, ctx) -> add(ctx, new Girder(tile.x, tile.y)))
    .register("Drone", (tile, level, ctx) -> addEnemy(ctx, new Drone(tile.x, tile.y)))
    .register("BatteryCell", (tile, level, ctx) ->
        addCollectible(ctx, new BatteryCell(tile.x, tile.y)));

// 2. Actors
class Runner extends PowerStateActor<RunnerState> {
    protected void applyMovement(PlatformerCommand cmd, float frames) {
        // this game's own jump/run feel — nothing borrowed from Mario's constants
    }
}
class Drone extends Enemy {
    public void onStomped(Runner r) { /* this game's own reaction */ }
}

// 3. Level data: assets/signalrunner/levels/level_1.json, same LevelDefinition schema.

// 4/5. Its own PackSignalRunnerAtlas tool + SignalRunnerConfiguration
//      (tile size, viewport, physics constants — all its own).

// 6. Screens: SignalRunnerGameActivity/GamePlay/GameScreen, same shape as MarioGameScreen,
//    but its render() builds its own CollisionPipeline and calls the generic LevelLoader.

// 7/8. Its own save-state namespace, world/level table, HUD strings, skin.
```

Nothing above needed a change to `platformer.*` — that's the acceptance criterion from §4.
If writing this sketch for real (Phase H) turns up something that genuinely does need a
toolkit change, that's exactly the kind of finding this phased plan exists to catch before
the design is treated as settled.

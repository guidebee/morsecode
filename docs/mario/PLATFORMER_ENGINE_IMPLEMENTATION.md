# Platformer Engine Re-Architecture: Implementation Plan

**Status: planning only — the runbook to follow when implementation starts, nothing
executed yet.** This is the actionable companion to
[PLATFORMER_ENGINE_ARCHITECTURE.md](PLATFORMER_ENGINE_ARCHITECTURE.md) (the design and
rationale) — the same relationship [MARIO_RESKIN_PLAN.md](MARIO_RESKIN_PLAN.md) has to
[MARIO_RESKIN_EXECUTION.md](MARIO_RESKIN_EXECUTION.md). Read the architecture doc first;
this document turns its §3 designs and §7 phase list into literal, file-by-file steps,
grounded in a real grep-and-read audit of the current code (every file/line cited below
was actually checked, not estimated).

Per [MARIO_RESKIN_PLAN.md §5](MARIO_RESKIN_PLAN.md#5-ordering-phase-2--platformer-re-architecture--reskin-as-sequential-passes),
this whole plan (Phases A–G) runs **before** [MARIO_RESKIN_EXECUTION.md](MARIO_RESKIN_EXECUTION.md)
starts.

## 0. Ground rules for every phase

- **No compilation is available in the environment this plan was written in** (no Android
  SDK/NDK, `gradle.properties` targets a Windows-only JDK path) — every step below was
  verified by reading the actual source and cross-checking every call site by hand, not by
  building. **Build and run the full [MARIO_LEVEL_ATLAS.md](MARIO_LEVEL_ATLAS.md)
  regression pass on your own machine after each phase**, and don't start the next phase
  until that pass is clean — the same discipline every other plan in this doc set uses.
- One phase per commit/PR. Never combine a pure move/rename with a logic change in the
  same commit — if something breaks, you want to know instantly which kind of change did
  it.
- Where a phase says "zero logic change," that's a literal correctness bar, not a goal —
  if achieving it requires an extra small step (see Phase A's `TileCollisionSource`
  interface below, added specifically to keep the move truly logic-free), take the extra
  step rather than relaxing the bar.
- Mark each numbered step `[ ]`/`[x]` as you go, the same convention
  [MARIO_PORT_PLAN_PHASE2.md](MARIO_PORT_PLAN_PHASE2.md) uses — this doc is meant to be
  edited in place as a tracker, not just read once.

---

## Phase A — Foundation: a collision-query seam, then move the zero-coupling utilities

**A technical correction to [PLATFORMER_ENGINE_ARCHITECTURE.md §7](PLATFORMER_ENGINE_ARCHITECTURE.md#7-migration-plan)'s
own description of this phase, found while planning the actual steps:** that document
says `TileMovement` moves to `platformer.core` "verbatim." It can't, quite — read
literally: `TileMovement.moveX(Actor actor, float dx, MarioWorld world)` (and `moveY`)
takes `MarioWorld` as a concrete parameter type, and calls `world.containsImpassableArea(...)`
on it. Moving the file to a Mario-agnostic package while it still names `MarioWorld` in
its own signature would just recreate the same coupling one directory over. The fix is
small — a one-method-shape interface `MarioWorld` already satisfies — but it means Phase A
has one more step than the architecture doc's summary implies. `OscillatorClock` and
`CameraController` genuinely have zero Mario-type references (confirmed by re-reading both
in full) and move exactly as described.

### A.1 — Create the package

- [x] Create `app/src/main/java/au/com/guidebee/morsetoolkit/platformer/core/`.

### A.2 — Define `TileCollisionSource`

- [x] New file `platformer/core/TileCollisionSource.java`:

  ```java
  package au.com.guidebee.morsetoolkit.platformer.core;

  /** Anything TileMovement can query for tile-grid + interactive-actor solidity —
    * exactly MarioWorld's own containsImpassableArea shape, extracted so
    * TileMovement doesn't need to name a Mario-specific type. */
  public interface TileCollisionSource {
      boolean containsImpassableArea(float x, float y, int width, int height);
      boolean containsImpassableArea(float x, float y, int width, int height, float duckAboveY);
  }
  ```

### A.3 — Retrofit `MarioWorld`

- [x] `world/MarioWorld.java`: add `implements TileCollisionSource` to the class
  declaration. No method bodies change — `containsImpassableArea`'s two existing
  overloads already match this interface exactly (confirmed: `MarioWorld.java`'s own
  two-overload shape is the interface's shape, verbatim).

### A.4 — Move `OscillatorClock`

- [x] Move `world/OscillatorClock.java` → `platformer/core/OscillatorClock.java`. Change
  only the `package` line. Zero other changes — it has no imports beyond its own package
  declaration today.
- [x] Update the import in every file that references it:
  `actors/enemies/FlyingTurtlePatrol.java`, `actors/enemies/OrbitingFireball.java`,
  `screen/MarioGameScreen.java` (calls `OscillatorClock.reset()`/`.advance(delta)`).

### A.5 — Move `CameraController` → `CameraFollow`

- [x] Move `world/CameraController.java` → `platformer/core/CameraFollow.java`, renaming
  the class itself (`public class CameraFollow`). No field/method changes — every member
  is already primitive-typed.
- [x] Update the import + type reference in `screen/MarioGameScreen.java` **and**
  `world/SpawnController.java` — the actual audit for this implementation found
  `SpawnController.update`/`updateBombs` also take a `CameraController` parameter (same
  package, so no import statement existed to grep for), which this plan's own audit
  missed. `hud/ScoreHud.java`'s doc-comment mention was left as historical prose,
  unchanged.

### A.6 — Move `TileMovement`, retyped to the interface

- [x] Move `world/TileMovement.java` → `platformer/core/TileMovement.java`. Change the
  `package` line, and change both `moveX`/`moveY`'s `MarioWorld world` parameter to
  `TileCollisionSource world`. No other changes — both methods only ever call
  `world.containsImpassableArea(...)`, never a `MarioWorld`-specific member, confirmed by
  re-reading the file in full.
- [x] Update the import in every caller. The actual grep run during implementation found
  12 direct callers, six more than this plan's own list:
  `actors/enemies/Enemy.java`, `actors/enemies/Boss.java`, `actors/items/Life.java`,
  `actors/items/Mushroom.java`, `actors/items/Star.java`,
  `actors/projectiles/FireBall.java` (as this plan said), plus
  `actors/enemies/EnemyTurtlePatrol.java`, `actors/enemies/FlyingTurtle.java`,
  `actors/enemies/HelmetShell.java`, `actors/enemies/Monkey.java`,
  `actors/enemies/SpikeyEgg.java`, `actors/enemies/TurtleShell.java` (missed by this
  plan's earlier audit).

### A.7 — Delete-and-verify

- [x] Confirm `world/TileMovement.java`, `world/OscillatorClock.java`,
  `world/CameraController.java` no longer exist under `mario/world/`.
- [x] Grep the whole `activity/mario` tree for `mario.world.TileMovement`,
  `mario.world.OscillatorClock`, `mario.world.CameraController` — zero hits (confirmed).

**Exit criteria:** full regression pass, identical to before the move — this phase changed
zero behavior, only package locations and one new interface with no new logic.
**Compiled successfully** via `gradlew :app:compileDebugJavaWithJavac` (the environment's
JDK 21 install and Android SDK are in fact present; the earlier "no compilation available"
note only applied to whatever machine originally wrote this plan) — a stronger check than
the manual read-through this plan otherwise budgets for. Still run the full
[MARIO_LEVEL_ATLAS.md](MARIO_LEVEL_ATLAS.md) on-device regression pass before starting
Phase B1.

---

## Phase B1 — `TileMetrics`: tile-size independence

This is the phase that actually answers "is the platformer engine tile-size independent" —
narrower than the full toolkit extraction, and sequenced first (before Phase B2) so B2's
own constructor rewrites take `tileSize` as a parameter from the start rather than needing
a second pass.

### B1.1 — Create `TileMetrics`

- [x] New file `platformer/core/TileMetrics.java`:

  ```java
  package au.com.guidebee.morsetoolkit.platformer.core;

  /** Immutable per-game world-grid metrics. Square tiles only, per this doc's scope. */
  public final class TileMetrics {
      public final int tileSize;
      public TileMetrics(int tileSize) {
          this.tileSize = tileSize;
      }
  }
  ```

### B1.2 — Thread it through `TileCollisionSource` and `MarioWorld`

- [x] Add `int tileSize();` to `TileCollisionSource` (§A.2) — every implementor must now
  expose its own grid unit, which is exactly what lets `TileMovement` (and everything
  else touched below) stop importing a static constant.
- [x] `MarioWorld`: replace the constructor's `super(cols, rows, tilesRegion,
  MarioConfiguration.TILE_SIZE, MarioConfiguration.TILE_SIZE)` with a `TileMetrics`
  parameter: `public MarioWorld(int cols, int rows, TextureRegion tilesRegion, TileMetrics
  metrics)`, storing `metrics.tileSize` in a new `private final int tileSize` field, and
  still passing `tileSize, tileSize` to `super(...)` (square tiles, unchanged). Add
  `public int tileSize() { return tileSize; }` to satisfy the interface.
- [x] `MarioWorld.getWidthPx()`/`getHeightPx()`: change
  `getColumns() * MarioConfiguration.TILE_SIZE` → `getColumns() * tileSize` (and rows
  likewise) — the instance field, not the static import.
- [x] `MarioWorld.containsImpassableArea(..., duckAboveY)` (the real implementation, not
  the 4-arg overload that delegates to it): change its own local
  `int tileSize = MarioConfiguration.TILE_SIZE;` to just read the instance field (drop the
  local variable, or rename it to avoid shadowing — either is fine, just don't leave two
  things named `tileSize` meaning different scopes in the same method).
- [x] Every construction site of `MarioWorld` (there is exactly one —
  `level/LevelLoader.java`'s `createWorld`) now passes a `TileMetrics`:
  `new MarioWorld(cols, rows, staticTilesRegion(level), new TileMetrics(MarioConfiguration.TILE_SIZE))`.
  `MarioConfiguration.TILE_SIZE` still exists and still equals `32` — this step doesn't
  remove the constant, it stops every *other* class from importing it directly, funneling
  everything through this one construction site instead.

### B1.3 — Fix `TileMovement` itself

- [x] `TileMovement.moveX`/`moveY`: change each method's own
  `int tileSize = MarioConfiguration.TILE_SIZE;` to `int tileSize = world.tileSize();` —
  now reading it off the `TileCollisionSource` parameter already in scope, not a static
  import. (`TileCollisionSource` needs `tileSize()` per B1.2 — done above.)

### B1.4 — Fix the 26-file / 71-reference `MarioConfiguration.TILE_SIZE` category

Every file below currently imports the static constant directly. Each is a mechanical
"where does this method/constructor get its `tileSize` from now" question — most already
declare `int tileSize = MarioConfiguration.TILE_SIZE;` as a local variable, so the fix is
usually "change the right-hand side of that one line," not a structural rewrite. Grouped
by what the right answer is:

**Already-local-variable pattern — change the RHS to an instance/parameter read:**

- [x] `level/LevelLoader.java` (11 occurrences, lines 164/296/374/389/434/484/512/578/607/625/645
  — one `int tileSize = MarioConfiguration.TILE_SIZE;` near the top of each `spawn*`
  method) — change each to `int tileSize = MarioContext.world().tileSize();` (the world is
  always initialized by the time any `spawn*` method runs — confirmed by
  `LevelLoader`'s own class doc: `spawnBricks`/`spawnEnemies`/etc. all require
  `MarioContext.init(...)` to have already run).
- [x] `world/TileMovement.java` — done in B1.3 above.
- [x] `actors/player/Player.java` lines 640/657 (`moveXWithCollision`/`moveYWithCollision`'s
  own local `int tileSize = MarioConfiguration.TILE_SIZE;`) — change to
  `world.tileSize()` (`Player` already holds a `private final MarioWorld world` field).
- [x] `actors/bricks/TemporaryInvisibleBrick.java` line 44 — same pattern; this class is
  constructed by `Brick#hitFromBelow` (per
  [MARIO_GAME_MECHANICS.md §9.2](MARIO_GAME_MECHANICS.md#92-bricks-extends-interactivebrick)),
  so thread `tileSize` in via its constructor from whichever caller already has a
  `MarioWorld` reference (`Brick` does, via `MarioContext.world()`).
- [x] `actors/scenery/FlagPole.java` lines 56/67 (two separate methods, each with their own
  local `int tileSize = MarioConfiguration.TILE_SIZE;`) — both need `tileSize` threaded
  in via the constructor (see B1.6 below — `FlagPole`'s constructor also has raw-literal
  work to do, do both in the same pass).
- [x] `fx/Fireworks.java` line 51, `fx/BridgeBlackout.java` line 36,
  `fx/BossFallingAnim.java` line 82 — same local-variable pattern; each is an `fx/` class
  constructed from a `MarioWorld`-aware caller (`MarioGameScreen` or another actor already
  holding a world reference) — thread `tileSize` through their constructors the same way.
- [x] `world/SpawnController.java` — lines 48/51/52 are `static final float` constants
  computed *once* from `MarioConfiguration.TILE_SIZE` at class-load time
  (`FLYING_FISH_MIN_X`, `FLYING_FISH_OFFSET_MIN_PX`, `FLYING_FISH_OFFSET_MAX_PX`) — these
  **cannot** stay `static final` if they're to depend on an instance `tileSize`; convert
  to non-static `private final float` fields computed in the constructor, which already
  takes a `LevelDefinition level` — add a `TileMetrics metrics` (or plain `int tileSize`)
  parameter alongside it, sourced from `MarioGameScreen` (the only caller). Lines 86/93
  (`updateBombs`'s own two remaining direct `MarioConfiguration.TILE_SIZE` reads) fold
  into the same constructor-parameter fix.
- [x] `debug/DebugPanel.java` — 6 occurrences (lines 148/212/213/228/256/257), all in
  debug-only tile↔pixel conversions for the warp panel — thread `tileSize` from the
  `MarioWorld`/`LevelDefinition` this panel already receives in its own constructor (per
  [MARIO_GAME_MECHANICS.md §12](MARIO_GAME_MECHANICS.md#12-debugqa-tooling)).
- [x] `fx/ItemReveal.java` line 40 (`targetY = y - MarioConfiguration.TILE_SIZE`) — thread
  `tileSize` via the constructor; caller is `QuestionMark`/`BankWithItem` (see B1.5 —
  same classes need the parameter for their own reasons, add it once).
- [x] `screen/MarioGameScreen.java` lines 320/380/381 — this class already constructs
  `MarioWorld` and `Player` directly; change `level.levelLength / MarioConfiguration.TILE_SIZE`
  and the `startTileX/Y * MarioConfiguration.TILE_SIZE` player-spawn math to read
  `world.tileSize()` off the `MarioWorld` this same method just built.

**Region-splitting pattern — these pass `TILE_SIZE` as a literal frame-slice size, not a
world-position multiplier — fold into B1.5's constructor-parameter fix since they're the
same classes:** `actors/bricks/BankWithItem.java` (lines 52/74), `actors/bricks/Tree.java`
(38), `actors/bricks/QuestionMark.java` (39/76), `actors/bricks/Iron.java` (47),
`actors/bricks/InvisibleBrck.java` (41/54), `actors/bricks/BrickWithStar.java` (33).

**Static-field-computed-from-the-constant pattern — same "can't stay static" issue as
`SpawnController` above:** `actors/lifts/Lift.java` (lines 50/52/54/57 —
`VERTICAL_OSCILLATION_AMPLITUDE`/`HORIZONTAL_OSCILLATION_AMPLITUDE`/`TRAVEL_RANGE`/
`LANDING_TOLERANCE`), `actors/lifts/LiftCar.java` (42/92), `actors/lifts/BalanceLiftPlatform.java`
(38/39/57/140), `actors/lifts/LiftFall.java` (27/75) — every one of these becomes a
non-static `private final float` computed in the constructor from a new `tileSize`
parameter (all 4 lift classes are constructed from `LevelLoader.spawnLifts`, which already
has `tileSize` in scope per B1.4's `LevelLoader` fix above — just pass it one level
further into each `new Lift(...)`/`new LiftCar(...)`/etc. call).

### B1.5 — Fix the 21 `FRAME_WIDTH`/`FRAME_HEIGHT`/`FRAME_SIZE = 32` classes

Every class below hardcodes its own frame/hitbox size as a literal `32` (or `32`/`48` for
1×1.5-tile enemies), fed into `Sprite(region, frameWidth, frameHeight)` — which (confirmed
in [PLATFORMER_ENGINE_ARCHITECTURE.md §3.8](PLATFORMER_ENGINE_ARCHITECTURE.md#38-tilemetrics-making-tile-size-a-first-class-non-global-value)
by reading `Sprite`'s own constructor) sets the actor's *collision hitbox* at the same
time as its atlas-slice size. The fix for each: replace the `private static final int
FRAME_WIDTH/HEIGHT/SIZE = 32` with an **instance** field computed from a new constructor
parameter, and replace every bare `48` in the same class with `tileSize * 3 / 2` (integer
division is safe here since `tileSize` will always be a multiple of 2 in practice, but use
`(tileSize * 3) / 2` explicitly to make the intent clear rather than relying on operator
precedence).

- [x] `actors/player/Player.java` — `TRANSITION_FRAME_WIDTH`/`HEIGHT` (32/64): thread
  `tileSize` through (Player already takes a `MarioWorld world` constructor parameter —
  read `world.tileSize()` once in the constructor and store it).
- [x] `actors/enemies/Helmet.java`, `FishyGround.java`, `Rocket.java`, `Spikey.java`,
  `SpikeyEgg.java` — `FRAME_SIZE = 32` (1×1-tile enemies) — add a `tileSize` constructor
  parameter, replace `FRAME_SIZE` with it.
- [x] `actors/enemies/EnemyTurtle.java`, `FlyingTurtle.java`, `SonOfABuitch.java`,
  `Monkey.java`, `FlyingTurtlePatrol.java`, `OctoPussy.java`, `PiranhaPlant.java`,
  `EnemyTurtlePatrol.java` — `FRAME_WIDTH = 32`/`FRAME_HEIGHT = 48` (1×1.5-tile enemies) —
  same fix, `FRAME_HEIGHT` becomes `(tileSize * 3) / 2`.
- [x] `actors/bricks/Axe.java` — `FRAME_WIDTH`/`HEIGHT = 32`/`32`.
- [x] `actors/scenery/Spring.java` — `FRAME_WIDTH = 32`/`FRAME_HEIGHT = 64` (2 tiles tall
  — becomes `tileSize * 2`).
- [x] `fx/Fireworks.java`, `fx/Explosion.java`, `fx/LavaBall.java` — `FRAME_SIZE = 32`.
- [x] `actors/items/Coin.java` — `FRAME_SIZE = 32`.
- [x] `actors/enemies/FishyWater.java` — `FRAME_SIZE = 32` (declared but verify at
  implementation time whether it's actually read anywhere beyond the field itself — this
  class's constructor signature wasn't fully re-confirmed during this audit pass, check
  it directly before assuming the same fix pattern applies verbatim).

For every one of the above, the constructor call site (almost always in
`level/LevelLoader.java`'s `spawnEnemies`/`spawnBricks`/etc., which already has `tileSize`
in scope after B1.4) needs one more argument added.

### B1.6 — Fix the raw-arithmetic spots with no constant reference at all

These don't import anything today — they just have `32` (or a tile-multiple like `48`,
`64`, `96`) typed directly into an expression. Each needs `tileSize` threaded through its
constructor the same way as B1.5, then the literal replaced:

- [x] `actors/enemies/Boss.java` line 134: `((int) getY() / 32) * 32` →
  `((int) getY() / tileSize) * tileSize`. `Boss` needs a new constructor parameter (its
  constructor is called from `LevelLoader.spawnEnemies`, which has `tileSize` in scope).
- [x] `actors/projectiles/BossFire.java` line 51: `targetY = (6 + RANDOM.nextInt(4)) * 32;`
  → `* tileSize`. `BossFire` is constructed both by `LevelLoader.spawnHazards` (placed
  instances) and by `Boss` itself (thrown instances) — both call sites need the extra
  argument; simplest is for `Boss` to hold its own `tileSize` (from B1.6's own fix above)
  and pass it along when it constructs a `BossFire`.
- [x] `actors/enemies/EnemyTurtlePatrol.java` line 40: `rightBoundX = x + 32 *
  patrolLengthTiles;` → `x + tileSize * patrolLengthTiles`.
- [x] `actors/enemies/FlyingTurtlePatrol.java` lines 35/46: `AMPLITUDE_PX = 4 * 32f`
  (→ non-static, `4 * tileSize`) and `centerY = y + 32 * patrolLengthTiles` (→
  `tileSize * patrolLengthTiles`).
- [x] `actors/scenery/FlagWinBanner.java` lines 21/22/30: `START_OFFSET_Y = -96f` (3
  tiles, → `-3f * tileSize`), `RISE_DISTANCE = 64f` (2 tiles, → `2f * tileSize`), and the
  constructor's own hardcoded `super(checkpointX - 24f, ..., 32, 32, true)` — the `-24f`
  is itself a tile-relative offset (0.75 tile) that needs `-0.75f * tileSize`, and the
  trailing `32, 32` (this actor's own hitbox size) needs `tileSize, tileSize`.
- [x] `actors/scenery/FlagPole.java` line 53's own `- 16f` (half-tile centering offset,
  alongside the `MarioConfiguration.TILE_SIZE` reference on the same lines already being
  fixed in B1.4) → `- tileSize / 2f`.
- [x] `actors/player/Player.java` lines 192/194: `DUCK_HEAD_ROOM_PX = 32f` →
  non-static, `tileSize`; `DUCK_OVERHEAD_CLEARANCE_PX = 48f` → `(tileSize * 3) / 2f`
  (this one is `public static final` today — check every external reader, currently just
  `EnemyCollisionResolver`, and change it to read `player.getDuckOverheadClearancePx()`
  or similar instance accessor instead once it's no longer static).
- [x] `actors/enemies/PiranhaPlant.java` — `TRAVEL_PX = 96f` (3 tiles) → non-static,
  `3f * tileSize`; also re-check this class's other offset literals (`+48` mentioned in
  its own doc comment per [MARIO_GAME_MECHANICS.md §8](MARIO_GAME_MECHANICS.md#8-tile-type-dispatch-registry))
  while already inside the file for this fix.

**This list is the product of one audit pass, not a formal exhaustive guarantee — before
closing this phase, re-run the searches in
[PLATFORMER_ENGINE_ARCHITECTURE.md §3.8](PLATFORMER_ENGINE_ARCHITECTURE.md#38-tilemetrics-making-tile-size-a-first-class-non-global-value)
(`grep -rn '\b32\b'` etc. across `activity/mario`, filtered for anything not already
using the new `tileSize` parameter) to catch anything this pass missed** — a handful of
enemy classes' own internal movement-offset constants weren't individually re-verified
line-by-line here (the audit prioritized breadth — finding every *class* with the pattern
— over confirming every single literal inside each one).

**Gaps this plan's own audit missed, found and fixed during implementation** (the re-grep
above, done for real): `actors/enemies/HelmetShell.java` and `TurtleShell.java` (bare
inline `32, 32` in `super(...)`/`.split(...)`, not a named `FRAME_*` constant, so missed by
a search for `static final`) — both are constructed by classes already in the B1.5 list
(`Helmet`/`EnemyTurtle`), so both now take a `tileSize` constructor parameter too, threaded
from their spawning parent. `actors/enemies/Boss.java`'s own `FRAME_WIDTH`/`HEIGHT = 64`
and `PATROL_RANGE_PX = 3 * 32f` (same pattern as the named B1.5/B1.4 classes, just not
listed — `Boss` was already getting a `tileSize` parameter for B1.6's line-134 fix, so this
folded in for free). `actors/enemies/Rocket.java`'s own two constructors and
`RocketLauncher.java` (the turret that fires it, holding its own `tileSize` field so it can
still fire correctly during `act()`, long after construction). `actors/enemies/OctoPussy.java`'s
`RISE_TRIGGER_Y`/`DART_OFFSET_PX`/`RISE_OFFSET_PX`/`WAIT_RISE_OFFSET_PX` and
`PiranhaPlant.java`'s `RETRACTED_ZONE_PX` (both named in this doc only for their headline
`FRAME_WIDTH/HEIGHT`/`TRAVEL_PX` fields — their other same-class tile-relative constants
weren't). `actors/enemies/FishyWater.java`'s `BOB_RANGE_PX` and `Monkey.java`'s
`PATROL_RANGE_PX` (both exactly one tile, folded into their constructors' own math once the
`tileSize` parameter existed for the frame-size fix anyway). `actors/items/Life.java`,
`Mushroom.java`, `Flower.java`, `Star.java` (bare inline `32, 32`, the same pattern as
`HelmetShell`/`TurtleShell` — these are what `BankWithItem`/`QuestionMark`/`InvisibleBrck`/
`BrickWithStar`'s item-reveal callbacks construct, so needed the parameter regardless).
`actors/enemies/EnemyMashroom.java` and `fx/CoinPopEffect.java` (bare inline `32, 32`,
found only by re-running B1.7's own grep after everything else was done).
`world/SpawnController.java`'s `Rocket`/`FishyGround` construction calls needed the same
`tileSize` this class's own constants fix already required. `level/LevelLoader.java`'s own
"pump" case (`tile.x * tileSize + 16, tile.y * tileSize + 48` feeding `PiranhaPlant`) and
its "HoriImage" case (`.split(64, 64)`, `x + 64`) were raw arithmetic in `LevelLoader`
itself, not named in B1.4–B1.6's file lists — fixed since `tileSize` was already a local in
both methods, at zero extra threading cost.

**Found and deliberately left out of scope** (not named anywhere in this plan, and fixing
them needs a judgment call about intent this pass didn't make): `collision/CheckpointResolver.java`'s
`TRIGGER_WIDTH`/`HEIGHT`/`CLOWD_TRIGGER_WIDTH` and `collision/TeleportResolver.java`'s
`TRIGGER_X_OFFSET`/`WIDTH`/`HEIGHT` (plausibly tile-multiples, but touch-trigger geometry
that was never audited here); `screen/MarioGameScreen.java`'s `DEBUG_BUTTON_SIZE = 32` (a
UI button's on-screen pixel size, unrelated to the level's tile grid — coincidentally equal
to 32, not a tile count); `actors/player/PlayerPowerState.java`'s `SMALL(32, 32, ...)`/
`BIG(32, 64, ...)`/`FIRE(32, 64, ...)` enum constants (Player's own hitbox/frame sizes —
enum constants are built at class-load time, before any `MarioWorld`/`TileMetrics` exists,
so making these tile-size-aware needs an actual redesign, not a constructor parameter; likely
belongs with Phase E's `PowerStateActor` extraction instead, since that phase already
restructures `Player`'s own state shape). None of these affect this phase's own exit
criterion (`MarioConfiguration.TILE_SIZE`'s *other* users are what mattered) but are noted
here so a future pass doesn't have to rediscover them.

### B1.7 — Verify nothing still imports the static constant except its one legitimate use

- [x] Grep `activity/mario` for `MarioConfiguration.TILE_SIZE` — the only remaining hit
  should be `LevelLoader.createWorld`'s `new TileMetrics(MarioConfiguration.TILE_SIZE)`
  call (B1.2) and `MarioConfiguration.java`'s own declaration. Anything else means a call
  site from B1.4–B1.6 was missed. **Confirmed clean** (only that one hit remains).
- [x] Grep for `= 32;`/`= 48;`/`= 64;`/`= 96;` as a `static final` field initializer
  across the same tree — any survivor is a candidate this pass missed. **Ran, found the
  gaps documented above, fixed the in-scope ones, documented the deferred ones.**

**Exit criteria:** full regression pass, zero gameplay/visual change — identical bar to
Phase A. This is the phase [MARIO_RESKIN_EXECUTION.md §R.0](MARIO_RESKIN_EXECUTION.md)
waits on. **Compiled successfully** via `gradlew :app:compileDebugJavaWithJavac` after every
edit in this phase — every constructor-signature change and call-site update above was
verified against the real compiler, not just by re-reading call sites by hand. Still run the
on-device `MARIO_LEVEL_ATLAS.md` regression pass before starting Phase B2, per this plan's
own ground rules — a clean compile proves every call site was updated, not that the
gameplay feel is unchanged.

---

## Phase B2 — `TileWorld` base class + `TileTypeRegistry`

Independent of Phase B1's tile-size work in principle, but sequenced right after it so
this phase's own constructor rewrites (every `LevelLoader` case becoming a registry
handler) take `tileSize` as a parameter from the start.

### B2.1 — Create the generic `TileWorld`

- [x] New file `platformer/core/TileWorld.java`, per
  [PLATFORMER_ENGINE_ARCHITECTURE.md §3.1](PLATFORMER_ENGINE_ARCHITECTURE.md#31-tileworld-from-7-hardcoded-lists-to-a-typed-registry):
  `extends TiledLayer implements TileCollisionSource`, constructor takes
  `(int cols, int rows, TextureRegion tilesRegion, TileMetrics metrics)`, holds the
  `Map<Class<?>, List<Object>>` registry, `register`/`listFor`, and `tileSize()`. Move
  `containsImpassableArea`'s actual implementation here from `MarioWorld` — it only ever
  needed the tile grid (`TiledLayer`'s own `getCell`) plus *one* actor list (bricks); with
  the generic registry, that becomes `listFor(InteractiveBrick.class)` instead of a named
  field.
- [x] `findActiveBrickAt` moves here too — same reasoning, it's already generic over
  "the brick list," just needs to read it via `listFor(...)`.

  **Technical correction found while implementing:** `containsImpassableArea`/
  `findActiveBrickAt` can't literally query `listFor(InteractiveBrick.class)` from
  `platformer.core.TileWorld` — `InteractiveBrick` is a Mario-specific type in
  `activity.mario.actors.bricks`, and `TileWorld` (toolkit layer) can't import it without
  recreating exactly the coupling this phase exists to remove. Added a new marker
  interface `platformer/core/SolidTile.java` (`isActive()`/`overlaps(...)`/`getY()`/
  `getHeight()` — precisely `InteractiveBrick`'s existing shape, confirmed by reading
  `Sprite`/`Actor`'s own `getY()`/`getHeight()` return types before committing to `float`).
  `TileWorld` queries `listFor(SolidTile.class)` generically; `InteractiveBrick` now
  `implements SolidTile`; `MarioWorld.addBrick` registers each brick under *both*
  `InteractiveBrick.class` (for `getBricks()`) and `SolidTile.class` (for the solidity
  query) — two list entries pointing at the same object, not a real duplication. Same
  category of fix as Phase A's own `TileCollisionSource` correction.

### B2.2 — `MarioWorld extends TileWorld`

- [x] `MarioWorld` becomes a thin subclass: constructor just calls `super(...)`. Replace
  the 7 hardcoded `List<X>` fields with calls to `listFor(X.class)`, keeping the existing
  named methods (`addBrick`/`getBricks`/`addEnemy`/`getEnemies`/etc.) as one-line wrappers
  around `register(X.class, x)`/`listFor(X.class)` — this keeps every existing call site
  in the rest of `activity/mario` (there are many) working unchanged; only `MarioWorld`'s
  own internals change.
- [x] Verify every `world.getBricks()`/`getEnemies()`/`getCollectibles()`/`getFireBalls()`/
  `getLifts()`/`getHazards()`/`getAxes()` call site elsewhere in the codebase still
  compiles against the unchanged method signatures (they should — this step is designed
  to be invisible to callers). **Confirmed via a full `gradlew :app:compileDebugJavaWithJavac`**
  — every one of those still resolves; `getBricks()` itself turned out to have zero
  external callers (grep-confirmed), so its own exact return type was never actually at risk.

### B2.3 — `TileTypeRegistry` + `TileHandler`

- [x] New file `platformer/level/TileTypeRegistry.java` +
  `platformer/level/TileHandler.java`, per
  [PLATFORMER_ENGINE_ARCHITECTURE.md §3.3](PLATFORMER_ENGINE_ARCHITECTURE.md#33-tiletyperegistry-the-single-highest-leverage-change).

  **Technical correction found while implementing:** the architecture doc's own sketch
  types `TileHandler.spawn`'s third parameter as `GameContext<?, ?, ?> ctx` — but
  `GameContext` is Phase C's own deliverable, sequenced *after* B2, so it doesn't exist
  yet. Used a plain `int tileSize` third parameter instead (the only thing any handler
  body actually reads off "ctx" per this doc's own B2.4 description); Phase C can widen
  the signature later if a real `GameContext` ever needs to flow through it, but nothing
  in this phase's own scope needs more than the tile size.

  **Known, documented scope compromise:** `TileHandler`/`TileTypeRegistry` live in
  `platformer.level` (so a future second game could reuse the dispatch *mechanism*), but
  both still import Mario's own `LevelDefinition` type directly (it hasn't been
  generalized — that's real work this plan never scopes; see "What's deliberately not in
  this plan"/Phase H). The toolkit-vs-content boundary here is intentionally soft until a
  second consumer actually needs `LevelDefinition` generalized too. Documented in
  `TileHandler`'s own class doc, not just here.

### B2.4 — `MarioTileRegistry`

- [x] New file `level/MarioTileRegistry.java`, reproducing every `case` from
  `LevelLoader.spawnBricks`/`spawnEnemies`/`spawnHazards`/`spawnItems`/`spawnLifts`/
  `spawnScenery` (the full list is in
  [MARIO_GAME_MECHANICS.md §8](MARIO_GAME_MECHANICS.md#8-tile-type-dispatch-registry)) as
  a `.register("TypeString", (tile, level, tileSize) -> ...)` call, one-for-one. Each
  handler body is the existing case's body, unchanged, reading `tileSize` from the new
  parameter instead of a local variable. Verified by an automated set-diff of every
  `case`/`"...".equals(tile.type)` string in the old `LevelLoader` against every
  `.register("...")` string in the new file: all 56 tile types match exactly, none missing,
  none extra.
- [x] Did this incrementally in practice by grouping into six private
  `registerBricks`/`registerEnemies`/`registerHazards`/`registerItems`/`registerLifts`/
  `registerScenery` methods (bricks, then enemies, then hazards, then items, then lifts,
  then scenery) — the "six small, independently verifiable slices" this doc asked for,
  though "verifiable" here means careful reading + the set-diff above + a real compile,
  not an on-device playthrough (see this phase's own exit criteria below).

  **One real design problem found, not just a mechanical one — this doc's own single
  combined-registry model is not behavior-preserving:** `MarioGameScreen` calls
  `LevelLoader.spawnScenery`/`spawnBricks`/`spawnEnemies`/`spawnLifts`/`spawnHazards`/
  `spawnItems` as **six separate passes** over the full tile list, in that fixed order —
  every scenery actor gets appended to `layerManager` before any brick actor, which gets
  appended before any enemy actor, etc. (see that call site's own "Scenery goes first so
  bricks/enemies/the player draw in front of it" comment, and `LayerManager.append`'s own
  doc on append order affecting draw order). A single combined `TileTypeRegistry` with one
  `spawnAll` pass — literally what §3.3's own sketch shows — would instead append actors
  in whatever order their tiles happen to appear in a level's own JSON, interleaving
  categories by file position instead of grouping by category, silently changing on-screen
  z-order for any level whose tile list isn't already grouped that way. **Fixed by building
  six separate `TileTypeRegistry` instances** (`MarioTileRegistry.buildBricks()`/
  `buildEnemies()`/`buildHazards()`/`buildItems()`/`buildLifts()`/`buildScenery()`), one per
  existing category, each still eliminating its own switch statement but preserving the
  exact six-pass append order. Documented prominently in `MarioTileRegistry`'s own class
  doc, since it's the one place this doc's illustrative single-registry sketch doesn't hold.

  **Two cases needed real restructuring, not just transcription** (also documented in
  `MarioTileRegistry`'s own class doc): (1) `"pump"`/`"PumpWarp"` — the old switch's
  `"pump"` case fell through into `"PumpWarp"`'s shared body; a table can't fall through,
  so the shared body became a private `spawnPumpBody` helper both registrations call. (2)
  `"SmallCastle"`/`"BigCastle"` — previously the `default` branch of `spawnScenery`'s own
  if-chain; registered directly under both exact type strings now, both calling the same
  `LevelLoader.sceneryRegion` helper so the CloudsNight/`bw_*` logic isn't duplicated.

  **One genuine behavior-shape change, not a mechanical port:** the old `spawnScenery`
  returned the level's `FlagPole` (or null) directly — the one `spawn*` method with
  anything to report back to `MarioGameScreen`. A `void spawnAll` has no per-category
  return value, so the `"Flag"` handler now calls `MarioContext.world().setFlagPole(...)`
  (new field + getter/setter on `MarioWorld`, same "zero or one" pattern a `List`-based
  registry entry doesn't fit), and `MarioGameScreen` reads it back via
  `world.getFlagPole()` after `spawnScenery` returns, instead of from a return value.

### B2.5 — Generalize `LevelLoader`

- [x] `LevelLoader`'s six `spawn*` methods collapse to one line each — `REGISTRY.spawnAll(level,
  MarioContext.world().tileSize())` against their own dedicated `TileTypeRegistry` (see
  B2.4's own six-registries note for why it's six, not one) — plus the still-bespoke
  static-terrain population (`createWorld`/`populateStaticGeometry`/`staticTileIndex`),
  which stays exactly as-is, unchanged, since it was never part of the switch-statement
  problem. The six registries are built once, eagerly, as `private static final
  TileTypeRegistry` fields (registry construction is cheap - a handful of hashmap
  insertions - so there's no lazy-init complexity to bother with).

### B2.6 — Cutover

- [x] Deleted the old switch-statement bodies from `LevelLoader` — done in the same pass
  as B2.5 (a half-cutover state, `MarioTileRegistry` built but not yet wired in, isn't a
  meaningfully safer intermediate step, and the six-way set-diff plus a real compiler
  already catch every call-site/typo-class of error this cutover could introduce).
  Also widened several of `LevelLoader`'s own helper methods/constants
  (`add`/`addEnemy`/`addLift`/`addBalanceLiftPlatform`/`forEachCell`/`CellSpawner`/
  `spawnTree`/`spawnRocketLauncher`/`spawnWall`/`spawnFireBar`/`spawnBalanceLift`/
  `helmetColor`/`sceneryRegion`/`liftMotion`/`FIRE_BAR_COUNT`/`BIG_FIRE_BAR_COUNT`/
  `WHITE_LINE_HEIGHT_TILES`) from `private` to package-private, since `MarioTileRegistry`
  (same package) now calls them directly to keep every handler body byte-for-byte
  identical to its old case body rather than re-deriving the same logic twice.

  **What this cutover could not verify, and still needs the on-device pass below:**
  everything above is checked by careful reading, an automated tile-type set-diff, and a
  real compile (which catches every signature/call-site mismatch) — none of that proves
  gameplay/visual output is unchanged. In particular: the six-registry split is a reasoned
  argument for why append order is preserved, not an on-device confirmation of it; the
  `FlagPole` return-value-to-field change is a mechanical translation but wasn't watched
  render on a device; and the `"pump"`/`"PumpWarp"` fall-through restructuring, while
  traced through by hand, is exactly the kind of subtle control-flow change worth
  double-checking on a level that actually has both tile types.

**Exit criteria:** a full 8-world playthrough confirms every level spawns exactly the
actors it did before this phase — same counts, same positions, same types, **same z-order**
(the risk this phase's own audit found and reasoned through, but couldn't itself watch
render), checked against [MARIO_LEVEL_ATLAS.md](MARIO_LEVEL_ATLAS.md)'s per-level
enemy/brick tables as the reference. **Compiled successfully** via
`gradlew :app:compileDebugJavaWithJavac` (`--rerun-tasks`, a full clean rebuild, not just
an incremental one) after every edit in this phase.

---

## Phase C — `GameContext`

- [x] New file `platformer/core/GameContext.java`, per
  [PLATFORMER_ENGINE_ARCHITECTURE.md §3.5](PLATFORMER_ENGINE_ARCHITECTURE.md#35-gamecontexttplayer-tworld-tgamestate).
  Bounded `TWorld extends TileWorld` per the architecture doc's own sketch — satisfied
  since `MarioWorld extends TileWorld` as of Phase B2.
- [x] `world/MarioContext.java` becomes a thin wrapper parameterized as
  `GameContext<Player, MarioWorld, GameStateController>` — every existing call site
  (`MarioContext.world()`, `.player()`, `.gameState()`, `.spawn(...)`, `.init(...)`) keeps
  its exact signature; only the class's own internals change.

  **Technical correction found while implementing — "subclass" (this doc's first-listed
  option) doesn't compile:** `GameContext`'s instance methods and `MarioContext`'s target
  static methods must have the *exact same names* (`init`/`world`/`player`/`setPlayer`/
  `gameState`/`spawn`, per this doc's own call-site list) for every existing call site to
  keep working unchanged. If `MarioContext extends GameContext<...>`, Java refuses to
  compile a static method that has the same signature as an inherited instance method
  ("static methods cannot hide instance methods") — so `public static MarioWorld world()`
  in a subclass of a `GameContext` that already declares instance `world()` is a hard
  compile error, not a style choice. Used this doc's second-listed option instead:
  `MarioContext` holds one `private static final GameContext<Player, MarioWorld,
  GameStateController>` field (composition, not inheritance) and every static method is a
  one-line forward to it. Documented in both classes' own doc comments, not just here.

**Exit criteria:** full regression pass — purely mechanical, low risk. **Compiled
successfully** via `gradlew :app:compileDebugJavaWithJavac --rerun-tasks` (a full clean
rebuild) — `MarioContext` is used pervasively across `activity/mario`, so this phase's own
"purely mechanical" claim is now compiler-verified across every call site, not just
reasoned about.

---

## Phase D — `CollisionPipeline`

- [x] New files `platformer/collision/FrameResolver.java` (interface) and
  `platformer/collision/CollisionPipeline.java`, per
  [PLATFORMER_ENGINE_ARCHITECTURE.md §3.4](PLATFORMER_ENGINE_ARCHITECTURE.md#34-collisionpipeline-formalizing-the-existing-convention).
- [x] `screen/MarioGameScreen.java`: build one `CollisionPipeline` in the constructor (right
  after `MarioContext.setPlayer(player)`, the earliest point every resolver's own captured
  field exists) from the existing 8 resolver calls — the exact list and order is documented
  in [MARIO_GAME_MECHANICS.md §3](MARIO_GAME_MECHANICS.md#3-the-frame-loop) — replace the 8
  call lines in `render(delta)` with one `pipeline.resolveAll(world)`.

  **Technical note found while implementing (not a correction, just a detail the
  architecture doc's own sketch leaves implicit):** the doc's illustrative
  `new CollisionPipeline(this::resolvePickups, EnemyCollisionResolver::resolve, ...)`
  reads as if bare method references satisfy `FrameResolver`, but they can't here — every
  one of Mario's 8 resolvers takes `(Player, MarioWorld)`, `(MarioWorld)` alone, or (for
  `TeleportResolver`) `(List<TeleportLink>, Player)`, never the interface's own single
  `TileWorld world` parameter. Used lambdas instead (`w -> EnemyCollisionResolver.resolve(player,
  world)`), each closing over the screen's own `player`/`world`/`level` fields and ignoring
  the lambda's own parameter entirely — `world` (the field, already a `MarioWorld`) is what
  every resolver actually wants, not `w` (the interface's generic `TileWorld` parameter),
  so there's nothing to gain from casting `w` instead of reading the closed-over field.
  `AxeResolver.findTriggered`/`CheckpointResolver.findTouched` (called after `draw()`, not
  part of this per-frame resolver list per §3's own notes) are untouched.

**Exit criteria:** full regression pass — purely mechanical, the resolver order is
unchanged, just relocated into one list instead of 8 lines. **Compiled successfully** via
`gradlew :app:compileDebugJavaWithJavac --rerun-tasks` (a full clean rebuild).

---

## Phase E — `PowerStateActor`

- [x] New file `platformer/actor/PowerStateActor.java`, per
  [PLATFORMER_ENGINE_ARCHITECTURE.md §3.2](PLATFORMER_ENGINE_ARCHITECTURE.md#32-powerstateactor-the-reusable-half-of-player).
  Extracted from `Player.java`: the invincibility/star/shield timers (`invincibleTimer`/
  `starTimer`/`shieldTimer`/`blinkVisible`/`debugInvincible`, `isInvincible`/`hasStar`/
  `setInvincibleFor`/`setDebugInvincible`/`isDebugInvincible`), the morph-transition
  flipbook machinery (`beginTransition`/`updateTransition`/`transitionFrames`/`isTransitioning`/
  `currentTransitionFrame`), and the checkpoint/respawn fields + `updateCheckpoint`. Left
  `applyMovement` (everything else a frame does) and `paintPowerState` (rendering)
  abstract, exactly as asked.

  **Three technical corrections found while implementing** — the architecture doc's own
  "all moved verbatim, since none of it reads a Mario-specific constant" claim doesn't
  quite hold for two of these once the real code is read closely, and the plan's own
  extraction list includes one item that turned out not to belong here at all:

  1. **`beginTransition` couldn't move verbatim** — the original private method took a
     `String regionName` and called `MarioResourceManager.region(regionName)` directly, a
     Mario-specific resource lookup the generic base can't perform. Split it: the base's
     `beginTransition(TextureRegion[] frames, S target, float width, float height, float
     yShift)` now takes pre-built frames (Player's own new `startTransition` private
     method does the `MarioResourceManager.region(...).split(...)` + facing-flip work
     first, then calls the base method) and a plain `yShift` pixel amount instead of a
     `preShiftUp32` boolean that read two concrete `PlayerPowerState` enum constants
     directly (`PlayerPowerState.BIG.height - PlayerPowerState.SMALL.height`) — literal
     enum values a `Enum<S>`-bounded generic base has no way to reference. Completing a
     transition needed the same treatment: `updateTransition` now calls an abstract
     `onTransitionComplete(S target)` hook instead of applying the new power state itself
     (which needs `initFrames`/`setSize`, both Mario-specific), and `Player`'s override
     just calls its own existing `changePowerState(target)`.
  2. **`updateCheckpoint`'s own tuning constants stayed in `Player`, threaded as
     parameters** (`updateCheckpoint(delta, eligible, saveIntervalSeconds, minDistancePx)`)
     instead of moving into the base as baked-in values — `CHECKPOINT_SAVE_INTERVAL_SECONDS`/
     `CHECKPOINT_MIN_DISTANCE_PX` are this game's own tuning, not a toolkit default, the
     same reasoning Phase B1 already applied to `TileMetrics`. Similarly, `updateCheckpoint`
     doesn't read `onGround`/`onLift` itself (both stay Mario-specific physics fields in
     `Player`) — it takes a plain `eligible` boolean the caller computes
     (`onGround && !onLift`).
  3. **`setForcedCommand`/`clearForcedCommand` did *not* move, despite being named in this
     plan's own extraction list** — moving them would need the forced-command field typed
     as something, and the only concrete type available is Mario's own `PlayerCommand`;
     giving `PowerStateActor` a second generic type parameter for it would contradict this
     phase's own next line, which explicitly types the result as
     `PowerStateActor<PlayerPowerState>` (one parameter). Once `applyMovement` was scoped
     as `applyMovement(float delta)` — no command parameter at all, since nothing in the
     shared base ever needs to interpret a "command" — there was nothing left for the base
     to gain from owning this field anyway: `Player` already polls its own `input`/
     `forcedCommand` entirely inside its own `applyMovement` override. Left in `Player`,
     documented in both classes why. (This mechanism is arguably Phase G's more natural
     home once a generic `PlatformerCommand` exists, not this phase's.)

- [x] `actors/player/Player.java` becomes `extends PowerStateActor<PlayerPowerState>`,
  implementing `applyMovement` with today's `applyHorizontalInput`/`applyJump`/
  `applyGravity`/`moveXWithCollision`/`moveYWithCollision` body, unchanged — plus
  everything else the old `act()` override did that isn't part of the extraction above
  (input polling, the death-animation state machine, fire, animation), since
  `PowerStateActor.act()` now owns only the shared timer tick before delegating to
  `applyMovement`. Verified the exact original per-frame ordering survives the split:
  the old `act()` ticked star/invincible/shield timers *and* the star-color-cycle render
  update in one block, before checking `dyingAnimated`/`transitionFrames`; the new split
  ticks the three countdowns in `PowerStateActor.act()` first, then `Player.applyMovement`
  runs the color-cycle update as its own first line before its own dying/transitioning
  checks — same relative order, confirmed by tracing through by hand (the one place the
  two versions could theoretically differ - whether `starTimer`'s countdown is checked
  before or after this frame's own decrement - is provably unobservable, since `paint()`
  only ever reads the color-cycle state when `hasStar()` is still true, and the two
  versions never disagree about `hasStar()` within the same frame).

**This is the highest-risk mechanical phase** — `Player.java` is 1150 lines and the
scaffolding/physics boundary, while conceptually clean (per
[MARIO_GAME_MECHANICS.md §4](MARIO_GAME_MECHANICS.md#4-player-mechanics)), touches many
fields. This doc's own advice to do it as several small, independently-tested commits
(timers, then transitions, then checkpoints, then forced-command scripting) assumed a
human implementer working across sessions with real playtests between steps; done here in
one pass instead, verified by careful reading plus a full clean compile rather than
incremental on-device checks — the exit criteria below still needs a real playthrough
before this is trusted the way B1/B2's own phases already were.

**Exit criteria:** full regression pass, with particular attention to grow/shrink
transitions, Star invincibility, and the flagpole/axe scripted sequences (`forcedCommand`)
— the parts of `Player` this phase moves the most code around in. **Compiled successfully**
via `gradlew :app:compileDebugJavaWithJavac --rerun-tasks` (a full clean rebuild), and every
inherited-field/method reference (`invincibleTimer`, `blinkVisible`, `checkpointX/Y`,
`isInvincible()`, `hasStar()`, ...) was grep-checked for accidental re-declaration/shadowing
in `Player.java` after the split, not just left to the compiler to catch.

---

## Phase F — HUD / menu / save-state / debug panel generalization

- [x] `platformer/state/LevelProgressState.java` (generalized `MarioSaveState` — namespace
  string as a constructor parameter instead of the hardcoded `"mario_save_state"`).
  `MarioSaveState` becomes a thin wrapper (composition, not a subclass — no static-hides-
  instance conflict here the way `MarioContext` had, but consistent with that precedent
  anyway) around one private `LevelProgressState` instance.
- [x] `platformer/state/ScoreLivesState.java` (generalized `GameStateController` —
  starting lives / coin-to-life threshold / score-per-coin as constructor parameters
  instead of `STARTING_LIVES=3`/`COINS_PER_LIFE=100`/`SCORE_PER_COIN=200`).
  `GameStateController` becomes a thin subclass here (plain inheritance works fine — every
  method is already an instance method, unlike `MarioContext`'s static-API problem).
- [x] `platformer/hud/StatusBar.java` (generalized `ScoreHud` — `update` now takes the
  generic `ScoreLivesState` base instead of the concrete `GameStateController`, so
  `MarioGamePlay`'s own state object satisfies it for free via inheritance, zero call-site
  change needed). **Scope note:** left the actual SCORE/COINS/LIVES text formatting
  hardcoded rather than also parameterizing it — the audit table names this as the
  Mario-specific part to remove, but with no concrete second consumer and no sketch given,
  a speculative formatter API would be complexity paid for nothing (see
  [PLATFORMER_ENGINE_ARCHITECTURE.md §6.1](PLATFORMER_ENGINE_ARCHITECTURE.md#61-premature-abstraction--the-biggest-risk)'s
  own warning against exactly this).
- [x] `platformer/hud/PauseOverlay.java` (moved, parameterized by button labels/skin) — this
  one had no other Mario-specific part at all (never referenced a concrete Mario type), so
  it genuinely just moved; `MarioGameScreen` now imports and constructs
  `platformer.hud.PauseOverlay` directly rather than keeping a Mario-side subclass, same as
  `StatusBar` (old `hud/ScoreHud.java`/`hud/PauseOverlay.java` deleted, not left behind as
  dead code).
- [x] `platformer/screen/WorldLevelSelectScreen.java`, per
  [PLATFORMER_ENGINE_ARCHITECTURE.md §3.6](PLATFORMER_ENGINE_ARCHITECTURE.md#36-worldlevelselectscreen).
  `screen/MarioMenuScreen.java` becomes a thin construction call (a plain subclass — no
  static-API collision risk here, `ScreenAdapter`'s own `show`/`resize`/`render` are all
  instance methods `MarioMenuScreen` never needs to redeclare).

  **Filled in past the architecture doc's own illustrative sketch** (which omits several
  real construction details): added `viewportWidth`/`viewportHeight` constructor
  parameters and had the generic screen build its own `LayerManager`/`ExtendViewport`
  internally, exactly like `MarioGameScreen` already does — the sketch's own constructor
  signature has no viewport parameter at all, an omission rather than a deliberate design
  choice (a `ScreenAdapter` needs *some* viewport to exist). Also used `IntFunction<String>`
  for the level labeler instead of the sketch's `Function<Integer, String>` — an unboxed,
  slightly better fit for `LevelNumbering::label`'s actual `String label(int)` signature,
  and a method reference binds to either equally well. Left "SELECT A WORLD"/"BACK"/"EXIT"
  and the "WORLD "-prefixed button/header text as fixed strings inside the generic class
  rather than adding constructor parameters for each — the sketch itself only bothers to
  parameterize one `title` string, treating the rest as generic-enough menu vocabulary; this
  implementation follows that same judgment rather than expanding it into a half-dozen
  string parameters nothing asks for.
- [x] `platformer/debug/LevelWarpPanel.java`, per
  [PLATFORMER_ENGINE_ARCHITECTURE.md §3.7](PLATFORMER_ENGINE_ARCHITECTURE.md#37-levelwarppanel).
  `debug/DebugPanel.java` becomes a thin construction call passing Mario's own
  "interesting tile types" allow-list.

  **One real design gap the architecture doc's own §3.7 paragraph doesn't address:**
  its text says only "the interesting-tile-types allow-list" is the per-game input, but the
  actual panel also builds a GOD MODE / INF LIVES / CYCLE POWER / SPEED toggle row — real
  UI, not level-warp generation, and (per the architecture doc's own audit table entry for
  `DebugPanel`, elsewhere in the same document) "god mode, power-state cycling are
  Mario-specific *concepts*, though the *pattern* generalizes." Resolved by adding a
  `List<RowBuilder>` constructor parameter (`RowBuilder` = `(Table, Skin) -> void`, one
  functional-interface row-builder per extra row) that a game supplies to insert its own
  toggle rows between the coordinate readout and the data-driven warp buttons, preserving
  the exact original visual order. `DebugPanel`'s own row-building logic had to move into a
  `private static` helper method (not an instance method) since `LevelWarpPanel`'s
  constructor needs the finished row list *before* `super(...)` returns, at which point Java
  forbids referencing any of `DebugPanel`'s own instance state — the old constructor-local
  `timeScaleButton`/`timeScale` mutable state (previously a field, referenced from its own
  click listener) now lives in a one-element array captured by that row's closure instead.
  Also added a `protected String coordinateText(int, int)` hook (default: "Player tile:
  (x,y)") so `DebugPanel` can still say "Mario tile" specifically, matching the original
  text exactly rather than silently changing debug-panel wording.

**Exit criteria:** full regression pass, **on-device** specifically for
`WorldLevelSelectScreen` — `MarioMenuScreen`'s own class doc already flags UI-layout
fragility (non-`setFillParent` positioning quirks) as a real risk here, so code review
alone isn't sufficient for this one file. **Compiled successfully** via
`gradlew :app:compileDebugJavaWithJavac --rerun-tasks` (a full clean rebuild) and grep-
verified no stray references to the deleted `hud.ScoreHud`/`hud.PauseOverlay` classes
remain anywhere in the tree — but compilation obviously can't confirm the on-screen layout
itself is still correct, so the world-select/level-select screens and the debug panel's
own button layout both need real on-device eyes before this phase is trusted.

---

## Phase G — Input generalization

- [x] `platformer/input/PlatformerCommand.java` (generalized `PlayerCommand` —
  `firePressed` becomes neutral `actionPressed`).
- [x] `platformer/input/TouchOrKeyboardInput.java` (generalized
  `MarioInputController`).
- [x] Deleted `input/PlayerCommand.java`/`MarioInputController.java` and updated all
  Mario call sites to use the generalized classes directly.

**Follow-up found on review:** the initial generalization pass dropped a genuinely
non-obvious, on-device-verified comment along with the code it explained —
`TouchOrKeyboardInput.poll()`'s touchpad-knob Y-axis reads (`down`/`up`) are inverted
relative to `Touchpad`'s own javadoc, because this engine's Y-down camera convention means
"physically lower on the touchpad" maps to *increasing* `getKnobPercentY()`. That's
deliberate, not a bug, and was verified by hand on-device in the original `MarioInputController`
— but the explanation didn't survive the mechanical rename, which is exactly backwards: a
generic, reusable class is *more* likely to have someone "fix" this back to the wrong
behavior without it, not less. Restored (rephrased to drop the Mario-specific
`MarioGameScreen` reference and note that a Y-up-camera game should swap the two
comparisons instead), along with two smaller comments the same pass had trimmed (why
polling beats a listener here; why the controller's buttons need edge-detection) and
lightweight per-field docs on `PlatformerCommand` distinguishing held vs. edge-triggered
fields.

**Exit criteria:** full regression pass — purely mechanical, low risk. **Compiled
successfully** via `gradlew :app:compileDebugJavaWithJavac`.

---

## What's deliberately not in this plan

- **Phase H** (build a second, minimal consumer to validate the design) — per
  [PLATFORMER_ENGINE_ARCHITECTURE.md §6.4](PLATFORMER_ENGINE_ARCHITECTURE.md#64-sequencing-against-the-reskin--decided),
  this has no bearing on Mario or the reskin and runs on its own schedule, whenever a
  second platform game is actually planned — not part of "update Mario to use the new
  engine."
- **`ART_SCALE`** — a reskin-time concern
  ([MARIO_RESKIN_PLAN.md §4.1](MARIO_RESKIN_PLAN.md#41-resolution-architecture-this-is-not-a-pure-content-swap)),
  lands in [MARIO_RESKIN_EXECUTION.md](MARIO_RESKIN_EXECUTION.md) Step R.1 after this
  entire plan is done, not as part of it.
- **Any change to `TILE_SIZE`'s actual value.** This plan makes `32` a single,
  explicitly-threaded value instead of ~40 scattered literals — it does not change what
  that value *is*. Changing it later is a feel-retuning exercise
  ([PLATFORMER_ENGINE_ARCHITECTURE.md §3.8](PLATFORMER_ENGINE_ARCHITECTURE.md#38-tilemetrics-making-tile-size-a-first-class-non-global-value)'s
  own closing point), not something this plan tries to make free.

# Mario Port Plan — Phase 2: Full-Game Port

This picks up where [MARIO_PORT_PLAN.md](MARIO_PORT_PLAN.md) left off. That document's Steps
0–8 are done (World 1 — `Level_11`–`Level_14` + bonus areas 97/98 — is playable, plus a
fidelity pass beyond the original checklist: turbo, brick-break fragments, growth/shrink
morph animations, checkpoint respawn, star color-cycle, flagpole slide). This document
plans **Phase 2: porting the rest of `C:\workspace\Mario`** — all 8 worlds, ~55 more
playable levels, and every mechanic they introduce.

**Status of this document:** planning only, written from a full source survey of
`C:\workspace\Mario` (see §1). Nothing in this document has been implemented yet.

**Decisions locked in:**
- **Full scope**: all 8 worlds, their castle/bonus levels, and the 5 `Clowd` beanstalk
  levels — everything in `Levels/` except `Level_Start` (dead menu-hack) and `TestArea`
  (dev scratch level).
- **Morse-code training stays deferred**, per the original plan's §9 — out of scope for
  this document and this phase. Nothing here blocks it or assumes it.
- **Steps are organized by mechanic, not by world number.** §8 of the original plan
  assumed World 2/4/6 would be "skin reuse" before harder worlds — the survey in §1 shows
  that's wrong (World 4's castle alone needs 7 same-level teleports; World 6 needs
  `CloudsNight`, `BalenceLift`, and `Bouncer`). Building each new mechanic once, then
  converting whichever levels need it, is more accurate than the original's
  world-number-ordered guess.
- **Runs entirely on the existing Nintendo-derived placeholder art** — per
  [MARIO_RESKIN_PLAN.md](MARIO_RESKIN_PLAN.md)'s §5 (revised 2026-09-07), the reskin +
  high-resolution art upgrade now happens as a single pass *after* this document is
  complete, not folded into Step P2.1 as originally planned. **Hard gate: this app must
  not reach anything beyond this dev machine / a closed internal test group while any
  step below is in progress** — no public beta, no store listing, no build shared
  outside the immediate dev team — until the reskin plan's §4.4.7 regression pass is
  clean. See that document's §5/§6 for the full reasoning and risk.

## 1. Gap analysis

### 1.1 Correction: World 1 itself is not fully done

Before any phase-2 work: two gaps exist **inside the already-shipped World 1**, found
during this survey, not part of phase-2 scope creep —

- **`Level_12` uses a patrol enemy** (`AddEnemyTurtlePatrol(145, 12, 7)`) that
  `LevelLoader.java`'s tile switch has no case for. The art
  (`enemy_turtle_patrol`/`flying_turtle_patrol`) is already packed but nothing spawns it.
- **`Level_14` (World 1's castle) is missing its entire finale.** Confirmed by grepping
  `level_14.json`: it contains `Axe` (x141,y8), `Boss` (x135,y7, `patrolLength: 141` —
  the constructor's `maxx` bound, already captured by the level converter), 7×`FireBar`
  (`extraInfo: "CW"/"ACW"` — rotation direction, already captured), 4×`Lava`,
  `BridgeBloks` (x128,y10,13 wide), and 6×`InvisibleBrckWithCoin` — **`LevelLoader.java`
  has no case for any of them**, so Mario currently walks through empty space where
  Bowser's bridge should be. The JSON/schema already has everything needed
  (`patrolLength`, `extraInfo`, `bridgeLength` fields all present and correctly
  populated) — this is a `LevelLoader`-side gap only, not a data-pipeline gap.

**Step P2.0 (below) closes both before any new-world work starts**, since the castle
finale it builds is reused by every single castle level in all 8 worlds (see §1.3) —
fixing `Level_14` for real *is* the first slice of phase 2, not a detour from it.

### 1.2 Full content inventory

64 level classes under `Levels/` (full per-level attribute/mechanic list came from
surveying `Levels/One` through `Levels/Eight` plus `Levels/Extra`; regenerate via
`grep -rln "extends BasicLevel\|extends Extra\." Levels/` if a fresh pass is needed).
Totals:

- **36 main levels** (11–14, 21–24, 31–34, 41–44, 51–54, 61–64, 71–74, 81–83, 841–845 —
  World 8 has 8 levels, not 4, ending in `Level_845`'s `AddPrincess`).
- **14 per-world bonus areas**, all thin subclasses of **7 reusable templates**
  (`Extra/BonusAreaA`–`G`: 5×UnderGround-themed, 2×Sea-themed).
- **5 `Clowd_level_for_*` beanstalk levels** (`attribute: "Clowd"`), all built around
  `AddBouncer` + a `Clowd_CheckPoint`/`ClowdGoUP_CheckPoint` climb-up transition.
- **No world-map screen exists in the original** — world/level selection was always the
  `LevelNumber==10` reused-gameplay-loop hack. Our real `MarioMenuScreen` already beats
  this; it just needs to grow from 1 world's levels to 8 (§3.2).

Themes in play beyond World 1's Ground/UnderGround/Castle: **Sea** (3 main levels — 22,
72, 844 — plus 2 bonus templates; a *full-level* alternate physics mode, not a tile
reskin — see §1.4), and **CloudsNight** (World 6's level 63 — a black-and-white tint
variant, cosmetic only as far as this survey found).

### 1.3 New mechanics, by what needs them

| Mechanic | First needed by | Also needed by | Complexity |
|---|---|---|---|
| Castle finale (`Axe`, `WoodenBridge`, `Boss`/`BossFire`/`Hammer`, `FireBar`, `Lava`) | World 1 (`Level_14`, already shipped-but-broken) | Every castle level, all 8 worlds | See §1.4 — smaller than it looks |
| `EnemyTurtlePatrol`/`FlyingTurtlePatrol` (bounded patrol, not full-level walk) | World 1 (`Level_12`, `Level_13`) | Worlds 3/4/6/7 | Small — same `Enemy` base, add a patrol-bound field |
| Same-level teleports (pipe warps) | World 4 castle (7 warps) | World 7 castle (12), Worlds 8's 841–843 (3–4 each) | Medium — dense but mechanically simple (teleport on touch) |
| `Helmet`/`HelmetShell`/`MovingHelmetShell` (3 colour variants) | World 4 (`Level_42`) | Worlds 6/7/8 | Small — same shape as existing `TurtleShell` pair |
| `BalenceLiftParent`/`BalenceLiftChild` (seesaw pair) | World 3 (`Level_33`) | Worlds 4/6 | Medium — two `Lift`s whose Y is inversely linked |
| `Bouncer`/`Spring` (launch pad) | World 6 (`Level_63`) | World 8, all 5 `Clowd` levels | Small |
| `Monkey`, `RocketLauncher`/`Rocket`, `SonOfABuitch`, `Spikey`/`SpikeyEgg` | Worlds 3/4/5/6/8 (scattered) | — | Medium each — read source per-class before porting (see §1.4's open items) |
| `FishyGround`/`FishyWater`/`OctoPussy` | Sea levels only | — | Small — bundled with water work |
| `CloudsNight` tint | World 6 (`Level_63`) | — | Small — a render-time palette/overlay, not new gameplay |
| `Clowd_CheckPoint`/`ClowdGoUP_CheckPoint` | The 5 `Clowd` levels | — | Small — a checkpoint variant |
| `Bombs`/`FlyingFishes` spawn timers (the original plan's deferred `SpawnController`) | Worlds 2/5/6/7/8 | — | Small — one ticking component |
| Water/swim physics mode | World 2 (`Level_22`) | Worlds 3 (partial strip), 5, 7, 8 + 2 bonus templates | **Largest single item** — see §1.4 |
| `Princess`/`WhyYouDOThis` end-of-game checkpoints | World 8 finale / every other world's castle | — | Small — message + state, no new physics |

### 1.4 What the survey got wrong on first pass (verify-by-reading paid off)

Two things assumed to be bigger than they are, confirmed by reading
`Bricks/Axe.java`/`Bricks/WoodenBridge.java`/`Objects/Boss.java` directly:

- **`WoodenBridge` is a plain static platform tile** (`Sprite`, `HitFromDown()`/
  `RemoveIt()` both no-ops) — reuse the existing static-terrain-tile pattern (like
  `Stone`), not a new mechanic.
- **`Axe` is a one-line invisible wall**, not a "chop the rope, bridge collapses"
  set-piece: its entire `update()` is `if (player.getX() > this.getX())
  player.setX(this.getX())`. No animation trigger, no bridge-collapse code anywhere.
- **Boss defeat does not require the axe at all.** `Boss` is a self-contained
  patrol/jump enemy (bounded ±3 tiles around its spawn, or ±`patrolLength` toward Mario
  once he's past its patrol zone — see the `GoNearMario` branch) that throws either
  `BossFire` (arcing fireball, `AddBoss` levels) or `Hammer` (`AddBossHammer` levels —
  same class, `SetHammer(true)` just switches which projectile timer fires) on a
  randomized timer. It dies from **6 Fire Mario fireball hits** (`Life=5`,
  decremented, dies at `<0`) **or an instant kill on any Star-powered touch/stomp**
  (`DirectFalling` animation + `smb_bowserfalls`/`smb_kick` sound + every other active
  enemy in the level also deactivated). A stomp *without* Star just damages Mario
  (`player.Decerease()`) instead of hurting the boss — jumping on Bowser doesn't kill him
  here, matching the classic games. **Level completion is not gated on defeating the
  boss** — nothing in `Boss`/`Axe` calls a checkpoint; the actual "cleared this castle"
  trigger is a separately-placed `CheckPoints` object past the boss (already showing up
  as our own `level_14.json`'s `"WhyYouDOThis"` checkpoint kind), so walking past a boss
  you didn't fight (or couldn't get past — the axe wall may or may not physically block
  that depending on exact tile placement, verify per-level) is legitimate, same as the
  original games.

Net effect: the "castle finale" is mostly **reused existing patterns** (a static tile, an
invisible wall, a checkpoint message) plus **one real new enemy** (`Boss`) and **one new
orbital hazard** (`FireBar` — `EnemyFireBall` instances arranged in a rotating ring
around a static pivot, 6 or 12 per ring, `extraInfo` "CW"/"ACW" sets spin direction).
Scope this accordingly — it's not the biggest item in this plan (water is).

### 1.5 Open items to verify by reading source before implementing (not blocking, but load-bearing)

- `SonOfABuitch` — behavior unread; name suggests a Bowser-Jr.-style enemy, confirm
  before assuming it reuses the ground-enemy pattern.
- `Spikey`/`SpikeyEgg` — not spawned via any `BasicLevel.Add*` helper found in this
  survey; trace how levels actually place it (likely a raw `Construct` `Item_Type` the
  `AddXxx` convenience-method survey missed) before writing its `LevelLoader` case.
- `MovingTurtelShell` vs. the already-ported `TurtleShell` — confirm whether this is a
  genuine second mechanic (e.g. the shell that's already sliding vs. one just kicked) or
  a near-duplicate that can reuse one class.
- `plant.java` (Piranha-Plant-equivalent) — art (`plant`/`plant_dark`) is already packed
  as background decoration only; confirm whether **any** already-shipped World 1 level
  expected it to be a live hazard (would be a 3rd World-1 gap alongside §1.1's two).
- Audio diff — 28 audio files exist in the source tree total; the exact list beyond
  World 1's `SOUND_EFFECTS`/`MUSIC_TRACKS` hasn't been enumerated file-by-file. Do this
  during Step P2.1 (asset pipeline), not by guessing which new sounds each mechanic
  needs.

## 2. Architecture changes required

### 2.1 Atlas strategy: switch from one growing atlas to per-theme atlases

World 1 alone already pushed `mario.atlas` to 2 pages (2048×2048 each) — and just
regenerating it exposed a real bug in `tools/mario-atlas-packer` (missing blank-line
page separator, fixed this session). Adding ~61 more PNGs across Sea/Castle-finale/
CloudsNight art on top of that single growing atlas means every level pays the memory
and page-count cost of every theme, forever, and makes hitting further multi-page
packer bugs more likely as it grows unbounded.

**Plan:** split `PackMarioAtlas` into one **common** atlas (player + all palette
variants, HUD skin, enemies/items shared across every theme, fonts) plus one atlas per
**theme** (`mario-ground.atlas`, `mario-underground.atlas`, `mario-castle.atlas`,
`mario-sea.atlas`, `mario-night.atlas` — matching the original plan's §5 package layout,
which already anticipated this and was never acted on since World 1 fit in one atlas).
`MarioResourceManager.load()` becomes level-attribute-aware: load `common` +
`theme(level.attribute)` only, and (new) unload the previous level's theme atlas on
`MarioGameScreen` teardown so switching worlds doesn't accumulate atlases in memory.

This per-theme split does double duty later: [MARIO_RESKIN_PLAN.md](MARIO_RESKIN_PLAN.md)
§4.3 relies on it to keep the high-resolution art upgrade's memory growth bounded, since
that upgrade multiplies packed pixel area per asset roughly by `ART_SCALE²`. No action
needed here beyond keeping the split real (don't let atlases quietly re-merge for
convenience) — the resolution/hitbox work itself is entirely out of scope for this
document, done afterward per the reskin plan's §5.

### 2.2 Player: water/swim as a sibling mode to `PlayerPowerState`, not a tweak

`attribute == "Sea"` sets `Player.Water = true` for the *entire level* at load — full
alternate physics (different gravity, speed caps, a `Swim()` stroke input, `WaterJump()`)
for the level's whole duration, not per-tile. Model this as a `Player.water` boolean
(set once at `MarioGameScreen` construction from `level.attribute`, not touched by
gameplay) gating a parallel constant block and a `swim()`/`waterJump()` method pair,
mirroring how `PlayerPowerState` already cleanly separates SMALL/BIG/FIRE constants —
don't thread `if (water)` branches through the existing ground physics methods.

### 2.3 `MarioMenuScreen`: world grid, not a flat level list

Currently hardcodes `LEVEL_NUMBERS = {11,12,13,14}`. Needs a world-select screen (8
worlds) opening a per-world level-select screen (4–8 main levels + that world's bonus
areas), using the existing `Table`/`TextButton` pattern — no new UI mechanism needed,
just another screen and a data table of world → level numbers.

### 2.4 `MarioSaveState` (planned in the original doc's §5, never built)

~55 levels with no persistence means every session restarts from World 1-1. Add a small
`state/MarioSaveState` backed by Android `SharedPreferences` (simplest option that fits
this app's existing patterns — check what Flappy Bird/Battle City use for their own
high-score persistence, if anything, and match it) tracking per-level "reached"/
"cleared" so `MarioMenuScreen` can grey out or lock unreached levels.

### 2.5 Teleport resolver

`LevelDefinition.TeleportLink` already parses `teleports[]` (confirmed — data pipeline
is fine) but nothing consumes it. Add a `collision/TeleportResolver` alongside the
existing `CheckpointResolver`: on player-tile overlap with a teleport's `inX/inY`,
reposition to `outX/outY` (same-screen, no level swap — distinct from
`CheckpointResolver`'s cross-level jumps). Dense in castle levels (up to 12 per level)
but each instance is the same simple rule.

### 2.6 Boss mini-framework

One new actor (`actors/enemies/Boss.java`) covering both `AddBoss`/`AddBossHammer`
variants via a constructor flag (matching the original's `SetHammer(boolean)`), owning
its own patrol/jump/throw-timer state machine (see §1.4). `BossFire`/`Hammer` are two
small projectile classes (arcing gravity, like the already-ported `FireBall` pattern).
`FireBar` reuses the existing enemy-fireball art/behavior in a new `OrbitingHazard`
actor (rotates N instances around a fixed pivot at a fixed radius — new but small).

## 3. Package additions

```
app/src/main/java/au/com/guidebee/morsetoolkit/activity/mario/
  actors/enemies/{Boss,Monkey,SonOfABuitch,Spikey,SpikeyEgg,
                  HelmetShell,MovingHelmetShell,FishyGround,FishyWater,OctoPussy}.java
  actors/enemies/patrol/{PatrolTurtle,PatrolFlyingTurtle}.java   # or fold into Enemy.java as a bounded-range mode
  actors/projectiles/{BossFire,Hammer,Rocket,LavaBall}.java
  actors/hazards/{Lava,FireBar,OrbitingHazard}.java
  actors/bricks/{Axe,WoodenBridge,RocketLauncher,Bouncer,BankWithItem,Tree}.java
  actors/lifts/{BalenceLiftParent,BalenceLiftChild}.java
  collision/TeleportResolver.java
  world/SpawnController.java                # Bombs/FlyingFishes ticking spawners
  state/MarioSaveState.java
  screen/MarioWorldSelectScreen.java         # or fold into an expanded MarioMenuScreen

tools/mario-atlas-packer/
  # PackMarioAtlas.java split to emit mario-common.atlas + mario-{ground,underground,castle,sea,night}.atlas
```

## 4. Step-by-step sequence

Each step ends with something playable — same discipline as the original plan. Numbered
`P2.x` to avoid colliding with the original doc's Steps 0–12.

**Step P2.0 — Fix World 1 for real** *(do this first — see §1.1)*
- P2.0.1 Port `Boss`/`BossFire`/`Hammer`/`FireBar`/`Axe`/`WoodenBridge`/`Lava` (§2.6).
- P2.0.2 Add `LevelLoader` cases for all of the above plus `InvisibleBrckWithCoin`.
- P2.0.3 Add the bounded-patrol variant of the existing ground-turtle enemy; wire
  `Level_12`/`Level_13`'s already-shipped-but-inert patrol calls.
- P2.0.4 **Vertical slice:** replay `Level_14` end-to-end — bridge, fire bars, lava,
  Bowser (or Hammer Bro, depending on which castle) all present and working; confirm the
  `WhyYouDOThis` checkpoint fires correctly past the boss.

**Step P2.1 — Asset & content pipeline scale-out**
- P2.1.1 Split the atlas packer into common + per-theme atlases (§2.1); repack
  everything currently shipped into the new layout first as a no-behavior-change
  refactor, confirm World 1 still renders identically before adding new art.
- P2.1.2 Audit and add the ~61 unpacked PNGs (§1.2's asset groups) into their theme
  atlases — still sourced from `C:\workspace\Mario\SandBox` as placeholder art, per the
  reskin plan's revised §5 ordering (reskin happens once, afterward, over the complete
  list this step finalizes — not here); audit the 28 audio files against current
  `SOUND_EFFECTS`/`MUSIC_TRACKS`.
- P2.1.3 Re-run `tools/mario-level-converter` across all remaining `Levels/*` classes;
  spot-check one converted JSON per world by hand against the original level layout.

**Step P2.2 — Menu, progression, save state**
- P2.2.1 `MarioSaveState` (§2.4).
- P2.2.2 Expand `MarioMenuScreen` into a world-select + per-world level-select flow
  (§2.3), reading unlock state from `MarioSaveState`.

**Step P2.3 — Teleports**
- P2.3.1 `TeleportResolver` (§2.5).
- P2.3.2 **Vertical slice:** convert World 4's castle (`Level_44`, 7 warps) and confirm
  navigation matches the original.

**Step P2.4 — The "novel enemy" batch**
- P2.4.1 `Helmet`/`HelmetShell`/`MovingHelmetShell` (reuse `TurtleShell`'s shape).
- P2.4.2 `BalenceLiftParent`/`BalenceLiftChild` (inversely-linked `Lift` pair).
- P2.4.3 `Bouncer`/`Spring`.
- P2.4.4 `Monkey`, `RocketLauncher`/`Rocket`, `SonOfABuitch`, `Spikey`/`SpikeyEgg` —
  read each source class fully before porting (§1.5's open items live here).
- P2.4.5 `Bombs`/`FlyingFishes` spawn timers (`world/SpawnController`).
- P2.4.6 **Vertical slice:** World 3 fully playable (exercises patrol enemies,
  `BalenceLift`, `Monkey`, the World-3 bonus area) — good integration checkpoint since it
  touches most of this step's new actors without needing water yet.

**Step P2.5 — CloudsNight & Clowd levels**
- P2.5.1 `CloudsNight` render tint (confirm it's cosmetic-only per §1.5, adjust if not).
- P2.5.2 `Clowd_CheckPoint`/`ClowdGoUP_CheckPoint`.
- P2.5.3 **Vertical slice:** World 6 (`CloudsNight` level, `BalenceLift`, `Bouncer`,
  `AddBossHammer` variant) + all 5 `Clowd` beanstalk levels.

**Step P2.6 — Water/swim**
- P2.6.1 `Player` water-mode constants/state (§2.2): gravity, speed caps, `swim()`,
  `waterJump()`.
- P2.6.2 `FishyGround`/`FishyWater`/`OctoPussy`.
- P2.6.3 Sea-theme terrain tiles (`brick_Sea`, `stone_Sea`, `chocolate_Sea`, `pump Sea`,
  ...) into the Sea atlas from P2.1.
- P2.6.4 **Vertical slice:** World 2's `Level_22` (a full Sea level) fully playable —
  the highest-risk single item in this plan; budget the most slack here.

**Step P2.7 — Remaining worlds & bonus content**
- P2.7.1 Worlds 4, 5, 7, 8 (each should now be mostly data conversion — every mechanic
  they need was built in P2.0–P2.6; confirm this holds before assuming zero new code).
- P2.7.2 World 8's finale specifically: `Princess` checkpoint (true ending state) and
  `WhyYouDOThis` (confirm the fake-out ending's message/behavior matches what P2.0
  already built for World 1–7 castles).
- P2.7.3 Remaining bonus areas (all 7 templates, applied per-world).
- P2.7.4 QA pass per world as it's added, same as the original plan's Step 11.3.

**Step P2.8 — Handoff to the reskin plan** *(this document's exit criterion)*
- P2.8.1 Confirm all 8 worlds are playable end-to-end on placeholder art with no known
  regressions — the release gate in this document's "Decisions locked in" stays shut
  until this is true.
- P2.8.2 Export the finalized `PackMarioAtlas.ASSETS` table (every entry actually built
  across P2.0–P2.7, not the §1.2 estimate) as the input to
  [MARIO_RESKIN_PLAN.md](MARIO_RESKIN_PLAN.md) §4.4.1's asset spec sheet.
- P2.8.3 Hand off to that document's §4/§5 execution sequence. Nothing further in *this*
  document proceeds toward public distribution until that plan's §4.4.7 regression pass
  is clean.

## 5. Testing approach

Same build→install→relaunch→ask-user-to-test cadence as phase 1, with one addition:
because most of P2.4 onward is "mechanic built once, then N levels reuse it," test the
**first** level that introduces a mechanic thoroughly (per this doc's vertical slices),
then treat later levels reusing it as lighter smoke tests (does it load, does the
specific new *data* — different patrol lengths, different teleport counts — work)
rather than re-verifying the mechanic itself each time.

## 6. Risks

- **Distribution gate** — this entire document runs on placeholder Nintendo-derived art
  by design (see "Decisions locked in" and [MARIO_RESKIN_PLAN.md](MARIO_RESKIN_PLAN.md)
  §5). The longer this phase takes, the more calendar time there is for that constraint
  to be forgotten and a build to leak beyond the dev team (a friend asked to try it, a
  screenshot posted, a beta-track upload). Track the gate explicitly rather than relying
  on memory; nothing here is safe to distribute until P2.8 hands off cleanly and the
  reskin plan's §4.4.7 regression pass is done.
- **Water physics is the single biggest unknown** — budget it last (P2.6) so every other
  mechanic is stable first, and don't assume the ground-physics `frames = delta *
  PHYSICS_FPS` scaling trick needs to change; verify the original's `Swim()`/
  `WaterJump()` constants translate the same way before assuming otherwise.
- **`SonOfABuitch`/`Spikey` behavior is unread** — don't estimate P2.4.4 as "small" until
  those two are actually opened; they're the two remaining unknowns in an otherwise
  well-understood mechanic list.
- **Atlas re-split (P2.1.1) touches every already-shipped World-1 asset reference** —
  do it as its own isolated, no-new-content step with a full World-1 regression pass
  before adding any new art on top, so a packing mistake doesn't get buried under
  unrelated new-content changes.
- **Save-state format** (P2.2.1) should be decided once, early — retrofitting a save
  format after several worlds' worth of levels exist to test against is more painful
  than picking a simple schema up front (e.g. a set of cleared level numbers is probably
  sufficient; resist adding more than `MarioMenuScreen` actually needs to render).

## 7. Post-P2.8 gap audit (2026-09-07)

All 8 worlds are now playable end-to-end and hand-tested (P2.8.1's exit criterion is
met). Before handing off to the reskin plan, a systematic class-by-class comparison was
run between `C:\workspace\Mario` and this port: four parallel sweeps covering
`Objects/` (vs `actors/enemies` + `Player.java`), `Bricks/` (vs `actors/bricks` +
`actors/lifts`), `Animations/`+`Collusion/` (vs `fx/` + `collision/`), and tile-dispatch/
sound-effect/misc-systems completeness (`Mario.java`'s full construct switch,
`AmitsAudioPlayer`, per-level invisible objects, warp zones, pause/cheats). Every finding
below was cross-checked against real level data (`Levels/**` + the converted
`level_*.json` files), not just the reference source, to separate genuine gaps from
original code that's dead or unreachable in real play.

**Correction to §1.4**: this document's own claim that "`Axe` is a one-line invisible
wall... No animation trigger, no bridge-collapse code anywhere" is **wrong**, confirmed by
this audit — see P2.9.1 below. `actors/bricks/Axe.java`'s doc comment repeats the same
incorrect claim and needs correcting alongside that step's implementation.

### 7.1 Findings

Confirmed real gaps (reachable in actual gameplay, not dead original code):

| # | Finding | Evidence it's real (not dead code) |
|---|---|---|
| 1 | Boss defeat has no "cut the bridge" finale — bridge tiles should blacken and the boss should fall, ported from `RemoveBridge()`/`Black.java`/`BossFallingAnim.java` | Triggered from `Collusion/Player_Brick.java:88-108`'s `getID()==15` branch (landing on the Axe from above) — live in every boss level, not commented out |
| 2 | Piranha Plants aren't ported at all (`Objects/plant.java`) | Auto-spawned from nearly every pump/pipe-top tile per `SandBox/Mario.java` case 12 (excluded only for `"OrangePump"`-named levels and the 4-2 Clowd bonus); `plant`/`plant_dark` art already packed in the atlas, unused |
| 3 | Enemies killed by a fireball/shell vanish instantly instead of flipping upside-down and falling off-screen (`Animations/FallingDeadSprites.java`) | Called from ~16 enemy classes' fireball-death methods (EnemyMashroom, EnemyTurtle(Patrol), FishyGround/Water, FlyingTurtle(Patrol), Helmet(Shell), Monkey, TurtleShell variants, Rocket, Spikey(Egg)) |
| 4 | Fireballs disappear silently on impact instead of a small explosion + (on a wall hit) a bump sound (`Animations/Explosion.java`) | `Collusion/FireBallToBricks.java:46,52`, `FireBallToEnemys.java:90,104` |
| 5 | Enemies never bounce off each other on contact (`Collusion/EnemyToEnemy.java`) | No port resolver exists for this pairing at all; original excludes items (mushroom/flower/life/star/coin) from the bounce, keep that exclusion |
| 6 | Question Mark blocks always render the Ground-colored sprite, never the grey `UnderGround`/`Castle` tint | Confirmed real blocks affected in `level_12.json`, `level_34.json`, `level_42.json`; `question_mark_grey` already packed, unused |
| 7 | Pause has no sound | Original plays `smb_pause` on both entering and leaving pause (`SandBox/Mario.java:530,540`) |
| 8 | The flagpole's `Bouncer` launch has no sound | Original reuses `smb_stomp` as the "boing" (`Player.Jump(int)`, called from the Bouncer's own collision branch) |
| 9 | Pit-fall death plays `smb_mariodie` | Original's pit-fall (`Restart()`) is silent — only the enemy-hit death path plays that sound; `actors/player/Player.java`'s own doc comment on `die()` already *claims* silence but the code doesn't match it |

Lower-priority / cosmetic (real, but small payoff relative to effort):

| # | Finding | Notes |
|---|---|---|
| 10 | No ambient rising-bubble particles while swimming (`Animations/Bubble.java`) | Purely decorative, `Objects/Player.java:105-108` spawns unconditionally while `Water` |
| 11 | Pipe-entry checkpoints freeze real Mario in place instead of hiding him and showing an animated double sliding in (`MarioGoingDownAnimation`/`MarioGoingInPump`) | Current behavior isn't broken, just visually plainer than the original |
| 12 | A brick that breaks while an enemy stands on it lets the enemy fall through immediately, instead of the original's brief `TemporaryAndInvisibleBrick` placeholder collider | Very obscure edge case (`Bricks/Brick.java:44`) |
| 13 | Level 12's warp-zone room doesn't freeze the camera or show the secret pipe-count numbers graphic (`Player_InvisibleObjects.java`'s `StopScroll`/`StopScrollAndNumber`/`scroll` markers, `smb_world_clear` sound) | Cosmetic only — the warp pipes themselves already work correctly via the checkpoint system regardless of this |

Needs a quick manual check before scoping (not yet confirmed as real or dead):

| # | Item | What to check |
|---|---|---|
| 14 | Does a kicked turtle shell break bricks on contact? (`Collusion/Enemy_Brick.java`, 228 lines, not fully read) | Classic mechanic; likely partially covered by the generic `TileMovement` wall-bounce every enemy already uses, but shell-breaks-brick specifically wasn't verified either way |

Confirmed dead/non-issues (no action needed): `LavaBubble` (never placed by any level),
"big/small mountain"/"grass"/"sky" tile codes (never placed), code 43 `CheckPoint` and
code 46 generic `Lift` (bodies commented out in the original itself), `AddBossFire` tile
placement (never called — the live path is `Boss`'s own dynamic throw, already ported),
standalone `Star` tile placement (only ever dispensed from bricks, already handled),
`smb_vine`/`smb_warning` (loaded but never played in the original either), the original's
empty cheat-code key-switch (`SandBox/Mario.java:2261-2308`, does nothing).

### 7.2 Step sequence

Same discipline as §4 — numbered to continue after P2.8, each ending with something
testable on-device.

**Step P2.9 — Boss finale + hit-feedback fidelity** *(highest priority — most visible)*
- P2.9.1 Bridge-cut boss-defeat sequence: port `RemoveBridge()`/`Black.java`/
  `BossFallingAnim.java`, wired from the same axe-landing contact `Collusion/
  Player_Brick.java` gates on. Correct `actors/bricks/Axe.java`'s doc comment (and this
  document's own §1.4) once implemented.
- P2.9.2 `FallingDeadSprites` - one shared fx class, wired into every enemy's
  `onDefeatedByProjectile()` (currently just `deactivate()`).
- P2.9.3 Fireball impact `Explosion` puff + wall-hit bump sound, in `actors/projectiles/
  FireBall.java`'s `explode()` and `collision/ProjectileCollisionResolver.java`.
- P2.9.4 `EnemyToEnemy` bounce - new `collision/EnemyToEnemyResolver` (or fold into the
  existing `EnemyCollisionResolver`), excluding items per the original.
- P2.9.5 **Vertical slice:** replay a boss level end-to-end (e.g. `Level_14`) and confirm
  the bridge visibly collapses and the boss falls; trigger a fireball kill and an
  enemy-vs-enemy contact in the same session.

**Step P2.10 — Piranha Plant**
- P2.10.1 Port `plant.java` as a new `actors/enemies/PiranhaPlant` (or similar):
  retreat-when-Mario-is-near, pop out otherwise, hurts on touch unless starred, killed by
  fireball/shell/stomped-shell (never by a direct jump-stomp, matching the original).
- P2.10.2 Auto-spawn from pump/pipe-top tiles in `LevelLoader`, respecting the original's
  exclusions (`"OrangePump"`-named levels, the 4-2 Clowd bonus).
- P2.10.3 **Vertical slice:** any level with several pipes (e.g. World 1's `Level_11`) -
  confirm plants pop/retreat correctly and don't spawn where excluded.

**Step P2.11 — Small fidelity fixes** *(cheap, do together)*
- P2.11.1 `QuestionMark` grey tint on `UnderGround`/`Castle` levels (`question_mark_grey`
  already packed).
- P2.11.2 `smb_pause` on pause-toggle (both directions).
- P2.11.3 `smb_stomp` on the flagpole `Bouncer`'s launch.
- P2.11.4 Remove `Player.die()`'s `smb_mariodie` call (pit-falls are silent in the
  original) - fix the code to match this port's own already-correct doc comment.
- P2.11.5 **Vertical slice:** one smoke pass touching all four (an UnderGround "?"
  block, a pause/resume, a `Bouncer`, a pit-fall) - no full replay needed, these are
  independent one-line fixes.

**Step P2.12 — Cosmetic polish** *(optional - lower priority, judge by time remaining)*
- P2.12.1 Ambient water bubbles (`Bubble.java`).
- P2.12.2 Pipe-entry animation (hide real Mario, show the sliding double).
- P2.12.3 `TemporaryAndInvisibleBrick` placeholder under a breaking brick.
- P2.12.4 Level 12 warp-zone camera-freeze + secret-numbers reveal (depends on P2.13.1's
  finding for `Player_InvisibleObjects` - may be folded in or dropped as not worth the
  camera-architecture change for a cosmetic payoff).

**Step P2.13 — Verify-then-decide items**
- P2.13.1 Read `Collusion/Enemy_Brick.java` fully; if a kicked shell breaking bricks is
  real and reachable, add it (small, reuses the existing brick-break path); otherwise
  mark it confirmed-dead and close this item.
- P2.13.2 Fold the warp-zone camera-freeze decision (§7.1 #13) into P2.12.4's scope or
  explicitly drop it - don't leave it open past this step.

Once P2.9-P2.13 are done (or explicitly descoped per-item with a reason noted here),
re-confirm P2.8's exit criterion still holds, then proceed to the reskin plan unchanged.

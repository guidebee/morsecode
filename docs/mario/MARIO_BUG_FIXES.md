# Mario Bug Fixes

A running log of gameplay bugs found and fixed after the initial port, kept separate from
[MARIO_PORT_PLAN.md](MARIO_PORT_PLAN.md)/[MARIO_PORT_PLAN_PHASE2.md](MARIO_PORT_PLAN_PHASE2.md)
(which record how the port was *built*) — this doc records what broke afterward, why, and
how it was fixed.

**Two categories, and why the split matters:**

- **§1 Code fixes** — pure gameplay-logic bugs (physics, collision, state machines). Fixed
  entirely in Java; no sprite sheet, atlas region, or asset file changed. Irrelevant to the
  in-flight visual reskin ([MARIO_RESKIN_PLAN.md](MARIO_RESKIN_PLAN.md)) — nothing here
  changes when the art does.
- **§2 Sprite/rendering fixes** — bugs in how an actor's *existing* art gets drawn (facing
  direction, flip mechanism). These touch the same `Sprite`/`TextureRegion` plumbing the
  reskin's new art will be loaded through, so anyone doing reskin work on an affected actor
  needs to know the convention the fix now depends on — each entry says so explicitly.

Every fix below was verified with `./gradlew :app:compileDebugJavaWithJavac` (`BUILD
SUCCESSFUL`); none had an on-device regression test available in this session, so treat
"fixed" as "fixed per source-level analysis, compiles clean" and confirm on-device before
signing off.

## 1. Code fixes (logic only — no asset/sprite impact)

### 1.1 Fireball never despawned at the level's left edge, permanently eating a shot slot

**Symptom:** Fire Mario shoots left, the fireball reaches the world's left boundary, and
instead of bouncing/exploding it just sits there forever. Since `Player.shoot()` caps
concurrent fireballs at 2 (`Player.java`), a stuck fireball permanently occupies one slot —
after this happens twice, Mario can no longer shoot at all.

**Root cause:** `TileMovement.moveX()` (`platformer/core/TileMovement.java`) is the shared
horizontal-movement helper `FireBall` and several other actors (`Enemy`, `Mushroom`, `Star`,
`Life`, `FlyingTurtle`) all use. It only set its `blocked` return flag inside the two
`containsImpassableArea(...)` branches (an actual tile collision) — the separate line that
clamps the actor's `x` to `0` at the level's left edge (`actor.setX(Math.max(0, newX))`) did
*not* set `blocked`. `FireBall.act()` only calls `explodeAgainstWall()` when `moveX` returns
`true`, so a fireball pinned at `x=0` (no tile there to trigger the impassable-area check)
never explodes — it just sits at the boundary forever, still `active`.

**Fix:** `TileMovement.moveX()` now also sets `blocked = true` whenever the boundary clamp
itself is what stopped the actor (`newX < 0`), not just on a real tile hit. This fixes the
fireball (explodes at the boundary like it does against a wall) and, as a side effect, fixes
the same class of actor (`Enemy`/`Mushroom`/`Star`/`Life`/`FlyingTurtle`) getting stuck
pinned at `x=0` instead of turning around — they all read the same return value to decide
whether to reverse direction.

**Files:** `platformer/core/TileMovement.java`.

### 1.2 Mario rendered in front of the pipe he's entering, instead of hidden behind it

**Symptom:** Reported on level 1-2's end-of-level pipe, but reproducible on every pipe
checkpoint in every level: Mario slides toward the pipe and the sliding "double"
(`PipeEntryAnimation`) stays visibly on top of the pipe artwork the whole time, instead of
appearing to sink into it.

**Root cause:** `MarioContext.spawn()` just calls `LayerManager.append()`, which — per that
method's own doc — always gives the new actor the *highest* z-index (drawn last, i.e. on
top of every existing layer). That's correct for the real `Player` (appended once at level
load, meant to draw in front of the tile world for normal walking) but wrong for
`PipeEntryAnimation`: spawned *mid-level*, well after the tile world, it landed on top of
absolutely everything, including the pipe tile it's supposed to be swallowed by.

**Fix:** `PipeEntryAnimation.spawn()` now calls `animation.setZIndex(MarioContext.world().getZIndex())`
right after spawning — this moves it to just behind the tile world layer (still in front of
the scenery background band), so the pipe tile now correctly draws over it as it slides in.

**Files:** `activity/mario/fx/PipeEntryAnimation.java`.

### 1.3 Mario's power-up (Big/Fire) reset to Small on every level transition

**Symptom:** Feature gap, not a regression — carrying a Mushroom/Flower power-up through a
pipe or flagpole into the next level always dropped Mario back to Small, every time,
regardless of what he was holding.

**Root cause:** Each checkpoint transition (`MarioGamePlay.goToLevel`) constructs a brand
new `MarioGameScreen`, which constructs a brand new `Player` — and `Player`'s constructor
always starts at `PlayerPowerState.SMALL` (`super(x, y, PlayerPowerState.SMALL.width,
PlayerPowerState.SMALL.height, true, PlayerPowerState.SMALL)`). Nothing carried the outgoing
`Player`'s power state into the new one; `GameStateController` (score/coins/lives) already
survives a level swap this way, but power state had no equivalent.

**Fix:** Added `Player.applyPowerState(PlayerPowerState)` — applies a power state instantly
(no grow/shrink flipbook), reusing the existing `changePowerState` machinery. Added
`MarioGamePlay.pendingPowerState` (mirrors how `gameState` already crosses a level swap):
`MarioGameScreen.advanceToNextLevel()` stashes `player.getPowerState()` into it right before
switching screens; the new screen's constructor applies it to the freshly-built `Player`
right after construction. `MarioGamePlay.startLevel()` (a fresh pick from the menu) resets
it to `SMALL`, matching `gameState.reset()`'s own reset-on-fresh-start semantics.

**Files:** `activity/mario/actors/player/Player.java`, `activity/mario/MarioGamePlay.java`,
`activity/mario/screen/MarioGameScreen.java`.

### 1.4 Getting hit by an un-stompable enemy (fire chain, Piranha Plant, ...) spammed extra sound

**Symptom:** Reported as "the death sound plays multiple times" against level 4's fire
chain (`FireBar`/`OrbitingFireball`) and against Piranha Plants. Investigation showed
`Player.beginDeathAnimation()`'s own `dyingAnimated` re-entry guard was already correct —
`smb_mariodie` really did only ever play once. The actual repeating sound was
`"smb_stomp"`.

**Root cause:** `EnemyCollisionResolver.resolve()` classified *any* overlap where Mario's
center was above the enemy's as a stomp, and unconditionally reacted to it as a successful
one:

```java
if (overlapY <= overlapX && playerAbove) {
    enemy.onStomped(player);
    player.bounceOffEnemy();                 // always ran
    MarioContext.gameState().addScore(STOMP_SCORE);
}
```

Several enemy types "can't be safely stomped" by design (`Spikey`, `PiranhaPlant`,
`OrbitingFireball`, `SpikeyEgg`, `OctoPussy`, `FishyWater`, `Boss`) — their `onStomped()`
override just redirects to `onTouchedSide()`, which hurts/kills Mario *instead* of dying to
the stomp. The resolver had no way to tell that redirect apart from a genuine kill, so it
called `player.bounceOffEnemy()` regardless — and that method has **no re-entry guard of
its own** (no invincibility/dying check, unlike `Player.shrink()`): it plays `"smb_stomp"`
and bounces Mario every single frame the "player is above" condition still holds. Since a
fire chain segment or a plant is stationary (or slowly orbiting) and Mario stays overlapping
it for several frames — including while `dyingAnimated`, since `bounceOffEnemy()` doesn't
check that either — that unguarded sound refired every frame, layering over the one real
`"smb_mariodie"` and reading as the death sound "playing multiple times."

**Fix:** `Enemy.onStomped(Player)` now returns `boolean` — `true` only for a genuine, safe
stomp (kills a normal enemy, deserving the bounce+score reaction); `false` when it actually
just delegated to `onTouchedSide()` to hurt/kill Mario instead. `EnemyCollisionResolver`
only calls `bounceOffEnemy()`/awards `STOMP_SCORE` when `onStomped()` returns `true`.
Updated every override: `Spikey`, `PiranhaPlant`, `OrbitingFireball`, `SpikeyEgg`,
`OctoPussy`, `FishyWater`, `Boss` → always `false` (pure delegate to `onTouchedSide`);
`EnemyTurtle`, `EnemyTurtlePatrol`, `FlyingTurtle`, `FlyingTurtlePatrol`, `Helmet`,
`HelmetShell`, `TurtleShell`, `Rocket` → `true` (unchanged behavior, just the new return
type).

**Files:** `activity/mario/actors/enemies/Enemy.java`,
`activity/mario/collision/EnemyCollisionResolver.java`, and the `onStomped` override in
every enemy class listed above.

### 1.5 Several enemies just vanished on death instead of falling like the original

**Symptom:** Stomping a `RocketLauncher`'s fired rocket, a Hammer Bro-analog Monkey, the
Lakitu-analog `SonOfABuitch` (level 4-1), or killing the end-of-castle Boss all just made
the enemy disappear on the spot — the original has each of them fall out of view first.

**Root cause:** Confirmed against the reference source at `C:\workspace\Mario`. This port
never had an equivalent of the original's `Animations/DirectFalling.java` at all — a
genuinely different death animation from the one already ported as `FallingDeadSprite`
(`Animations/FallingDeadSprites.java`, plural — used for the original's fireball/shell
kills): gravity ramps up from a standing start (`Gravity = 0`, capped at `3`, `+0.25`/tick)
rather than `FallingDeadSprites`' initial upward kick (`Gravity = -10`, capped at `10`,
`+0.5`/tick), and only its 4-arg constructor drifts horizontally at all — two distinct
animations with distinct tuning, not one animation with a parameter. Four original classes'
own death reactions use it and had nothing to port to:

- `Objects/Rocket.java#MarioJumpedOnEnemy()` — 3-arg (straight down), plus `smb_kick`.
- `Objects/Monkey.java#MarioJumpedOnEnemy()` — 4-arg (drifts via `MariotoRight()`), no extra
  sound (the engine's generic stomp bounce/sound already covers it).
- `Objects/SonOfABuitch.java#MarioJumpedOnEnemy()` — 3-arg (straight down), no extra sound.
- `Objects/Boss.java` — 3-arg (straight down), used by *every* one of its own death paths
  (star stomp, star touch, a moving shell, running out of fireball hits), not just a stomp;
  this port's `Boss.java` had an explicit doc comment calling the missing corpse a deliberate
  simplification — it wasn't one, it was this same gap, just written down as if intentional.

**Fix:** Added `activity/mario/fx/DirectFallingSprite.java`, porting `DirectFalling`'s
behavior with three entry points matching the original's constructor variants actually in
use: `spawn(x, y, region)` (straight down, for an enemy whose region already bakes its
left/right facing into separate frames — `SonOfABuitch`, `Boss`), `spawn(x, y, region,
movingRight)` (straight down, additionally folding in a *runtime-mirror-only* sprite's
current facing — `Rocket` is this port's one case that needs it, see §2.1), and
`spawnDrifting(x, y, region, driftRight)` (`Monkey`). Built correctly from the start using
the same lesson §2.1 below documents — flips are applied via `setTransform` after
construction, never by flipping the region before handing it to `Sprite`'s constructor.
`Rocket.onStomped()`, `Monkey.onStomped()` (new override), `SonOfABuitch.onStomped()` (new
override), and `Boss.die()` all now spawn one instead of deactivating silently.

**Files:** `activity/mario/fx/DirectFallingSprite.java` (new),
`activity/mario/actors/enemies/Rocket.java`,
`activity/mario/actors/enemies/Monkey.java`,
`activity/mario/actors/enemies/SonOfABuitch.java`,
`activity/mario/actors/enemies/Boss.java`.

### 1.6 Grabbing the axe while the boss was still alive showed a duplicate Mario

**Symptom:** Level 1-4 (and any other castle level): if Mario reaches the boss and grabs
the axe *before* killing it with fireballs, a second Mario appears on screen alongside the
real one for the whole bridge-collapse animation. Killing the boss with fire first (so it's
already dead by the time the axe is touched) skips the sequence entirely and never shows
the duplicate.

**Root cause, revision 2 - the first fix below (`setVisible`) turned out to target the wrong
layer, and uncovered a bigger sequencing bug behind it:**

- **First attempt:** hide the real `Player` (`setVisible(false)`) while `MarioGhost` stands
  in for him, restoring visibility once the post-boss checkpoint is reached. This didn't
  work - on-device testing still showed two Marios, *and* lost the walking animation
  entirely (just a static frame) for the whole sequence. Investigating why turned up that
  `PowerStateActor.paint()` (`Player`'s own base class) never checked `isVisible()` at all
  before drawing - unlike `microedition.Sprite#paint`'s own `final` implementation, which
  does. `setVisible(false)` was silently a no-op for `Player` specifically, so nothing this
  fix did could have ever hidden him - fixed as its own thing (below), but insufficient on
  its own, since simply hiding the walking real Mario for several seconds just replaces "two
  Marios" with "no visible walking Mario at all" until he's shown again.
- **The actual root cause:** `triggerAxe()` put the real `Player` under forced auto-walk
  *immediately* at axe-touch time. Reading the reference source's actual sequencing
  (`SandBox/Mario.java#RemoveBridge` and `Animations/BossFallingAnim.java#update`) shows
  this is backwards: the original's `RemoveBridge()` never touches the player at all -
  it's `BossFallingAnim.update()`'s own `getY() > 700` branch, reached only once the boss
  has *finished* falling, that calls `game.player.MoveForward()` and `game.removeDemoMario()`
  together. Until then, the real Mario just stands still, in the exact same spot the ghost
  was spawned at - perfectly co-located and pixel-identical, nothing to visibly "clone." This
  port instead started him walking away immediately, so for the whole multi-second collapse
  the real (moving) Mario and the frozen ghost were never in the same place at once. In an
  open level the camera scrolling to follow the walking real Mario would carry him out of
  frame fast enough that this barely registered; a castle's boss arena sits at the level's own
  right edge, so the camera can't scroll any further and both stayed on screen together the
  whole time.

**Fix:**
- `PowerStateActor.paint()` (`platformer/actor/PowerStateActor.java`) now checks
  `isVisible()` before calling `paintPowerState`, matching `Sprite#paint`'s own contract -
  a real, independent bug fix (anything else that ever calls `player.setVisible(false)`,
  e.g. the existing pipe-entry sequence, was equally silently broken).
- `triggerAxe()` no longer starts the walk itself for the boss-still-alive case - it just
  freezes the real `Player` (an empty forced command) at the boss's own position.
  `BossFallingAnim` now takes the `Player` too, and once its fall finishes, puts him under
  forced auto-walk right - matching the original's own `MoveForward()` call, reached the
  same way (once the boss has finished falling) - instead of an immediate walk.

**Revision 3 - still visible on-device after revision 2:** freezing the real Player in place
made him and the still-spawned `MarioGhost` stand-in *pixel-identical and co-located*, which
should have been indistinguishable - but on-device testing still showed a duplicate for the
whole collapse. Since the real, frozen Player already looks exactly like a standing Mario
on his own, the stand-in was never actually needed once he stopped walking away immediately
- so rather than debug why two identical, identically-positioned sprites were reading as
"two Marios" (a `setVisible`-style toggle on the ghost would have hit the exact same silently-broken
`isVisible()` gap `PowerStateActor` had, since `MarioGhost` had the same missing check),
`MarioGhost` was removed from this sequence entirely - it's simpler to not draw a redundant
sprite than to make a redundant sprite reliably invisible. `MarioGhost.java` was deleted
outright (its only other reference, `Player.isFacingRight()`, was unused everywhere else too
and removed with it) since nothing else in the codebase used it.

**Files:** `activity/mario/screen/MarioGameScreen.java`, `activity/mario/fx/BossFallingAnim.java`,
`platformer/actor/PowerStateActor.java`, `activity/mario/actors/player/Player.java`,
`activity/mario/fx/PipeEntryAnimation.java` (doc references only),
`activity/mario/fx/MarioGhost.java` (deleted).

### 1.7 The debug panel's "Cycle Power" could grow Mario into a ceiling

**Symptom:** Level 1-4, right at the spawn point: clicking the debug panel's "Cycle Power"
button to go Small → Big embeds Mario in the ceiling tile directly above him. Doesn't happen
in normal play - only the debug shortcut can trigger it, but it gets in the way of using the
button to test power states early in a level.

**Root cause - two separate bugs, both needed fixing:**

1. A Small → Big transition claims its extra 32px of headroom immediately and
   unconditionally, the moment the growth *starts* (`startTransition`'s own `preShiftUp32`
   branch, matching the original's own immediate `this.setY(this.getY() - 32)`) - with no
   collision check of any kind. Fine for a real Mushroom pickup (always level-placed with
   that headroom already clear); not fine for the debug cycle, which can trigger growth
   wherever Mario happens to already be standing, including right under Level 14's own
   spawn-point ceiling.
2. **Found while verifying the first fix didn't actually stop the embedding:**
   `changePowerState` - which runs once the grow *flipbook finishes*, not when it starts -
   independently re-derives the same "keep feet planted" shift from `getHeight()`. But
   `getHeight()` is still the *pre-transition* size at that point (nothing touches the
   actor's real size mid-flipbook, only the separate `transitionWidth`/`transitionHeight`
   fields used for rendering) - so for a Small → Big transition specifically, this
   re-applies the *exact same* 32px upward shift `preShiftUp32` already made at the start,
   for a **64px total** instead of the intended 32px. That's a real, general bug (every
   ordinary Mushroom pickup jumps Mario's feet 32px higher than where they started, not
   just where he's drawn - easy to miss in open air with nothing above to hit), not
   debug-only, and it's what defeated fix #1 above on its own: a headroom check sized for a
   32px shift can't protect against a 64px one.

**Fix:**
- Added `Player.pendingYPreShifted`, set whenever `startTransition` pre-shifts `y` this way,
  and checked by `changePowerState`: if set, treat the transition's *starting* height as
  already equal to the target (so the redundant shift becomes a no-op) instead of
  re-deriving one from the stale `getHeight()`. This is the real fix, and it applies to
  every Small → Big growth, not just the debug cycle.
- Added `Player.ensureHeadroomForGrowth()`, called only from `debugCyclePowerState()` right
  before a Small → Big cycle: probes upward from Mario's current position for a clear
  32×64 box and, if blocked, nudges him downward first (1px steps, capped at two tiles) so
  the transition's now-correctly-sized shift lands him somewhere clear. Deliberately not
  applied to `grow()`/`startTransition()` themselves - real Mushroom pickups need no such
  check and shouldn't silently reposition Mario if one were ever needed there for some
  other reason.

Verified both pieces together against Level 14's actual tile data (`level_14.json`): the
real arrival checkpoint into it (from Level 13) lands Mario at tile (2, 5), directly under a
stone ceiling block spanning rows 2-4 - zero clearance for a same-spot grow before either
fix - with open space at rows 5-6 before a rising staircase resumes solid ground at row 7.
With both fixes, the debug nudge nets exactly one tile of downward correction and the
(now-single) 32px growth shift lands Big Mario cleanly in that gap.

**Files:** `activity/mario/actors/player/Player.java`.

### 1.8 Carrying Big/Fire into Level 1-4 spawned Mario stuck in the same ceiling

**Symptom:** Clear the level 1-3 as Big (or Fire) Mario, so his power state carries into
1-4 (§1.3) - he spawns already embedded in the same ceiling tile §1.7 covers, immobile,
regardless of the debug panel. Spawning Small never has this problem; only a carried-over
Big/Fire spawn does.

**Root cause:** A cousin of §1.7's bug, in a different method. `Player.applyPowerState()`
(added for §1.3, to restore the outgoing power state on a fresh `Player`) reused
`changePowerState()`'s "keep Mario's feet planted, shift the box up by the height
difference" logic - correct for a live Mushroom pickup or a debug cycle, where Mario is
already settled and standing on solid ground at the moment of growth. But
`applyPowerState()` runs immediately after construction, *before gravity has ever had a
frame to settle him* onto the real floor below the spawn tile - Small Mario's own spawn
here has a full tile of empty air below him and falls into place over the next frame or
two. Shifting a *spawned* Big Mario's box up from that same raw, pre-fall spawn Y put his
head in the low ceiling directly above it - a ceiling his *unshifted* box was already clear
of, since Big is exactly the one tile taller that Small's own fall would otherwise have
covered.

**Fix:** `applyPowerState()` no longer shifts `y` at all - it just applies the new size in
place and lets the normal per-frame gravity in `applyMovement` settle him from there, same
as every other spawn regardless of power state. Confirmed against the same Level 14 tile
data as §1.7: Big Mario's unshifted spawn box (top at the checkpoint's own row, two tiles
tall) already rests exactly on the row-7 staircase with zero clearance to spare - no fall,
no shift, no embedding.

**Files:** `activity/mario/actors/player/Player.java`.

### 1.9 Riding a lift showed the jump pose instead of the walk cycle

**Symptom:** Level 1-3 (and anywhere else with a moving lift): stand on a lift and press
left/right - Mario doesn't animate walking, he just shows the airborne "jump" pose (one arm
raised) the whole time he's on it, moving or not.

**Root cause:** A lift isn't part of the tile grid, so within the same frame:
1. `Player.applyMovement()` runs `moveYWithCollision()` first, which unconditionally clears
   `onGround` to `false` whenever gravity ticks it even slightly positive and no *tile* is
   found below (true every frame on a lift, since there's no tile there by definition) - then
   immediately calls `updateAnimation()`, which picks the airborne pose whenever `!onGround`.
2. Only afterward, in the same frame's separate collision-resolver pass, does
   `LiftCollisionResolver` call `Player.landOnLift()` to re-confirm `onGround = true` - too
   late to affect the animation choice already made in step 1.

So a lift rider's `onGround` was `false` at the exact moment `updateAnimation()` read it,
every single frame, regardless of actual footing.

**Fix:** `updateAnimation()`'s airborne-pose check now also accepts `onLift` (set by
`landOnLift()`, cleared by `LiftCollisionResolver` only once it no longer finds a landing
spot) as proof of standing, not just this frame's own (not-yet-corrected) `onGround`. Since
`onLift` carries over from the previous frame's resolver pass, it still correctly reflects
"still on the lift" at the point this frame's animation is chosen, even though `onGround`
itself hasn't been re-confirmed yet.

**Files:** `activity/mario/actors/player/Player.java`.

### 1.10 Mario could stand on an un-hit invisible brick

**Symptom:** Level 1-4 (and every other level using one): an `InvisibleBrck` - invisible
until hit from below, at which point it turns into a real, standable `Iron` block - was
already fully solid/standable *before* being hit. Mario could land on top of and walk into
the side of thin air.

**Root cause:** Confirmed against the reference source. `Collusion/Player_Brick.collided`
wraps every directional collision reaction in an `if (b.getID() != 17)` guard *except* the
hit-from-below one (brick ID 17 is `InvisibleBrck`) - an untriggered invisible brick blocks
nothing from the top or sides, only reacting to a jump into its underside. This port's
`InteractiveBrick` registers every brick uniformly as a `SolidTile`, and `InvisibleBrck`
never overrode that - `isActive()` was `true` from construction (needed so the
hit-from-below check could still find it), which also made `TileWorld.containsImpassableArea`
- the same generic check `Player`'s landing and horizontal-movement code uses - treat it as
solid from every direction.

**Fix:** Added `InteractiveBrick.blocksLanding()` (default `true`), overridden to `false` on
`InvisibleBrck`. `Player.moveXWithCollision` and `moveYWithCollision`'s landing (`dy > 0`)
branch now look up which specific brick is causing an "impassable" result and skip
blocking/landing when it says it doesn't - falling straight through it, same as the
original. `moveYWithCollision`'s hit-from-below (`dy < 0`) branch deliberately keeps using
the raw, unfiltered check, so jumping into it from underneath still works exactly as before.

**Files:** `activity/mario/actors/bricks/InteractiveBrick.java`,
`activity/mario/actors/bricks/InvisibleBrck.java`,
`activity/mario/actors/player/Player.java`.

## 2. Sprite/rendering fixes — relevant to the reskin

**Read this section if you're touching `Rocket`, `FishyGround`, or any other actor art as
part of [MARIO_RESKIN_PLAN.md](MARIO_RESKIN_PLAN.md).** The bug here is in the *mechanism*
these two actors used to flip their sprite for a rightward launch, not in any specific pixel
content — but the fix changes what the *replacement* art needs to assume about its own
default facing direction, and how right-facing is achieved for these two actors going
forward. Every other actor's left/right facing (e.g. `Player`, `EnemyTurtle`, `Spikey`) uses
a *different*, already-correct mechanism (dedicated mirrored frames baked into the sprite
sheet, or splitting first and flipping each already-split frame after — see
[MARIO_GAME_MECHANICS.md §13.3](MARIO_GAME_MECHANICS.md#133-frame-strip-slicing-convention))
and is **not** affected by this bug or this fix.

### 2.1 Rocket (and the ambient jumping fish) always faced left, even when launched right

**Symptom (as reported, level 5-2):** the `RocketLauncher` turret fires its projectile
rightward toward the player, but the sprite keeps facing left the whole flight — looks like
it's flying backward.

**Root cause:** `Rocket.regionFor()` (and, found by inspection while fixing this,
`FishyGround.regionFor()` — identical pattern, same bug, never separately reported) built
its right-facing sprite by flipping the `TextureRegion` *before* handing it to the `Sprite`
superclass constructor:

```java
TextureRegion frame = MarioResourceManager.region("rocket_launcher").split(tileSize, tileSize)[3][0];
if (movingRight) {
    TextureRegion flipped = new TextureRegion(frame);
    flipped.flip(true, false);   // <- discarded, see below
    return flipped;
}
```

`microedition.Sprite`'s constructor immediately re-slices whatever region it's given via
`TextureRegion.split(frameWidth, frameHeight)` (this is the *normal*, correct way every
other actor passes its sprite sheet in — see §13.3). But `TextureRegion.split()`'s own doc
comment says exactly what happens here: *"This will not work on texture regions returned
from a TextureAtlas that either have whitespace removed or where flipped before the region
is split."* Looking at the implementation confirms it: `split()` rebuilds every sub-region
from raw pixel `x`/`y`/`width`/`height` coordinates (`getRegionX()`, `regionWidth`, ...),
completely ignoring the source region's `u`/`u2` flip state. So the `.flip(true, false)`
above had **zero effect** on what actually got drawn — every `Rocket`/`FishyGround` fired or
launched rightward rendered with the original, unflipped (left-facing) art regardless of
`movingRight`.

Confirmed against the actual sprite pixels, not assumed: dumping the alpha mask of
`rocket_launcher` frame index 3 (`app/src/main/assets/mario-common1.png`, region `xy: 36,
1352`, the 4th 32×32 cell) shows a tapered/pointed silhouette on the **left** edge and a
flat edge on the right — i.e. the base art's canonical default facing is **left**. (The
`bw_rocket_launcher` variant — the "ghost"-colored one used on CloudsNight levels — shares
the exact same silhouette, just re-palette'd; same bug, same fix.)

**Fix:** Construct with the plain, unflipped region, then flip via `Sprite`'s own
`setTransform(TRANS_MIRROR)` when `movingRight` — that mirror is applied at *draw time*,
after `split()` has already run, so it actually takes effect:

```java
super(regionFor(blackAndWhite, tileSize), tileSize, tileSize, x, y, movingRight);
if (movingRight) {
    setTransform(TRANS_MIRROR);
}
```

**Files:** `activity/mario/actors/enemies/Rocket.java`,
`activity/mario/actors/enemies/FishyGround.java`.

**What this means for the reskin:**
- **The convention going forward:** author `Rocket`'s (and `FishyGround`'s) replacement art
  facing **left** as the single stored frame, exactly like the original `rocket_launcher`/
  `fish_red` regions do today — do **not** bake a separate pre-flipped right-facing frame
  into the sheet for these two, and do **not** try to fix facing by pre-flipping the region
  in a `regionFor()`-style helper the way the old (buggy) code did. Right-facing is handled
  entirely at runtime by the `setTransform(TRANS_MIRROR)` call now in the constructor —
  new art only has to get the left-facing pose right.
- **This is actor-specific, not a global rule.** Don't assume every actor works this way:
  `Player`'s own walk-cycle mirroring (`startTransition()`) and most enemies' baked
  left/right frame pairs (e.g. `EnemyTurtle`'s frame `2` vs `0`) are unaffected by this bug
  and use their own, already-correct conventions — check the specific actor's constructor
  before assuming which pattern applies (same "always check the consuming Java class" rule
  [MARIO_RESKIN_JUNIOR_DEV_GUIDE.md §3](MARIO_RESKIN_JUNIOR_DEV_GUIDE.md) already gives for
  frame *size*, now also applies to facing-direction mechanism).
- **If you find another actor doing the same "flip-then-split" trick**, it has this same
  latent bug (confirmed still present, lower severity, not yet fixed here: `FlyingTurtle`/
  `FlyingTurtlePatrol`'s `onDefeatedByProjectile()` flips a shell region horizontally before
  handing it to `FallingDeadSprite`, which also re-splits internally via its own 1-arg
  `Sprite` constructor — the corpse likely always falls with the same un-flipped shell pose
  regardless of which way the turtle was facing. Cosmetic, on a one-off death animation,
  left as-is pending a real report.)
- **`DirectFallingSprite`** ([§1.5](#15-several-enemies-just-vanished-on-death-instead-of-falling-like-the-original))
  — for `Rocket` specifically (not `Monkey`/`SonOfABuitch`/`Boss`, which all bake facing
  into separate frames and need no special reskin handling), its stomp-death corpse also
  reuses the plain `rocket_launcher`/`bw_rocket_launcher` frame-3 region and gets its facing
  from `setTransform`, not a baked frame. Same "author left-facing only" rule applies to it;
  no separate reskin note needed beyond what's already said for `Rocket` above.

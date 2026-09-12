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

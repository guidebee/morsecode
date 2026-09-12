# Mario Player Guide

A player-facing manual for the Mario mini-game inside the Morse Code Toolkit app — how to
control it, what every power-up and enemy does, and a guided tour of all 8 worlds. Every
sprite shown below is pulled directly from the game's own art files, so what you see here
is exactly what you'll see on screen (on today's placeholder art — see the note at the
bottom on the upcoming visual refresh).

For the full technical breakdown of every level (minimaps, exact enemy/item counts,
secrets), see [MARIO_LEVEL_ATLAS.md](MARIO_LEVEL_ATLAS.md). For how any of this works
under the hood, see [MARIO_GAME_MECHANICS.md](MARIO_GAME_MECHANICS.md).

## Controls

| Action | Touchscreen | Keyboard (desktop testing) |
|---|---|---|
| Move left/right | Drag the on-screen joystick left/right | ← / → or A / D |
| Duck (Big/Fire only) / enter a pipe below you | Drag the joystick down | ↓ or S |
| Enter a same-level pipe ahead of you | Walk into the pipe's mouth, drag the joystick right | → or D |
| Climb a beanstalk | Drag the joystick up | ↑ or W |
| Jump | Button **B** | Space, ↑, or Z |
| Run (hold) / Throw a fireball (tap, Fire Mario only) | Button **A** | X |

Jump and fire are **tap** actions (they trigger on the frame you press, not every frame
you hold); running is a **hold** — keep button A / X held while moving to run faster.
Ducking only does anything once you're Big or Fire Mario — Small Mario is already short
enough that there's nothing to duck under.

## The core loop

Run and jump across a scrolling side view of each level, collect coins and power-ups,
avoid or stomp enemies, and reach the flagpole (or, in a castle, defeat/bypass the boss
and reach the checkpoint past it) to finish the level. Falling into a pit, touching an
enemy while small, or running out of health via repeated hits costs a life; running out
of lives ends the game.

## Growing up: the three power states

<p align="center"><img src="assets/mario-sprites/montages/player_states.png" width="700" alt="Player power states and animations"></p>

| State | How you get there | What changes |
|---|---|---|
| **Small** | Starting state, or after any hit while Big/Fire | One hit while Small costs a life |
| **Big** | Touch a Mushroom | Taller, survives one hit (shrinks back to Small instead of dying), can duck |
| **Fire** | Touch a Fire Flower while Big | Can throw fireballs (tap Fire/Run) — up to 2 in flight at once; taking a hit shrinks you to Small directly (not back to Big) |

A growth/shrink always plays a short transformation animation first (see the "morph"
rows in the image above) before your new size takes effect. **Invincibility Star**
touches also change how you're drawn (see below) without changing your size.

## Power-ups and pickups

<p align="center"><img src="assets/mario-sprites/montages/bricks_items.png" width="840" alt="Bricks and items"></p>

| Pickup | Effect |
|---|---|
| **Coin** | Adds to your score. Comes from placed coins, "?" blocks, or breaking bricks. |
| **Mushroom** | Grows Small Mario to Big (or does nothing extra if already Big+). |
| **Fire Flower** | Grows Big Mario to Fire Mario. |
| **Star** | Temporary invincibility — you flash through cycling colors, and touching *any* enemy during this window destroys it instead of hurting you. Wears off after a while. |
| **1-Up** | An extra life. |

**"?" blocks and bricks:** hit them from below (jump into their underside) to trigger
them. A "?" block reveals its item once and then turns into a plain used-up block. A
brick either breaks apart (if you're Big or Fire) or just bonks harmlessly (if you're
Small) — Small Mario can't break bricks. A gold **Bank** brick keeps dispensing coins for
about a second and a half of repeated hits before also turning into a used-up block.

## Enemy field guide

<p align="center"><img src="assets/mario-sprites/montages/enemies.png" width="840" alt="Enemy field guide"></p>

The golden rule: **jump on top of most enemies to defeat them; touching them from the
side hurts you** (unless you have a Star, in which case any touch destroys them). A few
enemies break that rule — noted below.

| Enemy | Can you stomp it? | Notes |
|---|---|---|
| **EnemyMushroom** (walking mushroom-shaped enemy) | Yes | The most common enemy in the game by far — just walks forward, falls off ledges into pits, turns around at walls. |
| **EnemyTurtle** / **Turtle (patrol)** | Yes — stomping leaves a shell behind | Kick the shell (touch it while it's resting) to send it sliding — it'll defeat other enemies it hits, but will also hurt *you* if it hits you. |
| **FlyingTurtle** / **FlyingTurtle (patrol)** | Yes | The patrol variant bobs up and down in place rather than flying around. |
| **Helmet** / **HelmetShell** | Yes, and it's **fireball-immune** | Same shell-kick trick as EnemyTurtle, but Fire Mario's fireballs bounce right off it. |
| **Monkey** | Yes | Throws hammers at you on a timer — stay mobile. |
| **Spikey** / **SpikeyEgg** | **No — never** | Every touch, from any side including straight down, hurts you unless you have a Star. Go around it, not over it. |
| **SonOfABuitch** | It floats out of stomp range | Sways side to side above you and drops `SpikeyEgg`s, which hatch into Spikeys on landing. |
| **PiranhaPlant** | It only pops up out of pipes | Bobs out of tall pipes when you're not standing close; wait for it to retract, or approach from a direction it can't reach you from. |
| **FishyWater** / **OctoPussy** | Yes (Sea levels) | OctoPussy rests, then darts toward you — time your approach. |
| **Boss** | Only with a Star | An ordinary stomp/touch just hurts you — jumping on its head doesn't work here either. Six Fire Mario fireballs (or one Star touch) defeat it. |

Every enemy that *can* be stomped can also be destroyed by a Fire Mario fireball or a
kicked shell — Helmet/HelmetShell only via the shell-kick, since they shrug off
fireballs.

## World tour

Every world follows roughly the same shape — a grassland level, a themed second level,
a sky/lift level, then a castle with a boss — except World 8, whose "castle" is really a
5-room gauntlet. Full level-by-level detail (including secrets) is in the
[Level Atlas](MARIO_LEVEL_ATLAS.md); here's the short version:

| World | What to expect |
|---|---|
| **1 — Green Hills** | The tutorial arc: bricks, pipes, the first enemies, the first boss fight, and a secret pipe room in Level 1-2 that warps you straight to World 2, 3, or 4 if you find it. |
| **2 — The Deep** | An underwater level — swimming feels very different from running/jumping: gentler gravity, a paddle-stroke jump, and fish/octopus enemies that don't fall to gravity at all. |
| **3 — Sky Gardens** | Introduces patrol enemies (that turn around at a fixed boundary rather than at walls), a hammer-throwing Monkey, and seesaw lift platforms. |
| **4 — The Winding Caves** | A floating, egg-throwing enemy joins the roster, and this world's castle is the first built around same-level pipe warps instead of one straight bridge. |
| **5 — Turret Row** | Rocket-firing turrets that only shoot once you leave their immediate blast-safe zone. |
| **6 — Nightfall** | The one and only black-and-white "night" level in the game (purely a palette swap — plays identically to a normal Ground level), plus launch-pad bouncers and the first hammer-throwing boss. |
| **7 — The Maze Fortress** | The most elaborate pipe-warp castle in the game — 12 same-level warps and almost no enemies; the challenge is entirely about finding the right path. |
| **8 — The Gauntlet** | Three long grassland levels, then a 5-room castle maze with genuine dead-end loops (some pipes lead you right back to the start of the maze), one last underwater approach, and the true ending. |

## Secrets worth knowing

- **The World 1 warp room** — Level 1-2 (the underground level) hides a small room with
  four pipes; three of them skip you straight ahead to World 2, 3, or 4. Like every
  same-level pipe, you enter by walking into its mouth (hold Right), not by pressing Down.
- **Beanstalks** — five levels have a hidden beanstalk entrance (hold Up at the right
  spot); climbing one is a short bonus-coin detour, not a progression shortcut.
- **Bonus rooms** — fourteen small coin rooms are tucked behind secret pipes throughout
  the game, always looping back to wherever you found them.
- **The castle "fake-out" ending** — every world's castle boss level ends with "your
  quest is over... but our princess is in another castle," advancing you to the next
  world. Only World 8's final room delivers the real ending.

## A note on the art

Every sprite pictured in this guide is the game's current placeholder art, carried over
directly from the original desktop game this was ported from. It's there so every
mechanic and level layout could be built and tested before original art exists — it is
**not** the final look of the game, and per
[MARIO_RESKIN_PLAN.md](MARIO_RESKIN_PLAN.md), the app won't ship publicly until it's
replaced. Every character, enemy, and world name used informally in this guide (and the
game's own current on-screen text) will change along with the art.

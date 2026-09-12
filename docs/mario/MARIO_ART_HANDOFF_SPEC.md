# Mario Art Handoff Spec — brief for an artist or an AI art tool

This is the document to actually hand to whoever draws the *next* skin — a human artist,
or the prompt/spec you feed an AI image tool. It's self-contained: it doesn't assume the
reader has seen any other doc in this set or read a line of the game's Java source.

It exists because this reskin (the current "Ampere's Run" pass, done 2026-09) hit two
real, shipped bugs from files that *looked* fine but didn't match the spec exactly — a
wrong filename that silently fell back to old art with no error, and a wrong pixel size
that rendered a pipe at half-width with an enemy visibly misaligned on top of it. Neither
was a code problem. Both are exactly the kind of mistake this doc is written to prevent
by being explicit about the parts that have to be *exact*, not "close enough."

## 1. Why this works at all — read this before anything else

The game never hard-codes a filename or a sprite's pixel size into its logic. Every
character/tile/effect is loaded by a short *name* (`"player"`, `"boss"`, `"stone"`, …),
and a separate build tool decides which actual PNG file backs that name:

1. It looks for the name's exact filename (see the tables below) in one folder —
   currently `docs/assets/mario-sprites/reskin-source/`.
2. If it's not there, it silently falls back to whatever art shipped before.
3. Whatever it finds gets packed into the texture sheets the game actually loads.

**This means a complete new skin is just a folder of PNG files with the right names and
sizes** — dropping them in and re-running the pack step is the entire integration. No
code changes, ever, as long as every file matches its spec below exactly. That last
word is the whole point of this document.

## 2. The four rules that must never be broken

1. **Filename is exact and case-sensitive.** `turtledark.png` and `Turtle_Dark.png` are
   different files as far as the build is concerned — a near-miss doesn't error, it just
   silently keeps the *old* art with zero warning. Copy every filename in the tables
   below character-for-character, including capitalization, spaces, and the couple of
   original-era misspellings (yes, `TurtelShell.png` is really missing an "t" — that's
   the actual expected name).
2. **Total pixel size is exact — not just "a multiple of 32."** Most sprites are simple
   32×32-per-frame grids, but a real handful are **not tile-sized at all**: a pipe body
   is 64×32 (two tiles wide), a lava/water hazard column is 32×128 (four tiles tall), the
   two castles are 160×160 and 304×352, the parallax skies are a full 1536×448. Several
   classes size the on-screen sprite directly from the file's own pixel dimensions — ship
   the wrong size and the object silently renders at the wrong scale or position, with no
   error at build time. **The "W×H total" column below is not a suggestion; treat it the
   same as the filename.**
3. **A multi-frame file is one strip image, not separate frame files.** If a row says
   "4 cols × 1 row," that means *one* PNG whose width is exactly 4× a single frame's
   width, frames laid left-to-right (and top-to-bottom for multiple rows) with no
   padding or gutters between them. The engine slices it as `width/cols` ×
   `height/rows` — uneven frame sizes or gaps between frames will slice wrong.
4. **PNG, RGBA, transparent background where the shape isn't a full rectangle.**
   Everything renders as flat pixel art with nearest-neighbor scaling (no smoothing) —
   soft/anti-aliased edges will look blurry and inconsistent next to hard-edged
   neighbors. Native resolution is 32px = 1 tile; there is no supported higher-res mode
   (see §6 if you're tempted to go bigger).

## 3. Delivery checklist (what to actually send back, and how it gets verified)

- One flat folder (subfolders only for the handful of `CloudsNight/…` entries called out
  in the tables) containing every file you were assigned, named exactly per the tables.
- Nothing else needs to be touched — no code, no config, no per-file metadata.
- Whoever integrates it:
  1. Drops the folder's contents into `docs/assets/mario-sprites/reskin-source/`
     (merging with/overwriting existing files, one asset at a time is fine — you don't
     need all ~122 files finished before testing any of them).
  2. Runs `bash tools/mario-atlas-packer/pack.sh` and checks its own printed summary:
     the "`N / 122 assets sourced from reskin overlay`" count should go up by exactly the
     number of files added, and the page/region counts shouldn't jump unexpectedly (a
     surprise new page usually means a size mismatch on one of the files).
  3. Runs a full-file dimension audit before trusting anything visually — compare every
     delivered file's actual pixel size against the tables below programmatically, not
     by eye. (This exact check is what caught both real bugs mentioned in the intro.)
  4. Compiles the app (`./gradlew :app:compileDebugJavaWithJavac`) — this only proves
     nothing is missing/broken at load time, not that the art looks right.
  5. **Actually looks at each result on-device or in the running game** — this is the
     step that catches "technically correct file, but reads as the wrong thing" (a
     stretched texture, a mistimed animation, a color that doesn't read against its
     background). Nothing earlier in this list substitutes for it.

## 4. Creative brief

### 4.1 The current identity (either continue it, or replace it wholesale)

The game currently ships a sci-fi/robot re-theme called **"Ampere's Run"**. If the new
skin is meant to be a variation on this rather than a total departure, here's the
existing cast and world names:

| Role | Name | Design intent |
|---|---|---|
| Player character | **Ampere** | A small scout robot. Grows visibly larger/bulkier in its two power-ups, doesn't change color scheme for "Fire" separately from "Big" unless you want to. |
| Mushroom-equivalent (grow) | **Battery cell** | Glowing power-cell icon |
| Fire-Flower-equivalent | **Charge coil** | A coiled/stacked energy-cylinder icon |
| Star-equivalent (invincibility) | **Overclock chip** | A circuit-chip/gem icon; the game palette-swaps the player 3 ways while this is active regardless of your art (see §5's player table) |
| Coin | **Bolt/gear** | Spinning collectible |
| 1-Up | **Spare chassis** | A small robot-head/chassis icon |
| Goomba-equivalent | **Scuttler** | Small skittering ground enemy, most common in the game |
| Koopa-equivalent | **Roller** | Retreats into a shell when stomped |
| Buzzy-Beetle-equivalent | **Plater** | Armored, same shell-kick mechanic, immune to the player's projectile |
| Bowser-equivalent (boss) | **The Warden** | Large sentry/tank-style boss |
| World themes | **Surface / Substrate / Fortress / Flooded Sector / Night Shift** | Replace Ground/UnderGround/Castle/Sea/CloudsNight 1:1 — see §5's per-theme palette |

**The one hard constraint, regardless of whether you keep this identity**: don't
reproduce Nintendo's specific character silhouettes or color schemes (no red-overalls
plumber, no spiky green shell-turtle, no mushroom-with-eyebrows) — a palette-swapped
trace of the same silhouette doesn't actually solve what this reskin exists to do.

### 4.2 Established color palette (reuse it for continuity, or hand back a full replacement)

Four world themes recolor the *same* terrain/enemy shapes rather than needing fully
separate art each — if the new skin keeps this convention, here's the current palette
(base / dark-outline / light-accent), so new pieces read as "the same world" as existing
ones:

| Theme | Base | Dark | Light/accent |
|---|---|---|---|
| Surface (Ground) | `#8A97A6` | `#4D5866` | `#C7D2DB` |
| Substrate (UnderGround) | `#2F4A41` | `#16241F` | `#35D0A0` (teal glow) |
| Fortress (Castle) | `#6B2F2F` | `#3A1717` | `#D98C4A` (warm amber) |
| Flooded Sector (Sea) | `#2F6377` | `#163540` | `#7FE0E8` (cyan) |
| Night Shift (CloudsNight) | `#5A5A5A` | `#2C2C2C` | `#B8B8B8` (plain greyscale — no accent) |

A wholesale new skin doesn't have to reuse this palette at all — it only matters if you
want the new art to feel like a recolor pass over the current one rather than a fully
new identity.

## 5. General conventions that apply across many files (read once, applies everywhere)

- **Facing convention for a 4-frame ground enemy** (`turtle`, `helmet`, `spikey`, and
  similar): columns 0–1 are the **left**-facing walk pair, columns 2–3 are the
  **right**-facing pair. Don't assume the reverse.
- **Theme-recolor row order** for a multi-row enemy strip that covers all 4 world themes
  in one file (e.g. `enemy`, 2 cols × 4 rows): row order is **Sea, Ground, UnderGround,
  Castle** (not alphabetical, not the order the themes are listed elsewhere).
- **Player-family frame grid** (`player`/`big_player`/`fire_player`/the 6 Star-recolor
  variants, all 4 cols × 7 rows, one shared layout): index 0/1 = idle right/left, 2/3 =
  airborne right/left, 4–6 = walk-right cycle, 7 = skid-right, 8–10 = walk-left cycle,
  11 = skid-left, 16–19 = swim-sink right/left pairs, 20–23 = swim-rise, 24/25 =
  duck/crouch right/left. (Index = row×4 + column.)
- **A "3-frame idle bob"** (`question_mark`/`question_mark_grey`) plays frames in the
  order 0,0,1,2,1,0 — frame 0 is rest, 2 is the peak of the bob, so make the visual
  progression rest→mid→peak across those 3 frames, not 3 arbitrary poses.
- **A "boss-style" 3×2 look-at-player strip** (`boss`, and `monkey`) uses linear index =
  row×3+col: 0/1 = look-left idle pair, 4/5 = look-right idle pair, 2 = a special pose
  (spitting-fire for the boss), 3 is unused/can duplicate anything.
- **`brick_peaces`** (the breakable-brick fragment strip, 2 cols × 4 rows): row 0 is
  unused, row 1 = Ground/Sea, row 2 = UnderGround, row 3 = Castle — each row a 2-frame
  "tumbling debris" flipbook, not 4 different fragment shapes.
- **Everything under `CloudsNight/`** is that file's black-and-white/night palette
  variant of the same design, not a different design.
- **Files whose name is a `Theme` variant of another** (`_Castle`, `_Sea`,
  `_UnderGround`, "grey", "dark", "white", "red") are the *same* character/object
  recolored for that context, not a new design — reusing the base silhouette and only
  changing the palette keeps the read consistent across a level.

## 6. Full asset table

122 named assets total. **Total px** is the whole file's pixel size (width×height);
**Frame grid** is cols×rows; **Per-frame** is what a single animation frame measures
(total ÷ grid — this is what actually needs to look like one coherent pose/tile).
Everything is native 32px/tile resolution (**do not** author at a higher resolution and
expect it to be scaled — see the note after this table).

### 6.1 Terrain (theme-specific ground tiles)

| Filename | Total px | Grid | Per-frame | Notes |
|---|---|---|---|---|
| `brick.png` | 32×32 | 1×1 | 32×32 | Surface breakable brick |
| `brick_UnderGround.png` | 32×32 | 1×1 | 32×32 | Substrate breakable brick |
| `brick_Castle.png` | 32×32 | 1×1 | 32×32 | Fortress breakable brick |
| `brick_Sea.png` | 32×32 | 1×1 | 32×32 | Flooded Sector breakable brick |
| `stone.png` / `stone_UnderGround.png` / `stone_Castle.png` / `stone_Sea.png` / `stone_Castle_Sea.png` | 32×32 each | 1×1 | 32×32 | Solid (unbreakable) terrain tile, 5 theme variants |
| `chocolate.png` / `chocolate_UnderGround.png` / `chocolate_Castle.png` / `chocolate_Sea.png` | 32×32 each | 1×1 | 32×32 | A second solid-terrain look, per theme |
| `stone_Clowd.png` | 32×32 | 1×1 | 32×32 | Separate solid tile for "Clowd" bonus areas |
| `CloudsNight/stone.png` | 32×32 | 1×1 | 32×32 | Night Shift's own solid-terrain look (see §5's greyscale note) |
| `chocolate_Castle.png` (reused) | — | — | — | Also serves as Night Shift's 2nd terrain look (`bw_chocolate`) — no separate file needed |
| `BrickPeaces.png` | 32×64 | 2×4 | 16×16 | Breakable-brick debris; see §5's row convention |
| `Bubble.png` | 32×14 | 4×1 | 8×14 | Sea-only ambient swim particle |

### 6.2 Pipes / pumps

| Filename | Total px | Grid | Per-frame | Notes |
|---|---|---|---|---|
| `pump.png` | **64×32** | 1×1 | 64×32 | Straight pipe body segment — **2 tiles wide**, not 1 (see §2 rule 2) |
| `pump top.png` (literal space in the name) | **64×64** | 1×1 | 64×64 | Pipe mouth/opening — 2×2 tiles |
| `pump Castle.png` / `pump top Castle.png` | 64×32 / 64×64 | 1×1 | same | Fortress theme recolor |
| `pump Sea.png` / `pump top Sea.png` | 64×32 / 64×64 | 1×1 | same | Flooded Sector theme recolor |
| `HoriImage.png` | 128×64 | 2×1 | 64×64 | Two 2×2-tile pipe-mouth pieces side by side, used for a specific level layout |
| `plant.png` | 64×48 | 2×1 | 32×48 | The pipe-dwelling enemy, popping out of a pipe — 2-frame bob |
| `plantdark.png` | 64×48 | 2×1 | 32×48 | Same, Substrate/dark palette |

### 6.3 Blocks / reveal items / power-ups

| Filename | Total px | Grid | Per-frame | Notes |
|---|---|---|---|---|
| `QuestionMark.png` | 96×32 | 3×1 | 32×32 | "?" block, Ground/Sea contexts — 3-frame idle bob, see §5 |
| `QuestionMarkGrey.png` | 96×32 | 3×1 | 32×32 | Same, UnderGround/Castle contexts (dimmer palette) |
| `Mashroom.png` | 32×32 | 1×1 | 32×32 | Battery-cell/Mushroom-equivalent, static reveal icon |
| `Mashrooms.png` | 64×32 | 2×1 | 32×32 | Same power-up, walking/ground-placed 2-frame version |
| `Flower.png` | 128×32 | 4×1 | 32×32 | Charge-coil/Fire-Flower-equivalent, 4-frame (e.g. a pulsing glow loop) |
| `Star.png` | 128×32 | 4×1 | 32×32 | Overclock-chip/Star-equivalent, 4-frame sparkle/shine loop |
| `CoinAnim.png` | 128×32 | 4×1 | 32×32 | The "pop out of a hit block" coin-reveal effect, 4-frame |
| `Coin.png` | 96×32 | 3×1 | 32×32 | The static, level-placed coin pickup, 3-frame spin |
| `1UP.png` | 64×32 | 2×1 | 32×32 | Spare-chassis/1-Up, 2-frame |
| `Iron.png` | 128×32 | 4×1 | 32×32 | The "used up" block left after a `?`/coin block is exhausted — 4 frames, one per theme, **in Sea, Ground, UnderGround, Castle order** |
| `BridgeBloks.png` | 32×32 | 1×1 | 32×32 | A brick-equivalent used for a specific bridge-shaped level layout |
| `Explosion.png` | 96×32 | 3×1 | 32×32 | Generic small explosion puff (fireball-vs-wall, fireball-vs-enemy), 3-frame growth |

### 6.4 Enemies

| Filename | Total px | Grid | Per-frame | Notes |
|---|---|---|---|---|
| `enemy.png` | 64×128 | 2×4 | 32×32 | Scuttler/Goomba-equivalent, the most common enemy — 2-frame walk × 4 theme rows (Sea/Ground/UnderGround/Castle order, §5) |
| `turtle.png` / `turtledark.png` | 128×48 | 4×1 | **32×48** | Roller/Koopa-equivalent — **taller than a tile**; cols 0-1 left-facing, 2-3 right-facing (§5) |
| `TurtelShell.png` / `TurtelShelldark.png` | 32×32 | 1×1 | 32×32 | Roller's shell (note the source's own "Turtel" spelling — copy it exactly) |
| `TurtelShellRed.png` | 32×32 | 1×1 | 32×32 | A warning-red shell variant (used by one enemy type's projectile-death pose) |
| `TurtelShellFilp.png` / `TurtelShellFilpdark.png` / `TurtelShellFilpRed.png` | 32×32 | 1×1 | 32×32 | Lowest priority — not reached by the main game content, but still loaded |
| `EnemyTurtlePatrol.png` | 128×48 | 4×1 | 32×48 | A fixed-palette (always "Surface"-toned) patrol variant of Roller |
| `FlyingTurtle.png` / `FlyingTurtledark.png` | 128×48 | 4×1 | 32×48 | A hovering/flying variant of Roller — same frame convention |
| `FlyingTurtlePatrol.png` | 128×48 | 4×1 | 32×48 | Bobs vertically in place — only frames 0/1 are ever shown (no left/right distinction) |
| `Monkey.png` | 96×96 | 3×2 | 32×48 | A hammer/projectile-throwing enemy — boss-style 3×2 look-at-player layout, §5 |
| `Helmet.png` / `Helmetdark.png` / `Helmetwhite.png` | 128×32 | 4×1 | 32×32 | Plater/Buzzy-Beetle-equivalent, 3 palettes — same walk convention as `turtle` but tile-sized, not tall |
| `HelmetShell.png` / `HelmetShelldark.png` / `HelmetShellwhite.png` | 32×32 | 1×1 | 32×32 | Plater's shell, matching palette |
| `SonOfABuitch.png` | 64×48 | 2×1 | 32×48 | A floating enemy that drops projectiles — frame 0 = idle float, frame 1 = "about to drop" pose |
| `SpikeyEgg.png` | 64×32 | 2×1 | 32×32 | The dropped projectile — 2-frame falling wobble, hatches into `Spikey` on landing |
| `Spikey.png` | 128×32 | 4×1 | 32×32 | Never safely stompable (design note, not an art constraint) — same 4-frame walk convention as `turtle`/`helmet` |
| `Boss.png` | 192×128 | 3×2 | **64×64** | The Warden/boss-equivalent — biggest single character, boss-style 3×2 layout (§5); this is the one asset worth spending the most polish on |
| `BossFire.png` | 96×16 | 2×1 | 48×16 | The boss's thrown projectile — 2-frame |
| `FishGrey.png` / `FishRed.png` | 64×32 | 2×1 | 32×32 | Sea-only swimming enemies, 2 palettes, 2-frame swim |
| `OctoPussy.png` | 64×48 | 2×1 | **32×48** | Sea-only chaser enemy — taller than a tile, 2-frame |
| `CloudsNight/Hammer.png` | 112×28 | 4×1 | 28×28 | A thrown-tool/hammer projectile, 4-frame spin (despite the "CloudsNight" path, this one file is used regardless of level theme) |
| `FireBall.png` | 64×16 | 4×1 | 16×16 | The player's own thrown projectile, and also reused for a rotating hazard-ring enemy — small, 4-frame |
| `Lava.png` | **32×128** | 1×1 | 32×128 | A static hazard — **4 tiles tall**, not a single tile |
| `LavaBall.png` | 64×32 | 2×1 | 32×32 | An erupting lava ball — 2-frame (rising/falling pose) |
| `Water.png` | **32×128** | 1×1 | 32×128 | Same "4 tiles tall" note as `Lava.png` |
| `Axe.png` | 128×32 | 4×1 | 32×32 | A castle-ending lever/switch the player triggers — 4-frame |

### 6.5 World mechanics / scenery

| Filename | Total px | Grid | Per-frame | Notes |
|---|---|---|---|---|
| `Wall.png` | 64×32 | 2×1 | 32×32 | Decorative vertical wall strip — frame 0 = top cap, frame 1 = repeating body |
| `RocketLauncher.png` / `CloudsNight/RocketLauncher.png` | **32×128** | 1×4 | 32×32 | A turret — row 0 = head, rows 1–2 = body segments (row 3 unused but must still exist) |
| `Bouncer.png` / `CloudsNight/Bouncer.png` | 32×32 | 1×1 | 32×32 | A launch-pad block |
| `Spring.png` | 96×64 | 3×1 | **32×64** | The decorative coil above a `Bouncer` — 2 tiles tall, 3-frame squish-and-recover |
| `WoodenBridge.png` | 32×32 | 1×1 | 32×32 | Static solid bridge-plank tile |
| `WhiteLine.png` | 32×32 | 1×1 | 32×32 | A thin decorative line/beam — the engine stretches this one to 13 tiles tall itself, so it just needs to tile cleanly top-to-bottom |
| `Chain.png` | 128×32 | 4×1 | 32×32 | Decorative chain-link strip, 4-frame |
| `Rope.png` | 32×32 | 1×1 | 32×32 | Decorative rope/cable tile |
| `SmallCastle.png` | **160×160** | 1×1 | 160×160 | End-of-level building — a single large image, not a tiled grid |
| `BigCastle.png` | **304×352** | 1×1 | 304×352 | The larger end-of-level building (boss levels) |
| `CloudsNight/SmallCastle.png` | 160×160 | 1×1 | 160×160 | Night Shift palette variant of `SmallCastle.png` |
| `CloudsNight/BigCastle.png` | 304×352 | 1×1 | 304×352 | Night Shift palette variant of `BigCastle.png` |
| `tree.png` | 160×64 | 5×2 | 32×32 | Scenery — row 0 cols 0-2 = left-cap/middle/right-cap of a solid decoration, col 3 = a vertical connector piece, col 4 unused; row 1 exists but isn't used by any current content |
| `CloudsNight/tree.png` | 160×64 | 5×2 | 32×32 | Same, Night Shift palette |
| `Lift.png` | **16×16** | 1×1 | 16×16 | A moving-platform texture, tiled by the engine to whatever width a lift needs — smaller than every other tile |
| `Mountain.png` / `Clouds.png` / `CloudsNight.png` / `Fence.png` / `Fence2.png` | **1536×448 each** | 1×1 | 1536×448 | **Full standalone parallax backdrops** — each is an entire level's whole background image (not a small tile the engine repeats), shown once per level based on which one that level picked. Must be seamless left-to-right (the engine literally tiles this same image 10× side by side to build a long level) |
| `Sea.png` (region `sea_background`) | 32×96 | 1×1 | 32×96 | The Sea-attribute backdrop — small and tiled every 32px horizontally (unlike the 5 above) |

### 6.6 Flags / level-end

| Filename | Total px | Grid | Per-frame | Notes |
|---|---|---|---|---|
| `Flag.png` | **4×288** | 1×1 | 4×288 | The flagpole rod — extremely thin and tall, tiles vertically |
| `FlagFence.png` | 4×288 | 1×1 | 4×288 | Same, for "Fence"/Night-Shift-background levels |
| `FlagTop.png` | 32×32 | 1×1 | 32×32 | The cloth banner that slides down the pole on touch |
| `FlagSphere.png` / `FlagSphereFence.png` | 32×32 | 1×1 | 32×32 | The fixed ornament at the top of the pole |
| `FlagWin.png` | 32×32 | 1×1 | 32×32 | A "victory" banner shown after reaching the pole |
| `AnotherCastleMessage.png` | 384×128 | 1×1 | 384×128 | End-of-boss-level text banner (a "not the real objective yet" message) — this one contains actual readable text, drawn directly into the image |
| `QuestComplete.png` | 384×128 | 1×1 | 384×128 | The true-ending banner — same "contains real text" note |

### 6.7 Player (the highest-priority, most-seen asset — worth the most polish time)

| Filename | Total px | Grid | Per-frame | Notes |
|---|---|---|---|---|
| `player.png` | 128×224 | 4×7 | 32×32 | Base/small form — full frame-index map in §5 |
| `BigPlayer.png` | 128×448 | 4×7 | **32×64** | Grown/"reinforced" form — twice the height, same frame-index layout |
| `FirePlayer.png` | 128×448 | 4×7 | 32×64 | "Charged" form — same layout as Big |
| `SmallToBigMarioAnim.png` | 384×64 | 12×1 | 32×64 | Grow transformation, played once |
| `BigToFireMarioAnim.png` | 320×64 | 10×1 | 32×64 | Charge-up transformation |
| `BigToSmallMarioAnim.png` | 320×64 | 10×1 | 32×64 | Shrink transformation (Big→Small) |
| `FireToSmallMarioAnim.png` | 320×64 | 10×1 | 32×64 | Shrink transformation (Fire→Small, direct) |
| `SmallToBigStarMaroAnim.png` | 384×64 | 12×1 | 32×64 | Grow transformation while invincible (color-cycling variant) |
| `SmallDeadMario.png` | 32×32 | 1×1 | 32×32 | Single static "defeated" pose |
| `SmallBlackMario.png` / `SmallGreenMario.png` / `SmallRedMario.png` | 128×224 each | 4×7 | 32×32 | Invincibility color-cycle palette swaps of the Small form — same layout as `player.png`, just flat-recolored |
| `BigBlackMario.png` / `BigGreenMario.png` / `BigRedMario.png` | 128×448 each | 4×7 | 32×64 | Same, for the Big form |

### 6.8 HUD

`Font.png` (256×48, 16×3), `Info.png` / `Info2.png` (640×480, 1×1) exist in the asset
table but are **confirmed dead code** — never actually loaded by the running game
(verified by searching every call site). **Don't spend art time on these** unless a
future code change starts using them; they're listed here only for completeness against
the master file list.

## 7. If you're briefing an AI image-generation tool specifically

Most AI art tools generate one image at a time and don't handle "a 4-frame animation
strip" as a single concept well. Two practical approaches:

- **Generate one panel at a time** (e.g. one 32×32 pose), keeping a fixed style/character
  reference across calls (many tools support a reference image or a fixed seed +
  consistent prompt prefix for this), then composite the panels into the strip
  dimensions in §6 yourself (any image editor or a five-line Pillow script: paste each
  frame at `(i * frame_width, 0)`).
- **Generate a same-count grid directly** if the tool supports multi-panel/sprite-sheet
  output, then verify the result is *exactly* `cols × frame_width` wide and
  `rows × frame_height` tall before treating it as the deliverable — AI sprite-sheet
  output frequently comes back a few pixels off from a clean grid, which breaks the
  slicing in §2 rule 3.

A reusable prompt skeleton for a single panel, filling in the specifics from §4/§6:

> *Pixel art, [character/object name], [pose/action description], viewed from the side
> for a 2D platformer. [N]×[N]px canvas, transparent background, hard pixel edges, no
> anti-aliasing, no soft shadows. Color palette: [hex codes from §4.2 or your own].
> Consistent with a small sci-fi robot game's established art style — mechanical,
> geometric silhouettes, not organic/cute.*

Whatever the source, run every result through the §3 checklist before considering it
done — an AI tool is exactly as likely to hand back a wrong-sized or oddly-cropped image
as a rushed human first draft, and the failure mode (silent fallback or silent
misalignment) is identical either way.

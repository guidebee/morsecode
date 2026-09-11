# Mario Reskin — Junior Developer Task Guide

You're picking up the "Ampere's Run" reskin at Step R.4. Steps R.0–R.3 (player character,
world terrain, the first enemy and coin) are done and confirmed working on-device — read
[MARIO_RESKIN_EXECUTION.md](MARIO_RESKIN_EXECUTION.md) for that history and
[KENNEY_ALL_IN_ONE_INDEX.md](KENNEY_ALL_IN_ONE_INDEX.md) for the asset-sourcing research.
This doc is the hands-on "how do I actually do the next one" guide — smaller-grained than
either of those, written after two real false starts so you don't repeat them.

## 0. Before you touch anything

1. **You're on the `reskin-copilot` branch.** Confirm with `git status` — don't create new art on
   `main`.
2. **Every generated asset lives in exactly one place**: source scripts in
   `docs/assets/mario-sprites/ampere-staging/`, their output in
   `docs/assets/mario-sprites/reskin-source/`. The packer (`tools/mario-atlas-packer/`)
   reads `reskin-source/` first per-asset, falling back to the original Nintendo art for
   anything you haven't replaced yet — this is *why* you can do this one asset at a time
   instead of needing all ~130 done before testing anything.
3. **`ART_SCALE` stays `1`, permanently.** This was a deliberate decision (see
   `MARIO_RESKIN_EXECUTION.md`'s Step R.0 update) to keep the process simple. Never touch
   `MarioConfiguration.ART_SCALE`. All new art is native 32px/tile.
4. **`import sprite_tools`** in every new build script — don't re-write crop/scale/tint
   logic inline. See §2.

## 1. The Golden Rule

**Never trust a pixel coordinate you haven't verified.** Every real bug in this reskin so
far — not most, *every one* — came from using a crop coordinate, a frame width, or a
composite order that was assumed or guessed instead of checked against the actual file.
Verifying takes 30 extra seconds per asset. Un-shipping a bad asset after it's already in
five places takes an hour. This is the whole guide in one sentence; everything else below
is how to actually do it.

### Mistakes Hall of Fame — read these before you write your first script

| # | What happened | Root cause | The fix, generalized |
|---|---|---|---|
| 1 | The Mushroom power-up rendered as unrecognizable tiny green dots | Crop coordinates were *guessed* from a written description of the source image ("approximate positions based on doc description"), never opened and checked | Always crop, then check `.getbbox()` isn't empty *and* looks like a reasonable size before using the result. `sprite_tools.verified_crop()` does this for you — use it. |
| 2 | The Charge coil power-up looked like a slice of wall texture, not an item | The source (`gate.png`) is a *seamlessly repeating* pillar pattern (its content touches all 4 edges of the canvas, confirmed via column alpha-sums) — slicing an arbitrary window out of an infinite pattern never looks like a discrete icon | Before using any source image as an icon, check `sprite_tools.is_tileable_texture()`. If it's tileable, you need a deliberately-bounded sub-region (e.g. one repeat unit), not an arbitrary crop. |
| 3 | The Charge coil also had a translucent blue haze filling its *background* | `Image.alpha_composite(glow, coil)` — arguments backwards. The **first** argument to `alpha_composite` is the base that shows through wherever the second is transparent; passing the glow rectangle as the base tinted the transparent area around the coil, not the coil itself | Use `sprite_tools.glow_pulse()` — it only ever touches the frame's own opaque pixels. If you're compositing manually, always ask "what shows through the *transparent* parts of my overlay?" before you decide the argument order. |
| 4 | Scuttler (the first enemy) rendered too small on its tile | Source content was copied 1:1 (recolored only) from a frame whose actual content was only ~15×14px inside a 32×32 canvas — nothing scaled it up | Use `sprite_tools.place_content()` — it crops to the *real* content first, then scales that content to fill most of the target cell. Never assume a source frame's canvas size tells you anything about how big the actual drawing inside it is. |
| 5 | Ampere's Big/Fire power states didn't visually grow — only changed color | The scaling formula used `min(width_ratio, height_ratio)` to avoid distortion — but that caps at 1.0× whenever the source is already as wide as the target, so a square 32×32 source going into a 32×64 (twice as tall) cell never actually got bigger | Same fix as #4 — `place_content()`'s scale is `min((fill*cell_w)/w, (fill*cell_h)/h)`, which always grows to fill the *limiting* dimension rather than capping at "no distortion, no matter how small the result." |
| 6 | Roller (the turtle enemy)'s frame dimensions didn't match what the game expected (128×48 vs 128×32) | The build script assumed frame width/height from the *source filename's* labeling convention instead of checking what the **consuming Java class** actually expects | **Always grep the actor class's `super(...)` or `.split(...)` call before assuming a frame size.** `EnemyTurtle.java`'s own `super(region, tileSize, (tileSize*3)/2, ...)` says the frame is 32 wide × 48 **tall** — taller than a tile — which you cannot know from the filename or a general convention. This is the single most common way to get a new asset's dimensions wrong. |

## 2. The shared tool module — use it, don't reinvent it

`docs/assets/mario-sprites/ampere-staging/sprite_tools.py` has five functions, each
directly fixing one of the bugs above: `verified_crop`, `is_tileable_texture`,
`place_content`, `tint`, `glow_pulse`, `mirror`, `build_sheet`. Read its docstrings — each
one names the real bug it fixes. Import it at the top of every new build script:

```python
import sys
sys.path.insert(0, "C:/workspace/morsecode/docs/assets/mario-sprites/ampere-staging")
from sprite_tools import verified_crop, place_content, tint, glow_pulse, mirror, build_sheet, is_tileable_texture
```

If you find yourself writing a `.crop(...)` or `.resize(...)` call that isn't going
through one of these, stop and ask whether it should be a new shared function instead of
one-off inline logic — that's how bug #1-#6 above happened in the first place (copy-pasted
inline crop math, slightly different each time, verified none of the times).

## 3. The recipe — follow this for every single asset, no exceptions

1. **Find the `AssetSpec`** in `tools/mario-atlas-packer/src/PackMarioAtlas.java` — the
   region name, exact filename (case-sensitive!), and `cols × rows`.
2. **Find the consuming Java class** — `grep -rn "region(\"<name>\")"` under
   `app/src/main/java/.../activity/mario/`. Read its `super(...)` call to get the *real*
   frame width/height (see Mistake #6) and read any `setFrame(...)`/frame-index logic to
   know which column/row means what (left vs right facing, which theme, etc.) — don't
   assume from another asset's convention.
3. **Pick a source.** Check `KENNEY_ALL_IN_ONE_INDEX.md` first — it's already surveyed
   Robot Master Series, Robot Platform Pack, and the Kenney bundle. If nothing fits, that's
   a real gap — flag it (see §6) rather than forcing a bad match.
4. **Check whether the source is a discrete icon or a tileable texture** (Mistake #2) —
   `is_tileable_texture()` before you crop anything from a new source file for the first
   time.
5. **Crop with `verified_crop()`, place with `place_content()`.** Watch the printed
   `content_bbox` — if it looks tiny relative to your crop box, or `None`, stop and look at
   the actual source image before proceeding.
6. **Save to `docs/assets/mario-sprites/reskin-source/<ExactAssetSpecFilename>`.**
7. **Actually view the generated PNG** (the Read tool renders images) before packing.
   Every bug in the Hall of Fame would have been caught at this step alone.
8. **Pack**: `bash tools/mario-atlas-packer/pack.sh` from the repo root. Check its output —
   `N / 122 assets sourced from reskin overlay` should go *up* by however many files you
   added, and the page/region counts (`"X regions across Y page(s)"`) shouldn't jump
   unexpectedly (a sudden new page usually means a size mismatch somewhere).
9. **Compile**: `./gradlew :app:compileDebugJavaWithJavac` — must say `BUILD SUCCESSFUL`.
10. **On-device check** — hand off to whoever's got the test device (or your own, if you
    have one) before marking a task done. Steps 7-9 catch dimension/pipeline bugs; only
    actually playing catches "this doesn't read as the right thing" bugs.
11. **Update the checklist** in `MARIO_RESKIN_EXECUTION.md`'s Step R.4 section — check the
    box, note what source you used and any judgment calls you made, the same way every
    prior step in that doc is documented. Future-you (or the next junior dev) will thank
    you.

## 4. Ready-to-execute tasks (do these first, in order)

Each task below has already had its source verified and its target Java class checked —
you're applying the recipe, not researching from scratch. Still verify the crops yourself
when you write the script; "someone already looked at this" is not the same as "you don't
need to check."

### Task 1 — Iron (the "used up" block)

- **AssetSpec**: `iron` → `Iron.png`, 4 cols × 1 row, 32×32 each.
- **Consuming class**: `actors/bricks/Iron.java` — frame 0 = Sea (unused in World 1), 1 =
  Ground, 2 = UnderGround, 3 = Castle. Simple static block, no animation.
- **Source suggestion**: Pixel Platformer Industrial Expansion or Tiny Dungeon (already
  used for terrain in R.3) — reuse the *same* tile family so Iron reads as "part of this
  world" rather than a mismatched fifth material. Recolor per the same 4-theme palette
  already established in `build_terrain_and_common.py`'s `THEMES` dict.
- **Test**: view the 128×32 sheet, confirm 4 visually-similar-but-differently-toned blocks.

### Task 2 — QuestionMark / Bank (the "?" block)

- **AssetSpec**: `question_mark` → `QuestionMark.png` (3×1) and `question_mark_grey` →
  `QuestionMarkGrey.png` (3×1) — two separate files, same frame convention.
- **Consuming class**: `actors/bricks/QuestionMark.java` — `IDLE_FRAMES = {0,0,1,2,1,0}`,
  a 3-frame bob loop (rest → mid → peak). `question_mark_grey` is used on
  UnderGround/Castle levels, `question_mark` everywhere else (Ground/Sea) — confirm `Bank`
  shares this by grepping it yourself (it wasn't fully traced in this guide).
- **Source suggestion**: keep the procedural beveled-block-with-glyph style already used
  for this in an earlier (abandoned) attempt — it actually looked fine, just needs
  redoing on a clean base. Or pull a "switch/button block" tile from Industrial Expansion
  if one fits better.
- **Test**: 4 files total (2 regions × the animation frames), each 3-frame sheet should
  show a subtle bounce/highlight change across its 3 frames, not 3 identical copies.

### Task 3 — Plater / Helmet family

- **AssetSpec**: `helmet`/`helmet_dark`/`helmet_white` (4×1 each, 32×32 walk cycle) +
  `helmet_shell`/`helmet_shell_dark`/`helmet_shell_white` (1×1 each, static).
- **Consuming class**: `actors/enemies/Helmet.java` — **same convention as `EnemyMashroom`
  (Scuttler)**: cols 0-1 = left-facing walk pair, cols 2-3 = right-facing walk pair, all
  32×32 (NOT tall like the turtle — confirm this yourself by reading `Helmet.java`'s own
  `super(...)` call, don't just trust this table).
- **Source suggestion**: Robot Master Series has a 3rd enemy type not yet used (enemy3,
  if it exists — check the pack's folder listing) — per the R.0 plan, this is meant to
  cover Plater specifically. If enemy3 doesn't exist or doesn't fit, this is a real gap;
  don't force enemy1/enemy2 art to double up without flagging it.
- **Test**: `helmet_shell` should look like a plausible "retracted" version of `helmet`'s
  walking pose, the same relationship Scuttler doesn't need (it has no shell) but Roller
  and Plater both do.

### Task 4 — The Warden (mini-boss)

- **AssetSpec**: `boss` → `Boss.png` (3×2 = 6 frames) + `boss_fire` → `BossFire.png`
  (2×1).
- **Consuming class**: `actors/enemies/Boss.java` — grep it yourself for the exact
  frame-index meaning (a prior research pass noted "0/1 = look-left idle, 4/5 = look-right
  idle, 2 = spitting-fire pose" but **this was not re-verified for this guide** — confirm
  before building, per the Golden Rule).
- **Source**: Robot Master Series' mini-boss (`other/` folder or a dedicated
  `miniboss`-named folder — check the pack root) is the R.0-decided source for this
  specifically. This is the highest-value single asset left in tier 3/4 — get it right,
  don't rush it. A previous unreviewed attempt at this asset produced corrupted vertical
  slivers instead of a character — a strong sign that source's frame layout wasn't
  checked before slicing. Check the mini-boss sheet's own actual dimensions and frame
  count before writing any crop code.
- **Test**: this is a boss — actually cycle through all 6 `boss` frames visually (not just
  frame 0) and confirm each one looks like a coherent pose, not a partial/misaligned slice.

### Task 5 — Spare chassis (1-Up)

- **AssetSpec**: `one_up` → `1UP.png`, 2×1.
- **Consuming class**: `actors/items/Life.java` (grep to confirm the exact frame usage).
- **Source**: per `KENNEY_ALL_IN_ONE_INDEX.md` §2, no clean thematic match was found
  anywhere scanned — Robot Platform Pack's heart icon is the documented pragmatic
  fallback. This is a good one to hand-pixel instead if you have any spare art time: a
  small chassis/head icon fits the identity better than a heart. Either is acceptable;
  don't spend more than 20 minutes searching further, per the existing research.

## 5. The rest of tier-3/4 — same recipe, less hand-holding

Everything below still needs the full recipe (§3) applied — find the `AssetSpec`, read
the consuming class, pick and verify a source, build, pack, compile, test, document. Use
this table as your checklist, filling in "Source used" and checking off "Done" yourself
as you go (mirror this into `MARIO_RESKIN_EXECUTION.md` too).

| Region(s) | File(s) | Frames | Notes |
|---|---|---|---|
| `monkey` | Monkey.png | 3×2 | Check `Monkey.java` for the grid's meaning — 6 cells is unusual, don't assume it's 2 poses × 3 themes without checking. |
| `son_of_a_buitch`, `spikey_egg`, `spikey` | 2×1, 2×1, 4×1 | Low placement-count enemies (§16.4 tier 4) — good AI-gen candidates per the original plan if no pack source fits, per `MARIO_RESKIN_EXECUTION.md`'s own R.4 guidance. |
| `fish_grey`, `fish_red`, `octopussy` | 2×1, 2×1, 2×1 | Sea-adjacent enemies — check `KENNEY_ALL_IN_ONE_INDEX.md`'s Roguelike Dungeon Pack / other Sea-adjacent finds first. |
| `flying_turtle`, `flying_turtle_dark`, `flying_turtle_patrol`, `enemy_turtle_patrol` | 4×1 each | Can likely reuse Roller's already-built art (Task in R.4 Phase 1) with a small wing/hover addition rather than sourcing something new — cheaper than a whole new character. |
| `turtle_shell_red`, `turtle_shell_flip`, `turtle_shell_flip_dark`, `turtle_shell_flip_red` | 1×1 each | Per `TurtleShell.java`'s own doc, **not reached by World 1's actual level data** — lowest priority in this whole list, do these last. |
| `fire_ball` | FireBall.png, 4×1 | Per §16.4 tier 3, this is meant to reuse the player's own fireball projectile art conceptually — check whether it can literally be the same source frames as whatever the Fire-state's projectile already uses, rather than new art. |
| `lava`, `lava_ball`, `water` | 1×1, 2×1, 1×1 | Simple background/hazard textures — Kenney's "Simple Space" or similar could supply a lava/hazard-glow look; check the index doc. |
| `axe` | Axe.png, 4×1 | The castle-ending bridge-cut lever — no source match found in the original scan; per the index doc's own recommendation, a quick custom "console/switch" icon is cheaper than continued searching. |
| `wall`, `rocket_launcher`, `bouncer`, `spring` | various | `bouncer`/`spring` can reuse the Charge-coil coil motif (already built, Task-1-adjacent) — a coil doubles as a spring visually, no new source needed. |
| `wooden_bridge`, `white_line`, `chain`, `rope`, `bridge_blocks` | 1×1 each | Simple connective/structural pieces — procedural (like the R.3 terrain tiles) is reasonable here, these don't need hand-picked source art. |
| `pump`, `pump_top` (+ `_castle`/`_sea` variants) | 1×1 each | Per `KENNEY_ALL_IN_ONE_INDEX.md` §3, RTS Sci-fi's pipe/conduit tiles are the documented candidate — that pack's tilesheet needs its own grid-index mapping pass (same technique as Step R.3's terrain, not yet done for this pack). |
| `plant`, `plant_dark` | 2×1 each | Check `PiranhaPlant.java` for usage — likely another AI-gen candidate per the original plan (no pack match found in the research pass). |
| `hori_image` | HoriImage.png, 2×1 | Check `MarioTileRegistry.java`'s `HoriImage` handler (already read once, during Step R.1 — "two 2-tile pieces side by side") for the exact composition. |
| `explosion` | Explosion.png, 3×1 | Robot Master Series' own `other/explode-Sheet[64height64wide].png` is documented, unused raw material — reuse it. |
| `bw_hammer` | CloudsNight/Hammer.png, 4×1 | The boss's thrown hammer — reuse whatever `boss`'s own weapon/prop art looks like (Task 4) for visual consistency, recolored for the CloudsNight look. |
| `small_castle`, `big_castle`, `tree`, `lift` | various | Scenery — `tree` per the index doc's own recommendation should be reframed as an antenna/pylon (no style-matched tree source exists anywhere scanned) rather than continuing to search. |
| `mountain`, `clouds`, `cloudsnight`, `fence`, `fence2`, `sea_background` | 1×1 each | Parallax backdrops — `KENNEY_ALL_IN_ONE_INDEX.md` §4 has concrete leads (Kenney "Background Elements," Robot Master Series' own background sections). |
| `bw_tree`, `bw_small_castle`, `bw_big_castle`, `bw_bouncer`, `bw_rocket_launcher`, `stone_clowd` | various | CloudsNight/Clowd-specific variants — likely just a desaturated recolor of whatever the normal-theme version ends up being, same technique as `bw_stone` in R.3. |
| `flag`, `flag_top`, `flag_sphere`, `flag_fence`, `flag_sphere_fence`, `flag_win`, `another_castle_message`, `quest_complete` | 1×1 each | End-of-level scenery/UI — lower priority, low placement count each. |
| `bubble` | Bubble.png, 4×1 | Sea-only swim particle — simple, low priority. |
| `font`, `info`, `info2` | — | **Check before doing any work here**: an earlier pass found `font`'s atlas region appears to be dead code — the actual score/lives HUD text goes through the shared engine `uiSkin()` bitmap font instead (see `KENNEY_ALL_IN_ONE_INDEX.md` §8 for the full finding, including why that shared font can't just be overwritten in place). Confirm `info`/`info2` are still reachable before including them in any art pass at all. |

## 6. When to stop and ask instead of guessing

- **A source pack has no plausible match after ~20 minutes of looking** — don't force a
  bad fit or spend an hour searching further. Flag it in the doc as an open gap (same
  pattern as `one_up`/Spare chassis and `axe` above) and move to the next task.
- **A consuming Java class's frame convention is genuinely ambiguous** even after reading
  its `super()`/`setFrame()` calls — ask rather than guess. Getting this wrong is Mistake
  #6, and it's the hardest one to catch visually (a wrong frame *count* often still
  "looks like something," it's just wrong).
- **You're about to touch anything outside `docs/assets/mario-sprites/` or
  `tools/mario-atlas-packer/`** — that's real engine/game code, not an asset swap. Ask
  first; R.1's `ART_SCALE` work and the reskin-overlay mechanism are the only two places
  code changed during this whole reskin so far, and both were deliberate, reviewed
  changes, not something to casually extend.

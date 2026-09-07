# Reskin Plan: Removing Nintendo IP + Going High-Resolution

**Why this exists:** the current port's assets are the original Super Mario Bros.'
actual sprites, music, and character/world identity (Mario, Bowser, Peach, Goombas,
Koopas, the Mushroom Kingdom) — not just "inspired by," but the same pixel art and audio
this session has been reading directly out of `C:\workspace\Mario\SandBox`. That's a
copyright/trademark problem the moment this app is distributed beyond this dev machine.
This plan replaces every infringing asset and user-facing name with original ones, **and**
upgrades the art from the original's native 32px-per-tile pixel density to a genuinely
higher-resolution look, while keeping every mechanic, level layout, and line of gameplay
code exactly as ported. This is engineering guidance, not legal advice — get an actual
IP-law review before any public release regardless of following this plan.

**Before touching asset code**, see [MARIO_GAME_MECHANICS.md §13](MARIO_GAME_MECHANICS.md#13-sprite-sheets-and-the-atlas-system)
for how the atlas/sprite-sheet system actually works today (theme atlases, the
`cols × rows` frame-grid convention, magenta masking, the per-cell vertical flip) — that
section is the technical reference this plan's §3/§4 asset-authoring work builds on.

**Ready to actually start sourcing/drawing assets?** See
[MARIO_RESKIN_EXECUTION.md](MARIO_RESKIN_EXECUTION.md) — this plan's actionable
companion: real, license-checked resource links (asset packs, fonts, audio, tools) for
this plan's §3 sourcing strategy, plus a concrete, ordered step-by-step task list for §4's
execution. This document stays the decisions/rationale record; that one is the checklist.

**Revision note (2026-09-07):** this revision changes two things versus the first pass:
(1) it folds in the high-resolution art upgrade the team wants, which — contrary to
this plan's original §4 — is **not** a pure content swap under the current architecture
(see new §4.1); (2) it **reverses** the original ordering recommendation (§5) — reskin
now runs as a single pass *after* [MARIO_PORT_PLAN_PHASE2.md](MARIO_PORT_PLAN_PHASE2.md)
lands, not folded into its Step P2.1. See §5 for why.

**What does *not* need to change:** game *mechanics* (jump/run/grow/shrink physics,
collision rules, power-up effects, enemy AI patterns, level geometry/tile layouts) are
generally not copyrightable subject matter — copyright protects the specific *expression*
(art, audio, character likeness, story text), not the abstract rules. This plan leaves
`Player`/`Enemy`/collision/level-data code untouched, **with one addition**: the small
`ART_SCALE` decoupling change in §4.1, needed only because of the resolution upgrade, not
the IP reskin itself. The one caveat on level data: recreating Nintendo's *exact* official
level layouts is a greyer area than art/audio (closer to "expression" than pure rules) —
flagged in §6 as a risk to get a legal opinion on specifically, since this plan otherwise
keeps layouts as-is per the request.

## 1. Inventory: what has to be replaced

### 1.1 Visual assets
Every PNG currently sourced from `C:\workspace\Mario\SandBox` via
`tools/mario-atlas-packer/src/PackMarioAtlas.java`'s `ASSETS` table — ~70 entries for the
shipped World-1 port today, growing to ~130 once
[MARIO_PORT_PLAN_PHASE2.md](MARIO_PORT_PLAN_PHASE2.md) is done. Every one of these is
either literally ripped SMB art or a redraw closely tracing it (the file names alone —
`Mashroom.png`, `TurtelShell.png`, `BigCastle.png` — say as much). No exceptions; there
is no "this one's probably fine" tile in that list. **Per §5, this plan now sources
replacement art for the full ~130-entry list in one pass, once Phase 2 has actually
built every entry it needs** — not ~70 now and ~61 more later.

### 1.2 Audio assets
All `SOUND_EFFECTS`/`MUSIC_TRACKS` in `MarioResourceManager.java` (`smb_jump-small`,
`smb_mariodie`, `smb_powerup`, `smb_1-up`, `smb_stomp`, the `Ground`/`UnderGround`/
`Castle`/`Star` music tracks, etc. — the `smb_` prefix is itself an admission). All of
it is either the actual SMB audio or a note-for-note MIDI/cover rendition, which is
still an infringing derivative of the original composition.

### 1.3 User-facing text/branding
Small footprint today, all from this session's own Step 8 work — no accumulated legacy
text debt:
- `app/src/main/res/values/strings.xml:52` — `<string name="mario">Mario</string>`
  (the home-screen tile label the user actually sees).
- `AndroidManifest.xml:48` — `android:label="Mario"` (the launcher/activity label).
- `MarioMenuScreen.java:60` — hardcoded `"SUPER MARIO BROS"` title text (the exact
  trademarked title string, verbatim).
- No other character names ("Bowser", "Peach", "Goomba", "Koopa", "Mushroom Kingdom")
  currently appear in any user-visible string — they only exist as internal Java
  class/asset-key names (see §1.4), which users never see.
- Phase 2 will add more: world names in the expanded `MarioMenuScreen` (§2.3 of the
  phase-2 doc), and possibly a `Princess`/end-game message string (World 8 finale).
  Same rule applies to those when they land: not currently user-visible, not a blocker,
  fold into this plan's §4 pass whenever they exist.

### 1.4 What does *not* need to change: internal code identifiers
`MarioGameActivity`, `MarioWorld`, `Player`, `Boss`, region/sound keys like
`"mashroom"`/`"smb_mariodie"`, package name `activity.mario` — none of this is shipped
to or visible to an end user, so none of it carries trademark/copyright exposure.
**Recommendation: leave it alone.** Renaming ~150 classes and every asset-key string
across the codebase is a large, purely-cosmetic, high-risk-of-breaking-something
refactor that buys zero legal protection. Revisit only if/when the team wants it for
its own sake, as an explicitly separate, low-priority cleanup — not part of this plan.

## 2. New identity (starting proposal — a creative decision for the team to adjust, not a fixed spec)

A concrete strawman, so the asset-sourcing work in §3 has a brief to work from:

| Original | Replacement concept |
|---|---|
| Mario (small/big/fire) | **"Ampere"** — a small robot/scout character. Power states: base, "reinforced" (bigger chassis), "charged" (shoots energy bolts instead of fireballs) |
| Mushroom (grow) | **Battery cell** power-up |
| Fire Flower | **Charge coil** power-up |
| Star (invincibility) | **Overclock chip** — same color-cycling-while-invincible effect, different sprite |
| Coin | **Bolt/gear** |
| 1-Up (Life) | **Spare chassis** |
| Goomba-analog (`EnemyMashroom`) | **"Scuttler"** — small skittering bot |
| Koopa-analog (`EnemyTurtle`/shell) | **"Roller"** — retreats into a rollable shell when stomped, same shell-kick mechanic |
| Buzzy-Beetle-analog (`Helmet`, phase 2) | **"Plater"** — armored variant, same shell-kick mechanic |
| Bowser-analog (`Boss`, phase 2) | **"The Warden"** — a larger sentry-bot boss, same patrol/throw/6-hit-or-instant-kill pattern |
| Princess (phase 2) | **"the Signal Core"** — an objective to reach/reactivate, sidesteps needing a rescued-character design entirely |
| World themes (Ground/UnderGround/Castle/Sea/Night) | **Surface / Substrate (caves-as-circuitry) / Fortress / Flooded Sector / Night Shift** |
| "SUPER MARIO BROS" title | Working title: **"Ampere's Run"** or similar — team's call |

This leans mechanical/sci-fi partly because it sits next to a Morse-code training app
(radio/signal theming could pay off later if Step 9's Morse hook is ever revisited) —
not a requirement, just a coherent starting point. Any consistent, sufficiently
different silhouette/theme works; the only hard constraint is **don't reuse Nintendo's
specific character silhouettes/color schemes** (no red-overalls plumber, no
spiky-shelled dragon-turtle, no mushroom-with-eyebrows) — a reskin that's just a
recolored trace of the same character design doesn't actually solve the problem.

**Resolution note:** since every one of these is being (re)authored from scratch anyway
(§5), author them directly at the §4.1 target resolution — there's no "draw it once at
low-res, upscale later" step to sequence in; low-res-then-upscale is strictly wasted
effort once the decision to go high-res is locked in before art production starts.

## 3. Asset sourcing strategy

Hand-drawing ~130 sprite sheets and composing ~30 audio tracks/SFX from scratch is a
large, specialized effort this plan shouldn't assume happens for free. Options, cheapest
first:

1. **CC0 ("no rights reserved") asset packs** — recommended primary path *for the
   original resolution tier*, with a caveat: Kenney.nl and OpenGameArt.org both publish
   platformer character/enemy/tile/UI/SFX packs, CC0, free, no attribution/royalty. Most
   packs of this kind ship at modest pixel-art resolution (commonly 16–48px tiles) — some
   Kenney packs do include higher-resolution variants, but **verify actual per-tile pixel
   dimensions against §4.1's target before committing to a pack**, not after; a pack
   that's the wrong native resolution means upscaling low-detail source art, which looks
   soft/blurry rather than genuinely high-resolution (defeats the point of this upgrade).
2. **Commissioned original art/audio** — highest quality and most tailored to both the
   identity in §2 and the resolution target in §4.1; costs money and calendar time.
   More likely to be needed than before, precisely *because* of the resolution bar — a
   from-scratch commission can hit an exact target pixel density where a CC0 pack search
   might not.
3. **In-house original art** — if anyone on the team draws pixel/digital art, use this
   plan's §4.1 spec sheet (exact dimensions/frame counts per asset **at the new target
   resolution**) as the brief.
4. **AI-generated art/audio** — possible, but verify the specific tool's license terms
   permit commercial redistribution before relying on it, and expect to hand-fix
   pixel-grid consistency (frame-strip alignment, transparent backgrounds, consistent
   palette, and — at a genuinely high pixel density — consistent line weight/detail
   level across a multi-frame strip) since generative tools aren't naturally consistent
   across a strip the way a human artist or a purpose-built asset pack is. This gets
   *harder*, not easier, at higher resolution: more surface area per frame for a
   generative tool's per-frame inconsistency to show up in.

**Recommendation: start with (1)**, but budget for (2)/(3) covering more of the list than
the original plan assumed, specifically *because* of the resolution requirement — don't
assume CC0 alone closes out all ~130 entries at the target density without checking.

## 4. Technical execution

### 4.1 Resolution architecture: this is not a pure content swap

The original version of this plan claimed swapping art was purely mechanical "if
replacement art is authored to the same frame-grid shape the existing slicing code
expects." That claim only holds at the *same* pixel density as today. Going
higher-resolution breaks it, because the current code conflates two things that happen
to be equal today only by coincidence: **world-space size** (used for physics/collision,
tuned by feel, must never change) and **texture-region pixel size** (used to slice
frames out of the atlas, this is exactly what's supposed to change). Concretely, today:

- `PlayerPowerState` (`SMALL(32,32,"player")`, `BIG(32,64,"big_player")`,
  `FIRE(32,64,"fire_player")`) uses its `width`/`height` for **both**
  `region.split(state.width, state.height)` (atlas slicing) **and**
  `Player`'s actual world collision box (`super(x, y, PlayerPowerState.SMALL.width, ...)`,
  `setSize(newState.width, newState.height)`, the crouch/grow Y-offset math).
- `Scenery.java:21` sizes an actor's world-space width/height **directly** from
  `region.getRegionWidth()`/`getRegionHeight()` — whatever pixel size the atlas region
  happens to be *is* that actor's hitbox size, no scale factor involved.
- `Lift.java` tiles a platform's texture at `region.getRegionWidth()` "native" pixel
  increments across its world-space `getWidth()`.
- `MarioConfiguration.TILE_SIZE = 32` (world units) and the `PackMarioAtlas.ASSETS`
  table's `cols`/`rows` per entry are the same 32px-native grid throughout.

Ship replacement art at, say, 2×/4× the pixel density per tile without touching any of
the above and every actor's hitbox silently inflates 2×/4× against the *unchanged*
32-unit tile grid — collision breaks immediately (Mario clips into ceilings, stands
half-inside the ground, enemies' hitboxes stop lining up with their sprites).

**The tempting wrong fix:** bump `MarioConfiguration.TILE_SIZE` itself (e.g. 32→64 or
32→128) so "1 texture px = 1 world unit" stays true at the new density. This looks clean
because `VIEWPORT_WIDTH`/`VIEWPORT_HEIGHT` already derive from `TILE_SIZE` (`20 *
TILE_SIZE`) and would auto-scale. **It's still wrong**: `Player.java`'s physics constants
(`MAX_SPEED = 60f`, `GRAVITY_STEP = 0.42f`, `JUMP_BASE = -11f`, etc.) are hardcoded
absolute world-units-per-frame numbers ported 1:1 from the original desktop game's feel —
none of them are expressed as a function of `TILE_SIZE`. Doubling `TILE_SIZE` without
independently rescaling every one of those constants changes jump-arc-height-vs-tile-
height and run-speed-vs-tile-width ratios, i.e. changes how the game *feels* to play,
which this plan (like the original) commits to leaving untouched.

**The correct fix: decouple art pixel density from world units with one new constant.**

- Add `MarioConfiguration.ART_SCALE` (e.g. `2` or `4` — pick the value once, see below).
  `TILE_SIZE` and every physics constant stay exactly as they are today — zero gameplay
  change.
- New art is authored/packed at `TILE_SIZE * ART_SCALE` pixels per grid cell (e.g. at
  `ART_SCALE=2`, a 32-world-unit tile is backed by a 64×64px source image).
- Every call site that currently treats "texture region pixel size" as "world size"
  divides by `ART_SCALE` first:
  - `PlayerPowerState`: keep `width`/`height` as **world-space** values (32/32, 32/64 —
    unchanged) for the collision-box call sites; add separate `frameWidthPx`/
    `frameHeightPx` (= `width/height * ART_SCALE`) for the `region.split(...)` call
    sites. Two call sites in `Player.java` to touch (frame-splitting vs. collision-box
    sizing), not a rewrite.
  - `Scenery.java:21`: change to
    `super(x, y, region.getRegionWidth() / ART_SCALE, region.getRegionHeight() / ART_SCALE, true)`.
  - `Lift.java`: change its native-width tiling increment to
    `region.getRegionWidth() / ART_SCALE`.
  - `PackMarioAtlas.ASSETS`' `cols`/`rows` stay describing the *frame grid* (unchanged —
    a 4×7 player strip is still 4×7 frames, just each frame is now `ART_SCALE`× more
    pixels); no changes needed there beyond the source PNGs themselves being bigger.
- This is a small, mechanical, one-time change (three or four call sites plus one new
  constant), done **once**, before any high-res art is dropped in — not per-asset, and
  not touched again afterward. It's the only code change this whole reskin makes;
  everything else in this document really is a content-only swap, exactly as the
  original plan claimed.

**Picking `ART_SCALE`:** a product/creative call, but as a technical recommendation,
**start at `ART_SCALE=2`** (64px-per-tile source art) rather than 4×. Reasoning: pixel
*area* — and therefore atlas memory/APK size — scales with the *square* of this factor
(2×→4× the pixels to store/decode/upload, 4×→16×), so 2× already delivers a very
noticeable crispness jump on modern high-DPI phones over the original's blocky 32px
tiles, at a memory cost this app's low-end-device budget can absorb; 4× is the option to
reach for later, per-asset, only for something that's genuinely still soft at 2× up
close (the player sprite, HUD digits) — don't default the *entire* asset list to 4× and
pay for it everywhere.

### 4.2 Filtering: revisit `Nearest,Nearest`

`PackMarioAtlas` currently hardcodes `filter: Nearest,Nearest` in the emitted `.atlas`
file (crisp/blocky scaling, appropriate for the original's retro pixel art scaled up by
the viewport). Whether that's still right depends on the §2 art style decision:
- If the new identity stays a "pixel art, just more of it" look (blocky by design, just
  authored at `ART_SCALE`'s higher native grid) — keep `Nearest,Nearest`.
- If the new identity leans toward smoother/painterly/higher-detail art — switch to
  `Linear,Linear` (and generate mipmaps for when the camera zooms out via pinch-zoom,
  §CameraController — a level viewed fully zoomed-out minifies every texture, and
  `Linear` without mipmaps aliases/shimmers on minification) so curves and gradients
  don't look aliased.
Decide this alongside §2's art brief, not independently — it's an art-direction call
that happens to also be a one-line packer change.

### 4.3 Atlas page size and memory budget

`PackMarioAtlas.PAGE_SIZE = 2048` was sized for 32px-native assets. At `ART_SCALE=2`,
total packed pixel area per asset roughly quadruples; combined with
[MARIO_PORT_PLAN_PHASE2.md](MARIO_PORT_PLAN_PHASE2.md) §2.1's already-planned per-theme
atlas split (common + one atlas per theme, loaded/unloaded per level instead of one
monolithic growing atlas), this should still comfortably fit — the per-theme split was
already motivated by unbounded atlas growth, and now does double duty capping the
resolution upgrade's memory growth too. Concretely:
- Re-run the packer at the new resolution per theme and check actual page counts before
  assuming anything; the packer already reports page count on every run.
- Don't bump `PAGE_SIZE` past 2048 to "fit more" without checking `GL_MAX_TEXTURE_SIZE`
  on this app's actual minimum supported Android API/GPU — some low-end/older GPUs cap
  at 2048; 4096 is common on modern hardware but isn't guaranteed on the minimum-spec
  device this app targets. Prefer more 2048 pages over one giant page if it comes to that.
- Actually test on a low-end device (or an emulator profile matching this app's min-spec
  target) after the resolution upgrade, specifically for texture-upload memory pressure —
  this is a real, new risk the original 32px assets never had to worry about (see §6).

### 4.4 Content-swap execution (unchanged from the original plan otherwise)

- 4.4.1 Produce a full asset spec sheet: one row per current `AssetSpec` entry (name,
  **new** target dims at `ART_SCALE`, cols×rows, used-by) plus one row per every
  asset [MARIO_PORT_PLAN_PHASE2.md](MARIO_PORT_PLAN_PHASE2.md) actually ended up
  building (not the phase-2 doc's upfront *estimate* — the real, as-built list, since by
  the time this runs Phase 2 is finished per §5) — a direct export of the finished
  `PackMarioAtlas.ASSETS` table, not new research.
- 4.4.2 Apply the §4.1 `ART_SCALE` code change (one PR, no new art yet) — confirm World 1
  (and by then, all 8 worlds) still render/collide identically with the *existing*
  Nintendo-derived art re-packed at the same `ART_SCALE=1` behavior, i.e. prove the
  decoupling is behavior-neutral before any new art enters the picture. This isolates
  "did the code change break anything" from "does the new art look right" as two
  separate, separately-verifiable steps.
- 4.4.3 Source/produce replacement art+audio per §3, at the §4.1 target resolution,
  matching 4.4.1's frame-grid (cols×rows) exactly (resize/re-lay-out at authoring time,
  not by stretching a final low-res PNG up — that produces blurry "high resolution" in
  name only).
- 4.4.4 Point `PackMarioAtlas`'s `sourceDir` (currently hardcoded to
  `C:\workspace\Mario\SandBox`) at a new, original asset directory; re-run the packer
  (now theme-split per Phase 2's §2.1, at the new `ART_SCALE`).
- 4.4.5 Swap audio files under `assets/mario/audio/` (same keys, or renamed — low-risk
  either way, since call sites use the constant, not a hardcoded string, at each
  `sound(...)`/`music(...)` call).
- 4.4.6 Fix every user-facing string in §1.3 (`strings.xml`, `AndroidManifest.xml`,
  `MarioMenuScreen.java`'s title, plus whatever world/end-game strings Phase 2 added).
- 4.4.7 Full regression pass: does every region/sound key still resolve, does every
  actor's hitbox still line up with its (now higher-res) sprite, does nothing visually
  clip/misalign against the unchanged tile grid — this is the pass that actually
  exercises §4.1's decoupling under real content, across **all 8 worlds** (not just
  World 1, since this now runs after Phase 2 — see §5).

## 5. Ordering: Phase 2 first, reskin+high-res last, as one pass

**Revised recommendation (reversing the original version of this plan): build
[MARIO_PORT_PLAN_PHASE2.md](MARIO_PORT_PLAN_PHASE2.md) to completion first, entirely on
the existing Nintendo-derived placeholder art, then run this plan once, as a single
final content pass over the complete, as-built asset list.** This matches the user's
own instinct going into this revision. Reasoning:

- **Phase 2's biggest risks are gameplay risks, not asset risks** — water physics
  (flagged in that doc's own §6 as "the single biggest unknown"), the `Boss` state
  machine, two genuinely-unread mechanics (`SonOfABuitch`, `Spikey`). QA'ing those
  against recognizable original art (does this still look/feel like real Bowser/real
  Goomba behavior?) is strictly easier than QA'ing brand-new mechanics *and* a brand-new
  unfamiliar art identity at the same time — a mismatch between "does this play right"
  and "does this look right" is much harder to triage when both are moving at once.
- **The original ordering's stated reason for reskin-first no longer applies once
  reskin is scheduled as one pass over the finished list.** The prior version's concern
  was "~61 more infringing assets get pulled in and need replacing later anyway" — true
  only if reskin partially happens now and gets revisited. Committing up front to *one*
  reskin pass, after Phase 2's asset list is final, means every asset is sourced/authored
  exactly once, at the correct final dimensions, with zero rework risk from Phase 2
  discovering it needs a different frame count/size than an early guess assumed (this
  has already happened once — see the phase-2 doc's own §1.4, which found the "castle
  finale" needed fewer new mechanics than a first survey pass guessed; the same kind of
  correction is exactly as likely for asset specs written before the code exists).
- **§4.1's `ART_SCALE` decoupling is verified once, against the complete, final actor
  list.** Doing it mid-Phase-2 (the original plan's idea, folded into Step P2.1) means
  verifying the decoupling against World 1's actors, then verifying it again against
  every new actor type Phase 2 adds afterward as they land — strictly more regression
  passes than one pass at the very end against everything at once.
- **Every phase-2 vertical slice gets QA'd once, in its final visual form**, instead of
  once now (looking like Mario) and again later (after reskinning) — this part of the
  original reasoning was correct and still holds, it just argues for reskin running
  *after* all of Phase 2's slices exist, not before any of them do.

**The hard gate this requires:** while Phase 2 is in progress, this app must not go
beyond this dev machine / a closed internal test group — no public beta, no store
listing, no link shared outside the immediate dev team. The original Nintendo-derived
placeholder art is fine to keep building and testing against internally (that's what
"porting the game logic" has always meant, and remains fine per both documents), but it
is not fine to ship. Treat "reskin+high-res has landed" as a hard release-blocking gate,
tracked explicitly (e.g. a checklist item on whatever tracks this app's release
readiness), not an implicit assumption.

**Concretely:**
1. Run [MARIO_PORT_PLAN_PHASE2.md](MARIO_PORT_PLAN_PHASE2.md) Steps P2.0–P2.7 to
   completion, entirely against placeholder Nintendo-derived art, internal-only.
2. Then run this plan's §4: 4.4.1 (finalize the real asset spec sheet from the
   as-built game) → 4.4.2 (land the `ART_SCALE` decoupling, verified against the
   *existing* art first) → 4.4.3–4.4.6 (source and swap in all ~130+ assets + audio +
   strings, at the new resolution, in the new identity) → 4.4.7 (full 8-world
   regression pass).
3. Only after that regression pass is clean does the release gate in the paragraph
   above open.

`C:\workspace\Mario`'s source code remains fair game to read as a *behavior/mechanics
reference* throughout all of this (that's what porting the game logic has always
meant) — the line is: never copy its pixel/audio files into the shipped asset
directories, and never let this app reach a device/tester outside the dev team while
it's still wearing the original's art.

## 6. Risks

- **Distribution-gate discipline** (new, from §5's reordering) — the biggest new risk
  this reordering introduces: it's easy for "internal testing" to quietly become "sent
  the APK to a friend" or "posted a screenshot" while Phase 2 is mid-flight, at which
  point the original art has already left the dev machine. Make the gate explicit and
  checked (§5), don't rely on remembering not to share a build.
- **Texture memory on low-end devices** (new, from the resolution upgrade, §4.3) — going
  from 32px-native to `ART_SCALE`×-native art measurably increases GPU texture memory
  and upload time; this app never had to budget for that before. Test on real low/
  min-spec hardware, not just a dev workstation or high-end test phone, before assuming
  `ART_SCALE=2` (or higher) is safe everywhere this app targets.
- **`ART_SCALE` decoupling correctness** (new, §4.1) — this is now the one real code
  change in an otherwise content-only plan; verify it in isolation (4.4.2) against the
  *unchanged* original art before any new art lands, so a collision/hitbox regression is
  clearly attributable to the code change, not confused with "does the new art line up."
- **Level-layout copying** (§ intro's caveat) — this plan, per the request, keeps exact
  original level geometry. That's a greyer legal area than art/audio; if this app is
  headed for public distribution, get a specific legal opinion on that point rather than
  assuming "we changed the art so we're covered."
- **Trade dress, not just literal copying** — a reskin that keeps the same silhouette/
  color scheme/pose set as the original character (e.g., still a red-and-blue plumber,
  just re-rendered, just at a higher resolution) doesn't meaningfully reduce risk. §2's
  replacement designs need to actually look different, not just be redrawn or upscaled.
- **CC0 license verification** — "CC0" claims should be verified at the source (Kenney's
  own site states CC0 explicitly; don't assume a random redistribution mirror is
  accurate) before bulk-importing any pack, and per §3, verify the pack's *actual native
  resolution* matches §4.1's target before committing to it.
- **Scope creep back into infringement** — while Phase 2 is reading `C:\workspace\Mario`
  for new mechanics' behavior, it's easy to also glance at "well the art's right there"
  and reuse it directly in a placeholder that quietly never gets replaced. Keep the
  asset pipeline pointed only at the original placeholder source until §5's Step 2
  formally re-points it (§4.4.4) — the fact that reskin now happens later, not sooner,
  makes this specific risk larger than the original plan's version of it, precisely
  because there's more calendar time for a placeholder asset to be forgotten. Track the
  asset spec sheet (4.4.1) as a checklist, not just a reference document.

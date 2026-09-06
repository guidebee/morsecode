# Reskin Plan: Removing Nintendo IP While Keeping Game Mechanics

**Why this exists:** the current port's assets are the original Super Mario Bros.'
actual sprites, music, and character/world identity (Mario, Bowser, Peach, Goombas,
Koopas, the Mushroom Kingdom) — not just "inspired by," but the same pixel art and audio
this session has been reading directly out of `C:\workspace\Mario\SandBox`. That's a
copyright/trademark problem the moment this app is distributed beyond this dev machine.
This plan replaces every infringing asset and user-facing name with original ones while
keeping every mechanic, level layout, and line of gameplay code exactly as ported. This
is engineering guidance, not legal advice — get an actual IP-law review before any public
release regardless of following this plan.

**What does *not* need to change:** game *mechanics* (jump/run/grow/shrink physics,
collision rules, power-up effects, enemy AI patterns, level geometry/tile layouts) are
generally not copyrightable subject matter — copyright protects the specific *expression*
(art, audio, character likeness, story text), not the abstract rules. This plan leaves
`Player`/`Enemy`/collision/level-data code untouched. The one caveat: recreating
Nintendo's *exact* official level layouts is a greyer area than art/audio (closer to
"expression" than pure rules) — flagged in §5 as a risk to get a legal opinion on
specifically, since this plan otherwise keeps layouts as-is per the request.

## 1. Inventory: what has to be replaced

### 1.1 Visual assets
Every PNG currently sourced from `C:\workspace\Mario\SandBox` via
`tools/mario-atlas-packer/src/PackMarioAtlas.java`'s `ASSETS` table — currently ~70
entries for the shipped World-1 port, growing to ~130 once
[MARIO_PORT_PLAN_PHASE2.md](MARIO_PORT_PLAN_PHASE2.md) is done. Every one of these is
either literally ripped SMB art or a redraw closely tracing it (the file names alone —
`Mashroom.png`, `TurtelShell.png`, `BigCastle.png` — say as much). No exceptions; there
is no "this one's probably fine" tile in that list.

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

## 3. Asset sourcing strategy

Hand-drawing ~130 sprite sheets and composing ~30 audio tracks/SFX from scratch is a
large, specialized effort this plan shouldn't assume happens for free. Options, cheapest
first:

1. **CC0 ("no rights reserved") asset packs** — recommended primary path. Kenney.nl
   publishes exactly this kind of content (platformer character/enemy/tile sprite packs,
   UI packs, SFX packs), all CC0 — free, no attribution required, no royalty, and sized
   for exactly this genre. OpenGameArt.org has a large CC0/CC-BY platformer-asset
   category as a second source. This gets a fully licensed, ready-to-use asset base with
   zero cost and minimal legal review burden (CC0 = public domain equivalent).
2. **Commissioned original art/audio** — highest quality and most tailored to the
   identity in §2, but costs money and calendar time; appropriate once the team commits
   to a specific creative direction and wants it fully custom.
3. **In-house original art** — if anyone on the team draws pixel art, use this plan's
   §4 spec sheet (exact dimensions/frame counts per asset, taken directly from the
   existing `PackMarioAtlas.ASSETS`/audio tables) as the brief.
4. **AI-generated art/audio** — possible, but verify the specific tool's license terms
   permit commercial redistribution before relying on it, and expect to hand-fix
   pixel-grid consistency (frame-strip alignment, transparent backgrounds, consistent
   palette) since generative tools aren't naturally consistent across a multi-frame
   strip the way a human pixel artist or a purpose-built asset pack is.

**Recommendation: start with (1)** for the full base game (it can cover essentially the
entire current + phase-2 asset list), and reserve (2)/(3) only for anything that doesn't
have a good CC0 equivalent (e.g., a bespoke boss design) or once the team wants to invest
in a fully custom look later.

## 4. Technical execution: keep this a content swap, not a code change

The current architecture already makes this mostly mechanical, *if* replacement art is
authored to the same frame-grid shape the existing slicing code expects:

- `PlayerPowerState` hardcodes region name + pixel size per power state
  (`SMALL(32,32,"player")`, `BIG(32,64,"big_player")`, `FIRE(32,64,"fire_player")`) — new
  art must ship at the same 32×32 / 32×64 cell size, 4-cols×7-rows strip layout, or
  `PlayerPowerState`'s constants (and everywhere that assumes them) need updating too.
- Every other actor (`Brick`, `QuestionMark`, `EnemyTurtle`, ...) reads its art via
  `MarioResourceManager.region("<name>")`/`.themedRegion(...)`, sized by whatever
  `AssetSpec(name, sourcePath, cols, rows)` the atlas packer recorded — same rule: match
  the existing `cols`/`rows`/pixel-size per entry and the actual game code needs zero
  changes, only the art underneath the same name changes.
- Audio is a flat key→file lookup (`MarioResourceManager.sound("smb_mariodie")`) — new
  files just need to be dropped in under the same keys (or the keys renamed in one pass,
  since call sites use the constant, not a hardcoded string, at each `sound(...)`/
  `music(...)` call — low-risk rename if the team wants cleaner asset keys as part of
  this work, unlike the class-name renaming in §1.4 which isn't worth doing).

**Plan:**
- 4.1 Produce a full asset spec sheet: one row per current `AssetSpec` entry (name,
  source dims, cols×rows, used-by) plus one row per phase-2-planned asset — this is a
  direct export of `PackMarioAtlas.ASSETS` plus §1.2/§1.3 of
  [MARIO_PORT_PLAN_PHASE2.md](MARIO_PORT_PLAN_PHASE2.md)'s asset inventory, not new
  research.
- 4.2 Source/produce replacement art+audio per §3, matching 4.1's dimensions exactly
  (resize/re-lay-out at authoring time, not by stretching the final PNG).
- 4.3 Point `PackMarioAtlas`'s `sourceDir` (currently hardcoded to
  `C:\workspace\Mario\SandBox`) at a new, original asset directory; re-run the packer.
  This is also exactly where [MARIO_PORT_PLAN_PHASE2.md](MARIO_PORT_PLAN_PHASE2.md)'s
  §2.1 per-theme atlas split was already going to touch the packer — do both in the same
  pass (see §5's ordering recommendation).
- 4.4 Swap audio files under `assets/mario/audio/` (same keys, or renamed — team's call,
  low-risk either way per above).
- 4.5 Fix the three user-facing strings in §1.3 (`strings.xml`, `AndroidManifest.xml`,
  `MarioMenuScreen.java`) plus any new ones phase 2's `MarioMenuScreen` expansion adds
  (world names — use §2's theme names, not "World 1"-style if that's judged too close;
  realistically "World 1/2/3..." numbering alone is not a Nintendo-specific trademark,
  lower priority than the character/title names above).
- 4.6 Full regression pass: since this should be a pure content swap, testing is "does
  every region/sound key still resolve, does everything still render/play at the right
  size" — not a gameplay-logic QA pass, because no gameplay logic changed.

## 5. Ordering: reskin before Phase 2, merged into its Step P2.1

**Recommendation: do the reskin first, folded directly into
[MARIO_PORT_PLAN_PHASE2.md](MARIO_PORT_PLAN_PHASE2.md)'s Step P2.1 (asset pipeline
scale-out)**, not as a fully separate sequential phase before or after it. Reasoning:

- P2.1 already plans to rebuild the atlas packer (splitting one growing atlas into
  per-theme atlases). Reskinning also means rebuilding the atlas packer (pointing it at
  new source art). Doing both in one pass avoids touching `PackMarioAtlas`/
  `MarioResourceManager` twice for two different reasons on two different days.
- Phase 2 is about to source **~61 more PNGs and more audio** for all-new content
  (Boss, water creatures, Helmet, etc.). If the reskin happens *after* phase 2's content
  work, that's ~61 more infringing assets pulled into the repo and referenced by new
  code, all needing replacement later anyway — strictly more total work than reskinning
  first and building every phase-2 asset directly against the new identity from the
  start.
- Every phase-2 vertical slice (P2.0's castle finale onward) then gets QA'd once, in its
  final visual form, instead of once now (looking like Mario) and again later (after
  reskinning) — halving the number of "does this still work" passes across all of
  phase 2's steps.

**Concretely: insert this plan's work as the very first thing done under Phase 2, ahead
of even P2.0** (Phase 2's own "fix World 1's castle finale" step) — reskin the
already-shipped World-1 assets first, confirm World 1 still plays identically in its new
skin, *then* proceed into P2.0 onward building every new mechanic's assets directly in
the new identity. `C:\workspace\Mario`'s source code remains fair game to read as a
*behavior/mechanics reference* throughout all of this (that's what porting the game
logic has always meant) — the line is: never copy its pixel/audio files into the shipped
asset directories again, from this point forward.

## 6. Risks

- **Level-layout copying** (§ intro's caveat) — this plan, per the request, keeps exact
  original level geometry. That's a greyer legal area than art/audio; if this app is
  headed for public distribution, get a specific legal opinion on that point rather than
  assuming "we changed the art so we're covered."
- **Trade dress, not just literal copying** — a reskin that keeps the same silhouette/
  color scheme/pose set as the original character (e.g., still a red-and-blue plumber,
  just re-rendered) doesn't meaningfully reduce risk. §2's replacement designs need to
  actually look different, not just be redrawn.
- **CC0 license verification** — "CC0" claims should be verified at the source (Kenney's
  own site states CC0 explicitly; don't assume a random redistribution mirror is
  accurate) before bulk-importing any pack.
- **Scope creep back into infringement** — once phase 2 starts reading
  `C:\workspace\Mario` for new mechanics' behavior (§1.5 of the phase-2 plan's open
  items), it's easy to also glance at "well the art's right there" — keep the asset
  pipeline pointed only at the new source directory (§4.3) so this can't happen by
  accident.

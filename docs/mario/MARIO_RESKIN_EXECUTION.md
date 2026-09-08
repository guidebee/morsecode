# Mario Reskin Execution Plan — Resources & Step-by-Step Guide

This is the **actionable companion** to [MARIO_RESKIN_PLAN.md](MARIO_RESKIN_PLAN.md) (the
decisions/rationale document) and
[MARIO_GAME_MECHANICS.md §16](MARIO_GAME_MECHANICS.md#16-reskin-scope--priority) (the
exact scope numbers). Where those answer "what and why," this answers **"using what
resources, in what order, with what tools."** Everything below assumes you've read both —
it doesn't repeat their reasoning.

**Status:** research complete, execution not yet started. This document records real,
checked-today resources and license terms (web research done 2026-09), not assumptions —
but "checked today" isn't "checked forever": re-verify any license at its source
immediately before bulk-importing anything, per
[MARIO_RESKIN_PLAN.md §6](MARIO_RESKIN_PLAN.md)'s own standing risk note.

## 1. The resolution reality check (read this before picking any asset source)

[MARIO_RESKIN_PLAN.md §4.1](MARIO_RESKIN_PLAN.md#41-resolution-architecture-this-is-not-a-pure-content-swap)
targets `ART_SCALE=2` — 64px-per-tile source art. Checking actual CC0 packs against that
target (not assumed, fetched from Kenney's own pages) confirms §3's own warning was
correct to flag: **Kenney's platformer packs — the single most obvious "free CC0 game
art" source — are natively 18×18px** (`Pixel Platformer` pack) or similarly small across
their platformer line, roughly **3.5× below** the 64px target. Using them as-is means
either upscaling (soft, blurry — exactly what §4.1 says defeats the point) or accepting a
much lower `ART_SCALE`/pixel density than planned.

This changes the sourcing calculus versus the original plan's "start with CC0 packs"
default. Four real paths exist; **pick per asset category, not one blanket answer** (see
§4's per-category breakdown):

| Path | Resolution fit | License | Effort | Best for |
|---|---|---|---|---|
| **A. Original authoring at 64px** | Exact | Whatever the team chooses (own it outright) | Highest | The player character + the handful of highest-frequency assets (§4.4's priority-1 tier) — the assets every player looks at the most deserve custom work |
| **B. LPC-family assets** (Liberated Pixel Cup / "Universal LPC Spritesheet") | **Exact — natively 64×64**, confirmed at the source | **CC-BY-SA 3.0 / GPLv3 dual-licensed — NOT CC0.** Requires crediting every contributing artist; ShareAlike means a *modified/redistributed* derivative of LPC art must itself be released under CC-BY-SA. Needs an explicit legal/product decision, not a default pick. | Medium (recombine/edit existing 64px parts rather than draw from scratch) | A resolution-matched fallback for enemy/NPC silhouettes if original authoring can't cover everything, *if* the team accepts the attribution + ShareAlike terms for those specific derived assets |
| **C. AI-assisted generation** (e.g. Retro Diffusion, PixelLab) | Configurable — can target 64px directly | Commercial use included on paid tiers (verify the specific plan/tier before committing, and keep records — see §6) | Medium — still needs a human cleanup pass for frame-to-frame consistency across a multi-frame strip (worse at higher resolution, more surface area per frame to look inconsistent — this is [MARIO_RESKIN_PLAN.md §3](MARIO_RESKIN_PLAN.md) item 4's own warning, confirmed still true) | Fast first-draft exploration of the new character/enemy designs in §2 of that plan, backgrounds, one-off scenery — not a substitute for hand-finishing the base player animation |
| **D. Low-res CC0 pack as a layout reference only** | Mismatched (16–18px) | CC0 (verified) | Low-to-medium (redraw at target density using the reference for silhouette/pose timing, not upscale) | Cheap inspiration for pose timing (walk-cycle frame count, jump arc) on lower-priority assets, **never used as final pixels** |

**Recommendation:** Path A for the player + top reskin-priority tier
([MARIO_GAME_MECHANICS.md §16.4](MARIO_GAME_MECHANICS.md#164-reskin-priority-by-on-screen-frequency)),
Path C to accelerate first drafts across the long tail of enemies/scenery (with a human
pass to fix cross-frame consistency before it enters the atlas), Path D for free pose/timing
reference throughout. Treat Path B as a deliberate, documented exception per-asset, not a
default — get the ShareAlike implication signed off before using it on anything that ships.

**2026-09-08 update:** §7 below supersedes this recommendation for the player/enemy/boss
roster specifically — two free, commercial-use-permitted packs built around a robot
identity close that gap without needing Path A/B/C at all for those assets. Path A/B/C/D
above still applies as originally written for whatever §7 doesn't cover.

## 2. Curated resource shortlist

Every entry below was checked against its own source page during this research pass —
license column quotes what that page itself states, not a third-party mirror.

### 2.1 Pixel art authoring tools

| Tool | Cost | Notes |
|---|---|---|
| [Aseprite](https://www.aseprite.org/) | $19.99 one-time | Industry-standard pixel art editor — layers, onion-skinning, tags/animation timeline, and a scriptable CLI that can batch-export sprite-sheet strips + JSON metadata. Worth the cost for anyone doing sustained pixel-art authoring on this project. |
| [LibreSprite](https://libresprite.github.io/) | Free, open-source | A maintained fork of an older Aseprite version — same core workflow (layers, frames, onion-skin), free. The practical default if the $20 license isn't approved yet. |
| [Piskel](https://www.piskelapp.com/) | Free, browser-based | Zero-install, good for quick one-off edits or palette experiments; missing some of Aseprite/LibreSprite's animation tooling. |

### 2.2 CC0 reference/inspiration packs (verified CC0 at kenney.nl)

Use these for **pose/timing reference and prototyping**, not final pixels, per §1's
resolution note:

| Pack | Native size | Contents | Link |
|---|---|---|---|
| Kenney "New Platformer Pack" | small (sub-64px) | ~440 assets: player, enemies, tiles, items | [kenney.nl/assets/new-platformer-pack](https://kenney.nl/assets/new-platformer-pack) |
| Kenney "Platformer Characters" | small | Multiple pre-posed character designs — good silhouette/pose inspiration for the new "Ampere" identity | [kenney.nl/assets/platformer-characters](https://kenney.nl/assets/platformer-characters) |
| Kenney "Pixel Platformer" | 18×18px (confirmed) | ~200 tile/character assets | [kenney.nl/assets/pixel-platformer](https://kenney.nl/assets/pixel-platformer) |
| Kenney "Platformer Kit" | small | Terrain/tile-focused | [kenney.nl/assets/platformer-kit](https://kenney.nl/assets/platformer-kit) |
| OpenGameArt "Generic Platformer Pack" | small | 1 tileset + 1 fully-animated main character (idle/jump/run) + 4 enemies — good reference for a *minimal* animation set if budget is tight | [opengameart.org/content/cc0-platformer](https://opengameart.org/content/cc0-platformer) |
| Kenney "Kenney Game Assets All-in-1" | mixed | ~60,000 assets across every category (sprites, UI, fonts, audio, 3D) in one download — useful as a single browsable library rather than per-pack hunting | [kenney.itch.io/kenney-game-assets](https://kenney.itch.io/kenney-game-assets) |

### 2.3 Theme-specific environment references

For the 5 world themes (Ground/UnderGround→Castle-cave/Castle/Sea/CloudsNight-night, per
[MARIO_LEVEL_ATLAS.md](MARIO_LEVEL_ATLAS.md)'s world tour). Check each pack's own license
page individually — several sites below are **not** blanket-CC0 even where marked "free":

| Theme | Resource | License | Note |
|---|---|---|---|
| Castle / UnderGround | [freegamesprites.itch.io/dungeon-pack](https://freegamesprites.itch.io/dungeon-pack) — 97 sprites, 16 seamless wall/floor tilesets + props | CC0 (stated on page) | Good starting silhouette reference for `Iron`/`stone`/`chocolate`/`brick_castle` looks |
| Sea | [freegamesprites.com underwater/reef pack](https://freegamesprites.com/en/packs/underwater) | Check per-download (freegamesprites generally states CC0, verify per pack) | Coral/kelp/fish silhouettes for `FishyWater`/`OctoPussy` redesigns |
| Sky/backdrop (Mountain/Clouds/Fence) | OpenGameArt "CC0 Backgrounds" collection | CC0 | Parallax cloud/mountain layer reference |
| Any theme, paid-tier option | [CraftPix.net freebies](https://craftpix.net/freebies/) | **Not CC0** — CraftPix's own license permits unlimited commercial use but explicitly **prohibits redistributing the original asset files as a standalone pack**; fine to use *in* the shipped game, not fine to re-bundle as a dev tool/template | Only use if the team is comfortable with a non-CC0, "used-in-product-only" license; document it in §6's tracker if so |

### 2.4 Fonts (for `font`/HUD, §16.5's "might not need original art" callout)

| Font | License | Link |
|---|---|---|
| "Public Pixel Font" | CC0 | Available on both itch.io and OpenGameArt — search "Public Pixel Font CC0" |
| "Good Neighbors" pixel font | CC0 | [opengameart.org/content/good-neighbors-pixel-font](https://opengameart.org/content/good-neighbors-pixel-font) |
| "Not Jam Font Pack" | CC0 | A 27-font CC0 bundle on itch.io — search "Not Jam Font Pack" |

Per [MARIO_GAME_MECHANICS.md §16.5](MARIO_GAME_MECHANICS.md#165-assets-that-might-not-need-original-art-at-all),
seriously consider skipping a custom `font` atlas region entirely and using one of these
(or the engine's already-bundled `uiSkin()` bitmap font) — zero new art for 48 of the
603 total frames. **2026-09-08 update:** §7.3 below adds a sci-fi-themed UI/font pack that
fits the identity better than a generic pixel font — prefer it if the team wants the HUD
visibly on-theme rather than just "any free bitmap font."

### 2.5 Audio: sound effects

| Pack | Contents | License | Link |
|---|---|---|---|
| Kenney "Digital Audio" | ~60 SFX, includes jump/coin/UI-style blips | CC0 | [kenney.nl/assets/digital-audio](https://kenney.nl/assets/digital-audio) |
| Kenney "Impact Sounds" | Hit/bump/break-style effects | CC0 | [kenney.nl/assets/impact-sounds](https://kenney.nl/assets/impact-sounds) |
| Kenney "Interface Sounds" / "UI Audio" | Menu/pause/select-style blips | CC0 | [kenney.nl/assets/interface-sounds](https://kenney.nl/assets/interface-sounds), [kenney.nl/assets/ui-audio](https://kenney.nl/assets/ui-audio) |
| Kenney "RPG Audio" | Broader SFX set (footsteps, pickups, magic-style) | CC0 | [kenney.nl/assets/rpg-audio](https://kenney.nl/assets/rpg-audio) |

Map against
[MARIO_GAME_MECHANICS.md §15.2](MARIO_GAME_MECHANICS.md#152-full-soundmusic-table)'s 23
sound keys — most (jump, coin, powerup, bump, stomp, kick, pause) have an obvious
Kenney-pack analog; a few (the boss-specific `smb_bowserfalls`/`smb_bowserfire`,
`smb_flagpole`) will need a closer listen-and-match pass or a custom recording/edit.
**2026-09-08 update:** §7.3 below found a better-themed Kenney pack (`Sci-Fi Sounds`) for
a robot-identity game — prefer it over the generic packs above where it covers the key.

### 2.6 Audio: music

| Pack | Contents | License | Link |
|---|---|---|---|
| Kenney "Music Jingles" | 85 short musical stingers (level-complete/game-over-style beats) | CC0 | [kenney.nl/assets/music-jingles](https://kenney.nl/assets/music-jingles) |

Kenney's own music offering is short jingles, not looping background tracks — the 5
looping `MUSIC_TRACKS` (`Ground`/`UnderGround`/`Castle`/`Star`/`Sea`, per
[MARIO_GAME_MECHANICS.md §15.2](MARIO_GAME_MECHANICS.md#152-full-soundmusic-table)) will
likely need a different source. Worth a follow-up search pass specifically for **looping
chiptune background tracks** (as opposed to one-shot jingles/SFX) before committing —
OpenGameArt's music section and itch.io's `tag-chiptune` + `tag-music` + `assets-cc0`
combination are the next places to check, not yet individually verified here.
**2026-09-08 update: this gap is now closed, see §7.3** — two verified CC0 sources of
seamless looping chiptune tracks were found.

### 2.7 AI-assisted generation (for first-draft exploration, per Path C in §1)

| Tool | Notes | Link |
|---|---|---|
| Retro Diffusion | Purpose-trained on pixel art; grid-aligned, palette-limited output; credit-based pricing (~1¢/image), commercial use on paid tiers | [retrodiffusion.ai](https://retrodiffusion.ai/) |
| PixelLab | Sketch-to-pixel-art assist, respects a supplied palette; subscription + pay-per-credit API, commercial use on paid tiers | search "PixelLab AI pixel art" |

Verify the exact plan/tier's commercial-use terms at time of purchase (pricing/tiers
change) and keep a record per §6 — "commercial use included" claims from a search summary
aren't a substitute for reading the actual terms page before shipping generated art.

## 3. Palette-swap tooling for the Star-recolor variants

Per [MARIO_GAME_MECHANICS.md §16.3](MARIO_GAME_MECHANICS.md#163-player-307-frames-sounds-huge-isnt-really-307-unique-drawings),
the 6 Star-invincibility recolor sheets (168 of the player category's 307 frames) don't
need independent hand-drawn art — they're a mechanical palette swap over the finished
Small/Big art. This repo's own documentation tooling already proved a Python+Pillow image
pipeline works well in this environment (see the sprite-extraction script this doc set's
research used) — the same approach covers this:

1. Finish the real Small/Big/Fire base art first (Path A, §1).
2. Write a short Pillow script: load each base frame, remap its palette to the
   black/green/red variant (a fixed color-substitution table, not a generic filter — match
   the *shape* of the original effect: same silhouette, different flat color per variant,
   confirmed by inspecting the existing `small_black_mario`/`small_green_mario`/
   `small_red_mario` regions extracted to `docs/assets/mario-sprites/common/` from this
   research pass).
3. Run it once per new base sheet (Small, Big — Fire has no separate recolor set per the
   existing asset list) to generate all 6 variants automatically.

Dedicated community tools exist too if a non-technical team member needs to do this
without scripting — "ColorCraft" and "SpritePalettizer" (both itch.io tools) are built
specifically for batch sprite-sheet recoloring; evaluate either only if the scripted
approach above doesn't fit the team's workflow.

## 4. Step-by-step execution sequence

This concretizes [MARIO_RESKIN_PLAN.md §4.4](MARIO_RESKIN_PLAN.md#44-content-swap-execution-unchanged-from-the-original-plan-otherwise)
into an ordered, checkable task list. Each step names its exit condition — don't start the
next until the current one is checked off.

### Step R.0 — Decisions (blocking everything else)

- [x] **Prerequisite, not a decision:** confirm
      [PLATFORMER_ENGINE_ARCHITECTURE.md §7](PLATFORMER_ENGINE_ARCHITECTURE.md#7-migration-plan)'s
      Phases A–G are complete and Mario's full regression pass is clean on the current
      placeholder art. Per
      [MARIO_RESKIN_PLAN.md §5](MARIO_RESKIN_PLAN.md#5-ordering-phase-2--platformer-re-architecture--reskin-as-sequential-passes)'s
      amended ordering, none of the steps below start before this is true. **Confirmed
      2026-09-08: on-device regression pass run against
      [MARIO_LEVEL_ATLAS.md](MARIO_LEVEL_ATLAS.md), works as expected** — this closes the
      "still the user's own responsibility" gap
      [PLATFORMER_ENGINE_IMPLEMENTATION.md §0](PLATFORMER_ENGINE_IMPLEMENTATION.md#0-ground-rules-this-implementation-followed)
      left open, including Phase B2's z-order risk, Phase E's grow/shrink/Star/flagpole
      sequences, and Phase F's `WorldLevelSelectScreen` layout.
- [x] Finalize the new identity (character/enemy/world names) — starting strawman already
      in [MARIO_RESKIN_PLAN.md §2](MARIO_RESKIN_PLAN.md#2-new-identity-starting-proposal--a-creative-decision-for-the-team-to-adjust-not-a-fixed-spec).
      **Decided 2026-09-08: accepted as-is.** Title "Ampere's Run." Ampere / Battery cell /
      Charge coil / Overclock chip / Bolt-gear coin / Spare chassis / Scuttler / Roller /
      Plater / The Warden / the Signal Core; world themes Surface / Substrate / Fortress /
      Flooded Sector / Night Shift — the exact §2 table, no changes. Rationale: coherent,
      clears Nintendo's specific silhouettes/color schemes per §2's hard constraint, and
      leaves room for a Morse-code tie-in later without forcing one now. **Refined
      2026-09-08 per [MARIO_RESKIN_PLAN.md §2's update](MARIO_RESKIN_PLAN.md#2-new-identity-starting-proposal--a-creative-decision-for-the-team-to-adjust-not-a-fixed-spec):**
      the Morse hook is now planned, not speculative, and the Coin→"Bolt/gear" asset is
      its carrier — see the new §7.5 note below and Step R.3's updated coin bullet.
- [x] Confirm `ART_SCALE` (recommend `2`, i.e. 64px/tile, per that plan's §4.1) — this
      pins every subsequent asset's target dimensions. **Decided 2026-09-08: `ART_SCALE=2`**,
      per the plan's own memory-budget reasoning (pixel area scales with the square of the
      factor). Filtering stays `Nearest,Nearest` — every asset source lined up in §7 (Robot
      Master Series, Robot Platform Pack, the CC0 fallbacks) is pixel-art style, not
      painterly, so smoother filtering would fight the art direction those packs already
      commit to.
- [x] Decide on Path B (LPC/CC-BY-SA) acceptability — yes/no, and if yes, for which
      specific asset categories only (§1's table). Record the decision in §6's tracker
      regardless of the answer. **2026-09-08 update:** §7.1 makes this likely moot for the
      player/enemy/boss roster specifically — revisit only if §7's packs don't pan out.
      **Decided 2026-09-08: declined entirely, including for Substrate/Flooded Sector**
      (§7.4's remaining open themes) — use the recolor-a-generic-CC0-tileset fallback
      instead. Not worth taking on CC-BY-SA/ShareAlike obligations for two background
      tilesets when every other sourced asset is clean CC0/free-commercial, and a recolor
      pass gives more palette control to match the mechanical/sci-fi identity than fixed
      LPC art would anyway. Logged in
      [MARIO_RESKIN_CREDITS.md](MARIO_RESKIN_CREDITS.md).
- [x] Pick the AI-generation tool (if any) and confirm its commercial license tier is
      actually purchased/active before any output from it enters the shipped asset tree.
      **Decided 2026-09-08: Retro Diffusion**, scoped narrowly — §7's packs now cover the
      player/enemy/boss roster, so this tool's actual job is the long-tail scenery/
      background dressing and the Substrate/Flooded Sector recolor-and-dress work, not
      hero assets. Picked over PixelLab for its grid-aligned pixel-art output (matches the
      `ART_SCALE`/filtering decision above) and per-image credit pricing, which fits a
      fill-the-gaps workflow better than a subscription. **Commercial tier must still be
      purchased/activated before Step R.4/R.5 generates anything that ships** — not done
      yet, tracked in [MARIO_RESKIN_CREDITS.md](MARIO_RESKIN_CREDITS.md).

### Step R.1 — Land the `ART_SCALE` code decoupling (no new art yet)

- [ ] Implement [MARIO_RESKIN_PLAN.md §4.1](MARIO_RESKIN_PLAN.md#41-resolution-architecture-this-is-not-a-pure-content-swap)'s
      `MarioConfiguration.ART_SCALE` constant + the matching `/ART_SCALE` division at
      every call site that treats a texture region's pixel size as a world size — by this
      point, R.0's prerequisite means those call sites already take `tileSize` as an
      explicit parameter (not a hardcoded literal) per
      [PLATFORMER_ENGINE_ARCHITECTURE.md §3.8](PLATFORMER_ENGINE_ARCHITECTURE.md#38-tilemetrics-making-tile-size-a-first-class-non-global-value)'s
      full ~40-call-site list, not just the `PlayerPowerState`/`Scenery`/`Lift` trio this
      step originally scoped.
- [ ] Re-pack the *existing* placeholder art unchanged, confirm all 8 worlds still
      render/collide identically (this is [MARIO_RESKIN_PLAN.md §4.4.2](MARIO_RESKIN_PLAN.md)'s
      own isolation step — verify the code change before any art changes, so a bug is
      unambiguously attributable to one or the other).

### Step R.2 — Player character (Path A, priority 1)

- [ ] Draw the 3 base states at 64×64px/frame, matching the frame-index convention in
      [MARIO_GAME_MECHANICS.md §13.3](MARIO_GAME_MECHANICS.md#133-frame-strip-slicing-convention)
      exactly (4 cols × 7 rows; idle/airborne/walk-cycle/skid/swim/duck poses at their
      existing frame indices) — 84 frames total (Small 28 + Big 28 + Fire 28).
- [ ] Draw the 4 transition/morph strips (Small→Big, Big→Fire, Big→Small, Fire→Small) —
      54 frames, following the existing strips' row-based layout
      (`TRANSITION_FRAME_WIDTH/HEIGHT` = 32×64 pre-`ART_SCALE`, per `Player.java`).
- [ ] Generate the Star-recolor variants via §3's script (not hand-drawn).
- [ ] Draw `small_dead_mario` (1 static pose).
- [ ] Swap `PackMarioAtlas`'s `sourceDir` for just these assets, re-pack, load into a dev
      build, and visually confirm every animation state in-game before moving on — use
      `debug.DebugPanel`'s power-state cycling
      ([MARIO_GAME_MECHANICS.md §12](MARIO_GAME_MECHANICS.md#12-debugqa-tooling)) to cycle
      Small→Big→Fire→Small on demand instead of hunting for a Mushroom/Flower in a level.

**2026-09-08 update:** consider sourcing this step's base art from
[§7.1](#71-player--enemies--boss--two-strong-all-in-one-candidates)'s "Robot Master Series
– Base Asset Pack" or "Robot Platform Pack" instead of drawing from scratch, then editing
frame counts/layout to match the convention above — still cheaper than full original
authoring even with the re-layout work.

### Step R.3 — Priority-1 world assets (highest on-screen frequency)

Per [MARIO_GAME_MECHANICS.md §16.4](MARIO_GAME_MECHANICS.md#164-reskin-priority-by-on-screen-frequency)'s
tier 1: `Brick`, `stone`/`chocolate` (×5 themes), `EnemyMushroom`-equivalent.

- [ ] Design and draw the new terrain tile look for all 5 themes (Ground/UnderGround/
      Castle/Sea + the CloudsNight decision from §4.4 of the mechanics doc — tint shader
      vs. hand-drawn `bw_*` set).
- [ ] Design and draw the breakable-brick sprite + its break-fragment art
      (`brick_peaces`, 2×4 grid).
- [ ] Design the Bolt/gear coin sprite with the future Morse hook in mind (§7.5) — no new
      art or code for the hook itself yet, just don't design the coin/HUD layout in a way
      that would need reworking once a `letterOfMorseCode`-style tag and a
      `ChallengeLetter`-style top-left HUD dock are added.
- [ ] Design and draw the most common enemy's replacement (walk cycle, matching
      whatever frame count the new design needs — not required to match the original's
      2×4 grid exactly, since this is new character art, not a literal reskin of the same
      pose timing).
- [ ] Re-pack and playtest one full world (World 1, per the level atlas) before scaling
      up to the rest — this is the same "prove the pipeline on a vertical slice" discipline
      the original port used.

### Step R.4 — Remaining enemies, bricks, items, hazards, lifts

- [ ] Work down [MARIO_GAME_MECHANICS.md §16.4](MARIO_GAME_MECHANICS.md#164-reskin-priority-by-on-screen-frequency)'s
      tiers 3 and 4 in order.
- [ ] For each, decide Path A vs. Path C (§1) individually rather than batching — a
      one-off enemy used 3 times in the whole game (e.g. `SonOfABuitch`) is a reasonable
      candidate for an AI-assisted first draft with a light cleanup pass; the boss is not.
      **2026-09-08 update:** the mini-boss in
      [§7.1](#71-player--enemies--boss--two-strong-all-in-one-candidates)'s "Robot Master
      Series" pack is a real candidate for "The Warden" specifically — check it before
      defaulting to a custom-drawn boss.

### Step R.5 — Scenery, backdrops, HUD, UI text

- [ ] Parallax backgrounds (Mountain/Clouds/CloudsNight/Fence/Sea) — reference §2.3's
      environment packs for composition ideas, plus
      [§7.2](#72-world-theme-tilesbackgrounds)'s Night Shift pick.
- [ ] Castles, flags, end-of-level banners.
- [ ] Decide on the `font`/`info`/`info2` question from §2.4 (swap for a CC0 font vs. the
      already-bundled engine skin font) rather than commissioning new glyph art — or the
      sci-fi UI pack in [§7.3](#73-ui-fonts-sfx-music).
- [ ] Rewrite the 3 user-facing strings
      ([MARIO_RESKIN_PLAN.md §1.3](MARIO_RESKIN_PLAN.md)) to match the finalized identity
      from Step R.0.

### Step R.6 — Audio

- [ ] Map all 23 sound effects to their closest Kenney-pack analog (§2.5, or §7.3's
      sci-fi-specific pack), edit/trim as needed to match the original's timing/feel where
      that matters (e.g. the jump sound's short punchy length).
- [ ] Source or compose the 5 looping music tracks (§2.6 flagged this as the one audio
      category without an obvious ready-made CC0 answer — **now closed, see §7.3**).
- [ ] Swap files under `assets/mario/audio/`, confirm every `sound(...)`/`music(...)` call
      site still resolves (call sites use the constant key, not a hardcoded filename, so
      this is a low-risk swap per
      [MARIO_RESKIN_PLAN.md §4.4.5](MARIO_RESKIN_PLAN.md)).

### Step R.7 — Full regression pass

- [ ] Run [MARIO_RESKIN_PLAN.md §4.4.7](MARIO_RESKIN_PLAN.md)'s full regression pass
      across **all 8 worlds** (use [MARIO_LEVEL_ATLAS.md](MARIO_LEVEL_ATLAS.md) as the
      checklist of what to visit — one level per row) — every region/sound key resolves,
      every actor's hitbox lines up with its new higher-res sprite, nothing clips against
      the unchanged tile grid.
- [ ] Test on a real low/min-spec device for texture-upload memory pressure, per
      [MARIO_RESKIN_PLAN.md §4.3](MARIO_RESKIN_PLAN.md)/§6.
- [ ] Only once this pass is clean does the distribution gate in
      [MARIO_RESKIN_PLAN.md §5](MARIO_RESKIN_PLAN.md) open.

## 5. Progress tracking

Track Steps R.2–R.6 against
[MARIO_GAME_MECHANICS.md §15.1](MARIO_GAME_MECHANICS.md#151-full-sprite-sheet-asset-table)'s
129-row asset table (or its underlying `docs/assets/mario-sprites/manifest.json`,
generated during this research pass) as the master checklist — one row per sheet, checked
off as its replacement lands and passes the debug-panel spot-check
([MARIO_GAME_MECHANICS.md §12](MARIO_GAME_MECHANICS.md#12-debugqa-tooling)). This is the
same discipline [MARIO_RESKIN_PLAN.md §5](MARIO_RESKIN_PLAN.md) already calls for
("track the asset spec sheet as a checklist, not just a reference document") — this
document just names the concrete file to use for it.

## 6. Licensing/attribution tracker

Keep a running record (a spreadsheet or a `docs/MARIO_RESKIN_CREDITS.md`, not yet
created) of, per shipped asset: source, license, attribution text required (if any,
e.g. any Path B/LPC asset), and the date/URL it was verified at. This is what makes
"we checked the license" defensible later, and is cheap to keep up to date incrementally
versus reconstructing it after the fact. CC0 assets (Kenney, most OpenGameArt content)
need no entry beyond "CC0, sourced from X on date Y" for the team's own records; anything
under a different license (LPC's CC-BY-SA, CraftPix's redistribution-restricted terms, any
AI tool's specific commercial-tier terms) needs its actual requirement spelled out here,
not just "checked, fine."

## 7. Sci-fi/robot-themed sourcing pass (2026-09-08 addendum)

The §2 shortlist above was researched generically ("platformer CC0 pack") before
[MARIO_RESKIN_PLAN.md §2](MARIO_RESKIN_PLAN.md#2-new-identity-starting-proposal--a-creative-decision-for-the-team-to-adjust-not-a-fixed-spec)'s
sci-fi/robot identity ("Ampere", "Scuttler", "Roller", "The Warden") was locked in, and
per the user's explicit ask, this pass is scoped to **existing free resources only** — no
in-house drawing, no paid commissions. Searching *for that specific identity* (robot
characters, sci-fi tiles/UI/SFX) turns up much better silhouette/theme matches than the
generic packs in §2.2, including two packs that are close to a 1:1 drop-in for this
game's actual roster. Every entry was checked against its own itch.io/kenney.nl page
during this pass (2026-09-08) — re-verify before bulk-importing, same standing rule as
§0 above.

### 7.1 Player + enemies + boss — two strong all-in-one candidates

| Pack | Contents | License | Fit |
|---|---|---|---|
| [**Robot Master Series – Base Asset Pack**](https://au-pixel.itch.io/robotbasepack) (AU_pixel) | 3 playable robots (walk/jump/attack/charge/dash/climb/damage/death, alt "covered-face" variants included specifically to reduce Mega Man similarity), 3 enemy types (walk/attack/bullets), **1 mini-boss** (gun + laser attacks, jet animation, pit-summon), plus intro-stage platforms, layered sky backgrounds, foreground props, HP-bar UI, explosions, gates | Pay-what-you-want (free tier). Commercial + non-commercial use OK, editable; **no resale/repackage/redistribution, no logo/trademark/NFT use** | Best single source for **Ampere + Scuttler/Roller + The Warden boss** in one coherent art style — the mini-boss alone is a rare find (most free packs stop at basic enemies) |
| [**Robot Platform Pack**](https://edusilvart.itch.io/robot-platform-pack) (edusilvart) | 1 player robot (idle/run/jump/land/attack×2/hurt/death), 1 enemy robot (idle/shot/run/death + bullet), tileset, platforms, boxes, trampoline, **key/keycard/keycard-reader/door**, coin, spikes, button, lever, HP heart | Free ("name your own price"). Commercial use OK, **no resale** | Its item roster maps almost 1:1 onto Mario's (coin→coin/bolt, key→"access key" item, heart→spare-chassis/1-up, trampoline→spring) — good pick if the simpler single-enemy scope fits a given world better than the Base Asset Pack's larger cast |
| [OpenGameArt "The Robot – Free Sprite"](https://opengameart.org/content/the-robot-free-sprite) | 10 animation states, separate PNG sequences | **CC0** (true public domain, no restrictions at all) | Fallback if the two packs above's no-redistribution clause is ever a problem — this one has zero strings attached |
| [Foozle "Cute Platformer Robot"](https://foozlecc.itch.io/cute-platformer-robot) | Idle/walk/run/jump, sold as separated body parts for custom animation | **CC0** | Simple, CC0, good for a secondary/minor character or as a base to hand-edit |

Both non-CC0 packs above license out "no resale/repackage as an asset pack," which is
irrelevant here (the game isn't reselling the art itself) — record them in §6's tracker
as "free, commercial-use-permitted, no-redistribution" rather than CC0.

### 7.2 World-theme tiles/backgrounds

| Theme (§2's world-name mapping) | Lead | License | Status |
|---|---|---|---|
| Surface (general fill-in) | [Kenney "Platformer Art Extended Tileset"](https://kenney.nl/assets/platformer-art-extended-tileset) — 360 assets | CC0 | Solid generic filler for whatever the two robot packs above don't cover |
| Fortress (Castle-analog) | ansimuz "Warped – Space Station" / aske4 "Free Sci-Fi TileSet Space Station" (both itch.io) | Check each page individually — not yet confirmed CC0, likely free/pay-what-you-want | Promising silhouette (station corridors, consoles) for a "fortress" reading; verify license before committing |
| Substrate (UnderGround-as-circuitry) | **No direct "circuit-board cave" pack found.** | — | Gap: recolor a free cave tileset (several 16×16 CC0 options exist, e.g. itch tag `cave`+`pixel-art`) with a circuit/conduit palette and Kenney sci-fi props layered in, rather than hunting further for an exact thematic match that likely doesn't exist as a free pack |
| Flooded Sector (Sea-analog) | **Still the weakest lead**, confirming §1's original flag | — | No good free "sci-fi underwater" combo turned up this pass either (found underwater packs and sci-fi packs, not both at once); plan to reskin a generic CC0 underwater tileset with metal/glass/pipe dressing rather than keep searching for an exact match |
| Night Shift (Night-analog) | [karsiori "Free Pixel Art – City Parallax Background"](https://karsiori.itch.io/free-pixel-art-city-background-pack) | CC0 (stated on page) | Good cyberpunk-night parallax backdrop layers |

### 7.3 UI, fonts, SFX, music

| Category | Pick | License | Note |
|---|---|---|---|
| HUD/UI (replaces needing a custom `font`/`info`/`info2` per §2.4) | [Kenney "UI Pack – Sci-Fi"](https://kenney.nl/assets/ui-pack-sci-fi) — 130 assets, buttons/panels/cursors/progress bars ×5 colors, **+2 bonus fonts** | CC0 | On-theme replacement for the whole HUD question in one pack — better fit than §2.4's generic pixel fonts given the sci-fi identity |
| SFX (replaces §2.5's generic picks) | [Kenney "Sci-Fi Sounds"](https://kenney.nl/assets/sci-fi-sounds) — ~70 effects (engine/laser/beep-style) | CC0 | Thematically a better match than "Digital Audio"/"Impact Sounds" for a robot game; still cross-check against the 23-key sound table per §2.5's method |
| Looping background music (closes §2.6's flagged gap — "no obvious ready-made CC0 answer") | [HydroGene "High Quality 8-bit/Chiptune Musics"](https://hydrogene.itch.io/high-quality-8-bit-musics) — 18 tracks, **confirmed seamless loops**, action/platformer-oriented | CC0, name-your-own-price, no attribution required | Direct fix for the one audio category §2.6 left open |
| Looping music (backup/larger pool) | Tallbeard/Abstraction ["Music Loop Bundle"](https://tallbeard.itch.io/music-loop-bundle) — 200+ seamless loops | CC0/public domain; page adds a **non-binding** request not to use in AI/NFT projects | Bigger pool if HydroGene's 18 tracks don't cover all 5 `MUSIC_TRACKS` moods |

### 7.4 Net effect on Step R.0's decisions

- This closes §1's Path B (LPC/CC-BY-SA) question by making it **unnecessary** for the
  player/enemy/boss roster — 7.1's packs cover that ground without ShareAlike strings.
- §2.6's "no answer yet" gap for looping music is now closed (7.3).
- Substrate and Flooded Sector world themes remain open — no exact free-pack match
  exists; budget a recolor/dressing pass over a generic free tileset for those two
  specifically rather than continuing to search for a perfect thematic hit.
- Update §6's tracker with all of 7.1–7.3's entries once actually downloaded, noting the
  two non-CC0-but-free-commercial packs' no-redistribution clause explicitly.

### 7.5 Morse-code training hook (forward-compatibility note, 2026-09-08)

The Morse hook is a planned future addition, not part of this reskin's own scope — but
the reskin's art/HUD decisions should leave room for it now rather than need reworking
later, per the precedent already proven in this app's other two mini-games:

- **`flappybird`**: `hud/ChallengeLetter.java` docks a queue of up to 5 target letters at
  top-left, drawn from a shared `morsecode.atlas` (`letterA-Z`/`number0-9`/`dot`/`dash`
  regions). `actor/Playground.java` tags each tube gate with a letter
  (`TubePosition.letterOfMorseCode`); matching the front of the queue scores bonus and
  advances it. A `gameEncode` user setting toggles between showing the plain letter and
  showing its dot/dash breakdown (via `helper/MorseHelper.morseCodeData`).
- **`battlecity`**: `LedLetters.java` renders a random letter as an LED dot-matrix out of
  destructible level bricks, with its Morse pattern rendered as a second brick pattern
  underneath (`actors/BattleField.java`), reusing the same `MorseHelper` table.

**What this means for the reskin now:**
- The Coin→"Bolt/gear" mapping (§2 of the plan) is the natural carrier — give it a
  `letterOfMorseCode`-style tag when the hook is actually built, the same role
  `TubePosition` plays in `flappybird`.
- Reserve HUD space for a `ChallengeLetter`-style dock (top-left) in whatever HUD layout
  Step R.5 settles on, so it doesn't get squeezed in awkwardly later.
- **Zero new art or licensing work**: `morsecode.atlas`/`MorseHelper` already exist
  in-house, CC-free, and are shared across two games today — reusing them a third time
  for Mario needs no sourcing, no AI-gen, no Path B consideration at all.
- **No `ART_SCALE`/`TileMetrics` interaction**: these glyphs are HUD-scale assets, not
  world-tile assets — same reasoning already applied to `ScoreHud` in Phase F of
  [PLATFORMER_ENGINE_IMPLEMENTATION.md](PLATFORMER_ENGINE_IMPLEMENTATION.md#phase-f--hud--menu--save-state--debug-panel-generalization).
  Don't thread `tileSize` through them.
- Actually wiring the tag/HUD/scoring logic into Mario is deferred, real engine work —
  not scheduled here, just protected against by this note.

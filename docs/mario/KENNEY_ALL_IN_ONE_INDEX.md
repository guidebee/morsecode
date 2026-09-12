# Kenney "Game Assets All-in-1" — Curated Index for Ampere's Run

Local path: `C:\workspace\Kenney_Game_Assets_All` (not part of this repo — a local
download, same as the two Robot Master Series/Robot Platform Pack folders). License:
confirmed by reading `Readme.html` directly — **the entire bundle is CC0 1.0**, "you may
use these graphics in personal and commercial projects," credit "nice but is not
mandatory." No per-pack license checking needed, unlike the two robot packs.

Scope: this is a curated shortlist, not an exhaustive catalog — the bundle has 156 2D
packs + 54 3D + 16 audio + 8 icon + 10 UI packs (~60,000 files total). Anything not
listed here can be found via the bundle's own `assets.json` manifest (`categories[].packs[]`,
each with a `preview` image path and `folders[].files[]`) rather than crawling the
filesystem blind.

## 0. Read this first: Kenney ships multiple visual sub-styles

The single most important filter for us, found by actually opening preview images rather
than going by pack names alone: packs with very similar, adjacent-sounding names are
often in *completely different art styles*, and only one style matches what's already
shipping (Robot Master Series' pixel art, used for Ampere/Scuttler/Roller/The Warden).

- **"Pixel ___" packs** (`Pixel Platformer`, `Pixel Platformer Blocks`, `Pixel Platformer
  Industrial Expansion`, `Pixel Shmup`, `Pixel Vehicle Pack`, `Pixel Line Platformer`,
  `Cursor Pixel Pack`, `UI Pixel Pack`, `UI Pack - Pixel Adventure`) — genuine retro pixel
  art. **These are the ones that visually match our existing art.**
- **Everything else in the "Platformer ___"/robot/space family** (`Robot Pack`,
  `Platformer Pack Industrial`, `Platformer Assets Base/Tile Extensions/Extra Animations
  & Enemies`, `Platformer Bricks`, `Platformer Characters 1`, `Alien UFO Pack`, `Simple
  Space`, `Space Shooter Remastered`, `Tank Pack`) — clean **flat-shaded vector/cartoon**
  style (rounded shapes, flat colors, soft drop-shadows). Confirmed by opening each
  pack's `Preview.png` directly. **Do not use these for player/enemy/terrain sprites** —
  mixing this style into Ampere's pixel-art world would look like two different games
  glued together. They're serviceable for UI chrome only, where style-matching is less
  strict (see §5).

Whenever pulling anything from this bundle, check the preview image first and confirm
it's from the Pixel family before treating it as a game-world sprite source.

## 1. Terrain/tile candidates (the reason this scan started)

Motivation: [MARIO_RESKIN_EXECUTION.md Step R.3](MARIO_RESKIN_EXECUTION.md#step-r3--priority-1-world-assets-highest-on-screen-frequency)'s
procedurally-drawn stone/chocolate/brick tiles work but look flat/programmer-art — real
hand-drawn pixel tiles should read better.

| Pack | Path | Contents | Fit |
|---|---|---|---|
| **Pixel Platformer Industrial Expansion** | `2D assets/Pixel Platformer Industrial Expansion/` | Girders/I-beams, riveted metal blocks, barrels, wrench/hammer tools, hazard tape, conveyor belts, warning signs, chain-link ladder, toxic-green liquid tiles, checkered hazard tiles — all genuine pixel art | **Best terrain candidate found.** Reads as "industrial facility" out of the box, no recoloring needed for Fortress; the girder/hazard-tape motifs alone are a big step up from the procedural panel tiles |
| **Pixel Platformer Blocks** | `2D assets/Pixel Platformer Blocks/` | Bordered/beveled block variants (plain, dotted, X-pattern, diamond-cutout, quartered) in one rust-orange palette, plus 4 flat corner/edge recolor swatches | Good supplementary shapes; monochrome so still needs the same per-theme recolor script already built for the procedural tiles — but the hand-drawn bevel/dither quality is much better raw material to recolor than a flat rectangle |
| **Pixel Platformer** (base) | `2D assets/Pixel Platformer/` | Terrain, ladders, spikes, flags, small round-headed characters, a few small pixel vehicles, water tiles, numbers/UI glyphs | General filler / pose-timing reference (same role §2.2 of `MARIO_RESKIN_EXECUTION.md` already gave it) — also has small critter heads worth a look for minor enemy variety later |
| **RTS Sci-fi** | `2D assets/RTS Sci-fi/` | Pixel art: organic orange "Tiberium-like" resource blobs, pipe/conduit pieces, small sci-fi buildings (grey with red/blue accent lights), vehicles | Good *decorative prop* layer (pipes, blobs) but not itself a full wall/floor tileset — see Tiny Dungeon below for the actual Substrate terrain base |
| **Tiny Dungeon** | `2D assets/Tiny Dungeon/` (132 individually-sliced tiles + a packed tilemap) | Pixel art dungeon set: dark stone-block walls/floors, metal-barred doors/gates, jail bars, torches, skulls | **Better Substrate terrain base than RTS Sci-fi** — its stone-block architecture is already cave/tunnel-shaped, and the metal-door/bar pieces retrofit into "circuit-cave" with a recolor + RTS Sci-fi's pipes/blobs layered on top as decoration, exactly the "recolor a free cave tileset with circuit dressing" fallback the original plan proposed |
| **Roguelike Dungeon Pack** | `2D assets/Roguelike Dungeon Pack/` | Pixel art dungeon set in a **blue/teal palette** (ice-cave-like walls, doors, ladders) | **Strong Flooded Sector candidate** — reframe as damp/flooded tunnels rather than literal underwater (the original sourcing pass's "still the weakest lead" theme). Its cool blue palette already reads as wet/cold without any recoloring, closing a gap the original §7.2 research never found a match for |
| **Pixel Shmup** | `2D assets/Pixel Shmup/` (`Ships/`, 24 files) | Pixel art small spaceship/fighter sprites | Not terrain, but worth flagging: a plausible source for a flying enemy/drone/mini-boss-jet replacement later (R.4), genuine pixel art matching our style |

**Recommendation:** swap Fortress's terrain to a recolor of Pixel Platformer Industrial
Expansion's girder/panel pieces first (highest visible improvement, matches its own
theme perfectly already). For Substrate, use Tiny Dungeon's stone/door tiles as the base
with a circuit-accent recolor. For Flooded Sector, use Roguelike Dungeon Pack's
already-blue palette reframed as flooded tunnels — this closes the one theme gap the
original research never solved. Ground (Surface) can stay on the procedural tile or get
the same Pixel Platformer Blocks treatment — lower priority since it already reads
acceptably.

**DONE 2026-09-08**: Fortress/Substrate/Flooded Sector all swapped per this
recommendation (script + exact tile indices in §12.1), packed, and **confirmed on-device
— looks fine**. Ground stays procedural, per the "lower priority" call above. See
[MARIO_RESKIN_EXECUTION.md Step R.3](MARIO_RESKIN_EXECUTION.md#step-r3--priority-1-world-assets-highest-on-screen-frequency)
for the closing write-up.

## 2. Power-up items — 3 of 4 solved directly from Robot Master Series' own `other/` folder

Found by finally opening two small files in that folder that earlier passes only
glimpsed (`miscel.png`, `gate.png`) — same pack as the player/enemy art already in use,
so zero style-matching risk.

| Original → identity | Source | Fit |
|---|---|---|
| Mushroom → **Battery cell** | `robot_series_base_pack/other/miscel.png` top two rows | A literal battery/capacitor icon (glowing orange core between grey caps) — reads exactly as "battery cell" with no reinterpretation needed. 6 near-identical variants available (could double as a subtle idle-glow animation). |
| Star → **Overclock chip** | `robot_series_base_pack/other/miscel.png` bottom row | A green circuit-chip/gem icon, some variants with a blue diagonal shine — reads as "overclock chip" directly. |
| Fire Flower → **Charge coil** | `robot_series_base_pack/other/gate.png` | A repeating stacked-cylinder column with a glowing teal band — literally coil-shaped. Source art is a tileable multi-segment strip (meant as a gate/pillar); crop one segment for a standalone pickup icon. |
| 1-Up → **Spare chassis** | **No clean match found anywhere scanned** (robot packs, Kenney bundle) | Pragmatic fallback: Robot Platform Pack's pink heart icon (§2.4 of `MARIO_RESKIN_EXECUTION.md`'s original R.0 comparison already noted this). Thematically soft (organic heart vs. mechanical identity) but functionally fine, and hearts are conventional enough across genres that even mechanical-themed games often keep them. The one item in this whole scan genuinely worth a small custom icon instead, if it bothers anyone — it's a single small sprite, low effort either way. |

**Bonus reuse:** the same coil motif from `gate.png` also makes a good **Bouncer/Spring**
replacement (a coil *is* a spring, visually and thematically) — one more asset closed
without pulling in a third source pack.

## 3. Bricks/world-mechanism candidates

| Original | Source | Fit |
|---|---|---|
| Pipes/`pump` | **RTS Sci-fi**'s `Tilesheet/scifi_tilesheet.png` — winding grey conduit/pipe-network pieces | Good direct visual match for a "sci-fi conduit" reading of the pipe-warp mechanic |
| `small_castle`/`big_castle` | **RTS Sci-fi**'s small tech/factory/refinery buildings (top-right of the same tilesheet) | Reframes "castle" as a fortress/industrial installation instead of a medieval keep — fits the identity better than hunting for a sci-fi "castle" literally |
| Bouncer/Spring | Robot Master Series `gate.png` coil motif (see §2) | Reuse, no new source needed |
| Axe (the castle-ending bridge-cut lever) | **No match found** | Small, single icon — likely fine as a quick custom "console/switch" icon (low effort) rather than worth more searching or AI-gen |
| Bridge/chain/rope | **No match found** | Same tier as the terrain tiles — a simple hand-drawn cable/plank texture (procedural, like Step R.3's terrain) is the pragmatic answer, not worth further hunting |

## 4. Scenery/parallax background candidates

| Theme | Source | Fit |
|---|---|---|
| Sea backdrop | Robot Master Series `other/platform.png`'s "BACKGROUND 2" section | A ready cyan/blue wave-water texture, same pack as everything else — direct match for the tiled `sea_background` asset |
| Substrate decoration | Robot Master Series `other/platform.png`'s "BACKGROUND 1" section | A dark-green circuit-board-patterned panel texture — good wall/backdrop dressing layered behind Tiny Dungeon's terrain (§1) |
| Generic Mountain/Clouds/Fence parallax | Kenney **"Background Elements"** (the original, *not* "Remastered" — confirmed by opening both previews) | The non-remastered pack is plain **silhouette** shapes (clouds/mountains/castle towers/wave-bands), not fully-rendered vector art — silhouettes tint/recolor cleanly and parallax layers are conventionally soft/monochrome anyway, so the usual pixel-vs-vector style concern matters much less here than for foreground sprites |
| Trees | **No good match found** — Kenney's tree assets are all in the mismatched flat-vector "Remastered" style, robot packs have none | Worth a design reframe instead of a continued search: replace organic trees with **antenna/pylon** scenery, which fits the mechanical identity better anyway and could be built with the same procedural approach already used for terrain |

## 5. Player/character candidates

| Pack | Style | Verdict |
|---|---|---|
| Robot Pack | Flat vector | **Don't use for Ampere/enemies** — wrong style family (see §0). Could be a foundation for a *totally different*, non-pixel visual direction if that's ever revisited, but not a drop-in supplement to the current look. |
| Platformer Characters 1 | Flat vector/toy-render | Same issue — style mismatch. |
| Pixel Platformer's small characters | Pixel | Too generic/blank (round blob heads) to read as Ampere or a named enemy; fine only as secondary background NPCs if the game ever needs unnamed extras. |

**Verdict: nothing here replaces Robot Master Series for the player/enemy/boss roster.**
The R.0 decision already made ([MARIO_RESKIN_EXECUTION.md §7.1](MARIO_RESKIN_EXECUTION.md#71-player--enemies--boss--two-strong-all-in-one-candidates))
stands — this bundle doesn't have a pixel-art robot character set to compete with it.

## 6. VFX candidates

| Pack | Style | Contents | Fit |
|---|---|---|---|
| Explosion Pack | Flat vector | Sparkle/burst shapes in 4 colors + white flash | Usable as a supplement/placeholder despite the style mismatch — abstract bursts read fine regardless of style, less "characterful" than a robot sprite. Robot Master Series' own `other/explode-Sheet[64height64wide].png` (pixel, already available) is still the better first choice. |
| Particle Pack | Flat vector | Generic particle sprites (smoke, sparks, stars) | Same reasoning as above — fine as abstract VFX filler. |

## 7. UI candidates

| Pack | Style | Contents |
|---|---|---|
| **UI Pack - Sci-fi** | Flat vector | Blue-toned panels/bars/buttons/cursors, sci-fi-styled — same pack already flagged in [MARIO_RESKIN_EXECUTION.md §7.3](MARIO_RESKIN_EXECUTION.md#73-ui-fonts-sfx-music), confirmed present in this local bundle too. HUD chrome is more style-tolerant than game-world sprites (mixing flat UI over pixel-art gameplay is common and accepted in real shipped games), so the vector/pixel mismatch matters less here than in §1-§2. |
| **Cursor Pixel Pack** | Pixel | 220 individually-sliced pixel-art cursors/glyph icons (arrows, hands, tools, letters) — genuine pixel style, but glyphs/cursors, not panel/bar chrome. |
| **UI Pixel Pack** | Pixel | **Better HUD chrome match than UI Pack - Sci-fi** — real 9-slice pixel-art panels/buttons/bars in white/yellow/green/red/blue plus a tan/brown set, confirmed by opening its preview (not just the name). Since HUD elements sit directly next to the pixel-art game world (score/lives readout, pause panel), this is the more consistent choice; UI Pack - Sci-fi's blue tech-panel *theming* is nice but its vector rendering will look softer/rounder next to sharp pixel sprites. |
| **Mobile Controls** | Flat vector (icon glyphs), but style-tolerant like other UI chrome | **Strong candidate for the on-screen joystick/action buttons** — `MarioConfiguration`'s own doc comment already flags these as needing consistent on-screen sizing regardless of camera zoom, and this pack ships exactly that: a joystick base+knob and D-pad/action buttons in 8 alternate visual styles (A-H), including a yellow neon-outline "sci-fi HUD" look (hexagon/circle/cross buttons) that fits Ampere's identity well without needing new art. |

## 8. Fonts — closes §16.5's "might not need original art" question with a concrete pick

The bundle has a **shared `Other/Fonts/` folder with 15 `.ttf` files**, not tied to any
one pack (missed by the `assets.json` manifest scan — its `folders` array is empty for
this entry, only found by listing the actual directory). Checked by opening
`Other/Fonts/Preview.png`, which renders all 15:

| Font | Look | Fit |
|---|---|---|
| **Kenney Blocks** | Genuinely blocky/8-bit pixelated | **Best match for the in-game HUD digit font** (`font`'s 48 frames, §16.5) — the only one of the 15 that's actually pixel-chunky rather than just "techy-looking" |
| Kenney Pixel / Kenney Pixel Square | A rounded sans-serif despite the name — not actually blocky | Skip for the HUD despite the promising name; confirmed by the preview image, not assumed |
| Kenney Space / Kenney Rocket / Kenney Rocket Square | Angular sci-fi display faces | Good for the **"Ampere's Run" title screen / menu headers** — bigger, more stylized text where a display font (not a tiny bitmap-style one) reads better |
| Kenney Mini / Kenney Mini Square | Small, compact | Backup option for dense HUD text if Kenney Blocks reads too chunky at small sizes in practice |

**Recommendation:** Kenney Blocks for `font` (in-game score/coin/lives digits), Kenney
Space or Kenney Rocket for the title/menu screen. Zero new art needed, matching
[MARIO_GAME_MECHANICS.md §16.5](MARIO_GAME_MECHANICS.md#165-assets-that-might-not-need-original-art-at-all)'s
own suggestion to skip commissioning font art entirely.

## 9. Audio candidates — closes real gaps in the earlier sourcing pass

[MARIO_RESKIN_EXECUTION.md §2.6/§7.3](MARIO_RESKIN_EXECUTION.md#26-audio-music) flagged
looping background music as needing an external download (HydroGene/Tallbeard on itch.io),
and §2.5 mapped SFX against generic Kenney packs without checking a sci-fi-specific one
closely. **This bundle covers both locally, no extra downloads needed.**

**DONE 2026-09-12**: both the music and SFX plans below were executed as written (exact
per-key file picks in `docs/assets/mario-audio/build_audio.py`), converted `.ogg` → `.wav`
via Python's `soundfile` (no `ffmpeg` available in this environment, but `soundfile`'s
bundled `libsndfile` decodes OGG Vorbis directly — closes this section's own open
question). See [MARIO_RESKIN_EXECUTION.md Step R.6](MARIO_RESKIN_EXECUTION.md#step-r6--audio)
for the full write-up, including the one deviation from the plan below (Castle/Star/Sea
pulled from the general `Loops` pool instead of `Retro`, since Retro's remaining 3 tracks
after Ground/UnderGround were all lighthearted — a poor mood fit) and the still-open
caveat that none of this has been confirmed by an actual listen-through yet.

### 6.1 Looping music (5 needed: Ground/UnderGround/Castle/Star/Sea)

| Pack | Contents |
|---|---|
| **Music Loops** | 3 subfolders: `Loops/` (19 general-purpose loop-able tracks), `Retro/` (5 explicitly retro-chiptune-styled tracks — Retro Beat/Comedy/Mystic/Polka/Reggae), `Idents/` (5 short stingers) |

**Recommendation:** check `Music Loops/Retro/`'s 5 tracks against the 5 mood slots first
(chiptune fits the identity best) — this closes §2.6's gap with zero new downloads.
Can't verify by ear from here (no audio playback in this pass) — actually listen before
committing a specific track to a specific level mood.

### 6.2 One-shot SFX — mapped against all 23 sound keys (`MARIO_GAME_MECHANICS.md §15.2`)

Current files are `.wav`; everything in this bundle is `.ogg` — confirm the engine's
sound loader accepts `.ogg` before committing to a swap (not checked in this pass).

| Original key | Best local match | Source pack |
|---|---|---|
| `smb_jump-small`/`smb_jump-super` | `phaseJump1-5.ogg` | Digital Audio (literally named for this) |
| `smb_coin` | `powerUp1-12.ogg` / `highUp.ogg` (short blip) | Digital Audio |
| `smb_fireball` | `laserRetro_000-004.ogg` | Sci-Fi Sounds |
| `smb_bowserfire` | `laserLarge_000-004.ogg` | Sci-Fi Sounds |
| `smb_bowserfalls` | `explosionCrunch_000-004.ogg` | Sci-Fi Sounds |
| `smb_breakblock`/`smb_bump`/`smb_kick` | `impactMetal_000-004.ogg` | Sci-Fi Sounds |
| `smb_stomp` | `impactGeneric_light_000-004.ogg` | Impact Sounds |
| `smb_powerup`/`smb_powerup_appears` | `forceField_000-004.ogg` | Sci-Fi Sounds |
| `smb_pipe` | `doorOpen_000-002.ogg`/`doorClose_000-002.ogg` | Sci-Fi Sounds |
| `smb_flagpole` | `phaserDown1-3.ogg` (descending whoosh) | Digital Audio |
| `smb_pause` | `switch1-33.ogg` or `click1-5.ogg` | UI Audio |
| `smb_1-up`/`smb_gameover`/`smb_stage_clear`/`smb_world_clear`/`smb_mariodie`/`smb_fireworks` | pick from `jingles-retro_00-16.ogg` | **Music Jingles** (Retro style — already local, not the external pack §2.5 assumed was needed) |
| `smb_vine`/`smb_warning` | — | dead code in the original too (loaded, never played) — skip per §16.1's own note, no replacement needed |

**Recommendation:** Sci-Fi Sounds covers the sci-fi-specific hits (lasers, force fields,
metal impacts, doors) better than the generic Digital/Impact Audio packs §2.5 originally
leaned on — use it as the primary source, falling back to Digital Audio for
jump/coin/UI-adjacent sounds it doesn't cover, and Music Jingles' Retro set for every
fanfare-style one-shot instead of composing/finding those separately.

## 10. For the other two games in this app (secondary — not this reskin's scope)

Noted since they turned up during the scan, not because anything here is being acted on:

- **Tank Pack** (`2D assets/Tank Pack/`) — flat vector top-down tanks in 4 factions,
  turret rotation, damage decals. If `battlecity` is ever reskinned, this is a plausible
  source, but its own current art style would need checking against this pack's vector
  look first, same style-matching discipline as above.
- **Tappy Plane** (`2D assets/Tappy Plane/`) — a purpose-built Flappy-Bird-clone kit
  (plane, obstacle terrain, GET READY/GAME OVER banners, letters). Flat vector. Direct
  thematic fit if `flappybird` is ever reskinned, style permitting.

## 11. Summary / next action / what still genuinely needs AI-gen or custom art

- Best immediate win: recolor **Pixel Platformer Industrial Expansion**'s girder/panel
  pieces for Fortress's terrain (Step R.3/R.4 follow-up), since it's already pixel-art
  and already industrial-themed — no recolor even strictly required, though matching the
  established per-theme accent colors would keep it consistent with the other 4 themes.
- **Both previously-unsolved theme gaps now have real candidates:** Substrate ←
  **Tiny Dungeon**'s stone/door tiles + RTS Sci-fi's pipes/blobs as decoration; Flooded
  Sector ← **Roguelike Dungeon Pack**'s existing blue palette, reframed as flooded
  tunnels rather than literal underwater. Neither needed further searching once actually
  opened and checked against the Pixel-family style filter.
- **Fonts are fully solved with zero new art:** Kenney Blocks (HUD digits) + Kenney
  Space/Rocket (title/menu) from the bundle's shared `Other/Fonts/` folder.
- **HUD chrome:** prefer UI Pixel Pack over UI Pack - Sci-fi for style consistency with
  the pixel-art game world; Mobile Controls for the on-screen joystick/action buttons
  (has an on-theme neon-outline sci-fi style built in).
- **Audio is now mostly solved locally:** Music Loops/Retro for the 5 looping tracks
  (needs an actual listen to confirm mood fit — not done in this pass), Sci-Fi Sounds as
  the primary one-shot SFX source (better thematic fit than the generic packs §2.5
  originally leaned on), Music Jingles' Retro set for every fanfare-style one-shot
  (1-up/game-over/stage-clear/world-clear/mariodie/fireworks) instead of composing those
  separately. Note: bundle audio is `.ogg`, current assets are `.wav` — confirm the
  engine's loader accepts `.ogg` before committing to any audio swap.
- **Pixel Shmup**'s ship sprites are a plausible source for a flying enemy/mini-boss-jet
  replacement later (R.4) — noted, not yet needed.
- Nothing here changes the Step R.0 player/enemy/boss decision (Robot Master Series
  stays) — this bundle has no pixel-art character set to compete with it.
- **Mechanical note for whoever integrates any of the above:** Industrial Expansion and
  Tiny Dungeon both ship as individually-sliced `tile_NNNN.png` files (112 and 132
  respectively) plus a packed tilemap sheet — picking specific tiles is a matter of
  opening a few candidates by index, not manually cropping a packed sheet the way the
  coin extraction attempt struggled with earlier in Step R.3.
- Power-ups: 3 of 4 (Battery cell, Overclock chip, Charge coil) solved directly from
  Robot Master Series' own `other/` folder (§2) — same pack, zero style risk. Spare
  chassis (1-Up) has no clean thematic match anywhere scanned; falling back to Robot
  Platform Pack's heart icon.
- Bricks/mechanisms (§3) and scenery (§4): pipes and castle-replacement buildings solved
  via RTS Sci-fi; Sea/Substrate backdrop textures solved via Robot Master Series' own
  unused background sections; generic parallax (mountains/clouds) via Kenney's
  silhouette-style "Background Elements" pack (not "Remastered").

### What this exhaustive pass could NOT find anywhere (robot packs + Kenney bundle) —
### the actual residual scope for AI-gen or quick custom art:

1. **The handful of enemies with no clean silhouette match** — `PiranhaPlant`,
   `OctoPussy`, `FishyWater`/`FishyGround`, `SonOfABuitch`, and any other odd-shaped
   enemy R.4 turns up without a Robot Master Series or Kenney equivalent. This is the
   category [MARIO_RESKIN_EXECUTION.md Step R.4](MARIO_RESKIN_EXECUTION.md#step-r4--remaining-enemies-bricks-items-hazards-lifts)
   already flagged as reasonable AI-first-draft-plus-cleanup candidates — nothing in this
   scan changes that.
2. **Trees** — no pixel-art tree asset in either robot pack or in-style within Kenney
   (the style-matched "Background Elements" pack has silhouettes for mountains/clouds
   but its tree shapes are only in the mismatched "Remastered" vector pack). Recommend
   the antenna/pylon reframe (§4) first — it needs no art search at all — and reach for
   AI-gen only if that reframe doesn't feel right to the team.
3. **Spare chassis** and **Axe/lever** — both single small icons with no match; low
   enough effort to hand-pixel directly rather than spin up AI-gen for two icons.
4. **Whatever else R.4's per-enemy/per-brick pass turns up** as it works through
   [MARIO_GAME_MECHANICS.md §16.4](MARIO_GAME_MECHANICS.md#164-reskin-priority-by-on-screen-frequency)'s
   tiers 3-4 — this scan covered tier-1/2 priorities plus items/scenery/audio/UI/fonts,
   not an exhaustive per-asset pass over all ~130 sheets.

**Net effect on the R.0 AI-gen decision:** [MARIO_RESKIN_EXECUTION.md's R.0](MARIO_RESKIN_EXECUTION.md#step-r0--decisions-blocking-everything-else)
already picked **Retro Diffusion**, scoped to "long-tail scenery/background dressing and
the Substrate/Flooded Sector recolor-and-dress work, not hero assets." This scan has
since *closed* the Substrate/Flooded Sector part of that scope with real Kenney assets
(§1) — so Retro Diffusion's actual remaining job is narrower than R.0 first assumed:
primarily item 1 above (the odd-shaped enemies with no match), and optionally item 2
(trees) if the pylon reframe isn't wanted. Its commercial tier still isn't
purchased/activated — that's the one real blocker left before R.4 can reach for it, but
the scope needing it has shrunk, not grown, since R.0.

## 12. Integration playbook — concrete steps per category

Written so a fresh session tomorrow can execute mechanically without re-deriving any of
the above. §12.1 is already done (today's terrain swap) and documents the actual working
pattern the rest of these steps follow.

### 12.1 Terrain (DONE 2026-09-08 — reference pattern for the rest)

Script: `docs/assets/mario-sprites/ampere-staging/build_terrain_from_kenney.py`.

1. For a packed-tilemap source (Industrial Expansion, Tiny Dungeon), compute the grid
   pitch as `native_tile_px + 1` (Kenney sheets use 1px spacing between cells) and confirm
   `cols * pitch - 1 == sheet_width` — if it doesn't divide evenly, the tile size or col
   count assumption is wrong.
2. Render a labeled grid overlay (`ImageDraw.line`/`.text` over an 8x-upscaled copy of the
   tilemap) to read off exact `(row, col)` → index visually — this is what turned "which
   tile looks right" into a concrete number, and is much more reliable than eyeballing the
   pack's `Preview.png` composite.
3. Crop `tile_from_grid(sheet, tile_px, cols, index)`, then `Image.resize((32,32),
   Image.NEAREST)` — never a smoothing filter, it'll blur pixel art.
4. Optional: `tint(frame, color, strength)` (alpha-preserving color blend) to nudge an
   off-the-shelf tile toward an established theme accent, as done for Substrate.
5. Save directly into `docs/assets/mario-sprites/reskin-source/<ExactAssetSpecFilename>`
   — filenames must match `PackMarioAtlas.ASSETS` exactly (case-sensitive).
6. `bash tools/mario-atlas-packer/pack.sh` (rebuilds atlases via the reskin-overlay
   mechanism), then `./gradlew :app:compileDebugJavaWithJavac` — a clean compile plus the
   packer's own "N regions across the same page count as before" log line is the signal
   nothing else broke.

### 12.2 Power-up items — same pattern, different sources

Targets (`PackMarioAtlas.ASSETS` exact cols×rows): `mashroom` (1×1), `mashrooms` (2×1),
`flower` (4×1), `star` (4×1), `one_up` (2×1).

1. **Battery cell** (`mashroom.png`/`mashrooms.png`): crop from
   `robot_series_base_pack/other/miscel.png` — the battery icon occupies roughly the top
   two rows of that 80×48 image (6 near-identical variants, each about 24×16px based on a
   6-across layout — confirm exact bounds with `getbbox()` per crop before finalizing,
   the way the Scuttler/coin fixes did). Scale to fill ~85% of a 32×32 cell, same
   `place_content`-style helper as `build_terrain_and_common.py` already has.
2. **Overclock chip** (`star.png`, 4 frames): crop the green chip/gem icon from the
   bottom row of the same `miscel.png` — it already has color-shine variants across its
   4 copies, good raw material for a 4-frame "sparkle" cycle without extra work.
3. **Charge coil** (`flower.png`, 4 frames): crop one segment of the coil column from
   `robot_series_base_pack/other/gate.png` (208×32, a repeating multi-segment strip) —
   since it's a repeating pattern, the same crop works for all 4 frames, or offset the
   crop by one repeat-unit per frame for a subtle "spinning" look.
4. **Spare chassis** (`one_up.png`, 2 frames): no sourced match — either crop
   `Robot_Platform_Pack/Tileset&Items.png`'s heart icon (pragmatic fallback, see §2) or
   hand-pixel a small chassis/head icon at 32×32 directly (single small sprite, low
   effort either way).

### 12.3 Bricks/mechanisms

Targets: `pump`/`pump_top` (1×1 each, + `_castle`/`_sea` theme variants), `small_castle`/
`big_castle` (1×1 each), `bouncer` (1×1), `spring` (3×1).

1. **Pump/pipes**: crop a straight and a corner segment from **RTS Sci-fi**'s
   `Tilesheet/scifi_tilesheet.png` conduit-pipe network (top-right quadrant of that sheet,
   the light-grey winding path pieces) — grid it the same way as §12.1 first to get exact
   indices (not yet done — this pack's tilesheet wasn't grid-mapped in this pass, only
   visually surveyed).
2. **small_castle/big_castle**: crop one of RTS Sci-fi's small tech/factory buildings
   (top-right of the same tilesheet, the grey structures with red/blue accent lights) —
   same grid-mapping step needed first.
3. **Bouncer/Spring**: reuse the Charge coil crop from §12.2 (a coil doubles as a spring
   visually) — no new source needed.

### 12.4 Scenery/parallax backgrounds

Targets: `sea_background` (1×1, tiled every 32px per its own `BackgroundBand` doc),
`mountain`/`clouds`/`fence`/`fence2` (1×1 each, tiled every 1536px).

1. **Sea backdrop**: crop `robot_series_base_pack/other/platform.png`'s "BACKGROUND 2"
   section (the cyan wave-water texture, roughly y=128-288 in that 320×592 sheet per
   §4's earlier row-band scan) — check its actual tile width against what
   `BackgroundBand`'s Sea-specific constructor overload expects before assuming a direct
   drop-in.
2. **Substrate decoration**: same file's "BACKGROUND 1" section (circuit-panel texture,
   y=0-112) as a layered decorative element behind Tiny Dungeon's terrain (§1) — not a
   like-for-like swap of `mountain`/`clouds`, additional dressing.
3. **Generic Mountain/Clouds/Fence**: crop silhouette shapes from Kenney's
   **"Background Elements"** (not "Remastered" — confirmed by opening both previews, see
   §4), then tint toward a dark navy/space palette so it reads as a distant
   cityscape/station skyline rather than literal mountains.
4. **Trees**: build the antenna/pylon reframe procedurally (same technique as §1's
   tiles) rather than continuing to search — no pack anywhere scanned has a style-matched
   tree.

### 12.5 Fonts — needs a small code change, not just an asset drop-in

**Important finding from this pass**: the game's shared text font
(`gameengine/src/main/assets/skin/default/{default.fnt,uiskin.json,uiskin.png}`) is a
**libGDX BitmapFont pair (.fnt descriptor + texture page), not a raw system font loaded
at runtime** — confirmed by reading `MarioResourceManager.uiSkin()`/`uiSkinYDown()`.
Kenney's `Other/Fonts/Kenney Blocks.ttf` can't just be dropped into `assets/` and picked
up; it has to be **converted into that same .fnt+page bitmap format first** (a tool like
Hiero/BMFont, or a scripted equivalent, rendering each needed glyph to a texture page and
emitting the `.fnt` XML/text descriptor libGDX's `BitmapFont` reads).

**Also important**: `skin/default/` is a **shared engine-level resource** — confirmed via
grep, both `flappybird` and `mario` (`MarioResourceManager`, `flappybird/ui/DesignWindow`,
`flappybird/ui/OptionWindow`) load the exact same path. **Overwriting it in place would
re-theme flappybird's UI too**, which is out of this reskin's scope. The correct approach:
create a new `skin/mario/` (or similarly Mario-specific) asset path with its own
`.fnt`/`uiskin.json`/texture page built from Kenney Blocks, and change
`MarioResourceManager.uiSkin()`/`uiSkinYDown()` to load from that new path instead of the
shared default — a small, localized code change (two method bodies), not a mutation of
shared engine assets. Kenney Space/Rocket for the title screen text is simpler (used
directly by whatever draws the menu title, likely without going through the
BitmapFont/Skin system at all — check `MarioMenuScreen`/`WorldLevelSelectScreen` before
assuming).

### 12.6 UI chrome and on-screen controls

1. **HUD panels/bars** (pause overlay, any panel background `StatusBar`/`PauseOverlay`
   draw): swap in **UI Pixel Pack**'s 9-slice panel/button pieces
   (`UI assets/UI Pixel Pack/Spritesheet/UIpackSheet_transparent.png`) — check how
   `platformer.hud.PauseOverlay`/`StatusBar` currently source their background
   drawables (likely via `uiSkin()`'s own `Drawable`s) before assuming a simple texture
   swap; may need new `Skin` entries alongside §12.5's font work rather than a separate
   change.
2. **On-screen joystick/action buttons**: `MarioConfiguration.CONTROLLER_TEXTURES` (see
   `MarioResourceManager.java` around its `CONTROLLER_TEXTURES` array) names the current
   texture keys — swap in **Mobile Controls**' style-A or similar neon-outline joystick
   base/knob and action-button PNGs at matching filenames/sizes. Confirm the exact pixel
   dimensions `CONTROLLER_TEXTURES`' own doc comment expects (the joystick must stay a
   constant on-screen size regardless of camera zoom, per that doc) before swapping.

### 12.7 Audio

1. **Format check first**: current files are `.wav` (`app/src/main/assets/mario/audio/`),
   Kenney's are `.ogg`. Confirm `com.guidebee.game.audio`'s `Sound`/`Music` loader accepts
   `.ogg` on this engine before converting anything — if not, convert Kenney's `.ogg`
   files to `.wav` (e.g. via `ffmpeg -i in.ogg out.wav`) rather than assuming.
2. **SFX**: copy the specific files named in §9.2's table, rename to the matching
   `smb_*` key (e.g. `Sci-Fi Sounds/laserRetro_000.ogg` → `smb_fireball.wav`), drop into
   `app/src/main/assets/mario/audio/` — call sites read the constant key, not the
   filename convention, so any rename is safe as long as `MarioResourceManager`'s own
   `SOUND_EFFECTS` mapping (if it maps key→filename explicitly) or direct filename
   convention (if key IS the filename minus extension) is respected — check which before
   renaming in bulk.
3. **Music**: same process for the 5 `MUSIC_TRACKS` keys, sourced from `Music
   Loops/Retro/` — **actually listen to the 5 tracks first** to assign moods sensibly
   (Ground/UnderGround/Castle/Star/Sea), this pass could only catalog filenames, not
   judge audio content.

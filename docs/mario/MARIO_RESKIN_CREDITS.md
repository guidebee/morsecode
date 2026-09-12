# Mario Reskin — Licensing/Attribution Tracker

Per [MARIO_RESKIN_EXECUTION.md §6](MARIO_RESKIN_EXECUTION.md#6-licensingattribution-tracker),
this is the running record of source/license/attribution per shipped reskin asset. This
file was referenced by several earlier entries in that doc before it actually existed —
created now, backfilled from those entries plus the audio work that prompted it.

## Image sources

| Source | License | Used for | Attribution required? |
|---|---|---|---|
| [Robot Master Series – Base Asset Pack](https://au-pixel.itch.io/robotbasepack) (AU_pixel) | Pay-what-you-want (free tier). Commercial + non-commercial OK, editable; **no resale/repackage/redistribution, no logo/trademark/NFT use.** | Ampere (player), Scuttler/Roller (`enemy1`/`enemy2`), Helmet family (`enemy3`), The Warden (`miniboss1`), Battery cell/Overclock chip/Charge coil (`other/miscel.png`/`gate.png`), Explosion (`other/explode-Sheet`) | Not legally required by the license; credited here for the team's own record. |
| [Robot Platform Pack](https://edusilvart.itch.io/robot-platform-pack) (edusilvart) | Free ("name your own price"). Commercial use OK, **no resale.** | Originally scoped for the Bolt/gear coin and Spare-chassis 1-Up — **superseded**: the coin ended up procedural (see Step R.3's own write-up, extraction from this pack's tileset caused a real shipped bug) and the 1-Up ended up hand-pixelled instead of using this pack's heart icon. Not actually used in the final asset tree. | N/A |
| Kenney "Game Assets All-in-1" bundle (local, `C:\workspace\Kenney_Game_Assets_All`) | **CC0 1.0** — confirmed by reading the bundle's own `Readme.html`: "you may use these graphics in personal and commercial projects," credit "nice but is not mandatory." | Terrain source reference (Pixel Platformer Industrial Expansion/Tiny Dungeon/Roguelike Dungeon Pack, per Step R.3); background/scenery research (not directly extracted in the final tier-3/4 pass — see `MARIO_RESKIN_EXECUTION.md`'s R.4 write-up, most tier-3/4 scenery ended up procedural instead). | No, CC0. |
| Procedural (hand-drawn via Pillow, this repo's own `docs/assets/mario-sprites/ampere-staging/*.py` scripts) | N/A — original work-for-hire, owned outright. | The large majority of tier-1 through tier-4 world/mechanism/scenery/enemy assets — terrain tiles, Iron, QuestionMark, Boss/BossFire, 1UP, all tier-3/4 mechanisms (pumps, axe, wall, rocket launcher, bouncer/spring, connective pieces), all tier-3/4 scenery (castles, tree/pylon, parallax backdrops, flags/banners), and the one-off enemies with no clean pack match (Monkey, SonOfABuitch, SpikeyEgg, Spikey, FishGrey/FishRed, OctoPussy). | No. |

## Audio sources

All from the same local Kenney "Game Assets All-in-1" bundle's `Audio/` folder — **CC0
1.0**, same bundle-wide license confirmed above (its `Readme.html` doesn't carve out a
different license for the audio packs). Converted from the bundle's native `.ogg` to
`.wav` via Python's `soundfile` (no re-licensing implication — a format conversion of
CC0 material stays CC0). See `docs/assets/mario-audio/build_audio.py` for the exact
per-key source file mapping.

| Pack | Used for |
|---|---|
| Sci-Fi Sounds | `smb_fireball`, `smb_bowserfire`, `smb_bowserfalls`, `smb_breakblock`, `smb_bump`, `smb_kick`, `smb_powerup`, `smb_powerup_appears`, `smb_pipe` |
| Digital Audio | `smb_jump-small`, `smb_jump-super`, `smb_coin`, `smb_flagpole` |
| Impact Sounds | `smb_stomp` |
| UI Audio | `smb_pause` |
| Music Jingles (Retro) | `smb_1-up`, `smb_gameover`, `smb_stage_clear`, `smb_world_clear`, `smb_mariodie`, `smb_fireworks` |
| Music Loops (Retro + general Loops) | `Ground`, `UnderGround`, `Castle`, `Star`, `Sea` (the 5 looping level/mood tracks) |

Not replaced: `smb_vine`/`smb_warning` (dead code — loaded, never played, in the
original game too) stay on the original Nintendo-derived `.wav` files. No licensing
entry needed for those; they're unchanged.

## Declined paths (recorded per §6's own instruction to log "checked, fine" and
## rejections alike)

- **Path B (LPC/CC-BY-SA family assets)** — declined entirely, including for
  Substrate/Flooded Sector background dressing (see `MARIO_RESKIN_EXECUTION.md` Step
  R.0's own write-up). Not worth the ShareAlike obligation once Robot Master Series +
  procedural art covered the same ground cleanly.
- **AI-assisted generation (Retro Diffusion, the R.0-picked tool)** — never actually
  purchased/activated, and no image-generation tool of any kind is reachable from this
  working environment. Every asset originally scoped for "AI-gen or quick custom art"
  (the odd-shaped enemies, the antenna/pylon reframe, Spare chassis, Axe) ended up
  hand-drawn procedurally instead — see `MARIO_RESKIN_EXECUTION.md`'s R.4 tier-3/4
  write-up for the specific substitution reasoning per asset.

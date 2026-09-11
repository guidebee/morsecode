# Mario docs

Everything about the in-app Mario port lives in this directory. Suggested reading order
depends on what you're doing:

## I want to play it / understand it as a game
- **[MARIO_PLAYER_GUIDE.md](MARIO_PLAYER_GUIDE.md)** — controls, power-ups, enemy field
  guide, a world tour. Start here if you're new to the game itself.
- **[MARIO_LEVEL_ATLAS.md](MARIO_LEVEL_ATLAS.md)** — every one of the 55 levels, with a
  schematic minimap, a data breakdown, and design notes (pits/pacing) for each, plus
  (§13) a complete level-JSON format reference for authoring brand-new levels.

## I want to extend or fix the code
- **[MARIO_GAME_MECHANICS.md](MARIO_GAME_MECHANICS.md)** — the as-built technical
  reference: physics, actors, collision, the level-data pipeline, the sprite-sheet/atlas
  system, and step-by-step recipes for adding new levels/enemies/bricks. **Start here.**
- **[PLATFORMER_ENGINE_ARCHITECTURE.md](PLATFORMER_ENGINE_ARCHITECTURE.md)** — a proposed
  restructuring that extracts the reusable "2D platformer" layer out of today's
  Mario-specific code, so a *future, different* platform game can be built on it the way
  Mario is built on the Guidebee Game Engine. Read this if you're planning new gameplay
  infrastructure, not just new Mario content.
- **[PLATFORMER_ENGINE_IMPLEMENTATION.md](PLATFORMER_ENGINE_IMPLEMENTATION.md)** — the
  literal, file-by-file runbook for actually carrying out the above: exact classes, exact
  call sites (grep-verified against the real codebase), phase by phase. This is what to
  follow when implementation actually starts — decided to run in full before the reskin,
  see [MARIO_RESKIN_PLAN.md §5](MARIO_RESKIN_PLAN.md#5-ordering-phase-2--platformer-re-architecture--reskin-as-sequential-passes).
- **[MARIO_PORT_PLAN.md](MARIO_PORT_PLAN.md)** and
  **[MARIO_PORT_PLAN_PHASE2.md](MARIO_PORT_PLAN_PHASE2.md)** — the historical record of
  how the port was actually built, step by step, with the design rationale behind
  decisions the mechanics doc now just states as fact. Read these for *why*, not *what*.

## I'm working on the visual reskin
- **[MARIO_RESKIN_PLAN.md](MARIO_RESKIN_PLAN.md)** — the decisions and rationale: new
  identity direction, the `ART_SCALE` resolution architecture, ordering, risks.
- **[MARIO_RESKIN_EXECUTION.md](MARIO_RESKIN_EXECUTION.md)** — the actionable companion:
  real, license-checked asset/tool resources and an ordered step-by-step task list.
- **[KENNEY_ALL_IN_ONE_INDEX.md](KENNEY_ALL_IN_ONE_INDEX.md)** — the detailed resource
  mapping: which local pack/file/tile-index supplies which game asset, plus a §12
  integration playbook (exact crop coordinates, target filenames, code-change notes).
- **[MARIO_RESKIN_JUNIOR_DEV_GUIDE.md](MARIO_RESKIN_JUNIOR_DEV_GUIDE.md)** — **start here
  if you're picking up Step R.4 onward as a task list.** The hands-on recipe, a shared
  Python helper module, a "mistakes hall of fame" from real bugs already hit, and
  ready-to-execute tasks.

## Generated assets

`assets/mario-sprites/` and `assets/mario-levels/` hold sprite crops, category montages,
and level minimaps generated from the shipped game data for use in the docs above —
regenerable from `app/src/main/assets/mario*` at any time (see
[MARIO_GAME_MECHANICS.md §13.5](MARIO_GAME_MECHANICS.md#135-how-to-regenerate-the-atlases)
and [MARIO_LEVEL_ATLAS.md](MARIO_LEVEL_ATLAS.md)'s own note on its minimap generator),
not hand-maintained.

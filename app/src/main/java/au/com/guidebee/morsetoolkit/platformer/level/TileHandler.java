package au.com.guidebee.morsetoolkit.platformer.level;

import au.com.guidebee.morsetoolkit.activity.mario.level.LevelDefinition;

/**
 * Spawns whatever one {@link LevelDefinition.Tile} type produces into the
 * world/context - see {@link TileTypeRegistry}. Most handlers are a one-line
 * lambda, matching a loader's existing per-type dispatch case exactly (see
 * PLATFORMER_ENGINE_ARCHITECTURE.md §3.3).
 *
 * <p><b>Known scope compromise:</b> this interface (and {@link TileTypeRegistry})
 * live here in {@code platformer.level} so a future second game could reuse
 * the registry/dispatch *mechanism*, but they still reference Mario's own
 * {@link LevelDefinition} type directly, since generalizing that schema
 * itself is explicitly out of this implementation plan's scope (see
 * PLATFORMER_ENGINE_IMPLEMENTATION.md's Phase B2 notes and "What's
 * deliberately not in this plan" - building an actual second consumer is
 * Phase H, run on its own schedule). A genuinely reusable version would need
 * {@code LevelDefinition}/{@code LevelCatalog} generalized too; until then,
 * the toolkit-vs-content boundary here is intentionally soft.
 */
public interface TileHandler {
    void spawn(LevelDefinition.Tile tile, LevelDefinition level, int tileSize);
}

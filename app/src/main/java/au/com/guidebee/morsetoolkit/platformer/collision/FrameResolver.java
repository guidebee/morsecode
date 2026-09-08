package au.com.guidebee.morsetoolkit.platformer.collision;

import au.com.guidebee.morsetoolkit.platformer.core.TileWorld;

/**
 * One interaction-pair check, run once per frame - see {@link CollisionPipeline}.
 * Most implementations are a lambda closing over a game's own player/level
 * fields rather than reading this method's own {@code world} parameter,
 * since a concrete resolver usually wants a game-specific world subclass
 * (or, for something like a teleport check, no world at all) - see
 * PLATFORMER_ENGINE_ARCHITECTURE.md §3.4.
 */
public interface FrameResolver {
    void resolve(TileWorld world);
}

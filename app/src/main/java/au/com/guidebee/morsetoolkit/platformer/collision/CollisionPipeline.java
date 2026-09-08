package au.com.guidebee.morsetoolkit.platformer.collision;

import java.util.List;

import au.com.guidebee.morsetoolkit.platformer.core.TileWorld;

/**
 * Formalizes the existing "one static method per interaction pair, called in
 * a fixed order once per frame" convention (see docs/MARIO_GAME_MECHANICS.md
 * §3) as a real, tiny runner instead of one hardcoded call per resolver. A
 * game builds one of these once (typically in its screen's constructor,
 * after its player/world exist) and calls {@link #resolveAll} once per
 * frame - see PLATFORMER_ENGINE_ARCHITECTURE.md §3.4.
 */
public final class CollisionPipeline {

    private final List<FrameResolver> resolvers;

    public CollisionPipeline(FrameResolver... resolvers) {
        this.resolvers = List.of(resolvers);
    }

    public void resolveAll(TileWorld world) {
        for (FrameResolver resolver : resolvers) {
            resolver.resolve(world);
        }
    }
}

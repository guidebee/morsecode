package au.com.guidebee.morsetoolkit.activity.mario.world;

import com.guidebee.game.microedition.Layer;
import com.guidebee.game.microedition.LayerManager;

import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.state.GameStateController;
import au.com.guidebee.morsetoolkit.platformer.core.GameContext;

/**
 * Static holder for the current level's {@link LayerManager}, {@link MarioWorld}
 * and {@link GameStateController}, so actors spawned dynamically at runtime
 * (item reveals popping a Mushroom/Flower/Star/Life, a broken brick's
 * replacement Iron, a coin credit, etc.) can reach all three without every
 * actor constructor needing them threaded through by hand.
 *
 * <p>This mirrors an existing pattern in this codebase - Battle City's
 * {@code Tank}/{@code Bullet}/{@code Explosion} classes each have their own
 * static {@code setBattleField}/{@code setLayerManager} setters for the same
 * reason - just consolidated into one holder instead of duplicated per class.
 * Safe because exactly one level/screen is active at a time and
 * {@link #init} is called fresh by every new {@code MarioGameScreen}.
 *
 * <p>A thin wrapper (not a subclass - see {@link GameContext}'s own doc for
 * why) around one private {@link GameContext} instance parameterized for
 * Mario's own {@link Player}/{@link MarioWorld}/{@link GameStateController}
 * types - every method here just forwards to it, so every existing call site
 * keeps its exact static-call shape.
 */
public final class MarioContext {

    private static final GameContext<Player, MarioWorld, GameStateController> CONTEXT = new GameContext<>();

    private MarioContext() {
    }

    public static void init(LayerManager layerManager, MarioWorld world, GameStateController gameState) {
        CONTEXT.init(layerManager, world, gameState);
    }

    public static MarioWorld world() {
        return CONTEXT.world();
    }

    /**
     * Set once {@code MarioGameScreen} constructs the level's {@code Player}
     * (after {@link #init}, which runs before the player exists yet - see
     * that method's caller). Needed by actors like {@code Boss} that read
     * the player's position directly every frame, matching the original
     * engine's own {@code Mario game} reference each enemy class held.
     */
    public static void setPlayer(Player player) {
        CONTEXT.setPlayer(player);
    }

    public static Player player() {
        return CONTEXT.player();
    }

    /** Score/coins/lives - see {@code MarioGamePlay#gameState()} for who owns the instance this returns. */
    public static GameStateController gameState() {
        return CONTEXT.gameState();
    }

    public static void spawn(Layer actor) {
        CONTEXT.spawn(actor);
    }
}

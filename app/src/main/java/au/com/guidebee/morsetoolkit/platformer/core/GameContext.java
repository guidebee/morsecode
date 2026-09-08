package au.com.guidebee.morsetoolkit.platformer.core;

import com.guidebee.game.microedition.Layer;
import com.guidebee.game.microedition.LayerManager;

/**
 * Generic per-level context: whichever {@link LayerManager}, world, game
 * state, and player a game needs is set once by {@link #init} and read back
 * by dynamically-spawned actors that don't have it threaded through by hand
 * (an item reveal popping a collectible, a broken brick's replacement, ...)
 * - see PLATFORMER_ENGINE_ARCHITECTURE.md §3.5.
 *
 * <p>A game declares its own static-holder wrapper (e.g. Mario's own
 * {@code MarioContext}) around one private instance of this class,
 * parameterized with its own player/world/game-state types, and forwards its
 * own static call sites to it - composition, not inheritance: a subclass
 * couldn't give its own static {@code init}/{@code world}/etc. methods the
 * same names as this class's instance methods (Java doesn't allow a static
 * method to hide an inherited instance method of the same signature), and
 * those exact names are what every existing call site
 * ({@code MarioContext.world()}, {@code .spawn(...)}, ...) needs to keep.
 */
public class GameContext<TPlayer, TWorld extends TileWorld, TGameState> {

    private LayerManager layerManager;
    private TWorld world;
    private TGameState gameState;
    private TPlayer player;

    public void init(LayerManager layerManager, TWorld world, TGameState gameState) {
        this.layerManager = layerManager;
        this.world = world;
        this.gameState = gameState;
        this.player = null;
    }

    public TWorld world() {
        return world;
    }

    /** Set once a game constructs the level's player (after {@link #init}, which runs before the player exists yet). */
    public void setPlayer(TPlayer player) {
        this.player = player;
    }

    public TPlayer player() {
        return player;
    }

    public TGameState gameState() {
        return gameState;
    }

    public void spawn(Layer actor) {
        layerManager.append(actor);
    }
}

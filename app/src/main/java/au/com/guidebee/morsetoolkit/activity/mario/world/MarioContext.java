package au.com.guidebee.morsetoolkit.activity.mario.world;

import com.guidebee.game.microedition.Layer;
import com.guidebee.game.microedition.LayerManager;

import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.state.GameStateController;

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
 */
public final class MarioContext {

    private static LayerManager layerManager;
    private static MarioWorld world;
    private static GameStateController gameState;
    private static Player player;

    private MarioContext() {
    }

    public static void init(LayerManager layerManager, MarioWorld world, GameStateController gameState) {
        MarioContext.layerManager = layerManager;
        MarioContext.world = world;
        MarioContext.gameState = gameState;
        MarioContext.player = null;
    }

    public static MarioWorld world() {
        return world;
    }

    /**
     * Set once {@code MarioGameScreen} constructs the level's {@code Player}
     * (after {@link #init}, which runs before the player exists yet - see
     * that method's caller). Needed by actors like {@code Boss} that read
     * the player's position directly every frame, matching the original
     * engine's own {@code Mario game} reference each enemy class held.
     */
    public static void setPlayer(Player player) {
        MarioContext.player = player;
    }

    public static Player player() {
        return player;
    }

    /** Score/coins/lives - see {@code MarioGamePlay#gameState()} for who owns the instance this returns. */
    public static GameStateController gameState() {
        return gameState;
    }

    public static void spawn(Layer actor) {
        layerManager.append(actor);
    }
}

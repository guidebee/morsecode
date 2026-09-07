package au.com.guidebee.morsetoolkit.activity.mario;

import com.guidebee.game.GamePlay;

import au.com.guidebee.morsetoolkit.activity.mario.level.LevelCatalog;
import au.com.guidebee.morsetoolkit.activity.mario.level.LevelDefinition;
import au.com.guidebee.morsetoolkit.activity.mario.screen.MarioGameScreen;
import au.com.guidebee.morsetoolkit.activity.mario.screen.MarioMenuScreen;
import au.com.guidebee.morsetoolkit.activity.mario.state.GameStateController;

/**
 * Mario Game Play. Owns cross-screen state and shared asset loading, following
 * the same pattern as {@code FlappyBirdGamePlay}/{@code BattleCityGamePlay}.
 *
 * <p>{@link #gameState} is the one piece of state that survives a level
 * swap within a single playthrough (score/coins/lives/pause - see
 * {@code GameStateController}) - {@link #goToLevel} (checkpoint transitions)
 * carries it forward as-is, while {@link #startLevel} (a fresh pick from
 * {@link MarioMenuScreen}) resets it.
 */
public class MarioGamePlay extends GamePlay {

    private final MarioGameActivity gameActivity;
    private final GameStateController gameState = new GameStateController();

    public MarioGamePlay(MarioGameActivity activity) {
        gameActivity = activity;
    }

    @Override
    public void create() {
        MarioResourceManager.loadCommon();
        setScreen(new MarioMenuScreen(this));
    }

    public void finish() {
        gameActivity.backToMainActivity();
    }

    /** Score/coins/lives/pause for whichever level is currently active - see the class doc. */
    public GameStateController gameState() {
        return gameState;
    }

    /**
     * World-1 levels a fresh {@link #startLevel} pick might need to arrive
     * at from "the previous level's own checkpoint" - see that method's doc.
     * 14 isn't included: nothing in v1 transitions *into* it except 13's own
     * checkpoint, so it never needs to be searched as a *source*.
     */
    private static final int[] PREDECESSOR_LEVELS = {11, 12, 13};

    /**
     * Starts a brand-new game at the given level - see {@code MarioMenuScreen}.
     *
     * <p>Every level's own {@code pos} field is dead data in the original
     * engine (never read by {@code Mario.java} - the shipped game only ever
     * reaches a level via a checkpoint's saved arrival tile) and was only
     * ever verified correct for Level 11's own geometry, the one level that
     * truly has no incoming checkpoint to instead use. Levels 12-14 all
     * happen to share that exact same literal {@code pos} value (copy-pasted
     * across the original's level classes) - which for Level 14 lands
     * squarely inside its castle's own solid floor block, spawning Mario
     * stuck inside a wall when the level-select menu jumps straight to it
     * instead of arriving there normally via Level 13's flagpole. Selecting a
     * level from the menu now spawns at whatever tile the natural checkpoint
     * chain would have used instead (see {@link #findArrivalTile}), falling
     * back to the level's own {@code pos} only when no such checkpoint exists
     * (Level 11).
     */
    public void startLevel(int levelNumber) {
        gameState.reset();
        int[] arrival = findArrivalTile(levelNumber);
        int spawnTileX = arrival != null ? arrival[0] : -1;
        int spawnTileY = arrival != null ? arrival[1] : -1;
        setScreen(new MarioGameScreen(levelNumber, this, spawnTileX, spawnTileY));
    }

    /** @return {locX, locY}, or null if no other World-1 level's checkpoint leads into {@code levelNumber}. */
    private static int[] findArrivalTile(int levelNumber) {
        for (int predecessor : PREDECESSOR_LEVELS) {
            if (predecessor == levelNumber) {
                continue;
            }
            LevelDefinition level = LevelCatalog.load(predecessor);
            for (LevelDefinition.Checkpoint checkpoint : level.checkpoints) {
                if (checkpoint.nextLevel == levelNumber) {
                    return new int[]{checkpoint.locX, checkpoint.locY};
                }
            }
        }
        return null;
    }

    /** Back to the level-select menu - a paused game's "quit", or a game over. */
    public void goToMenu() {
        setScreen(new MarioMenuScreen(this));
    }

    /**
     * Switches to another level at a specific spawn tile - how a checkpoint
     * (level-end flag, pipe entrance) actually finishes the transition it
     * starts; see {@code MarioGameScreen}'s level-completion state machine
     * and docs/MARIO_PORT_PLAN.md Step 7.1.
     *
     * <p>Level 12's own data includes checkpoints pointing at other worlds'
     * warp-zone secrets (levels 21/31/41) that v1 doesn't ship any JSON for
     * (see docs/MARIO_PORT_PLAN.md's World-1-only scope) - loading such a
     * level throws, which is caught here so finding one of those pipes just
     * silently does nothing instead of crashing.
     *
     * @return true if the switch happened
     */
    public boolean goToLevel(int levelNumber, int spawnTileX, int spawnTileY) {
        MarioGameScreen next;
        try {
            next = new MarioGameScreen(levelNumber, this, spawnTileX, spawnTileY);
        } catch (RuntimeException e) {
            return false;
        }
        setScreen(next);
        return true;
    }
}

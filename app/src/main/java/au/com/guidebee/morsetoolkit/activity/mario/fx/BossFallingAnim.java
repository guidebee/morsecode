package au.com.guidebee.morsetoolkit.activity.mario.fx;

import com.guidebee.game.microedition.Sprite;

import au.com.guidebee.morsetoolkit.activity.mario.MarioConfiguration;
import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.actors.bricks.Axe;
import au.com.guidebee.morsetoolkit.activity.mario.actors.enemies.Boss;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioContext;

/**
 * The boss's death-fall once the axe cuts the bridge out from under him,
 * ported from {@code Animations/BossFallingAnim.java}: holds a 2-frame pose
 * for a delay, plays "smb_bowserfalls" just before dropping, then falls
 * straight down and removes itself (and {@link #ghost}) once clear of the
 * level. {@link #spawnCollapse} is the entry point - it also lays out the 13
 * {@link BridgeBlackout} tiles and the {@link MarioGhost} stand-in, matching
 * the original's own {@code Mario.RemoveBridge}.
 *
 * <p>Only reachable while {@link Boss#isActive()} is still true at the moment
 * the axe is touched - see {@code MarioGameScreen}'s axe-handling doc for why
 * a boss already killed by fireballs/a star skips this whole sequence (it
 * already played its own {@code smb_bowserfalls}/death reaction via
 * {@code Boss#die}).
 */
public class BossFallingAnim extends Sprite {

    /** Ported from {@code Bricks/BridgeBloks} always being exactly 13 tiles wide in every castle level (confirmed by reading every converted level JSON). */
    private static final int BRIDGE_LENGTH_TILES = 13;
    /** Ported from the original's own `(26-i)*5` - tiles closer to the axe blacken first, sweeping back toward Mario. */
    private static final float DELAY_STEP_TICKS = 5f;
    private static final float DELAY_BASE_TICKS = 2 * BRIDGE_LENGTH_TILES;

    private static final int FRAME_WIDTH = 64;
    private static final int FRAME_HEIGHT = 64;
    private static final float PHYSICS_FPS = 60f;
    private static final float FRAME_INTERVAL = 0.1f;
    private static final float INITIAL_DELAY_TICKS = 180f;
    private static final float ROAR_TICKS_BEFORE_FALL = 2f;
    private static final float FALL_SPEED = 3f;
    private static final float FALL_OUT_MARGIN_PX = 200f;

    private final MarioGhost ghost;
    private float delayTicks = INITIAL_DELAY_TICKS;
    private float frameTimer;
    private boolean showingFirstFrame = true;
    private boolean falling;
    private boolean roared;

    private BossFallingAnim(float x, float y, MarioGhost ghost) {
        super(MarioResourceManager.region("boss"), FRAME_WIDTH, FRAME_HEIGHT);
        setPosition(x, y);
        this.ghost = ghost;
    }

    /** Ported from {@code Mario.RemoveBridge} - the axe's own touch handler is the only caller (see class doc). */
    public static void spawnCollapse(Axe axe, Boss boss, Player player) {
        boss.deactivate();

        MarioGhost ghost = new MarioGhost(player);
        MarioContext.spawn(ghost);

        int tileSize = MarioConfiguration.TILE_SIZE;
        int startTileX = Math.round(axe.getX() / tileSize) - BRIDGE_LENGTH_TILES;
        // The bridge deck sits 2 tiles below the axe in every castle level
        // (confirmed by reading every converted level JSON) - the original's
        // own equivalent is a literal `10*32`, kept relative here instead.
        float bridgeY = axe.getY() + 2 * tileSize;
        for (int i = 0; i < BRIDGE_LENGTH_TILES; i++) {
            float delay = DELAY_BASE_TICKS - i * DELAY_STEP_TICKS;
            MarioContext.spawn(new BridgeBlackout((startTileX + i) * tileSize, bridgeY, delay));
        }

        MarioContext.spawn(new BossFallingAnim(boss.getX(), boss.getY(), ghost));
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        float frames = delta * PHYSICS_FPS;

        if (falling) {
            setY(getY() + FALL_SPEED * frames);
            if (getY() > MarioContext.world().getHeightPx() + FALL_OUT_MARGIN_PX) {
                ghost.remove();
                remove();
            }
            return;
        }

        delayTicks -= frames;
        if (!roared && delayTicks <= ROAR_TICKS_BEFORE_FALL) {
            roared = true;
            MarioResourceManager.sound("smb_bowserfalls").play();
        }
        if (delayTicks <= 0) {
            falling = true;
            return;
        }

        frameTimer += delta;
        if (frameTimer >= FRAME_INTERVAL) {
            frameTimer = 0;
            showingFirstFrame = !showingFirstFrame;
            setFrame(showingFirstFrame ? 4 : 5);
        }
    }
}

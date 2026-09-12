package au.com.guidebee.morsetoolkit.activity.mario.fx;

import com.guidebee.game.microedition.Sprite;

import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.actors.bricks.Axe;
import au.com.guidebee.morsetoolkit.activity.mario.actors.enemies.Boss;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioContext;
import au.com.guidebee.morsetoolkit.platformer.input.PlatformerCommand;

/**
 * The boss's death-fall once the axe cuts the bridge out from under him,
 * ported from {@code Animations/BossFallingAnim.java}: holds a 2-frame pose
 * for a delay, plays "smb_bowserfalls" just before dropping, then falls
 * straight down and, once clear of the level, removes itself and puts the
 * real {@link Player} under forced auto-walk right - ported from the
 * original's own {@code update()}, whose {@code getY() > 700} branch calls
 * {@code game.player.MoveForward()}. {@link #spawnCollapse} is the entry
 * point - it also lays out the 13 {@link BridgeBlackout} tiles, matching the
 * original's own {@code Mario.RemoveBridge}.
 *
 * <p>Confirmed by reading the source: the original's {@code RemoveBridge()}
 * itself never touches the player at all - he isn't sent walking until
 * *this* class's fall finishes. {@code MarioGameScreen#triggerAxe} matches
 * that: it only freezes him (an empty forced command) at axe-touch time, so
 * he stays standing exactly where the boss was, right up until this class
 * hands him the real walk command. An earlier revision of this sequence
 * spawned a frozen "ghost" stand-in at the real Player's position for the
 * whole collapse, the way {@code Mario.java}'s own {@code DemoMario} does -
 * ported faithfully at first, but on-device testing showed it reads as a
 * visible duplicate Mario for the whole animation rather than the original's
 * fleeting, easy-to-miss overlap (the original's camera can scroll the real,
 * *already-walking* player away from the stand-in almost immediately; this
 * port's real Player instead stays frozen in place - see this class's own
 * "why frozen, not walking yet" doc above - so nothing ever separates the
 * two visually). Since the frozen real Player already looks exactly like a
 * standing Mario on his own, the stand-in added nothing here and was
 * removed rather than patched to stay hidden.
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

    private static final float PHYSICS_FPS = 60f;
    private static final float FRAME_INTERVAL = 0.1f;
    private static final float INITIAL_DELAY_TICKS = 180f;
    private static final float ROAR_TICKS_BEFORE_FALL = 2f;
    private static final float FALL_SPEED = 3f;
    /**
     * How far past its own starting Y the boss falls before this is removed -
     * generously larger than any castle level's own height (the tallest,
     * Level 14/64, is 800px total), so this always clears the visible level
     * regardless of which one is playing.
     *
     * <p>Deliberately measured from {@link #startY} (this sprite's own spawn
     * position), not {@code MarioContext.world().getHeightPx()} the way an
     * earlier version of this class did: that read shared, static, mutable
     * state every single frame across the several seconds this fall takes,
     * for a check that's purely cosmetic ("has this scrolled off screen yet")
     * and never needed level-specific accuracy in the first place - a needless
     * dependency, and the prime suspect for an on-device bug where the fall
     * stayed on screen and never got cleaned up. A fixed distance computed
     * once at construction removes that dependency entirely.
     */
    private static final float FALL_DISTANCE_PX = 1000f;

    private final Player player;
    private final float startY;
    private float delayTicks = INITIAL_DELAY_TICKS;
    private float frameTimer;
    private boolean showingFirstFrame = true;
    private boolean falling;
    private boolean roared;

    private BossFallingAnim(float x, float y, Player player, int tileSize) {
        super(MarioResourceManager.region("boss"), tileSize * 2, tileSize * 2);
        setPosition(x, y);
        this.startY = y;
        this.player = player;
    }

    /** Ported from {@code Mario.RemoveBridge} - the axe's own touch handler is the only caller (see class doc). */
    public static void spawnCollapse(Axe axe, Boss boss, Player player, int tileSize) {
        boss.deactivate();

        int startTileX = Math.round(axe.getX() / tileSize) - BRIDGE_LENGTH_TILES;
        // The bridge deck sits 2 tiles below the axe in every castle level
        // (confirmed by reading every converted level JSON) - the original's
        // own equivalent is a literal `10*32`, kept relative here instead.
        float bridgeY = axe.getY() + 2 * tileSize;
        for (int i = 0; i < BRIDGE_LENGTH_TILES; i++) {
            float delay = DELAY_BASE_TICKS - i * DELAY_STEP_TICKS;
            MarioContext.spawn(new BridgeBlackout((startTileX + i) * tileSize, bridgeY, delay, tileSize));
        }

        MarioContext.spawn(new BossFallingAnim(boss.getX(), boss.getY(), player, tileSize));
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        float frames = delta * PHYSICS_FPS;

        if (falling) {
            setY(getY() + FALL_SPEED * frames);
            if (getY() > startY + FALL_DISTANCE_PX) {
                PlatformerCommand walkForward = new PlatformerCommand();
                walkForward.right = true;
                player.setForcedCommand(walkForward);
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

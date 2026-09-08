package au.com.guidebee.morsetoolkit.activity.mario.actors.items;

import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioContext;

/**
 * A coin placed directly in level data, ported from {@code Objects/Coin.java}
 * - distinct from {@code fx.CoinPopEffect} (the ballistic "coin get" popup a
 * hit Brick/Bank/QuestionMark spawns): this one just sits in place, spinning,
 * until touched from any side, matching the original's identical
 * {@code MarioJumpedOnEnemy}/{@code EnemyJumperOnMario} bodies (stomp and
 * side-touch both just collect it - it's not a real enemy, it only
 * implemented that interface for the original's collision wiring).
 *
 * <p>Skips the original's one special case - {@code CollidedWithJumping_Brick}
 * (touched while jumping up into it) additionally spawns a {@code CoinAnim}
 * popup on top of crediting the coin - a cosmetic-only difference, the same
 * kind of minor animation nuance this port already skips elsewhere (see
 * {@code Brick}'s skipped bonk-jitter).
 *
 * <p>The "coin" region is 32x32 per frame, 3 frames - confirmed against
 * {@code tools/mario-atlas-packer}'s own {@code AssetSpec("coin", "Coin.png", 3, 1)}.
 * Frame sequence ({@code 0,0,0,0,1,2,1,0} at 150ms/frame) ported verbatim
 * from the original's own {@code setAnimationFrame(int[])} call.
 */
public class Coin extends CollectibleItem {

    private static final float FRAME_INTERVAL = 0.15f;
    private static final int[] FRAME_SEQUENCE = {0, 0, 0, 0, 1, 2, 1, 0};

    private float animTimer;
    private int sequenceIndex;

    public Coin(float x, float y, int tileSize) {
        super(MarioResourceManager.region("coin"), tileSize, tileSize, x, y);
        setFrame(FRAME_SEQUENCE[0]);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!isActive()) {
            return;
        }
        animTimer += delta;
        if (animTimer >= FRAME_INTERVAL) {
            animTimer = 0;
            sequenceIndex = (sequenceIndex + 1) % FRAME_SEQUENCE.length;
            setFrame(FRAME_SEQUENCE[sequenceIndex]);
        }
    }

    @Override
    public void onCollected(Player player) {
        MarioResourceManager.sound("smb_coin").play();
        MarioContext.gameState().addCoin();
        collect();
    }
}

package au.com.guidebee.morsetoolkit.activity.mario.fx;

import com.guidebee.game.microedition.Sprite;

import au.com.guidebee.morsetoolkit.activity.mario.MarioConfiguration;
import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.world.MarioContext;

import java.util.Random;

/**
 * The random celebratory burst at the castle-arrival checkpoint, ported from
 * {@code Animations/FireWorks.java}: a 3-frame explosion (each frame held
 * twice, matching the original's own {@code {0,0,1,1,2,2}} sequence) that
 * replays at a new random spot near the checkpoint - between 4 and 7 times
 * (also matching the original's own {@code getRandom(4,7)}) - before removing
 * itself. {@code MarioGameScreen}'s own celebration only spawns one of these
 * 50% of the time, matching {@code Player_CheckPoint.collided}'s own {@code
 * Random().nextBoolean()} gate.
 */
public class Fireworks extends Sprite {

    private static final int[] FRAME_SEQUENCE = {0, 0, 1, 1, 2, 2};
    private static final float FRAME_INTERVAL = 0.1f;
    private static final int MIN_BURSTS = 4;
    private static final int MAX_BURSTS = 7;
    private static final int SPREAD_X = 128;
    private static final int MIN_TILE_Y = 3;
    private static final int MAX_TILE_Y = 7;

    private static final Random RANDOM = new Random();

    private final int baseX;
    private final int tileSize;
    private int remainingBursts;
    private float frameTimer;

    private Fireworks(int baseX, int tileSize) {
        super(MarioResourceManager.region("explosion"),
                tileSize * MarioConfiguration.ART_SCALE, tileSize * MarioConfiguration.ART_SCALE);
        setSize(tileSize, tileSize);
        setFrameSequence(FRAME_SEQUENCE);
        this.baseX = baseX;
        this.tileSize = tileSize;
        remainingBursts = MIN_BURSTS + RANDOM.nextInt(MAX_BURSTS - MIN_BURSTS + 1);
        reposition();
    }

    public static void spawnAt(float checkpointX, int tileSize) {
        MarioContext.spawn(new Fireworks((int) checkpointX, tileSize));
    }

    private void reposition() {
        float x = baseX - SPREAD_X + RANDOM.nextInt(2 * SPREAD_X + 1);
        float y = (MIN_TILE_Y + RANDOM.nextInt(MAX_TILE_Y - MIN_TILE_Y + 1)) * tileSize;
        setPosition(x, y);
        setFrame(0);
        frameTimer = 0;
        MarioResourceManager.sound("smb_fireworks").play();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        frameTimer += delta;
        if (frameTimer < FRAME_INTERVAL) {
            return;
        }
        frameTimer = 0;
        boolean wrapped = getFrame() == getFrameSequenceLength() - 1;
        nextFrame();
        if (!wrapped) {
            return;
        }
        remainingBursts--;
        if (remainingBursts < 0) {
            remove();
        } else {
            reposition();
        }
    }
}

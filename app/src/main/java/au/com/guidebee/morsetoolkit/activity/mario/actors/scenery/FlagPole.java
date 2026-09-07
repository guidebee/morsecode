package au.com.guidebee.morsetoolkit.activity.mario.actors.scenery;

import com.guidebee.game.graphics.Batch;
import com.guidebee.game.graphics.TextureRegion;
import com.guidebee.game.microedition.Layer;

import au.com.guidebee.morsetoolkit.activity.mario.MarioConfiguration;
import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;
import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;

/**
 * The level-end flagpole's cloth pennant, ported from {@code Animations
 * .FlagTop.java}. The rod (a thin 4x288 strip, {@code Bricks/Flag.java}'s own
 * art) and the ball ornament above it are plain, never-moving {@link Scenery}
 * spawned alongside this by {@code LevelLoader#spawnScenery}'s own "Flag"
 * case; this class is the one part that slides down to the rod's foot once
 * {@link #startSliding()} is called (see {@code MarioGameScreen}'s
 * level-completion state machine), and the one part whose bounding box
 * {@link #overlaps} tests against for the original's own {@code Player_Flag
 * .collided} touch - widened to the rod's full 9-tile height (matching that
 * collision's real hitbox, the thin rod sprite itself) despite this cloth
 * only ever occupying a single tile of it.
 *
 * <p>Every level that has one places its "Flag" tile at the same relative
 * spot (confirmed by reading every converted level JSON), so the rod's fixed
 * 9-tile height and this cloth's fixed slide distance need no per-level
 * tuning, matching the original's own hardcoded {@code 11*32} stop line.
 */
public class FlagPole extends Layer {

    private static final int POLE_HEIGHT_TILES = 9;
    private static final float SLIDE_SPEED = 3f;
    private static final float PHYSICS_FPS = 60f;
    /**
     * The real NES game's flagpole-height score tiers (lowest grab to
     * highest) - not ported from this project's own reference source, which
     * has no working score system to match at all (every score-related line
     * in {@code Mario.java}'s {@code DrawScore} and {@code Player.java}'s
     * {@code IncreaseLife} is commented out, confirmed by reading both), so
     * this is a deliberate extension of the port's own already-added scoring
     * (see {@code ScoreHud}), not a fidelity port.
     */
    private static final int[] HEIGHT_BONUS_SCORES = {100, 400, 800, 2000, 5000};

    private final TextureRegion cloth = MarioResourceManager.region("flag_top");
    private final float stopY;
    private final float touchX;
    private final float touchTop;
    private final float touchBottom;
    private boolean sliding;

    public FlagPole(int tileX, int tileY) {
        super(tileX * (float) MarioConfiguration.TILE_SIZE - 16f,
                tileY * (float) MarioConfiguration.TILE_SIZE,
                MarioConfiguration.TILE_SIZE, MarioConfiguration.TILE_SIZE, true);
        int tileSize = MarioConfiguration.TILE_SIZE;
        float baseX = tileX * tileSize;
        float baseY = tileY * tileSize;
        touchX = baseX;
        touchTop = baseY;
        touchBottom = baseY + POLE_HEIGHT_TILES * tileSize;
        stopY = baseY + (POLE_HEIGHT_TILES - 1) * tileSize;
    }

    /** Ported from {@code Player_Flag.collided}'s own contact test - the rod's full height, not just this cloth sprite's current position. */
    public boolean overlaps(Player player) {
        int tileSize = MarioConfiguration.TILE_SIZE;
        return player.getX() < touchX + tileSize && player.getX() + player.getWidth() > touchX
                && player.getY() < touchBottom && player.getY() + player.getHeight() > touchTop;
    }

    public void startSliding() {
        sliding = true;
    }

    /** Higher up the pole at the moment of {@link #overlaps}'s own touch = a bigger bonus - see {@link #HEIGHT_BONUS_SCORES}'s own doc. */
    public int heightBonusScore(Player player) {
        float fraction = 1f - (player.getY() - touchTop) / (touchBottom - touchTop);
        fraction = Math.max(0f, Math.min(1f, fraction));
        int tier = Math.min(HEIGHT_BONUS_SCORES.length - 1, (int) (fraction * HEIGHT_BONUS_SCORES.length));
        return HEIGHT_BONUS_SCORES[tier];
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!sliding || getY() >= stopY) {
            return;
        }
        setY(Math.min(stopY, getY() + SLIDE_SPEED * delta * PHYSICS_FPS));
    }

    @Override
    public void paint(Batch g) {
        g.draw(cloth, getX(), getY(), getWidth(), getHeight());
    }
}

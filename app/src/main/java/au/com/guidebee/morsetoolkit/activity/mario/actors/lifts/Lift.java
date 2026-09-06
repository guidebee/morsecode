package au.com.guidebee.morsetoolkit.activity.mario.actors.lifts;

import com.guidebee.game.graphics.Batch;
import com.guidebee.game.graphics.TextureRegion;
import com.guidebee.game.microedition.Layer;

import au.com.guidebee.morsetoolkit.activity.mario.MarioConfiguration;
import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;

/**
 * A moving platform, ported from the original's five {@code Lifts.*} classes
 * (Lift_UpDown/Lift_LeftRight/Lift_LeftRightInvert/LiftUP/LiftDown) - unified
 * into one class with a {@link Motion} enum instead of five near-identical
 * classes, the same simplification {@code TurtleShell} already applied to
 * the original's two turtle-shell classes.
 *
 * <p>Not a tile: it moves, so it can't live in {@code MarioWorld}'s
 * {@code TiledLayer} grid, and it's never solid to the sides or from below
 * (unlike a brick) - {@code LiftCollisionResolver} only ever catches a
 * falling player from directly above and carries them along, matching the
 * original's {@code Player_Lift.collided}.
 *
 * <h2>Motion formulas</h2>
 * Ported from the original's {@code Lift_*.update()} methods, which read a
 * shared oscillating "clock" off {@code Mario.Distance}/{@code SlowDistance}/
 * {@code InvertDistance} (each incremented by a fixed amount once per engine
 * tick - the same "assume ~60fps, scale by frames" approach as
 * {@code Player}, see that class's doc). Each lift keeps its own phase
 * instead of sharing one global clock - nothing here needs multiple lifts to
 * stay in lockstep the way the original's shared fields happened to.
 * {@link Motion#UP}/{@link Motion#DOWN} travel continuously and wrap around a
 * fixed range centered on their spawn point, rather than replicating the
 * original's wrap thresholds tied to its fixed-size AWT canvas (meaningless
 * once the camera scrolls over a much larger world) - the level places
 * several instances spaced along one shaft (see {@code LevelLoader.spawnLifts})
 * so together they still read as a continuous conveyor.
 *
 * <h2>Why the platform image is tiled, not stretched</h2>
 * {@code Lift.png} is a single small (16x16) tile; the original's
 * {@code ImageUtil.TileImage(image, Points)} call built a wider platform by
 * repeating it {@code Points} times, not by scaling it - {@link #paint}
 * reproduces that by drawing the region side by side across the platform's
 * width instead of stretching one draw call over it (which would blur a
 * small source tile badly).
 */
public class Lift extends Layer {

    private static final float PHYSICS_FPS = 60f;
    private static final float VERTICAL_OSCILLATION_RATE = 0.015f;
    private static final float VERTICAL_OSCILLATION_AMPLITUDE = 5 * MarioConfiguration.TILE_SIZE;
    private static final float HORIZONTAL_OSCILLATION_RATE = 0.02f;
    private static final float HORIZONTAL_OSCILLATION_AMPLITUDE = 2 * MarioConfiguration.TILE_SIZE;
    private static final float TRAVEL_SPEED = 1.5f;
    private static final float TRAVEL_RANGE = 8 * MarioConfiguration.TILE_SIZE;

    /** A rider is considered "landing" while its feet are within this many pixels of the lift's top. */
    private static final float LANDING_TOLERANCE = MarioConfiguration.TILE_SIZE / 2f;

    public enum Motion {UP_DOWN, LEFT_RIGHT, LEFT_RIGHT_INVERT, UP, DOWN}

    private final Motion motion;
    private final float originX;
    private final float originY;
    private final TextureRegion region;

    private double phase;
    private float deltaX;

    public Lift(float x, float y, Motion motion, int widthTiles) {
        super(x, y, Math.max(1, widthTiles) * MarioConfiguration.TILE_SIZE, MarioConfiguration.TILE_SIZE / 2f, true);
        this.motion = motion;
        this.originX = x;
        this.originY = y;
        this.region = MarioResourceManager.region("lift");
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        float frames = delta * PHYSICS_FPS;
        float oldX = getX();

        switch (motion) {
            case UP_DOWN:
                phase += VERTICAL_OSCILLATION_RATE * frames;
                setY((float) (originY + Math.cos(phase) * VERTICAL_OSCILLATION_AMPLITUDE));
                break;
            case LEFT_RIGHT:
                phase += HORIZONTAL_OSCILLATION_RATE * frames;
                setX((float) (originX + Math.cos(phase) * HORIZONTAL_OSCILLATION_AMPLITUDE));
                break;
            case LEFT_RIGHT_INVERT:
                phase -= HORIZONTAL_OSCILLATION_RATE * frames;
                setX((float) (originX + Math.cos(phase) * HORIZONTAL_OSCILLATION_AMPLITUDE));
                break;
            case UP:
                setY(wrap(getY() - TRAVEL_SPEED * frames));
                break;
            case DOWN:
                setY(wrap(getY() + TRAVEL_SPEED * frames));
                break;
        }
        deltaX = getX() - oldX;
    }

    private float wrap(float y) {
        if (y < originY - TRAVEL_RANGE) {
            return originY + TRAVEL_RANGE;
        }
        if (y > originY + TRAVEL_RANGE) {
            return originY - TRAVEL_RANGE;
        }
        return y;
    }

    @Override
    public void paint(Batch g) {
        int nativeWidth = region.getRegionWidth();
        int height = (int) getHeight();
        for (int drawn = 0; drawn < getWidth(); drawn += nativeWidth) {
            g.draw(region, getX() + drawn, getY(), nativeWidth, height);
        }
    }

    /** This frame's horizontal movement, for carrying a rider - see {@code LiftCollisionResolver}. */
    public float getDeltaX() {
        return deltaX;
    }

    public float getTopY() {
        return getY();
    }

    /**
     * Whether a falling rectangle (given by its own position/size) is
     * landing on this platform's top surface right now - horizontal overlap,
     * plus its bottom edge sitting at (or just above) the platform's top
     * within {@link #LANDING_TOLERANCE}. Mirrors the original's per-frame
     * "snap onto whatever the lift's top is right now" behavior rather than
     * a swept collision test.
     */
    public boolean isLandingSpot(float x, float y, int width, int height) {
        boolean overlapsHorizontally = x < getX() + getWidth() && x + width > getX();
        if (!overlapsHorizontally) {
            return false;
        }
        float top = getTopY();
        float bottom = y + height;
        return bottom >= top - LANDING_TOLERANCE && bottom <= top + LANDING_TOLERANCE;
    }
}

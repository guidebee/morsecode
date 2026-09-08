package au.com.guidebee.morsetoolkit.activity.mario.actors.bricks;

import au.com.guidebee.morsetoolkit.activity.mario.MarioResourceManager;

/**
 * One solid cell of a tree's canopy top, ported from {@code Bricks/Tree.java}:
 * a plain, indestructible platform (like {@link Iron}) - {@code HitFromDown}
 * is a no-op in the original. Only the top row of a "tree" tile's
 * {@code lengthX x lengthY} footprint is ever solid; everything below is
 * purely decorative and non-collided (see {@code LevelLoader#spawnTree},
 * which is what actually decides that split - this class only knows how to
 * render one already-decided solid cell).
 *
 * <p>{@code capIndex} picks the left-cap/middle/right-cap frame (0/1/2 for
 * the "GreenAndTrees" palette, the only one any World-1 level's own
 * {@code type} uses - "OrangeAndMushroom" (frames 5/6/7) is still unported
 * until a level actually needs it, matching this port's usual "build what's
 * used" scoping; the black-and-white {@code CloudsNight} variant uses the
 * *same* 0/1/2 indices, just from the separate "bw_tree" strip - confirmed
 * against {@code Mario.java}'s own case 17).
 *
 * <p>The "tree"/"bw_tree" regions are each 32x32 per frame, 5 cols x 2 rows -
 * confirmed against {@code WholeGame.java}'s {@code getImages("tree.png", 5, 2)}.
 */
public class Tree extends InteractiveBrick {

    private static final int FRAME_LEFT_CAP = 0;
    private static final int FRAME_MIDDLE = 1;
    private static final int FRAME_RIGHT_CAP = 2;

    public Tree(float x, float y, int columnIndex, int lastColumnIndex, int tileSize) {
        this(x, y, columnIndex, lastColumnIndex, false, tileSize);
    }

    public Tree(float x, float y, int columnIndex, int lastColumnIndex, boolean blackAndWhite, int tileSize) {
        super(MarioResourceManager.region(blackAndWhite ? "bw_tree" : "tree"),
                tileSize, tileSize, x, y);
        int frame;
        if (columnIndex == 0) {
            frame = FRAME_LEFT_CAP;
        } else if (columnIndex == lastColumnIndex) {
            frame = FRAME_RIGHT_CAP;
        } else {
            frame = FRAME_MIDDLE;
        }
        setFrame(frame);
    }
}

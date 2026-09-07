package au.com.guidebee.morsetoolkit.activity.mario.level;

/**
 * The World-Level numbering scheme every menu-selectable level uses (e.g.
 * level 82 is "8-2") - shared by {@code MarioMenuScreen} (its own level-select
 * list) and {@code ScoreHud} (the persistent "WORLD X-Y" indicator ported
 * from {@code Mario.java}'s own {@code DrawScore}, which draws it throughout
 * gameplay, not just at a level's start).
 */
public final class LevelNumbering {

    /** Each world's main levels, in play order - World 8 alone has 8, not 4 (ends in the {@code Princess} finale). */
    public static final int[][] WORLD_LEVELS = {
            {11, 12, 13, 14},
            {21, 22, 23, 24},
            {31, 32, 33, 34},
            {41, 42, 43, 44},
            {51, 52, 53, 54},
            {61, 62, 63, 64},
            {71, 72, 73, 74},
            {81, 82, 83, 841, 842, 843, 844, 845},
    };

    private LevelNumbering() {
    }

    /** "1-1".."1-4"/"8-5" etc, or the raw level number if it's not one of {@link #WORLD_LEVELS} (a bonus-area level, which the original doesn't number this way either). */
    public static String label(int levelNumber) {
        for (int world = 0; world < WORLD_LEVELS.length; world++) {
            int[] levels = WORLD_LEVELS[world];
            for (int i = 0; i < levels.length; i++) {
                if (levels[i] == levelNumber) {
                    return (world + 1) + "-" + (i + 1);
                }
            }
        }
        return Integer.toString(levelNumber);
    }
}

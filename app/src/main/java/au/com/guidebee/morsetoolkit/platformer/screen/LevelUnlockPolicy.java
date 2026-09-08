package au.com.guidebee.morsetoolkit.platformer.screen;

/**
 * Whether a given level's button should be selectable on a {@link
 * WorldLevelSelectScreen} - see that class's own doc. The exact rule (which
 * earlier levels/worlds must be cleared first) is a game's own policy, not
 * something the generic screen decides.
 */
@FunctionalInterface
public interface LevelUnlockPolicy {
    boolean isUnlocked(int worldIndex, int levelNumber);
}

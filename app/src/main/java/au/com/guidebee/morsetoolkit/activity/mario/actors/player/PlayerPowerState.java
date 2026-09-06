package au.com.guidebee.morsetoolkit.activity.mario.actors.player;

/**
 * Mario's power state, matching the original engine's Player IDs (1=small,
 * 2=big, 3=fire). Only {@link #SMALL} is wired up in Step 4 (no items yet -
 * see docs/MARIO_PORT_PLAN.md Step 5 for growth/shrink); BIG/FIRE are defined
 * now since their atlas regions and dimensions are already known, so Step 5
 * only has to add the transition logic and animations, not this data.
 */
public enum PlayerPowerState {

    SMALL(32, 32, "player"),
    BIG(32, 64, "big_player"),
    FIRE(32, 64, "fire_player");

    public final int width;
    public final int height;
    public final String regionName;

    PlayerPowerState(int width, int height, String regionName) {
        this.width = width;
        this.height = height;
        this.regionName = regionName;
    }
}

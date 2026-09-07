package au.com.guidebee.morsetoolkit.activity.mario.collision;

import java.util.List;

import au.com.guidebee.morsetoolkit.activity.mario.actors.player.Player;
import au.com.guidebee.morsetoolkit.activity.mario.level.LevelDefinition;

/**
 * Same-level teleports (pipe warps), checked once per frame after movement.
 * Ported from {@code Collusion/Player_Teleport.java} +
 * {@code Teleport/SpriteTeleport.java}: the trigger is a 1-tile-wide,
 * 3-tile-tall invisible zone positioned at ({@code inX + 32, inY}) - not
 * exactly at {@code inX}, confirmed against {@code SpriteTeleport}'s own
 * {@code setLocation(teleport.getINx() + 32, teleport.INy)} - and touching it
 * only ever sets the player's *x* to {@code outX}; {@code y} is left alone
 * entirely, matching the original's own {@code p.setX(t.teleport.getOUTx())}
 * (every teleport pair in this port's data sits at the same floor height on
 * both ends, so this never needed to touch y). Distinct from
 * {@code CheckpointResolver}'s cross-level jumps - a teleport never swaps
 * levels, just repositions within the current one.
 */
public final class TeleportResolver {

    private static final int TRIGGER_X_OFFSET = 32;
    private static final int TRIGGER_WIDTH = 32;
    private static final int TRIGGER_HEIGHT = 96;

    private TeleportResolver() {
    }

    public static void resolve(List<LevelDefinition.TeleportLink> teleports, Player player) {
        float px = player.getX();
        float py = player.getY();
        int pw = (int) player.getWidth();
        int ph = (int) player.getHeight();

        for (LevelDefinition.TeleportLink teleport : teleports) {
            float tx = teleport.inX + TRIGGER_X_OFFSET;
            float ty = teleport.inY;
            boolean overlaps = px < tx + TRIGGER_WIDTH && px + pw > tx
                    && py < ty + TRIGGER_HEIGHT && py + ph > ty;
            if (overlaps) {
                player.setX(teleport.outX);
                return;
            }
        }
    }
}

package au.com.guidebee.morsetoolkit.activity.mario;

import com.guidebee.game.GameEngine;
import com.guidebee.game.audio.Music;
import com.guidebee.game.audio.Sound;
import com.guidebee.game.graphics.TextureAtlas;
import com.guidebee.game.graphics.TextureRegion;

import java.util.HashMap;
import java.util.Map;

/**
 * Loads and caches every World-1 texture region and sound/music asset via
 * {@code GameEngine.assetManager}. See docs/MARIO_PORT_PLAN.md Step 2.
 *
 * <p>Regions come from {@code mario.atlas}, built offline by
 * {@code tools/mario-atlas-packer} - see that tool's {@code ASSETS} table for
 * the source-PNG-to-region-name mapping and each region's original col x row
 * strip layout (needed by Step 3/4 to slice animation frames via
 * {@code new com.guidebee.game.microedition.Sprite(region, frameWidth, frameHeight)},
 * the same way the original engine's {@code Sprite(Image, width, height)} did).
 *
 * <p>Music tracks are keyed by the original engine's level {@code attribute}
 * string ("Ground"/"UnderGround"/"Castle"), matching {@code Mario.java}'s own
 * {@code amitsAudioPlayer.playMusic(CurrentLevel.attribute)} call, plus a
 * "Star" track for the starman-powerup jingle (played on pickup, not tied to
 * a level attribute).
 */
public final class MarioResourceManager {

    private static final String[] MUSIC_TRACKS = {
            "Ground", "UnderGround", "Castle", "Star"
    };

    private static final String[] SOUND_EFFECTS = {
            "smb_1-up", "smb_bowserfalls", "smb_bowserfire", "smb_breakblock", "smb_bump",
            "smb_coin", "smb_fireball", "smb_fireworks", "smb_flagpole", "smb_gameover",
            "smb_jump-small", "smb_jump-super", "smb_kick", "smb_mariodie", "smb_pause",
            "smb_pipe", "smb_powerup", "smb_powerup_appears", "smb_stage_clear", "smb_stomp",
            "smb_vine", "smb_warning", "smb_world_clear"
    };

    private static TextureAtlas atlas;
    private static final Map<String, Sound> SOUNDS = new HashMap<>();
    private static final Map<String, Music> MUSIC = new HashMap<>();

    private MarioResourceManager() {
    }

    public static void load() {
        GameEngine.assetManager.load("mario.atlas", TextureAtlas.class);
        for (String sfx : SOUND_EFFECTS) {
            GameEngine.assetManager.load(audioPath(sfx), Sound.class);
        }
        for (String track : MUSIC_TRACKS) {
            GameEngine.assetManager.load(audioPath(track), Music.class);
        }
        GameEngine.assetManager.finishLoading();

        atlas = GameEngine.assetManager.get("mario.atlas", TextureAtlas.class);
        for (String sfx : SOUND_EFFECTS) {
            SOUNDS.put(sfx, GameEngine.assetManager.get(audioPath(sfx), Sound.class));
        }
        for (String track : MUSIC_TRACKS) {
            MUSIC.put(track, GameEngine.assetManager.get(audioPath(track), Music.class));
        }
    }

    private static String audioPath(String name) {
        return "mario/audio/" + name + ".wav";
    }

    public static TextureRegion region(String name) {
        TextureRegion region = atlas.findRegion(name);
        if (region == null) {
            throw new IllegalArgumentException("No such mario.atlas region: " + name);
        }
        return region;
    }

    public static Sound sound(String name) {
        Sound sound = SOUNDS.get(name);
        if (sound == null) {
            throw new IllegalArgumentException("No such mario sound effect: " + name);
        }
        return sound;
    }

    /** @param attribute the level's theme, e.g. "Ground"/"UnderGround"/"Castle", or "Star". */
    public static Music music(String attribute) {
        Music music = MUSIC.get(attribute);
        if (music == null) {
            throw new IllegalArgumentException("No mario music track for attribute: " + attribute);
        }
        return music;
    }
}

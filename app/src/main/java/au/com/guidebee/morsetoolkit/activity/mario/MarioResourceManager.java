package au.com.guidebee.morsetoolkit.activity.mario;

import com.guidebee.game.GameEngine;
import com.guidebee.game.audio.Music;
import com.guidebee.game.audio.Sound;
import com.guidebee.game.graphics.Pixmap;
import com.guidebee.game.graphics.Texture;
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

    /**
     * The on-screen joystick + A/B button art - the same "Virtual Joystick"
     * asset set (style 08) Battle City loads for its own {@code GameController}
     * (see {@code BattleCityGameScene}'s constructor and
     * {@code au.com.guidebee.morsetoolkit.activity.battlecity.ResourceManager}),
     * sourced from the newer, organized {@code assets/controller/} tree rather
     * than Battle City's flat root-level copies of the same files.
     *
     * <p>This tree's art ships 4x the pixel size of Battle City's own copies
     * (e.g. 512x512 backgrounds vs. 128x128, verified by hand) - since
     * {@code GameController}/{@code Touchpad}/{@code ImageButton} all size
     * themselves to their drawable's native pixel dimensions (no built-in
     * scale knob), loading these as-is rendered a joystick 4x too big. Also,
     * unlike this tree's fully-opaque art, Battle City's copies bake in
     * partial transparency (alpha maxing out around 30%, verified by hand) -
     * {@link #controllerAlpha()} reproduces that instead of the source art's
     * full opacity. {@link #downscaledTexture} fixes the size by resampling
     * each PNG down at load time (once, cached here) rather than scaling the
     * widget post-construction, which would leave {@code GameController.layout()}'s
     * own button-offset math (computed from the same oversized native pixel
     * dimensions) placing button A/B off-screen.
     *
     * <p>The 4x figure above was tuned/verified by hand against
     * {@code MarioConfiguration.VIEWPORT_WIDTH} being {@link #REFERENCE_VIEWPORT_WIDTH}
     * - unlike every other on-screen control, this joystick's whole point is
     * to stay a constant, comfortably-tappable size regardless of how far the
     * camera is zoomed out (see that constant's own doc for why the viewport
     * size is tuned independently), so {@link #downscaledTexture} scales the
     * 4x figure itself by however much the viewport has grown or shrunk
     * since, rather than resampling to a fixed pixel size that would make the
     * joystick grow or shrink on screen right along with the world.
     */
    private static final String[] CONTROLLER_TEXTURES = {
            "controller/Backgrounds/Back_08.png",
            "controller/Joystick/Joystick_08.png",
            "controller/Buttons/Button_08_Normal_Shoot.png",
            "controller/Buttons/Button_08_Pressed_Shoot.png",
            "controller/Buttons/Button_08_Normal_Virgin.png",
            "controller/Buttons/Button_08_Pressed_Virgin.png",
    };

    private static final int CONTROLLER_ASSET_SCALE = 4;
    private static final int REFERENCE_VIEWPORT_WIDTH = 384;
    private static final float CONTROLLER_ALPHA = 0.3f;

    private static TextureAtlas atlas;
    private static final Map<String, Sound> SOUNDS = new HashMap<>();
    private static final Map<String, Music> MUSIC = new HashMap<>();
    private static final Map<String, Texture> CONTROLLER_TEXTURE_CACHE = new HashMap<>();

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
        // Not routed through GameEngine.assetManager - these need resampling
        // at load time, not just a file read (see CONTROLLER_TEXTURES' doc).
        for (String path : CONTROLLER_TEXTURES) {
            CONTROLLER_TEXTURE_CACHE.put(path, downscaledTexture(path));
        }
    }

    private static Texture downscaledTexture(String assetPath) {
        Pixmap source = new Pixmap(GameEngine.files.internal(assetPath));
        // See CONTROLLER_TEXTURES' doc: keeps the joystick's on-screen size
        // constant across viewport-zoom changes instead of shrinking/growing
        // it right along with the world.
        float scale = (MarioConfiguration.VIEWPORT_WIDTH / (float) REFERENCE_VIEWPORT_WIDTH) / CONTROLLER_ASSET_SCALE;
        int width = Math.max(1, Math.round(source.getWidth() * scale));
        int height = Math.max(1, Math.round(source.getHeight() * scale));
        Pixmap.setFilter(Pixmap.Filter.BiLinear);
        Pixmap scaled = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        scaled.drawPixmap(source, 0, 0, source.getWidth(), source.getHeight(), 0, 0, width, height);
        Texture texture = new Texture(scaled);
        source.dispose();
        scaled.dispose();
        return texture;
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

    /**
     * Looks up a "<base>"/"<base>_underground"/"<base>_castle" region by the
     * level's attribute, matching the original engine's repeated
     * if("Sea"/"Ground"/"UnderGround"/"Castle".equals(attribute)) branches
     * seen throughout {@code Mario.java}'s tile-spawning switch (World 1
     * never uses "Sea", so that case isn't included).
     */
    public static TextureRegion themedRegion(String base, String attribute) {
        if ("UnderGround".equals(attribute)) {
            return region(base + "_underground");
        }
        if ("Castle".equals(attribute)) {
            return region(base + "_castle");
        }
        return region(base);
    }

    /** @param path one of {@link #CONTROLLER_TEXTURES}. */
    public static Texture controllerTexture(String path) {
        Texture texture = CONTROLLER_TEXTURE_CACHE.get(path);
        if (texture == null) {
            throw new IllegalArgumentException("No such controller texture: " + path);
        }
        return texture;
    }

    /** How opaque to draw the on-screen joystick - see {@link #CONTROLLER_TEXTURES}' doc. */
    public static float controllerAlpha() {
        return CONTROLLER_ALPHA;
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

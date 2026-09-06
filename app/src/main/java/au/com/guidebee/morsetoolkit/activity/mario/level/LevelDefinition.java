package au.com.guidebee.morsetoolkit.activity.mario.level;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * One Mario level's static data: theme, tile layout, checkpoints and teleports.
 * Parsed from the JSON produced by {@code tools/mario-level-converter} out of
 * the original GTGE {@code Levels.*} classes — see docs/MARIO_PORT_PLAN.md §3.
 *
 * <p>This is pure data (no engine/rendering dependency), so it can be unit
 * tested without an Android runtime. Turning it into an actual game world is
 * {@code LevelLoader}'s job (docs/MARIO_PORT_PLAN.md Step 3).
 */
public final class LevelDefinition {

    /** One brick/enemy/item/etc. placement. Mirrors the original {@code Construct}. */
    public static final class Tile {
        public final String type;
        public final int x;
        public final int y;
        public final int lengthX;
        public final int lengthY;
        public final String extraInfo;
        public final int bridgeLength;
        public final int patrolLength;

        public Tile(String type, int x, int y, int lengthX, int lengthY,
                    String extraInfo, int bridgeLength, int patrolLength) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.lengthX = lengthX;
            this.lengthY = lengthY;
            this.extraInfo = extraInfo;
            this.bridgeLength = bridgeLength;
            this.patrolLength = patrolLength;
        }
    }

    /**
     * A level-transition trigger. {@code kind} is the original CheckPoints
     * subclass's simple name (e.g. "CheckPoints", "InsidePumpvertically",
     * "WhyYouDOThis") and decides its in-game behavior.
     *
     * <p>Note: {@code nextLevel} is not always a "normal" two-digit level
     * number — the original engine deliberately uses the char literals 'a'
     * (97) and 'b' (98) as level numbers for the two World-1 bonus areas, to
     * keep them out of the ordinary numeric range. Preserved as-is; see
     * {@code LevelCatalog} for how level numbers map to asset files.
     */
    public static final class Checkpoint {
        public final String kind;
        public final double x;
        public final double y;
        public final int nextLevel;
        public final int locX;
        public final int locY;

        public Checkpoint(String kind, double x, double y, int nextLevel, int locX, int locY) {
            this.kind = kind;
            this.x = x;
            this.y = y;
            this.nextLevel = nextLevel;
            this.locX = locX;
            this.locY = locY;
        }
    }

    /** A pipe/warp pair. Unused by any World-1 level, but part of the schema. */
    public static final class TeleportLink {
        public final int inX;
        public final int inY;
        public final int outX;
        public final int outY;

        public TeleportLink(int inX, int inY, int outX, int outY) {
            this.inX = inX;
            this.inY = inY;
            this.outX = outX;
            this.outY = outY;
        }
    }

    public final int levelNumber;
    public final String sourceClass;
    public final String backgroundColor;
    public final String time;
    public final String type;
    public final int posX;
    public final int posY;
    public final String backgroundImage;
    public final String attribute;
    public final int levelLength;
    public final boolean bombs;
    public final int bombsTurnOff;
    public final boolean flyingFishes;
    public final int flyingFishesLength;
    public final String levelName;
    public final List<Tile> tiles;
    public final List<Checkpoint> checkpoints;
    public final List<TeleportLink> teleports;

    private LevelDefinition(int levelNumber, String sourceClass, String backgroundColor,
                             String time, String type, int posX, int posY,
                             String backgroundImage, String attribute, int levelLength,
                             boolean bombs, int bombsTurnOff, boolean flyingFishes,
                             int flyingFishesLength, String levelName, List<Tile> tiles,
                             List<Checkpoint> checkpoints, List<TeleportLink> teleports) {
        this.levelNumber = levelNumber;
        this.sourceClass = sourceClass;
        this.backgroundColor = backgroundColor;
        this.time = time;
        this.type = type;
        this.posX = posX;
        this.posY = posY;
        this.backgroundImage = backgroundImage;
        this.attribute = attribute;
        this.levelLength = levelLength;
        this.bombs = bombs;
        this.bombsTurnOff = bombsTurnOff;
        this.flyingFishes = flyingFishes;
        this.flyingFishesLength = flyingFishesLength;
        this.levelName = levelName;
        this.tiles = tiles;
        this.checkpoints = checkpoints;
        this.teleports = teleports;
    }

    public static LevelDefinition parse(String json) throws JSONException {
        JSONObject root = new JSONObject(json);

        int posX = 0;
        int posY = 0;
        if (!root.isNull("pos")) {
            JSONObject pos = root.getJSONObject("pos");
            posX = pos.getInt("x");
            posY = pos.getInt("y");
        }

        List<Tile> tiles = new ArrayList<>();
        JSONArray tilesArray = root.getJSONArray("tiles");
        for (int i = 0; i < tilesArray.length(); i++) {
            JSONObject t = tilesArray.getJSONObject(i);
            tiles.add(new Tile(
                    t.getString("type"),
                    t.getInt("x"),
                    t.getInt("y"),
                    t.getInt("lengthX"),
                    t.getInt("lengthY"),
                    t.isNull("extraInfo") ? null : t.getString("extraInfo"),
                    t.getInt("bridgeLength"),
                    t.getInt("patrolLength")));
        }

        List<Checkpoint> checkpoints = new ArrayList<>();
        JSONArray checkpointsArray = root.getJSONArray("checkpoints");
        for (int i = 0; i < checkpointsArray.length(); i++) {
            JSONObject c = checkpointsArray.getJSONObject(i);
            checkpoints.add(new Checkpoint(
                    c.getString("kind"),
                    c.getDouble("x"),
                    c.getDouble("y"),
                    c.getInt("nextLevel"),
                    c.getInt("locX"),
                    c.getInt("locY")));
        }

        List<TeleportLink> teleports = new ArrayList<>();
        JSONArray teleportsArray = root.getJSONArray("teleports");
        for (int i = 0; i < teleportsArray.length(); i++) {
            JSONObject t = teleportsArray.getJSONObject(i);
            teleports.add(new TeleportLink(
                    t.getInt("inX"), t.getInt("inY"), t.getInt("outX"), t.getInt("outY")));
        }

        return new LevelDefinition(
                root.getInt("levelNumber"),
                root.getString("sourceClass"),
                root.isNull("backgroundColor") ? null : root.getString("backgroundColor"),
                root.isNull("time") ? null : root.getString("time"),
                root.isNull("type") ? null : root.getString("type"),
                posX, posY,
                root.isNull("backgroundImage") ? null : root.getString("backgroundImage"),
                root.isNull("attribute") ? null : root.getString("attribute"),
                root.getInt("levelLength"),
                root.getBoolean("bombs"),
                root.getInt("bombsTurnOff"),
                root.getBoolean("flyingFishes"),
                root.getInt("flyingFishesLength"),
                root.isNull("levelName") ? null : root.getString("levelName"),
                tiles, checkpoints, teleports);
    }
}

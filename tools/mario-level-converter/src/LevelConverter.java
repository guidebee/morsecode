import CheckPoint.BasicCheckPoints;
import Gears.Construct;
import Levels.BasicLevel;
import Levels.One.BonusArea.BonusArea11A;
import Levels.One.BonusArea.BonusArea12B;
import Levels.One.Level_11;
import Levels.One.Level_12;
import Levels.One.Level_13;
import Levels.One.Level_14;
import Teleport.Teleport;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

/**
 * Offline, dev-only tool. Not shipped in the app.
 *
 * <p>Converts the original GTGE {@code Levels.*} classes (which are plain data
 * builders — see docs/MARIO_PORT_PLAN.md §3) into one JSON file per level under
 * {@code app/src/main/assets/mario/levels/}, for the GGE-side {@code LevelLoader}
 * to consume. Each level is keyed by its original {@code LevelNumber} — including
 * the two World-1 bonus areas, which the original engine (see
 * {@code SandBox/Mario.java#SelectLevel}) deliberately addresses via the *char*
 * literals {@code 'a'} (97) and {@code 'b'} (98) rather than a two-digit number,
 * to keep them out of the normal numeric level range. We preserve that exactly
 * rather than inventing our own numbering, so the file names double as the
 * canonical level-number map.
 *
 * <p>Run via {@code convert.sh} from the repo root, or manually:
 * <pre>
 *   javac -d out src/**&#47;*.java
 *   java -cp out LevelConverter [output-dir]
 * </pre>
 */
public class LevelConverter {

    private record LevelSpec(int levelNumber, String sourceClass, Supplier<BasicLevel> factory) {
    }

    private static final List<LevelSpec> LEVELS = List.of(
            new LevelSpec(11, "Levels.One.Level_11", Level_11::new),
            new LevelSpec(12, "Levels.One.Level_12", Level_12::new),
            new LevelSpec(13, "Levels.One.Level_13", Level_13::new),
            new LevelSpec(14, "Levels.One.Level_14", Level_14::new),
            new LevelSpec(97, "Levels.One.BonusArea.BonusArea11A", BonusArea11A::new),
            new LevelSpec(98, "Levels.One.BonusArea.BonusArea12B", BonusArea12B::new)
    );

    public static void main(String[] args) throws IOException {
        Path outputDir = Path.of(args.length > 0 ? args[0] : "app/src/main/assets/mario/levels");
        Files.createDirectories(outputDir);

        for (LevelSpec spec : LEVELS) {
            BasicLevel level = spec.factory().get();
            String json = toJson(spec, level);
            Path outFile = outputDir.resolve("level_" + spec.levelNumber() + ".json");
            try (PrintWriter writer = new PrintWriter(
                    Files.newBufferedWriter(outFile, StandardCharsets.UTF_8))) {
                writer.write(json);
            }
            System.out.println("Wrote " + outFile + " (" + level.Items + " tiles, "
                    + level.checkPoints_Amount + " checkpoints, "
                    + level.Teleport_Items + " teleports)");
        }
    }

    private static String toJson(LevelSpec spec, BasicLevel level) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"levelNumber\": ").append(spec.levelNumber()).append(",\n");
        sb.append("  \"sourceClass\": ").append(str(spec.sourceClass())).append(",\n");
        sb.append("  \"backgroundColor\": ").append(str(level.BackGroundColor)).append(",\n");
        sb.append("  \"time\": ").append(str(level.Time)).append(",\n");
        sb.append("  \"type\": ").append(str(level.type)).append(",\n");
        sb.append("  \"pos\": ").append(level.pos == null ? "null"
                : "{\"x\": " + level.pos.x + ", \"y\": " + level.pos.y + "}").append(",\n");
        sb.append("  \"backgroundImage\": ").append(str(level.BackGroundImage)).append(",\n");
        sb.append("  \"attribute\": ").append(str(level.attribute)).append(",\n");
        sb.append("  \"levelLength\": ").append(level.LevelLength).append(",\n");
        sb.append("  \"bombs\": ").append(level.Bombs).append(",\n");
        sb.append("  \"bombsTurnOff\": ").append(level.BombsTurnOff).append(",\n");
        sb.append("  \"flyingFishes\": ").append(level.FlyingFishes).append(",\n");
        sb.append("  \"flyingFishesLength\": ").append(level.FlyingFishesLength).append(",\n");
        sb.append("  \"levelName\": ").append(str(level.level_Name)).append(",\n");

        sb.append("  \"tiles\": [\n");
        for (int i = 0; i < level.Items; i++) {
            Construct c = level.get_Item_Number(i);
            sb.append("    {\"type\": ").append(str(c.getItem_Type()))
                    .append(", \"x\": ").append(c.getX())
                    .append(", \"y\": ").append(c.getY())
                    .append(", \"lengthX\": ").append(c.getLength_X())
                    .append(", \"lengthY\": ").append(c.getLength_Y())
                    .append(", \"extraInfo\": ").append(str(c.getExtraInfo()))
                    .append(", \"bridgeLength\": ").append(c.getBridgeLength())
                    .append(", \"patrolLength\": ").append(c.PetrolLength)
                    .append("}").append(i < level.Items - 1 ? ",\n" : "\n");
        }
        sb.append("  ],\n");

        sb.append("  \"checkpoints\": [\n");
        for (int i = 0; i < level.checkPoints_Amount; i++) {
            BasicCheckPoints cp = level.get_CheckPoints_Number(i);
            com.golden.gamedev.object.Sprite sprite = (com.golden.gamedev.object.Sprite) cp;
            java.awt.Point loc = cp.GetNextLocation();
            sb.append("    {\"kind\": ").append(str(cp.getClass().getSimpleName()))
                    .append(", \"x\": ").append(sprite.getX())
                    .append(", \"y\": ").append(sprite.getY())
                    .append(", \"nextLevel\": ").append(cp.GetNextLevel())
                    .append(", \"locX\": ").append(loc.x)
                    .append(", \"locY\": ").append(loc.y)
                    .append("}").append(i < level.checkPoints_Amount - 1 ? ",\n" : "\n");
        }
        sb.append("  ],\n");

        sb.append("  \"teleports\": [\n");
        int teleportCount = countTeleports(level);
        for (int i = 0; i < teleportCount; i++) {
            Teleport t = level.get_Teleport_Number(i);
            sb.append("    {\"inX\": ").append(t.getINx())
                    .append(", \"inY\": ").append(t.getINy())
                    .append(", \"outX\": ").append(t.getOUTx())
                    .append(", \"outY\": ").append(t.getOUTy())
                    .append("}").append(i < teleportCount - 1 ? ",\n" : "\n");
        }
        sb.append("  ]\n");

        sb.append("}\n");
        return sb.toString();
    }

    private static int countTeleports(BasicLevel level) {
        // Teleport_Items is public on BasicLevel; named as a method call here only
        // to keep the reflection-free field access pattern visually consistent
        // with the Items/checkPoints_Amount loops above.
        return level.Teleport_Items;
    }

    private static String str(String s) {
        if (s == null) return "null";
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> sb.append(c);
            }
        }
        return sb.append('"').toString();
    }
}

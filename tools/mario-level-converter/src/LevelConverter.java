import CheckPoint.BasicCheckPoints;
import Gears.Construct;
import Levels.BasicLevel;
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
 * every world's own bonus areas, which the original engine (see
 * {@code SandBox/Mario.java#SelectLevel}) deliberately addresses via *char*
 * literals ({@code 'a'}-{@code 'n'}, 97-110, in the order each bonus area is
 * first reached: World1's two, then World2's one, World3's one, ...) rather
 * than a two-digit number, to keep them out of the normal numeric level
 * range - confirmed by grepping every {@code AddCheckPoints_InsidePumpXXX}
 * call's char-literal argument across all 8 worlds' own level classes, not
 * assumed. The 5 {@code Clowd} beanstalk levels use their own small numeric
 * range (92-96, one per world that has one: 2/3/4/5/6) instead of the letter
 * scheme - also confirmed the same way, via each origin level's own
 * {@code AddClowdGoUP_CheckPoints} call. We preserve both exactly rather than
 * inventing our own numbering, so the file names double as the canonical
 * level-number map. See docs/MARIO_PORT_PLAN_PHASE2.md Step P2.1.3.
 *
 * <p>Uses fully-qualified {@code Levels.*} references directly in
 * {@link #LEVELS} instead of one import per class (55 of them, across 8
 * worlds) - Java allows this in a method reference the same as anywhere else
 * a type name is used.
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
            // World 1
            new LevelSpec(11, "Levels.One.Level_11", Levels.One.Level_11::new),
            new LevelSpec(12, "Levels.One.Level_12", Levels.One.Level_12::new),
            new LevelSpec(13, "Levels.One.Level_13", Levels.One.Level_13::new),
            new LevelSpec(14, "Levels.One.Level_14", Levels.One.Level_14::new),
            new LevelSpec(97, "Levels.One.BonusArea.BonusArea11A", Levels.One.BonusArea.BonusArea11A::new),
            new LevelSpec(98, "Levels.One.BonusArea.BonusArea12B", Levels.One.BonusArea.BonusArea12B::new),
            // World 2
            new LevelSpec(21, "Levels.Two.Level_21", Levels.Two.Level_21::new),
            new LevelSpec(22, "Levels.Two.Level_22", Levels.Two.Level_22::new),
            new LevelSpec(23, "Levels.Two.Level_23", Levels.Two.Level_23::new),
            new LevelSpec(24, "Levels.Two.Level_24", Levels.Two.Level_24::new),
            new LevelSpec(99, "Levels.Two.BonusArea.BonusArea21A", Levels.Two.BonusArea.BonusArea21A::new),
            new LevelSpec(92, "Levels.Extra.Clowd_level_for_2_1", Levels.Extra.Clowd_level_for_2_1::new),
            // World 3
            new LevelSpec(31, "Levels.Three.Level_31", Levels.Three.Level_31::new),
            new LevelSpec(32, "Levels.Three.Level_32", Levels.Three.Level_32::new),
            new LevelSpec(33, "Levels.Three.Level_33", Levels.Three.Level_33::new),
            new LevelSpec(34, "Levels.Three.Level_34", Levels.Three.Level_34::new),
            new LevelSpec(100, "Levels.Three.BonusArea.BonusArea31C", Levels.Three.BonusArea.BonusArea31C::new),
            new LevelSpec(93, "Levels.Extra.Clowd_level_for_3_1", Levels.Extra.Clowd_level_for_3_1::new),
            // World 4
            new LevelSpec(41, "Levels.Four.Level_41", Levels.Four.Level_41::new),
            new LevelSpec(42, "Levels.Four.Level_42", Levels.Four.Level_42::new),
            new LevelSpec(43, "Levels.Four.Level_43", Levels.Four.Level_43::new),
            new LevelSpec(44, "Levels.Four.Level_44", Levels.Four.Level_44::new),
            new LevelSpec(101, "Levels.Four.BonusArea.BonusArea41D", Levels.Four.BonusArea.BonusArea41D::new),
            new LevelSpec(102, "Levels.Four.BonusArea.BonusArea42E", Levels.Four.BonusArea.BonusArea42E::new),
            new LevelSpec(94, "Levels.Extra.Clowd_level_for_4_2", Levels.Extra.Clowd_level_for_4_2::new),
            // World 5
            new LevelSpec(51, "Levels.Five.Level_51", Levels.Five.Level_51::new),
            new LevelSpec(52, "Levels.Five.Level_52", Levels.Five.Level_52::new),
            new LevelSpec(53, "Levels.Five.Level_53", Levels.Five.Level_53::new),
            new LevelSpec(54, "Levels.Five.Level_54", Levels.Five.Level_54::new),
            new LevelSpec(103, "Levels.Five.BonusArea.BonusArea51E", Levels.Five.BonusArea.BonusArea51E::new),
            new LevelSpec(104, "Levels.Five.BonusArea.BonusArea52F", Levels.Five.BonusArea.BonusArea52F::new),
            new LevelSpec(95, "Levels.Extra.Clowd_level_for_5_2", Levels.Extra.Clowd_level_for_5_2::new),
            // World 6
            new LevelSpec(61, "Levels.Six.Level_61", Levels.Six.Level_61::new),
            new LevelSpec(62, "Levels.Six.Level_62", Levels.Six.Level_62::new),
            new LevelSpec(63, "Levels.Six.Level_63", Levels.Six.Level_63::new),
            new LevelSpec(64, "Levels.Six.Level_64", Levels.Six.Level_64::new),
            new LevelSpec(105, "Levels.Six.BonusArea.BonusArea62D", Levels.Six.BonusArea.BonusArea62D::new),
            new LevelSpec(106, "Levels.Six.BonusArea.BonusArea62E", Levels.Six.BonusArea.BonusArea62E::new),
            new LevelSpec(107, "Levels.Six.BonusArea.BonusArea62G", Levels.Six.BonusArea.BonusArea62G::new),
            new LevelSpec(96, "Levels.Extra.Clowd_level_for_6_2", Levels.Extra.Clowd_level_for_6_2::new),
            // World 7
            new LevelSpec(71, "Levels.Seven.Level_71", Levels.Seven.Level_71::new),
            new LevelSpec(72, "Levels.Seven.Level_72", Levels.Seven.Level_72::new),
            new LevelSpec(73, "Levels.Seven.Level_73", Levels.Seven.Level_73::new),
            new LevelSpec(74, "Levels.Seven.Level_74", Levels.Seven.Level_74::new),
            new LevelSpec(108, "Levels.Seven.BonusArea.BonusArea71A", Levels.Seven.BonusArea.BonusArea71A::new),
            // World 8
            new LevelSpec(81, "Levels.Eight.Level_81", Levels.Eight.Level_81::new),
            new LevelSpec(82, "Levels.Eight.Level_82", Levels.Eight.Level_82::new),
            new LevelSpec(83, "Levels.Eight.Level_83", Levels.Eight.Level_83::new),
            new LevelSpec(841, "Levels.Eight.Level_841", Levels.Eight.Level_841::new),
            new LevelSpec(842, "Levels.Eight.Level_842", Levels.Eight.Level_842::new),
            new LevelSpec(843, "Levels.Eight.Level_843", Levels.Eight.Level_843::new),
            new LevelSpec(844, "Levels.Eight.Level_844", Levels.Eight.Level_844::new),
            new LevelSpec(845, "Levels.Eight.Level_845", Levels.Eight.Level_845::new),
            new LevelSpec(109, "Levels.Eight.BonusArea.BonusArea81B", Levels.Eight.BonusArea.BonusArea81B::new),
            new LevelSpec(110, "Levels.Eight.BonusArea.BonusArea82E", Levels.Eight.BonusArea.BonusArea82E::new)
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

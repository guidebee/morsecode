import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Offline, dev-only tool - not part of the Android build.
 *
 * Packs the World-1 subset of the original Mario game's PNGs (from
 * C:\workspace\Mario\SandBox, plus CloudsNight\Hammer.png for the boss's
 * hammer) into a libGDX-format TextureAtlas (mario.png[+mario1.png...] and
 * mario.atlas), matching the format GGE's TextureAtlas/AssetManager already
 * loads for battlecity.atlas / flappybird.atlas.
 *
 * ASSETS below is the exact set of resource keys the original engine's
 * WholeGame.java registers with its BaseLoader (bsLoader.storeImage/
 * storeImages) that are actually reachable from World 1 (Levels 11-14 and
 * bonus areas 97/98) - cross-checked against Mario.java's tile-spawning
 * switch and each Bricks/Objects/Lifts class's own getStoredImage(s) calls.
 * Region names are re-cased (camel/snake, no spaces) since this tool and its
 * one consumer (MarioResourceManager) are both new code - there's no
 * original-engine string to stay compatible with.
 *
 * Each region is packed as a single whole image, preserving the original
 * col x row strip layout unsliced (recorded as a comment here for step
 * 3/4's use) - GGE's microedition Sprite(TextureRegion, frameWidth,
 * frameHeight) slices a strip region into frames exactly like GTGE's
 * Sprite(Image, width, height) did, so no re-slicing work is needed now.
 *
 * Run from the repo root:
 *   bash tools/mario-atlas-packer/pack.sh
 */
public class PackMarioAtlas {

    private record AssetSpec(String regionName, String sourcePath, int cols, int rows) {
    }

    private static final List<AssetSpec> ASSETS = List.of(
            // Ground/UnderGround/Castle terrain (attribute-themed single 32x32 tiles)
            new AssetSpec("brick", "brick.png", 1, 1),
            new AssetSpec("brick_underground", "brick_UnderGround.png", 1, 1),
            new AssetSpec("brick_castle", "brick_Castle.png", 1, 1),
            new AssetSpec("stone", "stone.png", 1, 1),
            new AssetSpec("stone_underground", "stone_UnderGround.png", 1, 1),
            new AssetSpec("stone_castle", "stone_Castle.png", 1, 1),
            new AssetSpec("chocolate", "chocolate.png", 1, 1),
            new AssetSpec("chocolate_underground", "chocolate_UnderGround.png", 1, 1),
            new AssetSpec("chocolate_castle", "chocolate_Castle.png", 1, 1),

            // Pipes/pumps (Ground + Castle only - World 1 never uses the Sea attribute)
            new AssetSpec("pump", "pump.png", 1, 1),
            new AssetSpec("pump_top", "pump top.png", 1, 1),
            new AssetSpec("pump_castle", "pump Castle.png", 1, 1),
            new AssetSpec("pump_top_castle", "pump top Castle.png", 1, 1),
            new AssetSpec("plant", "plant.png", 2, 1),
            new AssetSpec("plant_dark", "plantdark.png", 2, 1),
            new AssetSpec("hori_image", "HoriImage.png", 2, 1),

            // Bricks/blocks and their reveal items
            new AssetSpec("question_mark", "QuestionMark.png", 3, 1),
            new AssetSpec("mashroom", "Mashroom.png", 1, 1),
            new AssetSpec("mashrooms", "Mashrooms.png", 2, 1),
            new AssetSpec("flower", "Flower.png", 4, 1),
            new AssetSpec("coin_anim", "CoinAnim.png", 4, 1),
            new AssetSpec("star", "Star.png", 4, 1),
            new AssetSpec("one_up", "1UP.png", 2, 1),
            new AssetSpec("coin", "Coin.png", 3, 1),
            new AssetSpec("iron", "Iron.png", 4, 1),
            new AssetSpec("bridge_blocks", "BridgeBloks.png", 1, 1),

            // Enemies
            new AssetSpec("enemy", "enemy.png", 2, 4),
            new AssetSpec("turtle", "turtle.png", 4, 1),
            new AssetSpec("turtle_dark", "turtledark.png", 4, 1),
            new AssetSpec("turtle_shell", "TurtelShell.png", 1, 1),
            new AssetSpec("turtle_shell_dark", "TurtelShelldark.png", 1, 1),
            new AssetSpec("turtle_shell_red", "TurtelShellRed.png", 1, 1),
            new AssetSpec("enemy_turtle_patrol", "EnemyTurtlePatrol.png", 4, 1),
            new AssetSpec("flying_turtle_patrol", "FlyingTurtlePatrol.png", 4, 1),
            new AssetSpec("boss", "Boss.png", 3, 2),
            new AssetSpec("boss_fire", "BossFire.png", 2, 1),
            // NOTE: the original Boss.java always throws bsLoader "BWHammer" regardless of
            // level theme (likely an oversight left in the original game) - preserved as-is.
            new AssetSpec("bw_hammer", "CloudsNight/Hammer.png", 4, 1),
            new AssetSpec("fire_ball", "FireBall.png", 4, 1),
            new AssetSpec("lava", "Lava.png", 1, 1),

            // Scenery
            new AssetSpec("small_castle", "SmallCastle.png", 1, 1),
            new AssetSpec("big_castle", "BigCastle.png", 1, 1),
            new AssetSpec("tree", "tree.png", 5, 2),
            new AssetSpec("lift", "Lift.png", 1, 1),
            new AssetSpec("mountain", "Mountain.png", 1, 1),
            new AssetSpec("clouds", "Clouds.png", 1, 1),

            // Flag / level-end
            new AssetSpec("flag", "Flag.png", 1, 1),
            new AssetSpec("flag_top", "FlagTop.png", 1, 1),
            new AssetSpec("flag_sphere", "FlagSphere.png", 1, 1),
            new AssetSpec("flag_win", "FlagWin.png", 1, 1),
            new AssetSpec("another_castle_message", "AnotherCastleMessage.png", 1, 1),
            new AssetSpec("quest_complete", "QuestComplete.png", 1, 1),

            // Player
            new AssetSpec("player", "player.png", 4, 7),
            new AssetSpec("big_player", "BigPlayer.png", 4, 7),
            new AssetSpec("fire_player", "FirePlayer.png", 4, 7),
            new AssetSpec("small_to_big_mario", "SmallToBigMarioAnim.png", 12, 1),
            new AssetSpec("big_to_fire_mario", "BigToFireMarioAnim.png", 10, 1),
            new AssetSpec("big_to_small_mario", "BigToSmallMarioAnim.png", 10, 1),
            new AssetSpec("fire_to_small_mario", "FireToSmallMarioAnim.png", 10, 1),
            new AssetSpec("small_to_big_star_mario", "SmallToBigStarMaroAnim.png", 12, 1),
            new AssetSpec("small_dead_mario", "SmallDeadMario.png", 1, 1),

            // HUD
            new AssetSpec("font", "Font.png", 16, 3),
            new AssetSpec("info", "Info.png", 1, 1),
            new AssetSpec("info2", "Info2.png", 1, 1)
    );

    private static final int PAGE_SIZE = 2048;
    private static final int PADDING = 2;

    private record LoadedAsset(AssetSpec spec, BufferedImage image) {
    }

    private record PlacedRegion(AssetSpec spec, int pageIndex, int x, int y, int w, int h) {
    }

    private static final class Page {
        int cursorX = PADDING;
        int cursorY = PADDING;
        int shelfHeight = 0;

        int[] place(int w, int h) {
            if (cursorX + w + PADDING > PAGE_SIZE) {
                cursorY += shelfHeight + PADDING;
                cursorX = PADDING;
                shelfHeight = 0;
            }
            if (cursorY + h + PADDING > PAGE_SIZE) {
                return null;
            }
            int[] pos = {cursorX, cursorY};
            cursorX += w + PADDING;
            shelfHeight = Math.max(shelfHeight, h);
            return pos;
        }
    }

    public static void main(String[] args) throws Exception {
        File sourceDir = new File(args.length > 0 ? args[0] : "C:/workspace/Mario/SandBox");
        File outDir = new File(args.length > 1 ? args[1] : "app/src/main/assets");
        outDir.mkdirs();

        List<LoadedAsset> loaded = new ArrayList<>();
        for (AssetSpec spec : ASSETS) {
            File f = new File(sourceDir, spec.sourcePath());
            BufferedImage img = ImageIO.read(f);
            if (img == null) {
                throw new IllegalStateException("Failed to read " + f);
            }
            loaded.add(new LoadedAsset(spec, img));
        }
        // Tallest-first shelf packing keeps shelves tightly packed.
        loaded.sort((a, b) -> b.image.getHeight() - a.image.getHeight());

        List<Page> pages = new ArrayList<>();
        pages.add(new Page());
        List<PlacedRegion> placements = new ArrayList<>();

        for (LoadedAsset asset : loaded) {
            int w = asset.image.getWidth();
            int h = asset.image.getHeight();
            if (w + PADDING * 2 > PAGE_SIZE || h + PADDING * 2 > PAGE_SIZE) {
                throw new IllegalStateException("Asset too large for a " + PAGE_SIZE
                        + "x" + PAGE_SIZE + " page: " + asset.spec.regionName());
            }
            Page page = pages.get(pages.size() - 1);
            int[] pos = page.place(w, h);
            if (pos == null) {
                page = new Page();
                pages.add(page);
                pos = page.place(w, h);
            }
            placements.add(new PlacedRegion(asset.spec, pages.size() - 1, pos[0], pos[1], w, h));
        }

        String baseName = "mario";
        for (int i = 0; i < pages.size(); i++) {
            BufferedImage canvas = new BufferedImage(PAGE_SIZE, PAGE_SIZE, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = canvas.createGraphics();
            for (PlacedRegion pr : placements) {
                if (pr.pageIndex() != i) {
                    continue;
                }
                BufferedImage img = findImage(loaded, pr.spec());
                g.drawImage(img, pr.x(), pr.y(), null);
            }
            g.dispose();
            String pageFile = pages.size() == 1 ? baseName + ".png" : baseName + i + ".png";
            File out = new File(outDir, pageFile);
            ImageIO.write(canvas, "PNG", out);
            System.out.println("Wrote " + out + " (" + PAGE_SIZE + "x" + PAGE_SIZE + ")");
        }

        StringBuilder atlas = new StringBuilder();
        for (int i = 0; i < pages.size(); i++) {
            String pageFile = pages.size() == 1 ? baseName + ".png" : baseName + i + ".png";
            atlas.append(pageFile).append("\n");
            atlas.append("size: ").append(PAGE_SIZE).append(",").append(PAGE_SIZE).append("\n");
            atlas.append("format: RGBA8888\n");
            atlas.append("filter: Nearest,Nearest\n");
            atlas.append("repeat: none\n");
            for (PlacedRegion pr : placements) {
                if (pr.pageIndex() != i) {
                    continue;
                }
                atlas.append(pr.spec().regionName()).append("\n");
                atlas.append("  rotate: false\n");
                atlas.append("  xy: ").append(pr.x()).append(", ").append(pr.y()).append("\n");
                atlas.append("  size: ").append(pr.w()).append(", ").append(pr.h()).append("\n");
                atlas.append("  orig: ").append(pr.w()).append(", ").append(pr.h()).append("\n");
                atlas.append("  offset: 0, 0\n");
                atlas.append("  index: -1\n");
            }
        }
        File atlasFile = new File(outDir, baseName + ".atlas");
        try (FileWriter fw = new FileWriter(atlasFile)) {
            fw.write(atlas.toString());
        }
        System.out.println("Wrote " + atlasFile + " (" + placements.size()
                + " regions across " + pages.size() + " page(s))");

        System.out.println();
        System.out.println("Region -> (cols x rows) frame grid, for Step 3/4's Sprite(region, frameWidth, frameHeight):");
        for (AssetSpec spec : ASSETS) {
            System.out.println("  " + spec.regionName() + " -> " + spec.cols() + "x" + spec.rows());
        }
    }

    private static BufferedImage findImage(List<LoadedAsset> loaded, AssetSpec spec) {
        for (LoadedAsset asset : loaded) {
            if (asset.spec() == spec) {
                return asset.image();
            }
        }
        throw new IllegalStateException("Unreachable: " + spec.regionName());
    }
}

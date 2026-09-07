import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Offline, dev-only tool - not part of the Android build.
 *
 * Packs the World-1 subset of the original Mario game's PNGs (from
 * C:\workspace\Mario\SandBox, plus CloudsNight\Hammer.png for the boss's
 * hammer) into libGDX-format TextureAtlases, matching the format GGE's
 * TextureAtlas/AssetManager already loads for battlecity.atlas / flappybird.atlas.
 *
 * <p>Emits one atlas per {@link Theme} instead of a single growing one -
 * see docs/MARIO_PORT_PLAN_PHASE2.md Step P2.1.1. {@link Theme#COMMON} (player,
 * enemies, items, HUD, fonts - everything used regardless of a level's
 * attribute) is always loaded; {@code MarioResourceManager} additionally loads
 * just whichever one of {@link Theme#GROUND}/{@link Theme#UNDERGROUND}/
 * {@link Theme#CASTLE} the current level's attribute calls for, so a Ground
 * level never pays to keep Castle-only terrain art resident. Region *names*
 * are unchanged from the single-atlas version (still e.g. "brick_underground",
 * not just "brick", picked by {@code MarioResourceManager.themedRegion} the
 * same way as before) - only *which physical atlas file* a region lives in
 * changed, so no actor/call-site code needed to change for this split.
 *
 * <p>ASSETS below is the set of resource keys the original engine's
 * WholeGame.java registers with its BaseLoader (bsLoader.storeImage/
 * storeImages) that are reachable via a real Mario.java tile-spawning case
 * (cross-checked against that switch and each Bricks/Objects/Lifts class's
 * own getStoredImage(s) calls) - originally just World 1's own subset
 * (Levels 11-14 + bonus areas 97/98), extended per
 * docs/MARIO_PORT_PLAN_PHASE2.md Step P2.1.2 to also cover every asset the
 * *rest* of Phase 2's own §1.3 mechanic table plans to build (Helmet family,
 * Bouncer/Spring, Monkey, RocketLauncher, SonOfABuitch, Spikey/SpikeyEgg,
 * WoodenBridge, LavaBall, Chain/Rope, the regular FlyingTurtle, ...) -
 * packed ahead of the actor code that will read them, so P2.2-P2.5's own
 * steps are pure gameplay-code work with zero further packer round-trips.
 * Region names are re-cased (camel/snake, no spaces) since this tool and its
 * one consumer (MarioResourceManager) are both new code - there's no
 * original-engine string to stay compatible with.
 *
 * <p><b>Deliberately still excluded</b> (Step P2.1.2's own scope decision,
 * not an oversight): every Sea-attribute terrain/creature asset
 * (chocolate_Sea/brick_Sea/stone_Sea/pump variants/stone_Castle_Sea/
 * stone_Clowd/OrangePump/FishGrey/FishRed/OctoPussy/Bubble/Water) and every
 * CloudsNight-theme asset beyond the already-packed "bw_hammer"
 * (BWBigCastle/BWRocketLauncher/BWBouncer/BWstone/BWtree/BWSmallCastle) -
 * both would need a brand-new {@link Theme} bucket (and a matching
 * {@code MarioResourceManager.loadTheme} mapping) that can't be verified
 * against a real level until Phase 2's own P2.5 (CloudsNight)/P2.6 (Sea)
 * steps actually land; pack those alongside that work instead of guessing
 * the bucket now. Also excluded: assets that look like dead/abandoned
 * original-engine resources with no reachable spawn path found
 * (numbers432/numbers876 - superseded by this port's own bitmap-font HUD
 * per {@code ScoreHud}'s doc; Start/bend; the root "Hammer.png" duplicate of
 * "BWHammer", never actually read per {@code Boss}'s own doc) - re-check
 * before assuming any of these are needed if a later survey suggests
 * otherwise.
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

    private enum Theme {
        /** Loaded for the whole app session - see {@code MarioResourceManager#loadCommon}. */
        COMMON("mario-common"),
        GROUND("mario-ground"),
        UNDERGROUND("mario-underground"),
        CASTLE("mario-castle");

        final String baseName;

        Theme(String baseName) {
            this.baseName = baseName;
        }
    }

    private record AssetSpec(Theme theme, String regionName, String sourcePath, int cols, int rows) {
    }

    private static final List<AssetSpec> ASSETS = List.of(
            // Ground/UnderGround/Castle terrain (attribute-themed single 32x32 tiles) -
            // each variant lives in its own theme's atlas; region *names* keep the old
            // suffix convention (MarioResourceManager.themedRegion picks by name, same as
            // pre-split) so no actor code needed to change.
            new AssetSpec(Theme.GROUND, "brick", "brick.png", 1, 1),
            new AssetSpec(Theme.UNDERGROUND, "brick_underground", "brick_UnderGround.png", 1, 1),
            new AssetSpec(Theme.CASTLE, "brick_castle", "brick_Castle.png", 1, 1),
            new AssetSpec(Theme.GROUND, "stone", "stone.png", 1, 1),
            new AssetSpec(Theme.UNDERGROUND, "stone_underground", "stone_UnderGround.png", 1, 1),
            new AssetSpec(Theme.CASTLE, "stone_castle", "stone_Castle.png", 1, 1),
            new AssetSpec(Theme.GROUND, "chocolate", "chocolate.png", 1, 1),
            new AssetSpec(Theme.UNDERGROUND, "chocolate_underground", "chocolate_UnderGround.png", 1, 1),
            new AssetSpec(Theme.CASTLE, "chocolate_castle", "chocolate_Castle.png", 1, 1),

            // Pipes/pumps - "pump"/"pump_top" are shared Ground+UnderGround art in the
            // original (see actors.bricks.Pump's own doc), so they're COMMON, not
            // GROUND-only; only the Castle recolor is theme-exclusive. World 1 never
            // places a pipe on the "Sea" attribute.
            new AssetSpec(Theme.COMMON, "pump", "pump.png", 1, 1),
            new AssetSpec(Theme.COMMON, "pump_top", "pump top.png", 1, 1),
            new AssetSpec(Theme.CASTLE, "pump_castle", "pump Castle.png", 1, 1),
            new AssetSpec(Theme.CASTLE, "pump_top_castle", "pump top Castle.png", 1, 1),
            new AssetSpec(Theme.COMMON, "plant", "plant.png", 2, 1),
            new AssetSpec(Theme.COMMON, "plant_dark", "plantdark.png", 2, 1),
            new AssetSpec(Theme.COMMON, "hori_image", "HoriImage.png", 2, 1),

            // Bricks/blocks and their reveal items - not theme-swapped (each already
            // themes itself via a frame index or an attribute-picked *name* read from
            // the common atlas, e.g. Iron/BrickFragment), so COMMON.
            new AssetSpec(Theme.COMMON, "question_mark", "QuestionMark.png", 3, 1),
            new AssetSpec(Theme.COMMON, "mashroom", "Mashroom.png", 1, 1),
            new AssetSpec(Theme.COMMON, "mashrooms", "Mashrooms.png", 2, 1),
            new AssetSpec(Theme.COMMON, "flower", "Flower.png", 4, 1),
            new AssetSpec(Theme.COMMON, "coin_anim", "CoinAnim.png", 4, 1),
            new AssetSpec(Theme.COMMON, "star", "Star.png", 4, 1),
            new AssetSpec(Theme.COMMON, "brick_peaces", "BrickPeaces.png", 2, 4),
            new AssetSpec(Theme.COMMON, "one_up", "1UP.png", 2, 1),
            new AssetSpec(Theme.COMMON, "coin", "Coin.png", 3, 1),
            new AssetSpec(Theme.COMMON, "iron", "Iron.png", 4, 1),
            new AssetSpec(Theme.COMMON, "bridge_blocks", "BridgeBloks.png", 1, 1),
            // The "used up" Bank/QuestionMark replacement image - packed now (ahead of
            // the P2.4-onward mechanic that reads it) per docs/MARIO_PORT_PLAN_PHASE2.md
            // Step P2.1.2, matching this table's existing practice of tracking exactly
            // what WholeGame.java registers regardless of which port step first spawns it.
            new AssetSpec(Theme.COMMON, "question_mark_grey", "QuestionMarkGrey.png", 3, 1),
            new AssetSpec(Theme.COMMON, "explosion", "Explosion.png", 3, 1),

            // Enemies - never theme-swapped by atlas (a "dark" variant is just a
            // differently-*named* common region, picked by attribute the same way
            // Iron's frame is), so all COMMON.
            new AssetSpec(Theme.COMMON, "enemy", "enemy.png", 2, 4),
            new AssetSpec(Theme.COMMON, "turtle", "turtle.png", 4, 1),
            new AssetSpec(Theme.COMMON, "turtle_dark", "turtledark.png", 4, 1),
            new AssetSpec(Theme.COMMON, "turtle_shell", "TurtelShell.png", 1, 1),
            new AssetSpec(Theme.COMMON, "turtle_shell_dark", "TurtelShelldark.png", 1, 1),
            new AssetSpec(Theme.COMMON, "turtle_shell_red", "TurtelShellRed.png", 1, 1),
            // Pre-flipped shell art (the original also flips TurtelShellRed at *runtime*
            // for FlyingTurtlePatrol's own kick reaction instead of using this - packed
            // for whichever P2.4-onward mechanic actually turns out to want a static
            // pre-flipped strip; verify the exact call site before relying on it).
            new AssetSpec(Theme.COMMON, "turtle_shell_flip", "TurtelShellFilp.png", 1, 1),
            new AssetSpec(Theme.COMMON, "turtle_shell_flip_dark", "TurtelShellFilpdark.png", 1, 1),
            new AssetSpec(Theme.COMMON, "turtle_shell_flip_red", "TurtelShellFilpRed.png", 1, 1),
            new AssetSpec(Theme.COMMON, "enemy_turtle_patrol", "EnemyTurtlePatrol.png", 4, 1),
            new AssetSpec(Theme.COMMON, "flying_turtle_patrol", "FlyingTurtlePatrol.png", 4, 1),
            // The regular (non-patrol) flying turtle - Mario.java case 20; distinct from
            // FlyingTurtlePatrol (bobs in place vs. this one's own free-roam behavior,
            // unread - see docs/MARIO_PORT_PLAN_PHASE2.md's open items).
            new AssetSpec(Theme.COMMON, "flying_turtle", "FlyingTurtle.png", 4, 1),
            new AssetSpec(Theme.COMMON, "flying_turtle_dark", "FlyingTurtledark.png", 4, 1),
            new AssetSpec(Theme.COMMON, "monkey", "Monkey.png", 3, 2),
            new AssetSpec(Theme.COMMON, "helmet", "Helmet.png", 4, 1),
            new AssetSpec(Theme.COMMON, "helmet_dark", "Helmetdark.png", 4, 1),
            new AssetSpec(Theme.COMMON, "helmet_white", "Helmetwhite.png", 4, 1),
            new AssetSpec(Theme.COMMON, "helmet_shell", "HelmetShell.png", 1, 1),
            new AssetSpec(Theme.COMMON, "helmet_shell_dark", "HelmetShelldark.png", 1, 1),
            new AssetSpec(Theme.COMMON, "helmet_shell_white", "HelmetShellwhite.png", 1, 1),
            new AssetSpec(Theme.COMMON, "son_of_a_buitch", "SonOfABuitch.png", 2, 1),
            new AssetSpec(Theme.COMMON, "spikey_egg", "SpikeyEgg.png", 2, 1),
            new AssetSpec(Theme.COMMON, "spikey", "Spikey.png", 4, 1),
            new AssetSpec(Theme.COMMON, "boss", "Boss.png", 3, 2),
            new AssetSpec(Theme.COMMON, "boss_fire", "BossFire.png", 2, 1),
            // NOTE: the original Boss.java always throws bsLoader "BWHammer" regardless of
            // level theme (likely an oversight left in the original game) - preserved as-is.
            new AssetSpec(Theme.COMMON, "bw_hammer", "CloudsNight/Hammer.png", 4, 1),
            new AssetSpec(Theme.COMMON, "fire_ball", "FireBall.png", 4, 1),
            new AssetSpec(Theme.COMMON, "lava", "Lava.png", 1, 1),
            new AssetSpec(Theme.COMMON, "lava_ball", "LavaBall.png", 2, 1),
            // 4-frame strip (128x32, confirmed against the source PNG), cycled
            // 0,1,2,3,2,1 by Axe.java - see actors.bricks.Axe.
            new AssetSpec(Theme.COMMON, "axe", "Axe.png", 4, 1),

            // World-mechanics bricks/scenery for P2.3-P2.4 (teleports/novel enemies) -
            // packed now per Step P2.1.2, actors land in their own later steps.
            new AssetSpec(Theme.COMMON, "wall", "Wall.png", 2, 1),
            new AssetSpec(Theme.COMMON, "rocket_launcher", "RocketLauncher.png", 1, 4),
            new AssetSpec(Theme.COMMON, "bouncer", "Bouncer.png", 1, 1),
            new AssetSpec(Theme.COMMON, "spring", "Spring.png", 3, 1),
            new AssetSpec(Theme.COMMON, "wooden_bridge", "WoodenBridge.png", 1, 1),
            new AssetSpec(Theme.COMMON, "white_line", "WhiteLine.png", 1, 1),
            new AssetSpec(Theme.COMMON, "chain", "Chain.png", 4, 1),
            new AssetSpec(Theme.COMMON, "rope", "Rope.png", 1, 1),
            new AssetSpec(Theme.COMMON, "clowd_checkpoint", "clowd_checkpoint.png", 1, 1),

            // Scenery
            new AssetSpec(Theme.COMMON, "small_castle", "SmallCastle.png", 1, 1),
            new AssetSpec(Theme.COMMON, "big_castle", "BigCastle.png", 1, 1),
            new AssetSpec(Theme.COMMON, "tree", "tree.png", 5, 2),
            new AssetSpec(Theme.COMMON, "lift", "Lift.png", 1, 1),
            new AssetSpec(Theme.COMMON, "mountain", "Mountain.png", 1, 1),
            new AssetSpec(Theme.COMMON, "clouds", "Clouds.png", 1, 1),

            // Flag / level-end
            new AssetSpec(Theme.COMMON, "flag", "Flag.png", 1, 1),
            new AssetSpec(Theme.COMMON, "flag_top", "FlagTop.png", 1, 1),
            new AssetSpec(Theme.COMMON, "flag_sphere", "FlagSphere.png", 1, 1),
            new AssetSpec(Theme.COMMON, "flag_win", "FlagWin.png", 1, 1),
            new AssetSpec(Theme.COMMON, "another_castle_message", "AnotherCastleMessage.png", 1, 1),
            new AssetSpec(Theme.COMMON, "quest_complete", "QuestComplete.png", 1, 1),

            // Player
            new AssetSpec(Theme.COMMON, "player", "player.png", 4, 7),
            new AssetSpec(Theme.COMMON, "big_player", "BigPlayer.png", 4, 7),
            new AssetSpec(Theme.COMMON, "fire_player", "FirePlayer.png", 4, 7),
            new AssetSpec(Theme.COMMON, "small_to_big_mario", "SmallToBigMarioAnim.png", 12, 1),
            new AssetSpec(Theme.COMMON, "big_to_fire_mario", "BigToFireMarioAnim.png", 10, 1),
            new AssetSpec(Theme.COMMON, "big_to_small_mario", "BigToSmallMarioAnim.png", 10, 1),
            new AssetSpec(Theme.COMMON, "fire_to_small_mario", "FireToSmallMarioAnim.png", 10, 1),
            new AssetSpec(Theme.COMMON, "small_to_big_star_mario", "SmallToBigStarMaroAnim.png", 12, 1),
            new AssetSpec(Theme.COMMON, "small_dead_mario", "SmallDeadMario.png", 1, 1),
            new AssetSpec(Theme.COMMON, "small_black_mario", "SmallBlackMario.png", 4, 7),
            new AssetSpec(Theme.COMMON, "small_green_mario", "SmallGreenMario.png", 4, 7),
            new AssetSpec(Theme.COMMON, "small_red_mario", "SmallRedMario.png", 4, 7),
            new AssetSpec(Theme.COMMON, "big_black_mario", "BigBlackMario.png", 4, 7),
            new AssetSpec(Theme.COMMON, "big_green_mario", "BigGreenMario.png", 4, 7),
            new AssetSpec(Theme.COMMON, "big_red_mario", "BigRedMario.png", 4, 7),

            // HUD
            new AssetSpec(Theme.COMMON, "font", "Font.png", 16, 3),
            new AssetSpec(Theme.COMMON, "info", "Info.png", 1, 1),
            new AssetSpec(Theme.COMMON, "info2", "Info2.png", 1, 1)
    );

    /**
     * The static-terrain tiles composited into each theme's own "tiles" region -
     * a uniform 32x32 grid {@code TiledLayer} requires. Unlike the single-atlas
     * version, each theme gets its *own* 2-cell composite (stone+chocolate for
     * that theme only) rather than one 6-cell sheet spanning all three - cell
     * value 1=stone, 2=chocolate, the *same two indices regardless of which
     * theme atlas is currently loaded* (see {@code MarioConfiguration}'s
     * TILE_STONE/TILE_CHOCOLATE - the four now-redundant per-theme variants were
     * removed since the theme atlas itself supplies the right texture).
     *
     * <p>NOTE: "pump" and its variants are deliberately excluded, even though
     * they're conceptually static terrain - pump.png etc. are 64x32 (2 tiles
     * wide, drawn as freely-positioned/overlapping sprites in the original
     * engine, not tile-grid cells), which TiledLayer's uniform grid can't
     * represent without distorting them - it's a Sprite actor instead (see
     * {@code actors.bricks.Pump}).
     *
     * <p>NOTE: "brick" and its variants are ALSO excluded - Brick turned out to
     * be breakable (see {@code Bricks/Brick.java}'s HitFromDown), so it's a
     * Sprite actor too ({@code actors.bricks.Brick}), same reasoning as pump.
     */
    private record TerrainTile(Theme theme, String stoneRegion, String chocolateRegion) {
    }

    private static final List<TerrainTile> TERRAIN_TILES = List.of(
            new TerrainTile(Theme.GROUND, "stone", "chocolate"),
            new TerrainTile(Theme.UNDERGROUND, "stone_underground", "chocolate_underground"),
            new TerrainTile(Theme.CASTLE, "stone_castle", "chocolate_castle")
    );
    private static final int TILE_SHEET_COLS = 2;
    private static final int TILE_SIZE = 32;

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
        for (TerrainTile tile : TERRAIN_TILES) {
            loaded.add(new LoadedAsset(
                    new AssetSpec(tile.theme(), "tiles", null, TILE_SHEET_COLS, 1),
                    buildTileSheet(loaded, tile)));
        }

        Map<Theme, List<LoadedAsset>> byTheme = new EnumMap<>(Theme.class);
        for (LoadedAsset asset : loaded) {
            byTheme.computeIfAbsent(asset.spec().theme(), t -> new ArrayList<>()).add(asset);
        }

        for (Theme theme : Theme.values()) {
            List<LoadedAsset> themeAssets = byTheme.getOrDefault(theme, List.of());
            if (themeAssets.isEmpty()) {
                continue;
            }
            packTheme(theme, themeAssets, outDir);
        }

        System.out.println();
        System.out.println("Region -> (cols x rows) frame grid, for Step 3/4's Sprite(region, frameWidth, frameHeight):");
        for (AssetSpec spec : ASSETS) {
            System.out.println("  [" + spec.theme() + "] " + spec.regionName() + " -> " + spec.cols() + "x" + spec.rows());
        }
        for (TerrainTile tile : TERRAIN_TILES) {
            System.out.println("  [" + tile.theme() + "] tiles -> " + TILE_SHEET_COLS
                    + "x1 (composite TiledLayer tile set; 1=" + tile.stoneRegion() + ", 2=" + tile.chocolateRegion() + ")");
        }
    }

    /** Packs one theme's assets into its own page(s) + .atlas, exactly like the old single-atlas packer did for everything at once. */
    private static void packTheme(Theme theme, List<LoadedAsset> themeAssets, File outDir) throws Exception {
        // Tallest-first shelf packing keeps shelves tightly packed.
        List<LoadedAsset> sorted = new ArrayList<>(themeAssets);
        sorted.sort((a, b) -> b.image.getHeight() - a.image.getHeight());

        List<Page> pages = new ArrayList<>();
        pages.add(new Page());
        List<PlacedRegion> placements = new ArrayList<>();

        for (LoadedAsset asset : sorted) {
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

        String baseName = theme.baseName;
        for (int i = 0; i < pages.size(); i++) {
            BufferedImage canvas = new BufferedImage(PAGE_SIZE, PAGE_SIZE, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = canvas.createGraphics();
            for (PlacedRegion pr : placements) {
                if (pr.pageIndex() != i) {
                    continue;
                }
                BufferedImage img = findImage(sorted, pr.spec());
                drawFlippedPerCell(g, img, pr.x(), pr.y(), pr.spec().cols(), pr.spec().rows());
            }
            g.dispose();
            String pageFile = pages.size() == 1 ? baseName + ".png" : baseName + i + ".png";
            File out = new File(outDir, pageFile);
            ImageIO.write(canvas, "PNG", out);
            System.out.println("Wrote " + out + " (" + PAGE_SIZE + "x" + PAGE_SIZE + ")");
        }

        StringBuilder atlas = new StringBuilder();
        for (int i = 0; i < pages.size(); i++) {
            // A blank line is how TextureAtlas$TextureAtlasData's own parser
            // (see its `line.trim().length() == 0` check) knows a new page's
            // header is starting rather than another region of the current
            // one - omitting it reads fine for a single-page atlas but
            // corrupts parsing of every page after the first once there's
            // more than one.
            if (i > 0) {
                atlas.append("\n");
            }
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
    }

    private static BufferedImage findImage(List<LoadedAsset> loaded, AssetSpec spec) {
        for (LoadedAsset asset : loaded) {
            if (asset.spec() == spec) {
                return asset.image();
            }
        }
        throw new IllegalStateException("Unreachable: " + spec.regionName());
    }

    private static BufferedImage findImageByRegionName(List<LoadedAsset> loaded, Theme theme, String regionName) {
        for (LoadedAsset asset : loaded) {
            if (asset.spec().theme() == theme && asset.spec().regionName().equals(regionName)) {
                return asset.image();
            }
        }
        throw new IllegalStateException("No such source asset for tile sheet: " + regionName);
    }

    /** Draws {@code img} into the page at (destX,destY), flipping each cols x rows frame cell vertically IN PLACE - see the class doc's original note on why. */
    private static void drawFlippedPerCell(Graphics2D g, BufferedImage img, int destX, int destY,
                                            int cols, int rows) {
        int cellWidth = img.getWidth() / cols;
        int cellHeight = img.getHeight() / rows;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int sx = col * cellWidth;
                int sy = row * cellHeight;
                int dx = destX + sx;
                int dy = destY + sy;
                // Dest y-coordinates given bottom-then-top (reversed) flips this cell vertically.
                g.drawImage(img,
                        dx, dy + cellHeight, dx + cellWidth, dy,
                        sx, sy, sx + cellWidth, sy + cellHeight,
                        null);
            }
        }
    }

    private static BufferedImage buildTileSheet(List<LoadedAsset> loaded, TerrainTile tile) {
        BufferedImage sheet = new BufferedImage(TILE_SHEET_COLS * TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = sheet.createGraphics();
        String[] order = {tile.stoneRegion(), tile.chocolateRegion()};
        for (int i = 0; i < order.length; i++) {
            BufferedImage cell = findImageByRegionName(loaded, tile.theme(), order[i]);
            if (cell.getWidth() != TILE_SIZE || cell.getHeight() != TILE_SIZE) {
                throw new IllegalStateException("Tile sheet source must be " + TILE_SIZE + "x"
                        + TILE_SIZE + ": " + order[i]);
            }
            g.drawImage(cell, i * TILE_SIZE, 0, null);
        }
        g.dispose();
        return sheet;
    }
}

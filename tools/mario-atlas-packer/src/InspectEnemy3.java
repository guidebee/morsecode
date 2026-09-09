import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.imageio.ImageIO;

/** Read-only inspection of the approved staged enemy3 files; never writes images. */
public final class InspectEnemy3 {
    private static final File SOURCE = new File(
            "C:/workspace/morsecode/docs/mario/assets/mario-sprites/ampere-staging/enemy3-source");
    private static final String[] FILES = {
            "enemy3all.png", "enemy3attack-Sheet[32height32wide].png",
            "enemy3_attacking-Sheet[80height32wide].png", "enemy3bullet.png"
    };

    public static void main(String[] args) throws Exception {
        for (String name : FILES) {
            BufferedImage image = ImageIO.read(new File(SOURCE, name));
            if (image == null) throw new IllegalStateException("Cannot decode " + name);
            int w = image.getWidth(), h = image.getHeight();
            boolean[] occupied = new boolean[w * h];
            boolean[] columns = new boolean[w], rows = new boolean[h];
            int minX = w, minY = h, maxX = -1, maxY = -1, pixels = 0, partial = 0;
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int alpha = image.getRGB(x, y) >>> 24;
                    if (alpha == 0) continue;
                    occupied[y * w + x] = true;
                    columns[x] = rows[y] = true;
                    minX = Math.min(minX, x); minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x); maxY = Math.max(maxY, y);
                    pixels++;
                    if (alpha < 255) partial++;
                }
            }
            System.out.printf("%n%s: %dx%d alpha=%s occupied=%d partialAlpha=%d bbox=[%d,%d,%d,%d) %n",
                    name, w, h, image.getColorModel().hasAlpha(), pixels, partial,
                    minX, minY, maxX + 1, maxY + 1);
            System.out.println("Occupied column runs: " + runs(columns));
            System.out.println("Occupied row runs: " + runs(rows));
            boolean[] seen = new boolean[w * h];
            List<int[]> components = new ArrayList<>();
            for (int start = 0; start < occupied.length; start++) {
                if (!occupied[start] || seen[start]) continue;
                ArrayDeque<Integer> queue = new ArrayDeque<>();
                queue.add(start); seen[start] = true;
                int x0 = w, y0 = h, x1 = -1, y1 = -1, count = 0;
                while (!queue.isEmpty()) {
                    int p = queue.removeFirst(), x = p % w, y = p / w;
                    x0 = Math.min(x0, x); y0 = Math.min(y0, y);
                    x1 = Math.max(x1, x); y1 = Math.max(y1, y); count++;
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dx = -1; dx <= 1; dx++) {
                            int nx = x + dx, ny = y + dy;
                            if (nx < 0 || nx >= w || ny < 0 || ny >= h) continue;
                            int next = ny * w + nx;
                            if (occupied[next] && !seen[next]) {
                                seen[next] = true; queue.addLast(next);
                            }
                        }
                    }
                }
                components.add(new int[]{x0, y0, x1 + 1, y1 + 1, count});
            }
            components.sort(Comparator.<int[]>comparingInt(c -> c[1]).thenComparingInt(c -> c[0]));
            System.out.println("8-connected alpha components (not inferred animation frames):");
            for (int[] c : components) {
                System.out.printf("  [%d,%d,%d,%d) pixels=%d%n", c[0], c[1], c[2], c[3], c[4]);
            }
        }
    }

    private static String runs(boolean[] values) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (!values[i]) continue;
            int start = i;
            while (i + 1 < values.length && values[i + 1]) i++;
            if (result.length() > 0) result.append(", ");
            result.append('[').append(start).append(',').append(i + 1).append(')');
        }
        return result.toString();
    }
}

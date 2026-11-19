package rasterlab;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;

public class RasterAlgorithms {

    public static boolean LOG = true;

    public static AlgoResult step(int x1, int y1, int x2, int y2) {
        long t0 = System.nanoTime();
        List<Point> pts = new ArrayList<>();
        StringBuilder log = new StringBuilder();

        int dx = x2 - x1, dy = y2 - y1;

        if (dx == 0 && dy == 0) {
            pts.add(new Point(x1, y1));
            if (LOG) log.append("dx=0, dy=0 → одна точка\n");
            return new AlgoResult(pts, log.toString(), System.nanoTime() - t0);
        }

        if (LOG) log.append("dx=").append(dx).append(", dy=").append(dy).append('\n');

        if (Math.abs(dx) >= Math.abs(dy)) {
            double k = (double) dy / dx;
            if (LOG) log.append(String.format("|dx|>=|dy| → шаг по x, k=dy/dx=%.4f%n", k));
            int stepX = (x1 <= x2) ? 1 : -1;
            int x = x1;
            while (true) {
                double yf = y1 + k * (x - x1);
                int y = (int) Math.round(yf);
                pts.add(new Point(x, y));
                if (LOG && log.length() < 4000)
                    log.append(String.format("x=%d → y=%.2f → %d%n", x, yf, y));
                if (x == x2) break;
                x += stepX;
            }
        } else {
            double k = (double) dx / dy;
            if (LOG) log.append(String.format("|dy|>|dx| → шаг по y, k=dx/dy=%.4f%n", k));
            int stepY = (y1 <= y2) ? 1 : -1;
            int y = y1;
            while (true) {
                double xf = x1 + k * (y - y1);
                int x = (int) Math.round(xf);
                pts.add(new Point(x, y));
                if (LOG && log.length() < 4000)
                    log.append(String.format("y=%d → x=%.2f → %d%n", y, xf, x));
                if (y == y2) break;
                y += stepY;
            }
        }
        return new AlgoResult(pts, log.toString(), System.nanoTime() - t0);
    }

    public static AlgoResult dda(int x1, int y1, int x2, int y2) {
        long t0 = System.nanoTime();
        List<Point> pts = new ArrayList<>();
        StringBuilder log = new StringBuilder();

        int dx = x2 - x1, dy = y2 - y1;
        int steps = Math.max(Math.abs(dx), Math.abs(dy));
        if (LOG) log.append(String.format("dx=%d, dy=%d, steps=max(|dx|,|dy|)=%d%n", dx, dy, steps));

        if (steps == 0) {
            pts.add(new Point(x1, y1));
            return new AlgoResult(pts, log.toString(), System.nanoTime() - t0);
        }
        double xi = (double) dx / steps, yi = (double) dy / steps;
        if (LOG) log.append(String.format("xi=%.4f, yi=%.4f%n", xi, yi));

        double x = x1, y = y1;
        for (int i = 0; i <= steps; i++) {
            int px = (int) Math.round(x), py = (int) Math.round(y);
            pts.add(new Point(px, py));
            if (LOG && log.length() < 4000)
                log.append(String.format("i=%d: (%.3f, %.3f) → (%d, %d)%n", i, x, y, px, py));
            x += xi;
            y += yi;
        }
        return new AlgoResult(pts, log.toString(), System.nanoTime() - t0);
    }

    public static AlgoResult bresenhamLine(int x1, int y1, int x2, int y2) {
        long t0 = System.nanoTime();
        List<Point> pts = new ArrayList<>();
        StringBuilder log = new StringBuilder();

        int x = x1, y = y1;
        int dx = Math.abs(x2 - x1), dy = Math.abs(y2 - y1);
        int sx = (x1 < x2) ? 1 : -1;
        int sy = (y1 < y2) ? 1 : -1;
        int err = dx - dy;

        if (LOG) log.append(String.format("dx=%d, dy=%d, sx=%d, sy=%d, err0=%d%n", dx, dy, sx, sy, err));
        while (true) {
            pts.add(new Point(x, y));
            if (LOG && log.length() < 4000)
                log.append(String.format("pixel (%d,%d), err=%d%n", x, y, err));
            if (x == x2 && y == y2) break;
            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; x += sx; }
            if (e2 < dx) { err += dx; y += sy; }
        }
        return new AlgoResult(pts, log.toString(), System.nanoTime() - t0);
    }

    public static AlgoResult bresenhamCircle(int cx, int cy, int R) {
        long t0 = System.nanoTime();
        List<Point> pts = new ArrayList<>();
        StringBuilder log = new StringBuilder();

        int x = 0, y = R, d = 3 - 2 * R;
        if (LOG) log.append(String.format("R=%d, x=0, y=R, d0=3-2R=%d%n", R, d));

        while (x <= y) {
            add8(pts, cx, cy, x, y);
            if (LOG && log.length() < 4000)
                log.append(String.format("x=%d, y=%d, d=%d → 8 точек%n", x, y, d));
            if (d < 0) d += 4 * x + 6;
            else { d += 4 * (x - y) + 10; y--; }
            x++;
        }

        long elapsed = System.nanoTime() - t0;

        LinkedHashSet<Point> uniq = new LinkedHashSet<>(pts);
        List<Point> result = new ArrayList<>(uniq);
        result.sort(Comparator.comparingDouble(p -> Math.atan2(p.y - cy, p.x - cx)));

        return new AlgoResult(result, log.toString(), elapsed);
    }

    private static void add8(List<Point> pts, int cx, int cy, int x, int y) {
        pts.add(new Point(cx + x, cy + y));
        pts.add(new Point(cx - x, cy + y));
        pts.add(new Point(cx + x, cy - y));
        pts.add(new Point(cx - x, cy - y));
        pts.add(new Point(cx + y, cy + x));
        pts.add(new Point(cx - y, cy + x));
        pts.add(new Point(cx + y, cy - x));
        pts.add(new Point(cx - y, cy - x));
    }
}
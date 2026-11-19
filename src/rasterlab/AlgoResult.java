package rasterlab;
import java.awt.Point;
import java.util.List;

public class AlgoResult {
    public final List<Point> pixels;
    public final String log;
    public final long nanos;
    public final int count;

    public AlgoResult(List<Point> pixels, String log, long nanos) {
        this.pixels = pixels;
        this.log = log;
        this.nanos = nanos;
        this.count = pixels.size();
    }

    public String timeMs() {
        return String.format("%.4f мс", nanos / 1_000_000.0);
    }
}
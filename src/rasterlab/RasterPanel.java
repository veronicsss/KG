package rasterlab;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class RasterPanel extends JPanel {

    private int scale = 24;
    private boolean showGrid = true;
    private boolean showLabels = true;
    private boolean showPixels = true;

    private List<Point> pixels;
    private Color pixelColor = new Color(0x81, 0xC7, 0x84);

    public RasterPanel() {
        setBackground(new Color(0x11, 0x11, 0x1E));
    }

    public void setScale(int s) { this.scale = s; repaint(); }
    public void setShowGrid(boolean v) { showGrid = v; repaint(); }
    public void setShowLabels(boolean v) { showLabels = v; repaint(); }
    public void setShowPixels(boolean v) { showPixels = v; repaint(); }
    public void setPixels(List<Point> px, Color c) {
        this.pixels = px; this.pixelColor = c; repaint();
    }
    public void clearPixels() { pixels = null; repaint(); }

    public Point worldToScreen(int x, int y) {
        int cx = getWidth() / 2, cy = getHeight() / 2;
        return new Point(cx + x * scale, cy - y * scale);
    }

    public Point screenToWorld(int sx, int sy) {
        int cx = getWidth() / 2, cy = getHeight() / 2;
        int x = Math.round((sx - cx) / (float) scale);
        int y = Math.round((cy - sy) / (float) scale);
        return new Point(x, y);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        int W = getWidth(), H = getHeight();
        int cx = W / 2, cy = H / 2;

        if (showGrid) {
            g2.setColor(new Color(0x26, 0x26, 0x3A));
            int maxX = (int) Math.ceil(cx / (double) scale);
            int maxY = (int) Math.ceil(cy / (double) scale);
            for (int x = -maxX; x <= maxX; x++) {
                int sx = cx + x * scale;
                g2.drawLine(sx, 0, sx, H);
            }
            for (int y = -maxY; y <= maxY; y++) {
                int sy = cy - y * scale;
                g2.drawLine(0, sy, W, sy);
            }
        }

        g2.setColor(new Color(0x7A, 0x7A, 0x9A));
        g2.drawLine(0, cy, W, cy);
        g2.drawLine(cx, 0, cx, H);

        if (showLabels) {
            g2.setColor(new Color(0x9A, 0xA0, 0xA0));
            g2.setFont(new Font("Consolas", Font.PLAIN, 10));
            FontMetrics fm = g2.getFontMetrics();

            int maxX = (int) Math.ceil(cx / (double) scale);
            int stepX = Math.max(1, (int) Math.round(60.0 / scale));
            for (int x = -maxX; x <= maxX; x++) {
                if (x == 0) continue;
                if (x % stepX != 0 && scale < 30) continue;
                String s = String.valueOf(x);
                int tw = fm.stringWidth(s);
                g2.drawString(s, cx + x * scale - tw / 2, cy + 14);
            }

            int maxY = (int) Math.ceil(cy / (double) scale);
            int stepY = Math.max(1, (int) Math.round(40.0 / scale));
            for (int y = -maxY; y <= maxY; y++) {
                if (y == 0) continue;
                if (y % stepY != 0 && scale < 30) continue;
                String s = String.valueOf(y);
                int tw = fm.stringWidth(s);
                g2.drawString(s, cx - tw - 4, cy - y * scale + 4);
            }

            g2.setColor(new Color(0xCC, 0xCC, 0xCC));
            g2.setFont(new Font("Consolas", Font.BOLD, 12));
            g2.drawString("X", W - 16, cy - 8);
            g2.drawString("Y", cx + 6, 14);
            g2.drawString("0", cx + 4, cy + 14);
        }

        if (showPixels && pixels != null && !pixels.isEmpty()) {
            if (pixels.size() > 1) {
                g2.setColor(new Color(pixelColor.getRed(), pixelColor.getGreen(),
                        pixelColor.getBlue(), 140));
                g2.setStroke(new BasicStroke(1f));
                Point prev = worldToScreen(pixels.get(0).x, pixels.get(0).y);
                for (int i = 1; i < pixels.size(); i++) {
                    Point cur = worldToScreen(pixels.get(i).x, pixels.get(i).y);
                    g2.drawLine(prev.x, prev.y, cur.x, cur.y);
                    prev = cur;
                }
            }
            g2.setColor(pixelColor);
            int half = scale / 2;
            for (Point p : pixels) {
                Point s = worldToScreen(p.x, p.y);
                g2.fillRect(s.x - half, s.y - half, scale, scale);
            }
        }
    }
}
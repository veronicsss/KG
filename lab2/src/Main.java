import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Main extends JFrame {

    private BufferedImage original;

    private final JLabel originalLabel = new JLabel("Оригинал", SwingConstants.CENTER);
    private final JLabel result1Label = new JLabel("Результат 1", SwingConstants.CENTER);
    private final JLabel result2Label = new JLabel("Результат 2", SwingConstants.CENTER);

    private static final int PANEL_W = 400;
    private static final int PANEL_H = 400;

    public Main() {
        super("Лабораторная работа 2. Вариант 8");

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel imagesPanel = new JPanel(new GridLayout(1, 3, 5, 5));

        for (JLabel label : new JLabel[]{originalLabel, result1Label, result2Label}) {
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setVerticalTextPosition(SwingConstants.BOTTOM);
            label.setHorizontalTextPosition(SwingConstants.CENTER);
            label.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            label.setPreferredSize(new Dimension(PANEL_W, PANEL_H));
            imagesPanel.add(label);
        }

        add(imagesPanel, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel();

        JButton loadButton = new JButton("Загрузить изображение");
        JButton sharpenButton = new JButton("Высокочастотный фильтр (резкость)");
        JButton thresholdButton = new JButton("Глобальная пороговая обработка (Otsu + среднее)");
        JButton resetButton = new JButton("Сброс");

        buttonsPanel.add(loadButton);
        buttonsPanel.add(sharpenButton);
        buttonsPanel.add(thresholdButton);
        buttonsPanel.add(resetButton);

        add(buttonsPanel, BorderLayout.SOUTH);

        loadButton.addActionListener(e -> loadImage());

        sharpenButton.addActionListener(e -> {
            if (original == null) return;
            result1Label.setText("ВЧ-фильтр 1 (крест, центр 5)");
            result1Label.setIcon(scaleIcon(sharpen(original, KERNEL_CROSS)));
            result2Label.setText("ВЧ-фильтр 2 (8 соседей, центр 9)");
            result2Label.setIcon(scaleIcon(sharpen(original, KERNEL_FULL)));
        });

        thresholdButton.addActionListener(e -> {
            if (original == null) return;

            int[] gray = toGrayArray(original);
            int w = original.getWidth();
            int h = original.getHeight();

            int otsuT = otsuThreshold(gray);
            int meanT = meanThreshold(gray);

            BufferedImage otsuImg = binaryFromGray(gray, w, h, otsuT, true);
            BufferedImage meanImg = binaryFromGray(gray, w, h, meanT, false);

            result1Label.setText("Otsu (T=" + otsuT + ")");
            result1Label.setIcon(scaleIcon(otsuImg));
            result2Label.setText("Среднее (T=" + meanT + ")");
            result2Label.setIcon(scaleIcon(meanImg));
        });

        resetButton.addActionListener(e -> {
            if (original == null) return;
            result1Label.setText("Результат 1");
            result1Label.setIcon(null);
            result2Label.setText("Результат 2");
            result2Label.setIcon(null);
        });

        pack();
        setLocationRelativeTo(null);
    }

    private void loadImage() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                original = ImageIO.read(chooser.getSelectedFile());
                if (original != null) {
                    originalLabel.setText("Оригинал");
                    originalLabel.setIcon(scaleIcon(original));
                    result1Label.setText("Результат 1");
                    result1Label.setIcon(null);
                    result2Label.setText("Результат 2");
                    result2Label.setIcon(null);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Ошибка чтения файла: " + ex.getMessage());
            }
        }
    }

    private ImageIcon scaleIcon(BufferedImage img) {
        if (img == null) return null;
        double scale = Math.min((double) PANEL_W / img.getWidth(), (double) PANEL_H / img.getHeight());
        int w = Math.max(1, (int) (img.getWidth() * scale));
        int h = Math.max(1, (int) (img.getHeight() * scale));
        return new ImageIcon(img.getScaledInstance(w, h, Image.SCALE_SMOOTH));
    }
    private static final int[][] KERNEL_CROSS = {
            {0, -1, 0},
            {-1, 5, -1},
            {0, -1, 0}
    };

    private static final int[][] KERNEL_FULL = {
            {-1, -1, -1},
            {-1, 9, -1},
            {-1, -1, -1}
    };

    private BufferedImage sharpen(BufferedImage src, int[][] kernel) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int r = 0, g = 0, b = 0;

                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        int xx = Math.min(w - 1, Math.max(0, x + kx));
                        int yy = Math.min(h - 1, Math.max(0, y + ky));
                        int rgb = src.getRGB(xx, yy);
                        int k = kernel[ky + 1][kx + 1];

                        r += k * ((rgb >> 16) & 0xFF);
                        g += k * ((rgb >> 8) & 0xFF);
                        b += k * (rgb & 0xFF);
                    }
                }

                out.setRGB(x, y, (clamp(r) << 16) | (clamp(g) << 8) | clamp(b));
            }
        }
        return out;
    }
    private int otsuThreshold(int[] gray) {
        int[] hist = new int[256];
        for (int v : gray) hist[v]++;

        int total = gray.length;
        double sum = 0;
        for (int i = 0; i < 256; i++) sum += i * (double) hist[i];

        double sumB = 0;
        long wB = 0;
        double maxVar = -1;
        int threshold = 0;

        for (int t = 0; t < 256; t++) {
            wB += hist[t];
            if (wB == 0) continue;

            long wF = total - wB;
            if (wF == 0) break;

            sumB += t * (double) hist[t];

            double mB = sumB / wB;
            double mF = (sum - sumB) / wF;
            double between = (double) wB * wF * (mB - mF) * (mB - mF);

            if (between > maxVar) {
                maxVar = between;
                threshold = t;
            }
        }
        return threshold;
    }
    private int meanThreshold(int[] gray) {
        long sum = 0;
        for (int v : gray) sum += v;
        return (int) (sum / gray.length);
    }

    private int[] toGrayArray(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        int[] gray = new int[w * h];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = src.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                gray[y * w + x] = (int) (0.299 * r + 0.587 * g + 0.114 * b);
            }
        }
        return gray;
    }
    private BufferedImage binaryFromGray(int[] gray, int w, int h, int threshold, boolean strict) {
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int v = gray[y * w + x];
                boolean white = strict ? v > threshold : v >= threshold;
                out.setRGB(x, y, white ? 0xFFFFFF : 0x000000);
            }
        }
        return out;
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ColorLab extends JFrame {

    private static final int RGB = 0, CMYK = 1, HSV = 2;

    private int r = 128, g = 64, b = 192;
    private boolean updating = false; // защита от рекурсии

    private Channel[] rgbCh, cmykCh, hsvCh;

    private JPanel colorPreview, palettePanel;
    private JLabel rgbLabel, cmykLabel, hsvLabel;

    private class Channel {
        final JSlider slider;
        final JSpinner spinner;
        final JTextField field;
        final int min, max, model;
        final boolean hex;

        Channel(int model, int min, int max, boolean hex) {
            this.model = model; this.min = min; this.max = max; this.hex = hex;
            slider = new JSlider(min, max, min);
            spinner = new JSpinner(new SpinnerNumberModel(min, min, max, 1));
            field = new JTextField(4);
            field.setHorizontalAlignment(JTextField.CENTER);
            field.setToolTipText(hex ? "HEX: 00..FF" : "Число " + min + ".." + max);

            slider.addChangeListener(e -> { if (!updating) edit(slider.getValue()); });
            spinner.addChangeListener(e -> { if (!updating) edit((Integer) spinner.getValue()); });
            field.addActionListener(e -> commitField());
            field.addFocusListener(new FocusAdapter() {
                @Override public void focusLost(FocusEvent e) { commitField(); }
            });
        }

        void set(int v) {
            slider.setValue(v);
            spinner.setValue(v);
            field.setText(hex ? String.format("%02X", v) : String.valueOf(v));
        }

        int get() { return slider.getValue(); }


        void edit(int v) {
            updating = true;
            set(v);
            updating = false;
            updateFrom(model);
        }

        void commitField() {
            if (updating) return;
            try {
                int v = Integer.parseInt(field.getText().trim(), hex ? 16 : 10);
                if (v < min || v > max) throw new NumberFormatException();
                if (v != get()) edit(v);
                else { updating = true; set(v); updating = false; } // нормализуем текст
            } catch (NumberFormatException ex) {
                Toolkit.getDefaultToolkit().beep();
                updating = true; set(get()); updating = false;      // откат
            }
        }
    }

    public ColorLab() {
        super("Лабораторная работа №1-- Цветовые модели CMYK / RGB / HSV");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));


        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        colorPreview = new JPanel();
        colorPreview.setPreferredSize(new Dimension(200, 100));
        JButton chooseBtn = new JButton("Выбрать цвет (палитра)...");
        chooseBtn.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "Выбор цвета", new Color(r, g, b));
            if (c != null) { r = c.getRed(); g = c.getGreen(); b = c.getBlue(); updateFrom(-1); }
        });
        JPanel left = new JPanel(new BorderLayout(0, 5));
        left.add(colorPreview, BorderLayout.CENTER);
        left.add(chooseBtn, BorderLayout.SOUTH);

        palettePanel = new JPanel(new GridLayout(4, 8, 2, 2));
        palettePanel.setBorder(new TitledBorder("Палитра (нажмите на цвет)"));
        buildPalette();

        topPanel.add(left, BorderLayout.WEST);
        topPanel.add(palettePanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);


        JPanel modelsPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        modelsPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        rgbCh = new Channel[]{ new Channel(RGB, 0, 255, true),
                new Channel(RGB, 0, 255, true),
                new Channel(RGB, 0, 255, true) };
        cmykCh = new Channel[]{ new Channel(CMYK, 0, 100, false), new Channel(CMYK, 0, 100, false),
                new Channel(CMYK, 0, 100, false), new Channel(CMYK, 0, 100, false) };
        hsvCh = new Channel[]{ new Channel(HSV, 0, 360, false),
                new Channel(HSV, 0, 100, false),
                new Channel(HSV, 0, 100, false) };

        modelsPanel.add(buildPanel("RGB (0..255, поле — HEX)", rgbCh,
                new String[]{"R (красный)", "G (зелёный)", "B (синий)"}));
        modelsPanel.add(buildPanel("CMYK (0..100 %)", cmykCh,
                new String[]{"C (голубой)", "M (пурпурный)", "Y (жёлтый)", "K (чёрный)"}));
        modelsPanel.add(buildPanel("HSV (H 0..360°, S,V 0..100 %)", hsvCh,
                new String[]{"H (тон)", "S (насыщ.)", "V (значение)"}));
        add(modelsPanel, BorderLayout.CENTER);


        JPanel status = new JPanel(new GridLayout(3, 1));
        status.setBorder(BorderFactory.createTitledBorder("Текущие значения"));
        rgbLabel = new JLabel(); cmykLabel = new JLabel(); hsvLabel = new JLabel();
        status.add(rgbLabel); status.add(cmykLabel); status.add(hsvLabel);
        add(status, BorderLayout.SOUTH);

        updateFrom(-1);

        setSize(1150, 600);
        setLocationRelativeTo(null);
        setVisible(true);
    }





    private JPanel buildPanel(String title, Channel[] chs, String[] names) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(new TitledBorder(title));
        for (int i = 0; i < chs.length; i++) {
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(4, 4, 4, 4);
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridy = i;
            c.gridx = 0; c.weightx = 0; p.add(new JLabel(names[i]), c);
            c.gridx = 1; c.weightx = 1; p.add(chs[i].slider, c);
            c.gridx = 2; c.weightx = 0; p.add(chs[i].spinner, c);
            c.gridx = 3; c.weightx = 0; p.add(chs[i].field, c);
        }

        GridBagConstraints filler = new GridBagConstraints();
        filler.gridy = chs.length; filler.weighty = 1;
        p.add(Box.createVerticalGlue(), filler);
        return p;
    }

    private void buildPalette() {
        Color[] colors = {
                Color.BLACK, Color.DARK_GRAY, Color.GRAY, Color.LIGHT_GRAY, Color.WHITE,
                new Color(255, 0, 0), new Color(0, 255, 0), new Color(0, 0, 255),
                new Color(255, 255, 0), new Color(255, 0, 255), new Color(0, 255, 255),
                new Color(255, 128, 0), new Color(128, 0, 255), new Color(0, 128, 255),
                new Color(255, 0, 128), new Color(0, 255, 128), new Color(128, 255, 0),
                new Color(64, 64, 64), new Color(128, 128, 128), new Color(192, 192, 192),
                new Color(128, 0, 0), new Color(0, 128, 0), new Color(0, 0, 128),
                new Color(128, 128, 0), new Color(128, 0, 128), new Color(0, 128, 128),
                new Color(255, 192, 128), new Color(192, 128, 255), new Color(128, 192, 255),
                new Color(255, 200, 200), new Color(200, 255, 200), new Color(200, 200, 255)
        };
        for (Color col : colors) {
            JPanel sw = new JPanel();
            sw.setBackground(col);
            sw.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
            sw.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            sw.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    r = col.getRed(); g = col.getGreen(); b = col.getBlue();
                    updateFrom(-1);
                }
            });
            palettePanel.add(sw);
        }
    }



    private void updateFrom(int source) {
        switch (source) {
            case RGB:
                r = rgbCh[0].get(); g = rgbCh[1].get(); b = rgbCh[2].get();
                break;
            case CMYK: {
                int[] rgb = cmykToRgb(cmykCh[0].get() / 100.0, cmykCh[1].get() / 100.0,
                        cmykCh[2].get() / 100.0, cmykCh[3].get() / 100.0);
                r = rgb[0]; g = rgb[1]; b = rgb[2];
                break;
            }
            case HSV: {
                int[] rgb = hsvToRgb(hsvCh[0].get(), hsvCh[1].get() / 100.0, hsvCh[2].get() / 100.0);
                r = rgb[0]; g = rgb[1]; b = rgb[2];
                break;
            }
            default: break;
        }

        updating = true;

        if (source != RGB) {
            rgbCh[0].set(r); rgbCh[1].set(g); rgbCh[2].set(b);
        }
        if (source != CMYK) {
            double[] cmyk = rgbToCmyk(r, g, b);
            for (int i = 0; i < 4; i++) cmykCh[i].set((int) Math.round(cmyk[i] * 100));
        }
        if (source != HSV) {
            double[] hsv = rgbToHsv(r, g, b);
            int h = (int) Math.round(hsv[0]);
            int s = (int) Math.round(hsv[1] * 100);
            int v = (int) Math.round(hsv[2] * 100);
            // у серых/чёрных цветов H (и S) не определены
            if (v == 0) { h = hsvCh[0].get(); s = hsvCh[1].get(); }
            else if (s == 0) { h = hsvCh[0].get(); }
            hsvCh[0].set(h); hsvCh[1].set(s); hsvCh[2].set(v);
        }

        updating = false;
        refreshVisuals();
    }

    private void refreshVisuals() {
        colorPreview.setBackground(new Color(r, g, b));
        colorPreview.setBorder(BorderFactory.createLineBorder(
                (r + g + b > 382) ? Color.BLACK : Color.WHITE, 3));

        rgbLabel.setText(String.format("RGB:  R=%d, G=%d, B=%d   (#%02X%02X%02X)",
                r, g, b, r, g, b));
        cmykLabel.setText(String.format("CMYK: C=%d%%, M=%d%%, Y=%d%%, K=%d%%",
                cmykCh[0].get(), cmykCh[1].get(), cmykCh[2].get(), cmykCh[3].get()));
        hsvLabel.setText(String.format("HSV:  H=%d°, S=%d%%, V=%d%%",
                hsvCh[0].get(), hsvCh[1].get(), hsvCh[2].get()));
    }




    public static double[] rgbToCmyk(int r, int g, int b) {
        double rr = r / 255.0, gg = g / 255.0, bb = b / 255.0;
        double k = 1.0 - Math.max(rr, Math.max(gg, bb));
        double c, m, y;
        if (k >= 1.0 - 1e-9) {
            c = 0; m = 0; y = 0;
        } else {
            c = (1 - rr - k) / (1 - k);
            m = (1 - gg - k) / (1 - k);
            y = (1 - bb - k) / (1 - k);
        }
        return new double[]{ c, m, y, k };
    }


    public static int[] cmykToRgb(double c, double m, double y, double k) {
        return new int[]{
                clamp((int) Math.round(255 * (1 - c) * (1 - k))),
                clamp((int) Math.round(255 * (1 - m) * (1 - k))),
                clamp((int) Math.round(255 * (1 - y) * (1 - k)))
        };
    }


    public static double[] rgbToHsv(int r, int g, int b) {
        double rr = r / 255.0, gg = g / 255.0, bb = b / 255.0;
        double max = Math.max(rr, Math.max(gg, bb));
        double min = Math.min(rr, Math.min(gg, bb));
        double d = max - min;

        double h;
        if (d == 0)          h = 0;
        else if (max == rr)  h = 60 * (((gg - bb) / d) % 6);
        else if (max == gg)  h = 60 * (((bb - rr) / d) + 2);
        else                 h = 60 * (((rr - gg) / d) + 4);
        if (h < 0) h += 360;

        double s = (max == 0) ? 0 : (d / max);
        return new double[]{ h, s, max };
    }

    public static int[] hsvToRgb(double h, double s, double v) {
        h = ((h % 360) + 360) % 360;
        double c = v * s;
        double x = c * (1 - Math.abs(((h / 60.0) % 2) - 1));
        double m = v - c;
        double rr, gg, bb;
        if (h < 60)       { rr = c; gg = x; bb = 0; }
        else if (h < 120) { rr = x; gg = c; bb = 0; }
        else if (h < 180) { rr = 0; gg = c; bb = x; }
        else if (h < 240) { rr = 0; gg = x; bb = c; }
        else if (h < 300) { rr = x; gg = 0; bb = c; }
        else              { rr = c; gg = 0; bb = x; }
        return new int[]{
                clamp((int) Math.round((rr + m) * 255)),
                clamp((int) Math.round((gg + m) * 255)),
                clamp((int) Math.round((bb + m) * 255))
        };
    }

    private static int clamp(int v) { return Math.max(0, Math.min(255, v)); }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ColorLab::new);
    }
}
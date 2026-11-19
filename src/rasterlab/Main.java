package rasterlab;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class Main extends JFrame {

    private final RasterPanel canvas = new RasterPanel();
    private final JTextArea info = new JTextArea(12, 20);
    private final JLabel timing = new JLabel("—");

    private final JTextField x1Field = new JTextField("-8", 4);
    private final JTextField y1Field = new JTextField("-3", 4);
    private final JTextField x2Field = new JTextField("9", 4);
    private final JTextField y2Field = new JTextField("7", 4);

    private final JTextField cxField = new JTextField("0", 3);
    private final JTextField cyField = new JTextField("0", 3);
    private final JTextField rField  = new JTextField("8", 3);

    private final JSpinner scaleSpinner = new JSpinner(new SpinnerNumberModel(24, 4, 80, 1));

    private String currentAlgo = "step";

    public Main() {
        super("Лабораторная 3 — Базовые растровые алгоритмы");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel west = new JPanel();
        west.setLayout(new BoxLayout(west, BoxLayout.Y_AXIS));
        west.setPreferredSize(new Dimension(330, 0));
        west.setBackground(new Color(0x25, 0x25, 0x35));
        west.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel tabs = new JPanel(new GridLayout(2, 2, 4, 4));
        tabs.setOpaque(false);
        String[][] algoDefs = {
                {"Пошаговый", "step"},
                {"ЦДА", "dda"},
                {"Брезенхем", "bres"},
                {"Окружность", "circle"}
        };
        ButtonGroup bg = new ButtonGroup();
        for (String[] def : algoDefs) {
            JToggleButton btn = new JToggleButton(def[0]);
            btn.setActionCommand(def[1]);
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            btn.setFocusPainted(false);
            if (def[1].equals(currentAlgo)) btn.setSelected(true);
            btn.addActionListener(e -> {
                currentAlgo = btn.getActionCommand();
                build();
            });
            bg.add(btn);
            tabs.add(btn);
        }
        west.add(tabs);
        west.add(Box.createVerticalStrut(10));

        JPanel lineGrp = new JPanel(new GridLayout(2, 4, 4, 2));
        lineGrp.setBorder(new TitledBorder("Отрезок (x1,y1) → (x2,y2)"));
        lineGrp.add(new JLabel("x1")); lineGrp.add(x1Field);
        lineGrp.add(new JLabel("y1")); lineGrp.add(y1Field);
        lineGrp.add(new JLabel("x2")); lineGrp.add(x2Field);
        lineGrp.add(new JLabel("y2")); lineGrp.add(y2Field);
        west.add(lineGrp);
        west.add(Box.createVerticalStrut(8));

        JPanel circGrp = new JPanel(new GridLayout(1, 6, 4, 2));
        circGrp.setBorder(new TitledBorder("Окружность"));
        circGrp.add(new JLabel("cx")); circGrp.add(cxField);
        circGrp.add(new JLabel("cy")); circGrp.add(cyField);
        circGrp.add(new JLabel("R"));  circGrp.add(rField);
        west.add(circGrp);
        west.add(Box.createVerticalStrut(8));

        JPanel viewGrp = new JPanel(new GridLayout(0, 2, 4, 2));
        viewGrp.setBorder(new TitledBorder("Масштаб / вид"));
        viewGrp.add(new JLabel("Масштаб:"));
        viewGrp.add(scaleSpinner);
        JCheckBox cbGrid = new JCheckBox("сетка", true);
        JCheckBox cbLabels = new JCheckBox("подписи", true);
        JCheckBox cbPixels = new JCheckBox("пиксели", true);
        cbGrid.setOpaque(false);
        cbLabels.setOpaque(false);
        cbPixels.setOpaque(false);
        cbGrid.addActionListener(e -> canvas.setShowGrid(cbGrid.isSelected()));
        cbLabels.addActionListener(e -> canvas.setShowLabels(cbLabels.isSelected()));
        cbPixels.addActionListener(e -> canvas.setShowPixels(cbPixels.isSelected()));
        viewGrp.add(cbGrid); viewGrp.add(cbLabels); viewGrp.add(cbPixels);
        west.add(viewGrp);
        west.add(Box.createVerticalStrut(8));

        scaleSpinner.addChangeListener(e ->
                canvas.setScale((Integer) scaleSpinner.getValue()));

        JButton buildBtn = new JButton("Построить");
        JButton clearBtn = new JButton("Очистить");
        buildBtn.addActionListener(e -> build());
        clearBtn.addActionListener(e -> {
            canvas.clearPixels();
            info.setText("Очищено.");
            timing.setText("—");
        });
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 4, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(buildBtn);
        btnPanel.add(clearBtn);
        west.add(btnPanel);
        west.add(Box.createVerticalStrut(10));

        info.setEditable(false);
        info.setFont(new Font("Consolas", Font.PLAIN, 11));
        info.setBackground(new Color(0x1A, 0x1A, 0x28));
        info.setForeground(new Color(0xCC, 0xCC, 0xCC));
        JScrollPane infoScroll = new JScrollPane(info);
        infoScroll.setBorder(new TitledBorder("Пошаговые вычисления"));
        infoScroll.setPreferredSize(new Dimension(300, 220));
        west.add(infoScroll);
        west.add(Box.createVerticalStrut(6));

        timing.setForeground(new Color(0x9F, 0xD3, 0x9F));
        timing.setFont(new Font("Consolas", Font.PLAIN, 12));
        west.add(timing);
        west.add(Box.createVerticalStrut(6));

        JLabel hint = new JLabel("<html>Пиксель (x, y) — квадрат с центром в узле сетки (x, y). "
                + "Целые координаты лежат на пересечениях линий сетки; клик мышью выбирает ближайший узел.</html>");
        hint.setForeground(new Color(0xCC, 0xCC, 0xCC));
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        west.add(hint);

        JScrollPane westScroll = new JScrollPane(west);
        westScroll.setBorder(null);
        add(westScroll, BorderLayout.WEST);
        add(canvas, BorderLayout.CENTER);

        canvas.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                Point w = canvas.screenToWorld(e.getX(), e.getY());
                if (currentAlgo.equals("circle")) {
                    cxField.setText(String.valueOf(w.x));
                    cyField.setText(String.valueOf(w.y));
                } else {
                    x1Field.setText(String.valueOf(w.x));
                    y1Field.setText(String.valueOf(w.y));
                }
                build();
            }
        });

        setSize(1100, 720);
        setLocationRelativeTo(null);
        build();
    }

    private void build() {
        int x1 = parseInt(x1Field, 0);
        int y1 = parseInt(y1Field, 0);
        int x2 = parseInt(x2Field, 0);
        int y2 = parseInt(y2Field, 0);
        int cx = parseInt(cxField, 0);
        int cy = parseInt(cyField, 0);
        int R  = Math.max(1, parseInt(rField, 1));

        AlgoResult res;
        Color color;

        switch (currentAlgo) {
            case "step":
                res = RasterAlgorithms.step(x1, y1, x2, y2);
                color = new Color(0xFF, 0x6B, 0x6B);
                break;
            case "dda":
                res = RasterAlgorithms.dda(x1, y1, x2, y2);
                color = new Color(0x4D, 0xD0, 0xE1);
                break;
            case "bres":
                res = RasterAlgorithms.bresenhamLine(x1, y1, x2, y2);
                color = new Color(0x81, 0xC7, 0x84);
                break;
            case "circle":
            default:
                res = RasterAlgorithms.bresenhamCircle(cx, cy, R);
                color = new Color(0xFF, 0xB7, 0x4D);
                break;
        }

        canvas.setPixels(res.pixels, color);
        timing.setText("Время: " + res.timeMs() + "  ·  пикселей: " + res.count);
        info.setText("Алгоритм: " + currentAlgo + "\n" + res.log);
        info.setCaretPosition(0);
    }

    private int parseInt(JTextField f, int def) {
        try { return Integer.parseInt(f.getText().trim()); }
        catch (NumberFormatException e) { f.setText(String.valueOf(def)); return def; }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}
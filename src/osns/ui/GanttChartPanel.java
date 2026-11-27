package osns.ui;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import osns.model.GanttBlock;

/**
 * Draws the Gantt chart on a time scale. Bar width is proportional to its
 * duration; idle time is shown as a grey "Idle" bar.
 * Group OSNS - shared UI class.
 */
public class GanttChartPanel extends JPanel {
    private static final Color[] COLORS = {
        new Color(0x4E79A7), new Color(0xF28E2B), new Color(0x59A14F), new Color(0xE15759),
        new Color(0x76B7B2), new Color(0xEDC948), new Color(0xB07AA1), new Color(0xFF9DA7),
        new Color(0x9C755F), new Color(0x86BCB6)
    };
    private static final int LEFT = 20, TOP = 20, BAR_HEIGHT = 50;

    private List<GanttBlock> blocks = new ArrayList<GanttBlock>();
    private int unit = 30;   // pixels per time unit

    public GanttChartPanel() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(600, 110));
    }

    public void setBlocks(List<GanttBlock> blocks) {
        this.blocks = blocks;
        int total = blocks.isEmpty() ? 0 : blocks.get(blocks.size() - 1).end;
        unit = total <= 0 ? 30 : Math.max(10, Math.min(40, 1000 / total));
        setPreferredSize(new Dimension(LEFT * 2 + total * unit + 20, 110));
        revalidate();
        repaint();
    }

    public void clear() {
        setBlocks(new ArrayList<GanttBlock>());
    }

    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (blocks.isEmpty()) {
            g.setColor(Color.GRAY);
            g.drawString("Gantt chart will appear here after Compute.", LEFT, TOP + 30);
            return;
        }
        g.setFont(getFont().deriveFont(Font.BOLD, 13f));
        FontMetrics fm = g.getFontMetrics();
        for (GanttBlock b : blocks) {
            int x = LEFT + b.start * unit;
            int w = (b.end - b.start) * unit;
            g.setColor(b.isIdle() ? new Color(0xDDDDDD) : colorFor(b.label));
            g.fillRect(x, TOP, w, BAR_HEIGHT);
            g.setColor(Color.DARK_GRAY);
            g.drawRect(x, TOP, w, BAR_HEIGHT);
            String text = b.label;
            if (fm.stringWidth(text) < w - 2) {
                g.setColor(b.isIdle() ? Color.DARK_GRAY : Color.WHITE);
                g.drawString(text, x + (w - fm.stringWidth(text)) / 2, TOP + BAR_HEIGHT / 2 + fm.getAscent() / 2 - 2);
            }
            // Time label at the start of every bar
            g.setColor(Color.BLACK);
            String t = String.valueOf(b.start);
            g.drawString(t, x - fm.stringWidth(t) / 2, TOP + BAR_HEIGHT + 18);
        }
        GanttBlock last = blocks.get(blocks.size() - 1);
        String t = String.valueOf(last.end);
        g.drawString(t, LEFT + last.end * unit - fm.stringWidth(t) / 2, TOP + BAR_HEIGHT + 18);
    }

    private Color colorFor(String pid) {
        int n = Integer.parseInt(pid.substring(1));
        return COLORS[(n - 1) % COLORS.length];
    }
}
